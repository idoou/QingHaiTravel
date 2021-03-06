
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.VideoUploadActivity;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.widget.ActionBar;

public class HotelFragment extends BaseFragment implements OnClickListener {
    private final String TAG = "LiveFragment";

    private View mLoadingProgress;
    private final int REQUEST_CODE_TAKE_VIDEO = 1;
    Location myLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hotel, container, false);
        initActionBar(rootView);
        mLoadingProgress = rootView.findViewById(R.id.live_loading_progress);
        mLoadingProgress.setVisibility(View.VISIBLE);
        // showVideoList(LocationUtils.getLastKnownLocation(getActivity()));
        showVideoList();

        return rootView;
    }

    String city;
    String city_en;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        city  = getArguments().getString(Const.CITYNAME);
        city_en  = getArguments().getString(Const.CITYNAME_EN);
//      myLocation = getArguments().getParcelable(Const.EXTRA_COORDINATES);
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

    private void showVideoList(/*Location location*/) {
        HotelListFragment liveFragment = new HotelListFragment();
        liveFragment.attachLoadingProgress(mLoadingProgress);
        liveFragment.setMyLocation(myLocation);
        getFragmentManager().beginTransaction().add(R.id.live_container, liveFragment).commitAllowingStateLoss();
        liveFragment.initLoacation(city,city_en);
    }

    private void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.home_nearby_hotel);
        actionBar.getTitleView().setTextColor(Color.WHITE);
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView() .setImageResource(R.drawable.return_back);
        actionBar.getLeftView().setOnClickListener(this);
        // actionBar.getRight2View().setImageResource(R.drawable.ic_action_bar_search_selecter);
        // actionBar.getRight2View().setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left: {
                getActivity().finish();
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
        super.onDestroy();
    }

}
