package com.example.austin.inthemood;

/**
 * This is to describe the hard testing done.
 *
 * Testing was done for the ElasticSearchController by adding users to the server and then checking
 * the server for them. Also tested syncing users to the server in a similar fashion. Automated testing
 * could not be done because the testing was all on Async tasks which cannot be mocked in Android Studios
 * regular test files.
 *
 *
 * Testing of adding and editing moods was done manually. This was done because of the UI change to
 * put the add button on the banner with no text. This made it so we couldn't selected the button
 * using robotium.
 *
 * Friend requests were tested manually. We did this by requesting to follow a friend, checking the server
 * to ensure the request went through. Then we logged into that friend and accepted the request, once
 * again checking the server to ensure that the follow request was logged.
 */

public class HardTested {
}
