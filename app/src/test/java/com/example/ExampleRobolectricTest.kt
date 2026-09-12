package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PddDataProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ПДД 2026-2027", appName)
  }

  @Test
  fun `parse pdd_data json successfully`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val questions = PddDataProvider.loadQuestionsFromAssets(context)
    assertEquals("Should parse all 800 questions from assets", 800, questions.size)

    val tickets = questions.map { it.ticketNumber }.distinct().sorted()
    assertEquals(40, tickets.size)
    assertEquals(1, tickets.first())
    assertEquals(40, tickets.last())

    val withImages = questions.count { !it.imageUrl.isNullOrBlank() }
    assertTrue("At least 500 questions should have image URLs", withImages >= 500)
  }
}
