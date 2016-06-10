package com.example.taegyeong.hillgt;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String FIREBASE_URL = "https://hillgt.firebaseIO.com";
    private final String PREFS_KEY_USERID = "hillgt_userid";
    private final String PREFS_KEY_READLENGTH = "hillgt_read_length";

    private String userID;
    private String userName;
    private int readLength;
    private List<String> userList;
    private Firebase mFirebaseRef;
    private ValueEventListener mConnectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        setupUser();

        mFirebaseRef = new Firebase(FIREBASE_URL);

        final TextView msgText = (TextView) findViewById(R.id.msg);

        assert msgText != null;
        mFirebaseRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
//                System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
                if (snapshot.getValue() != null)
                    msgText.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });

        Button sendButton = (Button) findViewById(R.id.send_button);
        final EditText targetId = (EditText) findViewById(R.id.to_id);
        final EditText inputText = (EditText) findViewById(R.id.input_text);
        assert sendButton != null;
        assert targetId != null;
        assert inputText != null;
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                writeMsg(targetId.getText().toString(),inputText.getText().toString());
            }
        });
    }

    public void writeMsg(String id,String msg){
        mFirebaseRef.child(id).setValue(msg);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onStart(){
        super.onStart();
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(MainActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                    getUserList();

                } else {
                    Toast.makeText(MainActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }
    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
    }

    private void setupUser() {
        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
        userID = prefs.getString(PREFS_KEY_USERID, null);
        readLength = prefs.getInt(PREFS_KEY_READLENGTH,-1);
        if (userID == null) {
            Random r = new Random();
            // Assign a random user name if we don't have one saved.
            userID = "User_" + r.nextInt(100000);
            prefs.edit().putString(PREFS_KEY_USERID, userID).commit();
        }
        if (readLength < 0) {
            readLength = 0;
            prefs.edit().putInt(PREFS_KEY_READLENGTH,0).commit();
        }

        TextView idText = (TextView) findViewById(R.id.user_id);
        assert idText != null;
        idText.setText("Your ID: "+ userID);
    }

    public void getUserList(){
        final String userListKey = "userIdList";
        Firebase firebaseUserList = mFirebaseRef.child(userListKey);

        firebaseUserList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.w("debugging", dataSnapshot.toString());
//                Log.w("debugging", dataSnapshot.getValue().toString());
                userList = (List<String>) dataSnapshot.getValue();
                if(userList == null) {
                    userList = new ArrayList<>();
                    userList.add(userID);
                    mFirebaseRef.child(userListKey).setValue(userList);
                }
                else if (userList.indexOf(userID) < 0) {
                    userList.add(userID);
                    mFirebaseRef.child(userListKey).setValue(userList);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
