package com.pupptmstr.scooternav_s.client_manager

data class RequestPathWebsocketMessage(
    val nodeIdFrom: Long,
    val nodeIdTo: Long
) : WebsocketMessage
