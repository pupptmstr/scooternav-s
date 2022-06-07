package com.pupptmstr.scooternav_s.map.models.api

data class Leg(
    val steps: List<Step>,
    val summary: String,
    val weight: Double,
    val duration: Double,
    val distance: Double
)
