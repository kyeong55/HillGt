package com.example.taegyeong.hillgt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NunchiService nunchiService;

//    private String userID;
//    private String userName;

    private SharedPreferences prefs;

    private UserListAdapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        prefs = getApplication().getSharedPreferences("HillGtPrefs", 0);

        TextView toolbar = (TextView) findViewById(R.id.toolbar);
        TextView subtitle = (TextView) findViewById(R.id.subtitle);
        assert toolbar != null;
        assert subtitle != null;
        toolbar.setTypeface(BrandonTypeface.branBold);
        subtitle.setTypeface(BrandonTypeface.branRegular
        );

//        userID = getIntent().getStringExtra("user_id");
//        userName = getIntent().getStringExtra("user_name");


        Intent nunchiIntent = new Intent(this, NunchiService.class);
        nunchiIntent.putExtra("user_id", getIntent().getStringExtra("user_id"));
        nunchiIntent.putExtra("user_name", getIntent().getStringExtra("user_name"));
        startService(nunchiIntent);
        bindService(nunchiIntent, nunchiConnection, Context.BIND_AUTO_CREATE);
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
                getUserList();
            }
            else {
                nunchiService.mConnectedListener = nunchiService.rootRef.getRoot().child(".info/connected")
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        nunchiService.connected = (Boolean) dataSnapshot.getValue();
                        if (nunchiService.connected) {
                            getUserList();
                        } else {
                            if (nunchiService.childEventListener != null) {
                                nunchiService.rootRef.child(nunchiService.HILLGT_REF).child(nunchiService.userID)
                                        .removeEventListener(nunchiService.childEventListener);
                                nunchiService.childEventListener = null;
                            }
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

    public void getUserList(){
        final String userListKey = "UserList";
        final Firebase userListRef = nunchiService.rootRef.child(userListKey);

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nunchiService.userListMap = (HashMap) dataSnapshot.getValue();
                if(nunchiService.userListMap == null) {
                    Firebase newRef = userListRef.push();
                    newRef.setValue(nunchiService.userName);
                    nunchiService.userID = newRef.getKey();
                    prefs.edit().putString("hillgt_userid",nunchiService.userID).apply();
                }
                else if (nunchiService.userID == null) {
                    Firebase newRef = userListRef.push();
                    newRef.setValue(nunchiService.userName);
                    nunchiService.userID = newRef.getKey();
                    prefs.edit().putString("hillgt_userid",nunchiService.userID).apply();
                }
                else if (!nunchiService.userListMap.containsKey(nunchiService.userID)) {
                    Firebase newRef = userListRef.push();
                    newRef.setValue(nunchiService.userName);
                    nunchiService.userID = newRef.getKey();
                    prefs.edit().putString("hillgt_userid",nunchiService.userID).apply();
                }
                if (nunchiService.userListMap.containsKey(nunchiService.userID)) {
                    setUserList();
                    nunchiService.addListener();
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public void setUserList(){
        userListAdapter = new UserListAdapter(getApplicationContext(),nunchiService);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        RecyclerView fileListView = (RecyclerView) findViewById(R.id.userlist);
        assert fileListView != null;
        fileListView.setHasFixedSize(true);
        fileListView.setLayoutManager(layoutManager);
        fileListView.setAdapter(userListAdapter);
        fileListView.setVerticalScrollBarEnabled(true);
    }
}
