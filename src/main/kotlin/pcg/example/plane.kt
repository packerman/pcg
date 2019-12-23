package pcg.example

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Attribute.Normal
import pcg.scene.Attribute.Position
import pcg.scene.Geometry
import pcg.scene.oneMeshGeometry
import pcg.scene.scene
import pcg.util.Point3f

fun plane(
    origin: Vector3fc, axis1: Vector3fc, axis2: Vector3fc,
    m: Int = 1, n: Int = 1
): Geometry {
    val steps1: List<Vector3fc> = (0..m).map { i -> origin.lerp(axis1, i.toFloat() / m, Vector3f()) }
    val steps2: List<Vector3fc> = (0..n).map { j -> origin.lerp(axis2, j.toFloat() / n, Vector3f()) }
    return oneMeshGeometry {
        vertexArray3f(attribute = Position) {
            for (i in 0..m) {
                for (j in 0..n) {
                    vertex(Point3f(origin).add(steps1[i]).add(steps2[j]))
                }
            }
        }
        val normal: Vector3fc = axis1.cross(axis2, Vector3f()).normalize()
        vertexArray3f(attribute = Normal) {
            repeat((m + 1) * (n + 1)) { vertex(normal) }
        }
        indexArray {
            for (i in 0 until m) {
                for (j in 0 until n) {
                    square(
                        j + i * (n + 1), j + (i + 1) * (n + 1),
                        j + (i + 1) * (n + 1) + 1, j + i * (n + 1) + 1
                    )
                }
            }
        }
    }
}

fun main() {
    val s = scene {
        node(
            plane(
                origin = Vector3f(0f, 0f, 0f),
                axis1 = Vector3f(10f, 0f, 0f),
                axis2 = Vector3f(0f, 10f, 0f),
                m = 4, n = 4
            )
        ) {
            material()
        }
    }
    writeToFile("TestPlane.gltf", compile(s, CompileOptions(interleaved = true)))
}
