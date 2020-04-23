package pcg.example

import org.joml.Vector3f
import org.joml.Vector3fc
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Attribute.*
import pcg.scene.Float2VertexArray
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
    top: Boolean = true, bottom: Boolean = true,
    texture: Boolean = true
) {

    private val frontWall: Plane? = if (front) Plane(o, a, b, m, n, texture) else null
    private val rightWall: Plane? = if (right) Plane(o + a, c, b, p, n, texture) else null
    private val backWall: Plane? = if (back) Plane(Vector.add(o, a, c), -a, b, m, n, texture) else null
    private val leftWall: Plane? = if (left) Plane(o + c, -c, b, p, n, texture) else null
    private val topWall: Plane? = if (top) Plane(o + b, a, c, m, p, texture) else null
    private val bottomWall: Plane? = if (bottom) Plane(o + c, a, -c, m, p, texture) else null

    private val walls: List<Plane> =
        sequenceOf(frontWall, rightWall, backWall, leftWall, topWall, bottomWall)
            .filterNotNull().toList()

    val vertexCount = walls.sumBy(Plane::vertexCount)

    fun provideVertices(builder: Float3VertexArrayBuilder): Unit = with(builder) {
        walls.forEach { wall ->
            wall.provideVertices(this)
        }
    }

    fun provideTextures(builder: Float2VertexArray.Companion.Float2VertexArrayBuilder) = with(builder) {
        walls.forEach { wall ->
            wall.provideTextures(this)
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
    m: Int = 1, n: Int = 1, p: Int = 1,
    texture: Boolean = true
): Geometry {
    require(a.lengthSquared() > 0f)
    require(b.lengthSquared() > 0f)
    require(c.lengthSquared() > 0f)
    require(m >= 1)
    require(m >= 1)
    require(p >= 1)

    val box = Box(o, a, b, c, m, n, p, texture = texture)

    return oneMeshGeometry {
        vertexArray3f(attribute = Position) {
            box.provideVertices(this)
        }
        vertexArray2f(attribute = TexCoord) {
            box.provideTextures(this)
        }
        vertexArray3f(attribute = Normal) {
            box.provideNormals(this)
        }
        indexArray {
            box.provideIndices(this)
        }
    }
}

fun box(
    width: Float, height: Float, length: Float,
    m: Int = 1, n: Int = 1, p: Int = 1,
    texture: Boolean = true
): Geometry {
    require(width > 0f)
    require(height > 0f)
    require(length > 0f)
    return box(
        Point3f(0f, 0f, 0f),
        Vector3f(width, 0f, 0f),
        Vector3f(0f, height, 0f),
        Vector3f(0f, 0f, -length),
        m, n, p, texture
    )
}

fun boxWithWindow(
    o: Point3fc, a: Vector3fc, b: Vector3fc, c: Vector3fc,
    uMin: Float, uMax: Float,
    vMin: Float, vMax: Float
): Geometry {
    require(uMin in 0f..1f)
    require(uMax in 0f..1f)
    require(vMin in 0f..1f)
    require(vMax in 0f..1f)
    require(uMin < uMax)
    require(vMin < vMax)

    val leftBox = Box(o, a * uMin, b, c, right = false)
    val rightBox = Box(o + a * uMax, a * (1f - uMax), b, c, left = false)
    val bottomBox = Box(o + a * uMin, a * (uMax - uMin), b, c * vMin, back = false)
    val topBox = Box(o + a * uMin + c * vMax, a * (uMax - uMin), b, c * (1 - vMax), front = false)
    val boxes = listOf(leftBox, rightBox, bottomBox, topBox)

    val leftPlane = Plane(o + a * uMin + c * vMin, c * (vMax - vMin), b, texture = false)
    val rightPlane = Plane(o + a * uMax + c * vMax, c * (vMin - vMax), b, texture = false)
    val bottomPlane = Plane(o + a * uMax + c * vMin, a * (uMin - uMax), b, texture = false)
    val topPlane = Plane(o + a * uMin + c * vMax, a * (uMax - uMin), b, texture = false)
    val planes = listOf(leftPlane, rightPlane, bottomPlane, topPlane)

    return oneMeshGeometry {
        vertexArray3f(attribute = Position) {
            boxes.forEach { it.provideVertices(this) }
            planes.forEach { it.provideVertices(this) }
        }

        vertexArray3f(attribute = Normal) {
            boxes.forEach { it.provideNormals(this) }
            planes.forEach { it.provideNormals(this) }
        }

        indexArray {
            var localOffset = 0
            boxes.forEach {
                it.provideIndices(this, localOffset)
                localOffset += it.vertexCount
            }
            planes.forEach {
                it.provideIndices(this, localOffset)
                localOffset += it.vertexCount
            }
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
