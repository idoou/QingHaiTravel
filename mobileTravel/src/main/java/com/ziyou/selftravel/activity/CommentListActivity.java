/**
 *
 */

package com.ziyou.selftravel.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.CommentAdapter;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.Comments.Type;
import com.ziyou.selftravel.app.UserManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.Comment.NewCommentResult;
import com.ziyou.selftravel.model.QHComment;
import com.ziyou.selftravel.model.QHComment.QHCommentList;
import com.ziyou.selftravel.model.User;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author kuloud
 */
public class CommentListActivity extends BaseActivity implements OnClickListener {
    private final int REQUEST_CODE_LOGIN_NEW_COMMENT = 1;
    protected static final int RESULT_CODE_NEW_COMMENT = 2;
    private RecyclerView mRecyclerView;
    private View mEmptyHintView;
    private View mLoadingProgress;
    private View mContainerView;
    private View mPostButton;
    private EditText mNewCommentEditText;
    private CommentAdapter mAdapter;
    private int object_id = -1;
    private int ctype = -1;
    private int mNewCommentCount;
    private Type mType = Type.SCENIC;

    private static final int GET_COMMENTS_PARAM_LIMIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_details);
        object_id = getIntent().getIntExtra("object_id", 0);
        ctype = getIntent().getIntExtra("ctype", 0);
        initViews();
        getCommentList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN_NEW_COMMENT:
                    postComment(mNewCommentEditText.getText().toString());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void finish() {
        if (mNewCommentCount > 0) {
            Intent i = new Intent();
            i.putExtra("count", mNewCommentCount);
            setResult(RESULT_CODE_NEW_COMMENT, i);
        }
        super.finish();
    }

    private void initViews() {
        initActionBar();
        initListView();
        mPostButton = findViewById(R.id.comment_post_button);
        mPostButton.setOnClickListener(this);
        mNewCommentEditText = (EditText) findViewById(R.id.new_commnet_text);
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle("评论");
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(this);
    }

    private void initListView() {
        mEmptyHintView = findViewById(R.id.empty_hint_view);
        mLoadingProgress = findViewById(R.id.loading_progress);
        mContainerView = findViewById(R.id.container);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommentAdapter(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
    }

    public void getCommentList() {
//        final String url = ServerAPI.Comments.buildUrl(this, mId, mType,
//                GET_COMMENTS_PARAM_LIMIT, 0);
//        Log.d("Loading comment list from %s", url);
        final String url = getIntent().getStringExtra("url");

            RequestManager.getInstance().sendGsonRequest(url, QHCommentList.class,
                new Response.Listener<QHCommentList>() {
                    @Override
                    public void onResponse(QHCommentList response) {
                        Log.d("onResponse, CommentList=%s", response);
                        mLoadingProgress.setVisibility(View.GONE);
                        mContainerView.setVisibility(View.VISIBLE);
                        if (response == null || response.results == null || response.results.isEmpty()) {
                            mEmptyHintView.setVisibility(View.VISIBLE);
                            return;
                        }
                        mEmptyHintView.setVisibility(View.GONE);
                        onCommentListLoaded(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
                        // TODO handle it
                        mLoadingProgress.setVisibility(View.GONE);
                        mEmptyHintView.setVisibility(View.VISIBLE);
                    }
                }, requestTag);
    }

    private void postComment(String commentStr) {
        final QHComment comment = new QHComment();
        comment.content = commentStr;
        comment.object_id = object_id;
        comment.ctype = ctype;
//        comment.allowReply = false;
//        comment.allowVote = false;

        Gson gson = new Gson();
        String jsonBody = gson.toJson(comment);
        Log.d("posting comment, mComment=%s", jsonBody);
        RequestManager.getInstance().sendGsonRequest(Method.POST,
                ServerAPI.User.buildWriteCommentUrl(),
                jsonBody,
                QHComment.class, new Response.Listener<QHComment>() {
                    @Override
                    public void onResponse(QHComment result) {
                        Log.d("New comment posted, id=: %d", result.object_id);
                        if (result.object_id > 0) {
                            comment.object_id  =  result.object_id;
                            User user = AppUtils.getUser(getApplicationContext());
                            if (user != null) {
//                                comment.setAuthor(user);
                            }
                            // TODO: This time is may different from server time
//                            comment.createTime = TimeUtils.formatTime(System.currentTimeMillis(),
//                                    TimeUtils.DATE_TIME_FORMAT);
                            commentPostSuccess(result);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error post new comment, error=%s", error);
                        mNewCommentEditText.setEnabled(true);
                        AppUtils.handleResponseError(CommentListActivity.this, error);
                    }
                }, requestTag);
    }

    protected void commentPostError() {
        mNewCommentEditText.setEnabled(true);
        Toast.makeText(this, R.string.comment_post_failed, Toast.LENGTH_SHORT).show();
    }

    protected void commentPostSuccess(QHComment newComment) {
        mNewCommentEditText.setEnabled(true);
        mNewCommentEditText.setText("");
        List<QHComment> list = mAdapter.getDataItems();
        list.add(newComment);
        Collections.sort(list, new Comparator<QHComment>() {

            @Override
            public int compare(QHComment lhs, QHComment rhs) {
                return (int) 1;
//                        (TimeUtils
//                        .parseTimeToMills(rhs.createTime) - TimeUtils
//                        .parseTimeToMills(lhs.createTime));
            }
        });

        mAdapter.setDataItems(list);
        if (list.size() > 0) {
            mEmptyHintView.setVisibility(View.GONE);
        }
        mNewCommentCount++;
    }

    public void onCommentListLoaded(QHCommentList data) {
        mAdapter.setDataItems(data.results);
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
            case R.id.comment_post_button: {
                if (!TextUtils.isEmpty(mNewCommentEditText.getText().toString())) {
                    if (!UserManager.makeSureLogin(CommentListActivity.this, REQUEST_CODE_LOGIN_NEW_COMMENT)) {
                        mNewCommentEditText.setEnabled(false);
                        postComment(mNewCommentEditText.getText().toString());
                    }
                } else {
                    Toast.makeText(this, R.string.error_no_comment, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }
    }

}
