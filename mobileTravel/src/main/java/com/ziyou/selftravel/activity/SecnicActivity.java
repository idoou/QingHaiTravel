/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.fragment.QHScenicDetailsFragment;
import com.ziyou.selftravel.fragment.ScenicDetailsFragment;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author kuloud
 */
public class SecnicActivity extends BaseActivity {
    private Context mContext;
    private QHScenicDetailsFragment secnicFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_secnic);
        secnicFragment = new QHScenicDetailsFragment();
        if(null!=findViewById(R.id.fragment_container)){
            Intent intent =  getIntent();
            QHScenic mScenic = intent.getParcelableExtra(Const.QH_SECNIC);
            int mId = intent.getIntExtra(Const.QH_SECNIC_ID, 0);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Const.QH_SECNIC,mScenic);
            bundle.putInt(Const.QH_SECNIC_ID,mId);
//            b.putInt(Const.EXTRA_ID, (int) intent.getIntExtra(Const.EXTRA_ID, 1));
//            b.putString(Const.EXTRA_NAME, (String)intent.getStringExtra(Const.EXTRA_NAME));
//            b.putParcelable(Const.EXTRA_COORDINATES, (Location)intent.getParcelableExtra(Const.EXTRA_COORDINATES));
            secnicFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().add(R.id.fragment_container,secnicFragment).commit();
        }
    }
}
