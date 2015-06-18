package com.ziyou.selftravel.activity;

import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.fragment.GuiderMessageListFragment;


public class GuiderMessageListActivity extends BaseActivity {
    private GuiderMessageListFragment detailFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guider_list);
        detailFragment = new GuiderMessageListFragment();
        /*Bundle b = new Bundle();
        Location location = getIntent().getParcelableExtra(Const.EXTRA_COORDINATES);
        b.putString(Const.CITYNAME_EN,getIntent().getStringExtra(Const.CITYNAME_EN));
        b.putString(Const.CITYNAME,getIntent().getStringExtra(Const.CITYNAME));
        specialFragment.setArguments(b);*/
        if(null!=findViewById(R.id.fragment_container)){
            getFragmentManager().beginTransaction().add(R.id.fragment_container,detailFragment).commit();
        }
    }

}
