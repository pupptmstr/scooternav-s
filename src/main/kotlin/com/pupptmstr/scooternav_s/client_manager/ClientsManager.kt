package com.pupptmstr.scooternav_s.client_manager

import com.pupptmstr.scooternav_s.map.MapController
import com.pupptmstr.scooternav_s.map.Neo4jSessionFactory
import com.pupptmstr.scooternav_s.map.models.api.Coordinates
import com.pupptmstr.scooternav_s.ogm.Node
import io.ktor.server.websocket.*
import java.util.*

class ClientsManager {

    private val clients: MutableMap<String, WebSocketServerSession> = Collections.synchronizedMap(mutableMapOf())

    suspend fun writeClientData(
        clientData: DataWebSocketMessage,
        mapController: MapController,
        factory: Neo4jSessionFactory,
        isLast: Boolean = false
    ) {
        val node1 = mapController.getNearestNode(clientData.node1, factory)
        val nodeFrom: Node? = mapController.nodes["${node1.id}"]
        val ways = nodeFrom?.ways?.filter { Coordinates(it.nodeEnd.lat, it.nodeEnd.lon) == clientData.node2 }
        if (ways != null && ways.isNotEmpty()) {
            val way = ways[0]
            val waysIterator = mapController.nodes["${node1.id}"]!!.ways.iterator()
            while (waysIterator.hasNext()) {
                val currentWay = waysIterator.next()
                if (
                    (currentWay.nodeStart == way.nodeStart && currentWay.nodeEnd == way.nodeEnd)
                    || (currentWay.nodeStart == way.nodeEnd && currentWay.nodeEnd == way.nodeStart)
                ) {
                    currentWay.averageSpeed = (currentWay.averageSpeed * 49 + clientData.speed) / 50
                    currentWay.preference = 100.0 / (currentWay.averageSpeed * currentWay.surfaceMultiplier)
                    mapController.saveNodesAndValues(factory.getNeo4jSession()!!, 2)
                    if (!isLast) {
                        writeClientData(
                            DataWebSocketMessage(clientData.speed, clientData.node2, clientData.node1),
                            mapController,
                            factory,
                            true
                        )
                    }
                }
            }
        }

    }

    fun loginNewClientSession(id: String, socketSession: WebSocketServerSession) {
        clients[id] = socketSession
    }

    fun endClientSession(id: String) {
        clients.remove(id)
    }

    fun getUniqueID(): String {
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
