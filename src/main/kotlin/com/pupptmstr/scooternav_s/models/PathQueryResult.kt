package com.pupptmstr.scooternav_s.models

import com.pupptmstr.scooternav_s.ogm.Node

data class PathQueryResult(
    val path: List<Node>,
    val lengths: Array<Double>,
    val totalLength: Double
)
