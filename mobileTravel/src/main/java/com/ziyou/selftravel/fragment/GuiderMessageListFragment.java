
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemSubViewClickListener;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.GuiderMessageDetailActivity;
import com.ziyou.selftravel.adapter.GuiderMessageListAdapter;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ShareManager;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.HomeBanner;
import com.ziyou.selftravel.model.Message;
import com.ziyou.selftravel.model.Scenic;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GuiderMessageListFragment extends BaseFragment implements OnClickListener,DataLoaderCallback<Message.List> {
    private final String TAG = getClass().getName();//"GuiderListFragment";
    private final int REQUEST_CODE_SEARCH = 4;
    private ActionBar mActionBar;
    private GuiderMessageListAdapter mAdapter;
    private View mLoadingProgress;
    private View mReloadView;
    private View mEmptyHintView;
    private PullToRefreshRecyclerView mPullToRefreshView;
    private UrlListLoader<Message.List> mScenicListLoader;
//    private Request<HomeBanner.HomeBannerLists> mBannerRequest;
    private ViewGroup mRootView;
    private Scenic mScenic;
    private int HTTP_GET_PARAM_LIMIT = 10;
    private RecyclerView linesListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShareManager.getInstance().onStart(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.guide_list_fragment, container,false);
        mEmptyHintView = mRootView.findViewById(R.id.empty_hint_view);
        initViews();
        reload();
        loadMessage("北京");
        return mRootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        // hookSensorListener(!isHidden());
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
        // hookSensorListener(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
    private void reload() {
        mPullToRefreshView.setMode(Mode.PULL_FROM_END);
        linesListView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
//        mAdapter.setHeader(null);
//        mAdapter.setDataItems(null);
        mScenicListLoader = null;
        RequestManager.getInstance().getRequestQueue().cancelAll(mRequestTag);
    }
    private void initViews() {
        initActionBar();
        initListView();
        initPullToRefresh();
        mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);
    }
    private void initPullToRefresh() {
        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_twowayview);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
                    reload();
                } else {
//                    loadMessage(mScenicDetailsModel.cityZh);
                }
            }
        });
    }

    private void initListView() {
        linesListView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linesListView.setLayoutManager(layoutManager);
        mAdapter = new GuiderMessageListAdapter(getActivity());
        ItemClickSupport clickSupport = ItemClickSupport.addTo(linesListView);
        clickSupport.setOnItemSubViewClickListener(new OnItemSubViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                    onItemClicked(view);
                }
        });
       /* int scap = ScreenUtils.dpToPxInt(getActivity(), 13);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(linesListView);
        linesListView.addItemDecoration(decor);*/
        linesListView.setItemAnimator(new DefaultItemAnimator());
        linesListView.setAdapter(mAdapter);
    }

    private void initActionBar() {
        mActionBar = (ActionBar) mRootView.findViewById(R.id.action_bar);
        mActionBar.setBackgroundResource(R.drawable.fg_top_shadow);
        mActionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        mActionBar.getLeftView().setOnClickListener(this);
        mActionBar.getRightTextView().setTextColor(getActivity().getResources().getColor(R.color.title_bg));
        /*mActionBar.setRightMode(true);
        mActionBar.getRightTextView().setText("删除");
        mActionBar.getRightTextView().setOnClickListener(this);*/
        mActionBar.getTitleView().setTextColor(getActivity().getResources().getColor(R.color.black));
        mActionBar.setTitle("我的消息");

    }


    private void showReloadView() {
        linesListView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
    }


    private void loadMessage(String cityName) {
        Log.e("-------","loadMessage--");
        mLoadingProgress.setVisibility(View.VISIBLE);
        linesListView.setVisibility(View.GONE);

        if (mScenicListLoader == null) {
            mScenicListLoader = new UrlListLoader<Message.List>( mRequestTag, Message.List.class);
            String  url;
            url = ServerAPI.User.buildMessageListUrl() ;
            mScenicListLoader.setUrl(url);
        }
        mScenicListLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }
    @Override
    public void onLoadFinished(Message.List list, int mode) {
        mPullToRefreshView.onRefreshComplete();
        onListLoaded(list);
    }

    @Override
    public void onLoadError(int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        linesListView.setVisibility(View.VISIBLE);
    }

    private ArrayList<Message> messageList = new ArrayList();
    private void onListLoaded(Message.List data) {
        mLoadingProgress.setVisibility(View.GONE);
        linesListView.setVisibility(View.VISIBLE);
        if (data == null || data.list == null|| (mScenicListLoader != null && !mScenicListLoader.getPaginator().hasMorePages())) {
            // Disable PULL_FROM_END
//            mPullToRefreshView.setMode(Mode.PULL_FROM_END);
//            return;
        }


        List<Message> list = null;
        if (data != null) {
            list = data.list;
        }
        if (list == null || list.isEmpty()) {
            if (messageList.isEmpty()) {
                mEmptyHintView.setVisibility(View.VISIBLE);
            }
        }
        if (data != null) {
            if (data.pagination != null && data.pagination.offset == 0) {
                mAdapter.setDataItems(null);
            }
            mAdapter.appendDataItems(data.list);
            messageList.addAll(data.list);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void onItemClicked(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        mScenic = (Scenic) v.getTag();
        if (mScenic == null) {
            Log.e("NULL mScenic");
            return;
        }
        switch (v.getId()) {
            /*case R.id.is_select:
                if(mScenic.getSelect()){
                    mScenic.setSelect(false);
                }else {
                    mScenic.setSelect(true);
                }
                mAdapter.notifyDataSetChanged();
                break;*/
            case R.id.item_message:
                Intent intent = new Intent();
                intent.setClass(getActivity(), GuiderMessageDetailActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left:
                getActivity().finish();
                break;
            case R.id.action_bar_right_text: {

                break;
            }
            case R.id.reload_view: {
                reload();
                break;
            }
            default:
                break;
        }
    }
    private void onHeaderClicked(View v) {
        switch (v.getId()) {
          }
    }
    @Override
    public void onDestroy() {
        ShareManager.getInstance().onEnd();
        super.onDestroy();
    }



}
