
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.LiveVideo;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.model.Video;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.CommonUtils;
import com.ziyou.selftravel.util.FormatUtils;
import com.ziyou.selftravel.model.QHScenic.QHVideo;

import java.util.List;

public class VideoListAdapter extends AppendableAdapter<QHVideo> {
    private Activity mContext;

    public VideoListAdapter(Activity context) {
        this.mContext = context;
    }

    public VideoListAdapter(Activity context, List<QHVideo> items) {
        this(context);
        this.mDataItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_view, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        final QHVideo item = mDataItems.get(position);
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                QHVideo data = (QHVideo) v.getTag();
                if (data != null) {
                    CommonUtils.playVideo(mContext, data);
                }

            }
        });

        if (item.image_url != null) {
            RequestManager.getInstance().getImageLoader().get(item.image_url,
                    ImageLoader.getImageListener(viewHolder.thumbnail, R.drawable.bg_image_hint,
                            R.drawable.bg_image_hint));
        }

    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        public VideoViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.video_thumbnail);
        }
    }
}
