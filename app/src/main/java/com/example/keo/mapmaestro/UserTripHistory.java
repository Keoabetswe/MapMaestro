package com.example.keo.mapmaestro;

import java.util.Date;

public class UserTripHistory
{
    public String startingPoint;
    public String destination;
    //public String duration;
    //public String distance;
    public String tripDate;

    public UserTripHistory()
    {

    }

    public UserTripHistory(String startingPoint, String destination, String tripDate)
    {
        this.startingPoint = startingPoint;
        this.destination = destination;
        //this.distance = distance;
        //this.duration = duration;
        this.tripDate = tripDate;
    }

    public String getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }
}
