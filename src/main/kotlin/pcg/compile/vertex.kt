package pcg.compile

import pcg.common.BufferTarget
import pcg.gltf.Accessor
import pcg.gltf.BufferView
import pcg.scene.VertexArray
import pcg.scene.byteSize
import pcg.util.indexElements
import java.nio.ByteBuffer

interface VertexSerializer {

    /**
     * Vertex array in order their attributes appear in accessors
     */
    val attributes: Iterable<VertexArray<*>>

    fun createVertexAccessors(startBufferViewIndex: Int): List<Accessor>

    fun createVertexBufferViews(bufferIndex: Int, byteOffset: Int): List<BufferView>

    fun copyToByteBuffer(byteBuffer: ByteBuffer)
}

class StandardVertexSerializer(private val vertexArrays: List<VertexArray<*>>) : VertexSerializer {

    private val vertexArraysByByteStride = vertexArrays.groupBy(VertexArray<*>::byteStride)

    override val attributes: Iterable<VertexArray<*>> =
        vertexArraysByByteStride.flatMap { it.value }

    override fun createVertexAccessors(startBufferViewIndex: Int): List<Accessor> =
        mutableListOf<Accessor>().apply {
            val strideIndex = indexElements(vertexArraysByByteStride.keys)
            vertexArraysByByteStride.values.forEach { vertexArrays ->
                var byteOffset = 0
                vertexArrays.forEach { vertexArray ->
                    add(
                        vertexArray.createAccessor(
                            strideIndex.getValue(vertexArray.byteStride) + startBufferViewIndex,
                            byteOffset
                        )
                    )
                    byteOffset += vertexArray.byteSize
                }
            }
        }

    override fun createVertexBufferViews(
        bufferIndex: Int,
        byteOffset: Int
    ): List<BufferView> = mutableListOf<BufferView>().apply {
        var offset = byteOffset
        vertexArraysByByteStride.forEach { (byteStride, vertexArrays) ->
            add(
                BufferView(
                    buffer = bufferIndex,
                    byteOffset = offset,
                    byteLength = vertexArrays.byteSize,
                    byteStride = if (vertexArrays.size > 1) byteStride else null,
                    target = BufferTarget.ARRAY_BUFFER
                )
            )
            offset += vertexArrays.byteSize
        }
    }

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) =
        vertexArraysByByteStride.values.forEach { vertexArrays ->
            vertexArrays.forEach { it.copyToByteBuffer(byteBuffer) }
        }
}

class InterleavedVertexSerializer(private val vertexArrays: List<VertexArray<*>>) : VertexSerializer {

    private val count = vertexArrays.first().count

    override val attributes: Iterable<VertexArray<*>> = vertexArrays

    override fun createVertexAccessors(startBufferViewIndex: Int): List<Accessor> =
        mutableListOf<Accessor>().apply {
            var byteOffset = 0
            vertexArrays.forEach { vertexArray ->
                add(
                    vertexArray.createAccessor(
                        startBufferViewIndex,
                        byteOffset
                    )
                )
                byteOffset += vertexArray.byteStride
            }
        }


    override fun createVertexBufferViews(bufferIndex: Int, byteOffset: Int): List<BufferView> =
        listOf(
            BufferView(
                buffer = bufferIndex,
                byteOffset = byteOffset,
                byteLength = vertexArrays.byteSize,
                byteStride = vertexArrays.sumBy { it.byteStride },
                target = BufferTarget.ARRAY_BUFFER
            )
        )

    override fun copyToByteBuffer(byteBuffer: ByteBuffer) =
        repeat(count) { i ->
            vertexArrays.forEach { it.copyToByteBuffer(byteBuffer, i) }
        }
}

private fun VertexArray<*>.createAccessor(bufferViewIndex: Int, byteOffset: Int): Accessor {
    val accessorData = this.accessorData
    return Accessor(
        bufferView = bufferViewIndex,
        byteOffset = byteOffset,
        componentType = accessorData.componentType,
        count = count,
        type = accessorData.type,
        max = accessorData.max,
        min = accessorData.min
    )
}
