package com.example.austin.inthemood;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by olivier on 2017-03-10.
 *
 * This class contains a lot of main functionality used for manipulating our local and elastic search data.
 * The data controller also contains the userList, an Array list of Users, with each user containing all his
 * moods. The data controller also tracks the current user by setting a unique currentUserName to the username
 * of the current user.
 *
 */

public class DataController {
    private ArrayList<User> userList = new ArrayList<>();
    private String currentUserName;


    /**
     * Instantiates a new dataControler. Instantiated when app is first launched on a new device
     *
     * @param firstUser first registered user in our database
     */
    public DataController(User firstUser){
        this.userList = new ArrayList<User>();
        this.userList.add(firstUser);

    }

    /**
     * sets currentUserName to null
     */
    public void signOut(){
        this.currentUserName = null;
    }

    /**
     * set the current user interacting with the system in the data controller
     *
     * @param currentUser user interacting with the system
     */
    public void setCurrentUser(User currentUser){
        if (currentUser == null){
            this.currentUserName = null;
        }
        this.currentUserName = currentUser.getName();
    }

    /**
     * get current user interacting with the app
     *
     * @return user interacting with app
     */
    public User getCurrentUser() {

        return searchForUserByName(currentUserName);
    }

    /**
     * gets the list of registered users in the local database
     *
     * @return list of users
     */
    public ArrayList<User> getUserList() {
        return userList;
    }

    /**
     * update user in userList
     *
     * @param user the updated user replacing old user
     */
    public void updateUserList(User user) {
        for (int i = 0; i < this.userList.size(); i++){
            if (this.userList.get(i).getName() == user.getName()){
                this.userList.set(i, user);
                Gson gson = new Gson();
                Log.i("Updated user", gson.toJson(user));
            }
        }
    }

    /**
     * adds user to list of registered users (userList)
     * @param user user being added
     */
    public void addToUserList(User user){
        this.userList.add(user);
    }

