package com.example.austin.inthemood;

/**
 * Created by Austin on 2017-02-26.
 */

import java.util.ArrayList;

/**
 * The user class stores all information about a given user including his list of moods, follower and following
 * requests and followers and followed users.
 */
public class User{
    private String name;
    private String password;
    private String elasticSearchID;
    private ArrayList<Mood> myMoodsList;
    private ArrayList<String> myFollowersList;
    private ArrayList<String> myFollowingList;
    private ArrayList<String> myFollowerRequests;
    private ArrayList<String> myFollowRequests;

    /**
     * Instantiates a new User.
     *
     * @param name the name of the user
     * @param password the chosen password of the user
     */
    public User(String name, String password) {

        this.name = name;
        this.password = password;
        this.elasticSearchID = new String();
        this.myMoodsList = new ArrayList<Mood>();
        this.myFollowersList = new ArrayList<String >();
        this.myFollowingList = new ArrayList<String>();
        this.myFollowerRequests = new ArrayList<String>();
        this.myFollowRequests = new ArrayList<String>();

    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets user's login password
     *
     * @return login password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets user's login password
     *
     * @param password user's login password
     */

    public void setPassword(String password){

        this.password = password;
    }

    /**
     * gets users elastic search ID
     * @return user's elastic search ID
     */
    public String getElasticSearchID() {return elasticSearchID;}

    /**
     * Sets users elastic search ID. Should be called after adding a user to the server
     * @param esID String containing the user's unique elastic search id from the server
     */
    public void setElasticSearchID(String esID) {
        this.elasticSearchID = esID;
    }

    /**
     * gets array list of user's moods
     *
     * @return array list of moods
     */
    public ArrayList<Mood> getMyMoodsList(){
        return myMoodsList;
    }

    /**
     * adds mood to user's myMoodList
     *
     * @param mood a mood to add to list
     */
    public void addMood(Mood mood){
        this.myMoodsList.add(mood);
    }

    /**
     * removes mood from user's myMoodList
     *
     * @param mood a mood to remove from list
     */
    public void removeMood(Mood mood){
        this.myMoodsList.remove(mood);
    }

    /**
     *gets user's (owner's) followers list (people following him)
     *
     * @return list of followers following user (owner)
     */
    public ArrayList<String> getMyFollowersList(){
        return myFollowersList;
    }

    /**
     *adds a follower to user's (owner's) followers list
     *
     * @param followerName person following the user (owner)
     */
    public void addToMyFollowersList(String followerName){
        myFollowersList.add(followerName);
    }

    /**
     *gets user's (owner's) list of people the user (owner) is following
     *
     * @return list of people the user is following
     */
    public ArrayList<String> getMyFollowingList(){
        return myFollowingList;
    }

    /**
     * adds a followed user that the user (owner) is following to user's (owner's) list
     *
     * @param followedUserName user being followed by user (owner)
     */
    public void addToMyFollowingList(String followedUserName){
        myFollowingList.add(followedUserName);
    }

    /**
     *gets the list of user's a user (owner) wishes to follow
     *
     * @return the list of user's a user (owner) is requesting to follow
     */
    public ArrayList<String> getMyFollowRequests(){
        return myFollowRequests;
    }

    /**
     * adds a user being requested to be follow by user (owner) to user's (owner's) list of follow requests
     *
     * @param userNameBeingRequestedToFollow a user being requested to be followed by user (owner)
     */
    public void addToMyFollowRequests(String userNameBeingRequestedToFollow) {
        myFollowRequests.add(userNameBeingRequestedToFollow);
    }

    /**
     * User (owner) removes one of the users he wished to follow from his myFollowRequests list
     *
     * @param userNameBeingRequestedToFollow user that user (owner) wished to follow
     */
    public void removeFollowRequest(String userNameBeingRequestedToFollow){
        myFollowRequests.remove(userNameBeingRequestedToFollow);
    }

    /**
     * gets list of users requesting to follow user (owner)
     *
     * @return the list of users requesting to follow user (owner)
     */
    public ArrayList<String> getMyFollowerRequests(){
        return myFollowerRequests;
    }

    /**
     * adds a requesting follower to user's (owner's) myFollowerRequests list
     *
     * @param requestingUserName user name of user requesting to follow user (owner)
     */
    public void addToMyFollowerRequests(String requestingUserName){
        myFollowerRequests.add(requestingUserName);
    }

    /**
     * remove a user requesting to follow user (owner) from myFollowerRequests list
     *
     * @param requestingUserName a user requesting to follow user (owner)
     */
    public void removeFollowerRequest(String requestingUserName){
        myFollowerRequests.remove(requestingUserName);
    }
}

