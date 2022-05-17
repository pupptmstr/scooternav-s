package com.pupptmstr.scooternav_s.map.models.osm

data class Element(
    val type: String,
    val id: Long,
    val lat: Double,
    val lon: Double,
    val nodes: List<Long>?,
    val tags: Tags?
) {
}
