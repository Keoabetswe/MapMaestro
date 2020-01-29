package com.example.keo.mapmaestro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity
{
    TextView tvMap, tvMaestro; //app name
    ImageView ivPickerLogo; //app image
    private Animation animMoveLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        tvMap = (TextView) findViewById(R.id.tvSplashScreen1);
        tvMaestro = (TextView) findViewById(R.id.tvSplashScreen2);
        ivPickerLogo = (ImageView) findViewById(R.id.ivLogo);

        Animation anime = AnimationUtils.loadAnimation(this, R.anim.splash_transition);

        tvMap.startAnimation(anime);
        tvMaestro.startAnimation(anime);

        animMoveLogo = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        ivPickerLogo.startAnimation(animMoveLogo);

        //opens the login activity after the splash screen
        final Intent i = new Intent(this, Login.class);
        Thread timer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(5000); //delays for 5 seconds
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
    }
}