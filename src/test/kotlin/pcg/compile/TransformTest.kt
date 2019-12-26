package pcg.compile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pcg.gltf.Node
import pcg.scene.*
import pcg.scene.GeometryNode.Companion.GeometryNodeBuilder

internal class TransformTest {

    @Test
    internal fun shouldCompileTranslation() {
        val s = setUpSceneWithNode {
            translate(1f, 0f, 0f)
            translate(0.5f, 2f, 0f)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0,
            translation = floatArrayOf(1.5f, 2f, 0f)
        )
        assertEquals(expectedNode, actualNode)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }

    @Test
    internal fun shouldSkipDefaultTranslation() {
        val s = setUpSceneWithNode {
            translate(1f, -2f, 0f)
            translate(-1f, 2f, 0f)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0
        )
        assertEquals(expectedNode, actualNode)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }

    @Test
    internal fun shouldCompileRotation() {
        val s = setUpSceneWithNode {
            rotate(90f, Axis.Y)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0,
            rotation = floatArrayOf(0f, 0.70710677f, 0f, 0.70710677f)
        )
        assertEquals(expectedNode, actualNode)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }

    @Test
    internal fun shouldCompileManyRotations() {
        val s = setUpSceneWithNode {
            rotate(90f, Axis.Y)
            rotate(45f, Axis.Z)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0,
            rotation = floatArrayOf(0.27059805f, 0.6532815f, 0.27059805f, 0.6532815f),
            scale = floatArrayOf(0.99999994f, 0.99999994f, 1.0f)
        )
        assertEquals(expectedNode, actualNode)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }

    @Test
    internal fun shouldCompileTranslationAndRotation() {
        val s = setUpSceneWithNode {
            translate(1f, 0f, 0f)
            rotate(90f, Axis.Y)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0,
            translation = floatArrayOf(1f, 0f, 0f),
            rotation = floatArrayOf(0f, 0.70710677f, 0f, 0.70710677f)
        )
        assertEquals(expectedNode, actualNode)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }

    @Test
    internal fun shouldCompileRotationAndTranslation() {
        val s = setUpSceneWithNode {
            rotate(90f, Axis.Y)
            translate(1f, 0f, 0f)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0,
            matrix = floatArrayOf(
                -0.0f,
                0.0f,
                -1.0f,
                0.0f,
                0.0f,
                1.0f,
                0.0f,
                0.0f,
                1.0f,
                0.0f,
                -0.0f,
                0.0f,
                0.0f,
                0.0f,
                -1.0f,
                1.0f
            )
        )
        assertEquals(expectedNode, actualNode)

        SceneKtTest.writeToFile("TestCompileGraphScene.gltf", gltf)
    }

    @Test
    internal fun shouldCollectTRSTransform() {
        val s = setUpSceneWithNode {
            translate(2f, 0f, 0f)
            translate(0f, 1f, 0f)
            rotate(90f, Axis.Y)
            rotate(45f, Axis.Z)
            scale(3f, 2f, 1f)
            scale(1f, 2f, 3f)
        }

        val gltf = s.compile()
        val actualNode = requireNotNull(gltf.nodes?.get(0))

        val expectedNode = Node(
            mesh = 0,
            scale = floatArrayOf(2.9999998f, 3.9999998f, 3.0f),
            rotation = floatArrayOf(0.27059805f, 0.6532815f, 0.27059805f, 0.6532815f),
            translation = floatArrayOf(2.0f, 1.0f, 0.0f)
        )

        assertEquals(expectedNode, actualNode)
    }

    @Test
    internal fun shouldRecognizeTRSTransform() {
        val t1 = Translation(0.5f, 0.5f, 0.5f)
        val t2 = Translation(1f, 0f, 0f)
        val r1 = Rotation(90f, Axis.Y)
        val r2 = Rotation(45f, Axis.Z)
        val s1 = Scale(2f, 2f, 2f)
        assertTrue(listOf(t1, t2).isTRSTransform())
        assertTrue(listOf(t1, r1).isTRSTransform())
        assertFalse(listOf(r1, t1).isTRSTransform())
        assertFalse(listOf(t1, r1, t2).isTRSTransform())
        assertTrue(listOf(t1, r1, r2).isTRSTransform())
        assertTrue(listOf(t1, r1, s1).isTRSTransform())
        assertFalse(listOf(t1, s1, r1).isTRSTransform())
        assertFalse(listOf(s1, t1, r1).isTRSTransform())
    }

    private fun setUpSceneWithNode(block: GeometryNodeBuilder.() -> Unit): Scene {
        val g = oneMeshGeometry {
            vertexArray3f(attribute = Attribute.Position) {
                vertex(0f, 0f, 0f)
                vertex(0.5f, 0f, 0f)
                vertex(0f, 0.5f, 0f)
            }
        }

        val s = scene {
            node(g, block)
        }
        return s
    }
}
