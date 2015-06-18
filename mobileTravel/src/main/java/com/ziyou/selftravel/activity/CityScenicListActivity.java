/**
 *
 */

package com.ziyou.selftravel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.Search.SearchType;
import com.ziyou.selftravel.fragment.BasicScenicDataListFragment;
import com.ziyou.selftravel.fragment.DefaultScenicDataListFragmentImp;
import com.ziyou.selftravel.model.BasicScenicData;
import com.ziyou.selftravel.model.Scenic;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.widget.ActionBar;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CityScenicListActivity extends BaseActivity implements OnClickListener {
    private String mCity;
    private String mCity_zh;
    private BasicScenicDataListFragment mFragment;

    @InjectView(R.id.iv_service)
    ImageView serviceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCity = getIntent().getStringExtra(Const.EXTRA_CITY);
        mCity_zh = getIntent().getStringExtra(Const.CITYNAME_ZH);

        if (TextUtils.isEmpty(mCity)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_scenic_spot_list);
        ButterKnife.inject(this);
        initViews();

        mFragment = new DefaultScenicDataListFragmentImp();
        Bundle args = new Bundle();
        args.putString(Const.ARGS_KEY_MODEL_CLASS, Scenic.ScenicList.class.getName());

        String url;
        if (null==mCity_zh) {
            url = ServerAPI.ScenicList.buildUrl(ServerAPI.ScenicList.Type.NEARBY,  "北京", Const.HTTP_GET_PARAM_LIMIT,0) ;
        } else {
            url = ServerAPI.ScenicList.buildUrl(ServerAPI.ScenicList.Type.NEARBY,  mCity_zh,Const.HTTP_GET_PARAM_LIMIT,0);
        }
        args.putString(Const.ARGS_LOADER_URL,url);
//        args.putString(Const.CITYNAME_ZH,mCity_zh);
        mFragment.setArguments(args);
        mFragment.setOnItemClickListener(this);
        getFragmentManager().beginTransaction()
                .add(R.id.container, mFragment).commit();
    }

    private void initViews() {
        initActionBar();
        serviceBtn.setVisibility(View.GONE);
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.search_result);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ExcessiveClickBlocker.isExcessiveClick()) {
                    return;
                }
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        BasicScenicData data = (BasicScenicData) v.getTag();
        if (data != null) {
            Intent intent = new Intent();
            intent.putExtra(Const.EXTRA_ID, data.id());
            intent.putExtra(Const.EXTRA_NAME, data.name());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
