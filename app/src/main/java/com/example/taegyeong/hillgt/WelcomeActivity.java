package com.example.taegyeong.hillgt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;

public class WelcomeActivity extends AppCompatActivity {

    private final String PREFS_KEY_USERID = "hillgt_userid";
    private final String PREFS_KEY_USERNAME = "hillgt_username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final EditText userNameText = (EditText) findViewById(R.id.welcome_name);
        Button startButton = (Button) findViewById(R.id.welcome_button);

        assert userNameText != null;
        assert startButton != null;

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(userNameText.getText().toString());
            }
        });
    }

    private void signUp(String userName){
        if (userName == null)
            Snackbar.make(findViewById(R.id.welcome_view), "Type your name for HillGt!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        else if (userName.length() == 0)
            Snackbar.make(findViewById(R.id.welcome_view), "Type your name for HillGt!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        else {
//            Random r = new Random();
//            String userID = "User_"+r.nextInt(100000);
            SharedPreferences prefs = getApplication().getSharedPreferences("HillGtPrefs", 0);
//            prefs.edit().putString(PREFS_KEY_USERID, userID).commit();
            prefs.edit().putString(PREFS_KEY_USERNAME, userName).commit();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("user_id", userID);
            intent.putExtra("user_name", userName);
            startActivity(intent);
        }
    }
}
