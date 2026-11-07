package asia.aaii.soemoe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineCatalogTest {
    @Test fun courseIdsUnique() {
        assertEquals(OfflineCatalog.courses.size, OfflineCatalog.courses.map { it.id }.distinct().size)
    }
    @Test fun lessonIdsUnique() {
        assertEquals(OfflineCatalog.allLessons.size, OfflineCatalog.allLessons.map { it.id }.distinct().size)
    }
    @Test fun seedCurriculumHasReadableContent() {
        assertEquals(4, OfflineCatalog.courses.size)
        assertEquals(12, OfflineCatalog.allLessons.size)
        assertTrue(OfflineCatalog.allLessons.all { it.content.length > 80 })
    }
    @Test fun practiceTasksIncluded() {
        assertEquals(4, OfflineCatalog.practices.size)
    }
}
