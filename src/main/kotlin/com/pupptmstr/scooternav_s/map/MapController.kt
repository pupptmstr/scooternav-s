package com.pupptmstr.scooternav_s.map

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pupptmstr.scooternav_s.*
import com.pupptmstr.scooternav_s.map.models.api.*
import com.pupptmstr.scooternav_s.map.models.osm.Element
import com.pupptmstr.scooternav_s.map.models.osm.Response
import com.pupptmstr.scooternav_s.ogm.Node
import com.pupptmstr.scooternav_s.ogm.Way
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.neo4j.ogm.session.Session
import java.util.*
import kotlin.math.*


class MapController() {

    val nodes = mutableMapOf<String, Node>()
    private val GOOD_SURFACE = 1.9
    private val ALMOST_GOOD_SURFACE = 1.4
    private val MIDDLE_GOOD_SURFACE = 1.0
    private val BAD_SURFACE = 0.5

    private suspend fun getOverpassApiData(httpClient: HttpClient, requestString: String): Array<Element> {
        println("Making POST request for data from OpenStreetMaps...")
        val response = httpClient.post("https://overpass-api.de/api/interpreter") {
            setBody(requestString)
        }
        println("Get all data, building objects from json response and writing response to file...")
        val builder = GsonBuilder()
        val gson =
            builder.setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        val bodyAsString = response.body<String>()
        println("Wrote response to file.")
        val responseDataAsObject = gson.fromJson(bodyAsString, Response::class.java)
        println("Get all data as objects, starting enter data into the database...")
        return responseDataAsObject.elements
    }

    private fun prepareDataToDatabaseFormat(data: Array<Element>, factory: Neo4jSessionFactory, needToWrite: Boolean) {
        val session = factory.getNeo4jSession()
        if (session != null) {
            val loadedNodes = session.loadAll(Node::class.java).map { it.id.toString() to it }
            nodes.putAll(loadedNodes)
            var countAddedElements = 0L
            var isStillAddingNodes = true
            println("Started adding Nodes...")
            data.forEach {
                if (it.type == "node") {
                    countAddedElements++
                    val node = Node(it.id, it.lat, it.lon, it.tags?.amenity, it.tags?.highway)
                    nodes.putIfAbsent(node.id.toString(), node)
                } else if (it.type == "way") {
                    if (isStillAddingNodes) {
                        println("Added all Nodes. Totally was added $countAddedElements Nodes.")
                        if (needToWrite) {
                            saveNodesAndValues(session)
                        }
                        countAddedElements = 0
                        println("Started adding Ways...")
                        isStillAddingNodes = false
                    }
                    if (it.nodes != null) {
                        countAddedElements++
                        var bonus: Double = when (it.tags?.highway) {
                            "path" -> 0.5
                            "cycleway" -> 1.5
                            else -> 1.0
                        }

                        if (it.tags?.surface != null) {
                            bonus *= when (it.tags.surface) {
                                "asphalt", "chipseal", "tartan" -> {
                                    GOOD_SURFACE
                                }

                                "concrete", "concrete:lanes", "concrete:plates", "paved" -> {
                                    ALMOST_GOOD_SURFACE
                                }

                                "paving_stones", "sett" -> {
                                    MIDDLE_GOOD_SURFACE
                                }

                                else -> {
                                    BAD_SURFACE
                                }
                            }
                        }
                        val avarageSpeed = 10.0
                        val bandwidth = 5
                        val preference = 100.0 / (avarageSpeed * bonus)

                        for (i in it.nodes.indices) {
                            if (i + 1 <= it.nodes.lastIndex) {
                                val node1 = nodes[it.nodes[i].toString()]
                                val node2 = nodes[it.nodes[i + 1].toString()]
                                if (node1 != null && node2 != null) {
                                    val wayId1 = "${it.id} + ${node1.id} + ${node2.id}"
                                    val wayId2 = "${it.id} + ${node2.id} + ${node1.id}"

                                    val length = getWayLength(node1, node2)

                                    if (!node1.ways.contains(Way(node1, node2, wayId1, 0.0, 0, "", 0.0, 0.0, 0.0))) {
                                        node1.waysTo(
                                            node2,
                                            it.id,
                                            avarageSpeed,
                                            bandwidth,
                                            it.tags?.highway,
                                            bonus,
                                            length,
                                            preference
                                        )
                                    }

                                    if (!node2.ways.contains(Way(node2, node1, wayId2, 0.0, 0, "", 0.0, 0.0, 0.0))) {
                                        node2.waysTo(
                                            node1,
                                            it.id,
                                            avarageSpeed,
                                            bandwidth,
                                            it.tags?.highway,
                                            bonus,
                                            length,
                                            preference
                                        )
                                    }
                                } else {
                                    println("No node was previously, please rerun this")
                                }
                            }
                        }
                    }
                }
            }
            println("Added all Ways. Totally was added $countAddedElements Ways.")

            if (needToWrite) {
                println("Started saving Ways...")
                saveNodesAndValues(session)
                println("Saved all Ways. Creating gds graph")
                session.query(createGDSGraph(), mutableMapOf<String, Objects>())
                println("Created gds graph. Now database is ready")
            }

        } else {
            println("Database Session is null, can't work")
        }
    }

