package com.example.austin.inthemood;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
 * FriendRequests activity displays the current user's follow and follower requests in
 * 2 different listviews. follower and follow requests (usernames displayed) can be selected from listviews
 * which then starts a new activity to act on these requests.
 *
 * @see AcceptFollowerRequest
 * @see RemoveFollowRequest
 */
public class FriendRequests extends AppCompatActivity {

    private DataController controller;
    private ArrayAdapter<String> followAdapter;
    private ArrayAdapter<String> followerAdapter;
    private ListView pendingFollowRequests;
    private ListView pendingFollowerRequests;
    private TextView followRequests;
    private TextView followerRequests;
    public final static String EXTRA_MESSAGE = "com.example.InTheMood";
    static final int PICK_CONTACT_REQUEST = 1;

    private static final String FILENAME = "file.sav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        loadFromFile();

        controller.setCurrentUser(controller.addFollowerRequestsToUser(controller.getCurrentUser()));
        controller.setCurrentUser(controller.addFollowingToUser(controller.getCurrentUser()));
        for (int x = 0; x < controller.getCurrentUser().getMyFollowingList().size(); x++ ) {
            String following = controller.getCurrentUser().getMyFollowingList().get(x);
            if (following != null && controller.getCurrentUser().getMyFollowRequests().contains(following)) {
                controller.getCurrentUser().removeFollowRequest(following);
            }
        }
        saveInFile();
        controller.ElasticSearchsyncUser(controller.getCurrentUser());

        Gson gson = new Gson();
        Log.i("json", gson.toJson(controller.getCurrentUser()));

        //print textview
        followerRequests = (TextView) findViewById(R.id.followerRequests);
        followRequests = (TextView) findViewById(R.id.followRequests);
        followerRequests.setText("Follower Requests");
        followRequests.setText("Follow Requests");

        //print follow requests to listview
        pendingFollowRequests = (ListView) findViewById(R.id.pendingFollowRequests);
        followAdapter = new ArrayAdapter<String>(this,
                R.layout.list_item, controller.getCurrentUser().getMyFollowRequests());
       pendingFollowRequests.setAdapter(followAdapter);

        //print follower requests to listview
        pendingFollowerRequests = (ListView) findViewById(R.id.pendingFollowerRequests);
        followerAdapter = new ArrayAdapter<String>(this,
                R.layout.list_item, controller.getCurrentUser().getMyFollowerRequests());
        pendingFollowerRequests.setAdapter(followerAdapter);

        /**
         * onItemClickListener which responds to a follow request being selected from listview
         * and sends user to RemoveFollowRequest activity
         */
        pendingFollowRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentUpdate = new Intent(view.getContext(), RemoveFollowRequest.class);
                String username = followAdapter.getItem(position);
                intentUpdate.putExtra(EXTRA_MESSAGE, username);
                startActivityForResult(intentUpdate, PICK_CONTACT_REQUEST);

            }
        });

        /**
         * onItemClickListener which responds to a follower request being selected from listview
         * and sends user to AcceptFollowerRequest
         */
        pendingFollowerRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentUpdate = new Intent(view.getContext(), AcceptFollowerRequest.class);
                String username = followerAdapter.getItem(position);
                intentUpdate.putExtra(EXTRA_MESSAGE, username);
                startActivityForResult(intentUpdate, PICK_CONTACT_REQUEST);

            }
        });
    }

    /**
     * this method receives the control flow after either the AcceptFollowerRequest
     * activity or the RemoveFollowRequest activity and updates the list views with the new list
     * of follow and follower requests
     *
     * @param requestCode request code for sending data to this activity
     * @param resultCode result code for sending data to this activity
     * @param data intent data storing a boolean indicating if listview should be updated
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {

                loadFromFile();
                Gson gS = new Gson();

                String person = data.getStringExtra("MESSAGE");
                boolean result = gS.fromJson(person, boolean.class);

                if (result){
                    followAdapter.clear();
                    followAdapter.addAll(controller.getCurrentUser().getMyFollowRequests());
                    followAdapter.notifyDataSetChanged();
                    followerAdapter.clear();
                    followerAdapter.addAll(controller.getCurrentUser().getMyFollowerRequests());
                    followerAdapter.notifyDataSetChanged();
                    pendingFollowRequests.setAdapter(followAdapter);
                    pendingFollowerRequests.setAdapter(followerAdapter);
                } else {
                    pendingFollowRequests.setAdapter(followAdapter);
                    pendingFollowerRequests.setAdapter(followerAdapter);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        loadFromFile();

        //update current user from elasticSearch
        User updatedCurrentUser = controller.getElasticSearchUser(controller.getCurrentUser().getName());
        controller.updateUserList(updatedCurrentUser);
        saveInFile();
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
