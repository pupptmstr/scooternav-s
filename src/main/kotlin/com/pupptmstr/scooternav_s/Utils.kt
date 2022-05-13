package com.pupptmstr.scooternav_s
fun getAllPedestrianStreets() = "[out:json];area[name=\"Мурино\"]->.small;area[name=\"Ленинградская область\"]->.big;(way(area.big)(area.small)  ['highway'~'path|steps|living_street|footway|corridor|cycleway|residential']['foot'!~'no']['access' !~ 'private']['access' !~ 'no']; ); (._;>;);out geom; out tags; out meta;"
