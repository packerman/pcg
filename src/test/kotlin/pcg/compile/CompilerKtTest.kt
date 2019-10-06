package pcg.compile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.Primitive.Companion.Attribute
import pcg.scene.Material
import pcg.scene.Mesh.Companion.Attribute.Position
import pcg.scene.geometry
import pcg.scene.scene

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
            accessors = listOf(
                Accessor(
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
    }
}