    /**
     * checks to see if a user is registered (is in userList) and his corresponding password matches database
     *
     * @param name name being checked for in database
     * @param password corresponding password being checked for in database
     * @param isOnline a boolean indicating if the device is online
     * @return user if login successful or null if unsuccessful
     */
    public User verifyLogIn(String name, String password, Boolean isOnline){
        Log.i("Message","trying to get verify login");
        for (int i = 0; i < userList.size(); i++){
            if (userList.get(i).getName().equals(name)) {
                if (userList.get(i).getPassword().equals(password)) {
                    User user = userList.get(i);
                    Boolean syncSuccess = false;
                    if (isOnline) {
                        user = addFollowerRequestsToUser(user);
                        user = addFollowingToUser(user);
                        syncSuccess = ElasticSearchsyncUser(user);
                    }
                    Log.i("Found user", "in local");
                    Log.i("SyncSuccess", syncSuccess.toString());
                    Log.i("Users name:", user.getName());
                    Log.i("Users pass:", user.getPassword());
                    Log.i("Users ES ID", user.getElasticSearchID());
                    return user;

                }
            }
        }

        if (isOnline) {
            User user = getElasticSearchUser(name);
            if (user != null) {
                if (user.getPassword().equals(password)) {
                    Log.i("Found user:", "from elastic search");
                    Log.i("Users name:", user.getName());
                    Log.i("Users pass:", user.getPassword());
                    Log.i("Userus ES ID", user.getElasticSearchID());
                    addToUserList(user);
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * search userList for a user by name, return null if not found
     *
     * @param name of user being searched for
     * @return userList.get(i) the username of the located user, return null if user not in userList
     */
    public User searchForUserByName(String name) {
        for (int i = 0; i < userList.size(); i++){
            if (userList.get(i).getName().equals(name)){
                return userList.get(i);
            }
        }
        return null;
    }

    /**
     * Sort moods in moodsList by date (oldest to newest)
     *
     * pulled from http://stackoverflow.com/questions/36727700/sort-a-list-of-objects-by-date on March 11th, 2017
     *
     * @param moodsList list of moods to sort
     * @return sorted list of moods
     */
    public ArrayList<Mood> sortMoodsByDate(ArrayList<Mood> moodsList){
        Collections.sort(moodsList, new Comparator<Mood>() {
            @Override
            public int compare(Mood mood1, Mood mood2) {
                return mood1.getMoodDate().compareTo(mood2.getMoodDate());
            }
        });
        return moodsList;
    }

    /**
     * searches for moods with String moodName as a moodName and adds them to the filteredMoodList being returned
     *
     * @param moodName the moodName being searched for
     * @param moodList the list of moods being searched
     * @return the list of moods filtered by particular mood. this returned list is also sorted by date
     */
    public ArrayList<Mood> filterByMood(String moodName, ArrayList<Mood> moodList){
        ArrayList <Mood> filteredMoodList = new ArrayList<Mood>();
        for (int i = 0; i < moodList.size(); i++){
            if (moodList.get(i).getMoodName().equals(moodName)){
                filteredMoodList.add(moodList.get(i));
            }
        }
        return sortMoodsByDate(filteredMoodList);
    }

    /**
     * checks each mood to see if it was recorded in the last week. If so, it is added to the filteredMoodList being returned which is sorted by date
     * pulled from http://stackoverflow.com/questions/883060/how-can-i-determine-if-a-date-is-between-two-dates-in-java on March 11, 2017
     *
     * @param moodList list of moods to be checked if the moods were recorded in the last week
     * @return a list of moods recorded in the last week. this list is also sorted by date
     */
    public ArrayList<Mood> filterByWeek(ArrayList<Mood> moodList){
        ArrayList <Mood> filteredMoodList = new ArrayList<Mood>();

        Calendar cal = Calendar.getInstance();
        Date currentDate = new Date();
        cal.add(Calendar.DATE, -7);
        Date startWeekDate = cal.getTime();

        for (int i = 0; i < moodList.size(); i++){
            if (startWeekDate.compareTo(moodList.get(i).getMoodDate()) * moodList.get(i).getMoodDate().compareTo(currentDate) >= 0){
                filteredMoodList.add(moodList.get(i));
            }
        }
        return sortMoodsByDate(filteredMoodList);
    }

    /**
     * returns a list of moods from moodList that contains the string moodtrigger in the moodDescription
     *
     * @param moodTrigger the mood trigger description being searched for
     * @param moodList the list of moods that we are searching
     * @return a list of moods containing moodTrigger in the moodDescription. This list is sorted by date
     */
    public ArrayList<Mood> filterByTrigger(String moodTrigger, ArrayList<Mood> moodList){
        ArrayList <Mood> filteredMoodList = new ArrayList<Mood>();
        for (int i = 0; i < moodList.size(); i++){
            if (moodList.get(i).getMoodDescription().contains(moodTrigger)){
                filteredMoodList.add(moodList.get(i));
            }
        }
        return sortMoodsByDate(filteredMoodList);
    }

    /**
     * gets a user from elasticSearch using username
     *
     * @param username string username of user being looked for in elasticSearch
     * @return User being looked for if found or null if not found
     */
    public User getElasticSearchUser(String username) {
        ElasticSearchController.GetUserByName getUser = new ElasticSearchController.GetUserByName();
        getUser.execute(username);
        try {
            User locatedUser = getUser.get();
            return locatedUser;
        } catch (Exception e) {
            Log.i("Error", "Failed to get user by name");
            return null;
        }

    }

    /**
     *  adds user to ElasticSearch database
     * @param user user being added to elasticSearch
     * @return user being added or null if failed to add user
     */
    public User ElasticSearchaddUser(User user) {

        ElasticSearchController.AddUserTask addUser = new ElasticSearchController.AddUserTask();
        String userID = new String();
        addUser.execute(user);
        try {
            userID = addUser.get();
            user.setElasticSearchID(userID);
            ElasticSearchsyncUser(user);
            return user;
        } catch (Exception e) {
            Log.i("Error", "Failed to add user to Elastic Search");
            return null;
        }
    }

    /**
     * updates a user already in elasticSearch
     *
     * @param user user being updated
     * @return true if the update was successful and false if update was unsuccessful
     */
    public boolean ElasticSearchsyncUser(User user) {
        ElasticSearchController.SyncUserTask syncUser = new ElasticSearchController.SyncUserTask();
        Boolean syncSuccess = new Boolean(false);
        syncUser.execute(user);
        try {
            syncSuccess = syncUser.get();
            Gson gson = new Gson();
            Log.i("Synced User", gson.toJson(user));
            return syncSuccess;
        } catch (Exception e) {
            Log.i("Error", "Failed to sync user");
            return false;
        }
    }

    /**
     * gets an array list of all users on elasticSearch
     *
     * @return users, an array list of all users on elasticSearch
     */
    public ArrayList<User> ElasticSearchGetAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        ElasticSearchController.GetAllUsers getUsers = new ElasticSearchController.GetAllUsers();
        getUsers.execute("");
        try {
            users = getUsers.get();
        } catch (Exception e) {
            Log.i("Error", "Failed to get the users");
        }

        return users;
    }

    /**
     * gets all moods in a 5km range from currentLocation
     *
     * @param currentLocation device
     * @return closeMoods, all moods within 5km of current location
     */
    public ArrayList<Mood> getNearMoods(Location currentLocation) {
        if (currentLocation == null) {
            return new ArrayList<Mood>();
        }
        ArrayList<Mood> closeMoods = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        users = ElasticSearchGetAllUsers();

        for (int x = 0; x < users.size(); x++) {
            User user = users.get(x);
            ArrayList<Mood> usersMoods = user.getMyMoodsList();
            ArrayList<Mood> sortedMoods = sortMoodsByDate(usersMoods);

            if (sortedMoods.size() == 0) {
                continue;
            }

            Mood mostRecentMood = sortedMoods.get(sortedMoods.size() - 1);
            if (mostRecentMood.getLatLng() != null) {
                LatLng moodLocation = mostRecentMood.getLatLng();
                Location toPoint = new Location("to");
                toPoint.setLatitude(moodLocation.latitude);
                toPoint.setLongitude(moodLocation.longitude);
                if (currentLocation.distanceTo(toPoint) <= 5000.0f) {
                    closeMoods.add(mostRecentMood);
                }
            }

        }
        return closeMoods;
    }

    /**
     * gets array list of user's follower requests that are stored in elasticSearch
     * @param user, user with follower requests
     * @return ESuser.getMyFollowerRequests(), array list of follower requests (usernames of requesting followers)
     */
    public ArrayList<String> getFollowerRequests(User user) {
        User ESuser = getElasticSearchUser(user.getName());
        return ESuser.getMyFollowerRequests();
    }

    /**
     * gets array list of user's follow requests that are stored in elasticSearch
     * @param user, user with requests to follow other users
     * @return ESuser.getMyFollowingRequests(), array list of follow requests (usernames of users being requested to follow by user)
     */
    public ArrayList<String> getFollowingList(User user) {
        User ESuser = getElasticSearchUser(user.getName());
        return ESuser.getMyFollowingList();
    }

    /**
     * adds elasticSearch follower requests to user's local follower requests
     * @param user, the user being requested to be followed
     * @return user with updated follower requests list from elasticSearch
     */
    public User addFollowerRequestsToUser(User user){

        ArrayList<String> requests = getFollowerRequests(user);

        for (int x = 0; x < requests.size(); x++) {
            String requester = requests.get(x);
            if (!user.getMyFollowerRequests().contains(requester)){
                user.addToMyFollowerRequests(requester);
            }
        }
        return user;
    }

    /**
     * adds elasticSearch follow requests to user's local follow requests
     * @param user, requesting to follow other users
     * @return user with updated follow requests list from elasticSearch
     */
    public User addFollowingToUser(User user) {
        ArrayList<String> following = getFollowingList(user);

        for (int x = 0; x < following.size(); x++) {
            String friend = following.get(x);
            if (!user.getMyFollowingList().contains(friend)){
                user.addToMyFollowingList(friend);
                user.removeFollowRequest(friend);
            }
        }
        return user;

    }



}
