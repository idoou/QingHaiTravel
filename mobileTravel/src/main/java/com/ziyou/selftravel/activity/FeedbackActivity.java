/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.view.View;

import com.umeng.fb.fragment.FeedbackFragment;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author kuloud
 */
public class FeedbackActivity extends BaseActivity {
    private FeedbackFragment mFeedbackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initActionBar();
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            String conversation_id = getIntent().getStringExtra(FeedbackFragment.BUNDLE_KEY_CONVERSATION_ID);
            mFeedbackFragment = FeedbackFragment.newInstance(conversation_id);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.umeng_fb_container, mFeedbackFragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        mFeedbackFragment.onRefresh();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_feedback);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
    }
}
