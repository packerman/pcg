package pcg

import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer

enum class ComponentType(val type: Int) {
    BYTE(5120),
    UNSIGNED_BYTE(5121),
    SHORT(5122),
    UNSIGNED_SHORT(5123),
    UNSIGNED_INT(5125),
    FLOAT(5126);

    companion object {
        val serializer = JsonSerializer<ComponentType> { src, _, _ -> JsonPrimitive(src.type) }
    }
}

enum class Type {
    SCALAR,
    VEC2,
    VEC3,
    VEC4,
    MAT2,
    MAT3,
    MAT4
}

data class Accessor(
    val bufferView: Int? = null,
    val byteOffset: Int? = 0,
    val componentType: ComponentType,
    val count: Int,
    val max: List<Number>? = null,
    val min: List<Number>? = null,
    val name: String? = null,
    val type: Type
) {

}

data class Asset(
    val version: String,
    val minVersion: String? = null
)

data class Gltf(
    val accessors: List<Accessor>? = null,
    val asset: Asset,
    val materials: List<Material>? = null,
    val meshes: List<Mesh>? = null,
    val nodes: List<Node>? = null,
    val scene: Int? = null,
    val scenes: List<Scene>? = null
) {

    init {
        accessors?.let { requireNotEmpty(it, "accessors") }
        meshes?.let {
            requireNotEmpty(it, "meshes")
            it.forEach { mesh ->
                mesh.primitives.forEach { primitive ->
                    primitive.indices?.let { indices -> requireInRange(indices, accessors, "indices") }
                    primitive.attributes.entries.forEach { (attribute, accessor) ->
                        requireInRange(accessor, accessors, "Accessor for $attribute")
                    }
                    primitive.material?.let { material -> requireInRange(material, materials, "material") }
                }
            }
        }
        nodes?.let {
            requireNotEmpty(it, "nodes")
            it.forEach { node ->
                node.children?.forEach { child -> requireInRange(child, nodes, "child") }
                node.mesh?.let { mesh -> requireInRange(mesh, meshes, "mesh") }
            }
        }
        scenes?.let { requireNotEmpty(it, "scenes") }
        scene?.let { requireInRange(it, scenes, "scene") }
    }
}

data class Material(
    val name: String? = null
)

data class Mesh(
    val primitives: List<Primitive>,
    val name: String? = null
) {
    init {
        requireNotEmpty(primitives, "primitives")
    }
}

val identity = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

data class Node(
    val children: List<Int>? = null,
    val matrix: FloatArray? = null,
    val mesh: Int? = null
) {
    init {
        children?.let { requireNotEmpty(it, "children") }
        matrix?.let { requireSize(it, 16, "matrix") }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false

        if (children != other.children) return false
        if (matrix != null) {
            if (other.matrix == null) return false
            if (!matrix.contentEquals(other.matrix)) return false
        } else if (other.matrix != null) return false
        if (mesh != other.mesh) return false

        return true
    }

    override fun hashCode(): Int {
        var result = children?.hashCode() ?: 0
        result = 31 * result + (matrix?.contentHashCode() ?: 0)
        result = 31 * result + (mesh ?: 0)
        return result
    }
}

enum class Attribute {
    NORMAL,
    POSITION,
    TEXCOORD_0
}

//TODO mode has to be serialized as int, not string

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

data class Primitive(
    val attributes: Map<Attribute, Int>,
    val indices: Int? = null,
    val mode: Mode? = null,
    val material: Int? = null
) {
    //TODO - validate attributes and indices after adding accesssors
}

data class Scene(
    val nodes: List<Int>? = null,
    val name: String? = null
) {

    init {
        nodes?.let { requireNotEmpty(it, "nodes") }
    }
}

val defaultAsset = Asset(
    version = "2.0"
)

fun Gltf.toJson(prettyPrinting: Boolean = false): String {
    val builder = GsonBuilder()
    if (prettyPrinting) {
        builder.setPrettyPrinting()
    }
    builder.registerTypeAdapter(Mode::class.java, Mode.serializer)
    builder.registerTypeAdapter(ComponentType::class.java, ComponentType.serializer)
    val gson = builder.create()
    return gson.toJson(this)
}

fun main() {
    val gltf = Gltf(
        accessors = listOf(
            Accessor(
                bufferView = 0,
                byteOffset = 0,
                componentType = ComponentType.UNSIGNED_SHORT,
                count = 36,
                max = listOf(23.toShort()),
                min = listOf(0.toShort()),
                type = Type.SCALAR
            ),
            Accessor(
                bufferView = 1,
                byteOffset = 0,
                componentType = ComponentType.FLOAT,
                count = 24,
                max = listOf(1f, 1f, 1f),
                min = listOf(-1f, -1f, -1f),
                type = Type.VEC3
            ),
            Accessor(
                bufferView = 1,
                byteOffset = 288,
                componentType = ComponentType.FLOAT,
                count = 24,
                max = listOf(0.5f, 0.5f, 0.5f),
                min = listOf(-0.5f, -0.5f, -0.5f),
                type = Type.VEC3
            ),
            Accessor(
                bufferView = 2,
                byteOffset = 0,
                componentType = ComponentType.FLOAT,
                count = 24,
                max = listOf(6f, 1f),
                min = listOf(0f, 0f),
                type = Type.VEC2
            )
        ),
        asset = defaultAsset,
        scene = 0,
        scenes = listOf(
            Scene(nodes = listOf(0))
        ),
        nodes = listOf(
            Node(
                children = listOf(1),
                matrix = floatArrayOf(
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, -1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                )
            ),
            Node(
                mesh = 0
            )
        ),
        materials = listOf(
            Material(name = "Texture")
        ),
        meshes = listOf(
            Mesh(
                primitives = listOf(
                    Primitive(
                        attributes = mapOf(
                            Attribute.NORMAL to 1,
                            Attribute.POSITION to 2,
                            Attribute.TEXCOORD_0 to 3
                        ),
                        indices = 0,
                        mode = Mode.TRIANGLES,
                        material = 0
                    )
                ),
                name = "Mesh"
            )
        )
    )
    println(gltf.toJson(true))
}

fun requireNotEmpty(list: List<*>, name: String) =
    require(list.isNotEmpty()) { "'$name' has to be not empty" }

fun requireNonNegative(n: Int, name: String) =
    require(n >= 0) { "'$name' has to be non-negative" }

fun requireInRange(i: Int, list: List<*>?, name: String) =
    require(list != null && i >= 0 && i < list.size) { "'$name' is not valid index" }

fun requireSize(array: FloatArray, n: Int, name: String) =
    require(array.size == n) { "'$name' has to have size $n" }
