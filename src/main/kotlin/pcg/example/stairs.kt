package pcg.example

import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Attribute.Normal
import pcg.scene.Attribute.Position
import pcg.scene.Color
import pcg.scene.Geometry
import pcg.scene.oneMeshGeometry
import pcg.scene.scene

fun stairs(
    height: Float, length: Float, width: Float, steps: Int,
    sideWalls: Boolean = false
): Geometry {
    require(steps >= 1)
    val x0 = 0f
    val y0 = 0f
    val z0 = 0f
    val dy = height / steps
    val dz = -length / steps
    return oneMeshGeometry {
        var leftWallIndex: Int? = null
        var rightWallIndex: Int? = null
        vertexArray3f(attribute = Position) {
            for (s in 0 until steps) {
                vertex(x0, y0 + dy * s, z0 + dz * s)
                vertex(x0 + width, y0 + dy * s, z0 + dz * s)
                vertex(x0 + width, y0 + dy * (s + 1), z0 + dz * s)
                vertex(x0, y0 + dy * (s + 1), z0 + dz * s)

                vertex(x0, y0 + dy * (s + 1), z0 + s * dz)
                vertex(x0 + width, y0 + dy * (s + 1), z0 + s * dz)
                vertex(x0 + width, y0 + dy * (s + 1), z0 + (s + 1) * dz)
                vertex(x0, y0 + dy * (s + 1), z0 + (s + 1) * dz)
            }
            if (sideWalls) {
                leftWallIndex = currentCount
                vertex(x0, y0, z0 - length)
                vertex(x0, y0, z0)
                for (k in 0 until steps) {
                    vertex(x0, y0 + dy * (k + 1), z0 + dz * k)
                    vertex(x0, y0 + dy * (k + 1), z0 + dz * (k + 1))
                }
                vertex(x0, y0 + height, z0)

                rightWallIndex = currentCount
                vertex(x0 + width, y0, z0 - length)
                vertex(x0 + width, y0, z0)
                for (k in 0 until steps) {
                    vertex(x0 + width, y0 + dy * (k + 1), z0 + dz * k)
                    vertex(x0 + width, y0 + dy * (k + 1), z0 + dz * (k + 1))
                }
                vertex(x0 + width, y0 + height, z0)
            }
        }
        vertexArray3f(attribute = Normal) {
            for (s in 0 until steps) {
                vertex(0f, 0f, 1f)
                vertex(0f, 0f, 1f)
                vertex(0f, 0f, 1f)
                vertex(0f, 0f, 1f)

                vertex(0f, 1f, 0f)
                vertex(0f, 1f, 0f)
                vertex(0f, 1f, 0f)
                vertex(0f, 1f, 0f)
            }
            if (sideWalls) {
                vertex(-1f, 0f, 0f)
                vertex(-1f, 0f, 0f)
                for (k in 1..steps) {
                    vertex(-1f, 0f, 0f)
                    vertex(-1f, 0f, 0f)
                }
                vertex(-1f, 0f, 0f)

                vertex(1f, 0f, 0f)
                vertex(1f, 0f, 0f)
                for (k in 1..steps) {
                    vertex(1f, 0f, 0f)
                    vertex(1f, 0f, 0f)
                }
                vertex(1f, 0f, 0f)
            }
        }
        indexArray {
            for (s in 0 until steps) {
                triangle(8 * s, 8 * s + 1, 8 * s + 3)
                triangle(8 * s + 1, 8 * s + 2, 8 * s + 3)

                triangle(8 * s + 4, 8 * s + 5, 8 * s + 7)
                triangle(8 * s + 5, 8 * s + 6, 8 * s + 7)
            }
            leftWallIndex?.let { index ->
                for (k in 1..2 * steps) {
                    triangle(index, index + k, index + k + 1)
                }
            }
            rightWallIndex?.let { index ->
                for (k in 1..2 * steps) {
                    triangle(index, index + k + 1, index + k)
                }
            }
        }
    }
}

fun main() {
    val s = scene {
        node(
            stairs(
                10f, 20f, 10f, 8,
                sideWalls = true
            )
        ) {
            material(twoSided = false)
        }
        node(
            simplePlane(75f, 75f)
        ) {
            material(
                    diffuse = Color(0.5f, 0.5f, 0.5f),
                    twoSided = true
            )
            translate(0f, 0f, -10f)
        }

    }

    writeToFile("TestStairs.gltf", s.compile(CompileOptions(interleaved = true)))
}
