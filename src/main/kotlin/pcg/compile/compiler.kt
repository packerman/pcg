package pcg.compile

import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.scene.Float3VertexArray
import pcg.scene.Geometry
import pcg.scene.GeometryNode
import pcg.scene.Mesh.Companion.Attribute
import pcg.scene.Scene
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
        accessors = compiledGeometries.flatMap(GeometryCompiler::accessors)
    )
}

private val Scene.geometries: Set<Geometry>
    get() = nodes
        .flatMap { n -> if (n is GeometryNode) listOf(n.geometry) else emptyList() }
        .toSet()

class GeometryCompiler(private val geometry: Geometry) {

    val accessorMap: Map<GltfAttribute, Accessor> by lazy { createAccessors() }

    val accessors: Collection<Accessor> = accessorMap.values

    private fun createAccessors(): Map<GltfAttribute, Accessor> {
        val mesh = geometry.meshes[0]

        return mesh.vertexArrays.map { (attribute, vertexArray) ->
            val resultAttribute = requireNotNull(attributeMap[attribute]) { "Unknown attribute: $attribute" }
            val accessor = Accessor(
                componentType = when (vertexArray) {
                    is Float3VertexArray -> ComponentType.FLOAT
                    else -> error("Unknown Vertex Array type: ${vertexArray::class}")
                },
                count = vertexArray.count,
                type = when (vertexArray) {
                    is Float3VertexArray -> Type.VEC3
                    else -> error("Unknown Vertex Array type: ${vertexArray::class}")
                }
            )
            return@map resultAttribute to accessor
        }.toMap()
    }

    companion object {
        val attributeMap = mapOf(
            Attribute.Position to GltfAttribute.POSITION,
            Attribute.Normal to GltfAttribute.NORMAL,
            Attribute.TexCoord to GltfAttribute.TEXCOORD_0
        )
    }
}