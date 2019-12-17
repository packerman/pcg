package pcg.example

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.*

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
        val vertices = vertexArray3f(attribute = Attribute.Position) {
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

            leftSideIndex = currentCount
            add(x0, y0, z0 - thickness)
            add(x0, y0 + height - thickness, z0 - length)

            add(x0, y0, z0)
            for (s in 0 until steps) {
                add(x0, y0 + dy * (s + 1), z0 + dz * s)
                add(x0, y0 + dy * (s + 1), z0 + dz * (s + 1))
            }

            rightSideIndex = currentCount
            add(x0 + width, y0, z0 - thickness)
            add(x0 + width, y0 + height - thickness, z0 - length)
            add(x0 + width, y0, z0)
            for (s in 0 until steps) {
                add(x0 + width, y0 + dy * (s + 1), z0 + dz * s)
                add(x0 + width, y0 + dy * (s + 1), z0 + dz * (s + 1))
            }

            bottomSideIndex = currentCount
            add(x0, y0, z0 - thickness)
            add(x0, y0 + height - thickness, z0 - length)
            add(x0 + width, y0 + height - thickness, z0 - length)
            add(x0 + width, y0, z0 - thickness)
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
            add(-1f, 0f, 0f)
            add(-1f, 0f, 0f)

            add(-1f, 0f, 0f)
            for (s in 0 until steps) {
                add(-1f, 0f, 0f)
                add(-1f, 0f, 0f)
            }

            add(1f, 0f, 0f)
            add(1f, 0f, 0f)

            add(1f, 0f, 0f)
            for (s in 0 until steps) {
                add(1f, 0f, 0f)
                add(1f, 0f, 0f)
            }

            bottomSideIndex?.let { index ->
                val n = normalToTriangle(vertices[index], vertices[index + 1], vertices[index + 3])
                add(n)
                add(n)
                add(n)
                add(n)
            }
        }
        indexArray {
            for (s in 0 until steps) {
                add(8 * s, 8 * s + 1, 8 * s + 3)
                add(8 * s + 1, 8 * s + 2, 8 * s + 3)

                add(8 * s + 4, 8 * s + 5, 8 * s + 7)
                add(8 * s + 5, 8 * s + 6, 8 * s + 7)
            }
            leftSideIndex?.let { index ->
                add(index, index + 2, index + 1)
                add(index + 1, index + 2, index + 2 * steps + 2)
                for (s in 0 until steps) {
                    add(index + 2 * s + 2, index + 2 * s + 4, index + 2 * s + 3)
                }
            }
            rightSideIndex?.let { index ->
                add(index, index + 1, index + 2)
                add(index + 1, index + 2 * steps + 2, index + 2)
                for (s in 0 until steps) {
                    add(index + 2 * s + 2, index + 2 * s + 3, index + 2 * s + 4)
                }
            }
            bottomSideIndex?.let { index ->
                add(index, index + 1, index + 2)
                add(index + 2, index + 3, index)
            }
        }
    }
}

private fun normalToTriangle(p0: Vector3fc, p1: Vector3fc, p2: Vector3fc): Vector3fc {
    val a = p1.sub(p0, Vector3f())
    val b = p2.sub(p0, Vector3f())
    return a.cross(b).normalize()
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
            material(Material(twoSided = true))
        }
        node(
            planeGeometry(75f, 75f)
        ) {
            material(
                Material(
                    diffuse = Color(0.5f, 0.5f, 0.5f),
                    twoSided = false
                )
            )
            translate(0f, 0f, -10f)
        }

    }

    writeToFile("TestStairs2.gltf", compile(s, CompileOptions(interleaved = true)))
}
