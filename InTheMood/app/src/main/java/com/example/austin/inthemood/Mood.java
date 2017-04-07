package com.example.austin.inthemood;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.Date;


/**implementation of Mood class
 * Moods store all their relevant information such as creation date, emotional state,description,ect
 *
 * Multiple moods will be stored by each user object
 */
public class Mood {
    private Date moodDate;
    private String moodDescription;
    private String colorHexCode;
    private String moodScenario;
    private String moodName;
    private String ownerName;
    //Bitmap moodImg;
    private String moodImgStringForm;
    private LatLng latLng;
    private String ElasticSearchID;


    /**
     * Instantiates a new Mood.
     *
     * @param moodDate        the mood date
     * @param moodDescription the mood description
     */
    public Mood(Date moodDate, String moodDescription, String ownerName) {
        this.moodDate = moodDate;
        this.moodDescription = moodDescription;
        this.ownerName = ownerName;
    }

    /**
     * Instantiates a new Mood.
     */
    public Mood(String moodDescription) {
        this.moodDate = new Date();
        this.moodDescription = moodDescription;
    }

    /**
     * Gets mood name.
     *
     * @return the mood name
     */
    public String getMoodName() {
        return moodName;
    }

    /**
     * Sets mood name.
     *
     * @param moodName the mood name
     */
    public void setMoodName(String moodName) {

        this.moodName = moodName;
        this.inferMoodColor();
    }

    /**
     * Gets the elastic search ID of a mood
     * @return ElasticSearchID
     */
    public String getElasticSearchID() { return ElasticSearchID;}

    /**
     * Sets the elastic search ID of a mood
     * @param esID
     */
    public void setElasticSearchID(String esID) {
        this.ElasticSearchID = esID;
    }

    public String getOwnerName(){
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    /**
     * Gets mood date.
     *
     * @return the mood date
     */
    public Date getMoodDate() {
        return moodDate;
    }

    /**
     * Sets mood date.
     *
     * @param moodDate the mood date
     */
    public void setMoodDate(Date moodDate) {

        this.moodDate = moodDate;
    }

    /**
     * Gets mood description.
     *
     * @return the mood description
     */
    public String getMoodDescription() {

        return moodDescription;
    }

    /**
     * Sets mood description.
     *
     * @param moodDescription the mood description
     */
    public void setMoodDescription(String moodDescription) {
        if (moodDescription.length() >= 20)
        {
            this.moodDescription = moodDescription.substring(0,20);
        }else{
            this.moodDescription = moodDescription;
        }

    }

    /**
     *
     * @return moodScenario the Scenario a mood is in
     */
    public String getMoodScenario() {
        return moodScenario;
    }

    /**
     * Sets the moodScenario
     * @param scenario the scenario a mood occures in
     */
    public void setmoodScenario(String scenario) {
        moodScenario = scenario;
    }

    /**
     *
     * @return colorHexCode  - the color associated with a specific mood
     */
    public String getColorHexCode() {
        return colorHexCode;
    }

    //getMoodImg takes the encoded String form of the bitmap Img and returns its Bitmap form
    //Taken from http://stackoverflow.com/questions/30818538/converting-json-object-with-bitmaps
    /**
     * getMoodImg takes the encoded String form of the bitmap Img and returns its Bitmap form
     * BitMap allows for easy displaying of the image
     * Taken from http://stackoverflow.com/questions/30818538/converting-json-object-with-bitmaps
     * @return
     */
    public Bitmap getMoodImg() {
        if (moodImgStringForm != null){
            byte [] decodedString = Base64.decode(moodImgStringForm,Base64.DEFAULT);
            Bitmap decodedMap = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
            return decodedMap;
        }
        return null;
    }


    /**
     * takes a BitMap and encodes it into a string to store in the mood class to allow easier saving
     * with jSon & elastic search
     * Taken from http://stackoverflow.com/questions/30818538/converting-json-object-with-bitmaps
     * @param moodImg - Original bitmap of image taken by camera
     */
     public void setMoodImg(Bitmap moodImg) {

        //this.moodImg = moodImg;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        moodImg.compress(Bitmap.CompressFormat.PNG,100,byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        moodImgStringForm = Base64.encodeToString(b,Base64.DEFAULT);

    }

    /**
     *
     * @return string repersentation of the mood
     */
    public String toString(){
        return  "Mood on " + this.moodDate + " was " + this.moodName + " because " +
                this.moodDescription;
    }

    /**
     *
     * @return latLng - Latitude/Longitude location of the mood
     */
    public LatLng getLatLng() {
        return latLng;
    }

    /**
     *
     * @param latLng -Location where moood was taken
     */
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    /**
     * Function is called when a moodName is set
     * Ensures proper color gets saved with the emotional state
     */
    private void inferMoodColor(){
        if (this.moodName.equals("Anger")){
            this.colorHexCode = "#f0391c";

        }
        else if(this.moodName.equals("Confusion")) {
            this.colorHexCode = "#ffa500";

        }
        else if (this.moodName.equals("Disgust")){
            this.colorHexCode = "#9ae343";
        }
        else if (this.moodName.equals("Fear")){
            this.colorHexCode = "#8383a9";
        }
        else if (this.moodName.equals("Happiness")){
            this.colorHexCode = "#e8ef02";
        }
        else if (this.moodName.equals("Sadness")){
            this.colorHexCode = "#03acca";
        }
        else if (this.moodName.equals("Shame")){
            this.colorHexCode = "#cd00ff";

        }
        else if (this.moodName.equals("Surprise")){
            this.colorHexCode = "#ff006c";

        }


    }
}
