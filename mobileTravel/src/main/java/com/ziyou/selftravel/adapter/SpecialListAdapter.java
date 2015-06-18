
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.RestaurantDetailActivity;
import com.ziyou.selftravel.activity.SpecialDetailActivity;
import com.ziyou.selftravel.activity.WebActivity;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.HomeRestuarant;
import com.ziyou.selftravel.model.HomeSpecial;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.widget.RatioImageView;

import java.util.List;

public class SpecialListAdapter extends AppendableAdapter<QHStrategy> {
    private Activity mContext;

    public SpecialListAdapter(Activity context) {
        this.mContext = context;
    }

    public SpecialListAdapter(Activity context, List<QHStrategy> items) {
        this(context);
        this.mDataItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_special_item, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        final QHStrategy item = mDataItems.get(position);
        viewHolder.itemView.setTag(item);
        viewHolder.netImage.setTag(item);

        viewHolder.netImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onValidClick(View v) {

                QHStrategy item = (QHStrategy) v.getTag();
                Bundle bundle = new Bundle();
                bundle.putParcelable("guide",item);

                Intent intent  = new Intent();
                intent.putExtra("guide",bundle);
//                intent.putExtra(Const.SPECIAL_DETAIL_DATA, item);
                intent.setClass(mContext,SpecialDetailActivity.class);
                mContext.startActivity(intent);
            }
        });

        if (item.cover_image != null) {
            RequestManager.getInstance().getImageLoader().get(item.cover_image,
                    ImageLoader.getImageListener(viewHolder.netImage, R.drawable.bg_image_hint,
                            R.drawable.bg_image_hint));
        }

        viewHolder.item_name.setText(item.title);
        viewHolder.item_intro.setText("");
        viewHolder.item_distance.setText(item.created);

    }

    static class VideoViewHolder extends ViewHolder {
        NetworkImageView netImage;
        TextView item_name,item_intro,item_distance;
        ImageView is_audio;

        public VideoViewHolder(View itemView) {
            super(itemView);
            netImage = (NetworkImageView) itemView.findViewById(R.id.iv_cover_image);
            is_audio = (ImageView) itemView.findViewById(R.id.home_item_isaudio);
            item_name = (TextView) itemView.findViewById(R.id.home_item_secnic_name);
            item_intro = (TextView) itemView.findViewById(R.id.home_item_intro);
            item_distance = (TextView) itemView.findViewById(R.id.home_item_secnic_distance);

        }

    }
}
