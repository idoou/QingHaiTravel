
package com.ziyou.selftravel.activity;

import android.app.Fragment;
import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ShareManager;
import com.ziyou.selftravel.fragment.ScenicDetailsFragment;
import com.ziyou.selftravel.util.LocationUtils;
import com.ziyou.selftravel.util.Log;

/**
 * @author kuloud
 */
public class ScenicDetailActivity extends BaseActivity {
    private int mId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getIntExtra(Const.EXTRA_ID, -1);
        if (mId < 0) {
            Log.e("Invalid scenic id %s", mId);
            finish();
            return;
        }
        setContentView(R.layout.activity_fragment);
        Fragment fragment = new ScenicDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(Const.EXTRA_ID, mId);
        args.putParcelable(Const.EXTRA_COORDINATES, LocationUtils.getLastKnownLocation(this));
        args.putString(Const.ARGS_REQUEST_TAG, requestTag);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (!ShareManager.getInstance().hideBorad()) {
            super.onBackPressed();
        }
    }
}
