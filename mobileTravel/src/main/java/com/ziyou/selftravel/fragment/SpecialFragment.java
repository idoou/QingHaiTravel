
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lidroid.xutils.view.annotation.ViewInject;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.PublishActivity;
import com.ziyou.selftravel.activity.TripDayEditActivity;
import com.ziyou.selftravel.activity.VideoUploadActivity;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.UserManager;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.widget.ActionBar;

import butterknife.ButterKnife;

public class SpecialFragment extends BaseFragment implements OnClickListener {
    private final String TAG = "LiveFragment";

    private View mLoadingProgress;
    private final int REQUEST_CODE_TAKE_VIDEO = 1;
    private Location myLocation;
    private EditText et_search_content;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_special, container, false);
        initActionBar(rootView);
        mLoadingProgress = rootView.findViewById(R.id.live_loading_progress);
        et_search_content = (EditText) rootView.findViewById(R.id.fragment_search_et);
        mLoadingProgress.setVisibility(View.VISIBLE);
        showVideoList();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        et_search_content.setOnKeyListener(new View.OnKeyListener() {//输入完后按键盘上的搜索键【回车键改为了搜索键】

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_ENTER){//修改回车键功能
                    // 先隐藏键盘
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(
                                    getActivity().getCurrentFocus()
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    UploadRecyclerView();

                }
                return false;
            }
        });
    }

    private void UploadRecyclerView() {
        String condition = et_search_content.getText().toString();
        SpecialListFragment guideListFragment = (SpecialListFragment) getFragmentManager().findFragmentByTag("GuideListFragment");
        String url = ServerAPI.Guide.buildSearchCommentUrl(condition);
        guideListFragment.loadList(url);
        guideListFragment.reload();
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

    String city;
    String city_en;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       city  = getArguments().getString(Const.CITYNAME_EN);
       city_en  = getArguments().getString(Const.CITYNAME_EN);
//        myLocation = getArguments().getParcelable(Const.EXTRA_COORDINATES);
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
        SpecialListFragment liveFragment = new SpecialListFragment();
        liveFragment.attachLoadingProgress(mLoadingProgress);
        getFragmentManager().beginTransaction().add(R.id.live_container, liveFragment,"GuideListFragment").commitAllowingStateLoss();
        liveFragment.initLoacation(city,city_en);
    }

    private void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.special_title);
        actionBar.getTitleView().setTextColor(Color.WHITE);
        actionBar.setBackgroundResource(R.color.title_bg);
        actionBar.getLeftView() .setImageResource(R.drawable.return_back);
        actionBar.getLeftView().setOnClickListener(this);
        actionBar.getRightView().setImageResource(R.drawable.edit);
        actionBar.getRightView().setOnClickListener(this);
    }
    private final int REQUEST_CODE_ADD_TRIP_DAY_LOGIN = 2;
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
            case R.id.action_bar_right:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_DISCOVERY_ADD);
                if (!UserManager.makeSureLogin(getActivity(), REQUEST_CODE_ADD_TRIP_DAY_LOGIN)) {
                    Intent addTripDay = new Intent(getActivity(), TripDayEditActivity.class);
                    startActivity(addTripDay);
                }

                break;
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
