package com.vc.mapify.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vc.mapify.Activity.LoginActivity;
import com.vc.mapify.Constant.AllConstant;
import com.vc.mapify.Global;
import com.vc.mapify.Permissions.AppPermissions;
import com.vc.mapify.R;
import com.vc.mapify.Utility.LoadingDialog;
import com.vc.mapify.databinding.FragmentSettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;
import java.util.Map;

//CLASS THAT CONTROLS THE USER'S SETTINGS AND IT'S FUNCTIONS
public class SettingsFragment extends Fragment {

    //VARIABLES DECLARED
    private FragmentSettingsBinding binding;
    private FirebaseAuth firebaseAuth;
    private LoadingDialog loadingDialog;
    private AppPermissions appPermissions;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //BINDS THE DATA
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        //GETS CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();
        //LOADING DIALOG
        loadingDialog = new LoadingDialog(getActivity());
        //GETS PERMISSIONS
        appPermissions = new AppPermissions();


        //ALLOWS USERS TO CHANGE THEIR DISTANCE TYPE FROM SETTINGS
        binding.cardDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //DISPLAY A POP UP MENU TO THE USER
                PopupMenu popupMenu = new PopupMenu(requireContext(), view);

                //GETS THE MENU OPTIONS TO DISPLAY IN THE POP UP MENU
                popupMenu.getMenuInflater().inflate(R.menu.map_distance_menu, popupMenu.getMenu());

                //DATABASE REFERENCE TO GET THE CURRENT USER
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Distance")
                        .child(firebaseAuth.getUid());

                //OPTIONS FOR THE USER TO SELECT FROM THE MENU
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {

                        //KILOMETRES OPTION
                        case R.id.btnKilometres:

                            //SETS THE VALUE TO KILOMETRES
                            Global.distanceType = "kilometres";
                            databaseReference.setValue(Global.distanceType);
                            //NOTIFY USER
                            Toast.makeText(getActivity(), "Settings changed Successfully!", Toast.LENGTH_SHORT).show();
                            break;

                            //OPTION MILES
                        case R.id.btnMiles:

                            //SETS THE VALUE TO MILES
                            Global.distanceType = "miles";
                            databaseReference.setValue(Global.distanceType);
                            //NOTIFY USER
                            Toast.makeText(getActivity(), "Settings changed Successfully!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                });


                //SHOW THE POP UP MENU
                popupMenu.show();
            }
        });


        //ALLOWS THE USER TO LOG OUT FROM THE APP
        binding.cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //GETS THE CURRENT USER AND SIGNS THEM OUT
                FirebaseAuth.getInstance().signOut();
                //NOTIFY USER
                Toast.makeText(getActivity(), "Successfully logged out!", Toast.LENGTH_SHORT).show();
                //RETURN TO LOGIN SCREEN
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        //ALLOWS USER TO CHANGE THEIR USERNAME
        binding.txtUsername.setOnClickListener(username -> {
            usernameDialog();
        });

        return binding.getRoot();
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //SETS TITLE AS SETTINGS
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");
        //DISPLAYS USER EMAIL
        binding.txtEmail.setText(firebaseAuth.getCurrentUser().getEmail());
        //DISPLAYS USERNAME
        binding.txtUsername.setText(firebaseAuth.getCurrentUser().getDisplayName());

    }

    //DISPLAYS DIALOG FOR THE USER TO CHANGE THEIR USERNAME
    private void usernameDialog() {

        //GETS THE LAYOUT FOR THE DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.username_dialog_layout, null, false);
        builder.setView(view);
        TextInputEditText edtUsername = view.findViewById(R.id.edtDialogUsername);

        //SET TITLE
        builder.setTitle("Edit Username");

        //IF THE USER CHOOSES TO SAVE USERNAME, CHANGE THE USERNAME
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //GETS THE USERNAME
                String username = edtUsername.getText().toString().trim();
                if (!username.isEmpty()) {

                    //UPDATE USERNAME
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();

                    //FIND CURRENT USER AND CHANGE THE USERNAME
                    firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                //GET DATABASE REFERENCE
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                Map<String, Object> map = new HashMap<>();

                                //CHANGE THE USERNAME AND SET THE VALUE TO THE NEW USERNAME
                                map.put("username", username);
                                databaseReference.child(firebaseAuth.getUid()).updateChildren(map);

                                //SET THE USERNAME AS THE NEW USERNAME AND DISPLAY IN SETTINGS
                                binding.txtUsername.setText(username);
                                Toast.makeText(getContext(), "Username is updated", Toast.LENGTH_SHORT).show();

                            } else {
                                //NOTIFY USER
                                Log.d("TAG", "onComplete: " + task.getException());
                                Toast.makeText(getContext(), "" + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    //NOTIFY USER TO FILL IN FIELD
                    Toast.makeText(getContext(), "Username is required", Toast.LENGTH_SHORT).show();
                }
            }
        })
                //IF THE USER CHOOSES TO CANCEL, RETURN TO SCREEN
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                //SHOW DIALOG
                .create().show();
    }
}