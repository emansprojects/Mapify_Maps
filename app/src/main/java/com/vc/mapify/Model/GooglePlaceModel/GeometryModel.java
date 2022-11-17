package com.vc.mapify.Model.GooglePlaceModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//CLASS TO GET THE LOCATION OF A PLACE
public class GeometryModel {

    //VARIABLE DECLARED
    @SerializedName("location")
    @Expose
    private LocationModel location;


    //GETTER AND SETTER METHODS
    public LocationModel getLocation() {
        return location;
    }

    public void setLocation(LocationModel location) {
        this.location = location;
    }


}
