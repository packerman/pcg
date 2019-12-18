package pcg.util

import org.joml.Vector3f
import org.joml.Vector3fc

fun normalToTriangle(p0: Vector3fc, p1: Vector3fc, p2: Vector3fc): Vector3fc {
    val a = p1.sub(p0, Vector3f())
    val b = p2.sub(p0, Vector3f())
    return a.cross(b).normalize()
}
