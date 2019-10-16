package pcg.util

fun <T> indexElements(elements: Iterable<T>): Map<T, Int> = mutableMapOf<T, Int>().apply {
    elements.forEach { elem ->
        putIfAbsent(elem, size)
    }
}
