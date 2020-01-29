package com.example.keo.mapmaestro;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
//himport com.firebase.client.Firebase;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment
{
    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;
    Button btnSave;
    ArrayAdapter<String> adapter;
    ListView listviewProfile;
    EditText etName, etSurname, etEmail, etPassword;
    String name, surname, email, password;
    private String userID;

    public ProfileFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        //Authentication
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        etName = v.findViewById(R.id.etProfileName);
        etSurname = v.findViewById(R.id.etProfileSurname);
        etEmail = v.findViewById(R.id.etProfileEmail);
        etPassword = v.findViewById(R.id.etProfilePassword);
        btnSave = v.findViewById(R.id.btnProfileSave);


        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                updateProfile();
            }
        });

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

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
                UserProfile uInfo = new UserProfile();
                uInfo.setName(ds.child(userID).getValue(User.class).getName()); //set the name
                uInfo.setSurname(ds.child(userID).getValue(User.class).getSurname()); //set the email
                uInfo.setEmail(ds.child(userID).getValue(User.class).getEmail()); //set the email
                uInfo.setPassword(ds.child(userID).getValue(User.class).getPassword()); //set the email

                //display all the information
                Log.d(TAG, "showData: name: " + uInfo.getName());
                Log.d(TAG, "showData: surname: " + uInfo.getSurname());
                Log.d(TAG, "showData: email: " + uInfo.getEmail());
                Log.d(TAG, "showData: password: " + uInfo.getPassword());

                etName.setText(uInfo.getName());
                etSurname.setText(uInfo.getSurname());
                etEmail.setText(uInfo.getEmail());
                etPassword.setText(uInfo.getPassword());
            }
        }
        catch (DatabaseException e)
        {
            toastMessage("Profile error: " + e);
        }
    }

    public void updateProfile()
    {
        name = etName.getText().toString();
        surname = etSurname.getText().toString();
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(getActivity(), "Fill in all Fields!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //updates user profile
                User currentUser = new User(name, surname, email, password);
                dbRef.child("Users").child(userID).setValue(currentUser);
                toastMessage("Profile Updated!");
            }
        }

        user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Log.d(TAG, "updateEmail: successful");
                }
            }
        });

        user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Log.d(TAG, "updatePassword: successful");
                }
            }
        });
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

    private void toastMessage(String message){Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }
}
