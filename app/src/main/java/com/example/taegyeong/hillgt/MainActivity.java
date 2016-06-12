package com.example.taegyeong.hillgt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NunchiService nunchiService;

//    private String userID;
//    private String userName;

    private SharedPreferences prefs;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        prefs = getApplication().getSharedPreferences("HillGtPrefs", 0);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        assert mViewPager != null;

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

//        userID = getIntent().getStringExtra("user_id");
//        userName = getIntent().getStringExtra("user_name");

        Intent nunchiIntent = new Intent(this, NunchiService.class);
        nunchiIntent.putExtra("user_id", getIntent().getStringExtra("user_id"));
        nunchiIntent.putExtra("user_name", getIntent().getStringExtra("user_name"));
        startService(nunchiIntent);
        bindService(nunchiIntent, nunchiConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        nunchiService.rootRef.child("UserList").removeEventListener(nunchiService.valueEventListener);
        nunchiService.valueEventListener = null;
        nunchiService.isBinded = false;
        nunchiService.rootRef.child(nunchiService.TOTALSENT_REF).child(nunchiService.userID)
                .removeEventListener(nunchiService.totalSentListener);
        nunchiService.totalSentListener = null;
        unbindService(nunchiConnection);
        super.onDestroy();
    }

    private ServiceConnection nunchiConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            nunchiService = ((NunchiService.NunchiBinder) service).getService();
            nunchiService.isBinded = true;
            if (nunchiService.connected) {
                getUserList();
                updateHistory();
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

        if (nunchiService.valueEventListener == null)
            nunchiService.valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    nunchiService.userListMap = (HashMap) dataSnapshot.getValue();
                    if(nunchiService.userListMap == null) {
                        Firebase newRef = userListRef.push();
                        newRef.setValue(nunchiService.userName);
                        nunchiService.userID = newRef.getKey();
                        nunchiService.rootRef.child(nunchiService.TOTALSENT_REF).child(nunchiService.userID).setValue(0);
                        prefs.edit().putString("hillgt_userid",nunchiService.userID).apply();
                    }
                    else if (nunchiService.userID == null) {
                        Firebase newRef = userListRef.push();
                        newRef.setValue(nunchiService.userName);
                        nunchiService.userID = newRef.getKey();
                        nunchiService.rootRef.child(nunchiService.TOTALSENT_REF).child(nunchiService.userID).setValue(0);
                        prefs.edit().putString("hillgt_userid",nunchiService.userID).apply();
                    }
                    else if (!nunchiService.userListMap.containsKey(nunchiService.userID)) {
                        Firebase newRef = userListRef.push();
                        newRef.setValue(nunchiService.userName);
                        nunchiService.userID = newRef.getKey();
                        nunchiService.rootRef.child(nunchiService.TOTALSENT_REF).child(nunchiService.userID).setValue(0);
                        prefs.edit().putString("hillgt_userid",nunchiService.userID).apply();
                    }
                    if (nunchiService.userListMap != null) {
                        if (nunchiService.userListMap.containsKey(nunchiService.userID)) {
                            mSectionsPagerAdapter.setUserListView(nunchiService);
                            addListener();
                        }
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            };

        userListRef.addValueEventListener(nunchiService.valueEventListener);

        try {
            Firebase fb = nunchiService.rootRef.child(nunchiService.HILLGT_REF).child(nunchiService.userID);
            fb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        nunchiService.totalHistory = 0;
                    else {
                        Map<String, Map<String, String>> historyMap = (HashMap) dataSnapshot.getValue();
                        nunchiService.totalHistory = historyMap.size();
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }catch (Exception e) {nunchiService.totalHistory = 0;}
    }

    public void addListener(){
        if (nunchiService.childEventListener != null) {
            return;
//            rootRef.child(HILLGT_REF).child(userID).removeEventListener(childEventListener);
        }
        nunchiService.childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String,String> addedHillgt = (HashMap)snapshot.getValue();
                nunchiService.makeNotification(addedHillgt.get("id"),addedHillgt.get("name"));
                nunchiService.totalHistory += 1;
                if (nunchiService.isBinded)
                    updateHistory();
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(FirebaseError firebaseError) {}
        };
        nunchiService.rootRef.child(nunchiService.HILLGT_REF).child(nunchiService.userID)
                .addChildEventListener(nunchiService.childEventListener);
    }

    public void updateHistory() {
        Query q = nunchiService.rootRef.child(nunchiService.HILLGT_REF).child(nunchiService.userID).orderByChild("timestamp").startAt(getToday());
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nunchiService.todayHistoryMap = (HashMap) dataSnapshot.getValue();
                mSectionsPagerAdapter.setHistoryView(nunchiService);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public String getToday(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return ""+cal.getTimeInMillis();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        InfoFragment infoFragment;
        UserListFragment userListFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            infoFragment = new InfoFragment();
            userListFragment = new UserListFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return userListFragment;
                case 1:
                    return infoFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        public void setUserListView(NunchiService nunchiService) {
            userListFragment.setUserList(nunchiService);
            infoFragment.setUserInfo(nunchiService);
        }

        public void setHistoryView(NunchiService nunchiService) {
            infoFragment.setHistoryList(nunchiService);
        }
    }

    public class InfoFragment extends Fragment {

        public RecyclerView historyView;
        public HistoryAdapter historyAdapter;
        public TextView userName;
        public TextView userDetail;

        public InfoFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info, container, false);
            userName = (TextView) rootView.findViewById(R.id.info_user_name);
            userDetail = (TextView) rootView.findViewById(R.id.info_user_detail);
            historyView = (RecyclerView) rootView.findViewById(R.id.info_history);
            assert userName != null;
            assert userDetail != null;
            assert historyView != null;
            userName.setTypeface(BrandonTypeface.branBold);
            userDetail.setTypeface(BrandonTypeface.branRegular);

            historyAdapter = new HistoryAdapter(getApplicationContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            historyView.setHasFixedSize(true);
            historyView.setLayoutManager(layoutManager);
            historyView.setAdapter(historyAdapter);
            historyView.setVerticalScrollBarEnabled(true);

            return rootView;
        }

        public void setUserInfo(final NunchiService nunchiService) {
            userName.setText(nunchiService.userName);
            if (nunchiService.totalSentListener != null)
                return;
            nunchiService.totalSentListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        userDetail.setText(dataSnapshot.getValue() + " Hillgt Sent");
                        nunchiService.totalSent = (long) dataSnapshot.getValue();
                    }
                }
                @Override public void onCancelled(FirebaseError firebaseError) {}
            };
            nunchiService.rootRef.child(nunchiService.TOTALSENT_REF).child(nunchiService.userID)
                    .addValueEventListener(nunchiService.totalSentListener);
        }

        public void setHistoryList(NunchiService nunchiService) {
            historyAdapter = new HistoryAdapter(getApplicationContext(),nunchiService);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            historyView.setHasFixedSize(true);
            historyView.setLayoutManager(layoutManager);
            historyView.setAdapter(historyAdapter);
            historyView.setVerticalScrollBarEnabled(true);
        }
    }

    public class UserListFragment extends Fragment {

        public RecyclerView userListView;
        public UserListAdapter userListAdapter;

        public UserListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_userlist, container, false);
            userListView = (RecyclerView) rootView.findViewById(R.id.userlist);
            assert userListView != null;
            return rootView;
        }

        public void setUserList(NunchiService nunchiService){
            userListAdapter = new UserListAdapter(nunchiService);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            userListView.setHasFixedSize(true);
            userListView.setLayoutManager(layoutManager);
            userListView.setAdapter(userListAdapter);
            userListView.setVerticalScrollBarEnabled(true);
        }
    }
}
