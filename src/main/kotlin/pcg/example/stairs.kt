package pcg.example

import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.*

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
        var leftWallIndex = -1
        var rightWallIndex = -1
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
            if (sideWalls) {
                leftWallIndex = currentCount
                add(x0, y0, z0 - length)
                add(x0, y0, z0)
                for (k in 0 until steps) {
                    add(x0, y0 + dy * (k + 1), z0 + dz * k)
                    add(x0, y0 + dy * (k + 1), z0 + dz * (k + 1))
                }
                add(x0, y0 + height, z0)

                rightWallIndex = currentCount
                add(x0 + width, y0, z0 - length)
                add(x0 + width, y0, z0)
                for (k in 0 until steps) {
                    add(x0 + width, y0 + dy * (k + 1), z0 + dz * k)
                    add(x0 + width, y0 + dy * (k + 1), z0 + dz * (k + 1))
                }
                add(x0 + width, y0 + height, z0)
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
            if (sideWalls) {
                add(-1f, 0f, 0f)
                add(-1f, 0f, 0f)
                for (k in 1..steps) {
                    add(-1f, 0f, 0f)
                    add(-1f, 0f, 0f)
                }
                add(-1f, 0f, 0f)

                add(1f, 0f, 0f)
                add(1f, 0f, 0f)
                for (k in 1..steps) {
                    add(1f, 0f, 0f)
                    add(1f, 0f, 0f)
                }
                add(1f, 0f, 0f)
            }
        }
        indexArray {
            for (s in 0 until steps) {
                add(8 * s, 8 * s + 1, 8 * s + 3)
                add(8 * s + 1, 8 * s + 2, 8 * s + 3)

                add(8 * s + 4, 8 * s + 5, 8 * s + 7)
                add(8 * s + 5, 8 * s + 6, 8 * s + 7)
            }
            if (leftWallIndex >= 0) {
                for (k in 1..2 * steps) {
                    add(leftWallIndex, leftWallIndex + k, leftWallIndex + k + 1)
                }
            }
            if (rightWallIndex >= 0) {
                for (k in 1..2 * steps) {
                    add(rightWallIndex, rightWallIndex + k + 1, rightWallIndex + k)
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
            material(Material(twoSided = false))
        }
        node(
            planeGeometry(75f, 75f)
        ) {
            material(
                Material(
                    diffuse = Color(0.5f, 0.5f, 0.5f),
                    twoSided = true
                )
            )
            translate(0f, 0f, -10f)
        }

    }

    writeToFile("TestStairs.gltf", compile(s, CompileOptions(interleaved = true)))
}
