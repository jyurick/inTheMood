package com.example.austin.inthemood;

/**
 * Created by olivier on 2017-03-11.
 */

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class DataControlerTest {
    /**
     * Test get userList.
     */
    @Test
    public void testGetUserList(){
        User user1 = new User("user1", "p1");
        DataController controler = new DataController(user1);
        assertEquals(1, controler.getUserList().size());
        assertEquals(user1, controler.getUserList().get(0));
        assertEquals("user1", controler.getUserList().get(0).getName());
    }
    /**
     * Test addToUserList.
     */
    @Test
    public void testAddToUserList(){
        User user1 = new User("user1", "p1");
        User user2 = new User("user2", "p2");
        DataController controler = new DataController(user1);
        controler.addToUserList(user2);
        assertEquals(2, controler.getUserList().size());
        assertEquals(user1, controler.getUserList().get(0));
        assertEquals(user2, controler.getUserList().get(1));
        assertEquals("user1", controler.getUserList().get(0).getName());
        assertEquals("user2", controler.getUserList().get(1).getName());
    }




    /**
     * Test searchForUserByName
     */
    @Test
    public void testSearchForUserByName(){
        User user1 = new User("user1", "p1");
        DataController controler = new DataController(user1);
        assertEquals(user1, controler.searchForUserByName("user1"));
        assertEquals(controler.searchForUserByName("user2"), null);
    }
    /**
     * Test sortMoodsByDate
     */
    @Test
    public void testSortMoodsByDate(){
        User user1 = new User("user1", "p1");
        DataController controler = new DataController(user1);
        Mood mood1 = new Mood("user1");
        Mood mood2 = new Mood("user1");
        Date date = new Date(97, 1, 23);
        mood2.setMoodDate(date);
        user1.addMood(mood1);
        user1.addMood(mood2);
        assertEquals(user1.getMyMoodsList().get(0), mood1);
        assertEquals(user1.getMyMoodsList().get(1), mood2);
        ArrayList<Mood> filteredMoods = controler.sortMoodsByDate(user1.getMyMoodsList());
        assertEquals(filteredMoods.get(0), mood2);
        assertEquals(filteredMoods.get(1), mood1);
    }

    /**
     * Test filterByMood
     */
    @Test
    public void testFilterByMood(){
        User user1 = new User("user1", "p1");
        DataController controler = new DataController(user1);
        Mood mood1 = new Mood("user1");
        Mood mood2 = new Mood("user1");
        mood1.setMoodName("happy");
        mood2.setMoodName("grumpy");
        user1.addMood(mood1);
        user1.addMood(mood2);
        ArrayList<Mood> filteredMoodList = controler.filterByMood("happy", user1.getMyMoodsList());
        assertEquals(mood1, filteredMoodList.get(0));
        assertEquals(filteredMoodList.size(), 1);
    }
    /**
     * Test filterByWeek method and that moods are sorted by date
     */
    @Test
    public void testFilterByWeek(){
        User user1 = new User("user1", "p1");
        DataController controler = new DataController(user1);
        Mood mood1 = new Mood("user1");
        Mood mood3 = new Mood("user1");
        Date dateOld = new Date(98, 3, 8);
        mood1.setMoodDate(dateOld);
        user1.addMood(mood3);
        user1.addMood(mood1);
        assertEquals(user1.getMyMoodsList().size(), 2);
        ArrayList<Mood> filteredList = controler.filterByWeek(user1.getMyMoodsList());
        assertEquals(filteredList.size(), 1);
        assertEquals(filteredList.get(0), mood3);
    }


    /**
     * Test filterByTrigger
     */
    @Test
    public void testFilterByTrigger(){
        User user1 = new User("user1", "p1");
        DataController controler = new DataController(user1);
        Mood mood1 = new Mood("user1");
        Mood mood2 = new Mood("user1");
        mood2.setMoodDescription("grumpy");
        user1.addMood(mood1);
        user1.addMood(mood2);
        ArrayList<Mood> filteredMoodList = controler.filterByTrigger("grumpy", user1.getMyMoodsList());
        assertEquals(mood2, filteredMoodList.get(0));

    }

    /**
     * Test signing out of the data controller
     */
    @Test
    public void testSignout() {
        User user = new User("guy","guy");
        DataController controller = new DataController(user);
        controller.signOut();
        assertEquals(controller.getCurrentUser(), null);
    }

    @Test
    public void testSetAndGetCurrentUser() {
        User first = new User("first","first");
        DataController controller = new DataController(first);
        User second = new User("second","second");
        controller.setCurrentUser(second);
        assertEquals(second.getName(), controller.getCurrentUser().getName());
    }

}
