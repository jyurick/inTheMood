package com.example.austin.inthemood;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.searchbox.action.Action;
import io.searchbox.core.Doc;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * Created by jyurick on 2017-03-18.
 * Contains AsyncTasks that are used to communicate data to and from the elastic search server.
 */

public class ElasticSearchController {
    private static JestDroidClient client;
    private static String index = "cmput301w17t15";
    private static String user_type = "user";
    private static String mood_type = "mood";



   // This class supplies the functionality for adding a user to the elastic search server
    public static class AddUserTask extends AsyncTask<User, Void, String> {
        /**
         * Adds user to Server
         * @param users the user you want to add to the server
         * @return returns a string containing the unique id generated for the user by the server
         */
        @Override
        protected String doInBackground(User... users) {
            verifySettings();
            String userID = new String();
            for (User user : users) {
                try {
                    // where is the client?
                    DocumentResult result = client.execute(new Index.Builder(user).index(index).type(user_type).build());
                    Log.i("Error", "We sent the user");
                    if (result.isSucceeded() == false) {
                        Log.i("Error", "Elastic search couldn't add mood");
                    }else {
                        userID = result.getId();
                    }
                }
                catch (Exception e) {
                    Log.i("Error", "The application send the User");
                }
            }
            return userID;
        }
    }


    //This class retrieves and returns all users stored on the elastic search server
    public static class GetAllUsers extends AsyncTask<String, Void, ArrayList<User>> {
        /**
         * Gets all users stored on the elastic search server
         * @param strings
         * @return users which is an ArrayList<User>
         */
        @Override
        protected ArrayList<User> doInBackground(String ... strings) {
            verifySettings();
            ArrayList<User> users = new ArrayList<User>();

            // TODO Build the query
            String query = "";
            Search search = new Search.Builder(query)
                    .addIndex(index)
                    .addType(user_type)
                    .build();
            try {
                // TODO get the results of the query
                SearchResult result = client.execute(search);
                if (result.isSucceeded()){
                    List<User> foundUsers = result.getSourceAsObjectList(User.class);
                    users.addAll(foundUsers);
                    Log.i("Error", "We got the users!");
                    return users;
                }
                else {
                    Log.i("Error", "The search query failed to find any users");
                    return null;
                }
            }
            catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                return null;
            }


        }
    }

    //Retrieves a user from elastic search server who's name matches the string supplies
    public static class GetUserByName extends AsyncTask<String, Void, User> {
        /**
         * Gets the User object from server by their name
         * @param username
         * @return User object
         */
        @Override
        protected User doInBackground(String... username) {
            verifySettings();
            //ArrayList<User> users = new ArrayList<User>();
            User user = new User("","");
            // TODO Build the query
            String query = "{\n" +
                    "    \"query\": {\n" +
                    "        \"match\" : {\n" +
                    "            \"name\" : \n" +
                    "                \""+username[0]+"\"\n" +
                    "            }\n" +
                    "    }\n" +
                    "}";

            // TODO Build the query
            if (username[0] == ""){
                query = "";
            }
            System.out.print(query);
            Search search = new Search.Builder(query)
                    .addIndex(index)
                    .addType(user_type)
                    .build();
            try {
                // TODO get the results of the query
                SearchResult result = client.execute(search);
                if (result.isSucceeded()){
                    ///List<User> foundUsers = result.getSourceAsObjectList(User.class);
                    //users.addAll(foundUsers);
                    user = result.getSourceAsObject(User.class);
                    Log.i("Error", "We got the user!");
                    return user;
                }
                else {
                    Log.i("Error", "The search query failed to find any users that matched");
                    return null;
                }
            }
            catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                return null;
            }
            //user = users.get(0);

        }
    }

    //Syncs a mood to the elastic search server
    public static class SyncMoodTask extends AsyncTask<Mood, Void, Boolean> {
        /**
         * Syncs a given mood to the elastic search server
         * @param moods
         * @return wasSuccess is a boolean value showing whether or not the sync was successfull
         */
        @Override
        protected Boolean doInBackground(Mood ... moods) {
            verifySettings();
            Boolean wasSuccess = new Boolean(false);
            for (Mood mood: moods) {
                try {
                    String moodID = mood.getElasticSearchID();
                    DocumentResult result = client.execute(new Index.Builder(mood).index(index).type(mood_type).id(moodID).build());
                    wasSuccess = result.isSucceeded();
                    if (wasSuccess) {
                        Log.i("Success", "Mood was synced to elastic search");
                    } else {
                        Log.i("Failed", "Mood failed to sync to elastic search");
                    }
                } catch (Exception e) {
                    Log.i("Error", "Elastic search couldn't sync mood");
                }
            }

            return wasSuccess;
        }
    }

    //Syncs a user to the elastic search server
    public static class SyncUserTask extends AsyncTask<User, Void, Boolean> {
        /**
         * Syncs a given user to the elastic search server
         * @param users user to sync
         * @return wasSuccess boolean representing whether or not the sync was successfull
         */
        @Override
        protected Boolean doInBackground(User... users) {
            verifySettings();
            Boolean wasSuccess = new Boolean(false);
            for (User user : users) {
                try {
                    // where is the client?
                    String userID = user.getElasticSearchID();
                    DocumentResult result = client.execute(new Index.Builder(user).index(index).type(user_type).id(userID).build());
                    wasSuccess = result.isSucceeded();
                    Log.i("Error", "We synced the user!");
                    if (result.isSucceeded() == false) {
                        Log.i("Error", "Elastic search couldn't sync the user");
                    }
                    return result.isSucceeded();
                }
                catch (Exception e) {
                    Log.i("Error", "The application failed to sync the user");
                    return false;
                }
            }
            return wasSuccess;
        }
    }


    /**
     * Used to initialize the Jest client
     */
    public static void verifySettings() {
        if (client == null) {
            DroidClientConfig.Builder builder = new DroidClientConfig.Builder("http://cmput301.softwareprocess.es:8080");
            DroidClientConfig config = builder.build();

            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            client = (JestDroidClient) factory.getObject();
        }

    }
}
