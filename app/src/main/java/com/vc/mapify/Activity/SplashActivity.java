package com.vc.mapify.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.vc.mapify.Fragments.HomeFragment;
import com.vc.mapify.R;

//CLASS TO SHOW THE SPLASH SCREEN OF THE APP'S LOGO
public class SplashActivity extends AppCompatActivity {

    //GET CURRENT USER
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //GET CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //IF USER IS FOUND, LOGIN TO THE APP
                if(firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //IF USER IS NOT FOUND, USER MUST LOGIN
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }
                
            }
        }, 3000);


    }




}