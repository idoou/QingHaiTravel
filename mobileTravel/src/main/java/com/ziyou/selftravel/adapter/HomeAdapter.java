
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kuloud.android.widget.recyclerview.BaseHeaderAdapter;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.HomeBanner;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.QHHomeBanner;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.model.Scenic;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.AutoScrollViewPager;
import com.ziyou.selftravel.widget.ScrollPoints;

import java.util.List;

import static android.support.v4.view.ViewPager.OnPageChangeListener;

//QHHomeBanner.QHHomeBannerLists
//HomeBanner.HomeBannerLists
public class HomeAdapter extends BaseHeaderAdapter<QHHomeBanner.QHHomeBannerLists, QHScenic> {
    private final String TAG = "HomeAdapter";
    private Activity mActivity;
    private static final int BANNER_SCROLL_INTERVAL = 2500;
    private BannerPagerAdapter.IActionCallback<QHHomeBanner> mActionCallback;
    Location mLocation;

    public HomeAdapter(Activity activity) {
        mActivity = activity;
    }

    public HomeAdapter(Activity activity, List<QHScenic> items) {
        this(activity);
        setDataItems(items);
    }

    public HomeAdapter(Activity activity, BannerPagerAdapter.IActionCallback<QHHomeBanner> actionCallback) {
        this(activity);
        mActionCallback = actionCallback;
    }

    class HomeItemViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView netImage;
        TextView item_name, item_intro, item_distance;
        ImageView is_audio;

        public HomeItemViewHolder(View itemView) {
            super(itemView);
            netImage = (NetworkImageView) itemView.findViewById(R.id.iv_cover_image);
            is_audio = (ImageView) itemView.findViewById(R.id.home_item_isaudio);
            item_name = (TextView) itemView.findViewById(R.id.home_item_secnic_name);
            item_intro = (TextView) itemView.findViewById(R.id.home_item_intro);
            item_distance = (TextView) itemView.findViewById(R.id.home_item_secnic_distance);
        }

        public void setDataTag(QHScenic scenic) {
            itemView.setTag(scenic);
            netImage.setTag(scenic);
        }
    }

    private class HomeHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView search_et;
        public AutoScrollViewPager banner;
        public ScrollPoints points;
        private TextView tab_food, tab_hotel, tab_guide, tab_special;
        //results
        private int all_points;

        public HomeHeaderViewHolder(View itemView, BannerPagerAdapter.IActionCallback<QHHomeBanner> actionCallback) {
            super(itemView);
            banner = (AutoScrollViewPager) itemView.findViewById(R.id.banner_pager);
            banner.setAdapter(new BannerPagerAdapter<QHHomeBanner>(itemView.getContext(), BannerPagerAdapter.Scene.HOME).setInfiniteLoop(true).setActionCallback(actionCallback));
            banner.setInterval(BANNER_SCROLL_INTERVAL);
            banner.startAutoScroll();
            points = (ScrollPoints) itemView.findViewById(R.id.points);
            search_et = (TextView) itemView.findViewById(R.id.home_search_et);
            tab_food = (TextView) itemView.findViewById(R.id.home_tab_food);
            tab_hotel = (TextView) itemView.findViewById(R.id.home_tab_hotel);
            tab_guide = (TextView) itemView.findViewById(R.id.home_tab_guide);
            tab_special = (TextView) itemView.findViewById(R.id.home_tab_special);
            banner.setOnPageChangeListener(listener);

            all_points = mHeader.results.size();
            points.initPoints(mActivity, all_points, 0);
        }

        OnPageChangeListener listener = new OnPageChangeListener() {
            @Override
            public void onPageSelected(int index) {
                points.changeSelectedPoint(all_points == 0 ? 0 : index %all_points);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        };
    }

    @Override
    public android.support.v7.widget.RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_fragment_header, parent, false);
        return new HomeHeaderViewHolder(v, mActionCallback);
    }

    @Override
    public void onBinderHeaderViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        HomeHeaderViewHolder headerHolder = (HomeHeaderViewHolder) holder;
        //BannerPagerAdapter<HomeBanner> bannerAdapter = (BannerPagerAdapter<HomeBanner>) headerHolder.banner.getAdapter();
        BannerPagerAdapter<QHHomeBanner> bannerAdapter = (BannerPagerAdapter<QHHomeBanner>) headerHolder.banner.getAdapter();
        if (bannerAdapter != null) {
            //list
            bannerAdapter.setBannerSlide(mHeader.results);
            headerHolder.banner.onDateSetChanged();
        }
        attachClickListener(headerHolder, headerHolder.search_et, 0);
        attachClickListener(headerHolder, headerHolder.tab_food, 0);
        attachClickListener(headerHolder, headerHolder.tab_hotel, 0);
        attachClickListener(headerHolder, headerHolder.tab_guide, 0);
        attachClickListener(headerHolder, headerHolder.tab_special, 0);
    }

    @Override
    public android.support.v7.widget.RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext().getApplicationContext();
        View v = LayoutInflater.from(context).inflate(R.layout.home_fragment_item, parent, false);
        HomeItemViewHolder holder = new HomeItemViewHolder(v);
        return holder;
    }

    @Override
    public void onBinderItemViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder, int position) {
        if (mDataItems.size() <= position) {
            Log.e(TAG, "[onBinderItemViewHolder] position out of bound");
            return;
        }
        final QHScenic scenic = mDataItems.get(position);

        if (scenic == null) {
            Log.e(TAG, "[onBinderItemViewHolder] comment: " + scenic);
            return;
        }


        final HomeItemViewHolder commentHolder = (HomeItemViewHolder) holder;
        commentHolder.setDataTag(scenic);


        if (!TextUtils.isEmpty(scenic.image_url)) {
            RequestManager.getInstance().getImageLoader().get(scenic.image_url,
                    ImageLoader.getImageListener(commentHolder.netImage,
                            R.drawable.bg_banner_hint, R.drawable.bg_banner_hint));
        }

//        if (scenic.have_video == 1)
//            commentHolder.is_audio.setVisibility(View.VISIBLE);
//        else commentHolder.is_audio.setVisibility(View.GONE);

        commentHolder.item_name.setText(scenic.name);
        commentHolder.item_intro.setText(scenic.intro_text.trim());
        LatLng start = new LatLng(scenic.latitude, scenic.longitude);
        LatLng end = new LatLng(ConstTools.myCurrentLoacation.latitude, ConstTools.myCurrentLoacation.longitude);

        commentHolder.item_distance.setText(ConstTools.getDistance(start,end));
        attachClickListener(commentHolder, commentHolder.netImage, position);
    }


    public void setMyLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

}
