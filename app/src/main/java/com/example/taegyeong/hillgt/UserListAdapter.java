package com.example.taegyeong.hillgt;

import android.support.v7.widget.RecyclerView;
import android.content.Context;
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

    public UserListAdapter(Context context, Map<String,String> userList){
        this.context = context;
        userIDs = new String[userList.size()];
        userNames = new String[userList.size()];
        int index=0;
        for(Map.Entry<String,String> mapEntry : userList.entrySet()) {
            userIDs[index] = mapEntry.getKey();
            userNames[index] = mapEntry.getValue();
            index++;
        }
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        v= LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_elem,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserListAdapter.ViewHolder holder, final int position) {
        holder.userNameText.setText(userNames[position]);
    }

    @Override
    public int getItemCount() {
        return userIDs.length;
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