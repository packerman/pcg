package pcg.example

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.scene.Attribute
import pcg.scene.Geometry
import pcg.scene.oneMeshGeometry
import pcg.util.Point3f

fun plane(
    origin: Vector3fc, axis1: Vector3fc, axis2: Vector3fc,
    m: Int = 1, n: Int = 1
): Geometry {
    val steps1: List<Vector3fc> = (0 until m).map { i -> origin.lerp(axis1, i.toFloat() / m, Vector3f()) }
    val steps2: List<Vector3fc> = (0 until n).map { j -> origin.lerp(axis2, j.toFloat() / n, Vector3f()) }
    return oneMeshGeometry {
        vertexArray3f(attribute = Attribute.Position) {
            for (i in 0 until m) {
                for (j in 0 until n) {
                    add(Point3f(origin).add(steps1[i]).add(steps2[i]))
                }
            }
        }
        val normal: Vector3fc = axis1.cross(axis2, Vector3f()).normalize()
        vertexArray3f(attribute = Attribute.Normal) {
            repeat(m * n) { add(normal) }
        }
    }
}
