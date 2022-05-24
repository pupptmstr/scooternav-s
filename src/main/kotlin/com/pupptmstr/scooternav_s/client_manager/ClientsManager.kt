package com.pupptmstr.scooternav_s.client_manager

import com.pupptmstr.scooternav_s.map.MapController
import com.pupptmstr.scooternav_s.map.Neo4jSessionFactory
import io.ktor.server.websocket.*
import java.util.*

class ClientsManager {

    private val clients: MutableMap<String, WebSocketServerSession> = Collections.synchronizedMap(mutableMapOf())

    suspend fun getPath(
        nodeIdFrom: Long,
        nodeIdTo: Long,
        mapController: MapController,
        databaseFactory: Neo4jSessionFactory
    ) {
        val res = mapController.getShortestWay(nodeIdFrom, nodeIdTo, databaseFactory)
        if (res != null) {
            println(res)
        } else {
            println("No path between this nodes.")
        }
    }

    suspend fun writeClientData(averageSpeed: Double, wayId: Long, clientId: String) {
        println("Client = ${clientId}; wayId = ${wayId}; average speed = ${averageSpeed};")
    }

    suspend fun loginNewClientSession(id: String, socketSession: WebSocketServerSession) {
        clients[id] = socketSession
    }

    suspend fun endClientSession(id: String) {
        clients.remove(id)
    }

    suspend fun getUniqueID(): String {
        var end = false
        var res = ""
        while (end) {
            val id = UUID.randomUUID().toString()
            if (!clients.containsKey(id)) {
                end = true
                res = id
            }
        }
        return res
    }


}
