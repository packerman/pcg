package pcg.gltf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pcg.common.BufferTarget
import pcg.common.ComponentType
import pcg.common.Type

internal class GltfTest {

    @Test
    internal fun byteViewWithTwoAccessorsShouldShaveByteStride() {
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            Gltf(
                accessors = listOf(
                    Accessor(
                        bufferView = 0,
                        componentType = ComponentType.FLOAT,
                        count = 100,
                        type = Type.VEC3
                    ),
                    Accessor(
                        bufferView = 0,
                        componentType = ComponentType.FLOAT,
                        count = 100,
                        type = Type.VEC3
                    )
                ),
                bufferViews = listOf(
                    BufferView(
                        buffer = 0,
                        byteLength = 100,
                        target = BufferTarget.ARRAY_BUFFER
                    )
                ),
                buffers = listOf(
                    Buffer(
                        byteLength = 200
                    )
                )
            )
        }
        assertEquals("When two or more accessors use the same bufferView, byteStride must be defined.", thrown.message)
    }
}
