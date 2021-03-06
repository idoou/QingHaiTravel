/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;

/**
 * @author kuloud
 */
public class ScenicDetailMaskActivity extends BaseActivity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scenic_details_mask);
        ButterKnife.inject(activity);
    }

    @OnClick(R.id.index_mask_root)
    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        finish();
    }

}
