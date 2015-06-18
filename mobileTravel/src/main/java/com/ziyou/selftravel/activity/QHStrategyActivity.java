package com.ziyou.selftravel.activity;

import android.content.Context;
import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.fragment.SpecialFragment;
import com.ziyou.selftravel.model.Location;

/**
 * Created by Edward on 2015/5/17.
 */
public class QHStrategyActivity extends BaseActivity {
    private Context mContext;
    private /*RestaurantListFragment*/ SpecialFragment specialFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_special);
        specialFragment = new SpecialFragment();

        Bundle b = new Bundle();
        Location location = getIntent().getParcelableExtra(Const.EXTRA_COORDINATES);
        b.putString(Const.CITYNAME_EN,getIntent().getStringExtra(Const.CITYNAME_EN));
        b.putString(Const.CITYNAME,getIntent().getStringExtra(Const.CITYNAME));
        specialFragment.setArguments(b);

        if(null!=findViewById(R.id.fragment_container)){
            getFragmentManager().beginTransaction().add(R.id.fragment_container,specialFragment).commit();
        }
    }
}
