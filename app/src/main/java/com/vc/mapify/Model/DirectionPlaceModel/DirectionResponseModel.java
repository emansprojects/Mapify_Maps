package com.vc.mapify.Model.DirectionPlaceModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//CLASS TO GET THE RESPONSE FOR A ROUTE TO A PLACE
public class DirectionResponseModel {

    //VARIABLES DECLARED
    @SerializedName("routes")
    @Expose
    List<DirectionRouteModel> directionRouteModels;


    //GETTER AND SETTER METHOD FOR THE ROUTE RESPONSE
    public List<DirectionRouteModel> getDirectionRouteModels() {
        return directionRouteModels;
    }

    public void setDirectionRouteModels(List<DirectionRouteModel> directionRouteModels) {
        this.directionRouteModels = directionRouteModels;
    }
}
