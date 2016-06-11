package com.example.taegyeong.hillgt;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taegyeong on 16. 6. 12..
 */
public class HistoryAdapter extends  RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final int VIEW_TYPE_HEAD = 0;
    private final int VIEW_TYPE_BODY = 1;

    private Context context;
    private String[] statusSubtitle = {"TODAY","TOTAL"};

    private String[] userNames;
    private String[] timestamps;

    private int totalHistorySize;

    public HistoryAdapter(Context context, NunchiService service){
        this.context = context;
        userNames = new String[service.todayHistoryMap.size()];
        timestamps = new String[service.todayHistoryMap.size()];
        int index=0;
        for(Map.Entry<String,Map<String,String>> mapEntry : service.todayHistoryMap.entrySet()) {
            userNames[index] = mapEntry.getValue().get("name");
            timestamps[index] = getRelativeDateTimeString(mapEntry.getValue().get("timestamp"));
            index++;
        }
        totalHistorySize = service.totalHistory;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case VIEW_TYPE_HEAD:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_elem_header,parent,false);
                break;
            case VIEW_TYPE_BODY:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_elem,parent,false);
                break;
        }
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(final HistoryAdapter.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEAD:
                holder.headerSubtitle.setText(statusSubtitle[position]);
                if (position == 0)
                    holder.headerNum.setText(""+userNames.length);
                else if (position == 1)
                    holder.headerNum.setText(""+totalHistorySize);
                break;
            case VIEW_TYPE_BODY:
                holder.hillgterName.setText(userNames[position-2]);
                holder.hillgtTime.setText(timestamps[position-2]);
                break;
        }
    }

    public String getRelativeDateTimeString(String timestamp) {
        return (String) DateUtils.getRelativeDateTimeString(context,
                Long.parseLong(timestamp),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);
    }
    @Override
    public int getItemCount() {
        return userNames.length + 2;
    }

    @Override
    public int getItemViewType(int position){
        if (position < 2)
            return VIEW_TYPE_HEAD;
        return VIEW_TYPE_BODY;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView headerSubtitle;
        TextView headerNum;

        TextView hillgterName;
        TextView hillgtTime;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            switch (viewType){
                case VIEW_TYPE_HEAD:
                    headerSubtitle = (TextView) itemView.findViewById(R.id.history_header_subtitle);
                    headerNum = (TextView) itemView.findViewById(R.id.history_header_num);
                    headerSubtitle.setTypeface(BrandonTypeface.branBold);
                    headerNum.setTypeface(BrandonTypeface.branBold);
                    break;
                case VIEW_TYPE_BODY:
                    hillgterName = (TextView) itemView.findViewById(R.id.history_user);
                    hillgtTime = (TextView) itemView.findViewById(R.id.history_time);
                    hillgterName.setTypeface(BrandonTypeface.branBold);
                    hillgtTime.setTypeface(BrandonTypeface.branBold);
                    break;
            }
        }
    }
}