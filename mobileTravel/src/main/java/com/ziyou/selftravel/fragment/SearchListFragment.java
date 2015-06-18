package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.LoginActivity;
import com.ziyou.selftravel.activity.TripAddActivity;
import com.ziyou.selftravel.adapter.ScenicAdapter;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.GsonRequest;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.QHHomeBanner;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.model.QHUser;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;
import com.ziyou.selftravel.widget.circularprogressbar.CircularProgressBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Edward on 2015/5/16.
 */
public class SearchListFragment extends BaseFragment implements /*DataLoader.DataLoaderCallback<QHScenic.QHScenicList>, */View.OnClickListener, DataLoader.DataLoaderCallback<QHScenic.QHScenicList> {

    private UrlListLoader<QHScenic.QHScenicList> mScenicListLoader;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private ScenicAdapter mAdapter;

    @InjectView(R.id.loading_progress)
    CircularProgressBar mLoadingProgress;
    @InjectView(R.id.search_content)
    EditText search_content;
    @InjectView(R.id.pulltorefresh_twowayview)
    PullToRefreshRecyclerView mPullToRefreshView;
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = View.inflate(getActivity(), R.layout.activity_search_scenic, null);
        ButterKnife.inject(this, mRootView);
        initPullToRefresh();
        initListView();
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    public void onClick(View v) {

    }

    private void initPullToRefresh() {

        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_twowayview);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {

            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
                    reload();
                } else {
                    loadNearScenics();
                }
            }
        });
    }

    private void reload() {
        mLoadingProgress.setVisibility(View.VISIBLE);
        loadNearScenics();
    }

    private void loadNearScenics() {

        String url = ServerAPI.QHSearchList.URL;
        JsonObject jsonObject = new JsonObject();
        RequestQueue requestQueue = RequestManager.getInstance().getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(url,null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("respond","respond");
            }
        },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("respond","error");
            }
        });
        requestQueue.add(request);

