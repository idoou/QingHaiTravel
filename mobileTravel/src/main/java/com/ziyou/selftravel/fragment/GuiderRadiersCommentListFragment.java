
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemSubViewClickListener;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;

import com.ziyou.selftravel.activity.CommentDetailsActivity;
import com.ziyou.selftravel.activity.CommentEditActivity;
import com.ziyou.selftravel.activity.NavigationDetailActivity;
import com.ziyou.selftravel.adapter.GuiderRaidersCommentListAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ShareManager;
import com.ziyou.selftravel.app.UserManager;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.ResponseError;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.HomeBanner;
import com.ziyou.selftravel.model.QHCollectionStrategy;
import com.ziyou.selftravel.model.QHNavigation;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.model.QHToken;
import com.ziyou.selftravel.model.Scenic;
import com.ziyou.selftravel.util.AnimUtils;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiderRadiersCommentListFragment extends BaseFragment implements OnClickListener,DataLoaderCallback<Comment.CommentList> {
    private final String TAG = getClass().getName();//"GuiderListFragment";
    private final int REQUEST_CODE_LOGIN_NEW_COMMENT = 1;
    private final int REQUEST_CODE_LOGIN_VOTE = 2;
    private final int REQUEST_CODE_LOGIN_COMMENT_DETAIL = 3;
    private final int REQUEST_CODE_SEARCH = 4;
    private final int REQUEST_CODE_COMMENT_DETAIL = 5;
    private final int REQUEST_CODE_POST_COMMENT = 6;
    private ActionBar mActionBar;
    private GuiderRaidersCommentListAdapter mAdapter;
    private View mLoadingProgress;
    private View mReloadView;
    private PullToRefreshRecyclerView mPullToRefreshView;
    private UrlListLoader<Comment.CommentList> mScenicListLoader;
//    private Request<HomeBanner.HomeBannerLists> mBannerRequest;
    private ViewGroup mRootView;
    private View mEmptyHintView;
    private Scenic mScenic;
    private int HTTP_GET_PARAM_LIMIT = 10;
    private RecyclerView commentListView;
    private Comment mComment;
    private QHScenic scenic;
    private TextView mVoteText;
    private int mId;


    private ArrayList<Comment> raidersListData = new ArrayList<Comment>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShareManager.getInstance().onStart(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.guide_list_fragment, container,false);
        scenic = getArguments().getParcelable(Const.QH_SECNIC);
        mId = getArguments().getInt(Const.QH_SECNIC_ID);
        initViews();
        reload();
        loadCommentData("北京");
        initActionBar(mRootView);

        return mRootView;
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
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
    private void reload() {
        mPullToRefreshView.setMode(Mode.PULL_FROM_END);
        commentListView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
        mScenicListLoader = null;
        RequestManager.getInstance().getRequestQueue().cancelAll(mRequestTag);
    }
    private void initViews() {
        initListView();
        initPullToRefresh();
        mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mEmptyHintView = mRootView.findViewById(R.id.empty_hint_view);
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
//                    loadNearScenics(mScenicDetailsModel.cityZh);
                }
            }
        });
    }
    private void initListView() {
        commentListView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        commentListView.setLayoutManager(layoutManager);
        mAdapter = new GuiderRaidersCommentListAdapter(getActivity());
        ItemClickSupport clickSupport = ItemClickSupport.addTo(commentListView);
        clickSupport.setOnItemSubViewClickListener(new OnItemSubViewClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                    onItemClicked(view);
            }
        });
        commentListView.setItemAnimator(new DefaultItemAnimator());
        commentListView.setAdapter(mAdapter);
    }

    protected void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.getTitleView().setText("评论");
        actionBar.getLeftView().setImageResource(R.drawable.return_back);
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView().setOnClickListener(this);

        actionBar.setBackgroundResource(R.drawable.fg_top_shadow);
        actionBar.getRightView().setImageResource(R.drawable.icon_edit);
        actionBar.getRightView().setOnClickListener(this);

        actionBar.setBackgroundResource(R.color.title_bg);
