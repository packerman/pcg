package pcg.util

fun <T> nullIfDefault(value: T?, default: T): T? = if (value == default) null else value

fun nullIfDefault(value: FloatArray?, default: FloatArray): FloatArray? =
    if (value == null || value.contentEquals(default)) null else value
