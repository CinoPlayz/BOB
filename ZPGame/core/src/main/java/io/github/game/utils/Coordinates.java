package io.github.game.utils;

public class Coordinates{
    private double lat;
    private double lng;

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Coordinates(){

    }

    public Coordinates(double lat, double lng){
        setLat(lat);
        setLng(lng);
    }
}
