
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.ViewGroup;

import com.ziyou.selftravel.activity.RestaurantDetailActivity;
import com.ziyou.selftravel.adapter.row.RowGuideTripDay;
import com.ziyou.selftravel.adapter.row.RowRouteDate;
import com.ziyou.selftravel.adapter.row.RowRouteDetail;
import com.ziyou.selftravel.adapter.row.RowTripDate;
import com.ziyou.selftravel.adapter.row.RowTripDay;
import com.ziyou.selftravel.adapter.row.RowTripHeader;
import com.ziyou.selftravel.model.QHRoute;
import com.ziyou.selftravel.model.QHRouteDay;
import com.ziyou.selftravel.model.TripDay;
import com.ziyou.selftravel.model.TripDetail;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.ziyou.selftravel.adapter.TripDetailAdapter.CategoryDate;

/**
 * @author kuloud
 */
public class RouteDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int TYPE_HEADER = 1;
    public final static int TYPE_DATE = 2;
    public final static int TYPE_TRIP_DAY = 0;
    public final int TYPE_ADD = 4;
    public Activity activity;
    private QHRoute mTripDetail;
    private Time mCreateTime;
    private Map<Integer, List<QHRouteDay>> mDataItems;
    public RouteDetailAdapter() {}

    public RouteDetailAdapter(Activity activity) {
        this.activity = activity;
    }

    public QHRoute getTripDetail() {
        return mTripDetail;
    }
    private ArrayList<QHRouteDay> allList = new ArrayList();
    public void setTripDetail(QHRoute tripDetail) {
        mTripDetail = tripDetail;
        if (tripDetail != null && tripDetail.route_day != null) {
            if (!tripDetail.route_day.isEmpty()) {
//                mCreateTime = TimeUtils.parseDate(tripDetail.route_day.get(0).time);
            }
        }
        mDataItems = parseTripDetail(tripDetail);
        resetData();
    }
    private void resetData(){//重新排布数据
        QHRouteDay header = new QHRouteDay();
        header.itemType = TYPE_HEADER;
//        allList.add(header);
        int count = 0;
        int index = 0;
        for (List<QHRouteDay> tripDays : mDataItems.values()) {
            count++;
            QHRouteDay date = new QHRouteDay();//初始化数据
            date.itemType = TYPE_DATE;
            date.day = count;

            allList.add(date);
            for(int j = 0;j<tripDays.size();j++){
                tripDays.get(j).itemType= TYPE_TRIP_DAY;
                tripDays.get(j).index = index;
                allList.add(tripDays.get(j));
                index++;
            }
            int a  = allList.size();
        }
    }
    private static Map<Integer, List<QHRouteDay>> parseTripDetail(QHRoute tripDetail) {
        Map<Integer, List<QHRouteDay>> items = new HashMap<Integer, List<QHRouteDay>>();
        for (QHRouteDay tripDay : tripDetail.route_day) {
            if(items.containsKey(tripDay.day)){
                List<QHRouteDay> tripDays = items.get(tripDay.day);
                if (tripDays == null) {
                    tripDays = new ArrayList<QHRouteDay>();
                }
                tripDays.add(tripDay);
            }else {
                List<QHRouteDay> tripDays = new ArrayList<QHRouteDay>();
                tripDays.add(tripDay);
                items.put(tripDay.day, tripDays);
            }

        }
        return items;
    }
    @Override
    public int getItemCount() {
        return mDataItems == null ? 0 : allList.size();
    }
    private int computeInfoType(int position) {
        QHRouteDay day =  allList.get(position);
//        if (day.itemType == 1) {
//            return TYPE_HEADER;
//        }else
        if (day.itemType == 0) {
            return TYPE_TRIP_DAY;
        }if (day.itemType == 2) {
            return TYPE_DATE;
        }
        return TYPE_ADD;
    }
    public int computeImageIndex(int position) {
        int result = -1;
        QHRouteDay day = allList.get(position);
        if(day.itemType==TYPE_TRIP_DAY){
            return  day.index;
        }
        return result;
    }
    @Override
    public int getItemViewType(int position) {
        return computeInfoType(position);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
//            case TYPE_HEADER:
//                holder = RowTripHeader.onCreateViewHolder(parent);
//                break;
            case TYPE_DATE:
                holder = RowRouteDate.onCreateViewHolder(parent);
                break;
            case TYPE_TRIP_DAY:
                holder = RowRouteDetail.onCreateViewHolder(parent);
                break;
            default:
                Log.e("UNKNOW trip content type!!!");
                holder = RowTripDate.onCreateViewHolder(parent);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        QHRouteDay itemObj = allList.get(position);// getItemObj(position);
        if (itemObj == null) {
            return;
        }
        holder.itemView.setTag(itemObj);

        switch (getItemViewType(position)) {
//            case TYPE_HEADER:
//                    RowTripHeader.onBindViewHolder(holder, position, mTripDetail, mDataItems == null ? 1 : mDataItems.size());
//                break;
            case TYPE_DATE:
                TripDetailAdapter.CategoryDate cate = new CategoryDate();
//                    cate.date = itemObj.date;
                cate.index = itemObj.day;
//                    RowTripDate.onBindViewHolder(holder, position, cate);
                RowRouteDate.onBindViewHolder(holder, position, cate);
                break;
            case TYPE_TRIP_DAY:
                RowRouteDetail.onBindViewHolder(holder, position, itemObj, activity);
//                RowGuideTripDay.onBindViewHolder(holder, position, itemObj,activity);
                break;
            default:
                Log.e("UNKNOW trip detail type!!!");
                break;
        }
    }
//    public static class CategoryDate {
//        public String date;
//        public int index;
//    }
}
