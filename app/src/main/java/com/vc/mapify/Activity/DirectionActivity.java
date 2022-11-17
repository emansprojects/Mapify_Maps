package com.vc.mapify.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.vc.mapify.Adapter.DirectionStepAdapter;
import com.vc.mapify.Constant.AllConstant;
import com.vc.mapify.Global;
import com.vc.mapify.Model.DirectionPlaceModel.DirectionLegModel;
import com.vc.mapify.Model.DirectionPlaceModel.DirectionResponseModel;
import com.vc.mapify.Model.DirectionPlaceModel.DirectionRouteModel;
import com.vc.mapify.Model.DirectionPlaceModel.DirectionStepModel;
import com.vc.mapify.Permissions.AppPermissions;
import com.vc.mapify.R;
import com.vc.mapify.Utility.LoadingDialog;
import com.vc.mapify.WebServices.RetrofitAPI;
import com.vc.mapify.WebServices.RetrofitClient;
import com.vc.mapify.databinding.ActivityDirectionBinding;
import com.vc.mapify.databinding.BottomSheetLayoutBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//CLASS TO SHOW THE USER DIRECTIONS TO A PLACE
public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    //VARIABLES DECLARED
    private ActivityDirectionBinding binding;
    GoogleMap mGoogleMap;
    private FirebaseAuth firebaseAuth;
    private AppPermissions appPermissions;
    private boolean isLocationPermissionOk, isTrafficEnable;
    private BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;
    private BottomSheetLayoutBinding bottomSheetLayoutBinding;
    private RetrofitAPI retrofitAPI;
    private LoadingDialog loadingDialog;
    private Location currentLocation;
    private Double endLat, endLng;
    private String placeId;
    private int currentMode;
    Button start;
    private DirectionStepAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDirectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //GETS THE END LATITUDE
        endLat = getIntent().getDoubleExtra("lat", 0.0);
        //GETS THE END LONGITUDE
        endLng = getIntent().getDoubleExtra("lng", 0.0);
        //GETS THE PLACE ID
        placeId = getIntent().getStringExtra("placeId");
        //GETS THE CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();

        //CALLS APP PERMISSIONS TO GET THE USER'S LOCATION
        appPermissions = new AppPermissions();
        loadingDialog = new LoadingDialog(this);

        //CONNECTS TO THE GOOGLE API
        retrofitAPI = RetrofitClient.getRetrofitClient().create(RetrofitAPI.class);

        //ALLOWS THE USER TO PULL UP THE BOTTOM SCREEN TO SEE THE STEPS AND SLIDE IT BACK DOWN
        bottomSheetLayoutBinding = binding.bottomSheet;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayoutBinding.getRoot());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        //GETS THE DATA FOR THE STEPS
        adapter = new DirectionStepAdapter();

        bottomSheetLayoutBinding.stepRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //SETS THE STEPS AS A RECYCLERVIEW
        bottomSheetLayoutBinding.stepRecyclerView.setAdapter(adapter);


        //DISPLAYS A MAP ON THIS SCREEN
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.directionMap);

        mapFragment.getMapAsync(this);


        //CODE ATTRIBUTION AT THE END
        //SHOWS TRAFFIC TO THE USER
        binding.enableTraffic.setOnClickListener(view -> {
            if (isTrafficEnable) {
                //IF LIVE TRAFFIC IS TURNED OFF, DON'T DISPLAY TRAFFIC
                if (mGoogleMap != null) {
                    mGoogleMap.setTrafficEnabled(false);
                    //NOTIFY USER
                    Toast.makeText(this, "Traffic Disabled", Toast.LENGTH_SHORT).show();
                    isTrafficEnable = false;
                }
            } else {
                //IF LIVE TRAFFIC IS TURNED ON, SHOW TRAFFIC
                if (mGoogleMap != null) {
                    //NOTIFY USER
                    Toast.makeText(this, "Traffic Enabled", Toast.LENGTH_SHORT).show();
                    mGoogleMap.setTrafficEnabled(true);
                    isTrafficEnable = true;
                }
            }
        });


        //ALLOWS THE USER TO SELECT A TRAVEL MODE
        binding.travelMode.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId != -1) {
                    switch (checkedId) {
                        //IF THE USER SELECTS THE DRIVING OPTION, SHOW THE ROUTE FOR THIS THIS OPTION AND ADDITIONAL DATA
                        case R.id.btnChipDriving:
                            getDirection("driving");
                            break;

                            //IF THE USER SELECTS THE WALKING OPTION, SHOW THE ROUTE FOR THIS OPTION AND ADDITIONAL DATA
                        case R.id.btnChipWalking:
                            getDirection("walking");
                            break;
                    }
                }
            }
        });

    }


    //WHEN THE SCREEN DISPLDAYS, GIVE THE USER THEIR 10 POINTS FOR THE TRIP
    @Override
    public void onStart() {

        //DISPLAY DIALOG TO SHOW THE USER THEY HAVE EARNED 10 POINTS FOR THE TRIP
        super.onStart();
        Dialog dialog = new Dialog(DirectionActivity.this);
        dialog.setContentView(R.layout.dialog_trip_loyout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.getWindow().setWindowAnimations(R.style.AnimationsForDialog);

        //START DIALOG ANIMATION
        start = dialog.findViewById(R.id.startTrip);


        //DATABASE REFERENCE TO ADD POINTS FOR THE USER
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Progress").child(firebaseAuth.getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //COUNT THE NUMBER OF TRIPS THE USER HAS DONE
                    int distanceModel = (int) snapshot.getChildrenCount();

                    //SET THE VALUE
                    Global.progress = distanceModel;


                }else{
                    //IF NO TRIPS ARE DONE, SET THE VALUE AS 0
                    Global.progress = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //WHEN THE USER CLICKS START, ADD POINTS
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //GET THE DATABASE REFERENCE
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Progress").child(firebaseAuth.getCurrentUser().getUid());

                //ADD 1 TO THE NUMBER OF TRIPS DONE
                int calc = Global.progress + 1;
                //SET VALUE IN DATABASE
                String val = "Trip " + calc;
                reference.child(reference.push().getKey()).setValue(val);

                //CLOSE DIALOG
                dialog.cancel();
            }
        });

        //SHOW DIALOG
        dialog.show();
    }


    //GETS THE DIRECTIONS TO THE PLACE
    private void getDirection(String mode) {

        //CHECK IF THE LOCATION IS PERMITTED
        if (isLocationPermissionOk) {
            loadingDialog.startLoading();
            //CONNECTS TO THE GOOGLE API
            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                    "&destination=" + endLat + "," + endLng +
                    "&mode=" + mode +
                    "&key=" + getResources().getString(R.string.API_KEY);


            //GETS THE RESPONSE FOR THE STEPS TO THE LOCATION FROM GOOGLE MAPS API
            retrofitAPI.getDirection(url).enqueue(new Callback<DirectionResponseModel>() {
                @Override
                public void onResponse(Call<DirectionResponseModel> call, Response<DirectionResponseModel> response) {
                    Gson gson = new Gson();
                    String res = gson.toJson(response.body());
                    Log.d("TAG", "onResponse: " + res);

                    if (response.errorBody() == null) {
                        if (response.body() != null) {
                            clearUI();

                            if (response.body().getDirectionRouteModels().size() > 0) {
                                DirectionRouteModel routeModel = response.body().getDirectionRouteModels().get(0);
                                DirectionLegModel legModel = routeModel.getLegs().get(0);

                                //SET START LOCATION
                                binding.txtStartLocation.setText(legModel.getStartAddress());
                                //SET END LOCATION
                                binding.txtEndLocation.setText(legModel.getEndAddress());


                                //DATABASE REFERENCE TO GET THE PREFERRED DISTANCE TYPE
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Distance").child(firebaseAuth.getCurrentUser().getUid());
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {

                                            //GET VALUE
                                            String distanceModel = snapshot.getValue().toString().trim();
                                            //SET THE TOTAL TIME
                                            bottomSheetLayoutBinding.txtSheetTime.setText(legModel.getDuration().getText());

                                            //IF DISTANCE IS KILOMETRES
                                            if(distanceModel.matches("kilometres")){

                                                //DISPLAY DISTANCE IN KILOMETRES
                                                bottomSheetLayoutBinding.txtSheetDistance.setText(legModel.getDistance().getText());
                                            }

                                            //IF THE DISTANCE TYPE IS MILES
                                            else if(distanceModel.matches("miles")){

                                                //GET THE KM DISTANCE
                                                bottomSheetLayoutBinding.txtSheetDistance.setText(legModel.getDistance().getText());

                                                //GET THE DISTANCE IN KM
                                                String text = legModel.getDistance().getText();

                                                //REMOVE KM FROM THE END OF THE DISTANCE
                                                String text2 = text.substring(0,text.length() - 2);

                                                //CONVERT STRING TO DOUBLE
                                                double num1 = Double.parseDouble(text2);
                                                //CONVERT KM TO MILES
                                                double miles = num1 * 0.621371;

                                                //CONVERT TO DECIMAL FORMAT - ROUND OFF TO 2 DIGITS
                                                DecimalFormat df = new DecimalFormat("####0.00");

                                                //DISPLAY DISTANCE IN MILES
                                                bottomSheetLayoutBinding.txtSheetDistance.setText(df.format(miles) + " miles");
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });



                                //MARKS THE END LOCATION
                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(legModel.getEndLocation().getLat(), legModel.getEndLocation().getLng()))
                                        .title("End Location"));

                                //MARKS THE START LOCATION
                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(legModel.getStartLocation().getLat(), legModel.getStartLocation().getLng()))
                                        .title("Start Location"));

                                //GETS THE STEPS
                                adapter.setDirectionStepModels(legModel.getSteps());
                                List<LatLng> stepList = new ArrayList<>();

                                //CODE ATTRIBUTION AT THE END
                                //MARKS THE ROUTE
                                PolylineOptions options = new PolylineOptions()
                                        .width(25)
                                        .color(Color.BLUE)
                                        .geodesic(true)
                                        .clickable(true)
                                        .visible(true);

                                //SHOWS THE ROUTE IF THE USER WANTS TO WALK
                                List<PatternItem> pattern;
                                if (mode.equals("walking")) {
                                    pattern = Arrays.asList(
                                            new Dot(), new Gap(10));

                                    options.jointType(JointType.ROUND);
                                } else {
                                    pattern = Arrays.asList(
                                            new Dash(30));
                                }

                                options.pattern(pattern);

                                //GETS THE STEPS FOR THE DIRECTIONS TO THE PLACE
                                for (DirectionStepModel stepModel : legModel.getSteps()) {
                                    List<com.google.maps.model.LatLng> decodedLatLng = decode(stepModel.getPolyline().getPoints());
                                    for (com.google.maps.model.LatLng latLng : decodedLatLng) {
                                        stepList.add(new LatLng(latLng.lat, latLng.lng));
                                    }
                                }

                                options.addAll(stepList);


                                Polyline polyline = mGoogleMap.addPolyline(options);

                                //SHOWS START LOCATION
                                LatLng startLocation = new LatLng(legModel.getStartLocation().getLat(), legModel.getStartLocation().getLng());
                                //SHOWS END LOCATION
                                LatLng endLocation = new LatLng(legModel.getStartLocation().getLat(), legModel.getStartLocation().getLng());


                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(startLocation, endLocation), 17));

                            } else {
                                //DISPLAY ERROR
                                Toast.makeText(DirectionActivity.this, "No route find", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //DISPLAY ERROR
                            Toast.makeText(DirectionActivity.this, "No route find", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //DISPLAY RESPONSE
                        Log.d("TAG", "onResponse: " + response);
                    }

                    //STOP LOADING
                    loadingDialog.stopLoading();
                }

                @Override
                public void onFailure(Call<DirectionResponseModel> call, Throwable t) {

                }
            });
        }

    }

    //CLEAR THE SCREEN OF TEXTS AND DATA
    private void clearUI() {

        mGoogleMap.clear();
        binding.txtStartLocation.setText("");
        binding.txtEndLocation.setText("");
        bottomSheetLayoutBinding.txtSheetDistance.setText("");
        bottomSheetLayoutBinding.txtSheetTime.setText("");

    }


    //DISPLAYS THE MAP
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        //IF THE USER SELECTED NORMAL MAP TYPE
        if(Global.mapType == "normal"){
            //SHOW NORMAL MAP TYPE MAP
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            //IF THE USER SELECTED SATELLITE MAP TYPE
        }else if(Global.mapType == "satellite"){
            //SHOW THE MAP AS A SATELLITE MAP TYPE
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            //IF THE USER SELECTED THE TERRAIN MAP TYPE
        }else if(Global.mapType == "terrain"){
            //SHOW THE MAP AS A TERRAIN MAP TYPE
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

        //CHECK IF PERMISSION TO ACCESS USER LOCATION IS OKAY
        if (appPermissions.isLocationOk(this)) {
            isLocationPermissionOk = true;
            setupGoogleMap();
        } else {

            //GET PERMISSION TO ACCESS THE USER'S LOCATION
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Near Me required location permission to show you near by places")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                appPermissions.requestLocationPermission(DirectionActivity.this);


                            }

                        })
                        .create().show();
            } else {
                appPermissions.requestLocationPermission(DirectionActivity.this);
            }
        }
    }


    //GET PERMISSION FROM THE USER TO ACCESS THEIR LOCATION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AllConstant.LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionOk = true;
                setupGoogleMap();
            } else {
                isLocationPermissionOk = false;
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //SETS UP MAP
    private void setupGoogleMap() {

        //CHECKS IF THE USER GAVE PERMISSION FOR LOCATION ACCESS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        //SET UP MAP
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setTiltGesturesEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(false);

        //GETS THE USER'S LOCATION
        getCurrentLocation();
    }

    //CODE ATTRIBUTION AT THE END
    //GETS THE USER'S LOCATION
    private void getCurrentLocation() {

        //CHECKS THAT PERMISSION IS GRANTED
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        //CODE ATTRIBUTION AT THE END
        //GETS THE USER'S LOCATION
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    //GETS DIRECTION FOR DRIVING TO THE DESTINATION
                    getDirection("driving");

                } else {
                    //DISPLAY ERROR
                    Toast.makeText(DirectionActivity.this, "Location Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //METHOD FOR BACK PRESSED
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    //CLOSES THE DIRECTIONS SHEET IF THE USER BACK PRESSES
    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    //CODE ATTRIBUTION AT THE END
    //METHOD TO GET THE STEPS AND PATH NEEDED TO TAKE WITH LATITUDE AND LONGITUDE IN ORDER TO GET TO THE DESTINATION
    private List<com.google.maps.model.LatLng> decode(String points) {

        int len = points.length();

        final List<com.google.maps.model.LatLng> path = new ArrayList<>(len / 2);
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = points.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = points.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new com.google.maps.model.LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;

    }
}


//ATTRIBUTION FOR TRAFFIC
/**AUTHOR: MAPS AND APPS
 * YEAR: 2020
 * TITLE: GPS MAPS, LOCATION, DIRECTIONS, TRAFFIC  AND ROUTES
 * LINK: https://www.youtube.com/watch?v=6KlaxF7xDos
 * PLATFORM: YOUTUBE
 */

//ATTRIBUTION FOR GETTING THE ROUTE
/**AUTHOR: THE CODE CITY
 * YEAR: 2018
 * TITLE: DRAW ROUTE BETWEEN TWO LOCATIONS IN ANDROID - GOOGLE MAPS DIRECTIONS API
 * LINK: https://www.youtube.com/watch?v=wRDLjUK8nyU
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: THE EASY LEARN ANDROID
 * YEAR: 2020
 * TITLE: HOW TO GET ROUTE IN GOOGLE MAP IN ANDROID || HOW TO SHOW ROUTE IN MAPS
 * LINK: https://www.youtube.com/watch?v=-wRqXeORuqo
 * PLATFORM: YOUTUBE
 */


//ATTRIBUTION FOR GETTING THE USER'S CURRENT LOCATION
/**AUTHOR: ABBAS HASSAN
 * YEAR: 2019
 * TITLE: CURRENT LOCATION AND NEARBY PLACES SUGGESTIONS IN ANDROID | GOOGLE MAPS API & PLACES SDK
 * LINK: https://www.youtube.com/watch?v=ifoVBdtXsv0
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: MD JAMAL
 * YEAR: 2020
 * TITLE: CURRENT LOCATION IN GOOGLE MAP ANDROID STUDIO | CURRENT LOCATION ON GOOGLE MAP
 * LINK: https://www.youtube.com/watch?v=kRAyXxgwOhQ
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: SMALL ACADEMY
 * YEAR: 2019
 * TITLE: LOCATE & SHOW USER LOCATION IN GOOGLE MAP | GEO LOCATION TUTORIAL | ANDROID STUDIO 3.4 TUTORIAL
 * LINK: https://www.youtube.com/watch?v=pNeuuImirHY
 * PLATFORM: YOUTUBE
 */

//ATTRIBUTION FOR POLY LINES
/**AUTHOR: ANDROID CODING
 * YEAR: 2019
 * TITLE: HOW TO DRAW POLYLINE ON GOOGLE MAP IN ANDROID STUDIO | DRAW POLYLINE | ANDROID CODING
 * LINK: https://www.youtube.com/watch?v=rEhYTd4T__c
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: MASTER CODING
 * YEAR: 2020
 * TITLE: POLY LINES IN GOOGLE MAPS - [GOOGLE MAPS COURSE]
 * LINK: https://www.youtube.com/watch?v=NOVacL7ZPrc
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: PROGLABS OFFICIAL
 * YEAR: 2019
 * TITLE: HOW TO DRAW POLYLINE BETWEEN TWO POINTS ON GOOGLE MAPS IN ANDROID || DRAW ROUTE BETWEEN TWO POINTS
 * LINK: https://www.youtube.com/watch?v=b5U8WZM45aY
 * PLATFORM: YOUTUBE
 */

//ATTRIBUTION TO GET STEPS FOR DIRECTIONS
/**AUTHOR: THEMESCODE
 * YEAR: 2020
 * TITLE: HOW TO CREATE GOOGLE MAPS API KEY FOR FREE (EASY STEPS BY STEPS INSTRUCTIONS)
 * LINK: https://www.youtube.com/watch?v=OGTG1l7yin4
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: CODE WITH AP
 * YEAR: 2019
 * TITLE: USING GOOGLE MAP API IN ANDROID STUDIO APPLICATION
 * LINK: https://www.youtube.com/watch?v=Gcv2orQSMYA
 * PLATFORM: YOUTUBE
 */


//ATTRIBUTION TO GET MARKERS
/**AUTHOR: ANDROID CODING
 * YEAR: 2019
 * TITLE: HOW TO ADD MARKER ON GOOGLE MAP IN ANDROID STUDIO | ADDMARKER | ANDROID CODING
 * LINK: https://www.youtube.com/watch?v=2ppri1ovIQA
 * PLATFORM: YOUTUBE
 */

/**AUTHOR: GADGETS AND TECHNICAL FIELD ANDROID TECH
 * YEAR: 2019
 * TITLE: HOW TO ADD CUSTOM MARKER IN GOOGLE MAPS IN ANDROID
 * LINK: https://www.youtube.com/watch?v=26bl4r3VtGQ
 * PLATFORM: YOUTUBE
 */