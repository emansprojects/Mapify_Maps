package com.vc.mapify.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vc.mapify.Activity.DirectionActivity;
import com.vc.mapify.R;
import com.vc.mapify.SavedLocationInterface;
import com.vc.mapify.SavedPlaceModel;
import com.vc.mapify.Utility.LoadingDialog;
import com.vc.mapify.databinding.FragmentSavedPlacesBinding;
import com.vc.mapify.databinding.SavedItemLayoutBinding;

import java.util.ArrayList;

//CLASS TO SHOW PLACES THAT THE USER HAS SAVED
public class SavedPlacesFragment extends Fragment implements SavedLocationInterface {

    //VARIABLES DECLARED
    private FragmentSavedPlacesBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<SavedPlaceModel> savedPlaceModelArrayList;
    private LoadingDialog loadingDialog;
    private FirebaseRecyclerAdapter<String, ViewHolder> firebaseRecyclerAdapter;
    private SavedLocationInterface savedLocationInterface;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //BIND DATA
        binding = FragmentSavedPlacesBinding.inflate(inflater, container, false);
        savedLocationInterface = this;
        //GET CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();
        savedPlaceModelArrayList = new ArrayList<>();

        //SET TITLE TO SAVED PLACES
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Saved Places");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //DISPLAY DATA IN THE FORM OF A LOADING DIALOG
        loadingDialog = new LoadingDialog(requireActivity());
        binding.savedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(binding.savedRecyclerView);
        getSavedPlaces();
    }


    //GET PLACES THAT THE USER HAS SAVED
    private void getSavedPlaces() {
        loadingDialog.startLoading();

        //DATABASE REFERENCE AND GET THE CURRENT USER
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseAuth.getUid()).child("Saved Locations");

        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, String.class).build();

        //DISPLAY DATA IN THE FORM OF A RECYCLER VIEW
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull String savePlaceId) {

                //GET DATABASE REFERENCE AND PLACE DATA
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Places").child(savePlaceId);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //IF THE PLACE EXISTS
                        if (snapshot.exists()) {

                            //DISPLAY THE PLACE DATA
                            SavedPlaceModel savedPlaceModel = snapshot.getValue(SavedPlaceModel.class);
                            holder.binding.setSavedPlaceModel(savedPlaceModel);
                            holder.binding.setListener(savedLocationInterface);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }


            //COMBINE DATA AND THE LAYOUT
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                SavedItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                        R.layout.saved_item_layout, parent, false);
                return new ViewHolder(binding);
            }
        };

        //STOP LOADING DIALOG
        binding.savedRecyclerView.setAdapter(firebaseRecyclerAdapter);
        loadingDialog.stopLoading();
    }

    //GET DATA
    @Override
    public void onResume() {
        super.onResume();
        firebaseRecyclerAdapter.startListening();
    }

    //PAUSE GETTING DATA
    @Override
    public void onPause() {
        super.onPause();
        firebaseRecyclerAdapter.stopListening();
    }


    //PROVIDES THE USER WITH DATA TO GET DIRECTIONS WHEN THEY CLICK ON THE PLACE
    @Override
    public void onLocationClick(SavedPlaceModel savedPlaceModel) {

        //GETS THE PLACE'S LATITUDE AND LONGITUDE AND LOCATION
        if (savedPlaceModel.getLat() != null && savedPlaceModel.getLng() != null) {
            Intent intent = new Intent(requireContext(), DirectionActivity.class);
            intent.putExtra("placeId", savedPlaceModel.getPlaceId());
            intent.putExtra("lat", savedPlaceModel.getLat());
            intent.putExtra("lng", savedPlaceModel.getLng());

            startActivity(intent);

        } else {
            //DISPLAY ERROR
            Toast.makeText(requireContext(), "Location Not Found", Toast.LENGTH_SHORT).show();
        }

    }

    //COMBINE DATA AND SHOW
    public class ViewHolder extends RecyclerView.ViewHolder {
        private SavedItemLayoutBinding binding;

        public ViewHolder(@NonNull SavedItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}