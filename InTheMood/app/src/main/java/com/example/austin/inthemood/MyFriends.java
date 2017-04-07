package com.example.austin.inthemood;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * This class displays the current user's Friends and each of their most recent moods. Furthermore,
 * you can filter these most recent moods from your friends by last week only, moods containing
 * the string from the search field, or by emotional state, just like in the MyMoods activity.
 * The User may also navigate to the Mood Map with friends' Moods, or to the Find Friends or
 * Friend Request activities from here.
 *
 * @see MapActivity
 * @see FindFriends
 * @see FriendRequests
 */
public class MyFriends extends AppCompatActivity {

    //UI Elements
    private Button emotionFilterButton;
    private Button weekFilterButton;
    private Button triggerFilterButton;
    private Button mapButton;
    private EditText triggerText;
    private Spinner moodFilterSpinner;
    private User currentUser;
    private ArrayList<Mood> newMoodList = new ArrayList<Mood>();
    private ArrayList<Mood> originalMoodList = new ArrayList<Mood>();
    private String filterMood;
    private String filterTrigger;
    private int filterNumber;

    private static final String FILENAME = "file.sav";
    private ListView myFriendsListView;
    private ArrayList<User> followingList;
    private ArrayList<Mood> sortedFollowingMoods = new ArrayList<Mood>();
    private ArrayList<String> followedUserStringMessage;
    private DataController controller;
    private MoodAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_friends);
        loadFromFile();

        //Initialize UI elements
        emotionFilterButton = (Button) findViewById(R.id.emotionalStateFilterButton);
        weekFilterButton = (Button) findViewById(R.id.weekFilterButton);
        triggerFilterButton = (RadioButton) findViewById(R.id.triggerFilterButton);
        mapButton = (Button) findViewById(R.id.mapButton);

        triggerText = (EditText) findViewById(R.id.triggerFilterEditText);
        moodFilterSpinner = (Spinner) findViewById(R.id.moodFilterSpinner);
        myFriendsListView = (ListView) findViewById(R.id.myMoodsListView);


        ArrayAdapter<CharSequence> moodSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.moods, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        moodSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodFilterSpinner.setAdapter(moodSpinnerAdapter);

        //Print to list view. For each followed user, print his name and his most recent mood with mood date
        //just print followeduser name if no moods have been recorded
        myFriendsListView = (ListView) findViewById(R.id.myFriendsListView);
        followingList = new ArrayList<User>();
        for (int i = 0; i < controller.getCurrentUser().getMyFollowingList().size(); i++){
           User user = controller.getElasticSearchUser(controller.getCurrentUser().getMyFollowingList().get(i));
           if (user != null) {
               followingList.add(user);
           }
        }
        followedUserStringMessage = new ArrayList<String>();
        for (int i = 0; i < followingList.size(); i++){
            ArrayList<Mood> followedUserMoods = followingList.get(i).getMyMoodsList();

            //if the followed user has moods, find his most recent mood and display it. If not,
            //only display his name
            if (followedUserMoods != null) {
                followedUserMoods = controller.sortMoodsByDate(followedUserMoods);
                originalMoodList.add(followedUserMoods.get(followedUserMoods.size() - 1));
            }
        }
        //make a copy of the following mood list for easy filtering.

        for (int i=0; i < originalMoodList.size(); i++ ){
            sortedFollowingMoods.add(originalMoodList.get(i));
        }
        adapter = new MoodAdapter(this, sortedFollowingMoods,controller.getCurrentUser().getName());
        //adapter = new ArrayAdapter<String>(this,
          //     R.layout.list_item, followedUserStringMessage);
        myFriendsListView.setAdapter(adapter);

        // start the map activity with the proper intents
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyFriends.this ,MapActivity.class);
                intent.putExtra("activity", "MyFriends");
                if (filterNumber == 1)
                    intent.putExtra("emotion", filterMood);

                if (filterNumber == 2)
                    intent.putExtra("lastweek", 1);

                if (filterNumber == 3)
                    intent.putExtra("trigger", filterTrigger);

                startActivity(intent);
                finish();
            }

        });
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        loadFromFile();

        moodFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(emotionFilterButton.isSelected()){

                    triggerFilterButton.setSelected(false);
                    weekFilterButton.setSelected(false);
                    emotionFilterButton.setSelected(true);
                    newMoodList = controller.filterByMood(moodFilterSpinner.getSelectedItem().toString(), originalMoodList);
                    sortedFollowingMoods.clear();

                    for (int i = 0; i < newMoodList.size(); i++) {
                        sortedFollowingMoods.add(newMoodList.get(i));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * onclick findFriends button is pressed and the FindFriend activity is called
     *
     * @param view
     */
    public void findFriends(View view){
        Intent intent = new Intent(this, FindFriends.class);
        startActivity(intent);
    }

    /**
     * onclick friendRequests button is pressed and the friendRequests activity is called
     *
     * @param view
     */
    public void friendRequests(View view){
        Intent intent = new Intent(this, FriendRequests.class);
        startActivity(intent);
    }

    /**
     * Called when the user clickes a filter radio button
     * Filters moods of friends
     * @param view
     */
    public void filter(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.emotionalStateFilterButton:
                if (checked) {
                    // Filter by emotional state

                    triggerFilterButton.setSelected(false);
                    weekFilterButton.setSelected(false);
                    emotionFilterButton.setSelected(true);
                    newMoodList = controller.filterByMood(moodFilterSpinner.getSelectedItem().toString(), originalMoodList);
                    sortedFollowingMoods.clear();

                    for (int i = 0; i < newMoodList.size(); i++) {
                        sortedFollowingMoods.add(newMoodList.get(i));
                    }
                    adapter.notifyDataSetChanged();

                    filterMood = moodFilterSpinner.getSelectedItem().toString();
                    filterNumber = 1;

                    break;
                }
            case R.id.weekFilterButton:
                if (checked) {
                    // Filter by last week's moods only
                    emotionFilterButton.setSelected(false);
                    triggerFilterButton.setSelected(false);
                    newMoodList = controller.filterByWeek(originalMoodList);
                    sortedFollowingMoods.clear();
                    for (int i = 0; i < newMoodList.size(); i++) {
                        sortedFollowingMoods.add(newMoodList.get(i));
                    }
                    weekFilterButton.setSelected(true);
                    adapter.notifyDataSetChanged();

                    filterNumber = 2;
                    break;
                }
            case R.id.triggerFilterButton:
                if (checked) {
                    // Filter by Moods containing the trigger filter
                    if (triggerText.getText().toString().split(" ").length < 4 && triggerText.getText().toString().length() < 21)
                    {
                        emotionFilterButton.setSelected(false);
                        weekFilterButton.setSelected(false);
                        newMoodList = controller.filterByTrigger(triggerText.getText().toString(), originalMoodList);
                        sortedFollowingMoods.clear();

                        for (int i = 0; i < newMoodList.size(); i++) {
                            sortedFollowingMoods.add(newMoodList.get(i));
                        }
                        adapter.notifyDataSetChanged();
                        triggerFilterButton.setSelected(true);
                        filterTrigger = triggerText.getText().toString();
                        filterNumber = 3;
                    }else{
                        Toast.makeText(MyFriends.this, "Trigger too long: Max Length 20 Characters or 3 words", Toast.LENGTH_SHORT).show();

                    }
                    break;
                }
            case R.id.noFilterButton:
                if(checked){
                    emotionFilterButton.setSelected(false);
                    triggerFilterButton.setSelected(false);
                    weekFilterButton.setSelected(false);
                    sortedFollowingMoods.clear();
                    for (int i = 0; i < originalMoodList.size(); i++) {
                        sortedFollowingMoods.add(originalMoodList.get(i));
                    }
                    adapter.notifyDataSetChanged();
                }
        }
    }

    /**
     *  load the data controller. called at the start of the activity. All data is stored in the controller.
     */
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
