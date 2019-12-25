package pcg.compile

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pcg.common.Attribute.POSITION
import pcg.common.BufferTarget.ARRAY_BUFFER
import pcg.common.ComponentType.FLOAT
import pcg.common.Type
import pcg.gltf.*
import pcg.scene.Attribute.Position
import pcg.scene.oneMeshGeometry
import pcg.scene.scene

internal class SceneGraphTest {

    @Test
    internal fun shouldCompileGraphScene() {
        val g = oneMeshGeometry {
            vertexArray3f(attribute = Position) {
                vertex(0f, 0f, 0f)
                vertex(0.5f, 0f, 0f)
                vertex(0f, 0.5f, 0f)
            }
        }

        val s = scene {
            node(g) {
                node {
                    node(g)
                }
            }
            node {
                node(g)
            }
        }

        val gltf = s.compile()

        val expected = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(0)
                )
            ),
            nodes = listOf(
                Node(mesh = 0)
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(POSITION to 0)
                        )
                    )
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    byteOffset = 0,
                    componentType = FLOAT,
                    count = 3,
                    max = listOf(0.5f, 0.5f, 0f),
                    min = listOf(0f, 0f, 0f),
                    type = Type.VEC3
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 36,
                    target = ARRAY_BUFFER
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 36,
                    uri = "data:application/octet-stream;base64,AAAAAAAAAAAAAAAAAAAAPwAAAAAAAAAAAAAAAAAAAD8AAAAA"
                )
            )
        )

        Assertions.assertEquals(expected, gltf)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }
}
