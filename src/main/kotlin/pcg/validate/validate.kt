package pcg.validate

import kotlin.reflect.KClass

fun requireNotEmpty(list: List<*>, name: String) =
    require(list.isNotEmpty()) { "'$name' has to be not empty" }

fun requireNotEmpty(map: Map<*, *>, name: String) =
    require(map.isNotEmpty()) { "'$name' has to be not empty" }

fun requirePositive(n: Int, name: String) =
    require(n > 0) { "'$name' has to be non-negative" }

fun requireInRange(i: Int, list: List<*>?, name: String) =
    require(list != null && i >= 0 && i < list.size) { "'$name' is not valid index" }

fun requireSize(array: FloatArray, n: Int, name: String) =
    require(array.size == n) { "'$name' has to have size $n" }

fun requireSize(list: List<*>, n: Int, name: String) =
    require(list.size == n) { "'$name' has to have size $n" }

fun hasElementsOf(list: List<Number>, kClass: KClass<out Number>, name: String) {
    list.forEach { element ->
        require(kClass.isInstance(element)) {
            "List $name has some element of type ${element::class} while all elements have to be of $kClass type"
        }
    }
}
