package com.ziyou.selftravel.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.TripDetailEditActivity;
import com.ziyou.selftravel.adapter.BannerPagerAdapter;
import com.ziyou.selftravel.adapter.DiscoveryAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.model.Trip;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Edward on 2015/5/16.
 */
public class MarketFragment extends BaseFragment {

    @InjectView(R.id.webview_market)
    WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);
        initActionBar(view);
        ButterKnife.inject(this,view);
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //webview嵌入的h5接口
        webView.loadUrl("http://www.baidu.com");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
    }

    public WebView getWebView(){
        return webView;
    }

    private void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_market);
        actionBar.setBackgroundResource(R.color.title_bg);
        // actionBar.getRight2View().setImageResource(R.drawable.ic_action_bar_search_selecter);
        // actionBar.getRight2View().setOnClickListener(this);
    }

}
