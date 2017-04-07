package com.example.austin.inthemood;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * My moods activity displays the list of all moods a user inputs and allows filtering the list to
 * show only relevant moods.
 * <p>
 * Clicking the + icon allows for creation of new moods, after creation it returns to this activity
 * with a new mood appearing in the listview
 * <p>
 * Clicking the listed views allows for editting/deletion
 *
 * @see AddEditMood
 * @see MapActivity
 * @see MoodCalendarActivity
 */
public class MyMoods extends AppCompatActivity {


    //UI Elements
    private RadioButton emotionFilterButton;
    private RadioButton weekFilterButton;
    private RadioButton triggerFilterButton;
    private RadioButton noFilterButton;
    private EditText triggerText;
    private ImageButton newMoodButton;
    private ImageButton calendarButton;
    private ImageButton mapButton;
    private Spinner moodFilterSpinner;
    private ListView moodsListView;
    /**
     * The Controller.
     */
    public DataController controller;
    private static final String FILENAME = "file.sav";
    private MoodAdapter moodAdapter;
    private User currentUser;
    private ArrayList<Mood> SortedMoodList = new ArrayList<Mood>();
    private ArrayList<Mood> NewMoodList = new ArrayList<Mood>();
    private ArrayList<Mood> OriginalMoodList = new ArrayList<Mood>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_moods);

        //Initialize UI elements
        emotionFilterButton = (RadioButton) findViewById(R.id.emotionalStateFilterButton);
        weekFilterButton = (RadioButton) findViewById(R.id.weekFilterButton);
        triggerFilterButton = (RadioButton) findViewById(R.id.triggerFilterButton);
        noFilterButton = (RadioButton) findViewById(R.id.noFilterButton);
        triggerText = (EditText) findViewById(R.id.triggerFilterEditText);
        moodFilterSpinner = (Spinner) findViewById(R.id.moodFilterSpinner);
        moodsListView = (ListView) findViewById(R.id.myMoodsListView);
        newMoodButton = (ImageButton) findViewById(R.id.newMoodImg);
        calendarButton = (ImageButton) findViewById(R.id.calendarImg);
        mapButton = (ImageButton) findViewById(R.id.mapImg);


        ArrayAdapter<CharSequence> moodSpinnerAdapter = ArrayAdapter.createFromResource(this,

                R.array.moods, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        moodSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodFilterSpinner.setAdapter(moodSpinnerAdapter);


        //onclick listener to apply/deapply trigger mood filter.
        triggerFilterButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setResult(RESULT_OK);
                if (triggerFilterButton.isSelected() == false) {
                    if(triggerText.getText().toString().split(" ").length < 4 && triggerText.getText().toString().length() <21) {
                        emotionFilterButton.setSelected(false);
                        weekFilterButton.setSelected(false);
                        NewMoodList = controller.filterByTrigger(triggerText.getText().toString(), OriginalMoodList);
                        SortedMoodList.clear();

                        for (int i = 0; i < NewMoodList.size(); i++) {
                            SortedMoodList.add(NewMoodList.get(i));
                        }

                        moodAdapter.notifyDataSetChanged();
                        triggerFilterButton.setSelected(true);
                    }else{
                        Toast.makeText(MyMoods.this, "Trigger too long: Max Length 20 Characters or 3 words", Toast.LENGTH_SHORT).show();

                    }


                }
            }

        });
        //Apply last week filter for moods
        weekFilterButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setResult(RESULT_OK);

                if (!weekFilterButton.isSelected()) {
                    emotionFilterButton.setSelected(false);
                    triggerFilterButton.setSelected(false);
                    NewMoodList = controller.filterByWeek(OriginalMoodList);
                    SortedMoodList.clear();
                    for (int i = 0; i < NewMoodList.size(); i++) {
                        SortedMoodList.add(NewMoodList.get(i));
                    }
                    weekFilterButton.setSelected(true);
                    moodAdapter.notifyDataSetChanged();

                }
            }

        });
        //apply selected emotion filter
        emotionFilterButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setResult(RESULT_OK);

                if (!emotionFilterButton.isSelected()) {
                    triggerFilterButton.setSelected(false);
                    weekFilterButton.setSelected(false);
                    emotionFilterButton.setSelected(true);
                    NewMoodList = controller.filterByMood(moodFilterSpinner.getSelectedItem().toString(), OriginalMoodList);
                    SortedMoodList.clear();

                    for (int i = 0; i < NewMoodList.size(); i++) {
                        SortedMoodList.add(NewMoodList.get(i));
                    }
                    moodAdapter.notifyDataSetChanged();

                }
            }

        });


        noFilterButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                emotionFilterButton.setSelected(false);
                triggerFilterButton.setSelected(false);
                weekFilterButton.setSelected(false);
                SortedMoodList.clear();
                for (int i = 0; i < OriginalMoodList.size(); i++) {
                    SortedMoodList.add(OriginalMoodList.get(i));
                }
                moodAdapter.notifyDataSetChanged();
            }

        });

        newMoodButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                addMood(v);
            }

        });

        calendarButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MyMoods.this ,MoodCalendarActivity.class);
                startActivity(intent);
                finish();

            }

        });

        mapButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // send moods to the new activity
                Intent intent = new Intent(MyMoods.this ,MapActivity.class);
                intent.putExtra("activity", "MyMoods");
                if (triggerFilterButton.isChecked())
                    intent.putExtra("trigger", triggerFilterButton.getText().toString());

                if (emotionFilterButton.isChecked())
                    intent.putExtra("emotion", moodFilterSpinner.getSelectedItem().toString());

                if (weekFilterButton.isChecked())
                    intent.putExtra("lastweek", 1);

                startActivity(intent);
                finish();
            }

        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        loadFromFile();
        currentUser = controller.getCurrentUser();
        SortedMoodList = currentUser.getMyMoodsList();

        //store a copy of original mood list to allow easier unapplying of filters
        for (int i=0; i < currentUser.getMyMoodsList().size(); i++ ){
            OriginalMoodList.add(currentUser.getMyMoodsList().get(i));
        }
        moodFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(emotionFilterButton.isSelected()){

                    triggerFilterButton.setSelected(false);
                    weekFilterButton.setSelected(false);
                    emotionFilterButton.setSelected(true);
                    NewMoodList = controller.filterByMood(moodFilterSpinner.getSelectedItem().toString(), OriginalMoodList);
                    SortedMoodList.clear();

                    for (int i = 0; i < NewMoodList.size(); i++) {
                        SortedMoodList.add(NewMoodList.get(i));
                    }
                    moodAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        moodAdapter = new MoodAdapter(this,SortedMoodList,controller.getCurrentUser().getName());
        moodsListView.setAdapter(moodAdapter);
        moodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent,View view, int position,long id){
                editMood(view,position);
            }
        });
    }

    /**
     * Start edit mood activity
     * @param view
     * @param index - what element of the list is clicked
     */

    //Start edit mood activity
    private void editMood(View view, int index){
        Intent editMoodIntent = new Intent(this,AddEditMood.class);
        editMoodIntent.putExtra("Mood index",index);
        startActivity(editMoodIntent);
        finish();



    }

    /**
     *  start activity to add another mood
     *
     * @param view
     */
    private void addMood(View view){
        Intent addMoodIntent = new Intent(this,AddEditMood.class);
        startActivity(addMoodIntent);
        finish();

    }

    /**
     * Loads locally stored data controller
     */
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
}
