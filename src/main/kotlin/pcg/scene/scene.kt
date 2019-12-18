package pcg.scene

import org.joml.*
import pcg.common.AccessorData
import pcg.common.ComponentType
import pcg.common.Type
import pcg.common.WithAccessorData
import pcg.scene.Float2VertexArray.Companion.Float2VertexArrayBuilder
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

interface IndexArray<T> : ByteSized, Iterable<Int>, WithAccessorData {
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

    override val accessorData: AccessorData
        get() = AccessorData(
            componentType = ComponentType.UNSIGNED_SHORT,
            type = Type.SCALAR,
            max = listOf(max),
            min = listOf(min)
        )

    companion object {
        class ShortIndexArrayBuilder(private val material: Int = 0) : Builder<ShortIndexArray> {
            private val indices = mutableListOf<Short>()

            fun add(i: Short, j: Short, k: Short) {
                indices.add(i)
                indices.add(j)
                indices.add(k)
            }

            fun add(i: Int, j: Int, k: Int) {
                indices.add(i.toShort())
                indices.add(j.toShort())
                indices.add(k.toShort())
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

            override fun build(): Node = GeometryNode(geometry, materials, transforms)
        }
    }
}

data class Material(
    val name: String? = null,
    val twoSided: Boolean = false,
    val diffuse: Color = Color(1f, 1f, 1f),
    val specular: Color = Color(0f, 0f, 0f),
    val emission: Color = Color(0f, 0f, 0f),
    val opacity: Color = Color(1f, 1f, 1f),
    val transparency: Color = Color(0f, 0f, 0f),
    val specularPower: Float = 1f,
    val diffuseTexture: Texture? = null,
    val specularTexture: Texture? = null,
    val specularPowerTexture: Texture? = null,
    val emissionTexture: Texture? = null,
    val opacityTexture: Texture? = null,
    val transparencyTexture: Texture? = null,
    val normalTexture: Texture? = null
) {
    companion object {
        val default = Material()
    }
}

enum class Attribute {
    Position,
    Normal,
    TexCoord
}

class Mesh(
    val primitive: Primitive = Triangles,
    val vertexArrays: List<VertexArray<*>>,
    val indexArrays: List<IndexArray<*>>
) : ByteSized {
    override val byteSize: Int =
        vertexArrays.sumBy(VertexArray<*>::byteSize) + indexArrays.sumBy(IndexArray<*>::byteSize)

    init {
        requireNotEmpty(vertexArrays, "vertexArrays")
        require(allTheSame(vertexArrays.map(VertexArray<*>::count))) { "All Vertex Arrays need to have the same count" }
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

        class MeshBuilder(private val primitive: Primitive) : Builder<Mesh> {

            private val vertexArrays = mutableListOf<VertexArray<*>>()
            private val indexArrays = mutableListOf<IndexArray<*>>()

            fun vertexArray3f(
                attribute: Attribute,
                block: Float3VertexArrayBuilder.() -> Unit
            ): Float3VertexArray {
                val array = Float3VertexArrayBuilder(attribute).apply(block).build()
                vertexArrays.add(array)
                return array
            }

            fun vertexArray2f(
                attribute: Attribute,
                block: Float2VertexArrayBuilder.() -> Unit
            ) {
                vertexArrays.add(Float2VertexArrayBuilder(attribute).apply(block).build())
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

    val geometries: Set<Geometry>
        get() = nodes.mapNotNull { n -> n as? GeometryNode }
            .map { it.geometry }
            .toSet()

    val materials: Set<Material>
        get() = nodes.mapNotNull { it as? GeometryNode }
            .flatMap { it.materials.values }
            .toSet()

    companion object {

        class SceneBuilder : Builder<Scene> {

            private val nodes = mutableListOf<Node>()

            fun node(geometry: Geometry, block: GeometryNodeBuilder.() -> Unit = {}) {
                nodes.add(GeometryNodeBuilder(geometry).apply(block).build())
            }

            override fun build(): Scene = Scene(nodes)
        }
    }
}

class Texture(val fileName: String)

interface Transform {

    fun getMatrix(m: Matrix4f): Matrix4f
}

class Translation(val dx: Float, val dy: Float, val dz: Float) : Transform {

    override fun getMatrix(m: Matrix4f): Matrix4f = m.apply { setTranslation(dx, dy, dz) }
}


interface VertexArray<T> : ByteSized, WithAccessorData {

    val attribute: Attribute

    val count: Int

    val max: T

    val min: T

    val byteStride: Int

    operator fun get(index: Int): T

    fun copyToByteBuffer(byteBuffer: ByteBuffer)

    fun copyToByteBuffer(byteBuffer: ByteBuffer, index: Int)
}

interface VertexArrayBuilder<V> : Builder<V> {
    val currentCount: Int
}

class Float3VertexArray(override val attribute: Attribute, private val vertices: Array<Vector3fc>) :
    VertexArray<Vector3fc> {

    override val byteSize: Int = 3 * 4 * vertices.size

    override val count: Int = vertices.size

    override val max: Vector3fc by lazy { maxVector(vertices) }

    override val min: Vector3fc by lazy { minVector(vertices) }

    override val byteStride: Int = 12

    override fun get(index: Int): Vector3fc = vertices[index]

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) = with(byteBuffer) {
        for (vertex in vertices) {
            putFloat(vertex.x())
            putFloat(vertex.y())
            putFloat(vertex.z())
        }
    }

    override fun copyToByteBuffer(byteBuffer: ByteBuffer, index: Int): Unit = with(byteBuffer) {
        putFloat(vertices[index].x())
        putFloat(vertices[index].y())
        putFloat(vertices[index].z())
    }

    override val accessorData: AccessorData
        get() = AccessorData(
            componentType = ComponentType.FLOAT,
            type = Type.VEC3,
            max = listOf(max.x(), max.y(), max.z()),
            min = listOf(min.x(), min.y(), min.z())
        )

    companion object {

        class Float3VertexArrayBuilder(private val attribute: Attribute) : VertexArrayBuilder<Float3VertexArray> {
            private val vertices = mutableListOf<Vector3fc>()

            fun add(x: Float, y: Float, z: Float) {
                vertices.add(Vector3f(x, y, z))
            }

            fun add(v: Vector3fc) {
                vertices.add(v)
            }

            override val currentCount: Int
                get() = vertices.size

            override fun build(): Float3VertexArray = Float3VertexArray(attribute, vertices.toTypedArray())
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

class Float2VertexArray(override val attribute: Attribute, private val vertices: Array<Vector2fc>) :
    VertexArray<Vector2fc> {

    override val byteSize: Int = 2 * 4 * vertices.size

    override val count: Int = vertices.size

    override val max: Vector2fc by lazy { maxVector(vertices) }

    override val min: Vector2fc by lazy { minVector(vertices) }

    override val byteStride: Int = 8

    override fun get(index: Int): Vector2fc = vertices[index]

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) = with(byteBuffer) {
        for (vertex in vertices) {
            putFloat(vertex.x())
            putFloat(vertex.y())
        }
    }

    override fun copyToByteBuffer(byteBuffer: ByteBuffer, index: Int): Unit = with(byteBuffer) {
        putFloat(vertices[index].x())
        putFloat(vertices[index].y())
    }

    override val accessorData: AccessorData
        get() = AccessorData(
            componentType = ComponentType.FLOAT,
            type = Type.VEC2,
            max = listOf(max.x(), max.y()),
            min = listOf(min.x(), min.y())
        )

    companion object {

        class Float2VertexArrayBuilder(private val attribute: Attribute) : VertexArrayBuilder<Float2VertexArray> {
            private val vertices = mutableListOf<Vector2fc>()

            fun add(x: Float, y: Float) {
                vertices.add(Vector2f(x, y))
            }

            override val currentCount: Int
                get() = vertices.size

            override fun build(): Float2VertexArray = Float2VertexArray(attribute, vertices.toTypedArray())
        }

        private fun maxVector(array: Array<Vector2fc>): Vector2fc {
            val result = Vector2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
            for (elem in array) {
                if (elem.x() > result.x) {
                    result.x = elem.x()
                }
                if (elem.y() > result.y) {
                    result.y = elem.y()
                }
            }
            return result
        }

        private fun minVector(array: Array<Vector2fc>): Vector2fc {
            val result = Vector2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            for (elem in array) {
                if (elem.x() < result.x) {
                    result.x = elem.x()
                }
                if (elem.y() < result.y) {
                    result.y = elem.y()
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
