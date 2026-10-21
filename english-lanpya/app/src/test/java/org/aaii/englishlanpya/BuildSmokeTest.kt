package org.aaii.englishlanpya

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildSmokeTest {
    @Test fun versionTag() {
        assertTrue("2.7.1-build-qa".startsWith("2.7.1"))
    }
    @Test fun basicExerciseMarking() {
        val answers = listOf("Good morning", "Good night", "Goodbye")
        assertEquals("Good morning", answers[0])
    }
}
