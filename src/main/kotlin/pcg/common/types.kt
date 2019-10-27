package pcg.common

import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import kotlin.reflect.KClass

enum class ComponentType(val type: Int, val kClass: KClass<out Number>, val size: Int) {
    BYTE(5120, Byte::class, 1),
    UNSIGNED_BYTE(5121, Byte::class, 1),
    SHORT(5122, Short::class, 2),
    UNSIGNED_SHORT(5123, Short::class, 2),
    UNSIGNED_INT(5125, Int::class, 4),
    FLOAT(5126, Float::class, 4);

    companion object {
        val serializer = JsonSerializer<ComponentType> { src, _, _ -> JsonPrimitive(src.type) }
    }
}

enum class Type(val length: Int) {
    SCALAR(1),
    VEC2(2),
    VEC3(3),
    VEC4(4),
    MAT2(4),
    MAT3(9),
    MAT4(16)
}

data class AccessorData(
    val componentType: ComponentType,
    val type: Type,
    val max: List<Number>?,
    val min: List<Number>?
)

interface WithAccessorData {
    val accessorData: AccessorData
}
