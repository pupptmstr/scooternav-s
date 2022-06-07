package com.pupptmstr.scooternav_s.map.models.api

data class Maneuver(
    val bearingAfter: Int,
    val bearingBefore: Int,
    val location: List<Double>,
    val type: String

)
