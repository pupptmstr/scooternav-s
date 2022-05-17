package com.pupptmstr.scooternav_s

import com.google.gson.FieldNamingPolicy
import com.pupptmstr.scooternav_s.map.MapController
import com.pupptmstr.scooternav_s.map.Neo4jSessionFactory
import com.pupptmstr.scooternav_s.map.mapApi
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.configure() {

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            serializeNulls()
        }
    }


    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    val databaseFactory = Neo4jSessionFactory()
    val mapController = MapController()
    val httpClient = HttpClient(CIO.create {
        requestTimeout = 0
    }) {
        expectSuccess = false
    }


    routing {
        route("/api/v1") {
            route("") {
                mapApi(mapController, databaseFactory, httpClient)
            }
        }
    }


}
