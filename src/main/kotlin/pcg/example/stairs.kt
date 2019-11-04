package pcg.example

import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.*

fun stairs(height: Float, length: Float, width: Float, steps: Int): Geometry {
    val x0 = 0f
    val y0 = 0f
    val z0 = 0f
    val dy = height / steps
    val dz = -length / steps
    return oneMeshGeometry {
        vertexArray3f(attribute = Attribute.Position) {
            for (s in 0 until steps) {
                add(x0, y0 + dy * s, z0 + dz * s)
                add(x0 + width, y0 + dy * s, z0 + dz * s)
                add(x0 + width, y0 + dy * (s + 1), z0 + dz * s)
                add(x0, y0 + dy * (s + 1), z0 + dz * s)

                add(x0, y0 + dy * (s + 1), z0 + s * dz)
                add(x0 + width, y0 + dy * (s + 1), z0 + s * dz)
                add(x0 + width, y0 + dy * (s + 1), z0 + (s + 1) * dz)
                add(x0, y0 + dy * (s + 1), z0 + (s + 1) * dz)
            }
        }
        vertexArray3f(attribute = Attribute.Normal) {
            for (s in 0 until steps) {
                add(0f, 0f, 1f)
                add(0f, 0f, 1f)
                add(0f, 0f, 1f)
                add(0f, 0f, 1f)

                add(0f, 1f, 0f)
                add(0f, 1f, 0f)
                add(0f, 1f, 0f)
                add(0f, 1f, 0f)
            }
        }
        indexArray {
            for (s in 0 until steps) {
                add((8 * s).toShort(), (8 * s + 1).toShort(), (8 * s + 3).toShort())
                add((8 * s + 1).toShort(), (8 * s + 2).toShort(), (8 * s + 3).toShort())

                add((8 * s + 4).toShort(), (8 * s + 5).toShort(), (8 * s + 7).toShort())
                add((8 * s + 5).toShort(), (8 * s + 6).toShort(), (8 * s + 7).toShort())
            }
        }
    }
}

fun main() {
    val s = scene {
        node(stairs(10f, 10f, 10f, 10)) {
            material(Material())
        }
    }

    writeToFile("TestStairs.gltf", compile(s, CompileOptions(interleaved = true)))
}
