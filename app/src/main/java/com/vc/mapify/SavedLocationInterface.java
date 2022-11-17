package com.vc.mapify;

//CLASS TO SAVE THE PLACES THAT THE USER IS INTERESTED IN
public interface SavedLocationInterface {

    //GIVES USERS THE OPTION TO SAVE A PLACE ONCE THEY VIEW A PLACE AND IT'S DATA
    void onLocationClick(SavedPlaceModel savedPlaceModel);
}
