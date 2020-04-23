package pcg.scene

import pcg.scene.Float2VertexArray.Companion.Float2VertexArrayBuilder
import pcg.scene.Float3VertexArray.Companion.Float3VertexArrayBuilder
import pcg.scene.Geometry.Companion.GeometryBuilder
import pcg.scene.Mesh.Companion.MeshBuilder
import pcg.scene.Mesh.Companion.Primitive
import pcg.scene.Mesh.Companion.Primitive.Triangles
import pcg.scene.ShortIndexArray.Companion.ShortIndexArrayBuilder
import pcg.util.allTheSame
import pcg.validate.requireNotEmpty

fun geometry(block: GeometryBuilder.() -> Unit): Geometry =
    GeometryBuilder().apply(block).build()

fun oneMeshGeometry(
    primitive: Primitive = Triangles,
    block: MeshBuilder.() -> Unit
): Geometry {
    return geometry {
        mesh(primitive, block)
    }
}

class Geometry(val meshes: List<Mesh>) : ByteSized {
    override val byteSize: Int = meshes.sumBy(Mesh::byteSize)

    init {
        requireNotEmpty(meshes, "meshes")
    }

    companion object {
        class GeometryBuilder : Builder<Geometry> {

            private val meshes = mutableListOf<Mesh>()

            fun mesh(
                primitive: Primitive = Triangles,
                block: MeshBuilder.() -> Unit
            ) {
                meshes.add(MeshBuilder(primitive).apply(block).build())
            }

            override fun build(): Geometry {
                return Geometry(meshes)
            }
        }
    }
}

class Mesh(
    val primitive: Primitive = Triangles,
    val vertexArrays: List<VertexArray<*>>,
    val indexArrays: List<IndexArray<*>>
) : ByteSized {
    override val byteSize: Int =
        vertexArrays.sumBy(VertexArray<*>::byteSize) + indexArrays.sumBy(IndexArray<*>::byteSize)

    init {
        requireNotEmpty(vertexArrays, "vertexArrays")
        require(allTheSame(vertexArrays.map(VertexArray<*>::count))) { "All Vertex Arrays need to have the same count" }
    }

    companion object {
        enum class Primitive {
            Points,
            Lines,
            LineStrip,
            Triangles,
            TriangleStrip,
            Quads
        }

        class MeshBuilder(private val primitive: Primitive) : Builder<Mesh> {

            private val vertexArrays = mutableListOf<VertexArray<*>>()
            private val indexArrays = mutableListOf<IndexArray<*>>()

            fun vertexArray3f(
                attribute: Attribute,
                block: Float3VertexArrayBuilder.() -> Unit
            ): Float3VertexArray {
                val array = Float3VertexArrayBuilder(attribute).apply(block).build()
                vertexArrays.add(array)
                return array
            }

            fun vertexArray2f(
                attribute: Attribute,
                block: Float2VertexArrayBuilder.() -> Unit
            ) {
                vertexArrays.add(Float2VertexArrayBuilder(attribute).apply(block).build())
            }

            fun indexArray(material: Int = 0, block: ShortIndexArrayBuilder.() -> Unit) {
                indexArrays.add(ShortIndexArrayBuilder(material).apply(block).build())
            }

            override fun build(): Mesh {
                return Mesh(primitive, vertexArrays, indexArrays)
            }
        }
    }
}

