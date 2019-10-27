package pcg.gltf

import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import pcg.common.ComponentType
import pcg.common.Type
import pcg.gltf.BufferView.Companion.Target
import pcg.gltf.Primitive.Companion.Mode
import pcg.gltf.Sampler.Companion.Filter
import pcg.gltf.Sampler.Companion.Wrap
import pcg.util.nullIfDefault
import pcg.validate.*
import java.io.File

/**
 * See
 * <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0"/>
 * <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#properties-reference"/>
 */

/**
 * See <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-accessor"/>
 */
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
    init {
        max?.let {
            requireSize(max, type.length, "max")
            hasElementsOf(it, componentType.kClass, "max")
        }
        min?.let {
            requireSize(min, type.length, "min")
            hasElementsOf(it, componentType.kClass, "min")
        }
        byteOffset?.let { require(it % componentType.size == 0) }
    }
}

/**
 * See <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-asset"/>
 */
data class Asset(
    val version: String,
    val minVersion: String? = null
) {
    companion object {
        val default = Asset(
            version = "2.0"
        )
    }
}

data class Buffer(
    val byteLength: Int,
    val uri: String? = null
) {
    init {
        requirePositive(byteLength, "byteLength")
    }
}

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-bufferview"/>
 */
data class BufferView(
    val buffer: Int,
    val byteOffset: Int? = 0,
    val byteLength: Int,
    val byteStride: Int? = null,
    val target: Target? = null
) {
    companion object {
        enum class Target(val target: Int) {
            ARRAY_BUFFER(34962),
            ELEMENT_ARRAY_BUFFER(34963);

            companion object {
                val serializer = JsonSerializer<Target> { src, _, _ -> JsonPrimitive(src.target) }
            }
        }
    }
}

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-gltf"/>
 */
data class Gltf(
    val accessors: List<Accessor>? = null,
    val asset: Asset = Asset.default,
    val buffers: List<Buffer>? = null,
    val bufferViews: List<BufferView>? = null,
    val materials: List<Material>? = null,
    val images: List<Image>? = null,
    val meshes: List<Mesh>? = null,
    val nodes: List<Node>? = null,
    val samplers: List<Sampler>? = null,
    val scene: Int? = null,
    val scenes: List<Scene>? = null,
    val textures: List<Texture>? = null
) {

    init {
        accessors?.let {
            requireNotEmpty(it, "accessors")
            it.forEach { accessor ->
                accessor.bufferView?.let { bufferView ->
                    requireInRange(
                        bufferView,
                        bufferViews,
                        "bufferView"
                    )
                }
            }

            val accessorsByBufferView: Map<Int?, List<Accessor>> = it.groupBy(Accessor::bufferView)
            accessorsByBufferView.forEach { (bufferViewIndex, accessors) ->
                val bufferView = bufferViewIndex?.let { i -> bufferViews?.get(i) }
                require(
                    bufferView == null ||
                            bufferView.target != Target.ARRAY_BUFFER ||
                            accessors.size == 1 ||
                            bufferView.byteStride != null
                ) { "When two or more accessors use the same bufferView, byteStride must be defined." }
            }
        }
        buffers?.let { requireNotEmpty(it, "buffers") }
        bufferViews?.let {
            requireNotEmpty(it, "bufferViews")
            it.forEach { bufferView ->
                requireInRange(bufferView.buffer, buffers, "buffer")
            }
        }
        images?.let {
            requireNotEmpty(it, "images")
        }
        materials?.let {
            requireNotEmpty(it, "materials")
        }
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
        samplers?.let {
            requireNotEmpty(it, "samplers")
        }
        scenes?.let { requireNotEmpty(it, "scenes") }
        scene?.let { requireInRange(it, scenes, "scene") }
        textures?.let {
            requireNotEmpty(it, "textures")
            it.forEach { texture ->
                texture.sampler?.let { sampler -> requireInRange(sampler, samplers, "sampler") }
                texture.source?.let { source -> requireInRange(source, images, "source") }
            }
        }
    }
}

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-image"/>
 */
data class Image(
    val uri: String? = null
)

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-material"/>
 */
data class Material(
    val name: String? = null,
    val pbrMetallicRoughness: PbrMetallicRoughness? = null,
    val doubleSided: Boolean? = null
) {
    companion object {
        fun withoutDefaults(
            name: String?,
            pbrMetallicRoughness: PbrMetallicRoughness?,
            doubleSided: Boolean?
        ) = Material(
            name,
            nullIfDefault(pbrMetallicRoughness, PbrMetallicRoughness.DEFAULT),
            nullIfDefault(doubleSided, false)
        )
    }
}

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-mesh"/>
 */
