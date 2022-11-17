package com.vc.mapify.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vc.mapify.Forecast;
import com.vc.mapify.R;

import java.util.ArrayList;

//GETS AND SETS THE DATA FOR THE FORECAST
public class ForecastAdapter extends ArrayAdapter<Forecast> {

    //CONSTRUCTOR TO GET THE WEATHER DATA AND SET IT
    public ForecastAdapter(@NonNull Context context, ArrayList<Forecast> weatherArrayList)
    {
        super(context,0, weatherArrayList);
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //DISPLAY THE FORECAST
        Forecast forecast = getItem(position);
        Context context;


        //DISPLAY LAYOUT FOR THE WEATHER
        if (convertView == null)
        {
            convertView= LayoutInflater.from(getContext())
                    .inflate(R.layout.weather_item, parent, false);

        }

        //SETS THE MAXIMUM TEMP
        TextView maxTemp = convertView.findViewById(R.id.txt_maxTemp);
        //SETS THE MINIMUM TEMP
        TextView minTemp = convertView.findViewById(R.id.txt_minTemp);
        //SETS THE DATE
        TextView date = convertView.findViewById(R.id.txt_Date);

        //GETS THE MAX TEMP
        maxTemp.setText(forecast.getfMax());
        //GETS THE MIN TEMP
        minTemp.setText(forecast.getfMin());
        //GETS THE DATE
        date.setText(forecast.getfDate());

        context = convertView.getContext();

        //RETURNS THE DETAILS
        return convertView;
    }
}