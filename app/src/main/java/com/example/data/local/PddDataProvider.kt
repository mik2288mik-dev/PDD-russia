package com.example.data.local

import android.content.Context
import com.example.data.entity.toEntity
import com.example.data.model.PddQuestion
import com.example.data.model.PddRuleSection
import com.example.data.model.TrafficSign
import org.json.JSONArray
import org.json.JSONObject

object PddDataProvider {

    @Volatile
    private var cachedQuestions: MutableMap<String, List<PddQuestion>> = mutableMapOf()

    @Synchronized
    fun init(context: Context) {
        if (cachedQuestions.isEmpty()) {
            val questionsFromAssets = loadQuestionsFromAssets(context.applicationContext)
            if (questionsFromAssets.isNotEmpty()) {
                val assetAbm = questionsFromAssets.filter { it.category == "ABM" }
                val assetCd = questionsFromAssets.filter { it.category == "CD" }

                val fullAbm = if (assetAbm.size < 800) {
                    val defaults = generateDefaultQuestions("ABM")
                    val assetTicketKeys = assetAbm.map { "${it.ticketNumber}_${it.questionNumber}" }.toSet()
                    assetAbm + defaults.filter { "${it.ticketNumber}_${it.questionNumber}" !in assetTicketKeys }
                } else {
                    assetAbm
                }

                val fullCd = if (assetCd.size < 800) {
                    val defaults = generateDefaultQuestions("CD")
                    val assetTicketKeys = assetCd.map { "${it.ticketNumber}_${it.questionNumber}" }.toSet()
                    assetCd + defaults.filter { "${it.ticketNumber}_${it.questionNumber}" !in assetTicketKeys }
                } else {
                    assetCd
                }

                cachedQuestions["ABM"] = fullAbm
                cachedQuestions["CD"] = fullCd
            }
        }
    }

    fun getQuestions(category: String): List<PddQuestion> {
        val cached = cachedQuestions[category]
        if (!cached.isNullOrEmpty()) {
            return cached
        }
        val generated = generateDefaultQuestions(category)
        cachedQuestions[category] = generated
        return generated
    }

    suspend fun populateDatabaseFromJson(context: Context, database: PddDatabase) {
        init(context)
        val dao = database.pddDao()
        val existingCount = dao.getQuestionsCount()

        val fullAbm = getQuestions("ABM")
        val fullCd = getQuestions("CD")
        val questionsToInsert = fullAbm + fullCd

        if (existingCount < questionsToInsert.size) {
            dao.insertQuestions(questionsToInsert.map { it.toEntity() })
        }

        // Populate traffic signs if empty
        if (dao.getTrafficSignsCount() == 0) {
            dao.insertTrafficSigns(getTrafficSigns().map { it.toEntity() })
        }
    }

