package com.example.keo.mapmaestro;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class TripHistoryFragment extends Fragment
{
    private static final String TAG = "TripHistoryFragment";
    ListView listViewTripHistory;
    ArrayList<String> tripHistoryList;
    ArrayAdapter<String> adapter;
    ArrayList<String> list = new ArrayList<>();
    DatabaseReference databaseReference;
    String [] trips = {"Centurion","Pretoria","Midrand"};

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;
    private String userID;
    List<String> itemlist;

    public TripHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_trip_history, container, false);

        //Authentication
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference("TripHistory");

        listViewTripHistory = v.findViewById(R.id.TripHistoryListView);

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        itemlist = new ArrayList<>();


        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out");
                }
                // ...
            }
        };

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                showData(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return  v;
    }


    private void showData(DataSnapshot dataSnapshot)
    {
        try
        {
            for(DataSnapshot ds : dataSnapshot.getChildren())
            {
                String key = ds.getKey();
                String start = ds.child("startingPoint").getValue(String.class);
                String des = ds.child("destination").getValue(String.class);
                String date = ds.child("tripDate").getValue(String.class);
                itemlist.add(start);
                itemlist.add(des);
                itemlist.add(date);

                Log.v(TAG, "Trip History starting point: " + start);
                Log.v(TAG, "Trip History destination: " + des);

                adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,itemlist);
                listViewTripHistory.setAdapter(adapter);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), "Trip history Listview Error: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void toastMessage(String message){
        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }
}
