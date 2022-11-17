package com.vc.mapify.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vc.mapify.Utility.LoadingDialog;
import com.vc.mapify.databinding.ActivityLoginBinding;

//CLASS TO ALLOW THE USER TO LOG INTO THE APP
public class LoginActivity extends AppCompatActivity {

    //VARIABLES DECLARED
    private ActivityLoginBinding binding;
    private String email, password;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //BINDING DATA
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingDialog = new LoadingDialog(this);

        //TAKES THE USER TO THE SIGN UP SCREEN
        binding.btnSignUp.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        //IF ALL FIELDS ARE ENTERED, RUN THE LOGIN METHOD
        binding.btnLogin.setOnClickListener(view -> {
            if (areFieldReady()) {
                login();
            }
        });
    }

    //LOGS USER INTO THE APP
    private void login() {
        loadingDialog.startLoading();
        //GETS CURRENT USER
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //CHECKS IF USER'S EMAIL AND PASSWORD EXISTS
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    //LOG USER INTO THE APP IF DETAILS EXIST
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
        }
        });
    }

    //CHECKS IF ALL FIELDS ARE ENTERED
    private boolean areFieldReady() {

        //GET VALUES TO STRING
        email = binding.edtEmail.getText().toString().trim();
        password = binding.edtPassword.getText().toString().trim();

        boolean flag = false;
        View requestView = null;

        //CHECKS IF EMAIL IS ENTERED
        if (email.isEmpty()) {
            binding.edtEmail.setError("Field is required");
            flag = true;
            requestView = binding.edtEmail;
            //CHECKS IF PASSWORD IS ENTERED
        } else if (password.isEmpty()) {
            binding.edtPassword.setError("Field is required");
            flag = true;
            requestView = binding.edtPassword;
            //CHECKS PASSWORD COMPLEXITY - LENGTH
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
}