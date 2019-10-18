package pcg.scene

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.scene.Float3VertexArray.Companion.Float3VertexArrayBuilder
import pcg.scene.Geometry.Companion.GeometryBuilder
import pcg.scene.Mesh.Companion.Attribute.Normal
import pcg.scene.Mesh.Companion.Attribute.Position
import pcg.scene.Mesh.Companion.MeshBuilder
import pcg.scene.Mesh.Companion.Primitive
import pcg.scene.Mesh.Companion.Primitive.Triangles
import pcg.scene.Scene.Companion.SceneBuilder
import pcg.scene.ShortIndexArray.Companion.ShortIndexArrayBuilder
import pcg.validate.requireNotEmpty
import java.nio.ByteBuffer

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f)

interface IndexArray<T> : ByteSized {
    val count: Int

    val max: T
    val min: T

    fun copyToByteBuffer(byteBuffer: ByteBuffer)
}

class ShortIndexArray(private val indices: ShortArray) : IndexArray<Short> {
    override val byteSize: Int = 2 * indices.size

    override val count: Int = indices.size

    override val max: Short = indices.max() ?: Short.MIN_VALUE

    override val min: Short = indices.min() ?: Short.MAX_VALUE

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) = with(byteBuffer) {
        for (index in indices) {
            putShort(index)
        }
    }

    companion object {
        class ShortIndexArrayBuilder : Builder<ShortIndexArray> {
            private val indices = mutableListOf<Short>()

            fun add(i: Short, j: Short, k: Short) {
                indices.add(i)
                indices.add(j)
                indices.add(k)
            }

            override fun build(): ShortIndexArray = ShortIndexArray(indices.toShortArray())
        }
    }
}

fun geometry(block: GeometryBuilder.() -> Unit): Geometry = GeometryBuilder().apply(block).build()

class Geometry(val meshes: List<Mesh>) : ByteSized {
    override val byteSize: Int = meshes.sumBy(Mesh::byteSize)

    init {
        requireNotEmpty(meshes, "meshes")
    }

    companion object {
        class GeometryBuilder : Builder<Geometry> {

            private val meshes = mutableListOf<Mesh>()

            fun mesh(
                primitive: Primitive = Triangles,
                block: MeshBuilder.() -> Unit
            ) {
                meshes.add(MeshBuilder(primitive).apply(block).build())
            }

            override fun build(): Geometry {
                return Geometry(meshes)
            }
        }
    }
}

class GeometryNode(val geometry: Geometry, val material: Material) : Node()

class Material(
    val name: String? = null,
    val diffuse: Color = Color(1f, 1f, 1f),
    val specular: Color = Color(0f, 0f, 0f),
    val emission: Color = Color(0f, 0f, 0f),
    val opacity: Color = Color(1f, 1f, 1f),
    val transparency: Color = Color(0f, 0f, 0f),
    val specularPower: Float = 1f
)

class Mesh(
    val primitive: Primitive = Triangles,
    val vertexArrays: Map<Attribute, VertexArray<*>>,
    val indexArrays: List<IndexArray<*>>
) : ByteSized {
    override val byteSize: Int =
        vertexArrays.values.sumBy(VertexArray<*>::byteSize) + indexArrays.sumBy(IndexArray<*>::byteSize)

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

        enum class Attribute {
            Position,
            Normal,
            TexCoord
        }

        class MeshBuilder(val primitive: Primitive) : Builder<Mesh> {

            private val vertexArrays = mutableMapOf<Attribute, VertexArray<*>>()
            private val indexArrays = mutableListOf<IndexArray<*>>()

            fun vertexArray3f(
                attribute: Attribute,
                block: Float3VertexArrayBuilder.() -> Unit
            ) {
                vertexArrays[attribute] = Float3VertexArrayBuilder().apply(block).build()
            }

            fun indexArray(block: ShortIndexArrayBuilder.() -> Unit) {
                indexArrays.add(ShortIndexArrayBuilder().apply(block).build())
            }

            override fun build(): Mesh {
                return Mesh(primitive, vertexArrays, indexArrays)
            }
        }
    }
}

open class Node {}

fun scene(block: SceneBuilder.() -> Unit): Scene = SceneBuilder().apply(block).build()

class Scene(val nodes: List<Node>) {

    companion object {

        class SceneBuilder : Builder<Scene> {

            private val nodes = mutableListOf<Node>()

            fun node(node: Node) {
                nodes.add(node)
            }

            fun node(geometry: Geometry, material: Material) {
                nodes.add(GeometryNode(geometry, material))
            }

            override fun build(): Scene = Scene(nodes)
        }
    }
}

interface VertexArray<T> : ByteSized {
    val count: Int

    val max: T
    val min: T

    val byteStride: Int

    fun copyToByteBuffer(byteBuffer: ByteBuffer)
}

class Float3VertexArray(private val vertices: Array<Vector3fc>) : VertexArray<Vector3fc> {

    override val byteSize: Int = 3 * 4 * vertices.size

    override val count: Int = vertices.size

    override val max: Vector3fc by lazy { maxVector(vertices) }

    override val min: Vector3fc by lazy { minVector(vertices) }

