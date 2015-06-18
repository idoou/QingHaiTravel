
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.IndexActivity;
import com.ziyou.selftravel.activity.SearchActivity;
import com.ziyou.selftravel.activity.VideoUploadActivity;
import com.ziyou.selftravel.adapter.LiveVideoListAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.UserManager;
import com.ziyou.selftravel.data.LocationDetector;
import com.ziyou.selftravel.data.LocationDetector.LocationListener;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.NetUtils;
import com.ziyou.selftravel.widget.ActionBar;

public class LiveFragment extends BaseFragment implements OnClickListener {
    private final String TAG = "LiveFragment";

    private View mLoadingProgress;
    private final int REQUEST_CODE_TAKE_VIDEO = 1;
    private LocationDetector mLocationDetector;
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onReceivedLocation(Location loc) {
            Log.d("onReceivedLocation, location=%s", loc);
            // Can't show video list when leaving
            if (isAdded()) {
                showVideoList(loc);
            } else {
                Log.e("fragmeng is detaching");
            }
        }

        @Override
        public void onLocationTimeout() {
            Log.d("onLocationTimeout");
            if (isAdded()) {
                if(null!=ConstTools.myCurrentLoacation){
                    showVideoList(ConstTools.myCurrentLoacation);
                }else {
                    mLoadingProgress.setVisibility(View.GONE);
                    ((IndexActivity) getActivity()).showLocationSelector();
                }

            } else {
                Log.e("fragmeng is detaching");
            }
        }

        @Override
        public void onLocationError() {
            Log.d("onLocationError");
            // TODO
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_live, container, false);
        initActionBar(rootView);
        mLoadingProgress = rootView.findViewById(R.id.live_loading_progress);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mLocationDetector = new LocationDetector(getActivity().getApplicationContext());
        boolean useCachedLocation = !NetUtils.isNetworkAvailable(getActivity());
        mLocationDetector.detectLocation(mLocationListener, useCachedLocation, true);
        // showVideoList(LocationUtils.getLastKnownLocation(getActivity()));
        return rootView;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_TAKE_VIDEO:
                    startUploadActivity();
                    break;

                default:
                    break;
            }
        }
    }

    private void showVideoList(Location location) {
        LiveVideoListFragment liveFragment = LiveVideoListFragment.newInstance(LinearLayoutManager.class,LiveVideoListAdapter.class);
        liveFragment.attachLoadingProgress(mLoadingProgress);
        Bundle args = new Bundle();
        args.putString(Const.ARGS_LOADER_URL,"http://182.92.218.24:9000/api/videos/?format=json");
        liveFragment.setArguments(args);
        getFragmentManager().beginTransaction().add(R.id.live_container, liveFragment).commitAllowingStateLoss();
    }

    private void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_live);
        actionBar.setBackgroundResource(R.color.title_bg);
        // actionBar.getRight2View().setImageResource(R.drawable.ic_action_bar_search_selecter);
        // actionBar.getRight2View().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_right: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_LIVE_SEARCH);
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra(Const.EXTRA_SEARCH_TYPE, SearchActivity.SEARCH_VIDEO);
                getActivity().startActivity(intent);
                break;
            }

            case R.id.action_bar_right2: {
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_LIVE_VIDEO);
                if (!UserManager.makeSureLogin(getActivity(), REQUEST_CODE_TAKE_VIDEO)) {
                    startUploadActivity();
                }
                break;
            }
        }
    }

    private void startUploadActivity() {
        Intent intent = new Intent(getActivity(), VideoUploadActivity.class);
        // FIXME
        intent.putExtra(Const.EXTRA_ID, 6);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        if (mLocationDetector != null) {
            mLocationDetector.close();
        }

        super.onDestroy();
    }

}