//        if (mScenicListLoader == null) {
//            mScenicListLoader = new UrlListLoader<QHScenic.QHScenicList>(mRequestTag, QHScenic.QHScenicList.class);
//            String url = ServerAPI.QHScenicList.URL;
//            mScenicListLoader.setUrl(url);
//        }
//        //加载更多
//        mScenicListLoader.loadMoreQHData(this, DataLoader.MODE_REFRESH);
    }

    @Override
    public void onLoadFinished(QHScenic.QHScenicList list, int mode) {
        mPullToRefreshView.onRefreshComplete();
        onListLoaded(list);
    }

    @Override
    public void onLoadError(int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "加载更多失败，请检查网络", Toast.LENGTH_SHORT).show();
    }



    private void initListView() {
        mRecyclerView = mPullToRefreshView.getRefreshableView();
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ScenicAdapter(getActivity());
        mAdapter.setHeaderEnable(false);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void onListLoaded(QHScenic.QHScenicList data) {
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (data == null || data.results == null|| mScenicListLoader != null) {
            // Disable PULL_FROM_END
            mPullToRefreshView.setMode(Mode.PULL_FROM_END);
            Toast.makeText(getActivity(),"没有更多数据",Toast.LENGTH_SHORT).show();
            return;
        }

        if (data != null) {
            if (data.pagination != null && data.pagination.offset == 0) {
                mAdapter.setDataItems(null);
            }
            mAdapter.appendDataItems(data.results);
            mAdapter.notifyDataSetChanged();
        }
    }


    protected void initData() {
        reload();
    }

//    private GsonRequest<QHHomeBanner.QHHomeBannerLists> mBannerRequest;
//    private UrlListLoader<QHScenic.QHScenicList> mScenicListLoader;
//    protected View mRootView;
//    protected Activity context = getActivity();
//
//    @InjectView(R.id.action_bar)
//    ActionBar action_bar;
//    @InjectView(R.id.loading_progress)
//    CircularProgressBar mLoadingProgress;
//    @InjectView(R.id.fragment_search_et)
//    EditText fragment_search_et;
//
//    private PullToRefreshRecyclerView mPullToRefreshView;
//
//    private RecyclerView mRecyclerView;
//    private LinearLayoutManager layoutManager;
//    private ScenicAdapter mAdapter;
//
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.activity_search_scenic, container,false);
////        mRootView = View.inflate(getActivity(), R.layout.activity_search_scenic, null);
//        ButterKnife.inject(this, mRootView);
//        initSearchEdit();
//        initPullToRefresh();
//        initListView();
//        return mRootView;
//    }
//
//    private void initSearchEdit() {
//        fragment_search_et.setVisibility(View.VISIBLE);
//        fragment_search_et.setOnKeyListener(new View.OnKeyListener() {//输入完后按键盘上的搜索键【回车键改为了搜索键】
//
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if(keyCode== KeyEvent.KEYCODE_ENTER){//修改回车键功能
//                    // 先隐藏键盘
//                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
//                            .hideSoftInputFromWindow(
//                                    getActivity().getCurrentFocus()
//                                            .getWindowToken(),
//                                    InputMethodManager.HIDE_NOT_ALWAYS);
//                    UploadRecyclerView();
//
//                }
//                return false;
//            }
//        });
//    }
//
//    private void UploadRecyclerView() {
//        String condition = fragment_search_et.getText().toString();
//        String url = ServerAPI.QHScenicList.buildSearchCommentUrl(condition);
//        mScenicListLoader.setUrl(url);
//        reload();
//    }
//
//    protected void initActionBar() {
//        action_bar.getTitleView().setText("搜索");
//        action_bar.getLeftView().setImageResource(R.drawable.return_back);
//        action_bar.getLeftView().setOnClickListener(this);
//        action_bar.getRightView().setImageResource(R.drawable.edit);
//        action_bar.getRightView().setOnClickListener(this);
//    }
//
//    private void initPullToRefresh() {
//
//        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_twowayview);
//        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {
//
//            @Override
//            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
//                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
//                    reload();
//                } else {
//                    //下拉加载
//                    loadNearScenics();
//                }
//            }
//        });
//
////        mPullToRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
////        mPullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
////
////            @Override
////            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
//////                }
////                if (refreshView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_START){
////                    return;
////                }else if(refreshView.getCurrentMode() == PullToRefreshBase.Mode.PULL_FROM_END&&mScenicListLoader.hasMorePages()){
////                    mScenicListLoader.loadMoreData(GuideFragment.this, DataLoader.MODE_LOAD_MORE);
////                }else{
////                    Toast.makeText(context, AppUtils.getResString(R.string.load_empty), Toast.LENGTH_SHORT).show();
////                    mPullToRefreshView.onRefreshComplete();
////                }
////
////            }
////        });
//    }
//
//    private void initListView() {
//        mRecyclerView = mPullToRefreshView.getRefreshableView();
//        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
//        mRecyclerView.setLayoutManager(layoutManager);
//        mAdapter = new ScenicAdapter(getActivity());
//        mAdapter.setHeaderEnable(false);
//        mRecyclerView.setAdapter(mAdapter);
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        initData();
//    }
//
//    protected void initData() {
//        reload();
//    }
//
//    private void reload() {
//        mLoadingProgress.setVisibility(View.VISIBLE);
//        loadNearScenics();
//    }
//
//    private void loadNearScenics() {
//
//        if (mScenicListLoader == null) {
//            mScenicListLoader = new UrlListLoader<QHScenic.QHScenicList>(mRequestTag, QHScenic.QHScenicList.class);
//            String url = ServerAPI.QHScenicList.URL;
//            mScenicListLoader.setUrl(url);
//        }
//        //加载更多
//        mScenicListLoader.loadMoreData(this, DataLoader.MODE_REFRESH);
//    }
//
//    @Override
//    public void onLoadFinished(QHScenic.QHScenicList list, int mode) {
//        mPullToRefreshView.onRefreshComplete();
//        onListLoaded(list);
//    }
//
//    @Override
//    public void onLoadError(int mode) {
//        mPullToRefreshView.onRefreshComplete();
//        mLoadingProgress.setVisibility(View.GONE);
//        mRecyclerView.setVisibility(View.VISIBLE);
//        Toast.makeText(context, "加载更多失败，请检查网络", Toast.LENGTH_SHORT).show();
//    }
//
//    private void onListLoaded(QHScenic.QHScenicList data) {
//        mLoadingProgress.setVisibility(View.GONE);
//        mRecyclerView.setVisibility(View.VISIBLE);
//        if (data == null || data.results == null|| mScenicListLoader != null) {
//            // Disable PULL_FROM_END
//            mPullToRefreshView.setMode(Mode.PULL_FROM_END);
//            Toast.makeText(getActivity(),"没有更多数据",Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (data != null) {
//            if (data.pagination != null && data.pagination.offset == 0) {
//                mAdapter.setDataItems(null);
//            }
//            mAdapter.appendDataItems(data.results);
//            mAdapter.notifyDataSetChanged();
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (ExcessiveClickBlocker.isExcessiveClick()) {
//            return;
//        }
//        switch (v.getId()){
//            case R.id.action_bar_left:
//                getActivity().finish();
//                break;
//            case R.id.action_bar_right:
//                QHUser user = AppUtils.getQHUser(getActivity());
//                if (user == null) {
//                    Intent login = new Intent(getActivity(), LoginActivity.class);
//                    startActivity(login);
//                } else {
//                    Intent intent = new Intent(context, TripAddActivity.class);
//                    startActivity(intent);
//                }
//
//                break;
//        }
//    }
}
