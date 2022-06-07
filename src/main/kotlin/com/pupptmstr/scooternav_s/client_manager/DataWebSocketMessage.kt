package com.pupptmstr.scooternav_s.client_manager

import com.pupptmstr.scooternav_s.map.models.api.Coordinates

data class DataWebSocketMessage(
    val speed: Double,
    val node1: Coordinates,
    val node2: Coordinates
)
