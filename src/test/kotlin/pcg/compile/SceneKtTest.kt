package pcg.compile

import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pcg.common.*
import pcg.gltf.*
import pcg.scene.Attribute.*
import pcg.scene.Color
import pcg.scene.Material
import pcg.scene.Texture
import pcg.scene.geometry
import pcg.scene.scene
import java.io.File
import pcg.gltf.Material as GltfMaterial

internal class SceneKtTest {

    @Test
    fun shouldCompileTriangleWithoutIndices() {
        val g = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                }
            }
        }
        val m = Material()
        val s = scene {
            node(geometry = g) {
                material(m)
            }
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 0
                            ),
                            material = 0
                        )
                    )
                )
            ),
            materials = listOf(
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(metallicFactor = 0f)
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteLength = 36,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 36,
                    uri = "data:application/octet-stream;base64,AAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAA"
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestTriangleWithoutIndices.gltf", compiledGltf)
    }

    @Test
    fun shouldCompileTriangleWithIndices() {
        val g = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                }
                indexArray {
                    add(0, 1, 2)
                }
            }
        }
        val m = Material()
        val s = scene {
            node(geometry = g) {
                material(m)
            }
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 1
                            ),
                            indices = 0,
                            material = 0
                        )
                    )
                )
            ),
            materials = listOf(
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(metallicFactor = 0f)
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 6,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 8,
                    byteLength = 36,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf(2.toShort()),
                    min = listOf(0.toShort())
                ),
                Accessor(
                    bufferView = 1,
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 44,
                    uri = "data:application/octet-stream;base64,AAABAAIAAAAAAAAAAAAAAAAAAAAAAIA/AAAAAAAAAAAAAAAAAACAPwAAAAA="
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestTriangleWithIndices.gltf", compiledGltf)
    }

    @Test
    fun shouldCompileSimpleMeshes() {
        val g = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                }
                vertexArray3f(attribute = Normal) {
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                }
                indexArray {
                    add(0, 1, 2)
                }
            }
        }
        val m = Material()
        val s = scene {
            node(geometry = g) {
                material(m)
            }
            node(geometry = g) {
                material(m)
                translate(1f, 0f, 0f)
            }
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0, 1
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                ),
                Node(
                    mesh = 0,
                    translation = floatArrayOf(1f, 0f, 0f)
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 1,
                                Attribute.NORMAL to 2
                            ),
                            indices = 0,
                            material = 0
                        )
                    )
                )
            ),
            materials = listOf(
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(metallicFactor = 0f)
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 6,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 8,
                    byteLength = 72,
                    byteStride = 12,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf<Short>(2),
                    min = listOf<Short>(0)
                ),
                Accessor(
                    bufferView = 1,
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                ),
                Accessor(
                    bufferView = 1,
                    byteOffset = 36,
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(0f, 0f, 1f),
                    min = listOf(0f, 0f, 1f)
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 80,
                    uri = "data:application/octet-stream;base64,AAABAAIAAAAAAAAAAAAAAAAAAAAAAIA/AAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAAAAAAAAAAgD8AAAAAAAAAAAAAgD8="
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestSimpleMesh.gltf", compiledGltf)
    }

    @Test
    fun shouldCompileManyGeometries() {
        val triangle = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                }
                indexArray {
                    add(0, 1, 2)
                }
            }
        }
        val triangleMaterial = Material(
            diffuse = Color(0.8f, 0f, 0f)
        )
        val square = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                    add(1f, 1f, 0f)
                }
                indexArray {
                    add(0, 1, 2)
                    add(1, 3, 2)
                }

            }
        }
        val squareMaterial = Material(
            diffuse = Color(0f, 0.8f, 0f)
        )
        val s = scene {
            node(triangle) {
                material(triangleMaterial)
            }
            node(square) {
                material(squareMaterial)
                translate(1f, 0f, 0f)
            }
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0, 1
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                ),
                Node(
                    mesh = 1,
                    translation = floatArrayOf(1f, 0f, 0f)
                )
            ),
            materials = listOf(
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0.8f, 0f, 0f, 1f),
                        metallicFactor = 0f
                    )
                ),
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0f, 0.8f, 0f, 1f),
                        metallicFactor = 0f
                    )
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 1
                            ),
                            indices = 0,
                            material = 0
                        )
                    )
                ),
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 3
                            ),
                            indices = 2,
                            material = 1
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 6,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 8,
                    byteLength = 36,
                    target = BufferTarget.ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 1,
                    byteOffset = 0,
                    byteLength = 12,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 1,
                    byteOffset = 12,
                    byteLength = 48,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf<Short>(2),
                    min = listOf<Short>(0)
                ),
                Accessor(
                    bufferView = 1,
                    componentType = ComponentType.FLOAT,
                    count = 3,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                ),
                Accessor(
                    bufferView = 2,
                    byteOffset = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 6,
                    type = Type.SCALAR,
                    max = listOf<Short>(3),
                    min = listOf<Short>(0)
                ),
                Accessor(
                    bufferView = 3,
                    byteOffset = 0,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 44,
                    uri = "data:application/octet-stream;base64,AAABAAIAAAAAAAAAAAAAAAAAAAAAAIA/AAAAAAAAAAAAAAAAAACAPwAAAAA="
                ),
                Buffer(
                    byteLength = 60,
                    uri = "data:application/octet-stream;base64,AAABAAIAAQADAAIAAAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAAAACAPwAAgD8AAAAA"
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestManyGeometries.gltf", compiledGltf)
    }

    @Test
    internal fun shouldCompileMultiMaterialNode() {
        val red = Material(
            diffuse = Color(0.8f, 0f, 0f),
            twoSided = true
        )
        val green = Material(
            diffuse = Color(0f, 0.8f, 0f),
            twoSided = true
        )
        val blue = Material(
            diffuse = Color(0f, 0f, 0.8f),
            twoSided = true
        )
        val yellow = Material(
            diffuse = Color(0.8f, 0.8f, 0f),
            twoSided = true
        )
        val g = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                    add(1f, 1f, 0f)

                    add(2f, 0f, 0f)
                    add(2f, 1f, 0f)
                }
                indexArray(material = 0) {
                    add(0, 1, 2)
                }
                indexArray(material = 3) {
                    add(4, 5, 3)
                }
                indexArray(material = 1) {
                    add(1, 3, 2)
                }
                indexArray(material = 2) {
                    add(1, 4, 3)
                }
            }
        }
        val s = scene {
            node(geometry = g) {
                material(0, red)
                material(1, green)
                material(2, blue)
                material(3, yellow)
            }
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 4
                            ),
                            indices = 0,
                            material = 0
                        ),
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 4
                            ),
                            indices = 1,
                            material = 3
                        ),
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 4
                            ),
                            indices = 2,
                            material = 1
                        ),
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 4
                            ),
                            indices = 3,
                            material = 2
                        )
                    )
                )
            ),
            materials = listOf(
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0.8f, 0f, 0f, 1f),
                        metallicFactor = 0f
                    ),
                    doubleSided = true
                ),
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0f, 0.8f, 0f, 1f),
                        metallicFactor = 0f
                    ),
                    doubleSided = true
                ),
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0f, 0f, 0.8f, 1f),
                        metallicFactor = 0f
                    ),
                    doubleSided = true
                ),
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0.8f, 0.8f, 0f, 1f),
                        metallicFactor = 0f
                    ),
                    doubleSided = true
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 24,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 24,
                    byteLength = 72,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    byteOffset = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf<Short>(2),
                    min = listOf<Short>(0)
                ),
                Accessor(
                    bufferView = 0,
                    byteOffset = 6,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf<Short>(5),
                    min = listOf<Short>(3)
                ),
                Accessor(
                    bufferView = 0,
                    byteOffset = 12,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf<Short>(3),
                    min = listOf<Short>(1)
                ),
                Accessor(
                    bufferView = 0,
                    byteOffset = 18,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 3,
                    type = Type.SCALAR,
                    max = listOf<Short>(4),
                    min = listOf<Short>(1)
                ),
                Accessor(
                    bufferView = 1,
                    componentType = ComponentType.FLOAT,
                    count = 6,
                    type = Type.VEC3,
                    max = listOf(2f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 96,
                    uri = "data:application/octet-stream;base64,AAABAAIABAAFAAMAAQADAAIAAQAEAAMAAAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAAAACAPwAAgD8AAAAAAAAAQAAAAAAAAAAAAAAAQAAAgD8AAAAA"
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestMultiMaterial.gltf", compiledGltf)
    }

    @Test
    fun shouldCompileTextures() {
        val square = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                    add(1f, 1f, 0f)
                }
                vertexArray2f(attribute = TexCoord) {
                    add(0f, 0f)
                    add(1f, 0f)
                    add(0f, 1f)
                    add(1f, 1f)
                }
                vertexArray3f(attribute = Normal) {
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                }
                indexArray {
                    add(0, 1, 2)
                    add(1, 3, 2)
                }
            }
        }
        val squareMaterial = Material(
            twoSided = true,
            diffuseTexture = Texture("/textures/CesiumLogoFlat.png")
        )
        val s = scene {
            node(square) {
                material(squareMaterial)
            }
        }

        val compiledGltf = compile(s)

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                )
            ),
            materials = listOf(
                GltfMaterial(
                    doubleSided = true,
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorTexture = JsonObject().apply {
                            addProperty("index", 0)
                        },
                        metallicFactor = 0f
                    )
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 1,
                                Attribute.TEXCOORD_0 to 3,
                                Attribute.NORMAL to 2
                            ),
                            indices = 0,
                            material = 0
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 12,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 12,
                    byteStride = 12,
                    byteLength = 96,
                    target = BufferTarget.ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 108,
                    byteLength = 32,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    byteOffset = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 6,
                    type = Type.SCALAR,
                    max = listOf(3.toShort()),
                    min = listOf(0.toShort())
                ),
                Accessor(
                    bufferView = 1,
                    byteOffset = 0,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                ),
                Accessor(
                    bufferView = 1,
                    byteOffset = 48,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    max = listOf(0f, 0f, 1f),
                    min = listOf(0f, 0f, 1f),
                    type = Type.VEC3
                ),
                Accessor(
                    bufferView = 2,
                    byteOffset = 0,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    type = Type.VEC2,
                    max = listOf(1f, 1f),
                    min = listOf(0f, 0f)
                )
            ),
            textures = listOf(
                Texture(
                    sampler = 0,
                    source = 0
                )
            ),
            images = listOf(
                Image(
                    uri = javaClass.getResourceAsStream("/textures/CesiumLogoFlat.base64.txt").use { String(it.readBytes()).trim() }
                )
            ),
            samplers = listOf(
                Sampler(
                    magFilter = Filter.Linear,
                    minFilter = Filter.NearestMipmapLinear,
                    wrapS = Wrap.Repeat,
                    wrapT = Wrap.Repeat
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 140,
                    uri = "data:application/octet-stream;base64,AAABAAIAAQADAAIAAAAAAAAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAgD8AAAAAAACAPwAAgD8AAAAAAAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AACAPwAAgD8="
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestCompileTextures.gltf", compiledGltf)
    }

    @Test
    fun shouldCompileInterleaved() {
        val square = geometry {
            mesh {
                vertexArray3f(attribute = Position) {
                    add(0f, 0f, 0f)
                    add(1f, 0f, 0f)
                    add(0f, 1f, 0f)
                    add(1f, 1f, 0f)
                }
                vertexArray2f(attribute = TexCoord) {
                    add(0f, 0f)
                    add(1f, 0f)
                    add(0f, 1f)
                    add(1f, 1f)
                }
                vertexArray3f(attribute = Normal) {
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                    add(0f, 0f, 1f)
                }
                indexArray {
                    add(0, 1, 2)
                    add(1, 3, 2)
                }
            }
        }
        val squareMaterial = Material(
            twoSided = true,
            diffuseTexture = Texture("/textures/CesiumLogoFlat.png")
        )
        val s = scene {
            node(square) {
                material(squareMaterial)
            }
        }

        val compiledGltf = compile(s, CompileOptions(interleaved = true))

        val expectedGltf = Gltf(
            scenes = listOf(
                Scene(
                    nodes = listOf(
                        0
                    )
                )
            ),
            nodes = listOf(
                Node(
                    mesh = 0
                )
            ),
            materials = listOf(
                GltfMaterial(
                    doubleSided = true,
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorTexture = JsonObject().apply {
                            addProperty("index", 0)
                        },
                        metallicFactor = 0f
                    )
                )
            ),
            meshes = listOf(
                Mesh(
                    primitives = listOf(
                        Primitive(
                            attributes = mapOf(
                                Attribute.POSITION to 1,
                                Attribute.TEXCOORD_0 to 2,
                                Attribute.NORMAL to 3
                            ),
                            indices = 0,
                            material = 0
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 12,
                    target = BufferTarget.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 12,
                    byteStride = 32,
                    byteLength = 128,
                    target = BufferTarget.ARRAY_BUFFER
                )
            ),
            accessors = listOf(
                Accessor(
                    bufferView = 0,
                    byteOffset = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 6,
                    type = Type.SCALAR,
                    max = listOf(3.toShort()),
                    min = listOf(0.toShort())
                ),
                Accessor(
                    bufferView = 1,
                    byteOffset = 0,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    type = Type.VEC3,
                    max = listOf(1f, 1f, 0f),
                    min = listOf(0f, 0f, 0f)
                ),
                Accessor(
                    bufferView = 1,
                    byteOffset = 12,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    type = Type.VEC2,
                    max = listOf(1f, 1f),
                    min = listOf(0f, 0f)
                ),
                Accessor(
                    bufferView = 1,
                    byteOffset = 20,
                    componentType = ComponentType.FLOAT,
                    count = 4,
                    max = listOf(0f, 0f, 1f),
                    min = listOf(0f, 0f, 1f),
                    type = Type.VEC3
                )
            ),
            textures = listOf(
                Texture(
                    sampler = 0,
                    source = 0
                )
            ),
            images = listOf(
                Image(
                    uri = javaClass.getResourceAsStream("/textures/CesiumLogoFlat.base64.txt").use { String(it.readBytes()).trim() }
                )
            ),
            samplers = listOf(
                Sampler(
                    magFilter = Filter.Linear,
                    minFilter = Filter.NearestMipmapLinear,
                    wrapS = Wrap.Repeat,
                    wrapT = Wrap.Repeat
                )
            ),
            buffers = listOf(
                Buffer(
                    byteLength = 140,
                    uri = "data:application/octet-stream;base64,AAABAAIAAQADAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgD8AAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAAAAAACAPwAAAAAAAIA/AAAAAAAAAAAAAIA/AAAAAAAAAAAAAIA/AACAPwAAgD8AAAAAAACAPwAAgD8AAAAAAAAAAAAAgD8="
                )
            )
        )
        assertEquals(expectedGltf, compiledGltf)
        writeToFile("TestCompileInterleaved.gltf", compiledGltf)
    }

    companion object {
        private const val writeToFiles = true

        @SuppressWarnings("ConstantConditions")
        fun writeToFile(fileName: String, gltf: Gltf) {
            if (writeToFiles) {
                File(fileName).writeText(gltf.toJson(true))
            }
        }
    }
}
