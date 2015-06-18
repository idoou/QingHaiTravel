/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.ResponseError;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.QHCollectionStrategy;
import com.ziyou.selftravel.model.QHComment;
import com.ziyou.selftravel.model.QHStrategy;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.BottomTab;

import org.w3c.dom.Text;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * @author kuloud
 */
public class SpecialDetailActivity extends BaseActivity implements View.OnClickListener{
    private Context mContext;
    private LayoutInflater mInflater;
    private NetworkImageView headerImg;
    private TextView img_num,special_name,special_intro;
    private QHStrategy guide_detail;
    private LinearLayoutManager mLayoutManager;

    private GuideDetailAdapter mAdapter;
    private QHStrategy guide_detail1;

    @InjectView(R.id.guide_recyclerview)
    public RecyclerView mRecyclerView;

    @InjectView(R.id.tv_comment)
    public TextView tv_comment;
    @InjectView(R.id.tv_praise)
    public TextView tv_praise;

//    @InjectView(R.id.comment)
//    public TextView comment;

    private QHComment.QHCommentList mCommentList;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mInflater = LayoutInflater.from(this);
        setContentView(R.layout.activity_special_detail);
        ButterKnife.inject(this);
        Bundle guide = getIntent().getBundleExtra("guide");
        guide_detail1 = guide.getParcelable("guide");

        initView();
        initData();
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(guide_detail1.title);
        actionBar.getTitleView().setTextColor(Color.WHITE);
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView() .setImageResource(R.drawable.return_back);
        actionBar.getLeftView().setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left: {
               finish();
                break;
            }
            case R.id.tv_comment:
                Intent intent = new Intent(SpecialDetailActivity.this,CommentListActivity.class);
                intent.putExtra("url",url);
                intent.putExtra("object_id",guide_detail1.id);
                intent.putExtra("ctype",Integer.parseInt(ServerAPI.Comments.Type.RAIDERS.value()));
                startActivity(intent);
                break;
            case R.id.tv_praise:
                voteComment();
                break;
        }
    }
    private void initView(){
//        headerImg = (NetworkImageView)findViewById(R.id.iv_cover_image);
//        img_num = (TextView)findViewById(R.id.rest_imgs);
//        special_name = (TextView)findViewById(R.id.rest_name);
//        special_intro = (TextView)findViewById(R.id.special_context);
    }


    private void initData(){

        mRecyclerView.setVisibility(View.INVISIBLE);
        //创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        //mRecyclerView.setHasFixedSize(true);
        //创建并设置Adapter
        mAdapter = new GuideDetailAdapter(guide_detail1);
        mRecyclerView.setAdapter(mAdapter);

        tv_comment.setOnClickListener(this);
        tv_praise.setOnClickListener(this);
        getCommentList();

        tv_praise.setText(""+guide_detail1.vote_count);
        tv_comment.setText(""+guide_detail1.comment_count);

    }

    public void getCommentList() {
        url = "http://182.92.218.24:9000/api/guides/"+guide_detail1.id+"/comments/?format=json";

        RequestManager.getInstance().sendGsonRequest(url, QHComment.QHCommentList.class,
                new Response.Listener<QHComment.QHCommentList>() {
                    @Override
                    public void onResponse(QHComment.QHCommentList response) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        Log.i("law respond",response.toString());
                        Log.i("law",response.toString());
                        mCommentList = response;
//                        Log.d("onResponse, CommentList=%s", response);
//                        mLoadingProgress.setVisibility(View.GONE);
//                        mContainerView.setVisibility(View.VISIBLE);
//                        if (response == null || response.list == null || response.list.isEmpty()) {
//                            mEmptyHintView.setVisibility(View.VISIBLE);
//                            return;
//                        }
//                        mEmptyHintView.setVisibility(View.GONE);
//                        onCommentListLoaded(response);
                        updateComment(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("law error",error.toString());
                        Log.i("law",error.toString());
//                        Log.e(error, "Error loading scenic details from %s", url);
//                        // TODO handle it
//                        mLoadingProgress.setVisibility(View.GONE);
//                        mEmptyHintView.setVisibility(View.VISIBLE);
                    }
                }, requestTag);
    }

    private void updateComment(QHComment.QHCommentList response) {
        tv_comment.setText(""+response.results.size());
    }

    private class GuideDetailAdapter extends RecyclerView.Adapter {

        private QHStrategy strategyDetail;

        public GuideDetailAdapter(QHStrategy strategyDetail) {
            this.strategyDetail = strategyDetail;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(getApplicationContext(),R.layout.item_route_detail1,null);
            return new GuideHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            GuideHolder routeHolder = (GuideHolder) holder;
            String large_url = strategyDetail.guide_info.get(position).image_url;
            String content = strategyDetail.guide_info.get(position).content;

            if (!TextUtils.isEmpty(large_url)) {
                RequestManager.getInstance().getImageLoader().get(large_url,
                        ImageLoader.getImageListener(routeHolder.imageView,
                                R.drawable.bg_banner_hint, R.drawable.bg_banner_hint));
            }

            routeHolder.textView.setText(content);

        }

        @Override
        public int getItemCount() {
            return strategyDetail.guide_info.size();
        }


        private class GuideHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            public GuideHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.route_detail_image);
                textView = (TextView) view.findViewById(R.id.route_detail_tv);
            }
        }
    }

    private void voteComment() {
        if (guide_detail1 == null || tv_praise == null) {
            com.ziyou.selftravel.util.Log.e("[voteComment] NULL comment:" + guide_detail1);
            return;
        }

        CommentObj obj = new CommentObj(""+guide_detail1.id,ServerAPI.Comments.Type.RAIDERS.value());
        Gson gson = new Gson();
        String jsonBody = gson.toJson(obj);
        RequestManager.getInstance().sendGsonRequest(Request.Method.POST, ServerAPI.Comments.GUIDER_VOTE_URL,
                jsonBody,
                QHCollectionStrategy.class, new Response.Listener<QHCollectionStrategy>() {
                    @Override
                    public void onResponse(QHCollectionStrategy response) {
                        tv_praise.setText(String.valueOf(response.vote_count));
//                        tv_praise.getCompoundDrawables()[1].setLevel(2);
                        guide_detail1.voted = 1;
                        guide_detail1.vote_count = response.vote_count;
                        tv_praise.setEnabled(false);
                        tv_praise.setClickable(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof ResponseError
                                && ((ResponseError) error).errCode == ServerAPI.ErrorCode.ERROR_ALREADY_VOTED) {
                            Toast.makeText(getApplicationContext(), R.string.error_already_voted,  Toast.LENGTH_SHORT).show();
                        } else {
                            AppUtils.handleResponseError(SpecialDetailActivity.this, error);
                        }

                    }
                }, "--------------");
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

}
