package com.vc.mapify.Model.DirectionPlaceModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//CLASS TO GET THE ROUTE TO A PLACE
public class DirectionRouteModel {

    //VARIABLES DECLARED
    @SerializedName("legs")
    @Expose
    private List<DirectionLegModel> legs;

    @SerializedName("overview_polyline")
    @Expose
    private DirectionPolylineModel polylineModel;

    @SerializedName("summary")
    @Expose
    private String summary;

    //GETTER AND SETTER METHODS
    public List<DirectionLegModel> getLegs() {
        return legs;
    }

    public void setLegs(List<DirectionLegModel> legs) {
        this.legs = legs;
    }

    public DirectionPolylineModel getPolylineModel() {
        return polylineModel;
    }

    public void setPolylineModel(DirectionPolylineModel polylineModel) {
        this.polylineModel = polylineModel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
