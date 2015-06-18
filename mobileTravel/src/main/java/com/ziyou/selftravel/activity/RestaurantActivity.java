/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.fragment.RestaurantFragment;
import com.ziyou.selftravel.fragment.RestaurantListFragment;
import com.ziyou.selftravel.fragment.ScenicDetailsFragment;
import com.ziyou.selftravel.model.Location;

/**
 * @author kuloud
 */
public class RestaurantActivity extends BaseActivity {
    private Context mContext;
    private RestaurantFragment restaurantFragment = new RestaurantFragment();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_restaurant);
        Intent intent =  getIntent();
        Bundle b = new Bundle();
        b.putString(Const.CITYNAME_EN,getIntent().getStringExtra(Const.CITYNAME_EN));
        b.putString(Const.CITYNAME,getIntent().getStringExtra(Const.CITYNAME));
        b.putString(Const.LAT,getIntent().getStringExtra(Const.LAT));
        b.putString(Const.LON,getIntent().getStringExtra(Const.LON));
        restaurantFragment.setArguments(b);
        if(null!=findViewById(R.id.fragment_container)){
            getFragmentManager().beginTransaction().add(R.id.fragment_container,restaurantFragment).commit();
        }
    }
}
