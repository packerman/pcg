package pcg.compile

import pcg.common.BufferTarget
import pcg.gltf.Accessor
import pcg.gltf.Buffer
import pcg.gltf.BufferView
import pcg.scene.*
import pcg.util.align
import pcg.util.fillBytes
import pcg.util.remaining
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import pcg.common.Attribute as GltfAttribute

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

    private val vertexSerializer = StandardVertexSerializer(mesh.vertexArrays)

    private val vertexAccessors: List<Accessor> by lazy {
        vertexSerializer.createVertexAccessors(baseOffset.bufferView + if (mesh.indexArrays.isEmpty()) 0 else 1)
    }

    val accessors: Collection<Accessor> by lazy { createIndexAccessors() + vertexAccessors }

    private val vertexBufferViews: List<BufferView> by lazy {
        vertexSerializer.createVertexBufferViews(baseOffset.buffer, align(mesh.indexArrays.byteSize, 4))
    }

    val bufferViews: Collection<BufferView> by lazy { createIndexBufferViews() + vertexBufferViews }

    val buffer: Buffer by lazy { createBuffer() }

    val attributes: Map<GltfAttribute, Int> = vertexSerializer.attributes
        .mapIndexed { index, vertexArray ->
            attributeMap.getValue(vertexArray.attribute) to baseOffset.accessor + mesh.indexArrays.size + index
        }.toMap()

    val indices: Map<Int, Int> = mesh.indexArrays.mapIndexed { index, indexArray ->
        baseOffset.accessor + index to indexArray.material
    }.toMap()

    val offset = Offset(
        buffer = 1,
        bufferView = vertexBufferViews.size + if (mesh.indexArrays.isEmpty()) 0 else 1,
        accessor = mesh.indexArrays.size + vertexAccessors.size
    )

    private fun createIndexAccessors(): List<Accessor> {
        val offsets = getIndexByteOffsets(mesh.indexArrays)
        return mesh.indexArrays.mapIndexed { index, indexArray ->
            val accessorData = indexArray.accessorData
            Accessor(
                bufferView = baseOffset.bufferView,
                byteOffset = offsets[index],
                componentType = accessorData.componentType,
                count = indexArray.count,
                type = accessorData.type,
                max = accessorData.max,
                min = accessorData.min
            )
        }
    }

    private fun createIndexBufferViews(): List<BufferView> =
        if (mesh.indexArrays.isEmpty()) emptyList()
        else listOf(
            BufferView(
                buffer = baseOffset.buffer,
                byteOffset = 0,
                byteLength = mesh.indexArrays.byteSize,
                target = BufferTarget.ELEMENT_ARRAY_BUFFER
            )
        )

    private fun createBuffer(): Buffer {
        val byteArray = ByteArray(mesh.alignedByteSize)
        val byteBuffer = ByteBuffer
            .wrap(byteArray)
            .order(ByteOrder.LITTLE_ENDIAN)
        mesh.indexArrays.forEach { indexArray ->
            indexArray.copyToByteBuffer(byteBuffer)
        }
        byteBuffer.fillBytes(remaining(mesh.indexArrays.byteSize, 4))

        vertexSerializer.copyToByteBuffer(byteBuffer)

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

        private fun getIndexByteOffsets(indexArrays: List<IndexArray<*>>): Map<Int, Int> =
            mutableMapOf<Int, Int>().apply {
                var offset = 0
                indexArrays.forEachIndexed { index, indexArray ->
                    this[index] = offset
                    offset += indexArray.byteSize
                }
            }

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
