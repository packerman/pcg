package pcg.scene

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.PI

object Axis {
    val X: Vector3fc = Vector3f(1f, 0f, 0f)
    val Y: Vector3fc = Vector3f(0f, 1f, 0f)
    val Z: Vector3fc = Vector3f(0f, 0f, 1f)
}

interface Transform {

    fun applyInPlace(matrix: Matrix4f): Matrix4f
}

fun List<Transform>.isTRSTransform(): Boolean {
    if (!all { it is Translation || it is Rotation || it is Scale }) {
        return false
    }
    val translationEndIndex = indexOfLast { it is Translation }
    val rotationStartIndex = indexOfFirst { it is Rotation }
    val rotationEndIndex = indexOfLast { it is Rotation }
    val scaleStartIndex = indexOfFirst { it is Scale }

    fun lessThanIfBothPositive(index1: Int, index2: Int) =
        index1 == -1 || index2 == -1 || index1 < index2

    return lessThanIfBothPositive(translationEndIndex, rotationStartIndex) &&
            lessThanIfBothPositive(rotationEndIndex, scaleStartIndex) &&
            lessThanIfBothPositive(translationEndIndex, scaleStartIndex)
}


class Rotation(private val angleInDegrees: Float, private val axis: Vector3fc) : Transform {

    override fun applyInPlace(matrix: Matrix4f): Matrix4f {
        return matrix.rotate(toRadians(angleInDegrees), axis)
    }

    companion object {

        private fun toRadians(x: Float): Float = x * PI.toFloat() / 180f
    }
}

class Scale(val sx: Float, val sy: Float, val sz: Float) : Transform {

    override fun applyInPlace(matrix: Matrix4f): Matrix4f {
        return matrix.scale(sx, sy, sz)
    }
}

class Translation(private val dx: Float, private val dy: Float, private val dz: Float) : Transform {

    override fun applyInPlace(matrix: Matrix4f): Matrix4f {
        return matrix.translate(dx, dy, dz)
    }
}
