package com.example.taegyeong.hillgt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.taegyeong.hillgt.BrandonTypeface;
import com.example.taegyeong.hillgt.service.NunchiService;
import com.example.taegyeong.hillgt.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by taegyeong on 16. 6. 12..
 */
public class HistoryAdapter extends  RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final int VIEW_TYPE_HEAD = 0;
    private final int VIEW_TYPE_BODY = 1;

    private Context context;

    private String[] userNames;
    private String[] timestamps;

    private int totalHistorySize;

    public HistoryAdapter(Context context, final NunchiService service){
        this.context = context;
        userNames = new String[service.todayHistoryMap.size()];
        timestamps = new String[service.todayHistoryMap.size()];

        List<String> list = new ArrayList();
        list.addAll(service.todayHistoryMap.keySet());

        Collections.sort(list,new Comparator(){
            public int compare(Object o1,Object o2){
                Object v1 = service.todayHistoryMap.get(o1).get("timestamp");
                Object v2 = service.todayHistoryMap.get(o2).get("timestamp");
                return ((Comparable) v1).compareTo(v2);
            }
        });
        Collections.reverse(list);

        for (int i=0;i<list.size();i++) {
            userNames[i] = service.todayHistoryMap.get(list.get(i)).get("name");
            timestamps[i] = getRelativeDateTimeString(service.todayHistoryMap.get(list.get(i)).get("timestamp"));
        }
//        int index=0;
//        for(Map.Entry<String,Map<String,String>> mapEntry : service.todayHistoryMap.entrySet()) {
//            userNames[index] = mapEntry.getValue().get("name");
//            timestamps[index] = getRelativeDateTimeString(mapEntry.getValue().get("timestamp"));
//            index++;
//        }
        totalHistorySize = service.totalHistory;
    }
    public HistoryAdapter(Context context) {
        this.context = context;
        userNames = new String[0];
        timestamps = new String[0];
        totalHistorySize = 0;
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
                holder.headerToday.setText(""+userNames.length);
                holder.headerTotal.setText(""+totalHistorySize);
                break;
            case VIEW_TYPE_BODY:
                holder.hillgterName.setText(userNames[position-1]);
                holder.hillgtTime.setText(timestamps[position-1]);
                break;
        }
    }

    public String getRelativeDateTimeString(String timestamp) {
        String relativeTime = (String) DateUtils.getRelativeDateTimeString(context,
                Long.parseLong(timestamp), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
        int comma = relativeTime.indexOf(',');
        if (comma < 0)
            return relativeTime;
        return relativeTime.substring(0,comma);
    }
    @Override
    public int getItemCount() {
        return userNames.length + 1;
    }

    @Override
    public int getItemViewType(int position){
        if (position == 0)
            return VIEW_TYPE_HEAD;
        return VIEW_TYPE_BODY;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView headerToday;
        TextView headerTotal;

        TextView hillgterName;
        TextView hillgtTime;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            switch (viewType){
                case VIEW_TYPE_HEAD:
                    TextView headerTodaySubtitle = (TextView) itemView.findViewById(R.id.history_header_today_subtitle);
                    headerToday = (TextView) itemView.findViewById(R.id.history_header_today_num);
                    TextView headerTotalSubtitle = (TextView) itemView.findViewById(R.id.history_header_total_subtitle);
                    headerTotal = (TextView) itemView.findViewById(R.id.history_header_total_num);
                    headerTodaySubtitle.setTypeface(BrandonTypeface.branBold);
                    headerToday.setTypeface(BrandonTypeface.branBold);
                    headerTotalSubtitle.setTypeface(BrandonTypeface.branBold);
                    headerTotal.setTypeface(BrandonTypeface.branBold);
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