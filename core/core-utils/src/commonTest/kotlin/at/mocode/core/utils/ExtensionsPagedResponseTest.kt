package at.mocode.core.utils

import at.mocode.core.domain.model.PageNumber
import at.mocode.core.domain.model.PageSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtensionsPagedResponseTest {

    @Test
    fun `toPagedResponse basic pagination`() {
        val list = (1..50).toList()

        val page0 = list.toPagedResponse(page = 0, size = 10)
        assertEquals(10, page0.content.size)
        assertEquals(PageNumber(0), page0.page)
        assertEquals(PageSize(10), page0.size)
        assertEquals(50L, page0.totalElements)
        assertEquals(5, page0.totalPages)
        assertTrue(page0.hasNext)
        assertFalse(page0.hasPrevious)

        val page4 = list.toPagedResponse(page = 4, size = 10)
        assertEquals((41..50).toList(), page4.content)
        assertFalse(page4.hasNext)
        assertTrue(page4.hasPrevious)

        val emptyPage = list.toPagedResponse(page = 6, size = 10)
        assertTrue(emptyPage.content.isEmpty())
        assertEquals(5, emptyPage.totalPages)
        assertFalse(emptyPage.hasNext)
        assertTrue(emptyPage.hasPrevious)
    }
}
