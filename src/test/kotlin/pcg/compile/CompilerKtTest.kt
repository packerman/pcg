package pcg.compile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

        val gltf = compile(s)

        assertEquals(0, gltf.scene)
        assertEquals(1, gltf.scenes?.size)

        val scene = gltf.scenes?.get(0)

        assertEquals(1, scene?.nodes?.size)
    }
}
