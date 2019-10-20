package pcg.util

fun <T> indexElements(elements: Iterable<T>): Map<T, Int> = mutableMapOf<T, Int>().apply {
    elements.forEach { elem ->
        putIfAbsent(elem, size)
    }
}

fun <T> allTheSame(elements: Collection<T>): Boolean =
    if (elements.isEmpty()) true
    else {
        val first = elements.first()
        elements.all { it == first }
    }

fun ShortArray.intIterator(): IntIterator {
    val iterator = iterator()
    return object : IntIterator() {
        override fun hasNext(): Boolean = iterator.hasNext()

        override fun nextInt(): Int = iterator.nextShort().toInt()
    }
}
