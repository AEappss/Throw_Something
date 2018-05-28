package com.aeappss.multiplayer;

import android.net.Uri;

import java.util.HashMap;

public class Player {
    private String name;
    private double latitude = 0;
    private double longitude = 0;
    private double distance = 0;
    private String team;
    private double angle = 0;

    public String getTeam() {
        return team;
    }

    public void  setTeam(String team) {
        this.team = team;
    }

    public double getDistance() {
        return distance;
    }

    public double getAngle() {
        return angle;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public String toString(){
        return name +  " " + id + " " + team;
    }
}
