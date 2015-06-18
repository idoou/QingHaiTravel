
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import com.ziyou.selftravel.adapter.LiveVideoListAdapter;
import com.ziyou.selftravel.adapter.VideoListAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.LiveVideo;
import com.ziyou.selftravel.model.LiveVideo.LiveVideoList;
import com.ziyou.selftravel.model.QHScenic.QHVideo;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.ziyou.selftravel.model.QHScenic.QHVideo.QHVideoList;

public class LiveVideoListFragment extends BaseFragment implements
        DataLoaderCallback<QHVideoList>, OnClickListener {
    private PullToRefreshRecyclerView mPullToRefreshView;
    private Context mContext;
    private ArrayList<QHVideo> mVideoList = new ArrayList<QHVideo>();
    private UrlListLoader<QHVideoList> mLoader;

    private AppendableAdapter<QHVideo> mAdapter;

    private View mLoadingProgress;
    private View mEmptyHintView;
    private View mRootView;
    private View mReloadView;

    private LayoutManager mLayoutManager;

    private static final String ARGS_KEY_LAYOUT_MANAGER = "args_key_layout_manager";
    private static final String ARGS_KEY_ADAPTER = "args_key_adapter";

    public static LiveVideoListFragment newInstance(
            Class<? extends LayoutManager> layoutMgrClass,
            Class<? extends AppendableAdapter<QHVideo>> adapterClass) {
        LiveVideoListFragment fragment = new LiveVideoListFragment();

        Bundle args = new Bundle();
        args.putString(ARGS_KEY_LAYOUT_MANAGER, layoutMgrClass.getName());
        args.putString(ARGS_KEY_ADAPTER, adapterClass.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String layoutMgrClass = getArguments().getString(ARGS_KEY_LAYOUT_MANAGER);
        if (GridLayoutManager.class.getName().equals(layoutMgrClass)) {
            mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        } else {
            mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        }

        String adapterClass = getArguments().getString(ARGS_KEY_ADAPTER);
        if (VideoListAdapter.class.getName().equals(adapterClass)) {
            mAdapter = new VideoListAdapter(getActivity());
        } else {
            mAdapter = new LiveVideoListAdapter(getActivity());
        }

        mLoader = new UrlListLoader<QHVideoList>(mRequestTag,QHVideoList.class);
        mLoader.setUseCache(true);
        mLoader.setUrl(getArguments().getString(Const.ARGS_LOADER_URL));

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        reload();
        super.onActivityCreated(savedInstanceState);
    }

    private void reload() {
        mPullToRefreshView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);

        int scenicId = getArguments().getInt(Const.ARGS_SCENIC_ID);
        // TODO
        mLoader.loadMoreQHData(this, DataLoader.MODE_LOAD_MORE);
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity.getApplicationContext();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.layout_recycler_list, container, false);
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
                mLoader.loadMoreQHData(LiveVideoListFragment.this, mode);
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
    public void onLoadFinished(QHVideoList videoList, int mode) {
        mLoadingProgress.setVisibility(View.GONE);
        mPullToRefreshView.onRefreshComplete();

        List<QHVideo> list = null;
        if (videoList != null) {
            list = videoList.results;
        }

        if (list == null || list.isEmpty()) {
            if (mVideoList.isEmpty()) {
                mEmptyHintView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mode == DataLoader.MODE_REFRESH/* || videoList.pagination.offset == 0*/) {
                mVideoList.clear();
                ((AppendableAdapter) mPullToRefreshView.getRefreshableView().getAdapter())
                        .setDataItems(mVideoList);
            }

            mPullToRefreshView.setVisibility(View.VISIBLE);
//            ArrayList<Video> tempList  = new ArrayList();
//            for (LiveVideo liveVideo : list) {
//                if (liveVideo.id <= 0) {
//                    continue;
//                }
//                Video video = new Video();
//                video.liveId = liveVideo.id;
//                tempList.add(video);
//            }
            mVideoList = (ArrayList<QHVideo>) list;
            ((AppendableAdapter) mPullToRefreshView.getRefreshableView().getAdapter())
                    .appendDataItems(list);
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
        if (v.getId() == R.id.reload_view) {
            reload();
        }
    }

    public void attachLoadingProgress(View loadingProgress) {
        mLoadingProgress = loadingProgress;
    }
}