data class Mesh(
    val primitives: List<Primitive>,
    val name: String? = null
) {
    init {
        requireNotEmpty(primitives, "primitives")
    }
}

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-node"/>
 */
data class Node(
    val children: List<Int>? = null,
    val matrix: FloatArray? = null,
    val mesh: Int? = null,
    val translation: FloatArray? = null
) {
    init {
        children?.let { requireNotEmpty(it, "children") }
        matrix?.let { requireSize(it, 16, "matrix") }
        translation?.let { requireSize(it, 3, "translation") }
        require(matrix == null || translation == null) { "Translation and matrix cannot be defined at the same time" }
    }

    companion object {
        val default = Node(
            matrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        )
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
        if (translation != null) {
            if (other.translation == null) return false
            if (!translation.contentEquals(other.translation)) return false
        } else if (other.translation != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = children?.hashCode() ?: 0
        result = 31 * result + (matrix?.contentHashCode() ?: 0)
        result = 31 * result + (mesh ?: 0)
        result = 31 * result + (translation?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * @see <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-pbrmetallicroughness"/>
 */
data class PbrMetallicRoughness(
    val baseColorFactor: FloatArray? = null,
    val metallicFactor: Float? = null,
    val roughnessFactor: Float? = null
) {

    init {
        baseColorFactor?.let { requireSize(baseColorFactor, 4, "baseColorFactor") }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PbrMetallicRoughness) return false

        if (baseColorFactor != null) {
            if (other.baseColorFactor == null) return false
            if (!baseColorFactor.contentEquals(other.baseColorFactor)) return false
        } else if (other.baseColorFactor != null) return false
        if (metallicFactor != other.metallicFactor) return false
        if (roughnessFactor != other.roughnessFactor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = baseColorFactor?.contentHashCode() ?: 0
        result = 31 * result + (metallicFactor?.hashCode() ?: 0)
        result = 31 * result + (roughnessFactor?.hashCode() ?: 0)
        return result
    }

    companion object {
        private val BASE_COLOR_FACTOR_DEFAULT = floatArrayOf(1f, 1f, 1f, 1f)
        private const val METALLIC_FACTOR_DEFAULT = 1f
        private const val ROUGHNESS_FACTOR_DEFAULT = 1f

        val DEFAULT = PbrMetallicRoughness(null, null, null)

        fun withoutDefaults(
            baseColorFactor: FloatArray?,
            metallicFactor: Float?,
            roughnessFactor: Float?
        ) = PbrMetallicRoughness(
            nullIfDefault(baseColorFactor, BASE_COLOR_FACTOR_DEFAULT),
            nullIfDefault(metallicFactor, METALLIC_FACTOR_DEFAULT),
            nullIfDefault(roughnessFactor, ROUGHNESS_FACTOR_DEFAULT)
        )
    }
}

/**
 * See <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-primitive"/>
 */
data class Primitive(
    val attributes: Map<Attribute, Int>,
    val indices: Int? = null,
    val mode: Mode? = null,
    val material: Int? = null
) {
    init {
        requireNotEmpty(attributes, "attributes")
    }

    companion object {
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
    }
}

/**
 * See <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-sampler"/>
 */
data class Sampler(
    val magFilter: Filter? = null,
    val minFilter: Filter? = null,
    val wrapS: Wrap? = Wrap.Repeat,
    val wrapT: Wrap? = Wrap.Repeat
) {
    init {
        magFilter?.let { it in setOf(Filter.Nearest, Filter.Linear) }
    }

    companion object {

        val default = Sampler()

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
    }
}

data class Scene(
    val nodes: List<Int>? = null,
    val name: String? = null
) {

    init {
        nodes?.let { requireNotEmpty(it, "nodes") }
    }
}

/**
 * See <a href="https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#reference-texture"/>
 */
data class Texture(
    val sampler: Int? = null,
    val source: Int? = null
)

fun Gltf.toJson(prettyPrinting: Boolean = false): String {
    val builder = GsonBuilder()
    builder.disableHtmlEscaping()
    if (prettyPrinting) {
        builder.setPrettyPrinting()
    }
    builder.registerTypeAdapter(Mode::class.java, Mode.serializer)
    builder.registerTypeAdapter(ComponentType::class.java, ComponentType.serializer)
    builder.registerTypeAdapter(Target::class.java, Target.serializer)
    builder.registerTypeAdapter(Filter::class.java, Filter.serializer)
    builder.registerTypeAdapter(Wrap::class.java, Wrap.serializer)
    val gson = builder.create()
    return gson.toJson(this)
}

fun writeToFile(fileName: String, gltf: Gltf) {
    File(fileName).writeText(gltf.toJson(true))
}

