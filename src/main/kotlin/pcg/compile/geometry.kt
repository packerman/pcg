package pcg.compile

import pcg.gltf.Accessor
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.Buffer
import pcg.gltf.BufferView
import pcg.gltf.BufferView.Companion.Target
import pcg.scene.*
import pcg.scene.Mesh.Companion.Attribute
import pcg.util.align
import pcg.util.allTheSame
import pcg.util.fillBytes
import pcg.util.remaining
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import pcg.gltf.Primitive.Companion.Attribute as GltfAttribute

class GeometryCompiler(geometry: Geometry, offset: Offset) {

    init {
        require(geometry.meshes.size == 1) { "Only one mesh in geometry is supported so far (Constraint to be removed)" }
    }

    private val compiledMesh = MeshCompiler(geometry.meshes[0], offset)

    val accessors: Collection<Accessor> by lazy { compiledMesh.accessors }

    val bufferViews: Collection<BufferView> by lazy { compiledMesh.bufferViews }

    val buffer: Buffer by lazy { compiledMesh.buffer }

    val attributes: Map<GltfAttribute, Int> = compiledMesh.attributes

    val indices: Map<Int, Int> = compiledMesh.indices

    val offset: Offset = compiledMesh.offset
}

class MeshCompiler(private val mesh: Mesh, private val baseOffset: Offset) {

    private val vertexAccessors: List<Accessor> by lazy { createVertexAccessors() }

    private val indexAccessors: List<Accessor> by lazy { createIndexAccessors() }

    val accessors: Collection<Accessor> by lazy { indexAccessors + vertexAccessors }

    val bufferViews: Collection<BufferView> by lazy { createBufferViews() }

    val buffer: Buffer by lazy { createBuffer() }

    val attributes: Map<GltfAttribute, Int> = mesh.vertexArrays.keys.mapIndexed { index, attribute ->
        attributeMap.getValue(attribute) to baseOffset.accessor + index + indexAccessors.size
    }.toMap()

    val indices: Map<Int, Int> = mesh.indexArrays.mapIndexed { index, indexArray ->
        baseOffset.accessor + index to indexArray.material
    }.toMap()

    val offset = Offset(
        buffer = 1,
        bufferView = if (mesh.indexArrays.isEmpty()) 1 else 2,
        accessor = mesh.indexArrays.size + mesh.vertexArrays.size
    )

    private fun createIndexAccessors(): List<Accessor> {
        val offsets = getIndexByteOffsets(mesh.indexArrays)
        return mesh.indexArrays.mapIndexed { index, indexArray ->
            Accessor(
                bufferView = baseOffset.bufferView,
                byteOffset = offsets[index],
                componentType = ComponentType.UNSIGNED_SHORT,
                count = indexArray.count,
                type = Type.SCALAR,
                max = when (indexArray) {
                    is ShortIndexArray -> listOf(indexArray.max)
                    else -> unknownIndexArrayTypeError(indexArray)
                },
                min = when (indexArray) {
                    is ShortIndexArray -> listOf(indexArray.min)
                    else -> unknownIndexArrayTypeError(indexArray)
                }
            )
        }
    }

    private fun createVertexAccessors(): List<Accessor> {

        val byteOffsets = getVertexByteOffsets(mesh.vertexArrays)

        return mesh.vertexArrays.map { (attribute, vertexArray) ->
            Accessor(
                bufferView = if (mesh.indexArrays.isEmpty()) baseOffset.bufferView else baseOffset.bufferView + 1,
                byteOffset = requireNotNull(byteOffsets[attribute]) { "Unknown attribute: $attribute" },
                componentType = when (vertexArray) {
                    is Float3VertexArray -> ComponentType.FLOAT
                    else -> unknownVertexArrayTypeError(vertexArray)
                },
                count = vertexArray.count,
                type = when (vertexArray) {
                    is Float3VertexArray -> Type.VEC3
                    else -> unknownVertexArrayTypeError(vertexArray)
                },
                max = when (vertexArray) {
                    is Float3VertexArray -> listOf(vertexArray.max.x(), vertexArray.max.y(), vertexArray.max.z())
                    else -> unknownVertexArrayTypeError(vertexArray)
                },
                min = when (vertexArray) {
                    is Float3VertexArray -> listOf(vertexArray.min.x(), vertexArray.min.y(), vertexArray.min.z())
                    else -> unknownVertexArrayTypeError(vertexArray)
                }
            )
        }
    }

