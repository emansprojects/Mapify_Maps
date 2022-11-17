package com.vc.mapify.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vc.mapify.R;
import com.vc.mapify.UserModel;
import com.vc.mapify.databinding.ActivityMainBinding;
import com.vc.mapify.databinding.NavDrawerLayoutBinding;
import com.vc.mapify.databinding.ToolbarLayoutBinding;

import de.hdodenhof.circleimageview.CircleImageView;

//CLASS TO INTEGRATE NAV BAR WITH THE APP
public class MainActivity extends AppCompatActivity {

    //VARIABLES DECLARED
    private NavDrawerLayoutBinding navDrawerLayoutBinding;
    private ActivityMainBinding activityMainBinding;
    private ToolbarLayoutBinding toolbarLayoutBinding;
    private FirebaseAuth firebaseAuth;
    private CircleImageView imgHeader;
    private TextView txtName, txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navDrawerLayoutBinding = NavDrawerLayoutBinding.inflate(getLayoutInflater());
        setContentView(navDrawerLayoutBinding.getRoot());
        activityMainBinding = navDrawerLayoutBinding.mainActivity;
        toolbarLayoutBinding = activityMainBinding.toolbar;

        //SETS TOOL BAR FROM LAYOUT IN THE ACTIVITY LAYOUT
        setSupportActionBar(toolbarLayoutBinding.toolbar);

        //GETS CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();

        //METHOD TO OPEN AND CLOSE THE NAV BAR
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                navDrawerLayoutBinding.navDrawer,
                toolbarLayoutBinding.toolbar,
                R.string.open_navigation_drawer,
                R.string.close_navigation_drawer
        );

        navDrawerLayoutBinding.navDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavController navController = Navigation.findNavController(this, R.id.fragmentContainer);
        NavigationUI.setupWithNavController(
                navDrawerLayoutBinding.navigationView,
                navController
        );

        View headerLayout = navDrawerLayoutBinding.navigationView.getHeaderView(0);

        //SETS IMAGE OF THE APP'S LOGO
        imgHeader = headerLayout.findViewById(R.id.imgHeader);
        //SETS USER'S NAME IN NAV BAR
        txtName = headerLayout.findViewById(R.id.txtHeaderName);
        //SETS USER'S EMAIL IN NAV BAR
        txtEmail = headerLayout.findViewById(R.id.txtHeaderEmail);

        //CALLS METHOD TO GET USER DATA
        getUserData();


    }

    //BACK PRESSED METHOD TO CLOSE NAV DRAWER
    @Override
    public void onBackPressed() {

        if (navDrawerLayoutBinding.navDrawer.isDrawerOpen(GravityCompat.START))
            navDrawerLayoutBinding.navDrawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    //METHOD TO GET THE USER'S DATA AND THE CURRENT USER
    private void getUserData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    //SETS THE USER'S EMAIL AND USERNAME IN THE NAV BAR
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    txtName.setText(userModel.getUsername());
                    txtEmail.setText(userModel.getEmail());


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}