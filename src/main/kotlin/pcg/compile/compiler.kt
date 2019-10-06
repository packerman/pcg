package pcg.compile

import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.BufferView.Companion.Target
import pcg.gltf.Mesh
import pcg.gltf.Node
import pcg.scene.*
import pcg.scene.Mesh.Companion.Attribute
import pcg.scene.Scene
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import pcg.gltf.Primitive.Companion.Attribute as GltfAttribute
import pcg.gltf.Scene as GltfScene

fun compile(scene: Scene): Gltf {
    val geometries = scene.geometries
    val compiledGeometries = geometries.map(::GeometryCompiler)

    return Gltf(
        scene = 0,
        scenes = listOf(
            GltfScene(
                scene.nodes.mapIndexed { n, _ -> n }
            )
        ),
        nodes = scene.nodes.map { _ -> Node(mesh = 0) },
        meshes = listOf(
            Mesh(
                primitives = listOf(
                    Primitive(
                        mapOf(
                            GltfAttribute.POSITION to 0
                        )
                    )
                )
            )
        ),
        accessors = compiledGeometries.flatMap(GeometryCompiler::accessors),
        bufferViews = compiledGeometries.flatMap { it.bufferViews },
        buffers = compiledGeometries.map(GeometryCompiler::buffer)
    )
}

private val Scene.geometries: Set<Geometry>
    get() = nodes
        .flatMap { n -> if (n is GeometryNode) listOf(n.geometry) else emptyList() }
        .toSet()

//TODO - this should be mesh compiler
class GeometryCompiler(private val geometry: Geometry) {

    val accessorMap: Map<GltfAttribute, Accessor> by lazy { createAccessors() }

    val accessors: Collection<Accessor> = accessorMap.values

    val bufferViews: Collection<BufferView> by lazy { createBufferViews() }

    val buffer: Buffer by lazy { createBuffer() }

    private fun createAccessors(): Map<GltfAttribute, Accessor> {
        val mesh = geometry.meshes[0]

        return mesh.vertexArrays.map { (attribute, vertexArray) ->
            val resultAttribute = requireNotNull(attributeMap[attribute]) { "Unknown attribute: $attribute" }
            val accessor = Accessor(
                bufferView = 0,
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
        }.toMap()
    }

    private fun createBufferViews(): List<BufferView> {
        val mesh = geometry.meshes[0]

        return listOf(
            BufferView(
                buffer = 0,
                byteLength = mesh.byteSize,
                target = Target.ARRAY_BUFFER
            )
        )
    }

    private fun createBuffer(): Buffer {
        val mesh = geometry.meshes[0]

        val byteArray = ByteArray(mesh.byteSize)
        val byteBuffer = ByteBuffer
            .wrap(byteArray)
            .order(ByteOrder.LITTLE_ENDIAN)
        mesh.vertexArrays.values.forEach { it.copyToByteBuffer(byteBuffer) }

        return Buffer(
            byteLength = mesh.byteSize,
            uri = BASE64_DATA_URI_PREFIX + "," + getBase64Encoder().encodeToString(byteArray)
        )
    }

    companion object {
        val attributeMap = mapOf(
            Attribute.Position to GltfAttribute.POSITION,
            Attribute.Normal to GltfAttribute.NORMAL,
            Attribute.TexCoord to GltfAttribute.TEXCOORD_0
        )

        private fun unknownVertexArrayTypeError(vertexArray: VertexArray<*>): Nothing =
            error("Unknown Vertex Array type: ${vertexArray::class}")

        private fun getBase64Encoder() = Base64.getUrlEncoder()

        private const val BASE64_DATA_URI_PREFIX = "data:application/octet-stream;base64"
    }
}
