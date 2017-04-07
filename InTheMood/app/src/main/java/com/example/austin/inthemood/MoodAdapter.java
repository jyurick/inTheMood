package com.example.austin.inthemood;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Define an adapter to populate a ListView the require mood objects
 * based on https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 * accessed march 12 2017
 */
public class MoodAdapter extends ArrayAdapter<Mood> {
    private String userName;

    /**
     * Instantiates a new Mood adapter.
     *
     * @param context  the context
     * @param moodList the mood list
     */
    public MoodAdapter(Context context, ArrayList<Mood> moodList, String user) {
        super(context, 0, moodList);
        this.userName = user;
    }

    /**
     * Overrides getView to ensure proper mood list item formatting and associated img emoticon
     * @param position, the position of the mood selected
     * @param convertView, the view inside the ListView
     * @param parent, parent view
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Mood mood = (Mood) getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mood, parent, false);
        }

        // Lookup view for data population
        TextView moodString = (TextView) convertView.findViewById(R.id.moodString);
        TextView moodComment = (TextView) convertView.findViewById(R.id.moodComment);
        ImageView moodEmote = (ImageView) convertView.findViewById(R.id.img);
        // Populate the data into the template view using the data object

        if(!userName.equals(mood.getOwnerName())){
            moodString.setText(mood.getOwnerName() + " felt " +mood.getMoodName() + " on " + mood.getMoodDate().toString());

        }else{
            moodString.setText(mood.getMoodName() + " felt on " + new SimpleDateFormat("MMM dd, yyyy").format(mood.getMoodDate()));
        }
        moodComment.setText(mood.getMoodDescription());
        convertView.setBackgroundColor(Color.parseColor(mood.getColorHexCode()));

        //set emoticon
        if (mood.getMoodName().equals("Anger")) {
            moodEmote.setImageResource(R.drawable.anger);
        }
        else if (mood.getMoodName().equals("Confusion")) {
            moodEmote.setImageResource(R.drawable.confusion);
        }
        else if (mood.getMoodName().equals("Disgust")) {
            moodEmote.setImageResource(R.drawable.disgust);
        }
        else if (mood.getMoodName().equals("Fear")) {
            moodEmote.setImageResource(R.drawable.fear);
        }
        else if (mood.getMoodName().equals("Happiness")) {
            moodEmote.setImageResource(R.drawable.happy);
        }
        else if (mood.getMoodName().equals("Sadness")) {
            moodEmote.setImageResource(R.drawable.sad);
        }
        else if (mood.getMoodName().equals("Shame")) {
            moodEmote.setImageResource(R.drawable.shame);
        }
        else if (mood.getMoodName().equals("Surprise")) {
            moodEmote.setImageResource(R.drawable.surprise);
        }


        // Return the completed view to render on screen
        return convertView;
    }
}
