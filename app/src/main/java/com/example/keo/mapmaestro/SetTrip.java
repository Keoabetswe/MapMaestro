package com.example.keo.mapmaestro;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.keo.mapmaestro.Models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.GeoApiContext;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;


import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Modules.DirectionFinderListener;
import Modules.Route;

public class SetTrip extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, DirectionFinderListener
{
    private static final String TAG = "SetTrip";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final float DEFAULT_ZOOM = 15f;
    public static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    private static final int Nearby_Place_Request = 1;
    private static final int LOCATION_REQUEST = 500;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    //widgets
    private AutoCompleteTextView etOrigin;
    private AutoCompleteTextView etDestination;
    ImageView ivGPS, ivSetTripDirections;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private GoogleApiClient mGoogleApiClient;
    ArrayList<LatLng> listPoints;
    //LatLng origin, dest;
    TextView tvDistance;
    private GeoApiContext geoApiContext;

    //SharedPreferences
    SharedPreferences getData; //for mode of transport

    //mode of transport & unit types
    String mode;
    String units;

    public SetTrip()
    {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_trip);

        //Authentication
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference("TripHistory");

        tvDistance = findViewById(R.id.tvDistance);

        //adds back button on actionbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etOrigin = findViewById(R.id.etStartingPoint);
        etDestination = findViewById(R.id.etTripDestination);
        ivGPS = findViewById(R.id.ivGPS);
        ivSetTripDirections = findViewById(R.id.ivSetTrip);
        listPoints = new ArrayList<>();

        //Location search onClick Listener
        etOrigin.setOnItemClickListener(mAutocompeteItemClickListener);
        etDestination.setOnItemClickListener(mAutocompeteItemClickListener);

        //asks the user for location permission
        getLocationPermission();

        //gets current settings
        currentSettings();

        //calls settings method
        SettingsFragment settings = new SettingsFragment();
        String units = settings.unitText;

