package com.pupptmstr.scooternav_s.map.models.api



data class Step(
    val geometry: String,
    val maneuver: Maneuver,
    val mode: String,
    val drivingSide: String,
    val name: String,
    val intersections: List<Intersection>,
    val weight: Double,
    val duration: Double,
    val distance: Double
)
