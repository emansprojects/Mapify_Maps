package com.vc.mapify.Model.DirectionPlaceModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//CLASS TO SET THE POINTS FOR THE DIRECTION
public class DirectionPolylineModel {

    //VARIABLES DECLARED
    @SerializedName("points")
    @Expose
    private String points;

    //GETTER AND SETTER METHODS
    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}
