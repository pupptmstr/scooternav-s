package com.pupptmstr.scooternav_s.map

import com.google.gson.GsonBuilder
import com.pupptmstr.scooternav_s.client_manager.ClientsManager
import com.pupptmstr.scooternav_s.client_manager.DataWebsocketMessage
import com.pupptmstr.scooternav_s.client_manager.RequestPathWebsocketMessage
import com.pupptmstr.scooternav_s.client_manager.WebsocketMessage
import com.pupptmstr.scooternav_s.map.models.api.PathRequest
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.mapApi(
    controller: MapController,
    databaseFactory: Neo4jSessionFactory,
    client: HttpClient,
    clientsManager: ClientsManager
) {
    route("/map") {

        post("/path") {
            val pathRequest = call.receive<PathRequest>()
            val res = controller.getShortestWay(pathRequest.nodeFrom, pathRequest.nodeTo, databaseFactory)
            if (res != null) {
                call.respond(HttpStatusCode.OK, res)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/prepare") {
            controller.prepareDatabase(client, databaseFactory)
            call.respond(HttpStatusCode.OK)
        }

    }

    route("/client") {

        webSocket {
            this.pingInterval = null
            val id = clientsManager.getUniqueID()
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val message = frame.readText()
                        var clientMessage: WebsocketMessage? = null
                        try {
                            val gson = GsonBuilder().create()
                            val dataMessage = gson.fromJson(message, DataWebsocketMessage::class.java)
                            clientMessage = dataMessage
                        } catch (e: Exception) {
                            try {
                                val gson = GsonBuilder().create()
                                val dataMessage = gson.fromJson(message, RequestPathWebsocketMessage::class.java)
                                clientMessage = dataMessage
                            } catch (e: Exception) {
                                clientsManager.loginNewClientSession(id, this)
                            }
                        }

                        when (clientMessage) {
                            is DataWebsocketMessage -> {
                                clientsManager.writeClientData(clientMessage.averageSpeed, clientMessage.wayId, id)
                            }
                            is RequestPathWebsocketMessage -> {
                                clientsManager.getPath(
                                    clientMessage.nodeIdFrom,
                                    clientMessage.nodeIdTo,
                                    controller,
                                    databaseFactory
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                clientsManager.endClientSession(id)
            }
        }

    }
}
