package com.example.taegyeong.hillgt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

public class WelcomeActivity extends AppCompatActivity {

    private final String PREFS_KEY_USERID = "hillgt_userid";
    private final String PREFS_KEY_USERNAME = "hillgt_username";

    private TextView warningText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final EditText userNameText = (EditText) findViewById(R.id.welcome_name);
        TextView instructionText = (TextView) findViewById(R.id.welcome_instruction);
        TextView startButton = (TextView) findViewById(R.id.welcome_button);
        warningText = (TextView) findViewById(R.id.welcome_warning);

        assert userNameText != null;
        assert instructionText != null;
        assert startButton != null;
        assert warningText != null;

        instructionText.setTypeface(BrandonTypeface.branRegular);
        warningText.setTypeface(BrandonTypeface.branRegular);
        userNameText.setTypeface(BrandonTypeface.branBold);
        startButton.setTypeface(BrandonTypeface.branBold);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(userNameText.getText().toString());
            }
        });
        userNameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.welcome_done ||
                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    signUp(userNameText.getText().toString());
                    return true;
                }
                return false;
            }
        });
        userNameText.requestFocus();
    }

    private void signUp(String userName){
        if (userName == null) {
            Animation alphaAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_visible);
            warningText.startAnimation(alphaAni);
            warningText.setVisibility(View.VISIBLE);
        }
        else if (!isValidName(userName)) {
            Animation alphaAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_visible);
            warningText.startAnimation(alphaAni);
            warningText.setVisibility(View.VISIBLE);
        }
        else {
            SharedPreferences prefs = getApplication().getSharedPreferences("HillGtPrefs", 0);
            prefs.edit().putString(PREFS_KEY_USERNAME, userName).apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("user_name", userName);
            startActivity(intent);
        }
    }

    private boolean isValidName(String newName) {
        if (newName.length() == 0)
            return false;
        String available = " QWERTYUIOPASDFGHJKLZXCVBNM1234567890-!@#$%^&*()_?/";
        for (int i=0;i<newName.length();i++) {
            if (available.indexOf(newName.charAt(i)) < 0)
                return false;
        }
        return true;
    }
}
