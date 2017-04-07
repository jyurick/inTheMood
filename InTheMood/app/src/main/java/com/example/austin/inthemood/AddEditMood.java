package com.example.austin.inthemood;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;


/**
 * This activity handles all of creating, editing, and deletion of a User's Mood.
 * The activity knows if it is working with an existing mood if it receives a non-
 * negative (and thus valid) Mood index from from an intent extra. If working with
 * an existing Mood, the User can change the fields of the Mood (except the date,
 * which is set in real time for the User), and then save those changes with the
 * Save button, or delete the Mood with the Delete button. If no existing Mood was
 * supplied, then the User can only add a new Mood with the values in the text /
 * dropdown fields. Adding, saving, or deleting a mood will return the User to their
 * MyMoods page.
 *
 * @see MyMoods
 */
public class AddEditMood extends AppCompatActivity {
    /**
     * The constant REQUEST_ACCESS_CAMERA.
     */
    public static  final int REQUEST_ACCESS_CAMERA = 4;
    private AddEditMood activity = this;
    private DataController controller;
    private String FILENAME = "file.sav";
    private Mood targetMood;
    private Bitmap imageBitMap;
    private LocationController locationController;
    private String[] wordCount;
    private GoogleApiClient mGoogleApiClient;


    //UI Elements
    private Spinner moodSpinner;
    private Spinner scenarioSpinner;
    private EditText triggerText;
    private Button saveButton;
    private Button deleteButton;
    private Button imageButton;
    private Button editLocButton;
    private ImageView pictureView;
    private Switch locationSwitch;
    private DatePicker datePicker;

    private Boolean isOnline;

