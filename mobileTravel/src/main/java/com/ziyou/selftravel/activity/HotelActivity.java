/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.fragment.HotelFragment;
import com.ziyou.selftravel.fragment.RestaurantFragment;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.ScenicDetails;

/**
 * @author kuloud
 */
public class HotelActivity extends BaseActivity {
    private Context mContext;
    private HotelFragment restaurantFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_hotel);
        restaurantFragment = new HotelFragment();

        Bundle b = new Bundle();
        b.putString(Const.CITYNAME_EN,getIntent().getStringExtra(Const.CITYNAME_EN));
        b.putString(Const.CITYNAME,getIntent().getStringExtra(Const.CITYNAME));
        restaurantFragment.setArguments(b);
        if(null!=findViewById(R.id.fragment_container)){
            getFragmentManager().beginTransaction().add(R.id.fragment_container,restaurantFragment).commit();
        }
    }
}
