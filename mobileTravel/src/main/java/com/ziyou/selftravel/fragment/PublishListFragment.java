
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.AppendableAdapter;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.GuiderPublishedCommentListAdapter;
import com.ziyou.selftravel.adapter.GuiderRaidersCommentListAdapter;
import com.ziyou.selftravel.adapter.PublishRaidersListAdapter;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.QHRoute;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.model.QHUser;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PublishListFragment extends BaseFragment implements DataLoaderCallback<Comment.CommentList>,OnClickListener {
    private PullToRefreshRecyclerView commentRefreshView;
    private RecyclerView comment_listView;
    private PullToRefreshRecyclerView raidersfreshView;
    private RecyclerView raiders_listView;
    private Context mContext;
    private ArrayList<QHStrategy> raidersListData = new ArrayList<QHStrategy>();
    private ArrayList<Comment> commentListData = new ArrayList<Comment>();
    private UrlListLoader<Comment.CommentList> commentLoader;
    private UrlListLoader<QHStrategy.QHStrategyList> raidersmLoader;//推荐
    private GuiderPublishedCommentListAdapter commentAdapter;
    private AppendableAdapter<QHStrategy> raidersAdapter;
    private View mLoadingProgress;
    private View mEmptyHintView;
    private View mRootView;
    private View mReloadView;
    private LayoutManager commentLayoutManager;
    private LayoutManager raidersLayoutManager;
    private static final String ARGS_KEY_LAYOUT_MANAGER = "args_key_layout_manager";
    private static final String ARGS_KEY_ADAPTER = "args_key_adapter";
    private Location myLocation;
    private RadioGroup switch_ll;
    private RadioButton radier,comment;
    private RaiersListener raiersListener = new RaiersListener();
    private int state_type = 0;//0 攻略  1评论。
    QHUser user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        commentLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        raidersLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        commentAdapter = new GuiderPublishedCommentListAdapter(getActivity());
        raidersAdapter = new PublishRaidersListAdapter(getActivity());
        user = AppUtils.getQHUser(getActivity());
        initLoadUrl();
        super.onCreate(savedInstanceState);
    }
    private void initLoadUrl(){
        //加载发布信息
        commentLoader = new UrlListLoader<Comment.CommentList>(mRequestTag,Comment.CommentList.class/*, page*/ );
        commentLoader.setUseCache(true);
        String  url = ServerAPI.User.buildPublishInfo(3,user.id);
        commentLoader.setUrl(url);

        raidersmLoader = new UrlListLoader<QHStrategy.QHStrategyList>(mRequestTag,QHStrategy.QHStrategyList.class);
        raidersmLoader.setUseCache(true);
        String  urlraiders = ServerAPI.User.buildPublishInfo(2,user.id);
        raidersmLoader.setUrl(urlraiders);


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        reload();
        super.onActivityCreated(savedInstanceState);
    }

    private void reload() {
        if(state_type==0){
            raidersfreshView.setVisibility(View.GONE);
            mLoadingProgress.setVisibility(View.VISIBLE);
            mReloadView.setVisibility(View.GONE);
            raidersmLoader.loadMoreQHData(raiersListener, DataLoader.MODE_LOAD_MORE);
        }else if(state_type==1){
            commentRefreshView.setVisibility(View.GONE);
            mLoadingProgress.setVisibility(View.VISIBLE);
            mReloadView.setVisibility(View.GONE);
            commentLoader.loadMoreQHData(this, DataLoader.MODE_LOAD_MORE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity.getApplicationContext();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.publish_fragment, container, false);
//        initActionBar(mRootView);
        mEmptyHintView = mRootView.findViewById(R.id.empty_hint_view);
        if (mLoadingProgress != null) {
            mRootView.findViewById(R.id.loading_progress).setVisibility(View.GONE);
        } else {
            mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        }
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);
        initListView();


        radier = (RadioButton)mRootView.findViewById(R.id.me_strategy);
        comment = (RadioButton)mRootView.findViewById(R.id.me_route);
        comment.setText("点评");

        switch_ll = (RadioGroup)mRootView.findViewById(R.id.fragment_switch);
        switch_ll.setVisibility(View.VISIBLE);
        switch_ll.check(R.id.me_strategy);
        switch_ll.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.me_strategy){
                    state_type = 0;
                    resetStateView(0);
                }else if(checkedId==R.id.me_route){
                    state_type = 1;
                    resetStateView(1);
                }
            }
        });
        initActionBar(mRootView);
        return mRootView;
    }
    private void initListView(){

        commentRefreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_twowayview);
        commentRefreshView.setMode(Mode.BOTH);
        commentRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                int mode = refreshView.getCurrentMode() ==
                        Mode.PULL_FROM_START ? DataLoader.MODE_REFRESH
                        : DataLoader.MODE_LOAD_MORE;
                commentLoader.loadMoreData(PublishListFragment.this, mode);
            }
        });
        comment_listView = commentRefreshView.getRefreshableView();
        comment_listView.setLayoutManager(commentLayoutManager);
        comment_listView.setItemAnimator(new DefaultItemAnimator());
        int scap = ScreenUtils.dpToPxInt(getActivity(), 4);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(comment_listView);
        comment_listView.addItemDecoration(decor);
        comment_listView.setAdapter(commentAdapter);


        raidersfreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_newview);
        raidersfreshView.setMode(Mode.BOTH);
        raidersfreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                int mode = refreshView.getCurrentMode() ==
                        Mode.PULL_FROM_START ? DataLoader.MODE_REFRESH: DataLoader.MODE_LOAD_MORE;
                raidersmLoader.loadMoreData(raiersListener, mode);
            }
        });
        raiders_listView = raidersfreshView.getRefreshableView();
        raiders_listView.setLayoutManager(raidersLayoutManager);
        raiders_listView.setItemAnimator(new DefaultItemAnimator());
        decor.initWithRecyclerView(raiders_listView);
        raiders_listView.addItemDecoration(decor);
        raiders_listView.setAdapter(raidersAdapter);
    }

    public void resetStateView(int type){
        mEmptyHintView.setVisibility(View.GONE);
        if(type==0){
            raidersfreshView.setVisibility(View.VISIBLE);
            raiders_listView.setVisibility(View.VISIBLE);
            commentRefreshView.setVisibility(View.GONE);
            comment_listView.setVisibility(View.GONE);
            if (raidersListData.isEmpty()) {
                reload();
            }
        }
        else if(type==1){
            raidersfreshView.setVisibility(View.GONE);
            raiders_listView.setVisibility(View.GONE);
            commentRefreshView.setVisibility(View.VISIBLE);
            comment_listView.setVisibility(View.VISIBLE);
            if (commentListData.isEmpty()) {
                reload();
            }
        }
    }
    @Override
    public void onLoadFinished(Comment.CommentList videoList, int mode) {
        mLoadingProgress.setVisibility(View.GONE);
        commentRefreshView.onRefreshComplete();

        List<Comment> list = null;
        if (videoList != null) {
            list = videoList.results;
        }
        if (list == null || list.isEmpty()) {
            if (commentListData.isEmpty()) {
                mEmptyHintView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mode == DataLoader.MODE_REFRESH ||null==videoList.pagination|| videoList.pagination.offset == 0) {
                commentListData.clear();
                commentAdapter.setDataItems(commentListData);
            }
            commentRefreshView.setVisibility(View.VISIBLE);
            commentListData.addAll(videoList.results);
            commentAdapter.appendDataItems(videoList.results);
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
    protected void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.getTitleView().setText("我的发布");
        actionBar.getLeftView().setImageResource(R.drawable.return_back);
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView().setOnClickListener(this);
//        mActionBar.getRightView().setImageResource(R.drawable.);
    }


    public class RaiersListener implements DataLoaderCallback<QHStrategy.QHStrategyList>{

        @Override
        public void onLoadFinished(QHStrategy.QHStrategyList videoList, int mode) {
            mLoadingProgress.setVisibility(View.GONE);
            raidersfreshView.onRefreshComplete();
            List<QHStrategy> list = null;
            if (videoList != null) {
                list = videoList.results;
            }
            if (list == null || list.isEmpty()) {
                if (raidersListData.isEmpty()) {
                    mEmptyHintView.setVisibility(View.VISIBLE);
                }
            } else {
                if (mode == DataLoader.MODE_REFRESH || null == videoList.pagination || videoList.pagination.offset == 0) {
                    raidersListData.clear();
                    ((AppendableAdapter) raidersfreshView.getRefreshableView().getAdapter()).setDataItems(raidersListData);
                }
                raidersfreshView.setVisibility(View.VISIBLE);
                raidersListData.addAll(videoList.results);
                raidersAdapter.appendDataItems(videoList.results);
            }
        }
        @Override
        public void onLoadError(int mode) {
            mLoadingProgress.setVisibility(View.GONE);
            mReloadView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, R.string.loading_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
