/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.view.View;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.fragment.TripListFragment;
import com.ziyou.selftravel.model.Trip.TripList;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author kuloud
 */
public class PublishActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        initActionBar();

        Bundle args = new Bundle();
        TripListFragment myTripsFragment = new TripListFragment();
        args.putString(Const.ARGS_KEY_MODEL_CLASS, TripList.class.getName());
        args.putString(Const.ARGS_LOADER_URL, ServerAPI.User.URL_USER_TRIPS);
        args.putBoolean("editable", true);
        myTripsFragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .add(R.id.container, myTripsFragment).commit();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_trips);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
    }

}
