package com.vc.mapify.WebServices;

import com.vc.mapify.Model.DirectionPlaceModel.DirectionResponseModel;
import com.vc.mapify.Model.GooglePlaceModel.GoogleResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

//CLASS TO GET THE NEARBY PLACES AND DIRECTIONS THAT ARE CLOSE TO THE USER'S LOCATION
public interface RetrofitAPI {

    //GETS THE NEARBY PLACES
    @GET
    Call<GoogleResponseModel> getNearByPlaces(@Url String url);

    //GETS THE DIRECTIONS OF THE PLACES THAT ARE NEARBY
    @GET
    Call<DirectionResponseModel> getDirection(@Url String url);
}
