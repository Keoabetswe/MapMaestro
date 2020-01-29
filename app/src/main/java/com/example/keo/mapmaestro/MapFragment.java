package com.example.keo.mapmaestro;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keo.mapmaestro.Models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.io.IOException;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.example.keo.mapmaestro.SettingsFragment.SHARED_PREFS;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "MapFragment";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final float DEFAULT_ZOOM = 15f;
    public static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    private static final int Nearby_Place_Request = 1;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView ivGps;
    private ImageView ivMapType;
    private TextView tvFabBookmarkText;
    private TextView tvFabSetTripText;
    private TextView tvFabNearbyPlacesText;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private PlaceInfo mPlace;
    private Marker mMarker;
    ArrayList<LatLng> listPoints;

    //Floating action button
    FloatingActionButton fabBookmark, fabSetTrip, fabNearbyPlaces, fabOpen;
    Animation fab_open, fab_close, fab_rotate_clockwise, fab_rotate_anticlockwise;
    Boolean isOpen = false;

    private GoogleApiClient mGoogleApiClient;


    public MapFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        //widgets
        mSearchText = v.findViewById(R.id.etSearchInput);
        ivGps = v.findViewById(R.id.ivGPS);
        ivMapType = v.findViewById(R.id.ivMapType);

        //stores num of markers on map
        listPoints = new ArrayList<>();

        //floating action button
        fabOpen = v.findViewById(R.id.fab_main);
        fabBookmark = v.findViewById(R.id.fabBookmark);
        fabSetTrip = v.findViewById(R.id.fabSetTrip);
        fabNearbyPlaces = v.findViewById(R.id.fabNearbyPlaces);
        tvFabBookmarkText = v.findViewById(R.id.tvFabBookmark);
        tvFabNearbyPlacesText = v.findViewById(R.id.tvFabNearbyPlaces);
        tvFabSetTripText = v.findViewById(R.id.tvFabSetTrip);

        //opens and closes FAB  with animation
        fab_open = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close);
        fab_rotate_clockwise = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_rotate_clockwise);
        fab_rotate_anticlockwise = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_rotate_anticlockwise);

        //Location search onClick Listener
        mSearchText.setOnItemClickListener(mAutocompeteItemClickListener);

        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();

        //Map type setOnClick
        ivMapType.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mapType();
            }
        });

        //FAB setOnClicks
        fabOpen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openFab();
            }
        });
        fabBookmark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bookmarkLocation();
            }
        });

        fabSetTrip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //opens set trip
                Intent openSetTrip = new Intent(getActivity(), SetTrip.class);
                startActivity(openSetTrip);
            }
        });

        //asks the user for location permission
        getLocationPermission();

        return v;
    }
    private void init()
    {
        Log.d(TAG, "init: Initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getActivity(), mGoogleApiClient, LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent)
            {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        ||actionId == EditorInfo.IME_ACTION_DONE
                        ||keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        ||keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)
                {
                    //Execute out method for searching
                    geoLocate();
                }
                return false;
            }
        });

        ivGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: clicked gps icon");
                //returns to current location
                getDeviceLocation();
            }
        });

        fabNearbyPlaces.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try
                {
                    startActivityForResult(builder.build(getActivity()), Nearby_Place_Request);
                }
                catch (GooglePlayServicesRepairableException e)
                {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage());
                }
                catch (GooglePlayServicesNotAvailableException e)
                {
                    Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        //hides keyboard after search
        hideSoftKeyBoard();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Nearby_Place_Request)
        {
            if(requestCode == RESULT_OK)
            {
                Place place = PlacePicker.getPlace(data, getActivity());

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate()
    {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> list = new ArrayList<>();

        try
        {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch (IOException e)
        {
            Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if(list.size() > 0)
        {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM, address.getAddressLine(0));

        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

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
                            Toast.makeText(getActivity(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //when picker is on device location do not display it
        if(!title.equals("Device Location"))
        {
            //reset markers when 1 already
            if(listPoints.size() == 1)
            {
                listPoints.clear();
                gMap.clear();
            }

            //save marker point
            listPoints.add(latLng);

            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            gMap.addMarker(options);
        }

        //hides keyboard after search
        hideSoftKeyBoard();
    }

    private void initMap() {
        Log.d(TAG, "InitMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.displayMap);
        mapFragment.getMapAsync(MapFragment.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        Toast.makeText(getActivity(), "Loading Map!", Toast.LENGTH_SHORT).show();
        gMap = googleMap;

        if (mLocationPermissionGranted)
        {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            gMap.setMyLocationEnabled(true); //enables my location
            gMap.getUiSettings().setMyLocationButtonEnabled(false); //disables the locate me button
            gMap.getUiSettings().setCompassEnabled(false); //disable the built-in compass

            init();
        }
    }

    private void getLocationPermission()
    {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionGranted = true;
                initMap();
            }
            else
            {
                ActivityCompat.requestPermissions((Activity) getActivity().getApplicationContext(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else
        {
            ActivityCompat.requestPermissions((Activity) getActivity().getApplicationContext(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
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

    public void mapType()
    {
        PopupMenu menu = new PopupMenu(getActivity(), ivMapType);
        menu.getMenuInflater().inflate(R.menu.dialog_alert_map_type, menu.getMenu());

        //menu onClick listener
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.defaultMap:
                        if(gMap != null)
                        {
                            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            return true;
                        }
                        break;

                    case R.id.satelliteMap:
                        if(gMap != null)
                        {
                            gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
        menu.show();
    }


    //opens and close floating action button method
    public void openFab()
    {
        if(isOpen)
        {
            //widgets
            fabBookmark.startAnimation(fab_close);
            fabNearbyPlaces.startAnimation(fab_close);
            fabSetTrip.startAnimation(fab_close);

            //widget text
            tvFabBookmarkText.startAnimation(fab_close);
            tvFabNearbyPlacesText.startAnimation(fab_close);
            tvFabSetTripText.startAnimation(fab_close);

            //opens the FAB
            fabOpen.startAnimation(fab_rotate_anticlockwise);

            //unable to click widgets
            fabBookmark.setClickable(false);
            fabSetTrip.setClickable(false);
            fabNearbyPlaces.setClickable(false);

            //unable to click widget text
            tvFabBookmarkText.setClickable(false);
            tvFabNearbyPlacesText.setClickable(false);
            tvFabSetTripText.setClickable(false);

            isOpen = false;
        }
        else
        {
            //widgets
            fabBookmark.startAnimation(fab_open);
            fabNearbyPlaces.startAnimation(fab_open);
            fabSetTrip.startAnimation(fab_open);

            //widget text
            tvFabBookmarkText.startAnimation(fab_open);
            tvFabNearbyPlacesText.startAnimation(fab_open);
            tvFabSetTripText.startAnimation(fab_open);

            //closes the FAB
            fabOpen.startAnimation(fab_rotate_clockwise);

            //unable to click widgets
            fabBookmark.setClickable(true);
            fabSetTrip.setClickable(true);
            fabNearbyPlaces.setClickable(true);

            //unable to click widget text
            tvFabBookmarkText.setClickable(true);
            tvFabNearbyPlacesText.setClickable(true);
            tvFabSetTripText.setClickable(true);

            isOpen = true;
        }
    }

    private void hideSoftKeyBoard()
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }


    //-------------------------- Google places API autocompete suggestions --------------------------

    private AdapterView.OnItemClickListener mAutocompeteItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            hideSoftKeyBoard();

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

    @Override
    public void onPause()
    {
        super.onPause();

        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    //---------------------  floating action button ---------------------
    public void bookmarkLocation()
    {
        String search = mSearchText.getText().toString();
        if(search.isEmpty())
        {
            Toast.makeText(getActivity(), "Enter a location!", Toast.LENGTH_SHORT).show();
        }
        else if(listPoints.size() != 1)
        {
            Toast.makeText(getActivity(), "Enter a Valid location!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            try
            {
                //clear map and marker counter
                listPoints.clear();
                gMap.clear();

                Log.d(TAG, "bookmarkSearchedLocation: ");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null)
                {
                    String email = user.getEmail();
                    String location = mSearchText.getText().toString();

                    //store location data
                    UserBookmarks userBookmarks = new UserBookmarks(email, location);

                    FirebaseDatabase.getInstance().getReference("Bookmarks").child(FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getUid())
                            .push()
                            .setValue(userBookmarks).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                // If bookmark up succeeds
                                Log.d(TAG, "writeToBookmark: Successful", task.getException());

                                mSearchText.setText("");
                                Toast.makeText(getActivity(), "Bookmarked!", Toast.LENGTH_SHORT).show();

                            }
                            else
                            {
                                // If bookmark up fails
                                Log.d(TAG, "writeToBookmark: failure", task.getException());
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            catch (Exception e)
            {
                Toast.makeText(getActivity(), "Bookmark Error: " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
    
