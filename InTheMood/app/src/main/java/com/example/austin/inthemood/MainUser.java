package com.example.austin.inthemood;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

/**
 * This class is the main menu view. There are 5 buttons in the layout to choose from.
 * Each button sends the user to a different activity. Logging out will return the user to the
 * main login page
 *
 * @see MyMoods
 * @see MyFriends
 * @see MapActivity
 * @see MoodCalendarActivity
 * @see ExistingUserLogin
 */
public class MainUser extends AppCompatActivity {
    private MainUser activity = this;

    //UI Elements
    private Button myFriendsMoodsButton;
    private Button myMoodsButton;
    private Button myFriendsButton;
    private Button signOutButton;
    private Button moodCalendarButton;

    private static final String FILENAME = "file.sav";
    public DataController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        loadFromFile();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Start MyMoods activity
     * @param view
     */
    public void MyMoods(View view){
        Intent intent = new Intent(this, MyMoods.class);
        startActivity(intent);
    }

    /**
     * Remove the User from the data controller and return to login screen
     * @param view
     */
    public void SignOut(View view){
        controller.signOut();
        saveInFile();
        Intent intent = new Intent(this, ExistingUserLogin.class);
        startActivity(intent);
    }

    /**
     * Start the MyFriends Activity
     * @param view
     */
    public void MyFriends(View view){
        Boolean isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();
        if (!isOnline) {
            Context context = getApplicationContext();
            CharSequence text = "Friends can't be accessed when offline";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
        Intent intent = new Intent(this, MyFriends.class);
        startActivity(intent);}
    }

    /**
     * Start MapActivity
     * @param v - the View that calls the method
     */
    public void openMap(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("activity", "MainUser");
        startActivity(intent);
    }


    /**
     * Start the MoodCalendar activity
     * @param view
     */
    public void MoodCalendar(View view) {
        Intent intent = new Intent(this, MoodCalendarActivity.class);
        startActivity(intent);
    }

    /**
     * Load file FILENAME using GSON
     */
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
     * Save the data controller to file FILENAME using GSON.
     */
    private void saveInFile() {
        try {

            FileOutputStream fos = openFileOutput(FILENAME,0);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            Gson gson = new Gson();
            gson.toJson(controller, writer);
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }
}
