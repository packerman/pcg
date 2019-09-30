package pcg.scene

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.scene.Geometry.Companion.GeometryBuilder
import pcg.scene.Mesh.Companion.MeshBuilder
import pcg.scene.Mesh.Companion.Primitive
import pcg.validate.requireNotEmpty

class IndexArray {

}

fun geometry(block: GeometryBuilder.() -> Unit): Geometry = GeometryBuilder().apply(block).build()

class Geometry(val meshes: List<Mesh>) {

    constructor(mesh: Mesh) : this(listOf(mesh))

    init {
        requireNotEmpty(meshes, "meshes")
    }

    companion object {
        class GeometryBuilder : Builder<Geometry> {

            override fun build(): Geometry {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }
}

fun mesh(
    primitive: Primitive = Primitive.Triangles,
    block: MeshBuilder.() -> Unit
): Mesh = MeshBuilder(primitive).apply(block).build()

class Mesh(
    val primitive: Primitive = Primitive.Triangles,
    val vertexArrays: List<VertexArray>,
    val indexArrays: List<IndexArray>
) {

    init {
        requireNotEmpty(vertexArrays, "vertexArrays")
    }

    companion object {
        enum class Primitive {
            Points,
            Lines,
            LineStrip,
            Triangles,
            TriangleStrip,
            Quads
        }

        class MeshBuilder(val primitive: Primitive) : Builder<Mesh> {

            private val vertexArrays = mutableListOf<VertexArray>()
            private val indexArrays = mutableListOf<IndexArray>()

            override fun build(): Mesh {
                return Mesh(primitive, vertexArrays, indexArrays)
            }
        }
    }
}

open class VertexArray {

}

class Float3VertexArray(val vertices: Array<Vector3fc>) : VertexArray() {

    companion object {

        class Float3VertexArrayBuilder : Builder<VertexArray> {
            private val vertices = mutableListOf<Vector3fc>()

            fun add(x: Float, y: Float, z: Float) {
                vertices.add(Vector3f(x, y, z))
            }

            override fun build(): VertexArray = Float3VertexArray(vertices.toTypedArray())
        }
    }
}

interface Builder<out T> {
    fun build(): T
}

fun main() {
    val g = geometry {
        mesh {

        }
    }
}
