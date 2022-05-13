package com.pupptmstr.scooternav_s.ogm

import org.neo4j.ogm.annotation.*

@RelationshipEntity(type = "WAY")
data class Way(
    @StartNode var nodeStart: Node, @EndNode var nodeEnd: Node,

    @Id var id: String = "0",
    var averageSpeed: Double = 10.0, //km per hour
    var bandwidth: Int = 5, //num of client can be that street same time
    var highway: String,
    var surface: String,
    var length: Double
) {
    constructor() : this(
        Node(),
        Node(), "0", 10.0, 5, "no", "no", 0.0)
}