    fun loadQuestionsFromAssets(context: Context): List<PddQuestion> {
        return try {
            val inputStream = context.assets.open("pdd_data.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            parseQuestionsJson(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseQuestionsJson(jsonString: String): List<PddQuestion> {
        val result = mutableListOf<PddQuestion>()
        try {
            val trimmed = jsonString.trim()
            val jsonArray = if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                val rootObj = JSONObject(trimmed)
                when {
                    rootObj.has("questions") -> rootObj.getJSONArray("questions")
                    rootObj.has("tickets") -> rootObj.getJSONArray("tickets")
                    rootObj.has("data") -> rootObj.getJSONArray("data")
                    else -> JSONArray()
                }
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)

                // Extract ticket number (handles "Билет 1", 1, "1", etc.)
                val ticketStr = when {
                    item.has("ticket_number") -> item.optString("ticket_number")
                    item.has("ticketNumber") -> item.optString("ticketNumber")
                    item.has("ticket") -> item.optString("ticket")
                    else -> ""
                }
                val ticketNum = ticketStr.filter { it.isDigit() }.toIntOrNull()
                    ?: item.optInt("ticket_number", 1)

                // Extract question number (handles "Вопрос 1", 1, "1", or sequential position)
                val qStr = when {
                    item.has("question_number") -> item.optString("question_number")
                    item.has("questionNumber") -> item.optString("questionNumber")
                    item.has("title") -> item.optString("title")
                    else -> ""
                }
                val questionNum = qStr.filter { it.isDigit() }.toIntOrNull() ?: ((i % 20) + 1)

                val rawCategory = when {
                    item.has("ticket_category") -> item.optString("ticket_category", "ABM")
                    item.has("category") -> item.optString("category", "ABM")
                    else -> "ABM"
                }
                val category = if (rawCategory.contains("CD", ignoreCase = true) || rawCategory.contains("C,D", ignoreCase = true)) {
                    "CD"
                } else {
                    "ABM"
                }

                val questionText = when {
                    item.has("question") -> item.optString("question")
                    item.has("question_text") -> item.optString("question_text")
                    item.has("title") -> item.optString("title")
                    else -> ""
                }
                if (questionText.isBlank()) continue

                // Parse topic
                val topicTitle = when {
                    item.has("topic") -> {
                        val opt = item.opt("topic")
                        if (opt is JSONArray && opt.length() > 0) {
                            opt.getString(0)
                        } else {
                            opt?.toString() ?: "Общие положения"
                        }
                    }
                    item.has("topicTitle") -> item.optString("topicTitle", "Общие положения")
                    else -> "Общие положения"
                }

                // Parse answers/options
                val options = mutableListOf<String>()
                var correctIdx = 0

                if (item.has("answers")) {
                    val answersArr = item.getJSONArray("answers")
                    for (a in 0 until answersArr.length()) {
                        val aObj = answersArr.optJSONObject(a)
                        if (aObj != null) {
                            val text = aObj.optString("answer_text", aObj.optString("text", ""))
                            options.add(text)
                            if (aObj.optBoolean("is_correct", false)) {
                                correctIdx = a
                            }
                        } else {
                            options.add(answersArr.getString(a))
                        }
                    }
                } else if (item.has("options")) {
                    val optArr = item.getJSONArray("options")
                    for (o in 0 until optArr.length()) {
                        options.add(optArr.getString(o))
                    }
                }

                // Check correct answer override if specified ("Правильный ответ: 2" or int)
                val caStr = item.optString("correct_answer")
                val parsedCa = caStr.filter { it.isDigit() }.toIntOrNull()
                if (parsedCa != null && parsedCa in 1..options.size) {
                    correctIdx = parsedCa - 1
                } else if (item.has("correctAnswerIndex")) {
                    correctIdx = item.optInt("correctAnswerIndex", correctIdx)
                }

                val expertComment = when {
                    item.has("answer_tip") -> item.optString("answer_tip")
                    item.has("expertComment") -> item.optString("expertComment")
                    item.has("comment") -> item.optString("comment")
                    else -> ""
                }

                // Image handling: resolve relative paths to GitHub raw repository
                val rawImage = when {
                    item.has("image") && !item.isNull("image") -> item.optString("image")
                    item.has("imageUrl") && !item.isNull("imageUrl") -> item.optString("imageUrl")
                    else -> null
                }
                val imageUrl = when {
                    rawImage == null || rawImage.isBlank() || rawImage == "null" || rawImage.contains("no_image", ignoreCase = true) -> null
                    rawImage.startsWith("http://") || rawImage.startsWith("https://") -> rawImage
                    rawImage.startsWith("./images/") -> "https://raw.githubusercontent.com/etspring/pdd_russia/master/" + rawImage.removePrefix("./")
                    rawImage.startsWith("images/") -> "https://raw.githubusercontent.com/etspring/pdd_russia/master/" + rawImage
                    else -> "https://raw.githubusercontent.com/etspring/pdd_russia/master/images/A_B/" + rawImage.removePrefix("./")
                }

                // Deterministic integer ID for Room entity
                val id = (ticketNum * 100) + questionNum + (if (category == "CD") 5000 else 0)

                result.add(
                    PddQuestion(
                        id = id,
                        ticketNumber = ticketNum,
                        questionNumber = questionNum,
                        category = category,
                        topicTitle = topicTitle,
                        questionText = questionText,
                        options = options,
                        correctAnswerIndex = correctIdx,
                        expertComment = expertComment,
                        diagramType = null,
                        imageUrl = imageUrl
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun generateDefaultQuestions(category: String): List<PddQuestion> {
        val list = mutableListOf<PddQuestion>()
        var idCounter = if (category == "ABM") 1000 else 2000

        // Real official-style question categories for ABM & CD
        val topics = listOf(
            "Общие положения",
            "Обязанности водителей",
            "Применение спецсигналов",
            "Обязанности пешеходов",
            "Сигналы светофора и регулировщика",
            "Аварийная сигнализация",
            "Начало движения, маневрирование",
            "Расположение ТС на проезжей части",
            "Скорость движения",
            "Обгон, опережение, встречный разъезд",
            "Остановка и стоянка",
            "Проезд перекрестков",
            "Пешеходные переходы и остановки",
            "Движение через ж/д пути",
            "Движение по автомагистрали",
            "Жилые зоны и приоритет маршрутных ТС",
            "Пользование внешними световыми приборами",
            "Буксировка и учебная езда",
            "Перевозка людей и грузов",
            "Первая помощь и ответственность"
        )

        val sampleBaseQuestions = listOf(
            Triple(
                "Главной дорогой на перекрестке является:",
                listOf(
                    "Дорога с твердым покрытием по отношению к грунтовой",
                    "Дорога с асфальтобетонным покрытием по отношению к брусчатке",
                    "Любая дорога с двумя и более полосами движения",
                    "Дорога, по которой движется маршрутное транспортное средство"
                ),
                0 to "Согласно п. 1.2 ПДД РФ, «Главная дорога» — это дорога, обозначенная знаками 2.1, 2.3.1-2.3.7 или 2.2, либо дорога с твердым покрытием (асфальт, бетон, брусчатка) по отношению к грунтовой."
            ),
            Triple(
                "Разрешается ли вам произвести остановку в указанном месте за пешеходным переходом?",
                listOf(
                    "Разрешается непосредственно за переходом",
                    "Разрешается только в 5 метрах за переходом",
                    "Запрещается в любом случае",
                    "Разрешается только при отсутствии пешеходов"
                ),
                0 to "Согласно п. 12.4 ПДД РФ, остановка запрещается НА пешеходных переходах и ближе 5 м ПЕРЕД ними. Непосредственно ЗА пешеходным переходом остановка разрешена."
            ),
            Triple(
                "Какой минимальный стаж вождения необходим для обучения вождению транспортного средства?",
                listOf(
                    "Не менее 3 лет",
                    "Не менее 2 лет",
                    "Не менее 5 лет",
                    "Стаж вождения не регламентируется"
                ),
                0 to "По требованиям ПДД РФ (п. 21.3), обучающий вождению должен иметь водительское удостоверение соответствующей категории со стажем вождения не менее 3 лет."
            ),
            Triple(
                "В каких случаях водитель обязан уступить дорогу пешеходам, переходящим проезжую часть?",
                listOf(
                    "На нерегулируемых пешеходных переходах всегда",
                    "Только при повороте направо или налево",
                    "Только на регулируемых переходах при зеленом сигнале",
                    "При движении в жилой зоне и при выезде с прилегающей территории"
                ),
                0 to "Согласно п. 14.1 ПДД РФ, водитель ТС, приближающегося к нерегулируемому пешеходному переходу, обязан уступить дорогу пешеходам, переходящим дорогу или вступившим на проезжую часть."
            ),
            Triple(
                "Разрешено ли водителю пользоваться телефоном во время движения?",
                listOf(
                    "Запрещено без использования гарнитуры (Hands-Free)",
                    "Запрещено в любых случаях",
                    "Разрешено при скорости менее 40 км/ч",
                    "Разрешено только вне населенных пунктов"
                ),
                0 to "Согласно п. 2.7 ПДД РФ, водителю запрещается пользоваться во время движения телефоном, не оборудованным техническим устройством, позволяющим вести переговоры без использования рук."
            ),
            Triple(
                "Какой сигнал регулировщика запрещает движение всем транспортным средствам и пешеходам?",
                listOf(
                    "Рука поднята вверх",
                    "Руки вытянуты в стороны",
                    "Правая рука вытянута вперед",
                    "Регулировщик стоит лицом к вам"
                ),
                0 to "Согласно п. 6.10 ПДД РФ, когда рука регулировщика поднята вверх — движение всех транспортных средств и пешеходов запрещено во всех направлениях (аналог желтого сигнала)."
            ),
            Triple(
                "Какое значение имеет желтый мигающий сигнал светофора?",
                listOf(
                    "Разрешает движение и информирует о наличии нерегулируемого перекрестка",
                    "Требует обязательной остановки перед стоп-линией",
                    "Информирует о скором включении красного сигнала",
                    "Запрещает движение во всех направлениях"
                ),
                0 to "Согласно п. 6.2 ПДД РФ, желтый мигающий сигнал разрешает движение и информирует о наличии нерегулируемого перекрестка или пешеходного перехода."
            ),
            Triple(
                "С какой максимальной скоростью разрешено движение легковых автомобилей в жилых зонах и на дворовых территориях?",
                listOf(
                    "Не более 20 км/ч",
                    "Не более 30 км/ч",
                    "Не более 40 км/ч",
                    "Не более 10 км/ч"
                ),
                0 to "Согласно п. 10.2 ПДД РФ, в жилых зонах, велосипедных зонах и на дворовых территориях скорость движения транспортных средств не должна превышать 20 км/ч."
            ),
            Triple(
                "Вы намерены повернуть налево. Кому вы обязаны уступить дорогу на равнозначном перекрестке?",
                listOf(
                    "Транспортным средствам, движущимся со встречного направления прямо или направо",
                    "Только транспортным средствам, приближающимся справа",
                    "Всем транспортным средствам",
                    "Никому, у вас преимущество"
                ),
                0 to "Согласно п. 13.12 ПДД РФ, при повороте налево или развороте водитель безрельсового ТС обязан уступить дорогу ТС, движущимся по равнозначной дороге со встречного направления прямо или направо."
            ),
            Triple(
                "Каковы условия безопасного обгона с выездом на полосу встречного движения?",
                listOf(
                    "Полоса свободна на достаточном расстоянии и обгон не создаст опасности",
                    "Скорость обгоняемого автомобиля менее 30 км/ч",
                    "Наличие прерывистой линии разметки независимо от видимости",
                    "Включение ближнего света фар и звукового сигнала"
                ),
                0 to "Согласно п. 11.1 ПДД РФ, прежде чем начать обгон, водитель обязан убедиться в том, что полоса движения, на которую он намеревается выехать, свободна на достаточном расстоянии."
            ),
            Triple(
                "Какое расстояние должно быть обеспечено до сплошной линии разметки при остановке ТС?",
                listOf(
                    "Не менее 3 метров",
                    "Не менее 5 метров",
                    "Не менее 1.5 метров",
                    "Не менее 1 метра"
                ),
                0 to "Согласно п. 12.4 ПДД РФ, остановка запрещается на участках дорог, где расстояние между сплошной линией разметки (кроме обозначающей край проезжей части) и ТС менее 3 метров."
            ),
            Triple(
                "Где запрещен разворот согласно Правилам дорожного движения?",
                listOf(
                    "На пешеходных переходах, мостах, путепроводах, эстакадах и в тоннелях",
                    "На всех перекрестках вне населенных пунктов",
                    "На дорогах с двумя и более полосами движения",
                    "При включенном ближнем свете фар"
                ),
                0 to "Согласно п. 8.11 ПДД РФ, разворот запрещается: на пешеходных переходах, в тоннелях, на мостах, путепроводах, эстакадах и под ними, на ж/д переездах, при видимости менее 100 м."
            ),
            Triple(
                "Разрешается ли буксировка механического транспортного средства на гибкой сцепке в гололедицу?",
                listOf(
                    "Запрещается",
                    "Разрешается со скоростью не более 30 км/ч",
                    "Разрешается при включенной аварийной сигнализации",
                    "Разрешается только вне населенных пунктов"
                ),
                0 to "Согласно п. 20.4 ПДД РФ, буксировка на гибкой сцепке запрещается в гололедицу из-за высокого риска заноса и столкновения."
            ),
            Triple(
                "Какая сумма штрафа или наказание предусмотрено за непредоставление преимущества пешеходам?",
                listOf(
                    "Штраф от 1500 до 2500 рублей",
                    "Предупреждение или штраф 500 рублей",
                    "Лишение прав на 1-3 месяца",
                    "Штраф 5000 рублей"
                ),
                0 to "Статья 12.18 КоАП РФ предусматривает административный штраф в размере от 1500 до 2500 рублей за невыполнение требования ПДД уступить дорогу пешеходам."
            ),
            Triple(
                "Какое первое действие необходимо совершить при оказании первой помощи пострадавшему при ДТП?",
                listOf(
                    "Оценить обстановку и обеспечить собственную безопасность",
                    "Сразу извлечь пострадавшего из автомобиля",
                    "Дать пострадавшему обезболивающее средство",
                    "Положить пострадавшего на живот"
                ),
                0 to "Алгоритм первой помощи (Приказ Минздрава РФ): первым шагом является оценка обстановки, устранение угрожающих факторов и обеспечение собственной безопасности."
            ),
            Triple(
                "Разрешено ли движение задним ходом на автомагистрали?",
                listOf(
                    "Запрещено в любом случае",
                    "Разрешено только по обочине",
                    "Разрешено при включенной аварийной сигнализации",
                    "Разрешено для выезда со стоянки"
                ),
                0 to "Согласно п. 16.1 ПДД РФ, на автомагистралях запрещается движение задним ходом, а также разворот и въезд в технологические разрывы разделительной полосы."
            ),
            Triple(
                "В каких случаях включаются дневные ходовые огни или ближний свет фар в светлое время суток?",
                listOf(
                    "На всех движущихся транспортных средствах с целью их обозначения",
                    "Только вне населенных пунктов",
                    "Только при перевозке детей или опасных грузов",
                    "Только в условиях недостаточной видимости"
                ),
                0 to "Согласно п. 19.5 ПДД РФ, в светлое время суток на всех движущихся транспортных средствах с целью их обозначения должны включаться дневные ходовые огни или ближний свет фар."
            ),
            Triple(
                "Что обязан сделать водитель при заносе задней оси заднеприводного автомобиля?",
                listOf(
                    "Слегка уменьшить подачу топлива и повернуть руль в сторону заноса",
                    "Резко нажать на педаль тормоза",
                    "Увеличить подачу топлива и вывернуть руль в противоположную сторону",
                    "Выжать педаль сцепления и затянуть стояночный тормоз"
                ),
                0 to "Для заднеприводного автомобиля при заносе необходимо плавно уменьшить подачу топлива (снизить тягу) и опережающим движением повернуть руль в сторону заноса."
            ),
            Triple(
                "Разрешается ли эксплуатация автомобиля, если не работает спидометр?",
                listOf(
                    "Запрещается",
                    "Разрешается доехать до места ремонта со скоростью до 40 км/ч",
                    "Разрешается при наличии GPS-навигатора",
                    "Разрешается вне населенных пунктов"
                ),
                0 to "Перечень неисправностей и условий, при которых запрещается эксплуатация ТС: неработающий спидометр запрещает дальнейшую эксплуатацию до устранения неисправности."
            ),
            Triple(
                "Допускается ли установка на одну ось автомобиля шин с различным рисунком протектора?",
                listOf(
                    "Запрещается",
                    "Допускается только на заднюю ось",
                    "Допускается при высоте протектора более 4 мм",
                    "Допускается в летний период"
                ),
                0 to "Согласно Основным положениям по допуску ТС к эксплуатации (п. 5.5), запрещается установка на одну ось шин различных размеров, конструкций, моделей и с различными рисунками протектора."
            )
        )

        val testImageUrls = listOf(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Russian_road_sign_2.1.svg/640px-Russian_road_sign_2.1.svg.png", // Главная дорога
            "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Russian_road_sign_5.19.1.svg/640px-Russian_road_sign_5.19.1.svg.png", // Пешеходный переход
            "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Russian_road_sign_2.5.svg/640px-Russian_road_sign_2.5.svg.png", // STOP
            "https://upload.wikimedia.org/wikipedia/commons/thumb/6/64/Russian_road_sign_3.20.svg/640px-Russian_road_sign_3.20.svg.png", // Обгон запрещен
            "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Russian_road_sign_4.3.svg/640px-Russian_road_sign_4.3.svg.png", // Круговое движение
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/Russian_road_sign_3.24_60.svg/640px-Russian_road_sign_3.24_60.svg.png" // Ограничение 60
        )

        // Populate 40 tickets (20 questions per ticket = 800 questions)
        for (ticket in 1..40) {
            for (qNum in 1..20) {
                val baseIdx = ((ticket - 1) * 20 + (qNum - 1)) % sampleBaseQuestions.size
                val base = sampleBaseQuestions[baseIdx]
                val topic = topics[(qNum - 1) % topics.size]

                // Variations for rich content
                val questionTitle = if (ticket == 1 && qNum <= 5) {
                    base.first
                } else {
                    "Билет $ticket, Вопрос $qNum ($topic): ${base.first}"
                }

                val diagType = when ((qNum + ticket) % 5) {
                    0 -> "intersection_priority"
                    1 -> "traffic_sign_warning"
                    2 -> "pedestrian_crossing"
                    3 -> "overtaking_road"
                    else -> null
                }

                // Attach real test image URLs for visual tickets
                val questionImageUrl = when {
                    ticket == 1 && qNum == 1 -> testImageUrls[0] // Главная дорога
                    ticket == 1 && qNum == 2 -> testImageUrls[1] // Остановка за переходом
                    ticket == 1 && qNum == 4 -> testImageUrls[2] // STOP знак
                    ticket == 1 && qNum == 7 -> testImageUrls[4] // Круговое движение
                    (qNum + ticket) % 3 == 0 -> testImageUrls[(ticket + qNum) % testImageUrls.size]
                    else -> null
                }

                list.add(
                    PddQuestion(
                        id = idCounter++,
                        ticketNumber = ticket,
                        questionNumber = qNum,
                        category = category,
                        topicTitle = topic,
                        questionText = questionTitle,
                        options = base.second,
                        correctAnswerIndex = base.third.first,
                        expertComment = base.third.second,
                        diagramType = diagType,
                        imageUrl = questionImageUrl
                    )
                )
            }
        }

        return list
    }

    fun getTrafficSigns(): List<TrafficSign> {
        return listOf(
            TrafficSign("1.1", "Железнодорожный переезд со шлагбаумом", "Предупреждающие", "Предупреждает о приближении к ж/д переезду со шлагбаумом. Вне населенных пунктов устанавливается на расстоянии 150-300 м.", "warning"),
            TrafficSign("1.2", "Железнодорожный переезд без шлагбаума", "Предупреждающие", "Предупреждает о переезде без шлагбаума. Требует особого внимания и остановки перед путями при запрещающем сигнале.", "warning"),
            TrafficSign("1.8", "Светофорное регулирование", "Предупреждающие", "Перекресток, пешеходный переход или участок дороги, движение на котором регулируется светофором.", "warning"),
            TrafficSign("1.22", "Пешеходный переход", "Предупреждающие", "Приближение к нерегулируемому пешеходному переходу.", "warning"),
            TrafficSign("2.1", "Главная дорога", "Знаки приоритета", "Предоставляет право первоочередного проезда нерегулируемых перекрестков.", "priority"),
            TrafficSign("2.2", "Конец главной дороги", "Знаки приоритета", "Отменяет право первоочередного проезда перекрестков.", "priority"),
            TrafficSign("2.4", "Уступите дорогу", "Знаки приоритета", "Водитель должен уступить дорогу транспортным средствам, движущимся по пересекаемой дороге.", "priority"),
            TrafficSign("2.5", "Движение без остановки запрещено", "Знаки приоритета", "Запрещается движение без остановки перед стоп-линией или краем пересекаемой проезжей части.", "priority"),
            TrafficSign("3.1", "Въезд запрещен (Кирпич)", "Запрещающие", "Запрещает въезд всех транспортных средств в данном направлении.", "prohibitory"),
            TrafficSign("3.2", "Движение запрещено", "Запрещающие", "Запрещает движение всех транспортных средств (за исключением маршрутных, инвалидов и проживающих).", "prohibitory"),
            TrafficSign("3.20", "Обгон запрещен", "Запрещающие", "Запрещается обгон всех транспортных средств, кроме тихоходных, мопедов и двухколесных мотоциклов.", "prohibitory"),
            TrafficSign("3.24", "Ограничение максимальной скорости", "Запрещающие", "Запрещается движение со скоростью (км/ч), превышающей указанную на знаке.", "prohibitory"),
            TrafficSign("3.27", "Остановка запрещена", "Запрещающие", "Запрещаются остановка и стоянка транспортных средств.", "prohibitory"),
            TrafficSign("4.1.1", "Движение прямо", "Предписывающие", "Разрешает движение только прямо.", "mandatory"),
            TrafficSign("5.1", "Автомагистраль", "Знаки особых предписаний", "Дорога, на которой действуют особые правила движения (макс. скорость 110 км/ч, запрет пешеходов, велотранспорта и заднего хода).", "special"),
            TrafficSign("5.19.1", "Пешеходный переход", "Знаки особых предписаний", "Указывает границу пешеходного перехода.", "special"),
            TrafficSign("6.4", "Парковка (парковочное место)", "Информационные", "Обозначает площадку или зону для парковки транспортных средств.", "info"),
            TrafficSign("8.2.1", "Зона действия", "Знаки дополнительной информации", "Указывает протяженность опасного участка дороги или зоны действия запрещающих знаков.", "info")
        )
    }

    fun getPddRuleSections(): List<PddRuleSection> {
        return listOf(
            PddRuleSection(
                1,
                "1. Общие положения",
                "Настоящие Правила дорожного движения устанавливают единый порядок дорожного движения на всей территории Российской Федерации. В Правилах используются основные понятия и термины: Автомагистраль, Главная дорога, Дорожно-транспортное происшествие, Недостаточная видимость, Опасность для движения, Пешеходный переход, Уступить дорогу.",
                "Основные термины, статус ПДД и приоритеты правового регулирования."
            ),
            PddRuleSection(
                2,
                "2. Общие обязанности водителей",
                "Водитель механического ТС обязан иметь при себе водительское удостоверение, регистрационные документы на ТС (СТС), полис ОСАГО. Водитель обязан при движении на ТС, оборудованном ремнями безопасности, быть пристегнутым и не перевозить непристегнутых пассажиров.",
                "Документы, пристегивание ремнями, запрет алкоголя и телефона без Hands-Free."
            ),
            PddRuleSection(
                6,
                "6. Сигналы светофора и регулировщика",
                "В светофорах применяются световые сигналы зеленого, желтого, красного и бело-лунного цвета. Зеленый сигнал разрешает движение. Желтый signal запрещает движение (кроме экстренного торможения) и предупреждает о смене сигналов. Красный сигнал запрещает движение. Сигналы регулировщика имеют преимущество перед сигналами светофора.",
                "Значения сигналов светофора, регулировщика и порядок проезда."
            ),
            PddRuleSection(
                8,
                "8. Начало движения, маневрирование",
                "Перед началом движения, перестроением, поворотом (разворотом) и остановкой водитель обязан подавать сигналы световыми указателями поворота соответствующего направления. При перестроении водитель должен уступить дорогу ТС, движущимся попутно без изменения направления.",
                "Указатели поворота, уступка при перестроении, правило «правой руки»."
            ),
            PddRuleSection(
                12,
                "12. Остановка и стоянка",
                "Остановка и стоянка ТС разрешаются на правой стороне дороги на обочине, а при ее отсутствии — у края проезжей части. Остановка запрещается: на ж/д переездах, в тоннелях, на мостах, на пешеходных переходах и ближе 5 м перед ними, на остановках маршрутных ТС.",
                "Правила парковки, запрещенные зоны для остановки и стоянки."
            ),
            PddRuleSection(
                13,
                "13. Проезд перекрестков",
                "Перекресток, где очередность движения определяется сигналами светофора или регулировщика, считается регулируемым. На нерегулируемом перекрестке неравнозначных дорог водитель ТС, движущегося по второстепенной дороге, должен уступить дорогу ТС, приближающимся по главной.",
                "Регулируемые и нерегулируемые перекрестки, главная и второстепенная дороги."
            ),
            PddRuleSection(
                14,
                "14. Пешеходные переходы и места остановок маршрутных ТС",
                "Водитель ТС, приближающегося к нерегулируемому пешеходному переходу, обязан уступить дорогу пешеходам, переходящим дорогу или вступившим на проезжую часть для осуществления перехода.",
                "Приоритет пешеходов на переходах и безопасность посадки пассажиров."
            ),
            PddRuleSection(
                19,
                "19. Пользование внешними световыми приборами и звуковыми сигналами",
                "В темное время суток и в условиях недостаточной видимости на всех движущихся ТС должны быть включены фары дальнего или ближнего света. В светлое время суток на всех движущихся ТС должны включаться дневные ходовые огни или ближний свет фар.",
                "Фары, противотуманные фары, ходовые огни и звуковые сигналы."
            )
        )
    }
}
