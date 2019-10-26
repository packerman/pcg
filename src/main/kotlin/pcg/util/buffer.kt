package pcg.util

import java.nio.ByteBuffer

fun ByteBuffer.fillBytes(count: Int, byte: Byte = 0) = repeat(count) { put(byte) }