    fun saveNodesAndValues(session: Session, depth: Int = 3) {
        session.save(nodes.values, depth)
    }

    private fun getWayLength(nodeStart: Node, nodeEnd: Node): Double {
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

    suspend fun prepareDatabase(client: HttpClient, factory: Neo4jSessionFactory, needToWrite: Boolean) {
        val body = getOverpassApiData(client, getAllPedestrianStreets())
        prepareDataToDatabaseFormat(body, factory, needToWrite)
    }

    private fun getNearestNodeId(node: Coordinates, factory: Neo4jSessionFactory): Long =
        getNearestNode(node, factory).id


    fun getNearestNode(node: Coordinates, factory: Neo4jSessionFactory): Node {
        return nodes.values.sortedBy { getWayLength(it, Node(0, node.lat, node.lon, "", "")) }[0]
    }

    fun getPath(
        nodeFrom: Coordinates, nodeTo: Coordinates, factory: Neo4jSessionFactory
    ): PathResponse? {
        val session = factory.getNeo4jSession()
        val id1 = getNearestNodeId(nodeFrom, factory)
        val id2 = getNearestNodeId(nodeTo, factory)
        if (session != null) {
            val queryResult = session.query(getShortestWayCypher(id1, id2), mapOf<String, Objects>()).queryResults()
            val path: List<Node>
            val res: PathResponse
            if (queryResult.iterator().hasNext()) {
                val next = queryResult.iterator().next()
                path = next["path"] as List<Node>
                res = getFullPath(path)
            } else {
                val waypoints = mutableListOf<Waypoint>()
                waypoints.add(Waypoint("nodeFrom", 0, "", listOf(nodeFrom.lon, nodeFrom.lat)))
                waypoints.add(Waypoint("nodeTo", 0, "", listOf(nodeTo.lon, nodeTo.lat)))
                return PathResponse("can't find path", emptyList(), waypoints)
            }
            return res
        } else {
            println("Database Session is null, can't work")
            return null
        }
        //334409
        //9724356897

        //4317659966
        //7346426129
    }

    private fun getFullPath(path: List<Node>): PathResponse {
        val waypoints = mutableListOf<Waypoint>()
        waypoints.add(Waypoint(path.first().id.toString(), 0, "", listOf(path.first().lon, path.first().lat)))
        var length = 0.0
        var duration = 0.0
        val geometry = encodePolyline(path)
        val geometryParts = StringBuilder()
        val steps = mutableListOf<Step>()
        val geometryIterator = geometry.iterator()
        for (i in path.indices) {
            if (i < path.indices.last) {
                val firstPoint = path[i]
                val secondPoint = path[i + 1]
                val geometryStep1 = geometryIterator.next()
                val geometryStep2 = geometryIterator.next()
                val stepGeometry: String = geometryStep1 + geometryStep2
                val maneuver = Maneuver(
                    0,
                    0,
                    listOf(secondPoint.lon, secondPoint.lat),
                    if (i == path.indices.first) "depart" else if (i + 1 == path.indices.last) "arrive" else "continue"
                )
                val mode = "scooter"
                val drivingSide = "right"
                val name = ""
                val intersections = emptyList<Intersection>()
                val ways = nodes[firstPoint.id.toString()]!!.ways
                val way = ways.find {
                    it.id.contains(firstPoint.id.toString()) && it.id.contains(secondPoint.id.toString())
                }
                val weight = 0.0
                val stepDuration = way!!.length / (way.averageSpeed * 1000.0 / 60.0)
                val distance = way.length
                steps.add(
                    Step(
                        stepGeometry, maneuver, mode, drivingSide, name, intersections, weight, stepDuration, distance
                    )
                )
                length += distance
                duration += stepDuration
                geometryParts.append(stepGeometry)
            }
        }
        waypoints.add(Waypoint(path.last().id.toString(), length.toInt(), "", listOf(path.last().lon, path.last().lat)))
        val leg = Leg(steps, "", 0.0, duration, length)
        val stringBuilder = StringBuilder()
        geometry.forEach {
            stringBuilder.append(it)
        }
        val geometryFinal = stringBuilder.toString()
        return PathResponse("Ok", listOf(Path(geometryFinal, listOf(leg), "", 0.0, duration, length)), waypoints)
    }

    fun getNodesInRadius(latMax: Double, latMin: Double, lonMax: Double, lonMin: Double): List<Node> {
        val nodes = nodes.values
        val resList = nodes.filter { it.lat < latMax && it.lat > latMin && it.lon < lonMax && it.lon > lonMin }
        return resList.map { Node(it.id, it.lat, it.lon, it.amenity, it.highway) }
    }

}

