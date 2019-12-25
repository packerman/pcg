package pcg.example

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Attribute.Normal
import pcg.scene.Attribute.Position
import pcg.scene.Float3VertexArray.Companion.Float3VertexArrayBuilder
import pcg.scene.Geometry
import pcg.scene.ShortIndexArray.Companion.ShortIndexArrayBuilder
import pcg.scene.oneMeshGeometry
import pcg.scene.scene
import pcg.util.*

class Box(
    o: Point3fc, a: Vector3fc, b: Vector3fc, c: Vector3fc,
    m: Int = 1, n: Int = 1, p: Int = 1,
    front: Boolean = true, back: Boolean = true,
    left: Boolean = true, right: Boolean = true,
    top: Boolean = true, bottom: Boolean = true
) {

    private val frontWall: Plane? = if (front) Plane(o, a, b, m, n) else null
    private val rightWall: Plane? = if (right) Plane(o + a, c, b, p, n) else null
    private val backWall: Plane? = if (back) Plane(Vector.add(o, a, c), -a, b, m, n) else null
    private val leftWall: Plane? = if (left) Plane(o + c, -c, b, p, n) else null
    private val topWall: Plane? = if (top) Plane(o + b, a, c, m, p) else null
    private val bottomWall: Plane? = if (bottom) Plane(o + c, a, -c, m, p) else null

    private val walls: List<Plane> =
        sequenceOf(frontWall, rightWall, backWall, leftWall, topWall, bottomWall).filterNotNull().toList()

    fun provideVertices(builder: Float3VertexArrayBuilder): Unit = with(builder) {
        walls.forEach { wall ->
            wall.provideVertices(this)
        }
    }

    fun provideNormals(builder: Float3VertexArrayBuilder): Unit = with(builder) {
        walls.forEach { wall ->
            wall.provideNormals(this)
        }
    }

    fun provideIndices(builder: ShortIndexArrayBuilder, offset: Int = 0) = with(builder) {
        var localOffset = offset
        walls.forEach { wall ->
            wall.provideIndices(builder, localOffset)
            localOffset += wall.vertexCount
        }
    }
}

fun box(
    o: Point3fc, a: Vector3fc, b: Vector3fc, c: Vector3fc,
    m: Int = 1, n: Int = 1, p: Int = 1
): Geometry {
    val box = Box(o, a, b, c, m, n, p)

    return oneMeshGeometry {
        vertexArray3f(attribute = Position) {
            box.provideVertices(this)
        }
        vertexArray3f(attribute = Normal) {
            box.provideNormals(this)
        }
        indexArray {
            box.provideIndices(this)
        }
    }
}

fun main() {
    val s = scene {
        node(
            box(
                o = Point3f(0f, 0f, 0f),
                a = Vector3f(10f, 0f, 0f),
                b = Vector3f(0f, 10f, 0f),
                c = Vector3f(0f, 0f, -10f),
                m = 1, n = 1, p = 1
            )
        ) {
            material()
        }
    }
    writeToFile("TestBox.gltf", s.compile(CompileOptions(interleaved = true)))
}
