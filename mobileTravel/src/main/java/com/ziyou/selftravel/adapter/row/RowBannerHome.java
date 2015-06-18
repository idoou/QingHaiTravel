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
import com.ziyou.selftravel.model.HomeBanner;
import com.ziyou.selftravel.model.QHHomeBanner;
import com.ziyou.selftravel.model.Trip;
import com.ziyou.selftravel.support.OnClickListener;

/**
 * @author kuloud
 */
public class RowBannerHome {
    public static View getView(Context context, final QHHomeBanner banner, View convertView,
            ViewGroup container,
            final IActionCallback<QHHomeBanner> callback) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_banner_home,
                    container, false);
            holder = new ViewHolder();
            holder.image = (NetworkImageView) convertView.findViewById(R.id.discovery_banner_image);
            holder.title = (TextView) convertView.findViewById(R.id.discovery_banner_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (banner == null) {
            holder.image.setImageResource(R.drawable.bg_banner_hint);
        } else {
            holder.image.setDefaultImageResId(R.drawable.bg_banner_hint).setErrorImageResId(R.drawable.bg_banner_hint);
            //holder.image.setImageUrl(banner.imageUrl, RequestManager.getInstance().getImageLoader());
            //holder.title.setText(banner.title);
            holder.image.setImageUrl(banner.image_url, RequestManager.getInstance().getImageLoader());
            holder.image.setOnClickListener(new OnClickListener() {
                @Override
                public void onValidClick(View v) {
                    if (callback != null) {
                        callback.doAction(banner);
                    }
                }
            });
        }
        return convertView;
    }

    private static class ViewHolder {
        NetworkImageView image;
        TextView title;
    }
}
