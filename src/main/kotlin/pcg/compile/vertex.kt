package pcg.compile

import pcg.common.BufferTarget
import pcg.gltf.BufferView
import pcg.scene.VertexArray
import pcg.scene.byteSize
import java.nio.ByteBuffer

interface VertexSerializer {

    fun createVertexBufferViews(bufferIndex: Int, byteOffset: Int): List<BufferView>

    fun copyToByteBuffer(byteBuffer: ByteBuffer)
}

class StandardVertexSerializer(private val vertexArrays: List<VertexArray<*>>) : VertexSerializer {

    private val vertexArraysByByteStride = vertexArrays.groupBy(VertexArray<*>::byteStride)

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
