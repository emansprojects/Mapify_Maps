package com.vc.mapify.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vc.mapify.Global;
import com.vc.mapify.R;

//GETS THE NUMBER OF TRIPS THE USER HAS DONE IN ORDER TO REWARD THEM WITH A COUPON
public class ProgressFragment extends Fragment {

    //VARIABLES DECLARED
    ProgressBar progressBar;
    TextView textView;
    private FirebaseAuth firebaseAuth;
    Button claim;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public ProgressFragment() {

    }


    public static ProgressFragment newInstance(String param1, String param2) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //GETS ELEMENTS FROM THE LAYOUT
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        //FINDS THE ELEMENTS
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.progressBarText);
        //GET CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();


        //DATABASE REFERENCE TO GET THE CURRENT USER AND THE NUMBER OF TRIPS THEY HAVE DONE
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Progress").child(firebaseAuth.getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //IF USER EXISTS AND THEIR TRIP DATA EXISTS
                if (snapshot.exists()) {

                    //COUNT THE NUMBER OF TRIPS THEY HAVE DONE
                    int distanceModel = (int) snapshot.getChildrenCount();

                    //SET THE VALUE TO THE NUMBER OF TRIPS DONE
                    Global.progress = distanceModel;
                    //SET THE MAXIMUM VALUE OF THE PROGRESS BAR TO 10
                    progressBar.setMax(10);
                    //SET THE INITIAL PROGRESS TO 0
                    progressBar.setProgress(0);
                    //SET THE PROGRESS BAR TO THE NUMBER OF TRIPS THE USER HAS DONE
                    progressBar.setProgress(Global.progress);
                    //SET THE TEXT OF THE PROGRESS BAR TO THE NUMBER OF TRIPS THE USER HAS DONE TO SHOW THE PERCENTAGE COMPLETE
                    textView.setText(Global.progress + " 0 %");

                    //IF THE NUMBER OF TRIPS DONE IS EQUAL TO 10
                    if(Global.progress == 10){
                        //DELETE THE TRIPS DONE
                        reference.removeValue();

                        //DISPLAY A DIALOG TO INFORM THE USER THEY HAVE EARNED A PROTEA HOTEL COUPON
                        Dialog dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.dialog_trip_coupon);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        dialog.getWindow().setWindowAnimations(R.style.AnimationsForDialog);

                        claim = dialog.findViewById(R.id.claimCoupon);

                        //DIRECTS THE USER TO THE PROTEA HOTEL WEBSITE WHERE THEY CAN CLAIM THEIR COUPON
                        claim.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getActivity(), "Opening browser. . .", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://protea.marriott.com/offers/"));
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });

                        //SHOW DIALOG
                        dialog.show();
                    }

                }else{
                    //SET PROGRESS TO ZERO IF THEIR IS NO TRIPS DONE
                    Global.progress = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }
}