package com.ziyou.selftravel.activity;
///**
// * 
// */
//
//package com.ziyou.selftravel.activity;
//
//import java.util.ArrayList;
//
//import android.os.Bundle;
//import android.support.v7.widget.DefaultItemAnimator;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//
//
//import com.kuloud.android.widget.recyclerview.ItemClickSupport;
//import com.kuloud.android.widget.recyclerview.ListLayoutManager;
//import com.kuloud.android.widget.recyclerview.TwoWayLayoutManager.Orientation;
//import android.support.v7.widget.RecyclerView;
//import com.ziyou.selftravel.R;
//import com.ziyou.selftravel.adapter.TripDetailAdapter;
//import com.ziyou.selftravel.app.Const;
//import com.ziyou.selftravel.model.TripContent;
//import com.ziyou.selftravel.model.TripDay;
//import com.ziyou.selftravel.model.TripDetail;
//import com.ziyou.selftravel.util.AppUtils;
//import com.ziyou.selftravel.util.ScreenUtils;
//import com.ziyou.selftravel.widget.ActionBar;
//
///**
// * @author kuloud
// */
//public class TripEditStepTwoActivity extends BaseActivity {
//
//    private TwoWayView mRecyclerView = null;
//    private TripDetailAdapter mAdapter= null;
//    private TripDetail mTripDetail = null;
//    private String startDate = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_list);
//        mTripDetail = new TripDetail();
//        mTripDetail.title = getIntent().getStringExtra(Const.EXTRA_NAME);
//        startDate = getIntent().getStringExtra(Const.EXTRA_DATE);
//        mTripDetail.days = new ArrayList<TripDay>();
//        TripDay firstDay = new TripDay();
//        firstDay.date = startDate;
//        TripContent emptyContent = new TripContent();
//        firstDay.details = new ArrayList<TripContent>();
//        firstDay.details.add(emptyContent);
//        mTripDetail.days.add(firstDay);
//        mTripDetail.author = AppUtils.getUser(activity);
//        initViews();
//    }
//
//    private void initViews() {
//        initActionBar();
//        initListView();
//    }
//
//    private void initActionBar() {
//        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
//        actionBar.setTitle(mTripDetail.title);
//        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
//        actionBar.getLeftView().setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        actionBar.setRightMode(true);
//        actionBar.getRightTextView().setText(R.string.finish);
//        actionBar.getRightTextView().setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//            }
//        });
//    }
//
//    private void initListView() {
//        mRecyclerView = (TwoWayView) findViewById(R.id.recyclerview);
//        ListLayoutManager layoutManager = new ListLayoutManager(Orientation.VERTICAL);
//        mRecyclerView.setLayoutManager(layoutManager);
//        int padding = ScreenUtils.dpToPxInt(activity, 13);
//        mRecyclerView.setPadding(padding, 0, padding, padding);
//        mAdapter = new TripDetailAdapter();
//        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
//        clickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(RecyclerView parent, View view, int position, long id) {
//            }
//        });
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.setAdapter(mAdapter);
//    }
// }
