package pcg.scene

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.common.AccessorData
import pcg.common.ComponentType
import pcg.common.Type
import pcg.common.WithAccessorData
import pcg.util.intIterator
import java.nio.ByteBuffer

enum class Attribute {
    Position,
    Normal,
    TexCoord
}

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

            fun triangle(i: Short, j: Short, k: Short) {
                indices.add(i)
                indices.add(j)
                indices.add(k)
            }

            fun triangle(i: Int, j: Int, k: Int) {
                indices.add(i.toShort())
                indices.add(j.toShort())
                indices.add(k.toShort())
            }

            fun square(i: Int, j: Int, k: Int, l: Int) {
                triangle(i, j, l)
                triangle(j, k, l)
            }

            override fun build(): ShortIndexArray = ShortIndexArray(material, indices.toShortArray())
        }
    }
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

            fun vertex(x: Float, y: Float, z: Float) {
                vertices.add(Vector3f(x, y, z))
            }

            fun vertex(v: Vector3fc) {
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

            fun vertex(x: Float, y: Float) {
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
