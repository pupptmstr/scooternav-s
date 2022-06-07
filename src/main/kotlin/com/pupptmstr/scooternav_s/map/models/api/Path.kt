package com.pupptmstr.scooternav_s.map.models.api

data class Path(
    val geometry: String,
    val legs: List<Leg>,
    val weightName: String,
    val weight: Double,
    val duration: Double,
    val distance: Double
)
