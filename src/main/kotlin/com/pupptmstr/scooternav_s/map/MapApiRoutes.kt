package com.pupptmstr.scooternav_s.map

import com.pupptmstr.scooternav_s.map.models.api.PathRequest
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mapApi(controller: MapController, databaseFactory: Neo4jSessionFactory, client: HttpClient) {
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
}
