
package com.ziyou.selftravel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.CommentDetailsAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.Comments.Type;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.StringConverter;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.Comment.CommentList;
import com.ziyou.selftravel.model.Comment.NewCommentResult;
import com.ziyou.selftravel.model.Comment.VoteResponse;
import com.ziyou.selftravel.model.CommentImage;
import com.ziyou.selftravel.model.CompoundImage;
import com.ziyou.selftravel.upyun.UploadTask;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.ImageHelper;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.TimeUtils;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CommentDetailsActivity extends BaseActivity implements OnClickListener,
        DataLoaderCallback<CommentList> ,UploadTask.UploadCallBack{

    private Comment mComment;
    private View mLoadingProgress;
    private View mEmptyHintView;
    private View mContainerView;

    private RecyclerView mRecyclerView;
    private CommentDetailsAdapter mAdapter;

    private PullToRefreshRecyclerView mPullToRefreshView;

    private UrlListLoader<CommentList> mCommentLoader;

    private View mPostButton;
    private EditText mNewCommentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mComment = (Comment) getIntent().getParcelableExtra(Const.EXTRA_COMMENT_DATA);
        if (mComment == null) {
            Log.e("Invalid comment obj %s", mComment);
            finish();
            return;
        }
        setContentView(R.layout.activity_comment_details);
        initViews();
        loadComments();
    }

    private void initViews() {
        initActionBar();

        mLoadingProgress = findViewById(R.id.loading_progress);
        mEmptyHintView = findViewById(R.id.empty_hint_view);
        mContainerView = findViewById(R.id.container);

        initPullToRefresh();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommentDetailsAdapter(this);
        mAdapter.setHeader(mComment);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mPostButton = findViewById(R.id.comment_post_button);
        mPostButton.setOnClickListener(this);
        mNewCommentEditText = (EditText) findViewById(R.id.new_commnet_text);
    }

    private void initPullToRefresh() {
        mPullToRefreshView = (PullToRefreshRecyclerView) findViewById(R.id.pulltorefresh_recyclerview);
        mPullToRefreshView.setMode(Mode.PULL_FROM_END);
        mRecyclerView = mPullToRefreshView.getRefreshableView();
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {

            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadComments();
            }
        });
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        if (mComment.author != null && !TextUtils.isEmpty(mComment.author.name)) {
            actionBar.setTitle(mComment.author.name);
        }
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        if (mComment.isVoted) {
            actionBar.getRightView().setImageResource(R.drawable.ic_action_bar_praise_selected);
        } else {
            actionBar.getRightView().setImageResource(R.drawable.ic_action_bar_praise);
        }

        actionBar.getLeftView().setOnClickListener(this);
        actionBar.getRightView().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left:
                finish();
                break;
            case R.id.action_bar_right:
                if (mComment == null || mComment.isVoted) {
                    return;
                }
                voteComment(mComment, v, mAdapter.getVoteCountView());
                break;
            case R.id.comment_post_button: {
                if (!TextUtils.isEmpty(mNewCommentEditText.getText().toString())) {
                    mNewCommentEditText.setEnabled(false);
                    postComment(mNewCommentEditText.getText().toString());
                    mNewCommentEditText.setText("");
                } else {
                    Toast.makeText(this, R.string.error_no_comment, Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default:
                break;
        }
    }

    private void voteComment(final Comment comment, final View voteButton, final TextView voteText) {
        if (voteText == null) {
            Log.e("voteComment view NULL");
        }
        voteButton.setEnabled(false);
        Map<String, String> params = ServerAPI.Comments.buildVoteParams(getApplicationContext(), comment.id, Type.COMMENT);
        RequestManager.getInstance().sendGsonRequest(ServerAPI.Comments.VOTE_URL,
                VoteResponse.class,
                new Response.Listener<VoteResponse>() {
                    @Override
                    public void onResponse(VoteResponse response) {
                        voteText.setText(String.valueOf(response.voteCount));
                        voteButton.setEnabled(false);
                        comment.isVoted = true;
                        comment.voteCount = response.voteCount;

                        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
                        actionBar.getRightView().setImageResource(
                                R.drawable.ic_action_bar_praise_selected);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error voteComment: %s", error);
                        voteButton.setEnabled(true);
                    }
                }, false, params, requestTag);
    }

    private void postComment(String commentStr){
        final Comment comment = new Comment();
        comment.content = commentStr;
        comment.objectId = mComment.id;
        comment.type = Type.COMMENT.value();
        comment.ctype = Integer.parseInt(Type.COMMENT.value());
        comment.allowReply = false;
        comment.allowVote = false;
        comment.anonymous = AppUtils.getVistorId(activity);
         List<File> imgFiles = new ArrayList<File>();
        new UploadTask(CommentDetailsActivity.this, imgFiles , mComment.content, mComment.objectId, mComment.ctype ,this).execute("", "");
    }
    /*private void postComment(String commentStr) {
        final Comment comment = new Comment();
        comment.content = commentStr;
        comment.objectId = mComment.id;
        comment.type = Type.COMMENT.value();
        comment.ctype = Integer.parseInt(Type.COMMENT.value());
        comment.allowReply = false;
        comment.allowVote = false;
        comment.anonymous = AppUtils.getVistorId(activity);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(comment);
        Log.d("posting comment, mComment=%s", jsonBody);
        RequestManager.getInstance().sendGsonRequest(Method.POST,
                ServerAPI.User.buildWriteCommentUrl(),
                jsonBody,
                NewCommentResult.class, new Response.Listener<NewCommentResult>() {
                    @Override
                    public void onResponse(NewCommentResult result) {
                        Log.d("New comment posted, id=: %d", result.commentId);
                        if (result.commentId > 0) {
                            comment.id = result.commentId;
                            comment.setAuthor(AppUtils.getUser(getApplicationContext()));
                            // TODO: This time is may different from server time
                            comment.createTime = TimeUtils.formatTime(System.currentTimeMillis(), TimeUtils.DATE_TIME_FORMAT);
                            commentPostSuccess(comment);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error post new comment, error=%s", error);
                        commentPostError();
                    }
                }, requestTag);
    }*/

    protected void commentPostError() {
        mNewCommentEditText.setEnabled(true);
        Toast.makeText(this, R.string.comment_post_failed, Toast.LENGTH_SHORT).show();
    }

    protected void commentPostSuccess(Comment newComment) {
        mNewCommentEditText.setEnabled(true);
        List<Comment> list = mAdapter.getDataItems();
        list.add(newComment);
        Collections.sort(list, new Comparator<Comment>() {

            @Override
            public int compare(Comment lhs, Comment rhs) {
                return (int) (TimeUtils
                        .parseTimeToMills(rhs.createTime) - TimeUtils
                        .parseTimeToMills(lhs.createTime));
            }
        });

        mAdapter.setDataItems(list);

        Comment header = mAdapter.getHeader();
        if (header != null) {
            header.commentCount++;
            mAdapter.notifyItemChanged(0);
        }
        if (mAdapter.getItemCount() > 1) {
            mRecyclerView.scrollToPosition(1);
        }
    }

    private void loadComments() {
        String  url  = ServerAPI.User.buildCommentCommentUrl(mComment.id);


        if (mCommentLoader == null) {
            mCommentLoader = new UrlListLoader<CommentList>(requestTag, CommentList.class);
            mCommentLoader.setUrl(url);
//            mCommentLoader.setUrl(ServerAPI.Comments.buildUrl(this, mComment.id,ServerAPI.Comments.Type.COMMENT));
        }

        mCommentLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(Const.EXTRA_COMMENT_DATA, mComment);
        setResult(RESULT_OK, data);

        super.finish();
    }

    @Override
    public void onLoadFinished(CommentList data, int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mContainerView.setVisibility(View.VISIBLE);
        if (data == null || data.list == null
                || (mCommentLoader != null && !mCommentLoader.getPaginator().hasMorePages())) {
            // Disable PULL_FROM_END
            mPullToRefreshView.setMode(Mode.DISABLED);
        }

        if (data != null) {
            mAdapter.appendDataItems(data.list);
        }

    }

    @Override
    public void onLoadError(int mode) {
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mEmptyHintView.setVisibility(View.VISIBLE);
        Log.d("Error loadComments");
    }


    @Override
    public void onSuccess(String result) {
        Gson mGson = new Gson();
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(String.class, new StringConverter());
        mGson = gb.create();
        mComment =  mGson.fromJson(result.toString(), Comment.class);

        commentPostSuccess(mComment);
    }
    @Override
    public void onFailed() {

    }

}
