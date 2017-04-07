package com.example.austin.inthemood;


import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
 * A login screen that offers login via username/password. This is the default
 * activity for the InTheMood app. Here, the User may enter their login credentials
 * to access their InTheMood main page. If the User does not yet have their own
 * login credentials, they can click the "Register" button to be redirected to the
 * new user registration screen to make their own account.
 *
 * @see MainUser
 * @see NewUserLogin
 */
public class ExistingUserLogin extends AppCompatActivity{
    /**
     *  To pass in a message to the next activity
     */
    private static final String FILENAME = "file.sav";
    public DataController controller;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;

    // Error message reference.
    private TextView eL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_user_login);

        // Hide the error message by default.
        eL = (TextView) findViewById(R.id.eLogin);
        eL.setVisibility(View.GONE);
        Boolean isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();
        Log.i("Device is online", isOnline.toString());

        // Initialize the data controller.
        loadFromFile();
        saveInFile();


        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.user);
        mPasswordView = (EditText) findViewById(R.id.password);

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        loadFromFile();
        saveInFile();
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
            System.out.println("before");
            controller = new DataController(firstUser);
            System.out.println("after");
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
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    /**
     * Called when the log in button is clicked. First, it verifies the login
     * credentials. If the credentials match an existing account, the User is
     * is taken to that accounts respective login page. Else, they remain on
     * the login screen, which will now display an error message informing the
     * user that the inputted credentials are incorrect.
     *
     * @param view
     */
    public void login(View view) {
        String name = mUserView.getText().toString();
        String pass = mPasswordView.getText().toString();
        Boolean isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();
        User userLogin = controller.verifyLogIn(name, pass, isOnline);
        if (userLogin != null) {
            Intent intent = new Intent(this, MainUser.class);
            controller.setCurrentUser(userLogin);
            saveInFile();

            startActivity(intent);
            finish();
        } else {
            eL.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Called when the register button is clicked. The User is redirected to the
     * new user registration page.
     *
     * @param view
     */
    public void register(View view) {
        Boolean isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();
        if (!isOnline) {
            Context context = getApplicationContext();
            CharSequence text = "Registration can't be accessed when offline";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            Intent intent = new Intent(this, NewUserLogin.class);
            saveInFile();
            startActivity(intent);
            finish();
        }
    }

}

