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
public class AboutActivity extends BaseActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_about);
        initActionBar();
        if (Const.DEBUG) {
            ((TextView) findViewById(R.id.version_name)).setText(getString(R.string.about_version_name,
                    AppUtils.getVersionName(activity)));
            findViewById(R.id.riv_logo).setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    boolean debug = !PreferencesUtils.getBoolean(mContext, ServerAPI.KEY_DEBUG);
                    PreferencesUtils.putBoolean(mContext, ServerAPI.KEY_DEBUG, debug);
                    ServerAPI.switchServer(debug);
                    ToastUtils.show(mContext, debug ? "切换为测试服务器" : "切换为正式服务器");
                    return false;
                }
            });
        }

    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_about);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
    }
}