    private fun createBufferViews(): List<BufferView> {
        require(allTheSame(mesh.vertexArrays.values.map(VertexArray<*>::byteStride))) { "All Vertex Arrays need to have the same byteStride (Constraint to be removed)" }
        val bufferViews = mutableListOf<BufferView>()
        if (mesh.indexArrays.isNotEmpty()) {
            bufferViews.add(
                BufferView(
                    buffer = baseOffset.buffer,
                    byteOffset = 0,
                    byteLength = mesh.indexArrays.byteSize,
                    target = Target.ELEMENT_ARRAY_BUFFER
                )
            )
        }
        bufferViews.add(
            BufferView(
                buffer = baseOffset.buffer,
                byteOffset = align(mesh.indexArrays.byteSize, 4),
                byteLength = mesh.vertexArrays.values.byteSize,
                byteStride = if (mesh.vertexArrays.size > 1) mesh.vertexArrays.values.first().byteStride else null,
                target = Target.ARRAY_BUFFER
            )
        )

        return bufferViews
    }

    private fun createBuffer(): Buffer {
        val byteArray = ByteArray(mesh.alignedByteSize)
        val byteBuffer = ByteBuffer
            .wrap(byteArray)
            .order(ByteOrder.LITTLE_ENDIAN)
        mesh.indexArrays.forEach { indexArray ->
            indexArray.copyToByteBuffer(byteBuffer)
        }
        byteBuffer.fillBytes(remaining(mesh.indexArrays.byteSize, 4))
        mesh.vertexArrays.values.forEach { it.copyToByteBuffer(byteBuffer) }

        return Buffer(
            byteLength = mesh.alignedByteSize,
            uri = BASE64_DATA_URI_PREFIX + "," + getBase64Encoder().encodeToString(byteArray)
        )
    }

    companion object {

        val attributeMap = mapOf(
            Attribute.Position to GltfAttribute.POSITION,
            Attribute.Normal to GltfAttribute.NORMAL,
            Attribute.TexCoord to GltfAttribute.TEXCOORD_0
        )

        fun compileGeometries(geometries: Set<Geometry>): Map<Geometry, GeometryCompiler> =
            mutableMapOf<Geometry, GeometryCompiler>().apply {
                var offset = Offset()
                for (geometry in geometries) {
                    val compiled = GeometryCompiler(geometry, offset)
                    put(geometry, compiled)
                    offset += compiled.offset
                }
            }

        private fun getVertexByteOffsets(vertexArrays: Map<Attribute, VertexArray<*>>): Map<Attribute, Int> =
            mutableMapOf<Attribute, Int>().apply {
                var offset = 0
                for ((attribute, vertexArray) in vertexArrays) {
                    this[attribute] = offset
                    offset += vertexArray.byteSize
                }
            }

        private fun getIndexByteOffsets(indexArrays: List<IndexArray<*>>): Map<Int, Int> =
            mutableMapOf<Int, Int>().apply {
                var offset = 0
                indexArrays.forEachIndexed { index, indexArray ->
                    this[index] = offset
                    offset += indexArray.byteSize
                }
            }

        private fun unknownVertexArrayTypeError(vertexArray: VertexArray<*>): Nothing =
            error("Unknown Vertex Array type: ${vertexArray::class}")

        private fun unknownIndexArrayTypeError(indexArray: IndexArray<*>): Nothing =
            error("Unknown Vertex Array type: ${indexArray::class}")

        private fun getBase64Encoder() = Base64.getEncoder()

        private const val BASE64_DATA_URI_PREFIX = "data:application/octet-stream;base64"
    }
}

data class Offset(
    val buffer: Int = 0,
    val bufferView: Int = 0,
    val accessor: Int = 0
) {
    operator fun plus(other: Offset): Offset =
        Offset(
            buffer + other.buffer,
            bufferView + other.bufferView,
            accessor + other.accessor
        )
}
