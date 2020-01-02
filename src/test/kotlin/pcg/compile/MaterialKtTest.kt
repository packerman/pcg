package pcg.compile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pcg.gltf.PbrMetallicRoughness
import pcg.scene.Color
import pcg.scene.Material
import pcg.scene.Texture
import pcg.gltf.Material as GltfMaterial

internal class MaterialKtTest {

    @Test
    internal fun shouldCompileDiffuseMaterial() {
        val material = Material(
            diffuse = Color(0.5f, 0.7f, 0.3f, 0.9f)
        )

        val actual = material.compile()

        val expected = GltfMaterial(
            pbrMetallicRoughness = PbrMetallicRoughness(
                baseColorFactor = floatArrayOf(0.5f, 0.7f, 0.3f, 0.9f),
                metallicFactor = 0f
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    internal fun shouldCompileTwoSidedMaterial() {
        val material = Material(
            diffuse = Color(0.5f, 0.7f, 0.3f, 0.9f),
            twoSided = true
        )

        val actual = material.compile()

        val expected = GltfMaterial(
            doubleSided = true,
            pbrMetallicRoughness = PbrMetallicRoughness(
                baseColorFactor = floatArrayOf(0.5f, 0.7f, 0.3f, 0.9f),
                metallicFactor = 0f
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    internal fun shouldKeepMaterialName() {
        val material = Material(
            name = "TestName"
        )

        val actual = material.compile()

        val expected = GltfMaterial(
            name = "TestName",
            pbrMetallicRoughness = PbrMetallicRoughness(
                metallicFactor = 0f
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    internal fun shouldMarkHasTexture() {
        assertTrue(Material(diffuseTexture = Texture("/textures/CesiumLogoFlat.png")).hasTextures())
        assertFalse(Material(diffuse = Color(100, 200, 100)).hasTextures())
    }
}
