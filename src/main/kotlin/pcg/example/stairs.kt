package pcg.example

import pcg.compile.compile
import pcg.gltf.writeToFile
import pcg.scene.scene

fun main() {
    val s = scene { }

    writeToFile("TestStairs.gltf", compile(s))
}
