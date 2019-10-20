package pcg.compile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pcg.gltf.*
import pcg.gltf.Accessor.Companion.ComponentType
import pcg.gltf.Accessor.Companion.Type
import pcg.gltf.BufferView.Companion.Target
import pcg.gltf.Primitive.Companion.Attribute
import pcg.scene.Color
import pcg.scene.Material
import pcg.scene.Mesh.Companion.Attribute.Normal
import pcg.scene.Mesh.Companion.Attribute.Position
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
            node(geometry = g, material = m)
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
                            )
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteLength = 36,
                    target = Target.ARRAY_BUFFER
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
            node(geometry = g, material = m)
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
                            indices = 0
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 6,
                    target = Target.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 8,
                    byteLength = 36,
                    target = Target.ARRAY_BUFFER
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
            node(geometry = g, material = m)
            node(geometry = g, material = m) {
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
                            indices = 0
                        )
                    )
                )
            ),
            bufferViews = listOf(
                BufferView(
                    buffer = 0,
                    byteOffset = 0,
                    byteLength = 6,
                    target = Target.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 8,
                    byteLength = 72,
                    byteStride = 12,
                    target = Target.ARRAY_BUFFER
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
    internal fun shouldCompileManyGeometries() {
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
            node(triangle, triangleMaterial)
            node(square, squareMaterial) {
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
                        metallicFactor = 0f,
                        roughnessFactor = 1f
                    )
                ),
                GltfMaterial(
                    pbrMetallicRoughness = PbrMetallicRoughness(
                        baseColorFactor = floatArrayOf(0f, 0.8f, 0f, 1f),
                        metallicFactor = 0f,
                        roughnessFactor = 1f
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
                    target = Target.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 0,
                    byteOffset = 8,
                    byteLength = 36,
                    target = Target.ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 1,
                    byteOffset = 0,
                    byteLength = 12,
                    target = Target.ELEMENT_ARRAY_BUFFER
                ),
                BufferView(
                    buffer = 1,
                    byteOffset = 12,
                    byteLength = 48,
                    target = Target.ARRAY_BUFFER
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
                ),
                Accessor(
                    bufferView = 2,
                    byteOffset = 0,
                    componentType = ComponentType.UNSIGNED_SHORT,
                    count = 6,
                    type = Type.SCALAR,
                    max = listOf(3.toShort()),
                    min = listOf(0.toShort())
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
