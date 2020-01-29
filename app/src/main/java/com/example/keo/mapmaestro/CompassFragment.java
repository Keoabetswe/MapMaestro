package com.example.keo.mapmaestro;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompassFragment extends Fragment implements SensorEventListener
{
    // device sensor manager
    private android.hardware.SensorManager SensorManager;

    // define the compass picture that will be use
    private ImageView ivCompass;

    // record the angle turned of the compass picture
    private float DegreeStart = 0f;
    TextView tvDegree;

    public CompassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_compass, container, false);

        ivCompass = v.findViewById(R.id.compass_image);

        // TextView that will display the degree
        tvDegree = v.findViewById(R.id.tvDegree);

        // initialize your android device sensor capabilities
        SensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

        return v;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // to stop the listener and save battery
        SensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // code for system's orientation sensor registered listeners
        SensorManager.registerListener(this, SensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // get angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvDegree.setText("Heading: " + Float.toString(degree) + " degrees");
        // rotation animation - reverse turn degree degrees
        RotateAnimation ra = new RotateAnimation(
                DegreeStart,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // set the compass animation after the end of the reservation status
        ra.setFillAfter(true);
        // set how long the animation for the compass image will take place
        ra.setDuration(210);
        // Start animation of compass image
        ivCompass.startAnimation(ra);
        DegreeStart = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // not in use
    }

}
