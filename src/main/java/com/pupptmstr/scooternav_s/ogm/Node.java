package com.pupptmstr.scooternav_s.ogm;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Node {

    @Id
    private long id;
    private double lat;
    private double lon;
    private String amenity;
    private String highway;

    @Relationship(type = "WAY")
    private Set<Way> ways = new HashSet<>();

    public Node() {
    }

    public Node(long id, double lat, double lon, String amenity, String highway) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.amenity = amenity;
        this.highway = highway;
    }

    public void waysTo(Node node, Long wayId, double averageSpeed, int bandwidth, String highway, String surface, double length) {
        String fullWayId = wayId + "+" + this.id + "+" + node.id;
        Way way = new Way(this, node, fullWayId, averageSpeed, bandwidth, highway, surface, length);
        ways.add(way);
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public long getId() {
        return id;
    }

    public String getAmenity() {
        return amenity;
    }

    public String getHighway() {
        return highway;
    }

    public Set<Way> getWays() {
        return ways;
    }
}