        ivGPS.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: clicked gps icon");
                //returns to current location
                getDeviceLocation();
            }
        });

        ivSetTripDirections.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendRequest();
            }
        });
    }


    private void initMap()
    {
        Log.d(TAG, "InitMap: initializing map");
        SupportMapFragment setTripMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        setTripMap.getMapAsync(SetTrip.this);

        if(geoApiContext == null)
        {
            geoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.directionsApiKey)).build();
        }
    }

    private void init()
    {
        Log.d(TAG, "init: Initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        etOrigin.setAdapter(mPlaceAutocompleteAdapter);
        etDestination.setAdapter(mPlaceAutocompleteAdapter);

        etOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)
                {
                    //Executes the method for searching for the starting point
                    geoLocateStartingPoint();
                }
                return false;
            }
        });

        etDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)
                {
                    //Executes the method for searching for the destination
                    geoLocateDestination();
                }
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d(TAG, "onMapReady: Map is ready");
        Toast.makeText(this, "Loading Map!", Toast.LENGTH_SHORT).show();
        gMap = googleMap;

        if (mLocationPermissionGranted)
        {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            gMap.setMyLocationEnabled(true); //enables my location
            gMap.getUiSettings().setMyLocationButtonEnabled(false); //disables the locate me button
            gMap.getUiSettings().setCompassEnabled(false); //disables the built-in compass

            init();
        }
    }

    private void geoLocateStartingPoint()
    {
        Log.d(TAG, "geoLocate starting point: geolocating");

        String searchStartingPoint = etOrigin.getText().toString();


        Geocoder geocoder = new Geocoder(SetTrip.this);
        List<Address> list = new ArrayList<>();

        try
        {
            list = geocoder.getFromLocationName(searchStartingPoint, 1);
        }
        catch (IOException e)
        {
            Log.d(TAG,"geoLocate starting point: IOException: " + e.getMessage());
        }

        if(list.size() > 0)
        {
            Address addressStartingPoint = list.get(0);

            Log.d(TAG,"geoLocate starting point: found a location: " + addressStartingPoint.toString());

            moveCamera(new LatLng(addressStartingPoint.getLatitude(), addressStartingPoint.getLongitude()),
                    DEFAULT_ZOOM, addressStartingPoint.getAddressLine(0));

        }
    }

    private void geoLocateDestination()
    {
        Log.d(TAG,"geoLocate destination: geolocating");
        String searchDestination = etDestination.getText().toString();

        //MapFragment,this
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> list = new ArrayList<>();

        try
        {
            list = geocoder.getFromLocationName(searchDestination, 1);
        }
        catch (IOException e)
        {
            Log.d(TAG, "geoLocate destination: IOException: " + e.getMessage());
        }

        if(list.size() > 0)
        {
            Address addressDestination = list.get(0);

            Log.d(TAG, "geoLocate destination: found a location: " + addressDestination.toString());

            moveCamera(new LatLng(addressDestination.getLatitude(), addressDestination.getLongitude()),
                    DEFAULT_ZOOM, addressDestination.getAddressLine(0));
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //when picker is on device location do not display it
        if(!title.equals("Device Location"))
        {
            //reset markers when 2 already
            if(listPoints.size() == 2)
            {
                listPoints.clear();
                gMap.clear();
            }
            //create marker
            MarkerOptions options = new MarkerOptions();
            options.position(latLng);

            if(listPoints.size() == 1)
            {
                //add first marker
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            else
            {
                //add second marker
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                //place distance & duration things HERE!!!!!!!!!!!!!!!!!!!!!
            }
            gMap.addMarker(options);
        }

        //hides keyboard after search
        hideSoftKeyBoardStarting();
        hideSoftKeyBoardDestination();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "OnComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "Device Location");
                        } else {
                            Log.d(TAG, "OnComplete: current location is null");
                            Toast.makeText(SetTrip.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0)
                {
                    for (int x = 0; x < grantResults.length; x++)
                    {
                        if(grantResults[x] != PackageManager.PERMISSION_GRANTED)
                        {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");

                    //initialize map
                    initMap();
                }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Nearby_Place_Request)
        {
            if(requestCode == RESULT_OK)
            {
                Place place = PlacePicker.getPlace(data, this);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    //-------------------------- Google places API autocomplete suggestions --------------------------

    private AdapterView.OnItemClickListener mAutocompeteItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            hideSoftKeyBoardStarting();
            hideSoftKeyBoardDestination();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>()
    {
        @Override
        public void onResult(@NonNull PlaceBuffer places)
        {
            if(!places.getStatus().isSuccess())
            {
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final  Place place = places.get(0);

            try
            {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setAttributions(place.getAttributions().toString());
                mPlace.setId(place.getId());
                mPlace.setLatLng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsite(place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }
            catch (NullPointerException e)
            {
                Log.d(TAG, "onResult: NullPointerException: " + e.getMessage());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());

            //prevents memory leaks
            places.release();
        }
    };

    private void getLocationPermission()
    {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this,
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this,
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionGranted = true;
                initMap();
            }
            else
            {
                ActivityCompat.requestPermissions((Activity) this.getApplicationContext(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else
        {
            ActivityCompat.requestPermissions((Activity) this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void hideSoftKeyBoardStarting()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etOrigin.getWindowToken(), 0);
    }

    private void hideSoftKeyBoardDestination()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etDestination.getWindowToken(), 0);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /* --------------------------- Trip History --------------------------- */
    public void addTripHistory()
    {
        String startingPoint = "from: " + etOrigin.getText().toString();
        String destination = "to: " + etDestination.getText().toString();

        //get current trip date
        Calendar calendar = Calendar.getInstance();
        String tripDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

        Log.d(TAG,"addToTripHistory: ");
        if(!startingPoint.isEmpty() && !destination.isEmpty())
        {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null)
            {
                //stores trip history data
                UserTripHistory userTripHistory = new UserTripHistory(startingPoint, destination, tripDate);

                dbRef.child(FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid())
                        .push()
                        .setValue(userTripHistory).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            // If bookmark up succeeds
                            Log.w(TAG, "writeToTripHistory: Successful", task.getException());
                            Toast.makeText(SetTrip.this, "Added to History!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            // If bookmark up fails
                            Log.w(TAG, "writeToTripHistory: failure", task.getException());
                            Toast.makeText(SetTrip.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        else
        {
            Log.d(TAG, ": tripNotAdded: ");
            //Toast.makeText(SetTrip.this, "Trip history NOT Added", Toast.LENGTH_SHORT).show();
        }

    }

    /* --------------------------- Calculating Directions --------------------------- */
    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();

        if (origin.isEmpty())
        {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
        }
        if (destination.isEmpty())
        {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
        }

        if(listPoints.size() != 2)
        {
            Toast.makeText(SetTrip.this, "Enter a Valid location!", Toast.LENGTH_SHORT).show();
        }

        if(listPoints.size() == 2)
        {
            try {
                new DirectionFinder(this, origin, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //adds trip to history
            addTripHistory();
        }

    }

    //adds the back button on the to<-
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home) //returns to the map fragment
        {
            this.finish();
        }

        return true;
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(gMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarkers.add(gMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(gMap.addPolyline(polylineOptions));
            gMap.addPolyline(polylineOptions);
        }
    }

    private void currentSettings()
    {
        getData = this.getSharedPreferences("unitsPrefSettings",Context.MODE_PRIVATE);
        String units = getData.getString("units_text_key", "Metric");

        if(units.equals("Metric"))
        {
            tvDistance.setText("km");
        }
        else if(units.equals("Imperial"))
        {
            tvDistance.setText("mi");
        }
    }
}
