package com.vc.mapify.Model.GooglePlaceModel;

import com.vc.mapify.GooglePlaceModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.vc.mapify.GooglePlaceModel;

import java.util.List;

//CLASS TO GET THE RESULT OF PLACES FROM THE GOOGLE MAPS API
public class GoogleResponseModel {

    //LIST DECLARED TO STORE PLACES AVAILABLE
    @SerializedName("results")
    @Expose
    private List<GooglePlaceModel> googlePlaceModelList;

    //ERROR DECLARED
    @SerializedName("error_message")
    @Expose
    private String error;

    //GETTER AND SETTER METHODS FOR THE ERROR
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    //GETTER AND SETTER METHODS TO POPULATE THE LIST WITH PLACES
    public List<GooglePlaceModel> getGooglePlaceModelList() {
        return googlePlaceModelList;
    }

    public void setGooglePlaceModelList(List<GooglePlaceModel> googlePlaceModelList) {
        this.googlePlaceModelList = googlePlaceModelList;
    }
}
