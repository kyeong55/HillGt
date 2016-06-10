package com.example.taegyeong.hillgt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String FIREBASE_URL = "https://hillgt.firebaseIO.com";
    private final String PREFS_KEY_USERID = "hillgt_userid";
    private final String PREFS_KEY_READLENGTH = "hillgt_read_length";

    private String userID;
    private String userName;
    private int readLength;
    private Map<String,String> userList;
    private Firebase rootRef;
    private ValueEventListener mConnectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);
        rootRef = new Firebase(FIREBASE_URL);

        userID = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        TextView idText = (TextView) findViewById(R.id.user_id);
        assert idText != null;
        idText.setText("Your ID: "+ userID);

        final TextView msgText = (TextView) findViewById(R.id.msg);

        assert msgText != null;
//        rootRef.child(userID).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                if (snapshot.getValue() != null)
//                    msgText.setText(snapshot.getValue().toString());
//            }
//            @Override public void onCancelled(FirebaseError error) { }
//        });

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
        rootRef.child(id).setValue(msg);
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
        mConnectedListener = rootRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
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
        rootRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
    }

    public void getUserList(){
        final String userListKey = "UserList";
        final Firebase userListRef = rootRef.child(userListKey);

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.w("debugging", dataSnapshot.toString());
//                Log.w("debugging", dataSnapshot.getValue().toString());
                userList = (HashMap) dataSnapshot.getValue();
                if(userList == null) {
                    userListRef.setValue(userList);
                    Map<String, String> newUser = new HashMap<>();
                    newUser.put(userID,userName);
                    userListRef.setValue(newUser);
                }
                else if (!userList.containsKey(userID)) {
                    Map<String, String> newUser = new HashMap<>();
                    newUser.put(userID,userName);
                    userListRef.setValue(newUser);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public void hillgt(String targetUser){
        rootRef.child(targetUser).push().setValue("hillgt");
    }
}
