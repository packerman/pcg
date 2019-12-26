package pcg.scene

import pcg.scene.NodeContainer.Companion.NodeContainerBuilder
import pcg.scene.Scene.Companion.SceneBuilder

fun scene(block: SceneBuilder.() -> Unit): Scene = SceneBuilder().apply(block).build()

class Scene(nodes: List<Node>) : NodeContainer(nodes) {

    val rootNodes: List<Node> = nodes

    val allNodes: List<Node> = collectNodes()

    val geometries: Set<Geometry>
        get() = allNodes.mapNotNull { n -> n as? GeometryNode }
            .map { it.geometry }
            .toSet()

    val materials: Set<Material>
        get() = allNodes.mapNotNull { it as? GeometryNode }
            .flatMap { it.materials.values }
            .toSet()

    companion object {

        class SceneBuilder : NodeContainerBuilder() {

            override fun build(): Scene = Scene(nodes)
        }
    }
}
