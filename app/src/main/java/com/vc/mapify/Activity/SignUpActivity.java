package com.vc.mapify.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vc.mapify.Constant.AllConstant;
import com.vc.mapify.Permissions.AppPermissions;
import com.vc.mapify.R;
import com.vc.mapify.UserModel;
import com.vc.mapify.Utility.LoadingDialog;
import com.vc.mapify.databinding.ActivitySignUpBinding;

//CLASS TO SIGN UP FOR THE APP
public class SignUpActivity extends AppCompatActivity {

    //VARIABLES DECLARED
    private ActivitySignUpBinding binding;
    private LoadingDialog loadingDialog;
    private String email, username, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //BIND LAYOUT
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingDialog = new LoadingDialog(this);


        //GO TO THE BACK PRESSED METHOD
        binding.btnBack.setOnClickListener(view -> {
            onBackPressed();
        });

        //GO TO THE BACK PRESSED METHOD
        binding.txtLogin.setOnClickListener(view -> {
            onBackPressed();
        });

        //TAKE THE USER TO SIGN UP
        binding.btnSignUp.setOnClickListener(view -> {

            //CHECK ALL FIELDS ARE ENTERED
            if (areFieldReady()) {

                //SIGN UP
                signUp();
            }

        });


    }


    //CHECKS IF THE FIELDS ARE ALL ENTERED
    private boolean areFieldReady() {

        //GETS THE VALUES TO STRING
        username = binding.edtUsername.getText().toString().trim();
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        boolean flag = false;
        View requestView = null;

        //CHECKS IF USERNAME IS ENTERED
        if (username.isEmpty()) {
            binding.edtUsername.setError("Field is required");
            flag = true;
            requestView = binding.edtUsername;
            //CHECKS IS EMAIL IS ENTERED
        } else if (email.isEmpty()) {
            binding.edtEmail.setError("Field is required");
            flag = true;
            requestView = binding.edtEmail;
            //CHECKS IF PASSWORD IS ENTERED
        } else if (password.isEmpty()) {
            binding.edtPassword.setError("Field is required");
            flag = true;
            requestView = binding.edtPassword;
            //CHECKS IF PASSWORD COMPLEXITY IS CORRECT - LENGTH
        } else if (password.length() < 8) {
            binding.edtPassword.setError("Minimum 8 characters");
            flag = true;
            requestView = binding.edtPassword;
        }

        if (flag) {
            requestView.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    //METHOD TO SIGN UP THE USER
    private void signUp() {
        loadingDialog.startLoading();
        //GET CURRENT USER
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        //GETS DATABASE REFERENCE
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> singUp) {

                //IF USER HAS BEEN SIGNED UP WITH EMAIL AND PASSWORD, SAVE THEIR DETAILS
                if (singUp.isSuccessful()) {

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //SAVES THE USER DETAILS TO THE DATABASE
                            UserModel userModel = new UserModel(email,
                                    username, true);

                            //GIVES THE USER AN ID
                            databaseReference.child(firebaseAuth.getUid())
                                    .setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {

                                        //SENDS AN EMAIL VERIFICATION
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseAuth.getCurrentUser().sendEmailVerification()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    loadingDialog.stopLoading();
                                                    onBackPressed();
                                                }
                                            });
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }
        });
    }
}