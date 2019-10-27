package pcg.util

import java.nio.ByteBuffer

fun ByteBuffer.fillBytes(count: Int, byte: Byte = 0) = repeat(count) { put(byte) }

fun align(n: Int, b: Int): Int = if (n % b == 0) n else n + b - n % b

fun remaining(n: Int, b: Int): Int {
    val r = n % b
    return if (r == 0) 0 else b - r
}
