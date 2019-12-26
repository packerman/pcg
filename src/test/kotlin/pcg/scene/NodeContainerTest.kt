package pcg.scene

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NodeContainerTest {

    @Test
    internal fun shouldCollectAllNodes() {
        val s = scene {
            node {
                name = "1"
                node {
                    name = "2"
                }
                node {
                    name = "3"
                    node {
                        name = "4"
                    }
                }
            }
            node {
                name = "5"
            }
            node {
                name = "6"
                node {
                    name = "7"
                }
            }
        }

        val nodes: List<Node> = s.collectNodes()

        assertEquals(7, nodes.size)
        assertEquals(setOf("1", "2", "3", "4", "5", "6", "7"), nodes.map(Node::name).toSet())
    }
}
