package pcg.compile

import pcg.compile.MeshCompiler.Companion.compileGeometries
import pcg.gltf.Gltf
import pcg.gltf.Primitive
import pcg.scene.*
import pcg.util.emptyToNull
import pcg.util.indexElements
import pcg.util.indexUniqueElements
import pcg.gltf.Material as GltfMaterial
import pcg.gltf.Mesh as GltfMesh
import pcg.gltf.Node as GltfNode
import pcg.gltf.Scene as GltfScene
import pcg.gltf.Texture as GltfTexture

fun compile(scene: Scene, options: CompileOptions = CompileOptions()): Gltf =
    SceneCompiler(options, scene).compile()

data class CompileOptions(
    val interleaved: Boolean = false
)

class SceneCompiler(options: CompileOptions, private val scene: Scene) {

    fun compile() = Gltf(
        scenes = if (scene.nodes.isNotEmpty())
            listOf(
                GltfScene(
                    nodes = scene.nodes.indices.toList()
                )
            ) else null,
        materials = if (materialIndex.isEmpty()) null else materialIndex.keys.toList(),
        meshes = meshIndex.keys.toList().emptyToNull(),
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
        }.emptyToNull(),
        accessors = compiledGeometries.values.flatMap(GeometryCompiler::accessors).emptyToNull(),
        bufferViews = compiledGeometries.values.flatMap { it.bufferViews }.emptyToNull(),
        buffers = compiledGeometries.values.map(GeometryCompiler::buffer).emptyToNull(),
        textures = compiledTextures.values.mapIndexed { i, compiledTexture ->
            GltfTexture(
                source = i,
                sampler = samplerIndex.getValue(compiledTexture.sampler)
            )
        }.emptyToNull(),
        images = compiledTextures.values.map(TextureCompiler::image).emptyToNull(),
        samplers = samplerIndex.keys.toList().emptyToNull()
    )

    private val compiledGeometries: Map<Geometry, GeometryCompiler> = compileGeometries(options, scene.geometries)

    private val textures = scene.materials.mapNotNull { it.diffuseTexture }.toSet()
    private val compiledTextures: Map<Texture, TextureCompiler> = textures.map { it to TextureCompiler(it) }.toMap()
    private val samplerIndex = indexElements(compiledTextures.values.map(TextureCompiler::sampler))
    private val textureIndex = indexUniqueElements(textures)

    private val compiledMaterials: Map<Material, GltfMaterial> =
        scene.materials.map { it to it.compile(textureIndex) }.toMap()
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
