package com.vc.mapify.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.vc.mapify.Adapter.ForecastAdapter;
import com.vc.mapify.Forecast;
import com.vc.mapify.NetworkUtil;
import com.vc.mapify.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


//CLASS TO GET THE WEATHER FORECAST FOR THE WEEK
public class FiveDayWeather extends Fragment
{

    //VARIABLES DECLARED
    ListView listView;
    CardView cardView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //GETS THE CARD VIEW LAYOUT TO COMBINE DATA WITH
        View view = inflater.inflate(R.layout.fragment_five_day_weather, container,false);


        //FIND ELEMENTS FROM LAYOUT
        listView = view.findViewById(R.id.weatherList);
        cardView = view.findViewById(R.id.weatherCardView);


        //GETS THE URL TO CONNECT TO THE WEATHER API
        URL url = NetworkUtil.buildURLForWeather();

        new FetchWeatherData().execute(url);

        return view;

    }

    //GETS THE WEATHER
    class FetchWeatherData extends AsyncTask<URL,Void,String> {

        private  String TAG ="weatherDATA";

        //DECLARED ARRAY LIST
        ArrayList<Forecast> fiveDaylList = new ArrayList<Forecast>();

        @Override
        protected String doInBackground(URL... urls) {


            URL weatherURL = urls[0];
            String weatherData = null;

            //TRY AND CATCH

            try
            {
                //GETS THE WEATHER RESPONSE DATA
                weatherData=  NetworkUtil.getResponseFromHttpUrl(weatherURL);
            }

            //DISPLAYS ERROR
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //RETURNS THE DATA
            Log.i(TAG, "ourDATA " +weatherData);

            return weatherData;
        }

        //GETS THE WEATHER DATA AS JSON AND CONVERTS
        @Override
        protected void onPostExecute(String weatherData) {

            if (weatherData != null)
            {

                consumeJson(weatherData);

            }

            super.onPostExecute(weatherData);



        }


        public ArrayList<Forecast> consumeJson(String weatherJSON)
        {
            //CLEAR WEATHER LIST
            if (fiveDaylList != null)
            {
                fiveDaylList.clear();
            }

            //IF JSON RECEIEVED
            if (weatherJSON != null)

            {

                //TRY AND CATCH
                try{
                    //CREATE OBJECTS TO GET DATA
                    JSONObject rootWeatherData = new JSONObject(weatherJSON);
                    JSONArray fivedayForecast = rootWeatherData.getJSONArray("DailyForecasts");

                    //GETS THE DATA FOR 5 DAYS OF THE WEEK
                    for (int i =0 ; i < fivedayForecast.length();i++)
                    {
                        //OBJECT TO STORE DATA
                        Forecast forecastObject = new Forecast();

                        JSONObject  dailyWeather = fivedayForecast.getJSONObject(i);

                        //GETS DATE
                        String date = dailyWeather.getString("Date");
                        Log.i(TAG, "consumeJson: Date" + date);
                        forecastObject.setfDate(date);

                        //GETS MIN TEMP

                        JSONObject temperatureObject= dailyWeather.getJSONObject("Temperature");
                        JSONObject minTempObject = temperatureObject.getJSONObject("Minimum");
                        String minTemp = minTempObject.getString("Value");
                        Log.i(TAG, "consumeJson: minTemp" + minTemp);
                        forecastObject.setfMin(minTemp);

                        //GETS MAX TEMP

                        JSONObject maxTempObject = temperatureObject.getJSONObject("Maximum");
                        String maxTemp = maxTempObject.getString("Value");
                        Log.i(TAG, "consumeJson: maxTemp" + maxTemp);
                        forecastObject.setfMax(maxTemp);

                        //ADDS THE DATA FOR THE 5 DAYS OF THE WEEK
                        fiveDaylList.add(forecastObject);

                        if (fiveDaylList != null)
                        {

                            ForecastAdapter adapter = new ForecastAdapter(getContext(),fiveDaylList);
                            listView.setAdapter(adapter);

                        }

                    }

                    //RETURN DATA
                    return  fiveDaylList;


                }


                //DISPLAY ERROR
                catch (JSONException e)

                {
                    e.printStackTrace();
                }
            }

            return  null;
        }
    }

}