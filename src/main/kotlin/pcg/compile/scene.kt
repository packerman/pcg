package pcg.compile

import pcg.compile.MeshCompiler.Companion.compileGeometries
import pcg.gltf.Gltf
import pcg.gltf.Primitive
import pcg.scene.*
import pcg.util.indexElements
import pcg.gltf.Material as GltfMaterial
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
        materials = if (materialIndex.isEmpty()) null else materialIndex.keys.toList(),
        meshes = meshIndex.keys.toList(),
        nodes = scene.nodes.map { node ->
            check(node.transforms.size <= 1) { "Only one transform is supported so far (Constraint to be removed)" }
            GltfNode(
                mesh = meshByNode[node]?.let { meshIndex.getValue(it) },
                translation = node.transforms.firstOrNull()?.let { transform ->
                    (transform as? Translation)?.let { translation ->
                        floatArrayOf(translation.dx, translation.dy, transform.dz)
                    }
                }
            )
        },
        accessors = compiledGeometries.values.flatMap(GeometryCompiler::accessors),
        bufferViews = compiledGeometries.values.flatMap { it.bufferViews },
        buffers = compiledGeometries.values.map(GeometryCompiler::buffer)
    )

    private val compiledGeometries: Map<Geometry, GeometryCompiler> = compileGeometries(scene.geometries)

    private val compiledMaterials: Map<Material, GltfMaterial> = scene.materials.map { it to it.compile() }.toMap()
    private val materialIndex: Map<GltfMaterial, Int> = indexElements(compiledMaterials.values)

    private val meshByNode: Map<Node, GltfMesh> = scene.nodes
        .mapNotNull { it as? GeometryNode }
        .map { node ->
            val compiledGeometry = compiledGeometries.getValue(node.geometry)
            val mesh = GltfMesh(
                primitives = if (compiledGeometry.indices.isEmpty()) listOf(
                    Primitive(
                        attributes = compiledGeometry.attributes,
                        material = getCompiledMaterialIndex(node, 0)
                    )
                ) else compiledGeometry.indices.map { (index, material) ->
                    Primitive(
                        attributes = compiledGeometry.attributes,
                        material = getCompiledMaterialIndex(node, material),
                        indices = index
                    )
                }
            )
            node to mesh
        }
        .toMap()

    private fun getCompiledMaterialIndex(node: GeometryNode, index: Int) =
        node.materials[index]?.let { compiledMaterials[it] }?.let { materialIndex.getValue(it) }

    private val meshIndex: Map<GltfMesh, Int> = indexElements(meshByNode.values)
}
