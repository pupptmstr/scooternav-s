package com.pupptmstr.scooternav_s.client_manager

data class DataWebsocketMessage(
    val averageSpeed: Double,
    val wayId: Long
): WebsocketMessage
