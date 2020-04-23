package pcg.example

import org.joml.Vector3f
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Color
import pcg.scene.Material
import pcg.scene.Node.Companion.NodeBuilder
import pcg.scene.Texture
import pcg.scene.scene
import pcg.util.Point3f

fun main() {
    val levelHeight = 8f
    val planeWidth = 200f
    val planeLength = 200f
    val stairsLength = 16f
    val stairsWidth = 8f
    val thickness = 1f
    val floorWidth = 50f
    val floorLength = 50f
    val windowMargin = 2f
    val uLength = stairsWidth / floorWidth
    val vLength = (stairsLength + windowMargin) / floorLength
    val uMin = 0.2f
    val vMin = 0.5f
    val barrierWidth = stairsWidth + 4f
    val barrierLength = 0.25f
    val boxSize = 6f
    val vMargin = windowMargin / floorLength
    val floorMaterial = Material(name = "floor", diffuse = Color(128, 128, 0))
    val stairsMaterial = Material(name = "stairs", diffuse = Color(189, 183, 107))
    val groundMaterial =
        Material(name = "ground", twoSided = true, diffuseTexture = Texture("/textures/playground/grass.png"))
    val columnMaterial = Material(name = "column", diffuse = Color(85, 107, 47))
    val boxMaterial = Material(name = "box", diffuseTexture = Texture("/textures/playground/crate.png"))
    val myStairs = stairs2(levelHeight, stairsLength, stairsWidth, thickness, 8)
    val ground = horizontalPlane(planeWidth, planeLength, m = 25, n = 25)
    val myBox = box(boxSize, boxSize, boxSize)
    val floor = box(floorWidth, thickness, floorLength)
    val floor2 = boxWithWindow(
        Point3f(0f, 0f, 0f),
        Vector3f(floorWidth, 0f, 0f),
        Vector3f(0f, thickness, 0f),
        Vector3f(0f, 0f, -floorLength),
        uMin, uMin + uLength,
        vMin, vMin + vLength
    )
    val column = box(
        thickness,
        levelHeight,
        thickness
    )
    val barrier = box(barrierWidth, 4f, barrierLength)

    fun placeColumns(builder: NodeBuilder) = with(builder) {
        node(column) {
            material(columnMaterial)
        }
        node(column) {
            material(columnMaterial)
            translate(floorWidth - thickness, 0f, 0f)
        }
        node(column) {
            material(columnMaterial)
            translate(0f, 0f, -floorLength + thickness)
        }
        node(column) {
            material(columnMaterial)
            translate(floorWidth - thickness, 0f, -floorLength + thickness)
        }
    }

    fun placeBoxes(builder: NodeBuilder, vararg where: Pair<Float, Float>) = with(builder) {
        for ((u, v) in where) {
            node(myBox) {
                translate(floorWidth * u, 0f, -floorLength * v)
                material(boxMaterial)
            }
        }
    }

    val s = scene {
        node(ground) {
            name = "ground"
            material(groundMaterial)

            node {
                name = "levels"
                translate((planeWidth - floorWidth) / 2, 0f, -(planeLength - floorLength) / 2)

                node {
                    name = "level0"
                    node(myStairs) {
                        name = "stairs,level0"
                        translate((floorWidth - stairsWidth) / 2f, 0f, stairsLength)
                        material(stairsMaterial)
                    }
                    node {
                        name = "columns,level0"
                        placeColumns(this)
                    }
                    placeBoxes(this, 0.3f to 0.6f, 0.8f to 0.7f)
                }

                node {
                    name = "level1"
                    translate(0f, levelHeight - thickness, 0f)
                    node(floor) {
                        name = "floor,level1"
                        material(floorMaterial)
                    }
                    placeColumns(this)
                    node {
                        translate(0f, thickness, 0f)
                        placeBoxes(this, 0.75f to 0.25f)
                        node(myStairs) {
                            name = "stairs,level1"
                            translate(uMin * floorWidth, 0f, -(vMin + vMargin) * floorLength)
                            material(stairsMaterial)
                        }
                    }
                }

                node {
                    name = "level2"
                    translate(0f, 2 * levelHeight - thickness, 0f)
                    node(floor2) {
                        material(floorMaterial)
                    }
                    node {
                        translate(0f, thickness, 0f)
                        placeBoxes(this, 0.75f to 0.75f)
                        node(barrier) {
                            translate(
                                uMin * floorWidth - (barrierWidth - stairsWidth) / 2f,
                                0f,
                                -floorLength + barrierLength
                            )
                            material(stairsMaterial)
                        }
                    }
                }
            }
        }
    }

    writeToFile("TestPlatform2.gltf", s.compile(CompileOptions(interleaved = true)))
}
