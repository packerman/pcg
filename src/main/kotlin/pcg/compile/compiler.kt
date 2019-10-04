package pcg.compile

import pcg.gltf.Gltf
import pcg.scene.Scene
import pcg.gltf.Scene as GltfScene

fun compile(scene: Scene): Gltf =
    Gltf(
        scene = 0,
        scenes = listOf(
            GltfScene(
                scene.nodes.mapIndexed() { n, _ -> n }
            )
        ))
