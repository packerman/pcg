package pcg.compile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.BufferView.Companion.Target
import pcg.gltf.Primitive.Companion.Attribute
import pcg.scene.Material
import pcg.scene.Mesh.Companion.Attribute.Position
import pcg.scene.geometry
import pcg.scene.scene
import java.io.File

internal class CompilerKtTest {

    @Test
    fun shouldCompileScene() {
        val g = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                }
            }
        }
        val m = Material()
        val s = scene {
            node(geometry = g, material = m)
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scene = 0,
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 0
                            )
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteLength = 36,
                    target = Target.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 36,
                    uri = "data:application/octet-stream;base64,AAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAA"
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        File("TriangleWithoutIndices.gltf").writeText(compiledGltf.toJson(true))
    }

    companion object {
        private const val writeToFiles = true

        @SuppressWarnings("ConstantConditions")
        fun writeToFile(fileName: String, gltf: Gltf) {
            if (writeToFiles) {
                File(fileName).writeText(gltf.toJson(true))
            }
        }
    }
}
