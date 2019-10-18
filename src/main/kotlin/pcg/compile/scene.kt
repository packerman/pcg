package pcg.compile

import pcg.gltf.Gltf
import pcg.gltf.Primitive
import pcg.scene.Geometry
import pcg.scene.GeometryNode
import pcg.scene.Node
import pcg.scene.Scene
import pcg.util.indexElements
import pcg.gltf.Mesh as GltfMesh
import pcg.gltf.Node as GltfNode
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
