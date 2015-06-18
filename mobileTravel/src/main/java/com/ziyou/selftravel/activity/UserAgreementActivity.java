/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author Kuloud
 */
public class UserAgreementActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        ((TextView) findViewById(R.id.tv_content)).setMovementMethod(new ScrollingMovementMethod());
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_agreement);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
    }
}