//        mActionBar.getRightView().setImageResource(R.drawable.);
    }


    private void showReloadView() {
        commentListView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
    }

    public final static int COMMENT_SECNIC = 0;//景区评论
    public final static int COMMENT_RAIDERS = 1;//攻略评论
    public final static int COMMENT_TOUTE = 2;//路线评论
    public final static int COMMENT_COMMENT = 3;//评论的 评论
    public int commentType = 0;

    private void loadCommentData(String cityName) {
        Log.e("-------","loadNearScenics--");
        if(null==scenic) scenic = new QHScenic();scenic.id = mId;


        String  url  = ServerAPI.User.buildSecnicCommentUrl(scenic.id);
        switch (commentType){
            case COMMENT_SECNIC:
                 url  = ServerAPI.User.buildSecnicCommentUrl(scenic.id);
                break;
            case COMMENT_RAIDERS:
                url  = ServerAPI.User.buildSecnicCommentUrl(scenic.id);
                break;
            case COMMENT_TOUTE:
                url  = ServerAPI.User.buildSecnicCommentUrl(scenic.id);
                break;
            case COMMENT_COMMENT:
                url  = ServerAPI.User.buildSecnicCommentUrl(scenic.id);
                break;
        }
        mLoadingProgress.setVisibility(View.VISIBLE);
        commentListView.setVisibility(View.GONE);

        if (mScenicListLoader == null)
            mScenicListLoader = new UrlListLoader<Comment.CommentList>( mRequestTag, Comment.CommentList.class);
        mScenicListLoader.setUrl(url);
        mScenicListLoader.setPageLimit(HTTP_GET_PARAM_LIMIT);
        mScenicListLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }
    @Override
    public void onLoadFinished(Comment.CommentList list, int mode) {
        mPullToRefreshView.onRefreshComplete();
        onListLoaded(list);
    }

    @Override
    public void onLoadError(int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        commentListView.setVisibility(View.VISIBLE);
    }
    private void onListLoaded(Comment.CommentList data) {
        mLoadingProgress.setVisibility(View.GONE);
        commentListView.setVisibility(View.VISIBLE);

        if (data == null || data.list == null|| (mScenicListLoader != null && !mScenicListLoader.getPaginator().hasMorePages())) {
            // Disable PULL_FROM_END
//            mPullToRefreshView.setMode(Mode.PULL_FROM_END);
//            return;
            if (raidersListData.isEmpty()) {
                mEmptyHintView.setVisibility(View.VISIBLE);
            }
        }
        if (data != null) {
            if (data.pagination != null && data.pagination.offset == 0) {
                mAdapter.setDataItems(null);
            }
            mAdapter.appendDataItems(data.results);
            raidersListData.addAll(data.results);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void onItemClicked(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        mComment = (Comment) v.getTag();
        if (mComment == null) {
            Log.e("NULL comment");
            return;
        }

        switch (v.getId()) {
            case R.id.item_comment_root:
            case R.id.comment_cover_image:
            case R.id.comment_count: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_COMMENT_DETAIL);
                AnimUtils.doScaleFadeAnim(v);
                jumpCommentDetail();
                break;
            }

            case R.id.comment_vote_count: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_COMMENT_VOTE);
                AnimUtils.doScaleFadeAnim(v);
                mVoteText = (TextView) v;
                voteComment();
                break;
            }
            case R.id.comment_location:{
                //得到经纬度，打开一个dialog,服务器没有传入地理位置
//                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setPositiveButton("开始导航",new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Intent intent = new Intent(getActivity(),NavigationDetailActivity.class);
//                        Bundle bundle = new Bundle();
//                        //
////                        bundle.putParcelable("navi_detail",tag);
//                        intent.putExtra("navi_bundle",bundle);
//                        getActivity().startActivity(intent);
//                        dialogInterface.dismiss();
//                    }
//                });
//                builder.setNegativeButton("取消导航",new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//                builder.show();
//                break;
            }
        }


    }
    public static final String PARAM_OBJECT_TYPE = "";
    public class CommentObj  {
        public  String obj_id;
        public  String obj_type;

        CommentObj (String id,String type){
            obj_id = id;
            obj_type = type;
        }
    }
    private void voteComment() {
        if (mComment == null || mVoteText == null) {
            Log.e("[voteComment] NULL comment:" + mComment);
            return;
        }

        CommentObj obj = new CommentObj(""+mComment.id,ServerAPI.Comments.Type.COMMENT.value());
        Gson gson = new Gson();
        String jsonBody = gson.toJson(obj);
        RequestManager.getInstance().sendGsonRequest(Request.Method.POST, ServerAPI.Comments.GUIDER_VOTE_URL,
                jsonBody,
                Comment.VoteResponse.class, new Response.Listener<Comment.VoteResponse>() {
                    @Override
                    public void onResponse(Comment.VoteResponse response) {

                        mVoteText.setText(String.valueOf(response.voteCount));
                        mVoteText.getCompoundDrawables()[0].setLevel(2);
                        mComment.isVoted = true;
                        mComment.voteCount = response.voteCount;
                        mVoteText.setEnabled(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error voteComment: %s", error);
                        if (error instanceof ResponseError
                                && ((ResponseError) error).errCode == ServerAPI.ErrorCode.ERROR_ALREADY_VOTED) {
                            Toast.makeText(getActivity(), R.string.error_already_voted,Toast.LENGTH_SHORT).show();
                        } else {
                            AppUtils.handleResponseError(getActivity(), error);
                        }

                    }
                }, "--------------");
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
            case R.id.action_bar_right: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_COMMENT_EDIT);
                if (!UserManager.makeSureLogin(getActivity(), REQUEST_CODE_LOGIN_NEW_COMMENT)) {
                    postNewComment();
                }
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

    @Override
    public void onDestroy() {
        ShareManager.getInstance().onEnd();
        super.onDestroy();
    }
    private void postNewComment() {
        Intent commentEdit = new Intent(getActivity(), CommentEditActivity.class);
        commentEdit.putExtra(Const.EXTRA_ID, scenic.id);
        startActivityForResult(commentEdit, REQUEST_CODE_POST_COMMENT);
    }
    private void jumpCommentDetail() {
        if (mComment == null) {
            Log.e("[jumpCommentDetail] NULL comment");
            return;
        }
        Intent intent = new Intent(getActivity(), CommentDetailsActivity.class);
        intent.putExtra(Const.EXTRA_COMMENT_DATA, mComment);
        startActivityForResult(intent, REQUEST_CODE_COMMENT_DETAIL);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN_NEW_COMMENT:
//                    postNewComment();
                    break;
                case REQUEST_CODE_LOGIN_VOTE:
//                    voteComment();
                    break;
                case REQUEST_CODE_LOGIN_COMMENT_DETAIL:
                    jumpCommentDetail();
                    break;

                case REQUEST_CODE_COMMENT_DETAIL:
                    Comment comment = (Comment) data.getParcelableExtra(Const.EXTRA_COMMENT_DATA);
                    if (comment != null) {
                        mComment.commentCount = comment.commentCount;
                        mComment.isVoted = comment.isVoted;
                        mComment.voteCount = comment.voteCount;
                        List<Comment> dataItems = mAdapter.getDataItems();
                        if (dataItems != null) {
                            int position = -1;
                            for (int i = 0; i < dataItems.size(); i++) {
                                if (dataItems.get(i).id == mComment.id) {
                                    position = i;
                                    break;
                                }
                            }

                            if (position >= 0) {
                                // Count the header item
                                position++;
                                mAdapter.notifyItemChanged(position);
                            }
                        }
                    }
                    break;
                case REQUEST_CODE_POST_COMMENT: {
                    Comment c = (Comment) data.getParcelableExtra(Const.EXTRA_COMMENT_DATA);
                    if (c != null) {
                        mAdapter.getDataItems().add(0, c);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

}
