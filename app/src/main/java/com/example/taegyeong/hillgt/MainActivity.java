package com.example.taegyeong.hillgt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NunchiService nunchiService;

    private String userID;
    private String userName;
    private Map<String,String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        TextView idText = (TextView) findViewById(R.id.user_id);
        assert idText != null;
        idText.setText("Your ID: "+ userID);

//        final TextView msgText = (TextView) findViewById(R.id.msg);

//        assert msgText != null;
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
        assert sendButton != null;
        assert targetId != null;
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                writeMsg(targetId.getText().toString(),inputText.getText().toString());
                hillgt(targetId.getText().toString());
            }
        });

        Intent nunchiIntent = new Intent(this, NunchiService.class);
        nunchiIntent.putExtra("user_id", getIntent().getStringExtra("user_id"));
        nunchiIntent.putExtra("user_name", getIntent().getStringExtra("user_name"));
        startService(nunchiIntent);
        bindService(nunchiIntent, nunchiConnection, Context.BIND_AUTO_CREATE);
    }

    public void writeMsg(String id,String msg){
        nunchiService.rootRef.child(id).setValue(msg);
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
    public void onDestroy() {
        unbindService(nunchiConnection);
        super.onDestroy();
    }

    private ServiceConnection nunchiConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            nunchiService = ((NunchiService.NunchiBinder) service).getService();
            if (nunchiService.connected) {
            }
            else {
                nunchiService.mConnectedListener = nunchiService.rootRef.getRoot().child(".info/connected")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("debugging","service connected");
                        nunchiService.connected = (Boolean) dataSnapshot.getValue();
                        if (nunchiService.connected) {
                            Log.d("debugging","server connected");
                            nunchiService.getUserList();
//                            nunchiService.addListener();
                        } else {
                        }
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    public void hillgt(String targetUser) {
        Map<String,String> newHillgt = new HashMap<>();
        newHillgt.put("id",userID);
        newHillgt.put("name",userName);
        newHillgt.put("timestamp",""+System.currentTimeMillis());
        nunchiService.rootRef.child(nunchiService.HILLGT_REF).child(targetUser).push()
                .setValue(newHillgt);
    }
}
