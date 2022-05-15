package com.pupptmstr.scooternav_s

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pupptmstr.scooternav_s.database.Neo4jSessionFactory
import com.pupptmstr.scooternav_s.models.Element
import com.pupptmstr.scooternav_s.models.PathQueryResult
import com.pupptmstr.scooternav_s.models.Response
import com.pupptmstr.scooternav_s.ogm.Node
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.math.*

fun main(args: Array<String>) = runBlocking {
    val client = HttpClient(CIO.create {
        requestTimeout = 0
    }) {
        expectSuccess = false
    }

    val factory = Neo4jSessionFactory()

    if (args.toList().contains("--prepare")) {
        val body = getOverpassApiData(client, getAllPedestrianStreets())
        fillDatabaseWithApiGeoData(body, factory)
    }

    var stillWorks = true

    while (stillWorks) {
        print("First node id:")
        val node1 = readln()
        print("Second node id:")
        val node2 = readln()
        val result = getShortestWay(node1.toLong(), node2.toLong(), factory)
        if (result != null) {
            println("Way from $node1 to $node2. Total length = ${result.totalLength}")
            println("Full path: ${result.path.map { it.id }}")
            println("There are ${result.path.lastIndex+1} nodes in path.")
        } else {
            println("Path not found!")
        }
        println("Need more? (no to stop)")
        val ans = readln()
        if (ans == "no")
            stillWorks = false
    }

}

suspend fun getOverpassApiData(httpClient: HttpClient, requestString: String): Array<Element> {
    println("Making POST request for data from OpenStreetMaps...")
    val response = httpClient.post("https://overpass-api.de/api/interpreter") {
        setBody(requestString)
    }
    println("Get all data, building objects from json response and writing response to file...")
    val builder = GsonBuilder()
    val gson = builder.setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    val bodyAsString = response.body<String>()
    writeResponseToFile("response-murino.json", bodyAsString)
    println("Wrote response to file.")
    val responseDataAsObject = gson.fromJson(bodyAsString, Response::class.java)
    println("Get all data as objects, starting enter data into the database...")
    return responseDataAsObject.elements
}

suspend fun writeResponseToFile(fileName: String, requestBody: String) {
    val file = File(fileName)

    if (!file.exists()) {
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
    }

    val writer = BufferedWriter(withContext(Dispatchers.IO) {
        FileWriter(file)
    })
    withContext(Dispatchers.IO) {
        writer.write(requestBody)
    }
}

fun fillDatabaseWithApiGeoData(data: Array<Element>, factory: Neo4jSessionFactory) {
    val session = factory.getNeo4jSession()
    if (session != null) {
        val nodes = mutableMapOf<String, Node>()
        var countAddedElements = 0L
        var isStillAddingNodes = true
        println("Started adding Nodes...")
        data.forEach {
            if (it.type == "node") { //сохранять ноды пачками, neo4j так воспринимает гораздо лучше и быстрее
                countAddedElements++
                val node = Node(it.id, it.lat, it.lon, it.tags?.amenity, it.tags?.highway)
                nodes[it.id.toString()] = node
            } else if (it.type == "way") {
                if (isStillAddingNodes) {
                    println("Added all Nodes. Totally was added $countAddedElements Nodes.")
                    println("Started saving nodes...")
                    session.save(nodes.values)
                    println("Saved all Nodes.")
                    countAddedElements = 0
                    println("Started adding Ways...")
                    isStillAddingNodes = false
                }
                if (it.nodes != null) {
                    countAddedElements++
                    for (i in it.nodes.indices) {
                        if (i + 1 <= it.nodes.lastIndex) {
                            val node1 = nodes[it.nodes[i].toString()]
                            val node2 = nodes[it.nodes[i + 1].toString()]
                            if (node1 != null && node2 != null) {
                                val length = getWayLength(node1, node2)
                                node1.waysTo(
                                    node2, it.id, 10.0, 5, it.tags?.highway, it.tags?.surface ?: "asphalt", length
                                )
                                node2.waysTo(
                                    node1, it.id, 10.0, 5, it.tags?.highway, it.tags?.surface ?: "asphalt", length
                                )
                            } else {
                                println("No node was previously, please rerun this")
                            }
                        }
                    }
                }
            }
        }
        println("Added all Ways. Totally was added $countAddedElements Ways.")
        println("Started saving Ways...")
        session.save(nodes.values, 5)
        println("Saved all Ways. Creating gds graph")
        session.query(createGDSGraph(), mutableMapOf<String, Objects>())
        println("Created gds graph. Now database is ready")
    } else {
        println("Database Session is null, can't work")
    }
}

fun getWayLength(nodeStart: Node, nodeEnd: Node): Double {
    val earthRadius = 6372795 //Среднее значение радиуса земли значение в метрах
    val lat1 = (nodeStart.lat * PI).div(180)
    val lat2 = (nodeEnd.lat * PI).div(180)
    val lon1 = (nodeStart.lon * PI).div(180)
    val lon2 = (nodeEnd.lon * PI).div(180)


    val cl1 = cos(lat1)
    val cl2 = cos(lat2)
    val sl1 = sin(lat1)
    val sl2 = sin(lat2)
    val delta = lon2 - lon1
    val cDelta = cos(delta)
    val sDelta = sin(delta)

    val y = sqrt((cl2 * sDelta).pow(2.0) + (cl1 * sl2 - sl1 * cl2 * cDelta).pow(2.0))
    val x = sl1 * sl2 + cl1 * cl2 * cDelta

    val ad = atan2(y, x)

    return ad * earthRadius
}

fun getShortestWay(id1: Long, id2: Long, factory: Neo4jSessionFactory): PathQueryResult? {
    val session = factory.getNeo4jSession()
    if (session != null) {
        val queryResult = session.query(getShortestWayCypher(id1, id2), mapOf<String, Objects>()).queryResults()
        val totalLength: Double
        val path: List<Node>
        val lengths: Array<Double>
        if (queryResult.iterator().hasNext()) {
            val next = queryResult.iterator().next()
            totalLength = next["totalCost"] as Double
            path = next["path"] as List<Node>
            lengths = next["costs"] as Array<Double>
        } else {
            return null
        }
        return PathQueryResult(path, lengths, totalLength)
    } else {
        println("Database Session is null, can't work")
        return null
    }
    //334409
    //9724356897

    //4317659966
    //7346426129
}

