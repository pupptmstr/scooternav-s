package com.pupptmstr.scooternav_s.map

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pupptmstr.scooternav_s.client_manager.ClientsManager
import com.pupptmstr.scooternav_s.client_manager.DataWebSocketMessage
import com.pupptmstr.scooternav_s.map.models.api.Coordinates
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Route.mapApi(
    controller: MapController,
    databaseFactory: Neo4jSessionFactory,
    client: HttpClient,
    clientsManager: ClientsManager
) {
    val gson = GsonBuilder().setPrettyPrinting().serializeNulls()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

    route("/map") {

        route("/path") {
            get("/{coords}") {
                val parameters = call.parameters["coords"]
                try {
                    val coords = parameters?.split(";")
                    val cords1 = coords?.get(0)?.split(",")
                    val cords2 = coords?.get(1)?.split(",")
                    val lon1 = cords1!![0]
                    val lat1 = cords1[1]
                    val lon2 = cords2!![0]
                    val lat2 = cords2[1]
                    val nodeFrom = Coordinates(lat1.toDouble(), lon1.toDouble())
                    val nodeTo = Coordinates(lat2.toDouble(), lon2.toDouble())
                    val res = controller.getPath(nodeFrom, nodeTo, databaseFactory)
                    if (res != null) {
                        call.respond(HttpStatusCode.OK, res)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        get("/prepare") {
            controller.prepareDatabase(client, databaseFactory, true)
            call.respond(HttpStatusCode.OK)
        }

        post("/nearest") {
            val coords = call.receive<Coordinates>()
            val res = controller.getNearestNode(coords, databaseFactory)
            call.respond(Coordinates(res.lat, res.lon))
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
                        try {
                            CoroutineScope(Dispatchers.IO).launch {
                                clientsManager.writeClientData(
                                    gson.fromJson(message, DataWebSocketMessage::class.java),
                                    controller,
                                    databaseFactory
                                )
                            }
                        } catch (e: Exception) {
                            println("login new client")
                            clientsManager.loginNewClientSession(id, this)
                        }

                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                clientsManager.endClientSession(id)
                println("finishing client id")
            }
        }

    }
}
