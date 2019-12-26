package pcg.scene

import org.joml.Matrix4f
import pcg.scene.NodeContainer.Companion.NodeContainerBuilder
import pcg.scene.Scene.Companion.SceneBuilder

fun scene(block: SceneBuilder.() -> Unit): Scene = SceneBuilder().apply(block).build()

class Scene(nodes: List<Node>) : NodeContainer(nodes) {

    val rootNodes = nodes

    val allNodes = collectNodes()

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

interface Transform {

    fun getMatrix(m: Matrix4f): Matrix4f
}

class Translation(val dx: Float, val dy: Float, val dz: Float) : Transform {

    override fun getMatrix(m: Matrix4f): Matrix4f = m.apply { setTranslation(dx, dy, dz) }
}
