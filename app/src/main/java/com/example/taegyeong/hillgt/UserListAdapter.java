package com.example.taegyeong.hillgt;

import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by taegyeong on 16. 6. 11..
 */
public class UserListAdapter extends  RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private Context context;
    private String[] userIDs;
    private String[] userNames;

    private NunchiService service;

    public UserListAdapter(Context context, NunchiService service){
        this.context = context;
        this.service = service;
        userIDs = new String[service.userListMap.size()];
        userNames = new String[service.userListMap.size()];
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
    public void onBindViewHolder(UserListAdapter.ViewHolder holder, final int position) {
        holder.userNameText.setText(userNames[position]);
        holder.userNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHillgt(userIDs[position]);
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
        newHillgt.put("timestamp",""+System.currentTimeMillis());
        service.rootRef.child(service.HILLGT_REF).child(targetUser).push()
                .setValue(newHillgt);
    }

//    @Override
//    public int getItemViewType(int position){
//        if (fileTracker.getCurrentFileNum() == 0)
//            return VIEW_TYPE_EMPTY;
//        return VIEW_TYPE_ELEM;
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameText = (TextView)itemView.findViewById(R.id.userlist_elem_username);
        }
    }
}