package pcg

import com.google.gson.GsonBuilder

data class Gltf(
    val asset: Asset,
    val scene: Int? = null,
    val scenes: List<Scene>? = null
) {

    init {
        scenes?.let { requireNotEmpty(it, "scenes") }
        scene?.let { requireInRange(it, scenes, "scene") }
    }
}

data class Asset(
    val version: String,
    val minVersion: String? = null
)

data class Scene(
    val nodes: List<Int>? = null,
    val name: String? = null
) {

    init {
        nodes?.let { requireNotEmpty(it, "nodes") }
    }
}

val defaultAsset = Asset(
    version = "2.0"
)

fun Gltf.toJson(prettyPrinting: Boolean = false): String {
    val builder = GsonBuilder()
    if (prettyPrinting) {
        builder.setPrettyPrinting()
    }
    val gson = builder.create()
    return gson.toJson(this)
}

fun main() {
    val gltf = Gltf(
        asset = defaultAsset,
        scene = 0,
        scenes = listOf(
            Scene(nodes = listOf(0))
        )
    )
    println(gltf.toJson(true))
}

fun requireNotEmpty(list: List<*>, name: String) =
    require(list.isNotEmpty()) { "'$name' has to be not empty" }

fun requireNonNegative(n: Int, name: String) =
    require(n >= 0) { "'$name' has to be non-negative" }

fun requireInRange(i: Int, list: List<*>?, name: String) =
    require(list != null && i >= 0 && i < list.size) { "'$name' is not valid index" }
