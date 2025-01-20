package io.github.game.utils;

import org.bson.Document;

import java.util.Date;

public class TrainLocHistory {
    private Date timeOfRequest;
    private String trainType;
    private String trainNumber;
    private String routeFrom;
    private String routeTo;
    private String routeStartTime;
    private String nextStation;
    private int delay;
    private Coordinates coordinates;

    public Date getTimeOfRequest() {
        return timeOfRequest;
    }

    public String getTrainType() {
        return trainType;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getRouteFrom() {
        return routeFrom;
    }

    public String getRouteTo() {
        return routeTo;
    }

    public String getRouteStartTime() {
        return routeStartTime;
    }

    public String getNextStation() {
        return nextStation;
    }

    public int getDelay() {
        return delay;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setTimeOfRequest(Date timeOfRequest) {
        this.timeOfRequest = timeOfRequest;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public void setRouteFrom(String routeFrom) {
        this.routeFrom = routeFrom;
    }

    public void setRouteTo(String routeTo) {
        this.routeTo = routeTo;
    }

    public void setRouteStartTime(String routeStartTime) {
        this.routeStartTime = routeStartTime;
    }

    public void setNextStation(String nextStation) {
        this.nextStation = nextStation;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public TrainLocHistory(Document doc){
        setTimeOfRequest(doc.getDate("timeOfRequest"));
        setTrainType(doc.getString("trainType"));
        setRouteFrom(doc.getString("routeFrom"));
        setRouteTo(doc.getString("routeTo"));
        setRouteStartTime(doc.getString("routeStartTime"));
        setNextStation(doc.getString("nextStation"));
        setDelay(doc.getInteger("delay"));
        setCoordinates(doc.get("coordinates", Coordinates.class));
    }
}

