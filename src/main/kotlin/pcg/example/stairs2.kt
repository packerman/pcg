package pcg.example

import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Attribute.Normal
import pcg.scene.Attribute.Position
import pcg.scene.Geometry
import pcg.scene.oneMeshGeometry
import pcg.scene.scene
import pcg.util.normalToTriangle

fun stairs2(
    height: Float, length: Float, width: Float,
    thickness: Float,
    steps: Int
): Geometry {
    require(steps >= 1)
    val x0 = 0f
    val y0 = 0f
    val z0 = 0f
    val dy = height / steps
    val dz = -length / steps
    return oneMeshGeometry {
        var leftSideIndex: Int? = null
        var rightSideIndex: Int? = null
        var bottomSideIndex: Int? = null
        val vertices = vertexArray3f(attribute = Position) {
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

            leftSideIndex = currentCount
            vertex(x0, y0, z0 - thickness)
            vertex(x0, y0 + height - thickness, z0 - length)

            vertex(x0, y0, z0)
            for (s in 0 until steps) {
                vertex(x0, y0 + dy * (s + 1), z0 + dz * s)
                vertex(x0, y0 + dy * (s + 1), z0 + dz * (s + 1))
            }

            rightSideIndex = currentCount
            vertex(x0 + width, y0, z0 - thickness)
            vertex(x0 + width, y0 + height - thickness, z0 - length)
            vertex(x0 + width, y0, z0)
            for (s in 0 until steps) {
                vertex(x0 + width, y0 + dy * (s + 1), z0 + dz * s)
                vertex(x0 + width, y0 + dy * (s + 1), z0 + dz * (s + 1))
            }

            bottomSideIndex = currentCount
            vertex(x0, y0, z0 - thickness)
            vertex(x0, y0 + height - thickness, z0 - length)
            vertex(x0 + width, y0 + height - thickness, z0 - length)
            vertex(x0 + width, y0, z0 - thickness)
        }
        vertexArray3f(attribute = Normal) {
            for (s in 0 until steps) {
                repeat(4) { vertex(0f, 0f, 1f) }
                repeat(4) { vertex(0f, 1f, 0f) }
            }
            vertex(-1f, 0f, 0f)
            vertex(-1f, 0f, 0f)

            vertex(-1f, 0f, 0f)
            for (s in 0 until steps) {
                vertex(-1f, 0f, 0f)
                vertex(-1f, 0f, 0f)
            }

            vertex(1f, 0f, 0f)
            vertex(1f, 0f, 0f)

            vertex(1f, 0f, 0f)
            for (s in 0 until steps) {
                vertex(1f, 0f, 0f)
                vertex(1f, 0f, 0f)
            }

            bottomSideIndex?.let { index ->
                val n = normalToTriangle(vertices[index], vertices[index + 1], vertices[index + 3])
                repeat(4) { vertex(n) }
            }
        }
        indexArray {
            for (s in 0 until steps) {
                triangle(8 * s, 8 * s + 1, 8 * s + 3)
                triangle(8 * s + 1, 8 * s + 2, 8 * s + 3)

                triangle(8 * s + 4, 8 * s + 5, 8 * s + 7)
                triangle(8 * s + 5, 8 * s + 6, 8 * s + 7)
            }
            leftSideIndex?.let { index ->
                triangle(index, index + 2, index + 1)
                triangle(index + 1, index + 2, index + 2 * steps + 2)
                for (s in 0 until steps) {
                    triangle(index + 2 * s + 2, index + 2 * s + 3, index + 2 * s + 4)
                }
            }
            rightSideIndex?.let { index ->
                triangle(index, index + 1, index + 2)
                triangle(index + 1, index + 2 * steps + 2, index + 2)
                for (s in 0 until steps) {
                    triangle(index + 2 * s + 2, index + 2 * s + 4, index + 2 * s + 3)
                }
            }
            bottomSideIndex?.let { index ->
                triangle(index, index + 1, index + 2)
                triangle(index + 2, index + 3, index)
            }
        }
    }
}

fun main() {
    val s = scene {
        node(
            stairs2(
                height = 10f,
                length = 20f,
                width = 10f,
                thickness = 2f,
                steps = 8
            )
        ) {
            material(twoSided = false)
        }
        node(
            simplePlane(75f, 75f)
        ) {
            material(
                twoSided = true
            )
            translate(0f, 0f, -10f)
        }

    }

    writeToFile("TestStairs2.gltf", s.compile(CompileOptions(interleaved = true)))
}
