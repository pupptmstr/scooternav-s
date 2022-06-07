package com.pupptmstr.scooternav_s.map.models.api

data class PathResponse(
    val code: String,
    val routes: List<Path>,
    val waypoints: List<Waypoint>
)
