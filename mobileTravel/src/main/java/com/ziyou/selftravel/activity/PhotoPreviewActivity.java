
package com.ziyou.selftravel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.Image;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

/**
 * @author kuloud
 */

public class PhotoPreviewActivity extends BaseActivity {
    public static final String TAG_ID = "photo_id";

    private ArrayList<View> mPagerViews = null;
    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private PhotoPageAdapter mPhotoAdapter;

    private ArrayList<Image> mImageData;
    private TextView mPageIndicator;
    private TextView mInfoLocation;
    private TextView mInfoTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageData = getIntent().getParcelableArrayListExtra(Const.EXTRA_IMAGE_DATA);
        if (mImageData == null || mImageData.isEmpty()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_photo_preview);
        initActionBar();

        mInfoLocation = ((TextView) findViewById(R.id.image_info_location));
        mInfoTime = ((TextView) findViewById(R.id.image_info_time));
        mPageIndicator = (TextView) findViewById(R.id.pager_indicator);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(pageChangeListener);
        populateImageViews();

        mPhotoAdapter = new PhotoPageAdapter(mPagerViews);
        mViewPager.setAdapter(mPhotoAdapter);

        Intent intent = getIntent();
        int startIndex = intent.getIntExtra(Const.EXTRA_IMAGE_PREVIEW_START_INDEX, 0);
        if (startIndex >= mImageData.size() || startIndex < 0) {
            Log.d("Invalid startIndex %d against image data list size %d", startIndex,
                    mImageData.size());
            startIndex = 0;
        }
        mViewPager.setCurrentItem(startIndex);

        loadImageAtIndex(startIndex);
    }

    private void initActionBar() {
        mActionBar = (ActionBar) findViewById(R.id.action_bar);
        mActionBar.setBackgroundResource(R.drawable.fg_top_shadow);
        mActionBar.getLeftView()
                .setImageResource(R.drawable.ic_action_bar_back_selecter);
        mActionBar.getLeftView().setOnClickListener(mOnClickListener);
    }

    private void populateImageViews() {
        if (mPagerViews == null) {
            mPagerViews = new ArrayList<View>();
        }

        for (int i = 0; i < mImageData.size(); i++) {
            View itemView = LayoutInflater.from(activity).inflate(R.layout.item_photo_preview,
                    mViewPager, false);
            mPagerViews.add(itemView);
        }
    }

    private void loadImageAtIndex(int index) {
        View itemView = mPagerViews.get(index);
        Image image = mImageData.get(index);
        NetworkImageView imageView = (NetworkImageView) itemView.findViewById(R.id.image);
        if (TextUtils.isEmpty(image.infoLocation)) {
            mInfoLocation.setVisibility(View.GONE);
        } else {
            mInfoLocation.setVisibility(View.VISIBLE);
            mInfoLocation.setText(image.infoLocation);
        }

        if (TextUtils.isEmpty(image.infoTime)) {
            mInfoTime.setVisibility(View.GONE);
        } else {
            mInfoTime.setVisibility(View.VISIBLE);
            mInfoTime.setText(image.infoTime);
        }

        CircularProgressBar progressBar = (CircularProgressBar) itemView
                .findViewById(R.id.loading_progress);
        if (image.bitmap != null) {
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(image.bitmap);
        } else if (image.imagePath != null) {
            // if ("file".equals(image.imagePath.getScheme())) {
            // progressBar.setVisibility(View.GONE);
            // ImageLoaderManager.getInstance().getLoader()
            // .displayImage(image.imagePath.toString(), imageView);
            // } else {
            Log.d("Kuloud", "setErrorImageResId:"+System.currentTimeMillis());
            imageView.setErrorImageResId(R.drawable.bg_image_error).setLoadingView(progressBar);
            imageView.setImageUrl(image.imagePath.toString(), RequestManager.getInstance()
                    .getImageLoader());
            // }
        }

        mPageIndicator.setText(getString(R.string.page_index, index + 1, mImageData.size()));
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            loadImageAtIndex(arg0);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    class PhotoPageAdapter extends PagerAdapter {

        private ArrayList<View> listViews;

        private int size;

        public PhotoPageAdapter(ArrayList<View> listViews) {
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public void setListViews(ArrayList<View> listViews) {
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(listViews.get(arg1 % size));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            try {
                ((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);

            } catch (Exception e) {
            }
            return listViews.get(arg1 % size);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onValidClick(View v) {
            switch (v.getId()) {
                case R.id.action_bar_left:
                    finish();
                    break;

                default:
                    break;
            }
        }
    };
}
