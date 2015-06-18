
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.RestaurantListAdapter;
import com.ziyou.selftravel.adapter.SpecialListAdapter;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.HomeRestuarant;
import com.ziyou.selftravel.model.HomeSpecial;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.Pagination;
import com.ziyou.selftravel.model.Paginator;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SpecialListFragment extends BaseFragment implements
        DataLoaderCallback<QHStrategy.QHStrategyList>, OnClickListener {
    private PullToRefreshRecyclerView mPullToRefreshView;
    private Context mContext;
    private ArrayList<QHStrategy> mVideoList = new ArrayList<QHStrategy>();
    private UrlListLoader<QHStrategy.QHStrategyList> mLoader;
    private AppendableAdapter<QHStrategy> mAdapter;
    private View mLoadingProgress;
    private View mEmptyHintView;
    private View mRootView;
    private View mReloadView;
    private LayoutManager mLayoutManager;
    private static final String ARGS_KEY_LAYOUT_MANAGER = "args_key_layout_manager";
    private static final String ARGS_KEY_ADAPTER = "args_key_adapter";
    private Location myLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mAdapter = new SpecialListAdapter(getActivity());

        super.onCreate(savedInstanceState);
    }
    private void loadList(){
        /*Parcel parcel =Parcel.obtain() ;
        Pagination pag =  new Pagination(parcel);
        pag.limit = 2;
        pag.offset = 2;
        pag.total = 10;
        Paginator page =  new Paginator();
        page.addPage(pag);*/
        //加载攻略信息
        mLoader = new UrlListLoader<QHStrategy.QHStrategyList>(  mRequestTag,QHStrategy.QHStrategyList.class/*, page*/ );
        mLoader.setUseCache(true);
        String url;
//        if(null!=city)
//        url = ServerAPI.Home.buildSpecialUrl(city);
//        else
//        url = ServerAPI.Home.buildSpecialUrl("北京");
        url = "http://182.92.218.24:9000/api/guides/";

        mLoader.setUrl(url);
    }
    public void loadList(String url){
        //加载攻略信息
        mLoader = new UrlListLoader<QHStrategy.QHStrategyList>(  mRequestTag,QHStrategy.QHStrategyList.class/*, page*/ );
        mLoader.setUseCache(true);

        mLoader.setUrl(url);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        reload();
        super.onActivityCreated(savedInstanceState);
    }

    public void reload() {
        mPullToRefreshView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
        // TODO
        mLoader.loadMoreQHData(this, DataLoader.MODE_LOAD_MORE);
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity.getApplicationContext();
        super.onAttach(activity);
    }
    String city;
    String city_en;
    public void initLoacation(String city, String city_en){
        this.city = city;
        this.city_en = city_en;
        loadList();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.layout_recycler_list, container, false);
//        initActionBar(mRootView);
        mEmptyHintView = mRootView.findViewById(R.id.empty_hint_view);
        if (mLoadingProgress != null) {
            mRootView.findViewById(R.id.loading_progress).setVisibility(View.GONE);
        } else {
            mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        }
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);

        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView
                .findViewById(R.id.pulltorefresh_twowayview);
        mPullToRefreshView.setMode(Mode.BOTH);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {

            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                int mode = refreshView.getCurrentMode() ==
                        Mode.PULL_FROM_START ? DataLoader.MODE_REFRESH
                                : DataLoader.MODE_LOAD_MORE;
                mLoader.loadMoreData(SpecialListFragment.this, mode);
            }
        });

        RecyclerView recyclerView = mPullToRefreshView.getRefreshableView();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int scap = ScreenUtils.dpToPxInt(getActivity(), 4);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(recyclerView);
        recyclerView.addItemDecoration(decor);
        recyclerView.setAdapter(mAdapter);
        return mRootView;
    }

    @Override
    public void onLoadFinished(QHStrategy.QHStrategyList videoList, int mode) {
        mLoadingProgress.setVisibility(View.GONE);
        mPullToRefreshView.onRefreshComplete();

        List<QHStrategy> list = null;
        if (videoList != null) {
            list = videoList.results;
        }

        if (list == null || list.isEmpty()) {
            if (mVideoList.isEmpty()) {
                mEmptyHintView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mode == DataLoader.MODE_REFRESH ||null==videoList.pagination|| videoList.pagination.offset == 0) {
                mVideoList.clear();
                ((AppendableAdapter) mPullToRefreshView.getRefreshableView().getAdapter())
                        .setDataItems(mVideoList);
            }

            mPullToRefreshView.setVisibility(View.VISIBLE);
            for (QHStrategy liveVideo : list) {
//                QHStrategy rest = new QHStrategy();
//                rest.name = liveVideo.name;
//                rest.description = liveVideo.description;
//                rest.city = liveVideo.city;
//                rest.imageUrl = liveVideo.imageUrl;
//                rest.url = liveVideo.url;

//                mVideoList.add(rest);
            }
            ((AppendableAdapter) mPullToRefreshView.getRefreshableView().getAdapter()).appendDataItems(/*mVideoList*/videoList.results);
        }
    }

    @Override
    public void onLoadError(int mode) {
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
        Toast.makeText(mContext, R.string.loading_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
       else if (v.getId() == R.id.reload_view) {
            reload();
        }
        else if (v.getId() ==  R.id.action_bar_left){
            getActivity().finish();
        }
    }

    public void attachLoadingProgress(View loadingProgress) {
        mLoadingProgress = loadingProgress;
    }
}
