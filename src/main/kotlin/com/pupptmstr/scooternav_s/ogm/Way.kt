package com.pupptmstr.scooternav_s.ogm

import org.neo4j.ogm.annotation.*

@RelationshipEntity(type = "WAY")
data class Way(
    @StartNode var nodeStart: Node, @EndNode var nodeEnd: Node,

    @Id var id: String = "0",
    var averageSpeed: Double = 10.0, //km per hour
    var bandwidth: Int = 5, //num of client can be that street same time
    var highway: String,
    var surfaceMultiplier: Double, //bonus or fine for surface of way
    var length: Double,
    var preference: Double //prefer that street? less is better, works like cost
) {
    constructor() : this(
        Node(),
        Node(), "0", 10.0, 5, "no", 0.0, 0.0, 0.0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Way

        if (nodeStart != other.nodeStart) return false
        if (nodeEnd != other.nodeEnd) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nodeStart.hashCode()
        result = 31 * result + nodeEnd.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
