package com.vc.mapify;

//CLASS TO PROVIDE THE USER WITH OPTIONS TO SAVE OR GET DIRECTIONS ON A PLACE THEY SELECT
public interface NearLocationInterface {

    //SAVES PLACES THE USER IS INTERESTED IN
    void onSaveClick(GooglePlaceModel googlePlaceModel);

    //GIVES THE USER DIRECTIONS ON A PLACE SELECTED
    void onDirectionClick(GooglePlaceModel googlePlaceModel);
}
