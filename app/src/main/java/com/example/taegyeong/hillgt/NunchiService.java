package com.example.taegyeong.hillgt;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by taegyeong on 16. 6. 10..
 */
public class NunchiService extends Service {

    public final String FIREBASE_URL = "https://hillgt.firebaseIO.com";
    public final String HILLGT_REF = "HillGtRequest";

    public Firebase rootRef;
    public ValueEventListener mConnectedListener;
    public boolean connected;
    private String userID;
    private String userName;
    public Map<String,String> userList;

    SharedPreferences prefs;

    private final IBinder mBinder = new NunchiBinder();

    private NotificationManager notificationManager;

    public class NunchiBinder extends Binder {
        NunchiService getService() {
            return NunchiService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Firebase.setAndroidContext(this);
        rootRef = new Firebase(FIREBASE_URL);
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        userID = intent.getStringExtra("user_id");
        userName = intent.getStringExtra("user_name");
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        prefs = getApplication().getSharedPreferences("HillGtPrefs", 0);
        connected = false;
    }

    @Override
    public void onDestroy(){
        rootRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        super.onDestroy();
    }

    public void getUserList(){
        final String userListKey = "UserList";
        final Firebase userListRef = rootRef.child(userListKey);

        userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList = (HashMap) dataSnapshot.getValue();
                if(userList == null) {
//                    userListRef.setValue(userList);
//                    Map<String, String> newUser = new HashMap<>();
//                    newUser.put(userID,userName);
//                    userListRef.setValue(newUser);
                    Firebase newRef = userListRef.push();
                    newRef.setValue(userName);
                    userID = newRef.getKey();
                    prefs.edit().putString("hillgt_userid",userID).commit();
                }
                else if (userID == null) {
                    Firebase newRef = userListRef.push();
                    newRef.setValue(userName);
                    userID = newRef.getKey();
                    prefs.edit().putString("hillgt_userid",userID).commit();
                }
                else if (!userList.containsKey(userID)) {
//                    Map<String, String> newUser = new HashMap<>();
//                    newUser.put(userID,userName);
//                    userListRef.setValue(newUser);
                    Firebase newRef = userListRef.push();
                    newRef.setValue(userName);
                    userID = newRef.getKey();
                    prefs.edit().putString("hillgt_userid",userID).commit();
                }
                if (userList.containsKey(userID))
                    addListener();
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public void addListener(){
        rootRef.child(HILLGT_REF).child(userID).addChildEventListener(new ChildEventListener() {
            /*
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
//                    msgText.setText(snapshot.getValue().toString());
                    if (snapshot.getValue() != null) {
                        Log.d("debugging", snapshot.getValue().toString());
                        Map<String,Map> recvHillgt = (HashMap) snapshot.getValue();
                        for (String key : recvHillgt.keySet()) {
                            Map<String,String> hillgetMap = recvHillgt.get(key);
                            Log.d("debugging", hillgetMap.toString());
                            int notificationID = makeNotification(hillgetMap.get("name"));
                            new HillgtNunchiTask().execute(notificationID);
                            if (lastTimestamp.compareTo(hillgetMap.get("timestamp")) < 0) {
                                lastTimestamp = hillgetMap.get("timestamp");
                                Log.d("new time stamp",lastTimestamp);
                            }
                        }
//                        rootRef.child(HILLGT_REF).child(userID).removeValue();
                        prefs.edit().putString("lastTimeStamp",lastTimestamp).commit();
                    }
                }
            }
            */
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String,String> addedHillgt = (HashMap)snapshot.getValue();
                Log.d("debugging",addedHillgt.toString());
                Log.d("noti!!!",addedHillgt.get("name"));
                int notificationID = makeNotification(addedHillgt.get("name"));
                new HillgtNunchiTask().execute(notificationID);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public int makeNotification(String hillgter) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);

//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("HillGt")
                .setContentText(hillgter+"님이 힐끗 보는 중 입니다")
                .setAutoCancel(true);
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Random r = new Random();
        int notificationID = r.nextInt(100000);
        notificationManager.notify(notificationID, notificationBuilder.build());
        return notificationID;
    }

    public class HillgtNunchiTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        public Integer doInBackground(Integer... params) {
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return params[0];
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            notificationManager.cancel(result);
        }
    }
}
