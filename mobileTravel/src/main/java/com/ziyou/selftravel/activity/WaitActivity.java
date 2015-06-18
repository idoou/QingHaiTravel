/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author kuloud
 */
public class WaitActivity extends BaseActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_wait);
        initActionBar();

    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.map_dialog_secnic_wait);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
    }
}
