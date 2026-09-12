import json
import re
from pathlib import Path


SOURCE_DIR = Path(r"C:\Users\user\Downloads\pdd_russia-master\questions\A_B\tickets")
TARGET_FILE = Path(__file__).resolve().parent / "app" / "src" / "main" / "assets" / "pdd_data.json"


def ticket_number(path: Path) -> int:
    match = re.search(r"(\d+)", path.stem)
    if not match:
        raise ValueError(f"Не удалось определить номер билета: {path.name}")
    return int(match.group(1))


def main() -> None:
    if hasattr(__import__("sys").stdout, "reconfigure"):
        __import__("sys").stdout.reconfigure(encoding="utf-8")

    files = sorted(SOURCE_DIR.glob("*.json"), key=ticket_number)
    if len(files) != 40:
        raise ValueError(f"Ожидалось 40 JSON-файлов, найдено: {len(files)}")

    questions = []
    for path in files:
        with path.open("r", encoding="utf-8") as file:
            data = json.load(file)
        if not isinstance(data, list):
            raise TypeError(f"Корень файла должен быть массивом: {path.name}")
        questions.extend(data)

    TARGET_FILE.parent.mkdir(parents=True, exist_ok=True)
    with TARGET_FILE.open("w", encoding="utf-8", newline="\n") as file:
        json.dump({"questions": questions}, file, ensure_ascii=False, indent=2)
        file.write("\n")

    print(f"Объединено файлов: {len(files)}")
    print(f"Вопросов: {len(questions)}")
    print(f"Записано: {TARGET_FILE}")


if __name__ == "__main__":
    main()
