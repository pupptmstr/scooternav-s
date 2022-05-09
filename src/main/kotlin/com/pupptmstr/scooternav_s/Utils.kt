package com.pupptmstr.scooternav_s

fun getFootways() = "[out:json]; area[name=\"Санкт-Петербург\"]; way(area)[highway=\"footway\"]; out meta;"
fun getAllPedestrianStreets() = "[out:json];area[name=\"Санкт-Петербург\"];(way(area)['highway'~'path|steps|living_street|footway|corridor|cycleway|residential']['foot'!~'no']['access' !~ 'private']['access' !~ 'no']; ); (._;>;); out geom; out tags; out meta;"