    /**
     * The Mood index.
     */
//Mood Index
    int moodIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_mood);

        //Initialize UI Elements
        moodSpinner = (Spinner) findViewById(R.id.addEditMoodMoodSpinner);
        scenarioSpinner = (Spinner) findViewById(R.id.addEditMoodScenarioSpinner);
        triggerText = (EditText) findViewById(R.id.addEditMoodsTriggerText);
        saveButton = (Button) findViewById(R.id.addEditMoodSaveButton);
        deleteButton = (Button) findViewById(R.id.addEditMoodDeleteButton);
        imageButton = (Button) findViewById(R.id.imageButton);
        pictureView = (ImageView) findViewById(R.id.imageView);
        locationSwitch = (Switch) findViewById(R.id.locationSwitch);
        isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();
        datePicker = (DatePicker) findViewById(R.id.pickedDate);
        editLocButton = (Button) findViewById(R.id.editLocationButton);

        //Grab the data controller
        loadFromFile();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attemptToTakePhoto();
            }
        });

        locationController = new LocationController(mGoogleApiClient, activity);

        /*
         * Spinner initialization shamelessly taken from https://developer.android.com/guide/topics/ui/controls/spinner.html
         */

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> moodAdapter = ArrayAdapter.createFromResource(this,
                R.array.moods, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodSpinner.setAdapter(moodAdapter);

        ArrayAdapter<CharSequence> socialAdapter = ArrayAdapter.createFromResource(this,
                R.array.social_situations, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        scenarioSpinner.setAdapter(socialAdapter);



        //Check if a mood was passed in
        Intent intent = getIntent();
        moodIndex = intent.getIntExtra("Mood index", -1);
        if (moodIndex != -1) {
            targetMood = controller.getCurrentUser().getMyMoodsList().get(moodIndex);
            moodSpinner.setSelection(moodAdapter.getPosition(targetMood.getMoodName()));
            scenarioSpinner.setSelection(socialAdapter.getPosition(targetMood.getMoodScenario()));
            triggerText.setText(targetMood.getMoodDescription());
//            locationSwitch.setVisibility(View.GONE);
            if (targetMood.getLatLng() != null)
                locationSwitch.setChecked(true); // TODO what to do if they want to update location?
            if(targetMood.getMoodImg() != null) {
                pictureView.setImageBitmap(targetMood.getMoodImg());
            }



        } else {
            // Hide the delete button, since you can't delete a Mood that doesn't exist!
            deleteButton.setVisibility(View.GONE);
            editLocButton.setVisibility(View.GONE);
        }

        /*
         * Code that is run when the Save button is clicked. Saves the user input and creates a mood
         * and adds it to the list of moods made by the Current User.
         *
         */

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String moodName = moodSpinner.getSelectedItem().toString();
                String scenario = scenarioSpinner.getSelectedItem().toString();
                String trigger = triggerText.getText().toString();

                if (trigger.split(" ").length < 4 && trigger.length() < 21){
                    //set date from datePicker
                    int day = datePicker.getDayOfMonth();
                    day = day -1;
                    int month = datePicker.getMonth();
                    int year = datePicker.getYear();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    Date date = calendar.getTime();

                    //If making a new Mood:
                    if (moodIndex == -1) {
                        Mood newMood = new Mood(controller.getCurrentUser().getName());
                        newMood.setMoodDate(date);
                        newMood.setMoodName(moodName);
                        newMood.setMoodDescription(trigger);
                        newMood.setmoodScenario(scenario);
                        newMood.setOwnerName(controller.getCurrentUser().getName());
                        if(imageBitMap != null){newMood.setMoodImg(imageBitMap);}

                        if(locationSwitch.isChecked()){
                            Location location = locationController.getCurrentLocation();
                            if (location != null) {
                                LatLng latLng = LocationController.locationToLatLng(location);
                                newMood.setLatLng(latLng);
                            }
                        }


                        controller.getCurrentUser().addMood(newMood);


                    } else {
                        // Edit the existing Mood with the changes supplied.
                        targetMood.setMoodName(moodName);
                        targetMood.setMoodDate(date);
                        targetMood.setMoodDescription(trigger);
                        targetMood.setmoodScenario(scenario);
                        if(imageBitMap != null){targetMood.setMoodImg(imageBitMap);}
                    }
                    Intent intent = new Intent(activity, MyMoods.class);

                /* Attempts to connect to the elasticsearch database to push the changes to their moods, after grabbing
                * any changes to the user's follower / following lists. If the connection attempt fails, the changes
                * for mood are stored locally.
                */
                    Boolean syncSuccess = false;
                    if (isOnline) {
                        controller.setCurrentUser(controller.addFollowingToUser(controller.getCurrentUser()));
                        controller.setCurrentUser(controller.addFollowerRequestsToUser(controller.getCurrentUser()));
                        syncSuccess = controller.ElasticSearchsyncUser(controller.getCurrentUser());
                    }
                    Log.i("SyncSuccess", syncSuccess.toString());
                    saveInFile();
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(activity, "Trigger too long: Max Length 20 Characters or 3 words", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /*
         * Code that is run when the Delete button is clicked. Deletes the Mood and removes
         * it from the list of the User's Moods.
         */
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.getCurrentUser().removeMood(targetMood);

                /* Attempts to connect to the elasticsearch database to push the changes to their moods, after grabbing
                * any changes to the user's follower / following lists. If the connection attempt fails, the changes
                * for mood are stored locally.
                */
                Boolean syncSuccess = false;
                if (isOnline) {
                    controller.setCurrentUser(controller.addFollowingToUser(controller.getCurrentUser()));
                    controller.setCurrentUser(controller.addFollowerRequestsToUser(controller.getCurrentUser()));
                    syncSuccess = controller.ElasticSearchsyncUser(controller.getCurrentUser());
                }
                Log.i("SyncSuccess", syncSuccess.toString());
                Intent intent = new Intent(activity, MyMoods.class);
                saveInFile();
                startActivity(intent);
                finish();
            }
        });

        //Code that is run when the location switch is selected.
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    locationController.stopLocationUpdates();
                    return;
                }

                if (!locationController.checkLocationPermission()) {
                    locationController.requestLocationPermission();
                }


            }
        });

        editLocButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // get prompts.xml view
                LayoutInflater layoutInflater = LayoutInflater.from(activity);

                View promptView = layoutInflater.inflate(R.layout.edit_location_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                // set edit_location_prompt.xml to be the layout file of the alertdialog builder
                alertDialogBuilder.setView(promptView);

                final EditText latText = (EditText) promptView.findViewById(R.id.lat);
                final EditText lngText = (EditText) promptView.findViewById(R.id.lng);

                if(targetMood.getLatLng() != null) {
                    latText.setText(String.valueOf(targetMood.getLatLng().latitude));
                    lngText.setText(String.valueOf(targetMood.getLatLng().longitude));
                }

                // setup a dialog window
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Double lat, lng;
                                if (latText.getText().toString().length() > 0 && lngText.getText().toString().length() > 0){
                                    lat = Double.valueOf(latText.getText().toString());
                                    lng = Double.valueOf(lngText.getText().toString());
                                    if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                                        // invalid send a toast
                                        Toast.makeText(activity, "Location Invalid", Toast.LENGTH_LONG).show();
                                    } else {
                                        targetMood.setLatLng(new LatLng(lat, lng));
                                        locationSwitch.setChecked(true);
                                    }
                                } else {
                                    Toast.makeText(activity, "Location Invalid", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,	int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alertD = alertDialogBuilder.create();

                alertD.show();

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        locationController.connectGoogleApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationController.googleApiClientConnected()) {
            locationController.startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationController.googleApiClientConnected()) {
            locationController.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationController.disconnectGoogleApiClient();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationController.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // todo make sure its high accuracy
                        locationController.setCanGetLocation(true);
                        locationController.startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        locationController.setCanGetLocation(false);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if(extras.get("data") != null){
                        imageBitMap = (Bitmap) extras.get("data");
                        pictureView.setImageBitmap(imageBitMap);
                    }
                }
        }
    }

    // Load the data controller stored in the specified file.
    // Taken from: the CMPUT301 lonelyTwitter lab examples
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

    // Save the data controller into the specified file.
    // Taken from: the CMPUT301 lonelyTwitter lab examples
    private void saveInFile() {
        try {

            FileOutputStream fos = openFileOutput(FILENAME,0);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            Gson gson = new Gson();
            gson.toJson(controller, writer);
            Log.i("gsonToJson", gson.toJson(controller));
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    //Start intent to take a photo
    //Adapted from https://developer.android.com/training/camera/photobasics.html
    // and https://github.com/alisajedi/MyCameraTest1
    private void takePhoto()
    {
        //ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.CAMERA},
          //      MY_PERMISSIONS_REQUEST_CAMERA);
        
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }

    }

    private void attemptToTakePhoto()
    {
        //ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.CAMERA},
        //      MY_PERMISSIONS_REQUEST_CAMERA);

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA}, REQUEST_ACCESS_CAMERA);


    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // check location settings
                    takePhoto();

                }
        }   }
    }
}


