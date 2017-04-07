package com.example.austin.inthemood;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

/**
 * RemoveFollowRequests activity is called to give the current user the option to either
 * remove a follow request made by current user or return back to FriendRequests and
 * not remove his request.
 *
 * @see FriendRequests
 */
public class RemoveFollowRequest extends AppCompatActivity {

    private String username;
    private DataController controller;
    private static final String FILENAME = "file.sav";
    public final static String EXTRA_MESSAGE = "com.example.InTheMood";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_follow_request);
        Intent intent = getIntent();
        username = intent.getStringExtra(FriendRequests.EXTRA_MESSAGE);
        loadFromFile();
        TextView userToBeFollowed = (TextView) findViewById(R.id.userToBeFollowed);
        userToBeFollowed.setText(username);
    }

    /**
     * onClick method which removes a follow request made by current user
     * @param view this view
     */
    public void removeRequest(View view) {
        //update current user
        controller.getCurrentUser().removeFollowRequest(username);
        saveInFile();
        controller.ElasticSearchsyncUser(controller.getCurrentUser());

        //update the user that was previously requested to be followed
        if (controller.searchForUserByName(username) != null) {
            User current = controller.getCurrentUser();
            controller.setCurrentUser(controller.searchForUserByName(username));
            controller.getCurrentUser().removeFollowerRequest(current.getName());
            saveInFile();
            controller.ElasticSearchsyncUser(controller.getCurrentUser());
            controller.setCurrentUser(current);
        } else {
            User user = controller.getElasticSearchUser(username);
            user.removeFollowerRequest(controller.getCurrentUser().getName());
            controller.ElasticSearchsyncUser(user);
        }
        Gson gS = new Gson();
        boolean result = true;
        String stringResult = gS.toJson(result);

        Intent intent2 = new Intent();
        intent2.putExtra("MESSAGE", stringResult);
        setResult(RESULT_OK, intent2);
        finish();
    }


    /**
     * onClick method that returns activity flow back to FriendRequests activity
     * @param view this view
     */
    public void returnToRequests(View view) {
        Gson gS = new Gson();
        boolean result = false;
        String stringResult = gS.toJson(result);

        Intent intent2 = new Intent();
        intent2.putExtra("MESSAGE", stringResult);
        setResult(RESULT_OK, intent2);

        finish();
    }

    //save the data controller. This function is never called in here for the time being
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

    //load the data controller. called at the start of the activity. All data is stored in the controller.
    private void loadFromFile() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();

            Type objectType = new TypeToken<DataController>() {
            }.getType();
            controller = gson.fromJson(in, objectType);
        } catch (FileNotFoundException e) {
            User firstUser = new User("admin", "admin");
            controller = new DataController(firstUser);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
