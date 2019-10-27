package pcg.scene

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.scene.Float3VertexArray.Companion.Float3VertexArrayBuilder
import pcg.scene.Geometry.Companion.GeometryBuilder
import pcg.scene.GeometryNode.Companion.GeometryNodeBuilder
import pcg.scene.Mesh.Companion.MeshBuilder
import pcg.scene.Mesh.Companion.Primitive
import pcg.scene.Mesh.Companion.Primitive.Triangles
import pcg.scene.Node.Companion.NodeBuilder
import pcg.scene.Scene.Companion.SceneBuilder
import pcg.scene.ShortIndexArray.Companion.ShortIndexArrayBuilder
import pcg.util.align
import pcg.util.allTheSame
import pcg.util.intIterator
import pcg.validate.requireNotEmpty
import java.nio.ByteBuffer

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f)

interface IndexArray<T> : ByteSized, Iterable<Int> {
    val material: Int
        get() = 0

    val count: Int

    val max: T
    val min: T

    fun copyToByteBuffer(byteBuffer: ByteBuffer)
}

class ShortIndexArray(override val material: Int = 0, private val indices: ShortArray) : IndexArray<Short> {

    override val byteSize: Int = 2 * indices.size

    override val count: Int = indices.size

    override val max: Short = indices.max() ?: Short.MIN_VALUE

    override val min: Short = indices.min() ?: Short.MAX_VALUE

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) = with(byteBuffer) {
        for (index in indices) {
            putShort(index)
        }
    }

    override fun iterator(): IntIterator = indices.intIterator()

    companion object {
        class ShortIndexArrayBuilder(private val material: Int = 0) : Builder<ShortIndexArray> {
            private val indices = mutableListOf<Short>()

            fun add(i: Short, j: Short, k: Short) {
                indices.add(i)
                indices.add(j)
                indices.add(k)
            }

            override fun build(): ShortIndexArray = ShortIndexArray(material, indices.toShortArray())
        }
    }
}

fun geometry(block: GeometryBuilder.() -> Unit): Geometry = GeometryBuilder().apply(block).build()

fun oneMeshGeometry(
    primitive: Primitive = Triangles,
    block: MeshBuilder.() -> Unit
): Geometry {
    return geometry {
        mesh(primitive, block)
    }
}

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

class GeometryNode(
    val geometry: Geometry,
    val materials: Map<Int, Material>,
    transforms: List<Transform>
) : Node(transforms) {

    init {
        for (mesh in geometry.meshes) {
            require((mesh.indexArrays.isEmpty() && materials.size == 1 && 0 in materials) ||
                    (mesh.indexArrays.all { it.material in materials } && materials.keys.all { it in mesh.indexArrays.indices })
            )
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

            override fun build(): Node = GeometryNode(geometry, materials, transforms)
        }
    }
}

data class Material(
    val name: String? = null,
    val diffuse: Color = Color(1f, 1f, 1f),
    val specular: Color = Color(0f, 0f, 0f),
    val emission: Color = Color(0f, 0f, 0f),
    val opacity: Color = Color(1f, 1f, 1f),
    val transparency: Color = Color(0f, 0f, 0f),
    val specularPower: Float = 1f,
    val twoSided: Boolean = false
) {
    companion object {
        val default = Material()
    }
}

class Mesh(
    val primitive: Primitive = Triangles,
    val vertexArrays: Map<Attribute, VertexArray<*>>,
    val indexArrays: List<IndexArray<*>>
) : ByteSized {
    override val byteSize: Int =
        vertexArrays.values.sumBy(VertexArray<*>::byteSize) + indexArrays.sumBy(IndexArray<*>::byteSize)

    init {
        requireNotEmpty(vertexArrays, "vertexArrays")
        require(allTheSame(vertexArrays.values.map(VertexArray<*>::count))) { "All Vertex Arrays need to have the same count" }
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

        class MeshBuilder(private val primitive: Primitive) : Builder<Mesh> {

            private val vertexArrays = mutableMapOf<Attribute, VertexArray<*>>()
            private val indexArrays = mutableListOf<IndexArray<*>>()

            fun vertexArray3f(
                attribute: Attribute,
                block: Float3VertexArrayBuilder.() -> Unit
            ) {
                vertexArrays[attribute] = Float3VertexArrayBuilder().apply(block).build()
            }

            fun indexArray(material: Int = 0, block: ShortIndexArrayBuilder.() -> Unit) {
                indexArrays.add(ShortIndexArrayBuilder(material).apply(block).build())
            }

            override fun build(): Mesh {
                return Mesh(primitive, vertexArrays, indexArrays)
            }
        }
    }
}

open class Node(val transforms: List<Transform>) {

    companion object {

        open class NodeBuilder : Builder<Node> {

            protected val transforms = mutableListOf<Transform>()

            fun translate(dx: Float, dy: Float, dz: Float) {
                transforms.add(Translation(dx, dy, dz))
            }

            override fun build() = Node(transforms)
        }
    }
}

fun scene(block: SceneBuilder.() -> Unit): Scene = SceneBuilder().apply(block).build()

class Scene(val nodes: List<Node>) {

    companion object {

        class SceneBuilder : Builder<Scene> {

            private val nodes = mutableListOf<Node>()

            fun node(node: Node) {
                nodes.add(node)
            }

            fun node(geometry: Geometry, block: GeometryNodeBuilder.() -> Unit = {}) {
                nodes.add(GeometryNodeBuilder(geometry).apply(block).build())
            }

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

val Iterable<ByteSized>.byteSize: Int
    get() = this.sumBy(ByteSized::byteSize)

interface Builder<out T> {
    fun build(): T
}
