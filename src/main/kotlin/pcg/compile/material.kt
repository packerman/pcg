package pcg.compile

import pcg.gltf.PbrMetallicRoughness
import pcg.scene.Material
import pcg.gltf.Material as GltfMaterial

/**
 * See
 * https://github.com/BabylonJS/Babylon.js/blob/master/serializers/src/glTF/2.0/glTFMaterialExporter.ts
 * for conversion example
 */

fun Material.compile(): GltfMaterial =
    GltfMaterial(
        pbrMetallicRoughness = PbrMetallicRoughness(
            baseColorFactor = floatArrayOf(diffuse.red, diffuse.green, diffuse.blue, diffuse.alpha),
            roughnessFactor = 1f,
            metallicFactor = 0f
        )
    )
