package pcg.example

import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.*
import pcg.scene.Mesh.Companion.Attribute

val cubeGeometry =
    oneMeshGeometry {
        vertexArray3f(attribute = Attribute.Position) {
            add(-0.5f, -0.5f, -0.5f)
            add(-0.5f, 0.5f, -0.5f)

            add(0.5f, 0.5f, -0.5f)
            add(0.5f, -0.5f, -0.5f)

            add(-0.5f, -0.5f, 0.5f)
            add(0.5f, -0.5f, 0.5f)

            add(0.5f, 0.5f, 0.5f)
            add(-0.5f, 0.5f, 0.5f)

            add(-0.5f, -0.5f, -0.5f)
            add(0.5f, -0.5f, -0.5f)

            add(0.5f, -0.5f, 0.5f)
            add(-0.5f, -0.5f, 0.5f)

            add(0.5f, -0.5f, -0.5f)
            add(0.5f, 0.5f, -0.5f)

            add(0.5f, 0.5f, 0.5f)
            add(0.5f, -0.5f, 0.5f)

            add(0.5f, 0.5f, -0.5f)
            add(-0.5f, 0.5f, -0.5f)

            add(-0.5f, 0.5f, 0.5f)
            add(0.5f, 0.5f, 0.5f)

            add(-0.5f, 0.5f, -0.5f)
            add(-0.5f, -0.5f, -0.5f)

            add(-0.5f, -0.5f, 0.5f)
            add(-0.5f, 0.5f, 0.5f)
        }
        vertexArray3f(attribute = Attribute.Normal) {
            add(0.0f, 0.0f, -1.0f)
            add(0.0f, 0.0f, -1.0f)
            add(0.0f, 0.0f, -1.0f)
            add(0.0f, 0.0f, -1.0f)
            add(0.0f, 0.0f, 1.0f)
            add(0.0f, 0.0f, 1.0f)
            add(0.0f, 0.0f, 1.0f)
            add(0.0f, 0.0f, 1.0f)
            add(0.0f, -1.0f, 0.0f)
            add(0.0f, -1.0f, 0.0f)
            add(0.0f, -1.0f, 0.0f)
            add(0.0f, -1.0f, 0.0f)
            add(1.0f, 0.0f, 0.0f)
            add(1.0f, 0.0f, 0.0f)
            add(1.0f, 0.0f, 0.0f)
            add(1.0f, 0.0f, 0.0f)
            add(0.0f, 1.0f, 0.0f)
            add(0.0f, 1.0f, 0.0f)
            add(0.0f, 1.0f, 0.0f)
            add(0.0f, 1.0f, 0.0f)
            add(-1.0f, 0.0f, 0.0f)
            add(-1.0f, 0.0f, 0.0f)
            add(-1.0f, 0.0f, 0.0f)
            add(-1.0f, 0.0f, 0.0f)
        }
        indexArray {
            add(0, 1, 2)
            add(2, 3, 0)
            add(4, 5, 6)
            add(6, 7, 4)
            add(8, 9, 10)
            add(10, 11, 8)
            add(12, 13, 14)
            add(14, 15, 12)
            add(16, 17, 18)
            add(18, 19, 16)
            add(20, 21, 22)
            add(22, 23, 20)
        }
    }

fun planeGeometry(a: Float, b: Float): Geometry {
    val ha = a / 2
    val hb = b / 2
    return oneMeshGeometry {
        vertexArray3f(attribute = Attribute.Position) {
            add(-ha, 0f, hb)
            add(ha, 0f, hb)
            add(-ha, 0f, -hb)
            add(ha, 0f, -hb)
        }
        vertexArray3f(attribute = Attribute.Normal) {
            add(0f, 1f, 0f)
            add(0f, 1f, 0f)
            add(0f, 1f, 0f)
            add(0f, 1f, 0f)
        }
        indexArray {
            add(0, 1, 2)
            add(1, 3, 2)
        }
    }
}

fun main() {
    val s = scene {
        node(cubeGeometry, Material(diffuse = Color(0.8f, 0f, 0f))) {
            translate(-1.5f, 0f, 1.5f)
        }
        node(cubeGeometry, Material(diffuse = Color(0f, 0.8f, 0f))) {
            translate(1.5f, 0f, 1.5f)
        }
        node(cubeGeometry, Material(diffuse = Color(0f, 0f, 0.8f))) {
            translate(-1.5f, 0f, -1.5f)
        }
        node(cubeGeometry, Material(diffuse = Color(0.8f, 0.8f, 0f))) {
            translate(1.5f, 0f, -1.5f)
        }
        node(
            planeGeometry(6f, 6f),
            Material(
                diffuse = Color(0.5f, 0.5f, 0.5f),
                twoSided = true
            )
        ) {
            translate(0f, -0.5f, 0f)
        }
    }
    writeToFile("TestFourCubes.gltf", compile(s))
}
