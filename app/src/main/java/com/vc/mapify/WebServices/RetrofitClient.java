package com.vc.mapify.WebServices;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

//CLASS TO CONNECT TO THE GOOGLE MAPS API
public class RetrofitClient {

    //VARIABLES DECLARED
    private static Retrofit retrofit = null;
    public static final String BASE_URL = "https://maps.googleapis.com";

    //GETS URL TO CONNECT TO THE GOOGLE MAPS API
    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
