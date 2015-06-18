package com.ziyou.selftravel.activity;

import android.os.Bundle;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.fragment.SearchListFragment;


/**
 * Created by Edward on 2015/6/7.
 */
public class SearchListActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        SearchListFragment searchListFragment = new SearchListFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,searchListFragment,searchListFragment.getFragmentName()).commit();
    }
}
