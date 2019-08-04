package pcg

import com.google.gson.GsonBuilder

data class Gltf(
    val asset: Asset
)

data class Asset(
    val version: String,
    val minVersion: String?
)

val defaultAsset = Asset(
    version = "2.0",
    minVersion = "2.0"
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
        asset = defaultAsset
    )
    println(gltf.toJson())
}
