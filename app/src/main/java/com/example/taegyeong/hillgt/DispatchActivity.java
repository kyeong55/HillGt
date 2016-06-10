package com.example.taegyeong.hillgt;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class DispatchActivity extends AppCompatActivity {

    private final String PREFS_KEY_USERID = "hillgt_userid";
    private final String PREFS_KEY_USERNAME = "hillgt_username";

    private String userID;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
    }

    private void setupUser() {
        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
        userID = prefs.getString(PREFS_KEY_USERID, null);
        userName = prefs.getString(PREFS_KEY_USERNAME, null);
        if (userID == null) {
            Random r = new Random();
            // Assign a random user name if we don't have one saved.
            userID = "User_" + r.nextInt(100000);
            prefs.edit().putString(PREFS_KEY_USERID, userID).commit();
        }
        if (userName == null) {

        }

        TextView idText = (TextView) findViewById(R.id.user_id);
        assert idText != null;
        idText.setText("Your ID: "+ userID);
    }
}
