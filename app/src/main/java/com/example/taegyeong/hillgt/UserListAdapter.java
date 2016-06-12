package com.example.taegyeong.hillgt;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taegyeong on 16. 6. 11..
 */
public class UserListAdapter extends  RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private String[] userIDs;
    private String[] userNames;
    private ValueEventListener[] listeners;

    private NunchiService service;

    public UserListAdapter(NunchiService service){
        this.service = service;
        userIDs = new String[service.userListMap.size()];
        userNames = new String[service.userListMap.size()];
        listeners = new ValueEventListener[service.userListMap.size()];
        int index=0;
        for(Map.Entry<String,String> mapEntry : service.userListMap.entrySet()) {
            userIDs[index] = mapEntry.getKey();
            userNames[index] = mapEntry.getValue();
            Log.d("debugging_userlist",mapEntry.toString());
            Log.d("debugging_userlist",userNames[index]);
            index++;
        }
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_elem,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserListAdapter.ViewHolder holder, final int position) {
        holder.userNameText.setText(userNames[position]);
        holder.hillgtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHillgt(userIDs[position]);
                holder.hillgtSent.setVisibility(View.VISIBLE);
                holder.hillgtButton.setClickable(false);
                holder.hillgtButton.setVisibility(View.GONE);
                holder.hillgtProgress.setVisibility(View.VISIBLE);
                holder.nunchiResult.setVisibility(View.GONE);
                new showProgressTask().execute(holder);
                listeners[position] = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("debugging_response",dataSnapshot.toString());
                        if (dataSnapshot.getValue() != null) {
                            setImoticon((String) dataSnapshot.getValue(), holder.nunchiResult);
                            service.rootRef.child(service.NUNCHI_REF).child(service.userID).child(userIDs[position])
                                    .removeEventListener(listeners[position]);
                            service.rootRef.child(service.NUNCHI_REF).child(service.userID).child(userIDs[position])
                                    .removeValue();
                            holder.hillgtSent.setVisibility(View.INVISIBLE);
                            holder.hillgtProgress.setVisibility(View.GONE);
                            holder.nunchiResult.setVisibility(View.VISIBLE);
                            holder.hillgtButton.setClickable(true);
                            holder.hillgtButton.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override public void onCancelled(FirebaseError firebaseError) {}
                };
                service.rootRef.child(service.NUNCHI_REF).child(service.userID).child(userIDs[position])
                        .addValueEventListener(listeners[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userIDs.length;
    }

    public void requestHillgt(String targetUser) {
        Map<String,String> newHillgt = new HashMap<>();
        newHillgt.put("id",service.userID);
        newHillgt.put("name",service.userName);
        newHillgt.put("timestamp",""+ Calendar.getInstance().getTimeInMillis());//System.currentTimeMillis());
        service.rootRef.child(service.HILLGT_REF).child(targetUser).push()
                .setValue(newHillgt);
        service.rootRef.child(service.TOTALSENT_REF).child(service.userID).setValue(service.totalSent + 1);
    }

    public void setImoticon(String nunchi, TextView result) {
        if (nunchi.compareTo("Available") == 0) {
            result.setText(" ... ^o^");
        } else if (nunchi.compareTo("MightAvailable") == 0) {
            result.setText(" ... -_-?");
        } else if (nunchi.compareTo("NotAvailable") == 0) {
            result.setText(" ... zZzZ");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        TextView nunchiResult;
        ProgressBar hillgtProgress;
        View hillgtSent;
        View hillgtButton;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameText = (TextView)itemView.findViewById(R.id.userlist_elem_username);
            nunchiResult = (TextView)itemView.findViewById(R.id.userlist_elem_result);
            hillgtProgress = (ProgressBar)itemView.findViewById(R.id.userlist_elem_progressbar);
            hillgtSent = itemView.findViewById(R.id.userlist_elem_sent);
            hillgtButton = itemView.findViewById(R.id.userlist_elem_button);
            userNameText.setTypeface(BrandonTypeface.branBold);
            nunchiResult.setTypeface(BrandonTypeface.branBold);
        }
    }

    public class showProgressTask extends AsyncTask<UserListAdapter.ViewHolder, Void, UserListAdapter.ViewHolder> {
        @Override
        public UserListAdapter.ViewHolder doInBackground(UserListAdapter.ViewHolder... params) {
            try {
                Thread.sleep(1000*2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return params[0];
        }

        @Override
        public void onPostExecute(UserListAdapter.ViewHolder result) {
            super.onPostExecute(result);
            result.hillgtSent.setVisibility(View.GONE);
            result.hillgtButton.setVisibility(View.VISIBLE);
        }
    }
}