package com.example.taegyeong.hillgt.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yjchang on 6/13/16.
 */
public class NunchitbabService extends NotificationListenerService {

    private final int SAMPLE_PERIOD_US = 1000;

    private final String DEBUGLOG_NOTI = "notification_log";
    private final String DEBUGLOG_SENSOR = "sensor_log";

    private BroadcastReceiver ringerReceiver;
    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver screenReceiver;
    private BroadcastReceiver unlockReceiver;

    private SensorManager mSensorManager;
    private SensorEventListener proximityListener;
    private SensorEventListener lightListener;
    private SensorEventListener accelListener;

    private double lastMagAccel;
    private int lastBatteryState;

    public static final List<HillGtRequest> mHillGtRequestList = new ArrayList<>();
    private class HillGtRequest {
        int id;
        boolean attentive;
        boolean mightAttentive;
        long timestamp;
        HillGtRequest(int id, long timestamp) {
            this.id = id;
            this.attentive = false;
            this.mightAttentive = false;
            this.timestamp = timestamp;
        }
    }
    private void buildHillGtRequest(int id) {
        mHillGtRequestList.add(new HillGtRequest(id,
                Calendar.getInstance().getTimeInMillis()));
        Log.d("HILLGT_RQ", "NEW HillGt RQ #" + id);
    }
    private void positiveActionDetected() {
        for (HillGtRequest hgr : mHillGtRequestList) {
            hgr.attentive = true;
        }
    }
    private void negativeActionDetected() {
        for (HillGtRequest hgr : mHillGtRequestList) {
            hgr.mightAttentive |= hgr.attentive;
            hgr.attentive = false;
        }
    }
    public static String getHillGtValue(int notificationId) {
        Iterator<HillGtRequest> i = mHillGtRequestList.iterator();
        HillGtRequest hgr = null;
        String retval = null;
        while (i.hasNext()) {
            hgr = i.next();
            if (hgr.id == notificationId) {
                retval = hgr.attentive ? "Available" :
                        (hgr.mightAttentive ? "MightAvailable" : "NotAvailable");
                i.remove();
            }
        }
        Log.d("HILLGT_RQ", "REMOVE HillGt RQ #" + notificationId + " : " + retval);
        assert(retval != null);
        return retval;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        Log.d(DEBUGLOG_NOTI, "onNotificationPosted ["+sbn.getId()+", "+sbn.getPackageName()+"] "
                + extras.getString(Notification.EXTRA_TITLE));

        if (sbn.getPackageName().equals(getPackageName())) {
            buildHillGtRequest(sbn.getId());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        Log.d(DEBUGLOG_NOTI, "onNotificationRemoved ["+sbn.getId()+", "+sbn.getPackageName()+"] "
                + extras.getString(Notification.EXTRA_TITLE));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(DEBUGLOG_NOTI, "service created");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        defineBroadcastReceivers();
        defineSensorListeners();
        registerBroadcastReceiver();
        registerSensorListener(SAMPLE_PERIOD_US);
    }

    @Override
    public void onDestroy(){
        Log.d(DEBUGLOG_NOTI, "service destroyed");

        unregisterAll();

        super.onDestroy();
    }

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        return super.getActiveNotifications();
    }


    private void defineBroadcastReceivers() {

        ////////////
        // RINGER //
        ////////////
        ringerReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                int ringerMode = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE))
                        .getRingerMode();
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_SILENT:
                        Log.d("getRingerMode", "Silent");
                        positiveActionDetected();
                        negativeActionDetected();
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        Log.d("getRingerMode", "Vibrate");
                        positiveActionDetected();
                        break;
                    case AudioManager.RINGER_MODE_NORMAL:
                        Log.d("getRingerMode", "Bell");
                        positiveActionDetected();
                        break;
                }
            }
        };

        /////////////
        // BATTERY //
        /////////////
        batteryReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                /**
                 * BATTERY_STATUS_UNKNOWN       1
                 * BATTERY_STATUS_CHARGING      2
                 * BATTERY_STATUS_DISCHARGING   3
                 * BATTERY_STATUS_NOT_CHARGING  4
                 * BATTERY_STATUS_FULL          5
                 * */
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                            BatteryManager.BATTERY_STATUS_UNKNOWN);
                    if (batteryStatus == lastBatteryState)
                        return;
                    lastBatteryState = batteryStatus;
                    Log.d(DEBUGLOG_SENSOR, "battery status " + batteryStatus);
                }
            }
        };

        ////////////
        // SCREEN //
        ////////////
        screenReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                /**
                 * SCREEN_OFF   0
                 * SCREEN_ON    1
                 * */
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_SCREEN_OFF)){
                    Log.d(DEBUGLOG_SENSOR, "screen off");
                } else if (action.equals(Intent.ACTION_SCREEN_ON)){
                    Log.d(DEBUGLOG_SENSOR, "screen on");
                    positiveActionDetected();
                }
            }
        };

        ////////////
        // UNLOCK //
        ////////////
        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_USER_PRESENT)) {
                    Log.d(DEBUGLOG_SENSOR, "unlocked");
                    positiveActionDetected();
                }
            }
        };
    }

    private void defineSensorListeners(){

        ///////////////
        // PROXIMITY //
        ///////////////
        proximityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float proximityDistance = event.values[0];
                Log.d(DEBUGLOG_SENSOR,"proximity sensor changed: "+proximityDistance+"(cm)");
                JSONObject json = new JSONObject();
                positiveActionDetected();
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        ///////////
        // LIGHT //
        ///////////
        lightListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lightLevel = event.values[0];
                Log.d(DEBUGLOG_SENSOR,"light sensor changed: "+lightLevel+"(lx)");
                positiveActionDetected();
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        ///////////
        // ACCEL //
        ///////////
        accelListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float xAccel = event.values[0];
                float yAccel = event.values[1];
                float zAccel = event.values[2];
                double magAccel = Math.sqrt(xAccel * xAccel + yAccel * yAccel + zAccel * zAccel);
                if (Math.abs(magAccel - lastMagAccel) < 3.0) {
                    return;
                }
                lastMagAccel = magAccel;
                Log.d(DEBUGLOG_SENSOR,"accel sensor changed: "+magAccel+"(m/s^2)");
                positiveActionDetected();
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    public void registerBroadcastReceiver(){

        // RINGER
        IntentFilter ringerFilter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(ringerReceiver, ringerFilter);

        // BATTERY
        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryFilter);

        // SCREEN
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, screenFilter);

        // UNLOCK
        IntentFilter unlockFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(unlockReceiver, unlockFilter);
    }

    public void registerSensorListener(int samplePeriod){

        // PROXIMITY
        Sensor proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(proximityListener, proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL, samplePeriod);

        // LIGHT
        Sensor lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(lightListener, lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL, samplePeriod);

        // ACCEL
        Sensor accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(accelListener, accelSensor,
                SensorManager.SENSOR_DELAY_NORMAL, samplePeriod);
    }

    public void unregisterAll(){

        // Broadcast Receiver
        unregisterReceiver(ringerReceiver);
        unregisterReceiver(batteryReceiver);
        unregisterReceiver(screenReceiver);
        unregisterReceiver(unlockReceiver);

        // Sensor Listener
        mSensorManager.unregisterListener(proximityListener);
        mSensorManager.unregisterListener(lightListener);
        mSensorManager.unregisterListener(accelListener);
    }

}
