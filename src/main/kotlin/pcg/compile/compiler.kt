package pcg.compile

import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.BufferView.Companion.Target
import pcg.scene.*
import pcg.scene.Mesh
import pcg.scene.Mesh.Companion.Attribute
import pcg.scene.Node
import pcg.scene.Scene
import pcg.util.indexElements
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import pcg.gltf.Mesh as GltfMesh
import pcg.gltf.Node as GltfNode
import pcg.gltf.Primitive.Companion.Attribute as GltfAttribute
import pcg.gltf.Scene as GltfScene

fun compile(scene: Scene): Gltf =
    SceneCompiler(scene).compile()

class SceneCompiler(private val scene: Scene) {

    fun compile() = Gltf(
        scenes = listOf(
            GltfScene(
                nodes = scene.nodes.indices.toList()
            )
        ),
        nodes = scene.nodes.map { node ->
            GltfNode(
                mesh = meshByNode[node]?.let { meshIndex.getValue(it) }
            )
        },
        meshes = meshIndex.keys.toList(),
        accessors = compiledGeometries.values.flatMap(GeometryCompiler::accessors),
        bufferViews = compiledGeometries.values.flatMap { it.bufferViews },
        buffers = compiledGeometries.values.map(GeometryCompiler::buffer)
    )

    private val geometriesByNode: Map<in Node, Geometry>
        get() = scene.nodes.mapNotNull { n -> n as? GeometryNode }
            .map { n -> n to n.geometry }
            .toMap()

    private val geometries = geometriesByNode.values.toSet()

    private val compiledGeometries = geometries.map { it to GeometryCompiler(it) }.toMap()

    private val meshByNode: Map<Node, GltfMesh> = scene.nodes
        .mapNotNull { it as? GeometryNode }
        .map { node ->
            val compiledGeometry = compiledGeometries.getValue(node.geometry)
            val mesh = GltfMesh(
                primitives = if (compiledGeometry.indices.isEmpty()) listOf(
                    Primitive(
                        attributes = compiledGeometry.attributes
                    )
                ) else compiledGeometry.indices.map {
                    Primitive(
                        attributes = compiledGeometry.attributes,
                        indices = it
                    )
                }
            )
            node to mesh
        }
        .toMap()

    private val meshIndex = indexElements(meshByNode.values)
}

class GeometryCompiler(geometry: Geometry) {

    private val compiledMesh = MeshCompiler(geometry.meshes[0])

    val accessors: Collection<Accessor> by lazy { compiledMesh.accessors }

    val bufferViews: Collection<BufferView> by lazy { compiledMesh.bufferViews }

    val buffer: Buffer by lazy { compiledMesh.buffer }

    val attributes: Map<GltfAttribute, Int> = compiledMesh.attributes

    val indices: List<Int> = compiledMesh.indices
}

class MeshCompiler(private val mesh: Mesh) {

    private val vertexAccessors: List<Accessor> by lazy { createVertexAccessors() }

    private val indexAccessors: List<Accessor> by lazy { createIndexAccessors() }

    val accessors: Collection<Accessor> by lazy { indexAccessors + vertexAccessors }

    val bufferViews: Collection<BufferView> by lazy { createBufferViews() }

    val buffer: Buffer by lazy { createBuffer() }

    val attributes: Map<GltfAttribute, Int> = mesh.vertexArrays.keys.mapIndexed { index, attribute ->
        attributeMap.getValue(attribute) to index + indexAccessors.size
    }.toMap()

    val indices: List<Int> = indexAccessors.indices.toList()

    private fun createIndexAccessors(): List<Accessor> {
        return mesh.indexArrays.map { indexArray ->
            Accessor(
                bufferView = 0,
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
                bufferView = if (mesh.indexArrays.isEmpty()) 0 else 1,
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
        val bufferViews = mutableListOf<BufferView>()
        if (mesh.indexArrays.isNotEmpty()) {
            bufferViews.add(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = mesh.indexArrays.byteSize,
                    target = Target.ELEMENT_ARRAY_BUFFER
                )
            )
        }
        bufferViews.add(
            BufferView(
                buffer = 0,
                byteOffset = mesh.indexArrays.alignedByteSize,
                byteLength = mesh.vertexArrays.values.byteSize,
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
            val remaining = indexArray.alignedByteSize - indexArray.byteSize
            (1..remaining).forEach { _ ->
                byteBuffer.put(0)
            }
        }
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

        private fun getVertexByteOffsets(vertexArrays: Map<Attribute, VertexArray<*>>): Map<Attribute, Int> =
            mutableMapOf<Attribute, Int>().apply {
                var offset = 0
                for ((attribute, vertexArray) in vertexArrays) {
                    this[attribute] = offset
                    offset += vertexArray.byteSize
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
