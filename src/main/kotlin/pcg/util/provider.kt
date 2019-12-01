package pcg.util

class Provider<T> {

    private var value: T? = null

    fun provide(provided: T) {
        check(value == null) { "Value already provided" }
        value = provided
    }

    fun ifProvided(block: (T) -> Unit) {
        value?.let(block)
    }
}
