
package com.ziyou.selftravel.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuloud.android.widget.recyclerview.BaseHeaderAdapter;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.BannerPagerAdapter.IActionCallback;
import com.ziyou.selftravel.adapter.BannerPagerAdapter.Scene;
import com.ziyou.selftravel.adapter.TripListAdapter.TripAdapterHelper;
import com.ziyou.selftravel.model.Trip;
import com.ziyou.selftravel.model.Trip.TripList;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.AutoScrollViewPager;

import java.util.List;

/**
 * @author kuloud
 */
public class DiscoveryAdapter extends BaseHeaderAdapter<TripList, Trip> {
    private static final int BANNER_SCROLL_INTERVAL = 2500;
    private IActionCallback<Trip> mActionCallback;
    private boolean mNoMorePage;

    public DiscoveryAdapter() {
    }

    public DiscoveryAdapter(List<Trip> items) {
        setDataItems(items);
    }

    public DiscoveryAdapter(IActionCallback<Trip> actionCallback) {
        mActionCallback = actionCallback;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        AutoScrollViewPager banner;

        public HeaderViewHolder(View itemView, IActionCallback<Trip> actionCallback) {
            super(itemView);
            banner = (AutoScrollViewPager) itemView.findViewById(R.id.banner_pager);
            banner.setAdapter(new BannerPagerAdapter<Trip>(itemView.getContext(), Scene.DISCOVERY)
                    .setInfiniteLoop(true)
                    .setActionCallback(actionCallback));
            banner.setInterval(BANNER_SCROLL_INTERVAL);
            banner.startAutoScroll();
        }
    }

    @Override
    public android.support.v7.widget.RecyclerView.ViewHolder onCreateHeaderViewHolder(
            ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discovery_header, parent, false);
        return new HeaderViewHolder(v, mActionCallback);
    }

    @Override
    public void onBinderHeaderViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        if (mHeader != null) {
            Log.d("Kuloud", "onBinderHeaderViewHolder"+System.currentTimeMillis());
            HeaderViewHolder vh = (HeaderViewHolder) holder;
            BannerPagerAdapter<Trip> bannerAdapter = (BannerPagerAdapter<Trip>) vh.banner
                    .getAdapter();
            if (bannerAdapter != null) {
                bannerAdapter.setBannerSlide(mHeader.list);
                vh.banner.onDateSetChanged();
            }
        }
    }

    @Override
    public android.support.v7.widget.RecyclerView.ViewHolder onCreateItemViewHolder(
            ViewGroup parent, int viewType) {
        return TripAdapterHelper.onCreateItemViewHolder(parent, viewType);
    }

    @Override
    public void onBinderItemViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder,
            int position) {
        TripAdapterHelper.onBinderItemViewHolder(holder, position, mDataItems);
        if (mNoMorePage && (mDataItems != null) && (position == mDataItems.size() - 1)) {
            ToastUtils.show(holder.itemView.getContext(), R.string.msg_list_reach_end);
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataItems != null && mDataItems.size() > position) {
            Trip trip = mDataItems.get(position);
            if (trip != null) {
                return trip.id;
            }
        }
        return super.getItemId(position);
    }

    public void setNoMorePage(boolean noMorePage) {
        this.mNoMorePage = noMorePage;
    }

}
