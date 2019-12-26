package pcg.util

import org.joml.Vector3f
import org.joml.Vector3fc

typealias Point3fc = Vector3fc
typealias Point3f = Vector3f

fun normalToTriangle(p0: Point3fc, p1: Point3fc, p2: Point3fc): Vector3fc {
    val a = p1.sub(p0, Vector3f())
    val b = p2.sub(p0, Vector3f())
    return a.cross(b).normalize()
}

operator fun Vector3fc.plus(other: Vector3fc): Vector3fc = add(other, Vector3f())

operator fun Vector3fc.unaryMinus(): Vector3fc = negate(Vector3f())

operator fun Vector3f.plusAssign(other: Vector3fc) {
    add(other)
}

object Vector {

    fun add(vararg vectors: Vector3fc): Vector3fc = Vector3f().apply {
        vectors.forEach { v ->
            this += v
        }
    }
}
