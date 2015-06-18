
package com.ziyou.selftravel.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.ShareManager;
import com.ziyou.selftravel.fragment.DiscoveryFragment;
import com.ziyou.selftravel.fragment.HomeFragment;
import com.ziyou.selftravel.fragment.LiveFragment;
import com.ziyou.selftravel.fragment.MarketFragment;
import com.ziyou.selftravel.fragment.MeFragment;
import com.ziyou.selftravel.fragment.ScenicDetailsFragment;
import com.ziyou.selftravel.media.PlaybackService;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.service.LocationService;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.LocationUtil;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.BottomTab;
import com.ziyou.selftravel.widget.BottomTab.OnTabSelected;

public class IndexActivity extends BaseActivity implements OnClickListener {
    /**
     * Position of tab.
     */
    private static final int POS_SCENIC = 0;
    private static final int POS_LIVE = POS_SCENIC + 1;
    private static final int POS_DISCOVERY = POS_LIVE + 1;
    private static final int POS_ME = POS_DISCOVERY + 1;

    private static final int REQ_SELECT_LOCATION = 1;

    private BottomTab mBottomTab;

    private long mBackPressedTime;

    private Fragment mScenicFragment = null;
    private Fragment mLiveFragment = null;
    private Fragment mDiscoveryFragment = null;
    private Fragment mMeFragment = null;
    private Fragment mCurrentFragment = null;

    private View mLocationSelectorView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        LocationUtil.getInstance(this);

        mBottomTab = (BottomTab) findViewById(R.id.bottom_tab);
        mBottomTab.setOnTabSelected(new OnTabSelected() {

            //点击时触发的点击事件，通过回调接口形式提供给IndexActivity
            @Override
            public void onTabSeledted(int index) {
                Fragment fragment = null;
                switch (index) {
                    case POS_SCENIC:
                        if (mScenicFragment == null) {
                            mScenicFragment = new HomeFragment();
                        }
                        fragment = mScenicFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB,MobConst.VALUE_INDEX_TAB_SCENIC);
                        break;
                    case POS_LIVE:
                        if (mLiveFragment == null) {
                            mLiveFragment = new LiveFragment();
                        }
                        fragment = mLiveFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB, MobConst.VALUE_INDEX_TAB_LIVE);
                        break;
                    case POS_DISCOVERY:
                        if (mDiscoveryFragment == null) {
                            mDiscoveryFragment = new MarketFragment();
                        }
                        fragment = mDiscoveryFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB, MobConst.VALUE_INDEX_TAB_DISCOVERY);
                        break;
                    case POS_ME:
                        if (mMeFragment == null) {
                            mMeFragment = new MeFragment();
                        }
                        fragment = mMeFragment;
                        MobclickAgent.onEvent(getBaseContext(), MobConst.ID_INDEX_TAB,MobConst.VALUE_INDEX_TAB_ME);
                        break;

                    default:
                        Log.e("unknown position of SectionsPagerAdapter: " + index);
                        if (mScenicFragment == null) {
                            mScenicFragment = new HomeFragment /*ScenicDetailsFragment*/();
                        }
                        fragment = mScenicFragment;
                        break;
                }

                if (!fragment.isAdded()) {
                    Bundle args = fragment.getArguments();
                    if (args == null) {
                        args = new Bundle();
                    }
                    args.putString(Const.ARGS_REQUEST_TAG, requestTag);
                    fragment.setArguments(args);
                }

                if (activity != null && !activity.isFinishing()) {
                    //当前fragment为空
                    if (mCurrentFragment == null) {
                        getFragmentManager().beginTransaction()
                                .add(R.id.container, fragment).commit();
                    } else if (fragment.isAdded()) {
                        //当前fragment不为空，但是要显示的fragment已经添加过了，隐藏当前currentFragment，直接显示fragment
                        // TODO: mCurrentFragment is still attached to view
                        // hierarchy
                        getFragmentManager().beginTransaction().hide(mCurrentFragment)
                                .show(fragment)
                                .commit();
                    } else {
                        //fragment还没有添加过，隐藏当前currentFragment，显示新的fragment
                        getFragmentManager().beginTransaction().hide(mCurrentFragment)
                                .add(R.id.container, fragment).commit();
                    }
                } else {
                    Log.e("Activity is leaving");
                }
                mCurrentFragment = fragment;
            }
        });

        mLocationSelectorView = findViewById(R.id.scenic_select_location);
        mLocationSelectorView.setOnClickListener(this);

        UmengUpdateAgent.update(this);
        // Remove this according new requirement
//        if (!PreferencesUtils.getBoolean(getApplicationContext(),
//                PreferencesUtils.KEY_INDEX_FIRST_START)) {
//            PreferencesUtils.putBoolean(getApplicationContext(),
//                    PreferencesUtils.KEY_INDEX_FIRST_START, true);
//            Intent intent = new Intent(activity, ScenicDetailMaskActivity.class);
//            startActivity(intent);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在每个activity中都绑定
        // 用来保证获取正确的新增用户、活跃用户、启动次数、使用时长等基本数据
        MobclickAgent.onResume(activity);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(activity);
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if(mCurrentFragment instanceof MarketFragment){
            ((MarketFragment) mCurrentFragment).getWebView().goBack();
        }

        if (!ShareManager.getInstance().hideBorad()) {
            if ((System.currentTimeMillis() - mBackPressedTime) > 2000) {
                mBackPressedTime = System.currentTimeMillis();
                ToastUtils.show(activity, R.string.press_back_to_exit);
            } else {
                super.onBackPressed();
                cleanUp();
                // Exit application if back pressed.
                System.exit(0);
            }
        }
    }

    private void cleanUp() {
        stopService(new Intent(this, PlaybackService.class));
        stopService(new Intent(this, LocationService.class));
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.scenic_select_location: {
                Intent intent = new Intent(this, CityChooseActivity.class);
                startActivityForResult(intent, REQ_SELECT_LOCATION);
                break;
            }

            default:
                break;
        }
    }

    public void showLocationSelector() {
        mLocationSelectorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQ_SELECT_LOCATION && data != null) {
            Location location = (Location) data.getParcelableExtra(Const.EXTRA_COORDINATES);
            if (location != null && location.isValid()) {
                mLocationSelectorView.setVisibility(View.GONE);
                Intent intent = new Intent(LocationService.ACTION_UPDATE_LOCATION);
                intent.putExtra(Const.EXTRA_COORDINATES, location);
                startService(intent);
                Log.d("Mannally set city to %s", location);
            } else {
                Log.e("Invalid location %s", location);
            }
        } else if (mCurrentFragment != null) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationUtil.getInstance(AppUtils.getContext()).destroyAMapLocationListener();
    }
}
