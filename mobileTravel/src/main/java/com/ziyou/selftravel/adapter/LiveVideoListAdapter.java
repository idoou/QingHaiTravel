
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.kj.guradc.VideoActivity;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.MediaPlayerActivity;
import com.ziyou.selftravel.activity.NightLiveActivity;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.LiveVideo;
import com.ziyou.selftravel.model.Video;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.CommonUtils;
import com.ziyou.selftravel.util.FormatUtils;
import com.ziyou.selftravel.util.Log;

import java.util.List;
import com.ziyou.selftravel.model.QHScenic.QHVideo;

public class LiveVideoListAdapter extends AppendableAdapter<QHVideo> {
    private Activity mContext;

    public LiveVideoListAdapter(Activity context) {
        this.mContext = context;
    }

    public LiveVideoListAdapter(Activity context, List<QHVideo> items) {
        this(context);
        this.mDataItems = items;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.item_live_video_view, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        final QHVideo item = mDataItems.get(position);
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onValidClick(View view) {
                MobclickAgent.onEvent(mContext, MobConst.ID_INDEX_LIVE_ITEM);
                final QHVideo item = (QHVideo) view.getTag();
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setPositiveButton("白天",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(mContext,VideoActivity.class);
                        intent.putExtra("url",item.video_day);
                        mContext.startActivity(intent);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("夜间",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(mContext,NightLiveActivity.class);
                        intent.putExtra("url",item.video_night);
                        mContext.startActivity(intent);
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        if (item.image_url != null) {
            Log.d("position"+position+", thumbnail: " + item.image_url);
            viewHolder.thumbnail.setDefaultImageResId(R.drawable.bg_image_hint)
                    .setErrorImageResId(R.drawable.bg_image_hint);
            viewHolder.thumbnail.setImageUrl(item.image_url, RequestManager.getInstance()
                    .getImageLoader());
        }

    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView thumbnail;
        TextView playTimes;
        TextView duration;
        TextView videoName;

        public VideoViewHolder(View itemView) {
            super(itemView);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.video_thumbnail);
            playTimes = (TextView) itemView.findViewById(R.id.play_times);
            duration = (TextView) itemView.findViewById(R.id.duration);
            videoName = (TextView) itemView.findViewById(R.id.video_name);
        }
    }
}
