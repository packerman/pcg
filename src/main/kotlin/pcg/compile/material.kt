package pcg.compile

import com.google.gson.JsonObject
import pcg.gltf.PbrMetallicRoughness
import pcg.scene.Material
import pcg.scene.Texture
import pcg.util.nullIfDefault
import pcg.gltf.Material as GltfMaterial

/**
 * See
 * https://github.com/BabylonJS/Babylon.js/blob/master/serializers/src/glTF/2.0/glTFMaterialExporter.ts
 * for conversion example
 */

fun Material.compile(textureIndex: Map<Texture, Int> = emptyMap()): GltfMaterial =
    GltfMaterial.withoutDefaults(
        name = name,
        pbrMetallicRoughness = PbrMetallicRoughness.withoutDefaults(
            baseColorFactor = floatArrayOf(diffuse.red, diffuse.green, diffuse.blue, diffuse.alpha),
            baseColorTexture = diffuseTexture?.let { texture ->
                JsonObject().apply {
                    addProperty("index", textureIndex.getValue(texture))
                }
            },
            roughnessFactor = 1f,
            metallicFactor = 0f
        ),
        doubleSided = nullIfDefault(twoSided, false)
    )
