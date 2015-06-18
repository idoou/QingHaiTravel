
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.RestaurantDetailActivity;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.HomeRestuarant;
import com.ziyou.selftravel.model.QHRoute;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.ConstTools;

import java.util.List;

public class RestaurantListAdapter extends AppendableAdapter<QHRoute> {
    private Activity mContext;
    public RestaurantListAdapter(Activity context) {
        this.mContext = context;
    }
    public RestaurantListAdapter(Activity context, List<QHRoute> items) {
        this(context);
        this.mDataItems = items;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RestViewHolder viewHolder = (RestViewHolder) holder;
        final QHRoute item = mDataItems.get(position);
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                QHRoute route = (QHRoute) v.getTag();
                Bundle bundle  = new Bundle();
                bundle.putParcelable("route",route);
                Intent intent  = new Intent();
                intent.putExtra("route",bundle);
//                intent.putExtra(Const.REST_DETAIL_ID,rest.business_id);
//                intent.putExtra(Const.REST_DETAIL_PRICE,rest.price);
                intent.setClass(mContext,RestaurantDetailActivity.class);
                mContext.startActivity(intent);
            }
        });
//        holder.itemView.setOnClickListener();
        if (item.cover_image != null) {
            RequestManager.getInstance().getImageLoader().get(item.cover_image,
                    ImageLoader.getImageListener(viewHolder.rest_img, R.drawable.bg_image_hint,
                            R.drawable.bg_image_hint));
        }

        viewHolder.rest_name.setText(item.title);
        viewHolder.rest_address.setText(item.title);
//        if(ConstTools.isNumeric(""+item.avg_rating))
//            viewHolder.rest_star.setRating(Float.parseFloat(item.avg_rating));
//        else
//            viewHolder.rest_star.setRating(0.0f);

//        if(ConstTools.isNumeric(item.price)){
//            viewHolder.rest_cost.setText("￥"+item.price);
//            if("0".equals(item.price)){
//                viewHolder.rest_cost.setText("暂无");
//                viewHolder.rest_cost.setTextColor(0xffb4b4b4);
//                viewHolder.rest_cost.setTextSize(12);
//            }
//        }
//        else
//        {
//            viewHolder.rest_cost.setText("暂无");
//            viewHolder.rest_cost.setTextColor(0xffb4b4b4);
//            viewHolder.rest_cost.setTextSize(12);
//        }

//        if(null==item.distance||"null".equals(item.distance))item.distance = "9999999";
//        int dis = Integer.parseInt(item.distance);
//        String str_distance ;
//        if(dis>1000)str_distance =(int)(dis/1000)+"Km";
//        else str_distance = dis+"m";
//        viewHolder.rest_distance.setText(str_distance);
    }

    static class RestViewHolder extends ViewHolder {
        NetworkImageView rest_img;
        TextView rest_name,rest_address,rest_cost,rest_distance;
        RatingBar rest_star;
        public RestViewHolder(View itemView) {
            super(itemView);
            rest_img = (NetworkImageView) itemView.findViewById(R.id.rest_icon);
            rest_name = (TextView) itemView.findViewById(R.id.restaurant_name);
            rest_address = (TextView) itemView.findViewById(R.id.restaurant_address);
            rest_cost = (TextView) itemView.findViewById(R.id.restaurant_average_cost);
            rest_distance = (TextView) itemView.findViewById(R.id.restaurant_distance);
            rest_star = (RatingBar) itemView.findViewById(R.id.testaurant_rating);
        }
    }
}
