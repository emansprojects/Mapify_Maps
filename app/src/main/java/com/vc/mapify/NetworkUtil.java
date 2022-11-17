package com.vc.mapify;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

//CLASS TO GET THE WEATHER FORECAST FOR THE WEEK
public class NetworkUtil {

    //VARIABLES DECLARED
    private static final String BASE_URL = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/30605";
    private static final String API_KEY = "CIvazOzGvEmsEirJ7VMHvO0Bgu7ESOtB";
    private static final String PARAM_API_KEY = "apikey";
    private static final String METRIC = "metric";
    private static final String METRIC_PARAM = "true";
    public static String TAG = "NETWORK_UTIL";


    //METHOD TO CONNECT TO THE API TO GET WEATHER DATA
    public static URL buildURLForWeather() {

        //URL BUILT TO CONNECT TO THE API
        Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_API_KEY, API_KEY)// passing in api param + key
                .appendQueryParameter(METRIC, METRIC_PARAM) // passing in metric as measurement unit

                .build();

        URL url = null;

        //TRY AND CATCH TO GET THE URL
        try {
            //GETS THE DATA
            url = new URL(buildUri.toString());
            //RETURNS AN ERROR IF DATA CANNOT BE FETCHED
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //GETS THE URL TO CONNECT TO THE API
        Log.i(TAG, "buildURLForWeather: " + url);

        return url;

    }

    //METHOD TO GET THE WEATHER FORECAST
    public static String getResponseFromHttpUrl(URL url) throws IOException {

        //OPENS THE CONNECTION TO THE API FOR DATA
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        //TRY AND CATCH
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("//A");
            boolean hasinput = scanner.hasNext();

            if (hasinput) {
                return scanner.next();
            } else {
                return null;
            }

            //ENDS THE CONNECTION
        } finally {
            urlConnection.disconnect();

        }
    }

}


