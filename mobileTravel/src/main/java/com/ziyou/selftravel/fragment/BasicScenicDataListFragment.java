package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.ziyou.selftravel.adapter.BasicScenicDataListAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.OfflinePackageManager;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.BasicScenicData;
import com.ziyou.selftravel.model.ResultList;
import com.ziyou.selftravel.model.Scenic;
import com.ziyou.selftravel.model.ScenicSpot;
import com.ziyou.selftravel.model.ScenicSpot.ScenicSpotList;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicScenicDataListFragment extends BaseFragment implements
        DataLoaderCallback<ResultList<BasicScenicData>> {
    protected PullToRefreshRecyclerView mPullToRefreshView;
    protected RecyclerView mSpotsRecyclerView = null;
    protected View mEmptyHintView;
    protected Context mContext;
    protected ArrayList<BasicScenicData> mScenicDataList = new ArrayList<BasicScenicData>();
    protected DataLoader<ResultList<BasicScenicData>> mCb;

    protected View mLoadingProgress;

    public BasicScenicDataListAdapter mAdapter;
    public UrlListLoader<ResultList<BasicScenicData>> mLoader;

    public abstract BasicScenicDataListAdapter initAdapter();

    public BasicScenicDataListFragment() {
        mAdapter = initAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mSpotsRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);

        Class clazz;
        String modelClass = getArguments().getString(Const.ARGS_KEY_MODEL_CLASS);
        if (ScenicSpotList.class.getName().equals(modelClass)) {
            clazz = ScenicSpotList.class;
        } else {
            clazz = Scenic.ScenicList.class;
        }
        mLoader = new UrlListLoader<ResultList<BasicScenicData>>(
                mRequestTag, clazz);
        mLoader.setUrl(getArguments().getString(Const.ARGS_LOADER_URL));

        int scenicId = getArguments().getInt(Const.ARGS_SCENIC_ID);
        ScenicSpotList offlineList = OfflinePackageManager.getInstance().getOfflineData(
                scenicId,
                ScenicSpotList.class);
        if (offlineList != null) {
            // TODO: ugly!!
            Log.d("Loading offline spot list data for scenic %d", scenicId);
            ResultList tempList = new ResultList<ScenicSpot>();
            tempList.list = offlineList.list;
            tempList.pagination = offlineList.pagination;
            onLoadFinished(tempList, DataLoader.MODE_LOAD_MORE);
        } else {
            mLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity.getApplicationContext();
        super.onAttach(activity);
    }

    public ArrayList<BasicScenicData> getScenicDataList() {
        return mScenicDataList;
    }

    public void setOnItemClickListener(OnClickListener l) {
        mAdapter.setOnItemClickListener(l);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onLoadFinished(ResultList<BasicScenicData> resultList, int mode) {
        mLoadingProgress.setVisibility(View.GONE);

        List<BasicScenicData> list = null;
        if (resultList != null) {
            list = resultList.list;
        }
        if (list == null || list.isEmpty()) {
            if (mScenicDataList.isEmpty()) {
                mEmptyHintView.setVisibility(View.VISIBLE);
            }
        } else {
            mSpotsRecyclerView.setVisibility(View.VISIBLE);
            mScenicDataList.addAll(list);
            if (!mLoader.getPaginator().hasMorePages()) {
                mPullToRefreshView.setMode(PullToRefreshBase.Mode.DISABLED);
            }
            ((BasicScenicDataListAdapter) mSpotsRecyclerView.getAdapter()).addSpotList(list);
        }
    }

    @Override
    public void onLoadError(int mode) {
        // TODO handle this error
        mLoadingProgress.setVisibility(View.GONE);
    }

}
