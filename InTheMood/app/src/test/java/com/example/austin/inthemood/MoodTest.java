package com.example.austin.inthemood;

/**
 * Created by samue_000 on 2017-02-26.
 */
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class MoodTest {

    @Test
    public void testMoodName() {
        Mood testMood = new Mood("user1");
        testMood.setMoodName("Happy");
        assertEquals("Happy", testMood.getMoodName());
        testMood.setMoodName("Mad");
        assertEquals("Mad", testMood.getMoodName());
    }

    @Test
    public void testMoodDate() {
        Mood testMood = new Mood("user1");
        Date testDate = new Date();

        testMood.setMoodDate(testDate);
        assertEquals(testDate,testMood.getMoodDate());
        testMood.setMoodDate(testDate);
    }

    @Test
    public void testMoodDescription() {
        Mood testMood = new Mood("user1");

        assertEquals("Test Description",testMood.getMoodDescription());
        testMood.setMoodDescription("Updated Mood description over 20 chars");

        assertEquals("Updated Mood description over 20 chars".substring(0,20),testMood.getMoodDescription());
    }
}
