package com.example.keo.mapmaestro;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import static android.widget.Toast.LENGTH_SHORT;

public class Login extends AppCompatActivity
{
    private static final String TAG = "Login";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    Button btnLogin;
    TextView tvRegisterHere;
    EditText etEmail, etPassword;
    String myEmail, myPassword;
    ProgressBar progressbarLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        //Authentication
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressbarLogin = findViewById(R.id.progressbarRegister);
        progressbarLogin.setVisibility(View.GONE);

        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);




        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    loginUser();
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "Login Error: " + e, Toast.LENGTH_LONG).show();
                }

            }
        });

        tvRegisterHere.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openRegister();
            }
        });
    }

    public void loginUser()
    {
        myEmail = etEmail.getText().toString().trim();
        myPassword = etPassword.getText().toString().trim();

        if(myEmail.isEmpty() || myPassword.isEmpty())
        {
            Toast.makeText(this, "Fill in all fields!", LENGTH_SHORT).show();
        }
        else
        {
            progressbarLogin.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(myEmail, myPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            progressbarLogin.setVisibility(View.GONE); //HIDE progress bar
                            if (task.isSuccessful())
                            {
                                // Sign in successful
                                Log.d(TAG, "SignIn with email: success" + task.isSuccessful());
                                Intent main = new Intent(Login.this, MainActivity.class);
                                finish();
                                startActivity(main);

                              // Toast.makeText(Login.this, "Login Successful! ", LENGTH_SHORT).show();
                            }
                            else
                            {
                                //Sign in fails
                                Log.w(TAG, "SignIn with email: failure", task.getException());
                                etPassword.setText(""); //clears incorrect password

                                Toast.makeText(Login.this, "Login failed!", LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }



    public void openRegister()
    {
        Intent openReg = new Intent(this, Register.class);
        startActivity(openReg);
    }




}
