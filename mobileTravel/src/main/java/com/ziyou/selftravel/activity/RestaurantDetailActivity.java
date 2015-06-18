/**
 *
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.RouteDetailAdapter;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.ResponseError;
import com.ziyou.selftravel.model.HomeRestuarantDetail;
import com.ziyou.selftravel.model.QHCollectionStrategy;
import com.ziyou.selftravel.model.QHComment;
import com.ziyou.selftravel.model.QHRoute;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * @author kuloud
 */
public class RestaurantDetailActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private GridView food_type_grid;
    private TextView img_num, rest_name, rest_average_cost, rest_tel, rest_address, rest_special_foods;
    private NetworkImageView headerImg;
    private RatingBar star;
    private String special_foods = "牛肉 几楼问  宮保小球   毛血旺  烤羊排 都得 的典范 肉包囊  極品大閘蟹";
    public ArrayList<String> food_types = new ArrayList();
    protected String mRequestTag = RestaurantDetailActivity.class.getName();
    private String bussiness_id;
    private String bussiness_price;
    private String telphone;
    private QHRoute routedetail;
    @InjectView(R.id.route_recyclerview)
    public RecyclerView mRecyclerView;

    @InjectView(R.id.tv_comment)
    public TextView tv_comment;
    @InjectView(R.id.tv_praise)
    public TextView tv_praise;


    private LinearLayoutManager mLayoutManager;
    private RouteDetailAdapter mAdapter;

    private String url;
    private QHComment.QHCommentList mCommentList;
//    private RouteAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_restaurant_detail);
        ButterKnife.inject(this);
//        bussiness_id = getIntent().getStringExtra(Const.REST_DETAIL_ID);
//        bussiness_price = getIntent().getStringExtra(Const.REST_DETAIL_PRICE);
        Bundle route = getIntent().getBundleExtra("route");
        routedetail = route.getParcelable("route");
        initView();
