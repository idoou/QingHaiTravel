
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.SpecialDetailActivity;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.QHCollectionRoute;
import com.ziyou.selftravel.model.QHRoute;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.support.OnClickListener;

import java.util.List;

public class CollectionRoutelListAdapter extends AppendableAdapter<QHCollectionRoute> {
    private Activity mContext;

    public CollectionRoutelListAdapter(Activity context) {
        this.mContext = context;
    }

    public CollectionRoutelListAdapter(Activity context, List<QHCollectionRoute> items) {
        this(context);
        this.mDataItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collect_routes_item , parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        final QHCollectionRoute item = mDataItems.get(position);
        viewHolder.itemView.setTag(item);
        viewHolder.netImage.setTag(item);


        if (item.image_url != null) {
            RequestManager.getInstance().getImageLoader().get(item.image_url,ImageLoader.getImageListener(viewHolder.netImage, R.drawable.bg_image_hint, R.drawable.bg_image_hint));
        }

        viewHolder.item_name.setText(item.title);

        /*for(int i = 0;i<item.size();i++){
            if(i==scenic.spot_list.size()-1)
                result+=scenic.spot_list.get(i).scenic_name;
            else
                result+=(scenic.spot_list.get(i).scenic_name+"---");
        }*/
        viewHolder.item_routes.setText(item.title);


    }

    static class VideoViewHolder extends ViewHolder {
        NetworkImageView netImage;
        TextView item_name,item_routes;

        public VideoViewHolder(View itemView) {
            super(itemView);
            netImage = (NetworkImageView) itemView.findViewById(R.id.iv_cover_image);
            item_name = (TextView) itemView.findViewById(R.id.item_name);
            item_routes = (TextView) itemView.findViewById(R.id.item_routes);

        }

    }
}
