package com.vc.mapify.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.vc.mapify.Activity.DirectionActivity;
import com.vc.mapify.Adapter.GooglePlaceAdapter;
import com.vc.mapify.Adapter.InfoWindowAdapter;
import com.vc.mapify.Constant.AllConstant;
import com.vc.mapify.Global;
import com.vc.mapify.GooglePlaceModel;
import com.vc.mapify.Model.GooglePlaceModel.GoogleResponseModel;
import com.vc.mapify.NearLocationInterface;
import com.vc.mapify.Permissions.AppPermissions;
import com.vc.mapify.PlaceModel;
import com.vc.mapify.R;
import com.vc.mapify.SavedPlaceModel;
import com.vc.mapify.Utility.LoadingDialog;
import com.vc.mapify.WebServices.RetrofitAPI;
import com.vc.mapify.WebServices.RetrofitClient;
import com.vc.mapify.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//CLASS THAT DISPLAYS THE MAP TO THE USER AND ALLOWS USERS TO SELECT A PLACE
public class HomeFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, NearLocationInterface {

    //VARIABLES DECLARED
    private FragmentHomeBinding binding;
    private GoogleMap mGoogleMap;
    private AppPermissions appPermissions;
    private boolean isLocationPermissionOk, isTrafficEnable;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private FirebaseAuth firebaseAuth;
    private Marker currentMarker;
    private LoadingDialog loadingDialog;
    private int radius = 5000;
    private RetrofitAPI retrofitAPI;
    private List<GooglePlaceModel> googlePlaceModelList;
    private PlaceModel selectedPlaceModel;
    private GooglePlaceAdapter googlePlaceAdapter;
    private InfoWindowAdapter infoWindowAdapter;
    private ArrayList<String> userSavedLocationId;
    private DatabaseReference locationReference, userLocationReference;

