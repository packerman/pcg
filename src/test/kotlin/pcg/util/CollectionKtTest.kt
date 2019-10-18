package pcg.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class CollectionKtTest {

    @Test
    internal fun shouldIndexElements() {
        val toIndex = listOf(1, 2, 4, 2, 1, 4, 3)

        val index = indexElements(toIndex)

        assertEquals(mapOf(1 to 0, 2 to 1, 4 to 2, 3 to 3), index)
    }

    @Test
    internal fun allTheSame() {
        assertFalse(allTheSame(listOf(1, 2)))
        assertTrue(allTheSame(emptyList<Nothing>()))
        assertTrue(allTheSame(listOf(3)))
        assertTrue(allTheSame(listOf(3, 3)))
        assertTrue(allTheSame(listOf(2, 2, 2)))
        assertFalse(allTheSame(listOf(2, 1, 2)))
    }
}
