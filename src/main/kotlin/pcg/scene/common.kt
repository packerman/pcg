package pcg.scene

import pcg.util.align

interface Builder<out T> {
    fun build(): T
}

interface ByteSized {
    val byteSize: Int
}

val ByteSized.alignedByteSize: Int
    get() = align(byteSize, 4)

val Iterable<ByteSized>.byteSize: Int
    get() = this.sumBy(ByteSized::byteSize)
