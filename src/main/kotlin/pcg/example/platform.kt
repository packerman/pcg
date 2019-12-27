package pcg.example

import org.joml.Vector3f
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Color
import pcg.scene.Material
import pcg.scene.Node.Companion.NodeBuilder
import pcg.scene.scene
import pcg.util.Point3f

fun main() {
    val levelHeight = 5f
    val planeWidth = 70f
    val planeLength = 70f
    val standardMaterial = Material()
    val stairsLength = 5f
    val stairsWidth = 4f
    val thickness = 1f
    val floorWidth = 50f
    val floorLength = 50f
    val uLength = stairsWidth / floorWidth
    val vLength = stairsLength / floorLength
    val uMin = 0.25f
    val vMin = 0.75f
    val floorMaterial = Material(diffuse = Color(154, 205, 50))
    val myStairs = stairs2(levelHeight, stairsLength, stairsWidth, thickness, 6)
    val ground = plane(
        Point3f(0f, 0f, 0f),
        Vector3f(planeWidth, 0f, 0f),
        Vector3f(0f, 0f, -planeLength)
    )
    val floor = box(
        Point3f(0f, 0f, 0f),
        Vector3f(floorWidth, 0f, 0f),
        Vector3f(0f, thickness, 0f),
        Vector3f(0f, 0f, -floorLength)
    )
    val floor2 = boxWithWindow(
        Point3f(0f, 0f, 0f),
        Vector3f(floorWidth, 0f, 0f),
        Vector3f(0f, thickness, 0f),
        Vector3f(0f, 0f, -floorLength),
        uMin, uMin + uLength,
        vMin, vMin + vLength
    )
    val column = box(
        Point3f(0f, 0f, 0f),
        Vector3f(thickness, 0f, 0f),
        Vector3f(0f, levelHeight, 0f),
        Vector3f(0f, 0f, -thickness)
    )

    fun placeColumns(builder: NodeBuilder) = with(builder) {
        node(column) {
            material(standardMaterial)
        }
        node(column) {
            material(standardMaterial)
            translate(floorWidth - thickness, 0f, 0f)
        }
        node(column) {
            material(standardMaterial)
            translate(0f, 0f, -floorLength + thickness)
        }
        node(column) {
            material(standardMaterial)
            translate(floorWidth - thickness, 0f, -floorLength + thickness)
        }
    }

    val s = scene {
        node(ground) {
            name = "ground"
            material(twoSided = true)

            node {
                name = "levels"
                translate((planeWidth - floorWidth) / 2, 0f, -(planeLength - floorLength) / 2)

                node {
                    name = "level0"
                    node(myStairs) {
                        name = "stair,level0"
                        translate((floorWidth - stairsWidth) / 2f, 0f, stairsLength)
                        material(standardMaterial)
                    }
                    node {
                        name = "columns,level0"
                        placeColumns(this)
                    }
                }

                node {
                    name = "level1"
                    translate(0f, levelHeight - thickness, 0f)
                    node(floor) {
                        material(standardMaterial)
                    }
                    placeColumns(this)
                }

                node {
                    name = "level2"
                    translate(0f, 2 * levelHeight - thickness, 0f)
                    node(floor2) {
                        material(floorMaterial)
                    }
                }
            }
        }
    }

    writeToFile("TestPlatform.gltf", s.compile(CompileOptions(interleaved = true)))
}
