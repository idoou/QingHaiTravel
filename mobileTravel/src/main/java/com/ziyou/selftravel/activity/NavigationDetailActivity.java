package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.view.View;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.model.QHNavigation;
import com.ziyou.selftravel.util.LocationUtil;
import com.ziyou.selftravel.widget.circularprogressbar.CircularProgressBar;


import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 
 *导航界面
 * 
 * */
public class NavigationDetailActivity extends BaseActivity implements
        AMapNaviViewListener {

    @InjectView(R.id.loading_progress)
    CircularProgressBar loading_progress;
    @InjectView(R.id.simplenavimap)
    AMapNaviView mAmapAMapNaviView;

    //起点终点列表
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();

    //是否为模拟导航
	private boolean mIsEmulatorNavi = false;
	//记录有哪个页面跳转而来，处理返回键

    private NaviLatLng mNaviStart;
    private NaviLatLng mNaviEnd;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation_detail);
        ButterKnife.inject(this);

        initView();
        initData();
		init(savedInstanceState);
	
	}

    private void initView() {
        loading_progress.setVisibility(View.VISIBLE);
        mAmapAMapNaviView.setVisibility(View.INVISIBLE);
    }

    private void initData() {
        Bundle bundle = getIntent().getBundleExtra("navi_bundle");
        QHNavigation navi_detail = bundle.getParcelable("navi_detail");

        double mLatitude = LocationUtil.getInstance(this).getLatitude();
        double mLongitude = LocationUtil.getInstance(this).getLongitude();

        mNaviStart = new NaviLatLng(mLatitude, mLongitude);
        mNaviEnd = new NaviLatLng(navi_detail.latitude, navi_detail.longitude);

        mStartPoints.add(mNaviStart);
        mEndPoints.add(mNaviEnd);

        AMapNavi.getInstance(this).calculateDriveRoute(mStartPoints,
                mEndPoints, null, AMapNavi.DrivingDefault);
	}

	/**
	 * 初始化
	 * 
	 * @param savedInstanceState
	 */
	private void init(Bundle savedInstanceState) {
        mAmapAMapNaviView.onCreate(savedInstanceState);
		mAmapAMapNaviView.setAMapNaviViewListener(this);
		if (mIsEmulatorNavi) {
			// 设置模拟速度
			AMapNavi.getInstance(this).setEmulatorNaviSpeed(100);
			// 开启模拟导航
			AMapNavi.getInstance(this).startNavi(AMapNavi.EmulatorNaviMode);
		} else {
			// 开启实时导航
            AMapNavi.getInstance(this).setAMapNaviListener(new MyAMapNaviListener());
			AMapNavi.getInstance(this).startNavi(AMapNavi.GPSNaviMode);

		}
	}

    class MyAMapNaviListener implements AMapNaviListener {

        @Override
        public void onInitNaviFailure() {

        }

        @Override
        public void onInitNaviSuccess() {

        }

        @Override
        public void onStartNavi(int i) {

        }

        @Override
        public void onTrafficStatusUpdate() {

        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
            float speed = aMapNaviLocation.getSpeed();
        }

        @Override
        public void onGetNavigationText(int i, String s) {

        }

        @Override
        public void onEndEmulatorNavi() {

        }

        @Override
        public void onArriveDestination() {

        }

        @Override
        public void onCalculateRouteSuccess() {
            loading_progress.setVisibility(View.INVISIBLE);
            mAmapAMapNaviView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCalculateRouteFailure(int i) {

        }

        @Override
        public void onReCalculateRouteForYaw() {

        }

        @Override
        public void onReCalculateRouteForTrafficJam() {

        }

        @Override
        public void onArrivedWayPoint(int i) {

        }

        @Override
        public void onGpsOpenStatus(boolean b) {

        }

        @Override
        public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

        }

        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {

        }
    }

//-----------------------------导航界面回调事件------------------------
	/**
	 * 导航界面返回按钮监听
	 * */
	@Override
	public void onNaviCancel() {
		finish();
	}
   
	@Override
	public void onNaviSetting() {

	}

	@Override
	public void onNaviMapMode(int arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onNaviTurnClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNextRoadClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanViewButtonClick() {
		// TODO Auto-generated method stub

	}

    @Override
    public void onLockMap(boolean b) {

    }


    // ------------------------------生命周期方法---------------------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mAmapAMapNaviView.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mAmapAMapNaviView.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		mAmapAMapNaviView.onPause();
		AMapNavi.getInstance(this).stopNavi();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAmapAMapNaviView.onDestroy();

		
	}

}
