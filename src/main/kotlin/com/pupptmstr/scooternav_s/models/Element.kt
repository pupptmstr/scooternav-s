package com.pupptmstr.scooternav_s.models

data class Element(
    val type: String,
    val id: Long,
    val timestamp: String,
    val version: Int,
    val nodes: Array<Long>,
    val tags: Tags
) {
}