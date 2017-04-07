package com.example.austin.inthemood;

import android.app.Activity;
import android.content.Intent;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Display locations on a map
 * Needs a Android Google Maps API key in AndroidManifest.xml
 *
 * Bugs: Needs to have location enabled before launching.
 *
 * TODO: Test MyFriends and Nearby Moods
 *
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {
    private final String FILENAME = "file.sav";
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private DataController controller;
    private Location location;
    private LocationController locationController;
    private String triggerFilter;
    private String emotionFilter;
    private int lastWeekFilter;
    private String launchedFrom;
    private ArrayList<Mood> moodList = new ArrayList<>();
    private HashMap<String, Float> hexColorToHUE = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        buildHashMap();

        // check what the map should display
        triggerFilter = getIntent().getStringExtra("trigger");
        emotionFilter = getIntent().getStringExtra("emotion");
        lastWeekFilter = getIntent().getIntExtra("lastweek", -1);
        launchedFrom = getIntent().getStringExtra("activity");

        loadFromFile();
        if (launchedFrom.equals("MyMoods")) {
            moodList = controller.getCurrentUser().getMyMoodsList();
            filterMoods();
        }

        if (launchedFrom.equals("MyFriends")) {
            moodList = getFriendsMoods();
            filterMoods();
        }

        if (launchedFrom.equals("MainUser")) {
            locationController = new LocationController(mGoogleApiClient, MapActivity.this);
            locationSetup();
            mGoogleApiClient = locationController.getGoogleApiClient();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationController != null) {
            locationController.connectGoogleApiClient();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationController != null) {
            if (locationController.googleApiClientConnected()) {
                locationController.startLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationController != null) {
            if (locationController.googleApiClientConnected()) {
                locationController.stopLocationUpdates();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationController != null) {
            locationController.disconnectGoogleApiClient();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationController.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        locationController.setCanGetLocation(true);
                        locationController.startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        locationController.setCanGetLocation(false);
                }
        }
    }


    /**
     * Take the Mood hex colors and have a color for MakerOption objects
     * that are place in a GoogleMap
     *
     * TODO change the mood colour with #cecece to some orange colour
     */
    private void buildHashMap() {
        hexColorToHUE.put("#f0391c", BitmapDescriptorFactory.HUE_RED);
        hexColorToHUE.put("#cecece", BitmapDescriptorFactory.HUE_ORANGE);
        hexColorToHUE.put("#9ae343", BitmapDescriptorFactory.HUE_GREEN);
        hexColorToHUE.put("#8383a9", BitmapDescriptorFactory.HUE_AZURE);
        hexColorToHUE.put("#e8ef02", BitmapDescriptorFactory.HUE_YELLOW);
        hexColorToHUE.put("#03acca", BitmapDescriptorFactory.HUE_CYAN);
        hexColorToHUE.put("#cd00ff", BitmapDescriptorFactory.HUE_MAGENTA);
        hexColorToHUE.put("#ff006c", BitmapDescriptorFactory.HUE_ROSE);
    }

    /**
     * check the filtering flags use the data controller to give back a filtered list.
     */
    private void filterMoods() {
        if (emotionFilter != null)
            moodList = controller.filterByMood(emotionFilter, moodList);

        if (lastWeekFilter != -1)
            moodList = controller.filterByWeek(moodList);

        if (triggerFilter != null)
            moodList = controller.filterByTrigger(triggerFilter, moodList);
    }

    private void locationSetup() {
        if (!locationController.checkLocationPermission()) {
            locationController.requestLocationPermission();
        }
    }

    /**
     * Get the List of Users that the current user follows
     *
     * @return
     */
    private ArrayList<Mood> getFriendsMoods() {
        ArrayList<User> followingList = new ArrayList<>();
        ArrayList<Mood> sortedFollowingMoods = new ArrayList<>();

        for (int i = 0; i < controller.getCurrentUser().getMyFollowingList().size(); i++){
            User user = controller.getElasticSearchUser(controller.getCurrentUser().getMyFollowingList().get(i));
            if (user != null) {
                followingList.add(user);
            }
        }

        for (int i = 0; i < followingList.size(); i++){
            ArrayList<Mood> followedUserMoods = followingList.get(i).getMyMoodsList();

            //if the followed user has moods, find his most recent mood and display it. If not,
            //only display his name
            if (followedUserMoods.size() > 0) {
                followedUserMoods = controller.sortMoodsByDate(followedUserMoods);
                sortedFollowingMoods.add(followedUserMoods.get(0));
            }
        }

        return sortedFollowingMoods;
    }


    /**
     * Make MarkerOptions from an ArrayList of Moods for displaying on a GoogleMap
     * @param moods ArrayList of Mood objects
     * @return ArrayList of MakerOption objects
     */
    private ArrayList<MarkerOptions> makeMarkers(ArrayList<Mood> moods) {
        ArrayList<MarkerOptions> list = new ArrayList<>();

        for (Mood mood : moods) {
            MarkerOptions markerOption = new MarkerOptions();

            if (mood.getLatLng() == null) {
                continue; // only display moods with locations
            }
            markerOption.position(mood.getLatLng());

            // don't need titles for user moods or moods within 5km
            if (launchedFrom.equals("MyFriends"))
                markerOption.title(mood.getOwnerName() + " felt " + mood.getMoodName() + " on " + new SimpleDateFormat("MMM dd, yyyy").format(mood.getMoodDate()));
            else
                markerOption.title("Felt " + mood.getMoodName() + " on " + new SimpleDateFormat("MMM dd, yyyy").format(mood.getMoodDate()));

            float iconColor = hexColorToHUE.get(mood.getColorHexCode());

            markerOption.icon(BitmapDescriptorFactory.defaultMarker(iconColor));

            list.add(markerOption);
        }
        return list;
    }

    /**
     * Add MarkerOptions to a GoogleMap
     * @param list ArrayList of MarkerOptions
     * @param googleMap an initialized GoogleMap
     */
    private void addMarkers(ArrayList<MarkerOptions> list, GoogleMap googleMap) {
        if (list.size() == 0) {
            return;
        }
        mMap = googleMap;

        for (MarkerOptions option : list) {
            mMap.addMarker(option);
        }

        // set camera to last mood should be sorted with the most recent first
        if (launchedFrom.equals("UserMain")) {
            LatLng latLng = LocationController.locationToLatLng(location);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(list.get(0).getPosition()));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (launchedFrom.equals("MainUser")) {
            if (locationController.checkLocationPermission()) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            addMarkers(makeMarkers(moodList), mMap);
        }

        mMap.setOnMyLocationButtonClickListener(this);

    }

    //Load data controller
    private void loadFromFile() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();

            Type objectType = new TypeToken<DataController>() {}.getType();
            controller = gson.fromJson(in, objectType);

        } catch (FileNotFoundException e) {
            User firstUser = new User("admin", "admin");
            controller = new DataController(firstUser);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * When this activity is launched from "MyMoods" we need to get the location when the
     * button is pressed. Failing to do so will not give enough time for the Google API object
     * to initialize in the LocationController.
     * @return
     */
    @Override
    public boolean onMyLocationButtonClick() {
        location = locationController.getCurrentLocation();

        moodList = controller.getNearMoods(location);
        if (moodList.size() == 0) {
            Toast.makeText(MapActivity.this, "Cannot find any Moods", Toast.LENGTH_LONG).show();
            return false;
        }
        addMarkers(makeMarkers(moodList), mMap);
        return true;
    }
}

