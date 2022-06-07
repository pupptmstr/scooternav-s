package com.pupptmstr.scooternav_s.ogm;

import com.pupptmstr.scooternav_s.ogm.Way;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Objects;
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

    public void waysTo(
            Node node,
            long wayId,
            double averageSpeed,
            int bandwidth,
            String highway,
            double surfaceMultiplier,
            double length,
            double preference) {
        String fullWayId = wayId + "+" + this.id + "+" + node.id;
        Way way = new Way(this, node, fullWayId, averageSpeed, bandwidth, highway, surfaceMultiplier, length, preference);
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

    public int getLatitudeE6() {
        return (int) (this.getLat() * 1E6);
    }

    public int getLongitudeE6() {
        return (int) (this.getLon() * 1E6);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", amenity='" + amenity + '\'' +
                ", highway='" + highway + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id && Double.compare(node.lat, lat) == 0 && Double.compare(node.lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lat, lon);
    }
}
