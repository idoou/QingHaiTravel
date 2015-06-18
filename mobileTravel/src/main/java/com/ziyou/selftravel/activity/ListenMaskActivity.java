/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.view.View;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.support.OnClickListener;

/**
 * @author kuloud
 */
public class ListenMaskActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_listen_mask);

        findViewById(R.id.root_container).setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }

        });
    }
}
