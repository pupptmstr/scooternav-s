package com.pupptmstr.scooternav_s

import com.pupptmstr.scooternav_s.ogm.Node

fun getAllPedestrianStreets() =
    "[out:json];area[name=\"Мурино\"]->.small;area[name=\"Ленинградская область\"]->.big;(way(area.big)(area.small)  ['highway'~'path|steps|living_street|footway|corridor|cycleway|residential|living_street|service|pedestrian']['foot'!~'no']['access' !~ 'private']['access' !~ 'no']; ); (._;>;);out geom; out tags; out meta;"

fun createGDSGraph() = """
    CALL gds.graph.project(
        'murino',
        'Node',
        'WAY',
        {
            nodeProperties: ['lat', 'lon'],
            relationshipProperties: 'preference'
        }
    )
""".trimIndent()

fun getShortestWayCypher(id1: Long, id2: Long) = """
    MATCH (source:Node {id: $id1}), (target:Node {id: $id2})
    CALL gds.shortestPath.astar.stream('murino', {
        sourceNode: source,
        targetNode: target,
        latitudeProperty: 'lat',
        longitudeProperty: 'lon',
        relationshipWeightProperty: 'preference'
    })
    YIELD index, sourceNode, targetNode, totalCost, nodeIds, costs, path
    RETURN
        index,
        gds.util.asNode(sourceNode).id AS sourceNodeName,
        gds.util.asNode(targetNode).id AS targetNodeName,
        totalCost,
        [nodeId IN nodeIds | gds.util.asNode(nodeId).id] AS nodeNames,
        costs,
        nodes(path) as path
    ORDER BY index
""".trimIndent()

fun getClosestNodeCypher(lat: Double, lon: Double) = """
    MATCH (n: Node) 
    return n
    ORDER BY abs(n.lat - $lat), abs(n.lon - $lon) DESC LIMIT 1
""".trimIndent()


fun encodePolyline(nodes: List<Node>): List<String> {
    val encodedPoints = mutableListOf<String>()
    var previousLatitude = 0
    var previousLongitude = 0
    for (node in nodes) {
        val lat = node.latitudeE6 / 10
        val lon = node.longitudeE6 / 10
        encodedPoints.add(encodeSignedNumber(lat - previousLatitude).toString())
        encodedPoints.add(encodeSignedNumber(lon - previousLongitude).toString())

        previousLatitude = lat
        previousLongitude = lon
    }
    return encodedPoints
}

private fun encodeSignedNumber(num: Int): StringBuffer {
    var sgnNum = num shl 1
    if (num < 0) {
        sgnNum = sgnNum.inv()
    }
    return encodeNumber(sgnNum)
}

private fun encodeNumber(number: Int): StringBuffer {
    var num = number
    val encodeString = StringBuffer()
    while (num >= 0x20) {
        val nextValue = (0x20 or (num and 0x1f)) + 63
        encodeString.append(nextValue.toChar())
        num = num shr 5
    }
    num += 63
    encodeString.append(num.toChar())
    return encodeString
}


/*
    MATCH (source:Node {id: 1768890458}), (target:Node {id: 334409})
CALL gds.shortestPath.dijkstra.stream('murino', {
    sourceNode: source,
    targetNode: target,
    relationshipWeightProperty: 'length'
})
YIELD index, sourceNode, targetNode, totalCost, nodeIds, costs, path
RETURN
    index,
    gds.util.asNode(sourceNode).id AS sourceNodeName,
    gds.util.asNode(targetNode).id AS targetNodeName,
    totalCost,
    [nodeId IN nodeIds | gds.util.asNode(nodeId).id] AS nodeNames,
    costs,
    nodes(path) as path
ORDER BY index
*/

/*
CALL gds.graph.project(
    'murino',
    'Node',
    'WAY',
    {
        relationshipProperties: 'length'
    }
)
 */
