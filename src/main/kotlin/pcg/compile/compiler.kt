package pcg.compile

import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.BufferView.Companion.Target
import pcg.scene.*
import pcg.scene.Mesh.Companion.Attribute
import pcg.scene.Node
import pcg.scene.Scene
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import pcg.gltf.Mesh as GltfMesh
import pcg.gltf.Node as GltfNode
import pcg.gltf.Primitive.Companion.Attribute as GltfAttribute
import pcg.gltf.Scene as GltfScene

fun compile(scene: Scene): Gltf {
    val geometriesByNode: Map<in Node, Geometry> = scene.geometriesByNode
    val geometries = geometriesByNode.values.toSet()
    val compiledGeometries = geometries.map { it to GeometryCompiler(it) }.toMap()

    val hasIndices = scene.geometries
        .map { g -> g.meshes[0] }
        .any { m -> m.indexArrays.isNotEmpty() }


    return Gltf(
        scene = 0,
        scenes = listOf(
            GltfScene(
                scene.nodes.mapIndexed { n, _ -> n }
            )
        ),
        nodes = scene.nodes.map { _ -> GltfNode(mesh = 0) },
        meshes = listOf(
            GltfMesh(
                primitives = listOf(
                    Primitive(
                        mapOf(
                            GltfAttribute.POSITION to (if (hasIndices) 1 else 0)
                        ),
                        indices = if (hasIndices) 0 else null
                    )
                )
            )
        ),
        accessors = compiledGeometries.values.flatMap(GeometryCompiler::accessors),
        bufferViews = compiledGeometries.values.flatMap { it.bufferViews },
        buffers = compiledGeometries.values.map(GeometryCompiler::buffer)
    )
}

private val Scene.geometries: Set<Geometry>
    get() = nodes
        .flatMap { n -> if (n is GeometryNode) listOf(n.geometry) else emptyList() }
        .toSet()

private val Scene.geometriesByNode: Map<in Node, Geometry>
    get() = nodes
        .map { n -> n as? GeometryNode }
        .filterNotNull()
        .map { n -> n to n.geometry }
        .toMap()

class GeometryCompiler(private val geometry: Geometry) {

    val accessorMap: Map<GltfAttribute, Accessor> by lazy { createVertexAccessors() }

    private val indexAccessors: List<Accessor> by lazy { createIndexAccessors() }

    val accessors: Collection<Accessor> by lazy { indexAccessors + accessorMap.values }

    val bufferViews: Collection<BufferView> by lazy { createBufferViews() }

    val buffer: Buffer by lazy { createBuffer() }

    private val mesh = geometry.meshes[0]

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

    private fun createVertexAccessors(): Map<GltfAttribute, Accessor> {

        val byteOffsets = getVertexByteOffsets(mesh.vertexArrays)

        return mesh.vertexArrays.map { (attribute, vertexArray) ->
            val resultAttribute = requireNotNull(attributeMap[attribute]) { "Unknown attribute: $attribute" }
            val accessor = Accessor(
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
            return@map resultAttribute to accessor
        }.toMap();
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

        private fun <T> indexSet(elements: Set<T>): Map<T, Int> = mutableMapOf<T, Int>().apply {
            elements.forEachIndexed { index, elem ->
                putIfAbsent(elem, index)
            }
        }
    }
}
