/**
 * 
 */

package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.TripDayEditActivity;
import com.ziyou.selftravel.activity.TripDetailEditActivity;
import com.ziyou.selftravel.adapter.BannerPagerAdapter.IActionCallback;
import com.ziyou.selftravel.adapter.DiscoveryAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.UserManager;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.Trip;
import com.ziyou.selftravel.model.Trip.TripList;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.LocationUtils;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

/**
 * @author kuloud
 */
public class DiscoveryFragment extends BaseFragment implements DataLoaderCallback<TripList> {
    private final String TAG = "DiscoveryFragment";

    private final int REQUEST_CODE_ADD_TRIP_DAY = 1;
    private final int REQUEST_CODE_ADD_TRIP_DAY_LOGIN = 2;
    private Context mContext;
    private DiscoveryAdapter mAdapter;
    private Location mLastKnownLocation;
    private PullToRefreshRecyclerView mPullRefreshScrollView;
    private UrlListLoader<TripList> mTripLoader;

    /**
     * 
     */
    public DiscoveryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);
        initActionBar(view);

        mPullRefreshScrollView = (PullToRefreshRecyclerView) view
                .findViewById(R.id.pull_refresh_scrollview);
        mPullRefreshScrollView.setMode(Mode.BOTH);
        // TODO
        mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {

            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
                    mTripLoader = null;
                    mAdapter.setDataItems(null);
                    mPullRefreshScrollView.setMode(Mode.BOTH);
                    loadTrips();
                } else {
                    loadTrips();
                }

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()
                .getApplicationContext());
        mPullRefreshScrollView.getRefreshableView().setLayoutManager(layoutManager);
        mPullRefreshScrollView.getRefreshableView().setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mPullRefreshScrollView
                .getRefreshableView());
        clickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                if (position == 0) {
                    // banner click should be handle with doAction
                    return;
                }
                // MobclickAgent.onEvent(mContext, MobConst.ID_INDEX_DISCOVERY_ITEM);
                Intent intent = new Intent(getActivity(), TripDetailEditActivity.class);
                intent.putExtra(Const.EXTRA_ID, (int) mAdapter.getItemId(position));
                startActivity(intent);
            }
        });
        int scap = ScreenUtils.dpToPxInt(mContext, 6);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(mPullRefreshScrollView.getRefreshableView());
        mPullRefreshScrollView.getRefreshableView().addItemDecoration(decor);
        mAdapter = new DiscoveryAdapter(new IActionCallback<Trip>() {

            @Override
            public void doAction(Trip data) {
                // MobclickAgent.onEvent(mContext, MobConst.ID_INDEX_DISCOVERY_BANNER);
                if (data == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), TripDetailEditActivity.class);
                intent.putExtra(Const.EXTRA_ID, data.id);
                startActivity(intent);
            }

        });
        mPullRefreshScrollView.getRefreshableView().setAdapter(mAdapter);

        // FIXME just for test purpose
        loadBanners(ServerAPI.Trips.buildTripBannerSlidesUrl("sanya"));
        loadTrips();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        mLastKnownLocation = LocationUtils.getLastKnownLocation(mContext);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_TRIP_DAY_LOGIN:
                    Intent addTripDay = new Intent(getActivity(), TripDayEditActivity.class);
                    startActivityForResult(addTripDay, REQUEST_CODE_ADD_TRIP_DAY);
                    break;
                case REQUEST_CODE_ADD_TRIP_DAY:
                    if (data == null) {
                        Log.wtf("REQUEST_CODE_ADD_TRIP_DAY data NULL");
                        return;
                    }
                    mTripLoader = null;
                    mAdapter.setDataItems(null);
                    mPullRefreshScrollView.setMode(Mode.BOTH);
                    loadTrips();
//                    Trip newTrip = data.getParcelableExtra(Const.EXTRA_TRIP_DATA);
//                    if (newTrip != null) {
//                        List<Trip> trips = new ArrayList<Trip>();
//                        trips.add(newTrip);
//                        mAdapter.instertDataItemsAhead(trips);
//                    }
//                    // Success add trip day, update item and auto jump to detail
//                    int id = data.getIntExtra(Const.EXTRA_ID, -1);
//                    int index = 0;
//                    for (Trip trip : mAdapter.getDataItems()) {
//                        if (trip.id == id) {
//                            trip = newTrip;
//                            mAdapter.notifyItemChanged(index);
//                            Intent goDetail = new Intent(getActivity(),
//                                    TripDetailEditActivity.class);
//                            goDetail.putExtras(data);
//                            startActivity(goDetail);
//                            break;
//                        }
//                        index++;
//                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_discovery);
        actionBar.getRightView().setImageResource(R.drawable.ic_action_bar_camera_selecter);
        actionBar.getRightView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                MobclickAgent.onEvent(mContext, MobConst.ID_INDEX_DISCOVERY_ADD);
                if (!UserManager.makeSureLogin(getActivity(), REQUEST_CODE_ADD_TRIP_DAY_LOGIN)) {
                    Intent addTripDay = new Intent(getActivity(), TripDayEditActivity.class);
                    startActivityForResult(addTripDay, REQUEST_CODE_ADD_TRIP_DAY);
                }
            }
        });
    }

    private void loadBanners(String url) {
        RequestManager.getInstance().sendGsonRequest(url, TripList.class,
                new Response.Listener<TripList>() {
                    @Override
                    public void onResponse(TripList tripList) {
                        Log.d("onResponse, TripList=%s", tripList);
                        mAdapter.setHeader(tripList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                    }
                }, this);
    }

    private void loadTrips() {
        if (mTripLoader == null) {
            mTripLoader = new UrlListLoader<TripList>(mRequestTag, TripList.class);
            mTripLoader.setUrl(ServerAPI.Trips.buildGetTripsUrl(mLastKnownLocation));
            mTripLoader.setUseCache(true);
        }

        mTripLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }

    @Override
    public void onLoadFinished(TripList data, int mode) {
        mPullRefreshScrollView.onRefreshComplete();
        if (data == null || data.list == null
                || (mTripLoader != null && !mTripLoader.getPaginator().hasMorePages())) {
            // Disable PULL_FROM_END
            mPullRefreshScrollView.setMode(Mode.PULL_FROM_START);
            if (data != null && data.list != null) {
                mAdapter.setNoMorePage(true);
            } else {
                mAdapter.setNoMorePage(false);
            }
        }

        if (data != null) {
            mAdapter.appendDataItems(data.list);
        }
    }

    @Override
    public void onLoadError(int mode) {
        mPullRefreshScrollView.onRefreshComplete();
    }
}
