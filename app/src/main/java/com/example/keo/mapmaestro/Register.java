package com.example.keo.mapmaestro;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity
{
    private static final String TAG = "Register";
    Button btnRegister;
    TextView tvLoginHere;
    EditText etName, etSurname, etEmail, etPassword, etConfirmPass;
    String myEmail, myName, mySurname, myPassword, myConfirmPassword;
    ProgressBar progressbarReg;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Authentication
        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        progressbarReg = findViewById(R.id.progressbarRegister);
        progressbarReg.setVisibility(View.GONE);

        btnRegister = findViewById(R.id.btnRegister);
        tvLoginHere = findViewById(R.id.tvLoginHere);

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                registerUser();
            }
        });

        tvLoginHere.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openLogin();
            }
        });
    }

    public void registerUser()
    {
        myName = etName.getText().toString().trim();
        mySurname = etSurname.getText().toString().trim();
        myEmail = etEmail.getText().toString().trim();
        myPassword = etPassword.getText().toString().trim();
        myConfirmPassword = etConfirmPass.getText().toString().trim();

        if(myName.isEmpty() || mySurname.isEmpty() || myEmail.isEmpty() || myPassword.isEmpty() || myConfirmPassword.isEmpty())
        {
            Toast.makeText(this, "Fill in all fields!", Toast.LENGTH_SHORT).show();
        }
        else if(!myPassword.equals(myConfirmPassword))
        {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();

            //clears the incorrect password text
            etPassword.setText("");
            etConfirmPass.setText("");
        }
        else
        {
            progressbarReg.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(myEmail, myPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            progressbarReg.setVisibility(View.GONE); //HIDE progress bar
                            if (task.isSuccessful())
                            {
                                // Sign in success
                                Log.d(TAG, "createUserWithEmail:success");

                                //stores user data
                                User user = new User(myName, mySurname, myEmail, myPassword);

                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        progressbarReg.setVisibility(View.GONE); //HIDE progress bar
                                        if(task.isSuccessful())
                                        {
                                            // Sign up successful
                                            Toast.makeText(Register.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                                            //creates user profile
                                            userProfile();

                                            //takes user to login screen
                                            Intent login = new Intent(Register.this, Login.class);
                                            startActivity(login);
                                        }
                                        else
                                        {
                                            // If sign up fails
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                            else
                            {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        }
    }

    //collects info of current user - display name & surname on nav drawer
    public void userProfile()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(myName).build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Log.d(TAG, "getCurrentUser: user profile updated");
                    }
                }
            });
        }
    }


    public void openLogin()
    {
        Intent login = new Intent(this, Login.class);
        startActivity(login);
    }
}