    public HomeFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //BINDS DATA
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        //GETS APP PERMISSIONS
        appPermissions = new AppPermissions();
        //GETS CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();
        //GETS THE LOADING DIALOG LAYOUT
        loadingDialog = new LoadingDialog(requireActivity());
        //GET PLACES DATA
        retrofitAPI = RetrofitClient.getRetrofitClient().create(RetrofitAPI.class);
        googlePlaceModelList = new ArrayList<>();
        userSavedLocationId = new ArrayList<>();
        //GETS DATABASE REFERENCE WITH CURRENT USER
        locationReference = FirebaseDatabase.getInstance().getReference("Places");
        userLocationReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseAuth.getUid()).child("Saved Locations");



        //DISPLAYS OPTIONS FOR THE USER TO CHANGE THE MAP TYPE
        binding.btnMapType.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.map_type_menu, popupMenu.getMenu());


            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {

                    //OPTION NORMAL
                    case R.id.btnNormal:
                        //CHANGE MAP TYPE TO NORMAL
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        //SET VALUE TO NORMAL
                        Global.mapType = "normal";
                        //INFORM USER
                        Toast.makeText(getActivity(), "Google Maps Selected", Toast.LENGTH_SHORT).show();
                        break;

                        //OPTION SATELLITE
                    case R.id.btnSatellite:
                        //CHANGE MAP TYPE TO SATELLITE
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        //SET VALUE
                        Global.mapType = "satellite";
                        //INFORM USER
                        Toast.makeText(getActivity(), "Satellite Map Selected", Toast.LENGTH_SHORT).show();
                        break;

                        //OPTION TERRAIN
                    case R.id.btnTerrain:
                        //CHANGE MAP TYPE TO TERRAIN
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        //INFORM USER
                        Toast.makeText(getActivity(), "Terrain Map Selected", Toast.LENGTH_SHORT).show();
                        //SET VALUE TO TERRAIN
                        Global.mapType = "terrain";
                        break;
                }
                return true;
            });

            //SHOW POP UP MENU
            popupMenu.show();
        });


        //ALLOWS USERS TO SELECT DISTANCE PREFERENCE
        binding.enableDistance.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view);
            //GETS THE MENU OPTIONS FOR DISTANCE TYPE
            popupMenu.getMenuInflater().inflate(R.menu.map_distance_menu, popupMenu.getMenu());

            //GETS DATABASE REFERENCE AND CURRENT USER
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Distance")
                    .child(firebaseAuth.getUid());


            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {

                    //OPTION KILOMETRES
                    case R.id.btnKilometres:

                        //SET VALUE TO KILOMETRES
                        Global.distance = "kilometres";
                        //SET VALUE TO KILOMETRES
                        databaseReference.setValue(Global.distance);
                        //NOTIFY USER
                        Toast.makeText(getActivity(), "Settings changed Successfully!", Toast.LENGTH_SHORT).show();

                        //GET DATABASE REFERENCE AND CURRENT USER
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Distance").child(firebaseAuth.getCurrentUser().getUid());
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    //GET VALUE FROM DATABASE OF WHAT THE USER'S DISTANCE TYPE IS SET TO
                                    String distanceModel = snapshot.getValue().toString().trim();

                                    //SET VALUE TO THE RETRIEVED VALUE
                                    Global.distanceVal = distanceModel;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;

                        //OPTION MILES
                    case R.id.btnMiles:

                        //SET VALUE TO MILES
                        Global.distance = "miles";
                        databaseReference.setValue(Global.distance);
                        //NOTIFY USER
                        Toast.makeText(getActivity(), "Settings changed Successfully!", Toast.LENGTH_SHORT).show();


                        //GETS DATABASE REFERENCE TO CHECK WHAT THE USER HAS SET THEIR PREFERRED DISTANCE TYPE TO
                        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Distance").child(firebaseAuth.getCurrentUser().getUid());
                        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    //GET THE VALUE
                                    String distanceModel = snapshot.getValue().toString().trim();

                                    //SET THE VALUE
                                    Global.distanceVal = distanceModel;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        break;
                }
                return true;
            });


            //DISPLAY POP UP MENU
            popupMenu.show();
        });

        //GET THE DATABASE REERENCE TO CHECK WHAT THE USER HAS SET THE DISTANCE TYPE TO
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Distance").child(firebaseAuth.getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    //GET VALUE
                    String distanceModel = snapshot.getValue().toString().trim();

                    //SET VALUE
                    Global.distanceVal = distanceModel;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //GETS THE USER'S CURRENT LOCATION
                binding.currentLocation.setOnClickListener(currentLocation -> getCurrentLocation());

                //DISPLAYS THE VARIOUS LANDMARKS AVAILABLE
        binding.placesGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

                if (checkedId != -1) {
                    PlaceModel placeModel = AllConstant.placesName.get(checkedId - 1);
                    selectedPlaceModel = placeModel;
                    getPlaces(placeModel.getPlaceType());
                }
            }
        });
        return binding.getRoot();
    }


    //METHOD TO SHOW THE DIFFERENT LAND MARKS AVAILABLE
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.homeMap);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        for (PlaceModel placeModel : AllConstant.placesName) {

            Chip chip = new Chip(requireContext());
            chip.setText(placeModel.getName());
            chip.setId(placeModel.getId());
            chip.setPadding(8, 8, 8, 8);
            chip.setTextColor(getResources().getColor(R.color.white, null));
            chip.setChipBackgroundColor(getResources().getColorStateList(R.color.mapify_green, null));
            chip.setChipIcon(ResourcesCompat.getDrawable(getResources(), placeModel.getDrawableId(), null));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);

            binding.placesGroup.addView(chip);


        }

        //DISPLAY IN RECYCLER VIEW
        setUpRecyclerView();
        getUserSavedLocations();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;


        //CHECK IF USER'S LOCATION IS PERMITTED
        if (appPermissions.isLocationOk(requireContext())) {
            isLocationPermissionOk = true;

            //SET UP MAP AND DISPLAY
            setUpGoogleMap();

            //GET PERMISSION FROM THE USER TO ACCESS THEIR LOCATION
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Near me required location permission to show you near by places")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestLocation();
                        }
                    })
                    .create().show();
        } else {
            requestLocation();
        }

    }

    //REQUEST ACCESS TO THEIR LOCATION
    private void requestLocation() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_BACKGROUND_LOCATION}, AllConstant.LOCATION_REQUEST_CODE);
    }

    //GETS PERMISSION AND SET UP MAP TO DISPLAY
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AllConstant.LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionOk = true;
                setUpGoogleMap();
            } else {
                isLocationPermissionOk = false;
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //SET UP MAP TO DISPLAY TO THE USER
    private void setUpGoogleMap() {

        //CHECK IF THE USER HAS GIVEN PERMISSION TO ACCESS LOCATION
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionOk = false;
            return;
        }
        //SET UP MAP
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setTiltGesturesEnabled(true);
        mGoogleMap.setOnMarkerClickListener(this::onMarkerClick);

        //UPDATE MAP
        setUpLocationUpdate();
    }

    //SET UP UPDATED MAP TO DISPLAY TO THE USER
    private void setUpLocationUpdate() {

        //GET DATA ON THE LOCATION
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //SHOW DISTANCE AND DETAILS FOR THE PLACE
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d("TAG", "onLocationResult: " + location.getLongitude() + " " + location.getLatitude());
                    }
                }
                super.onLocationResult(locationResult);
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        //UPDATE LOCATION DETAILS
        startLocationUpdates();


    }

    //GET THE UPDATED LOCATION OF THE USER
    private void startLocationUpdates() {

        //CHECKS IF THE USER HAS GIVEN PERMISSION TO ACCESS THEIR LOCATION
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionOk = false;
            return;
        }

        //USE LOCATION TO GET UPDATES
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //NOTIFY USER
                            Toast.makeText(requireContext(), "Location updated started", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        //GET CURRENT LOCATION
        getCurrentLocation();
    }

    //CODE ATTRIBUTION AT THE END
    //GETS THE USER'S CURRENT LOCATION
    private void getCurrentLocation() {

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        //CHECKS IF THE USER HAS GIVEN PERMISSION TO ACCESS THEIR LOCATION
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionOk = false;
            return;
        }

        //SHOW DETAILS ABOUT A PLACE IN THE INFO WINDOW
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currentLocation = location;
                infoWindowAdapter = null;
                infoWindowAdapter = new InfoWindowAdapter(currentLocation, requireContext());
                mGoogleMap.setInfoWindowAdapter(infoWindowAdapter);
                moveCameraToLocation(location);


            }
        });
    }


    //GETS LOCATION OF MARKERS
    private void moveCameraToLocation(Location location) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new
                LatLng(location.getLatitude(), location.getLongitude()), 17);

        //SHOW CURRENT LOCATION OF MARKERS
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet(firebaseAuth.getCurrentUser().getDisplayName());

        if (currentMarker != null) {
            //REMOVES A MARKER
            currentMarker.remove();
        }

        //CODE ATTRIBUTION AT THE END
        //SETS CURRENT MARKER
        currentMarker = mGoogleMap.addMarker(markerOptions);
        currentMarker.setTag(703);
        mGoogleMap.animateCamera(cameraUpdate);

    }


    //STOPS GETTING UPDATES
    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        //NOTIFIES USER
        Log.d("TAG", "stopLocationUpdate: Location Update stop");
    }

    //STOPS LOCATION UPDATES
    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null)
            stopLocationUpdate();
    }

    //GETS LOCATION UPDATES
    @Override
    public void onResume() {
        super.onResume();

        if (fusedLocationProviderClient != null) {

            //GETS UPDATES AND REMOVES CURRENT MARKER
            startLocationUpdates();
            if (currentMarker != null) {
                currentMarker.remove();
            }
        }
    }


    //GETS PLACES BY THE LANDMARK TYPE
    private void getPlaces(String placeName) {


        //CHECKS THAT THE USER HAS GIVEN PERMISSION TO ACCESS THEIR LOCATION
        if (isLocationPermissionOk) {

            loadingDialog.startLoading();

            //CODE ATTRIBUTION AT THE END
            //CONNECTS TO THE GOOGLE MAPS API TO GET THEIR LOCATION
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                    + "&radius=" + radius + "&type=" + placeName + "&key=" +
                    getResources().getString(R.string.API_KEY);


            //CODE ATTRIBUTION AT THE END
            //IF LOCATION IS FOUND
            if (currentLocation != null) {

                //SHOW NEARBY PLACES
                retrofitAPI.getNearByPlaces(url).enqueue(new Callback<GoogleResponseModel>() {
                    @Override
                    public void onResponse(@NonNull Call<GoogleResponseModel> call, @NonNull Response<GoogleResponseModel> response) {
                        Gson gson = new Gson();

                        //GET PLACES DATA AS JSON
                        String res = gson.toJson(response.body());
                        Log.d("TAG", "onResponse: " + res);
                        if (response.errorBody() == null) {
                            if (response.body() != null) {
                                if (response.body().getGooglePlaceModelList() != null && response.body().getGooglePlaceModelList().size() > 0) {

                                    googlePlaceModelList.clear();
                                    mGoogleMap.clear();

                                    //GET A LIST OF ALL THE POSSIBLE PLACES NEARBY
                                    for (int i = 0; i < response.body().getGooglePlaceModelList().size(); i++) {

                                        if (userSavedLocationId.contains(response.body().getGooglePlaceModelList().get(i).getPlaceId())) {
                                            response.body().getGooglePlaceModelList().get(i).setSaved(true);
                                        }
                                        googlePlaceModelList.add(response.body().getGooglePlaceModelList().get(i));
                                        addMarker(response.body().getGooglePlaceModelList().get(i), i);
                                    }

                                    //GETS PLACES AS A LIST
                                    googlePlaceAdapter.setGooglePlaceModels(googlePlaceModelList);

                                    //DISPLAY ERROR
                                } else if (response.body().getError() != null) {
                                    Snackbar.make(binding.getRoot(),
                                            response.body().getError(),
                                            Snackbar.LENGTH_LONG).show();
                                } else {
                                    mGoogleMap.clear();
                                    googlePlaceModelList.clear();
                                    //SET PLACES ON MAP
                                    googlePlaceAdapter.setGooglePlaceModels(googlePlaceModelList);
                                    radius += 1000;
                                    //SET RADIUS TO SHOW PLACES WITHIN A CERTAIN RADIUS AT A TIME
                                    Log.d("TAG", "onResponse: " + radius);
                                    //SHOW NAMES
                                    getPlaces(placeName);

                                }
                            }

                            //DISPLAY ERROR
                        } else {
                            Log.d("TAG", "onResponse: " + response.errorBody());
                            Toast.makeText(requireContext(), "Error : " + response.errorBody(), Toast.LENGTH_SHORT).show();
                        }

                        //STOP LOADING DATA
                        loadingDialog.stopLoading();

                    }

                    //DISPLAY ERROR TO FETCH DATA
                    @Override
                    public void onFailure(Call<GoogleResponseModel> call, Throwable t) {

                        //SHOW ERROR
                        Log.d("TAG", "onFailure: " + t);
                        loadingDialog.stopLoading();

                    }
                });
            }
        }

    }

    //CODE ATTRIBUTION AT THE END
    //ADDS MARKERS ON THE MAP FOR THE LANDMARKS
    private void addMarker(GooglePlaceModel googlePlaceModel, int position) {

        //GETS THE LOCATION OF THE LANDMARKS ON THE MAP AND PLACES MARKERS
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(googlePlaceModel.getGeometry().getLocation().getLat(),
                        googlePlaceModel.getGeometry().getLocation().getLng()))
                .title(googlePlaceModel.getName())
                .snippet(googlePlaceModel.getVicinity());
        markerOptions.icon(getCustomIcon());
        mGoogleMap.addMarker(markerOptions).setTag(position);
    }

    //CUSTOMISES THE MARKERS FOR THE LANDMARKS
    private BitmapDescriptor getCustomIcon() {

        //SET ICON
        Drawable background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location);
        //SET TINT COLOUR
        background.setTint(getResources().getColor(R.color.mapify_green, null));
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    //SHOWS PLACES IN A RECYCLER VIEW FOR THE USER TO SCROLL
    private void setUpRecyclerView() {

        //SETS THE RECYCLER VIEW
        binding.placesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.placesRecyclerView.setHasFixedSize(false);
        googlePlaceAdapter = new GooglePlaceAdapter(this);
        //ADDS DATA INTO THE RECYCLER VIEW
        binding.placesRecyclerView.setAdapter(googlePlaceAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();

        snapHelper.attachToRecyclerView(binding.placesRecyclerView);

        binding.placesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                //GETS THE DETAILS FOR THE PLACES THAT APPEAR IN THE RECYCLER VIEW
                int position = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (position > -1) {
                    GooglePlaceModel googlePlaceModel = googlePlaceModelList.get(position);

                    //ZOOMS IN AND SHOWS THE USER WHERE EXACTLY THE LANDMARK IS ON THE MAP
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(googlePlaceModel.getGeometry().getLocation().getLat(),
                            googlePlaceModel.getGeometry().getLocation().getLng()), 20));
                }
            }
        });

    }

    //GIVES THE USER DETAILS WHEN THEY CLICK ON A MARKER
    @Override
    public boolean onMarkerClick(Marker marker) {

        int markerTag = (int) marker.getTag();
        //SHOW MARKER DETAILS
        Log.d("TAG", "onMarkerClick: " + markerTag);
        binding.placesRecyclerView.scrollToPosition(markerTag);
        return false;
    }



    //SAVES THE PLACES THE USER IS INTERESTED IN
    @Override
    public void onSaveClick(GooglePlaceModel googlePlaceModel) {

        //THE USER CAN REMOVE A SAVED PLACE
        if (userSavedLocationId.contains(googlePlaceModel.getPlaceId())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove Place")
                    //NOTIFY USER
                    .setMessage("Are you sure to remove this place?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //REMOVE THE PLACE
                            removePlace(googlePlaceModel);
                        }
                    })

                    //DO NOT REMOVE PLACE
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    //SHOW THE USER THE DIALOG
                    .create().show();
        } else {
            //START LOADING TO REMOVE PLACE
            loadingDialog.startLoading();


            //GETS DATABASE REFERENCE FOR LOCATION
            locationReference.child(googlePlaceModel.getPlaceId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {

                        //GETS THE DATA OF THE PLACE SAVED
                        SavedPlaceModel savedPlaceModel = new SavedPlaceModel(googlePlaceModel.getName(), googlePlaceModel.getVicinity(),
                                googlePlaceModel.getPlaceId(), googlePlaceModel.getRating(),
                                googlePlaceModel.getUserRatingsTotal(),
                                googlePlaceModel.getGeometry().getLocation().getLat(),
                                googlePlaceModel.getGeometry().getLocation().getLng());

                        saveLocation(savedPlaceModel);
                    }

                    //GETS THE PLACE ID
                    saveUserLocation(googlePlaceModel.getPlaceId());

                    //GET INDEX AND STOP LOADING
                    int index = googlePlaceModelList.indexOf(googlePlaceModel);
                    googlePlaceModelList.get(index).setSaved(true);
                    googlePlaceAdapter.notifyDataSetChanged();
                    loadingDialog.stopLoading();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    //REMOVES A PLACE FROM THE SAVED PLACES LIST
    private void removePlace(GooglePlaceModel googlePlaceModel) {

        //REMOVES THE PLACE
        userSavedLocationId.remove(googlePlaceModel.getPlaceId());
        int index = googlePlaceModelList.indexOf(googlePlaceModel);
        googlePlaceModelList.get(index).setSaved(false);
        googlePlaceAdapter.notifyDataSetChanged();

        //NOTIFIES USER
        Snackbar.make(binding.getRoot(), "Place removed", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //REMOVES THE PLACE
                        userSavedLocationId.add(googlePlaceModel.getPlaceId());
                        googlePlaceModelList.get(index).setSaved(true);
                        googlePlaceAdapter.notifyDataSetChanged();

                    }
                })
                //DOES NOT REMOVE THE PLACE
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);

                        //KEEPS THE VALUE
                        userLocationReference.setValue(userSavedLocationId);
                    }
                    //SHOW MESSAGE
                }).show();

    }

    //SAVED THE PLACE THE USER WANTS TO SAVE
    private void saveUserLocation(String placeId) {

        //STORES ID FOR THE PLACE
        userSavedLocationId.add(placeId);
        //STORES THE REFERENCE
        userLocationReference.setValue(userSavedLocationId);
        //NOTIFIES USER PLACE IS SAVED
        Snackbar.make(binding.getRoot(), "Place Saved", Snackbar.LENGTH_LONG).show();
    }

    //GETS THE PLACES THE USER SAVED
    private void saveLocation(SavedPlaceModel savedPlaceModel) {
        locationReference.child(savedPlaceModel.getPlaceId()).setValue(savedPlaceModel);
    }


    //GIVES THE USER DIRECTIONS TO A PLACE
    @Override
    public void onDirectionClick(GooglePlaceModel googlePlaceModel) {

        //GETS THE PLACE ID
        String placeId = googlePlaceModel.getPlaceId();
        //GETS THE LATITUDE
        Double lat = googlePlaceModel.getGeometry().getLocation().getLat();
        //GETS THE LONGITUDE
        Double lng = googlePlaceModel.getGeometry().getLocation().getLng();

        //DISPLAYS THE DATA
        Intent intent = new Intent(requireContext(), DirectionActivity.class);
        intent.putExtra("placeId", placeId);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);

        startActivity(intent);

    }

    //GETS THE USER'S SAVED LOCATIONS
    private void getUserSavedLocations() {

        //DATABASE REFERENCE AND CURRENT USER
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseAuth.getUid()).child("Saved Locations");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        //GET THE USER'S SAVED PLACES
                        String placeId = ds.getValue(String.class);
                        //DISPLAY THE SAVED PLACES
                        userSavedLocationId.add(placeId);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}

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