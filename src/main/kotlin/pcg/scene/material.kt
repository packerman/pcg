package pcg.scene

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f)

class Texture(val fileName: String)

data class Material(
    val name: String? = null,
    val twoSided: Boolean = false,
    val diffuse: Color = Color(1f, 1f, 1f),
    val specular: Color = Color(0f, 0f, 0f),
    val emission: Color = Color(0f, 0f, 0f),
    val opacity: Color = Color(1f, 1f, 1f),
    val transparency: Color = Color(0f, 0f, 0f),
    val specularPower: Float = 1f,
    val diffuseTexture: Texture? = null,
    val specularTexture: Texture? = null,
    val specularPowerTexture: Texture? = null,
    val emissionTexture: Texture? = null,
    val opacityTexture: Texture? = null,
    val transparencyTexture: Texture? = null,
    val normalTexture: Texture? = null
)
