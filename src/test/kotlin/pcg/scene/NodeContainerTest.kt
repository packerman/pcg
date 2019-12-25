package pcg.scene

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NodeContainerTest {

    @Test
    internal fun shouldCollectAllNodes() {
        val s = scene {
            node {
                node {
                    node {}
                }
            }
            node {}
            node {
                node {}
            }
        }

        val nodes: List<Node> = s.collectNodes()

        Assertions.assertEquals(6, nodes.size)
    }
}
