package com.example.austin.inthemood;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UserTest {
    /**
     * Test get name.
     */
    @Test
    public void testGetName() {
        User user = new User("Alice", "A123");
        assertEquals("Alice", user.getName());
    }

    /**
     * Test set name.
     */
    @Test
    public void testSetName() {
        User user = new User("Theodore teh Great", "T123");
        user.setName("Theodore the Great");
        assertEquals("Theodore the Great", user.getName());
    }

    /**
     * Test get password.
     */
    @Test
    public void testGetPassword() {
        User user = new User("Alice", "A123");
        assertEquals("A123", user.getPassword());

    }

    /**
     * Test set password.
     */
    @Test
    public void testSetPassword() {
        User user = new User("Theodore teh Great", "T123");
        user.setPassword("T123");
        assertEquals("T123", user.getPassword());
    }
    /**
     * Test getMyMoodsList function.
     */
    @Test
    public void testGetMyMoodsList(){
        User user = new User("Theodore teh Great", "T123");
        Mood mood1 = new Mood("Theodore teh Great");
        user.addMood(mood1);
        assertEquals("Test Description", user.getMyMoodsList().get(0).getMoodDescription());
    }
    /**
     * Test addMood function.
     */
    @Test
    public void testAddMood(){
        User user = new User("Theodore teh Great", "T123");
        Mood mood1 = new Mood("Theodore teh Great");
        user.addMood(mood1);
        assertEquals(1, user.getMyMoodsList().size());
    }
    /**
     * Test removeMood function.
     */
    @Test
    public void testRemoveMood(){
        User user = new User("Theodore teh Great", "T123");
        Mood mood1 = new Mood("Theodore teh Great");
        user.addMood(mood1);
        user.removeMood(mood1);
        assertEquals(0, user.getMyMoodsList().size());
    }
    /**
     * Test getMyFollowersList function.
     */
    @Test
    public void testGetMyFollowersList(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowersList("user2");
        assertEquals(1, user1.getMyFollowersList().size());
        assertEquals("user2",user1.getMyFollowersList().get(0));
    }
    /**
     * Test addToMyFollowersList function.
     */
    @Test
    public void testAddToMyFollowersList(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowersList("user2");
        assertEquals(1, user1.getMyFollowersList().size());
        assertEquals("user2",user1.getMyFollowersList().get(0));
    }


    /**
     * Test getMyFollowingList function.
     */
    @Test
    public void testGetMyFollowingList(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowingList("user2");
        assertEquals(1, user1.getMyFollowingList().size());
        assertEquals("user2",user1.getMyFollowingList().get(0));
    }
    /**
     * Test addToMyFollowingList function.
     */
    @Test
    public void testAddToMyFollowingList(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowersList("user2");
        assertEquals(1, user1.getMyFollowersList().size());
        assertEquals("user2",user1.getMyFollowersList().get(0));
    }
    /**
     * Test getMyFollowRequests function.
     */
    @Test
    public void testGetMyFollowRequests(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowRequests("user2");
        assertEquals(1, user1.getMyFollowRequests().size());
        assertEquals("user2",user1.getMyFollowRequests().get(0));
    }
    /**
     * Test addToMyFollowRequests function.
     */
    @Test
    public void testAddToMyFollowRequests(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowRequests("user2");
        assertEquals(1, user1.getMyFollowRequests().size());
        assertEquals("user2",user1.getMyFollowRequests().get(0));
    }
    /**
     * Test removeFollowRequest function.
     */
    @Test
    public void testRemoveFollowRequest(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowRequests("user2");
        user1.removeFollowRequest("user2");
        assertEquals(0, user1.getMyFollowRequests().size());
    }

    /**
     * Test getMyFollowerRequests function.
     */
    @Test
    public void testGetMyFollowerRequests(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowerRequests("user2");
        assertEquals(1, user1.getMyFollowerRequests().size());
        assertEquals("user2",user1.getMyFollowerRequests().get(0));
    }
    /**
     * Test addToMyFollowerRequests function.
     */
    @Test
    public void testAddToMyFollowerRequests(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowerRequests("user2");
        assertEquals(1, user1.getMyFollowerRequests().size());
        assertEquals("user2",user1.getMyFollowerRequests().get(0));
    }
    /**
     * Test removeFollowerRequest function.
     */
    @Test
    public void testRemoveFollowerRequest(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        user1.addToMyFollowerRequests("user2");
        user1.removeFollowerRequest("user2");
        assertEquals(0, user1.getMyFollowerRequests().size());
    }
}