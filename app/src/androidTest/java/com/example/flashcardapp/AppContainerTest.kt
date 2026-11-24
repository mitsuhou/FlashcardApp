package com.example.flashcardapp

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppContainerTest {
    @Test
    fun activityCreationExtrasProvideTheApplicationContainer() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val application = activity.application as FlashcardApplication
                val factoryApplication = activity.defaultViewModelCreationExtras.flashcardApplication()
                assertSame(application.container, factoryApplication.container)
            }
        }
    }
}
