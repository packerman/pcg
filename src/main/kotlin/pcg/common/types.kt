package pcg.common

import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import kotlin.reflect.KClass

enum class Attribute {
    NORMAL,
    POSITION,
    TEXCOORD_0
}

enum class Mode {
    POINTS,
    LINES,
    LINE_LOOP,
    LINE_STRIP,
    TRIANGLES,
    TRIANGLE_STRIP,
    TRIANGLE_FAN;

    companion object {
        val DEFAULT = TRIANGLES

        val serializer = JsonSerializer<Mode> { src, _, _ -> JsonPrimitive(src.ordinal) }
    }
}

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

enum class BufferTarget(val target: Int) {
    ARRAY_BUFFER(34962),
    ELEMENT_ARRAY_BUFFER(34963);

    companion object {
        val serializer = JsonSerializer<BufferTarget> { src, _, _ -> JsonPrimitive(src.target) }
    }
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

enum class Filter(val filter: Int) {
    Nearest(9728),
    Linear(9729),
    NearestMipmapNearest(9984),
    LinearMipmapNearest(9985),
    NearestMipmapLinear(9986),
    LinearMipmapLinear(9987);

    companion object {
        val serializer = JsonSerializer<Filter> { src, _, _ -> JsonPrimitive(src.filter) }
    }
}

enum class Wrap(val mode: Int) {
    ClampToEdge(33071),
    MirroredRepeat(33648),
    Repeat(10497);

    companion object {
        val serializer = JsonSerializer<Wrap> { src, _, _ -> JsonPrimitive(src.mode) }
    }
}
