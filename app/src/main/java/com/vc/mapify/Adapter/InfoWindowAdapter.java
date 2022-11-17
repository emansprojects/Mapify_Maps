package com.vc.mapify.Adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;
import com.vc.mapify.databinding.InfoWindowLayoutBinding;

//CLASS TO SHOW INFORMATION OF A PLACE ON A MAP
public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    //VARIABLES DECLARED
    private InfoWindowLayoutBinding binding;
    private Location location;
    private Context context;

    //CONSTRUCTOR TO SET THE DATA FOR THE PLACE
    public InfoWindowAdapter(Location location, Context context) {

        this.location = location;
        this.context = context;

        binding = InfoWindowLayoutBinding.inflate(LayoutInflater.from(context), null, false);
    }

    //METHOD TO GET THE DATA TO SHOW
    @Override
    public View getInfoWindow(Marker marker) {

        //SETS THE NAME OF THE PLACE
        binding.txtLocationName.setText(marker.getTitle());

        //GETS THE DISTANCE
        double distance = SphericalUtil.computeDistanceBetween(new LatLng(location.getLatitude(), location.getLongitude()),
                marker.getPosition());

        //DISPLAYS THE DISTANCE AND DISTANCE TYPE
        if (distance > 1000) {
            double kilometers = distance / 1000;
            binding.txtLocationDistance.setText(distance + " KM");
        } else {
            binding.txtLocationDistance.setText(distance + " Meters");

        }

        //SHOWS THE LOCATION SPEED
        float speed = location.getSpeed();

        //SHOWS THE SPEED AND TIME TO GET THE LOCATION
        if (speed > 0) {
            double time = distance / speed;
            binding.txtLocationTime.setText(time + " sec");
        } else {
            binding.txtLocationTime.setText("N/A");
        }
        return binding.getRoot();
    }

    //GETS THE DATA TO DISPLAY ABOUT THE PLACE
    @Override
    public View getInfoContents(Marker marker) {

        //SHOWS THE PLACE NAME
        binding.txtLocationName.setText(marker.getTitle());

        //GETS THE DISTANCE
        double distance = SphericalUtil.computeDistanceBetween(new LatLng(location.getLatitude(), location.getLongitude()),
                marker.getPosition());

        //DISPLAYS THE DISTANCE AND DISTANCE TYPE
        if (distance > 1000) {
            double kilometers = distance / 1000;
            binding.txtLocationDistance.setText(distance + " KM");
        } else {
            binding.txtLocationDistance.setText(distance + " Meters");

        }

        //DISPLAYS THE SPEED AND TIME TO GET TO THE PLACE
        float speed = location.getSpeed();

        if (speed > 0) {
            double time = distance / speed;
            binding.txtLocationTime.setText(time + " sec");
        } else {
            binding.txtLocationTime.setText("N/A");
        }
        return binding.getRoot();
    }
}