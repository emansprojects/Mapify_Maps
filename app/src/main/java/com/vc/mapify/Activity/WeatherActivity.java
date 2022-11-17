package com.vc.mapify.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.vc.mapify.Fragments.FiveDayWeather;
import com.vc.mapify.R;

//CLASS TO SHOW THE WEATHER
public class WeatherActivity extends AppCompatActivity {

    //VARIABLES DECLARED
    public static final String TAG ="NETWORK UTIL IN MAIN";
    Fragment weatherFragment ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //CONSTRUCTOR TO GET WEATHER
        weatherFragment = new FiveDayWeather();

        //DISPLAYS THE LAYOUT WHICH WILL SHOW THE 5 DAY WEATHER
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //GETS THE LAYOUT FOR THE WEATHER
        transaction.replace(R.id.weather_frame, weatherFragment);
        transaction.commit();


    }
}