    override val byteStride: Int = 12

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) = with(byteBuffer) {
        for (vertex in vertices) {
            putFloat(vertex.x())
            putFloat(vertex.y())
            putFloat(vertex.z())
        }
    }

    companion object {

        class Float3VertexArrayBuilder : Builder<Float3VertexArray> {
            private val vertices = mutableListOf<Vector3fc>()

            fun add(x: Float, y: Float, z: Float) {
                vertices.add(Vector3f(x, y, z))
            }

            override fun build(): Float3VertexArray = Float3VertexArray(vertices.toTypedArray())
        }

        private fun maxVector(array: Array<Vector3fc>): Vector3fc {
            val result = Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
            for (elem in array) {
                if (elem.x() > result.x) {
                    result.x = elem.x()
                }
                if (elem.y() > result.y) {
                    result.y = elem.y()
                }
                if (elem.z() > result.z) {
                    result.z = elem.z()
                }
            }
            return result
        }

        private fun minVector(array: Array<Vector3fc>): Vector3fc {
            val result = Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            for (elem in array) {
                if (elem.x() < result.x) {
                    result.x = elem.x()
                }
                if (elem.y() < result.y) {
                    result.y = elem.y()
                }
                if (elem.z() < result.z) {
                    result.z = elem.z()
                }
            }
            return result
        }
    }
}

interface ByteSized {
    val byteSize: Int
}

val ByteSized.alignedByteSize: Int
    get() = align(byteSize, 4)

fun align(n: Int, b: Int): Int = if (n % b == 0) n else n + b - n % b

val Iterable<out ByteSized>.byteSize: Int
    get() = this.sumBy(ByteSized::byteSize)

val Iterable<out ByteSized>.alignedByteSize: Int
    get() = this.sumBy(ByteSized::alignedByteSize)

interface Builder<out T> {
    fun build(): T
}

fun main() {
    val g = geometry {
        mesh(primitive = Triangles) {
            vertexArray3f(attribute = Position) {
                add(-0.5f, -0.5f, -0.5f)
                add(-0.5f, 0.5f, -0.5f)
                add(0.5f, 0.5f, -0.5f)
                add(0.5f, -0.5f, -0.5f)
                add(-0.5f, -0.5f, 0.5f)
                add(0.5f, -0.5f, 0.5f)
                add(0.5f, 0.5f, 0.5f)
                add(-0.5f, 0.5f, 0.5f)
                add(-0.5f, -0.5f, -0.5f)
                add(0.5f, -0.5f, -0.5f)
                add(0.5f, -0.5f, 0.5f)
                add(-0.5f, -0.5f, 0.5f)
                add(-0.5f, -0.5f, -0.5f)
                add(0.5f, 0.5f, -0.5f)
                add(0.5f, 0.5f, 0.5f)
                add(0.5f, -0.5f, 0.5f)
                add(0.5f, 0.5f, -0.5f)
                add(-0.5f, 0.5f, -0.5f)
                add(-0.5f, 0.5f, 0.5f)
                add(0.5f, 0.5f, 0.5f)
                add(-0.5f, 0.5f, -0.5f)
                add(-0.5f, -0.5f, -0.5f)
                add(-0.5f, -0.5f, 0.5f)
                add(-0.5f, 0.5f, 0.5f)
            }
            vertexArray3f(attribute = Normal) {
                add(0.0f, 0.0f, -1.0f)
                add(0.0f, 0.0f, -1.0f)
                add(0.0f, 0.0f, -1.0f)
                add(0.0f, 0.0f, -1.0f)
                add(0.0f, 0.0f, 1.0f)
                add(0.0f, 0.0f, 1.0f)
                add(0.0f, 0.0f, 1.0f)
                add(0.0f, 0.0f, 1.0f)
                add(0.0f, -1.0f, 0.0f)
                add(0.0f, -1.0f, 0.0f)
                add(0.0f, -1.0f, 0.0f)
                add(0.0f, -1.0f, 0.0f)
                add(1.0f, 0.0f, 0.0f)
                add(1.0f, 0.0f, 0.0f)
                add(1.0f, 0.0f, 0.0f)
                add(1.0f, 0.0f, 0.0f)
                add(0.0f, 1.0f, 0.0f)
                add(0.0f, 1.0f, 0.0f)
                add(0.0f, 1.0f, 0.0f)
                add(0.0f, 1.0f, 0.0f)
                add(-1.0f, 0.0f, 0.0f)
                add(-1.0f, 0.0f, 0.0f)
                add(-1.0f, 0.0f, 0.0f)
                add(-1.0f, 0.0f, 0.0f)
            }
            indexArray {
                add(0, 1, 2)
                add(2, 3, 0)
                add(4, 5, 6)
                add(6, 7, 4)
                add(8, 9, 10)
                add(10, 11, 8)
                add(12, 13, 14)
                add(14, 15, 12)
                add(16, 17, 18)
                add(18, 19, 16)
                add(20, 21, 22)
                add(22, 23, 20)
            }
        }
    }
    val m = Material(
        name = "Red",
        diffuse = Color(0.8f, 0f, 0f)
    )
    scene {
        node(g, m)
    }

    println(g.byteSize)
}
