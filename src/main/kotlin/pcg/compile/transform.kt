package pcg.compile

import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Vector3f

object FloatArrays {

    fun forMatrix(matrix: Matrix4fc): FloatArray = matrix.get(FloatArray(16))

    fun forRotation(matrix: Matrix4fc): FloatArray {
        val q = Quaternionf().apply {
            matrix.getUnnormalizedRotation(this)
        }
        return floatArrayOf(q.x, q.y, q.z, q.w)
    }

    fun forScale(matrix: Matrix4fc): FloatArray {
        val scale = Vector3f().apply {
            matrix.getScale(this)
        }
        return floatArrayOf(scale.x, scale.y, scale.z)
    }

    fun forTranslation(matrix: Matrix4fc): FloatArray {
        val dest = Vector3f().apply {
            matrix.getTranslation(this)
        }
        return floatArrayOf(dest.x, dest.y, dest.z)
    }
}
