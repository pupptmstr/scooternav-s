package com.pupptmstr.scooternav_s
fun getAllPedestrianStreets() = "[out:json];area[name=\"Мурино\"]->.small;area[name=\"Ленинградская область\"]->.big;(way(area.big)(area.small)  ['highway'~'path|steps|living_street|footway|corridor|cycleway|residential']['foot'!~'no']['access' !~ 'private']['access' !~ 'no']; ); (._;>;);out geom; out tags; out meta;"

fun createGDSGraph() = """
    CALL gds.graph.project(
        'murino',
        'Node',
        'WAY',
        {
            relationshipProperties: 'length'
        }
    )
""".trimIndent()

fun getShortestWayCypher(id1: Long, id2: Long) = """
    MATCH (source:Node {id: $id1}), (target:Node {id: $id2})
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
""".trimIndent()
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
