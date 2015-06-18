
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
import com.ziyou.selftravel.model.QHCollectionStrategy;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.support.OnClickListener;

import java.util.List;

public class CollectionRaidersListAdapter extends AppendableAdapter<QHCollectionStrategy> {
    private Activity mContext;

    public CollectionRaidersListAdapter(Activity context) {
        this.mContext = context;
    }

    public CollectionRaidersListAdapter(Activity context, List<QHCollectionStrategy> items) {
        this(context);
        this.mDataItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collect_radiers_item, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        final QHCollectionStrategy item = mDataItems.get(position);
        viewHolder.itemView.setTag(item);
        viewHolder.netImage.setTag(item);

        viewHolder.netImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onValidClick(View v) {
                QHCollectionStrategy item = (QHCollectionStrategy) v.getTag();
                QHStrategy temp = new QHStrategy();
                temp.id = item.id;
                temp.title = item.title;
                temp.created = item.created;

                Bundle bundle = new Bundle();
                bundle.putParcelable("guide",temp);
                Intent intent  = new Intent();
                intent.putExtra("guide",bundle);
                intent.setClass(mContext,SpecialDetailActivity.class);
                mContext.startActivity(intent);
            }
        });

        if (item.image_url != null) {
            RequestManager.getInstance().getImageLoader().get(item.image_url,
                    ImageLoader.getImageListener(viewHolder.netImage, R.drawable.bg_image_hint,
                            R.drawable.bg_image_hint));
        }

        viewHolder.item_name.setText(item.title);
        viewHolder.item_intro.setText(item.title);
        viewHolder.item_date.setText(item.created);

    }

    static class VideoViewHolder extends ViewHolder {
        NetworkImageView netImage;
        TextView item_name,item_intro,item_date;

        public VideoViewHolder(View itemView) {
            super(itemView);
            netImage = (NetworkImageView) itemView.findViewById(R.id.iv_cover_image);
            item_name = (TextView) itemView.findViewById(R.id.item_name);
            item_intro = (TextView) itemView.findViewById(R.id.item_intro);
            item_date = (TextView) itemView.findViewById(R.id.item_date);

        }

    }
}
