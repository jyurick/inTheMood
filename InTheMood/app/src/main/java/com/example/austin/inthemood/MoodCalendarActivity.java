package com.example.austin.inthemood;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.austin.inthemood.decorators.EventDecorator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * An activity that shows the which days the user documented a mood and shows them on
 * a MaterialCalendarView. When a day is selected, a ListView will update to show which moods happened.
 */
public class MoodCalendarActivity extends AppCompatActivity implements OnDateSelectedListener {

    private MaterialCalendarView widget;
    private ArrayList<Mood> moodListForDay;
    private ArrayList<Mood> moodListForMonth;
    private ListView moodForDayListView;
    private MoodAdapter moodArrayAdapter;
    private static final String FILENAME = "file.sav";

    public DataController controller;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        moodForDayListView = (ListView) findViewById(R.id.moodListViewForDay);

        widget = (MaterialCalendarView) findViewById(R.id.calendarView);
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);


        // set the range of the calendar. should be changed to be dynamic or maybe removed completely.
        Calendar instance1 = Calendar.getInstance();
        instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);

        Calendar instance2 = Calendar.getInstance();
        instance2.set(instance2.get(Calendar.YEAR), Calendar.DECEMBER, 31);

        widget.state().edit()
                .setMinimumDate(instance1.getTime())
                .setMaximumDate(instance2.getTime())
                .commit();


        new PutMoodsInMaterialCalendarView().executeOnExecutor(Executors.newSingleThreadExecutor());

        moodForDayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editMood(view, position);
            }
        });
    }

    /**
     * Launch AddEditMood activity
     * @param view
     * @param index
     */
    private void editMood(View view, int index) {
        Intent editMoodIntent = new Intent(this, AddEditMood.class);
        editMoodIntent.putExtra("Mood index", index);
        startActivity(editMoodIntent);
        finish();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        // clear current ListView
        moodListForDay.clear();
        moodArrayAdapter.notifyDataSetChanged();

        // find moods that occur on this day
        for (Mood mood : moodListForMonth) {
            Calendar moodDateCalendar = Calendar.getInstance();
            moodDateCalendar.setTime(mood.getMoodDate());
            if (moodDateCalendar.get(Calendar.DAY_OF_MONTH) == date.getCalendar().get(Calendar.DAY_OF_MONTH)) {
                moodListForDay.add(mood);
            }
        }

        // update adapter
        moodArrayAdapter.notifyDataSetChanged();
    }



    @Override
    protected void onStart() {
        super.onStart();
        moodListForDay = new ArrayList<>();
        moodListForMonth = new ArrayList<>();
        loadFromFile();

        moodArrayAdapter = new MoodAdapter(this, moodListForDay,controller.getCurrentUser().getName());
        moodForDayListView.setAdapter(moodArrayAdapter);
        moodArrayAdapter.notifyDataSetChanged();

        user = controller.getCurrentUser();
    }

    /**
     * Get moods that have happened and put them in the Calendar as a red circle under the day.
     *
     * Based on
     * https://github.com/prolificinteractive/material-calendarview/tree/master/sample/src/main/java/com/prolificinteractive/materialcalendarview/sample
     */
    private class PutMoodsInMaterialCalendarView extends AsyncTask<Void, Void, List<CalendarDay>> {

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Calendar calendar = Calendar.getInstance();

            ArrayList<Mood> moods = user.getMyMoodsList();
            ArrayList<CalendarDay> dates = new ArrayList<>();


            // if the mood has the same month as current date show the moods
            // this needs to be changed to allow if the user changes the month
            // but there isn't time to figure that out right now
            for (Mood mood : moods) {
                Calendar moodDateCalendar = Calendar.getInstance();
                moodDateCalendar.setTime(mood.getMoodDate());
                if (calendar.get(Calendar.MONTH) == moodDateCalendar.get(Calendar.MONTH)) {
                    dates.add(CalendarDay.from(moodDateCalendar));
                    moodListForMonth.add(mood); // for when a user selects a date
                }
            }

            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }

            widget.addDecorator(new EventDecorator(Color.BLACK, calendarDays));
        }
    }

    /**
     * Load DataController from FILENAME stored in gson format.
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
}
