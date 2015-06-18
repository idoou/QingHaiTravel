/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.fragment.HotelFragment;
import com.ziyou.selftravel.fragment.NavigationFragment;

/**
 * @author kuloud
 */
public class QHNavigationActivity extends BaseActivity {
    private Context mContext;
    private NavigationFragment navigationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_navigation);
        navigationFragment = new NavigationFragment();

        if(null!=findViewById(R.id.fragment_container)){
            getFragmentManager().beginTransaction().add(R.id.fragment_container,navigationFragment).commit();
        }
    }
}
