package com.example.taegyeong.hillgt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    public final String TOTALSENT_REF = "TotalSent";
    public final String NUNCHI_REF = "NunchiReturned";

    public Firebase rootRef;
    public ValueEventListener mConnectedListener;
    public boolean connected;
    public ChildEventListener childEventListener;
    public ValueEventListener valueEventListener;
    public ValueEventListener totalSentListener;
    public String userID;
    public String userName;
    public Map<String,String> userListMap;
    public Map<String,Map<String,String>> todayHistoryMap;
    public int totalHistory;
    public long totalSent;

    public boolean isBinded;

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
    public boolean onUnbind(Intent intent) {
        isBinded = false;
        return super.onUnbind(intent);
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
        connected = false;
    }

    @Override
    public void onDestroy(){
        rootRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        super.onDestroy();
    }
/*
    public void addListener(){
        if (childEventListener != null) {
            return;
//            rootRef.child(HILLGT_REF).child(userID).removeEventListener(childEventListener);
        }
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String,String> addedHillgt = (HashMap)snapshot.getValue();
                int notificationID = makeNotification(addedHillgt.get("name"));
                new HillgtNunchiTask().execute(notificationID);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(FirebaseError firebaseError) {}
        };
        rootRef.child(HILLGT_REF).child(userID).addChildEventListener(childEventListener);
    }
*/
    public void makeNotification(String hillgterID, String hillgterName) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);

//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri soundUri=Uri.parse("android.resource://"+getPackageName()+"/raw/hillgt_high");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(hillgterName+getString(R.string.noti_text))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setTicker("미리보기 입니다.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri);
//                .setContentIntent(pendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Random r = new Random();
        int notificationID = r.nextInt(100000);
        notificationManager.notify(notificationID, notificationBuilder.build());
        new HillgtNunchiTask().execute(notificationID);
    }

    public class HillgtNunchiTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        public Integer doInBackground(Integer... params) {
            try {
                //TODO: detect Nunchi
                Thread.sleep(1000*7);
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
