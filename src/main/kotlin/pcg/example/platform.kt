package pcg.example

import org.joml.Vector3f
import pcg.compile.CompileOptions
import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.Material
import pcg.scene.scene
import pcg.util.Point3f

fun main() {
    val levelHeight = 5f
    val planeWidth = 70f
    val planeLength = 70f
    val standardMaterial = Material()
    val stairsLength = 5f
    val stairsWidth = 4f
    val thickness = 0.5f
    val floorWidth = 50f
    val floorLength = 50f
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
    val column = box(
        Point3f(0f, 0f, 0f),
        Vector3f(thickness, 0f, 0f),
        Vector3f(0f, levelHeight, 0f),
        Vector3f(0f, 0f, -thickness)
    )
    val s = scene {
        node(ground) {
            name = "ground"
            material(twoSided = true)
            node {
                name = "level0=stairs+floor"
                node(myStairs) {
                    name = "stair,level0"
                    translate((floorWidth - stairsWidth) / 2f, 0f, stairsLength)
                    material(standardMaterial)
                }
                node(floor) {
                    material(standardMaterial)
                    translate(0f, levelHeight - thickness, 0f)
                }
                node {
                    name = "columns,level0"
                    node(column) {
                        material(standardMaterial)
                    }
                    node(column) {
                        material(standardMaterial)
                        translate(floorWidth - thickness, 0f, 0f)
                    }
                    node(column) {
                        material(standardMaterial)
                        translate(0f, 0f, -floorLength - thickness)
                    }
                    node(column) {
                        material(standardMaterial)
                        translate(floorWidth - thickness, 0f, -floorLength - thickness)
                    }
                }
                translate((planeWidth - floorWidth) / 2, 0f, -(planeLength - floorLength) / 2)
            }
        }
    }

    writeToFile("TestPlatform.gltf", s.compile(CompileOptions(interleaved = true)))
}
