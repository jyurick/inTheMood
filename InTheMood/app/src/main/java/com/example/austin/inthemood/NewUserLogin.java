package com.example.austin.inthemood;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

/** This activity takes care of handling registration of a new user of InTheMood.
 * The user can enter their desired username and password (which is entered twice
 * as a safety measure), and click the register button, which takes the User to
 * the main page if registration is successful.
 *
 * @see MainUser
 */
public class NewUserLogin extends AppCompatActivity {

    /**
     *  To pass in a message to the next activity
     */
    public static final String EXTRA_MESSAGE = "com.example.inthemood.MESSAGE";
    private static final String FILENAME = "file.sav";
    public DataController controller;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private EditText mConfirmPWView;

    //Error message references.
    private TextView eU;
    private TextView eP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        // Set up the registration field references.
        mUserView = (EditText) findViewById(R.id.user);
        mPasswordView = (EditText) findViewById(R.id.password);
        mConfirmPWView = (EditText) findViewById(R.id.confirm_password);

        // Get the data controller.
        loadFromFile();

        // Initialize error messages and hide them by default.
        eU = (TextView) findViewById(R.id.eUser);
        eU.setVisibility(View.GONE);
        eP = (TextView) findViewById(R.id.ePass);
        eP.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        loadFromFile();
    }

    /**
     * Called when the register button is clicked. Checks if the username is both
     * non-empty and unique, and if the password is both non-empty and matches the
     * confirm password field. If all these conditions are met, a new account is
     * created and the User is taken to the main menu page. Else, if any of the
     * conditions are not met, the User remains on the registration page, and an
     * error message with the respective error will pop up informing the User of
     * the issue.
     *
     * @param view
     */
    public void register(View view) {
        Boolean isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();
        if (validRegistration() == 1) {
            eU = (TextView) findViewById(R.id.eUser);
            eU.setVisibility(View.GONE);
            eP = (TextView) findViewById(R.id.ePass);
            eP.setVisibility(View.GONE);
            Intent intent = new Intent(this, MainUser.class);

            User newUser = new User(mUserView.getText().toString(),
                    mPasswordView.getText().toString());
            newUser = controller.ElasticSearchaddUser(newUser);
            Log.i("New User's ES id", newUser.getElasticSearchID());
            controller.addToUserList(newUser);
            controller.setCurrentUser(newUser);
            saveInFile();
            startActivity(intent);
            finish();

        } else if (validRegistration() == -1) {
            eU.setVisibility(View.VISIBLE);
            eU.setText(getString(R.string.eUser1));
        } else if (validRegistration() == -2) {
            eU.setVisibility(View.VISIBLE);
            eU.setText(getString(R.string.eUser2));
        } else if (validRegistration() == -4) {
            eP.setVisibility(View.VISIBLE);
            eP.setText(getString(R.string.ePass1));
        } else if (validRegistration() == -8) {
            eP.setVisibility(View.VISIBLE);
            eP.setText(getString(R.string.ePass2));
        }
    }

    /**
     * This function checks if the username and password supplied by the User are valid for
     * registering a new account. A valid username is any unique username (can't already be
     * registered in the database) that is not null, while a valid password is a nonempty password
     *
     * @return -1 if username is empty, -2 if username is already taken, -4 if password is empty,
     * -8 if the password and confirm fields do not match up, 1 if registration is valid.
     */
    public int validRegistration() {

        if (mUserView.getText().toString().equals("")) {
            return -1;
        }
        else if (controller.getElasticSearchUser(mUserView.getText().toString()) != null) {
            return -2;
        } else if (mPasswordView.getText().toString().equals("")) {
            return -4;
        } else if (!mPasswordView.getText().toString().equals(mConfirmPWView.getText().toString())){
            return -8;
        }
        return 1;
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
            Log.i("gson toJson", gson.toJson(controller));
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