//        initData();
        initActionBar();
        loadRestDetail(bussiness_id);

        getCommentList();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(routedetail.title);
        actionBar.getTitleView().setTextColor(Color.WHITE);
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView().setImageResource(R.drawable.return_back);
        actionBar.getLeftView().setOnClickListener(this);
    }


    public void getCommentList() {
        url = "http://182.92.218.24:9000/api/routes/"+routedetail.id+"/comments/?format=json";

        RequestManager.getInstance().sendGsonRequest(url, QHComment.QHCommentList.class,
                new Response.Listener<QHComment.QHCommentList>() {
                    @Override
                    public void onResponse(QHComment.QHCommentList response) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        android.util.Log.i("law respond", response.toString());
                        android.util.Log.i("law", response.toString());
                        mCommentList = response;
                        updateComment(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        android.util.Log.i("law error", error.toString());
                        android.util.Log.i("law", error.toString());
                    }
                }, requestTag);
    }

    private void updateComment(QHComment.QHCommentList response) {
        tv_comment.setText(""+response.results.size());
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
                Intent intent = new Intent(RestaurantDetailActivity.this,CommentListActivity.class);
                intent.putExtra("url",url);
                intent.putExtra("object_id",routedetail.id);
                intent.putExtra("ctype",Integer.parseInt(ServerAPI.Comments.Type.ROUTE.value()));
                startActivity(intent);
                break;
            case R.id.tv_praise:
                voteComment();
                break;

        }
    }

    private void initView() {
        headerImg = (NetworkImageView) findViewById(R.id.iv_cover_image);
        food_type_grid = (GridView) findViewById(R.id.rest_comment_type);
        star = (RatingBar) findViewById(R.id.testaurant_rating);
        img_num = (TextView) findViewById(R.id.rest_imgs);
        rest_name = (TextView) findViewById(R.id.rest_name);
        rest_average_cost = (TextView) findViewById(R.id.average_cost);
        rest_tel = (TextView) findViewById(R.id.rest_tel);
        rest_address = (TextView) findViewById(R.id.rest_address);
        rest_special_foods = (TextView) findViewById(R.id.rest_special_foods);
        findViewById(R.id.rest_phone).setOnClickListener(this);


        //创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        //mRecyclerView.setHasFixedSize(true);
        //创建并设置Adapter
        mAdapter = new RouteDetailAdapter(this);
        mAdapter.setTripDetail(routedetail);
        mRecyclerView.setAdapter(mAdapter);

        tv_comment.setOnClickListener(this);
        tv_praise.setOnClickListener(this);

        tv_praise.setText(""+routedetail.vote_count);
        tv_comment.setText(""+routedetail.comment_count);
    }



    private void loadRestDetail(String bussiness_id) {
        final String url;
        url = ServerAPI.Home.buildRestDetailUrl(bussiness_id);

        Log.d("Loading scenic details from %s", url);
        RequestManager.getInstance().sendGsonRequest(Request.Method.GET, url,
                HomeRestuarantDetail.class, null,
                new Response.Listener<HomeRestuarantDetail>() {
                    @Override
                    public void onResponse(HomeRestuarantDetail response) {
                        upadteDate(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
                    }
                }, true, mRequestTag);
    }


    public void upadteDate(HomeRestuarantDetail response) {

        if (response.photo_url != null) {
            RequestManager.getInstance().getImageLoader().get(response.photo_url,
                    ImageLoader.getImageListener(headerImg, R.drawable.bg_image_hint, R.drawable.bg_image_hint));
        }

        if (response.specialties.length > 0) {
            ArrayList<String> list = new ArrayList();
            food_types.clear();
            for (int i = 0; i < response.specialties.length; i++) {
                list.add(response.specialties[i]);
            }
            food_types.addAll(list);
        }

        if (ConstTools.isNumeric("" + response.avg_rating))
            star.setRating(Float.parseFloat(response.avg_rating));
        else
            star.setRating(0.0f);
        rest_name.setText(response.name);
        String foods_str = "";
        for (int i = 0; i < response.popular_dishes.length; i++) {
            foods_str += "    " + response.popular_dishes[i];
        }
        rest_special_foods.setText(foods_str);
        if (ConstTools.isNumeric(bussiness_price)) {
            rest_average_cost.setText("￥" + bussiness_price);
            if ("0".equals(bussiness_price)) {
                rest_average_cost.setText("暂无");
                rest_average_cost.setTextColor(0xffb4b4b4);
                rest_average_cost.setTextSize(12);
            }
        } else {
            rest_average_cost.setText("暂无");
            rest_average_cost.setTextColor(0xffb4b4b4);
            rest_average_cost.setTextSize(12);
        }
        telphone = response.telephone;
        rest_tel.setText(getString(R.string.rest_tel, telphone));
        rest_address.setText(getString(R.string.rest_address, response.address));
        img_num.setText("1");
    }

    private void voteComment() {
        if (routedetail == null || tv_praise == null) {
            com.ziyou.selftravel.util.Log.e("[voteComment] NULL comment:" + routedetail);
            return;
        }

        CommentObj obj = new CommentObj(""+routedetail.id,ServerAPI.Comments.Type.ROUTE.value());
        Gson gson = new Gson();
        String jsonBody = gson.toJson(obj);
        RequestManager.getInstance().sendGsonRequest(Request.Method.POST, ServerAPI.Comments.GUIDER_VOTE_URL,
                jsonBody,
                QHCollectionStrategy.class, new Response.Listener<QHCollectionStrategy>() {
                    @Override
                    public void onResponse(QHCollectionStrategy response) {
                        tv_praise.setText(String.valueOf(response.vote_count));
//                        tv_praise.getCompoundDrawables()[1].setLevel(2);
                        routedetail.voted = 1;
                        routedetail.vote_count = response.vote_count;
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
                            AppUtils.handleResponseError(RestaurantDetailActivity.this, error);
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
