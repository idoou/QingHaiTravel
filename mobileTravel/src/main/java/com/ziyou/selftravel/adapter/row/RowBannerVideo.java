/**
 *
 */

package com.ziyou.selftravel.adapter.row;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.BannerPagerAdapter.IActionCallback;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.Video;
import com.ziyou.selftravel.support.OnClickListener;

/**
 * @author kuloud
 */
public class RowBannerVideo {
    public static View getView(Context context, final Video bannerSilde, View convertView,
            ViewGroup container,
            final IActionCallback<Video> callback) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_video_banner,
                    container, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (bannerSilde == null) {
            holder.imageView.setImageResource(R.drawable.bg_banner_hint);
        } else {
            if (bannerSilde.thumbnail != null) {
                holder.imageView.setDefaultImageResId(R.drawable.bg_banner_hint)
                        .setErrorImageResId(R.drawable.bg_banner_hint);
                holder.imageView.setImageUrl(bannerSilde.thumbnail, RequestManager.getInstance()
                        .getImageLoader());
                holder.imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onValidClick(View v) {
                        if (callback != null) {
                            callback.doAction(bannerSilde);
                        }
                    }
                });
            }
            holder.titleText.setText(bannerSilde.title);
        }
        return convertView;
    }

    private static class ViewHolder {
        NetworkImageView imageView;
        TextView titleText;

        public ViewHolder(View itemView) {
            imageView = (NetworkImageView) itemView.findViewById(R.id.banner_image);
            titleText = (TextView) itemView.findViewById(R.id.banner_title);
        }
    }
}
