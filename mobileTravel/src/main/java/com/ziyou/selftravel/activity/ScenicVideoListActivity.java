
package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.VideoListAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.ScenicVideos.Type;
import com.ziyou.selftravel.fragment.VideoListFragment;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.Video;
import com.ziyou.selftravel.util.CommonUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.model.QHScenic.QHVideo;

public class ScenicVideoListActivity extends BaseActivity implements OnClickListener {
    private int mId = -1;
    private Type mType;
    private VideoListFragment mFragment;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mId = getIntent().getIntExtra(Const.EXTRA_ID, -1);
        mLocation = (Location) getIntent().getParcelableExtra(Const.EXTRA_COORDINATES);
        mType = (Type) getIntent().getSerializableExtra(Const.EXTRA_VIDEO_TYPE);
        if (mType == null) {
            Log.e("Invalid scenic mType %s", mType);
            finish();
            return;
        }
        setContentView(R.layout.activity_scenic_spot_list);
        initViews();

        mFragment = VideoListFragment.newInstance(GridLayoutManager.class,
                VideoListAdapter.class);
        Bundle args = new Bundle();
        args.putString(Const.ARGS_LOADER_URL, ServerAPI.ScenicVideos
                .buildUrl(mId, mType, mLocation));
        args.putInt(Const.ARGS_SCENIC_ID, mId);
        mFragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .add(R.id.container, mFragment).commit();
    }

    private void initViews() {
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.scenic_video_title);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ExcessiveClickBlocker.isExcessiveClick()) {
                    return;
                }
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        QHVideo data = (QHVideo) v.getTag();
        CommonUtils.playVideo(this, data);
    }
}
