/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.CommentAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.FootprintDetail;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author Kuloud
 */
public class TrackDetailActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initActionBar();
        initListView();
        getFootprintList();
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(getIntent().getStringExtra(Const.EXTRA_NAME));
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getRightView().setImageResource(R.drawable.ic_action_bar_praise_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
        actionBar.getRightView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                // TODO
            }
        });
    }

    private void initListView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommentAdapter();
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
        clickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
    }

    public void getFootprintList() {
        // TODO
        final String url = ServerAPI.Tracks.buildGetFootprintDetailUrl(1);

        RequestManager.getInstance().sendGsonRequest(url, FootprintDetail.class,
                new Response.Listener<FootprintDetail>() {
                    @Override
                    public void onResponse(FootprintDetail response) {
                        Log.d("onResponse, CommentList=%s", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
                        // TODO handle it
                    }
                }, requestTag);
    }
}
