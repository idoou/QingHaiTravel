/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.fragment.GuiderRadiersCommentListFragment;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.util.ConstTools;

/**
 *攻略/路线 评论
 */
public class GuiderRaidersCommentActivity extends BaseActivity {
    private Context mContext;
    private GuiderRadiersCommentListFragment specialFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_special);
        specialFragment = new GuiderRadiersCommentListFragment();
        QHScenic mScenic = getIntent().getParcelableExtra(Const.QH_SECNIC);
        int id = getIntent().getIntExtra(Const.QH_SECNIC_ID,0);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.QH_SECNIC,mScenic);
        bundle.putInt(Const.QH_SECNIC_ID,id);
        specialFragment.setArguments(bundle);
        if(null!=findViewById(R.id.fragment_container)){
            getFragmentManager().beginTransaction().add(R.id.fragment_container,specialFragment).commit();
        }
    }
}
