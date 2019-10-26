package pcg.compile

import pcg.gltf.PbrMetallicRoughness
import pcg.scene.Material
import pcg.util.nullIfDefault
import pcg.gltf.Material as GltfMaterial

/**
 * See
 * https://github.com/BabylonJS/Babylon.js/blob/master/serializers/src/glTF/2.0/glTFMaterialExporter.ts
 * for conversion example
 */

fun Material.compile(): GltfMaterial =
    GltfMaterial.withoutDefaults(
        name = null,
        pbrMetallicRoughness = PbrMetallicRoughness.withoutDefaults(
            baseColorFactor = floatArrayOf(diffuse.red, diffuse.green, diffuse.blue, diffuse.alpha),
            roughnessFactor = 1f,
            metallicFactor = 0f
        ),
        doubleSided = nullIfDefault(twoSided, false)
    )
