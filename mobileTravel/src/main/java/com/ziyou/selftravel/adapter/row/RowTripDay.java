/**
 * 
 */

package com.ziyou.selftravel.adapter.row;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.QHRoute;
import com.ziyou.selftravel.model.QHRouteDay;
import com.ziyou.selftravel.model.TripDay;
import com.ziyou.selftravel.widget.RatioImageView;

/**
 * @author kuloud
 */
public class RowTripDay {
    public static RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_day, parent,
                false);
        return new ViewHolder(v);
    }

    public static void onBindViewHolder(RecyclerView.ViewHolder holder, int position,
                                        QHRouteDay tripDay) {
        ViewHolder h = (ViewHolder) holder;
        if (tripDay == null) {
            return;
        }
        if (tripDay.scenic.image_url != null /*&& !TextUtils.isEmpty(tripDay.image.largeImage)*/) {
//            h.coverImage.setRatio(tripDay.image.getLargeRadio());
            h.coverImage.setDefaultImageResId(R.drawable.bg_image_hint)
                    .setErrorImageResId(R.drawable.bg_image_hint);
            h.coverImage.setImageUrl(tripDay.scenic.image_url, RequestManager.getInstance()
                    .getImageLoader());
        }

        if (!TextUtils.isEmpty(tripDay.scenic.intro_text)) {
            h.content.setVisibility(View.VISIBLE);
            h.content.setText(tripDay.scenic.intro_text);
        } else {
            h.content.setVisibility(View.GONE);
        }

        h.scenicName.setText(tripDay.scenic.intro_title);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RatioImageView coverImage;
        TextView scenicName;
        TextView content;

        public ViewHolder(View itemView) {
            super(itemView);
            coverImage = (RatioImageView) itemView.findViewById(R.id.cover_image);
            scenicName = (TextView) itemView.findViewById(R.id.scenic_name);
            content = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
