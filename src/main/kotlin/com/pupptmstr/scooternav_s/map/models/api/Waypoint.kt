package com.pupptmstr.scooternav_s.map.models.api

data class Waypoint(
    val hint: String,
    val distance: Int,
    val name: String,
    val location: List<Double>
)
