package com.pupptmstr.scooternav_s.map.models.api

data class Intersection(
    val out: Int,
    val entry: List<Boolean>,
    val bearings: List<Int>,
    val location: List<Double>
)
