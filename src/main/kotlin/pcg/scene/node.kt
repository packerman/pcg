package pcg.scene

import org.joml.Vector3fc
import pcg.scene.GeometryNode.Companion.GeometryNodeBuilder
import pcg.scene.Node.Companion.NodeBuilder
import pcg.scene.NodeContainer.Companion.NodeContainerBuilder

open class NodeContainer(val nodes: List<Node>) {

    companion object {

        open class NodeContainerBuilder : Builder<NodeContainer> {

            protected val nodes = mutableListOf<Node>()

            fun node(geometry: Geometry, block: GeometryNodeBuilder.() -> Unit = {}) {
                nodes.add(GeometryNodeBuilder(geometry).apply(block).build())
            }

            fun node(block: NodeBuilder.() -> Unit = {}) {
                nodes.add(NodeBuilder().apply(block).build())
            }

            override fun build(): NodeContainer {
                return NodeContainer(nodes)
            }
        }
    }
}

fun NodeContainer.collectNodes(): List<Node> {
    fun MutableList<Node>.collectNodes(container: NodeContainer): MutableList<Node> {
        container.nodes.forEach { node ->
            add(node)
            collectNodes(node)
        }
        return this
    }
    return mutableListOf<Node>().collectNodes(this)
}

open class Node(val transforms: List<Transform>, val name: String? = null, nodes: List<Node>) : NodeContainer(nodes) {

    companion object {

        open class NodeBuilder : NodeContainerBuilder() {

            var name: String? = null

            protected val transforms = mutableListOf<Transform>()

            fun translate(dx: Float, dy: Float, dz: Float) {
                transforms.add(Translation(dx, dy, dz))
            }

            fun rotate(angleInDegrees: Float, axis: Vector3fc) {
                transforms.add(Rotation(angleInDegrees, axis))
            }

            fun scale(sx: Float, sy: Float, sz: Float) {
                transforms.add(Scale(sx, sy, sz))
            }

            override fun build() = Node(transforms, name, nodes)
        }
    }
}

class GeometryNode(
    val geometry: Geometry,
    val materials: Map<Int, Material>,
    transforms: List<Transform>,
    nodes: List<Node>,
    name: String?
) : Node(transforms, name, nodes) {

    init {
        for (mesh in geometry.meshes) {
            require((mesh.indexArrays.isEmpty() && materials.size == 1 && 0 in materials) ||
                    (mesh.indexArrays.all { it.material in materials } &&
                            materials.keys.all { it in mesh.indexArrays.indices })
            )
            { "Node does not have a material or index arrays and materials do not match" }
        }
    }

    companion object {

        class GeometryNodeBuilder(private val geometry: Geometry) : NodeBuilder() {

            private val materials = mutableMapOf<Int, Material>()

            fun material(index: Int = 0, material: Material) {
                require(index !in materials) { "Node already has some material for index $index" }
                materials[index] = material
            }

            fun material(material: Material) = material(0, material)

            fun material(
                name: String? = null,
                twoSided: Boolean = false,
                diffuse: Color = Color(1f, 1f, 1f),
                specular: Color = Color(0f, 0f, 0f),
                emission: Color = Color(0f, 0f, 0f),
                opacity: Color = Color(1f, 1f, 1f),
                transparency: Color = Color(0f, 0f, 0f),
                specularPower: Float = 1f,
                diffuseTexture: Texture? = null,
                specularTexture: Texture? = null,
                specularPowerTexture: Texture? = null,
                emissionTexture: Texture? = null,
                opacityTexture: Texture? = null,
                transparencyTexture: Texture? = null,
                normalTexture: Texture? = null
            ) = material(
                Material(
                    name,
                    twoSided,
                    diffuse,
                    specular,
                    emission,
                    opacity,
                    transparency,
                    specularPower,
                    diffuseTexture,
                    specularTexture,
                    specularPowerTexture,
                    emissionTexture,
                    opacityTexture,
                    transparencyTexture,
                    normalTexture
                )
            )

            override fun build(): Node = GeometryNode(geometry, materials, transforms, nodes, name)
        }
    }
}
