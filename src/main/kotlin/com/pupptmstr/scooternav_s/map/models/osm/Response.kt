package com.pupptmstr.scooternav_s.map.models.osm

data class Response(
    val version: Double,
    val generator: String,
    val osm3s: Osm3S,
    val elements: Array<Element>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Response

        if (version != other.version) return false
        if (generator != other.generator) return false
        if (osm3s != other.osm3s) return false
        if (!elements.contentEquals(other.elements)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + generator.hashCode()
        result = 31 * result + osm3s.hashCode()
        result = 31 * result + elements.contentHashCode()
        return result
    }
}
