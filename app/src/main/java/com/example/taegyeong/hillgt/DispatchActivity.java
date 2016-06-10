package com.example.taegyeong.hillgt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class DispatchActivity extends AppCompatActivity {

    private final String PREFS_KEY_USERID = "hillgt_userid";
    private final String PREFS_KEY_USERNAME = "hillgt_username";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        setupUser();
    }

    private void setupUser() {
        SharedPreferences prefs = getApplication().getSharedPreferences("HillGtPrefs", 0);
        String userID = prefs.getString(PREFS_KEY_USERID, null);
        String userName = prefs.getString(PREFS_KEY_USERNAME, null);
        if ((userID == null)||(userName == null)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("user_id",userID);
            intent.putExtra("user_name",userName);
            startActivity(intent);
        }
    }
}
