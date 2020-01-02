package pcg.compile

import pcg.common.Filter
import pcg.common.Wrap
import pcg.gltf.Image
import pcg.gltf.Sampler
import pcg.scene.Texture
import java.io.File
import java.io.InputStream
import java.util.*

class TextureCompiler(private val texture: Texture) {

    val type =
        requireNotNull(extToType[getExtension(texture.fileName)]) { "Unknown extension: " + getExtension(texture.fileName) }

    val image = Image(
        uri = "data:" +
                type +
                ";base64," +
                encoder.encodeToString(readBytesResource(texture.fileName))
    )

    val sampler = Sampler(
        magFilter = Filter.Linear,
        minFilter = Filter.NearestMipmapLinear,
        wrapS = Wrap.Repeat,
        wrapT = Wrap.Repeat
    )

    companion object {

        private val extToType = mapOf(
            "png" to "image/png",
            "jpg" to "image/jpeg"
        )

        private fun getExtension(path: String) =
            File(path).extension

        private fun readBytesResource(path: String): ByteArray {
            val stream =
                requireNotNull(TextureCompiler::class.java.getResourceAsStream(path)) { "Resource '$path' not found" }
            return stream.use(InputStream::readBytes)
        }

        private val encoder = Base64.getEncoder()
    }
}
