package com.identic.fluentforge.dataReader.remote.epub.models

data class EpubImage(val absPath: String, val image: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EpubImage

        if (absPath != other.absPath) return false
        return image.contentEquals(other.image)
    }

    override fun hashCode(): Int {
        var result = absPath.hashCode()
        result = 31 * result + image.contentHashCode()
        return result
    }
}