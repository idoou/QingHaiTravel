package com.ziyou.selftravel.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.WriterException;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ShareManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.HomeRestuarantDetail;
import com.ziyou.selftravel.model.Image;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.widget.ActionBar;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GuiderRecommandActivity extends BaseActivity implements View.OnClickListener {
    private ActionBar mActionBar;
    private String mRequestTag = "GuiderMessageDetailActivity";
    private ImageView qr_img;
    public static final int IMAGE_HALFWIDTH = 40;

    @InjectView(R.id.tv_recommend)
    TextView tv_recommend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend);
        ButterKnife.inject(this);
        ShareManager.getInstance().onStart(this);
        initActionBar();
        initView();
        initData();
    }
    private void initView(){
        qr_img = (ImageView)findViewById(R.id.dimensioncode);
        tv_recommend.setOnClickListener(this);

    }
    private void initData(){
       Bitmap mBitmap = ((BitmapDrawable) getResources().getDrawable( R.drawable.ic_launcher)).getBitmap();
        Matrix m = new Matrix();
        float sx = (float) 2*IMAGE_HALFWIDTH / mBitmap.getWidth();
        float sy = (float) 2*IMAGE_HALFWIDTH / mBitmap.getHeight();
        m.setScale(sx, sy);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),mBitmap.getHeight(), m, false);
        try {
            String s = "https://www.baidu.com";
            qr_img.setImageBitmap(ConstTools.cretaeBitmap(getApplicationContext(),s/*new String(s.getBytes(), "ISO-8859-1")*/,mBitmap));
//            qr_img.setImageBitmap(ConstTools.Create2DCode(getApplicationContext(),"https://www.baidu.com"));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        /* catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/


        /*try{
            Bitmap img = ConstTools.Create2DCode(getApplicationContext(),"https://www.baidu.com");
            qr_img.setImageBitmap(img);
        }catch(Exception e){

        }*/
    }

    private void initActionBar() {
        mActionBar = (ActionBar) findViewById(R.id.action_bar);
        mActionBar.getLeftView().setImageResource(R.drawable.return_back);
        mActionBar.getLeftView().setOnClickListener(GuiderRecommandActivity.this);
        mActionBar.getTitleView().setTextColor(getApplicationContext().getResources().getColor(R.color.white));
        mActionBar.setTitle("推荐给好友");
    }
    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left:
                finish();
                return;
            case R.id.tv_recommend:
                ShareManager.getInstance().shareApp();
                break;
            default:
                break;
        }
    }
    private void upLoadInfo() {
        final String url;
        url = ServerAPI.Home.buildUrl("beijing"/*mLocation.city*/);
        RequestManager.getInstance().sendGsonRequest(Request.Method.GET, url,
                HomeRestuarantDetail.class, null,
                new Response.Listener<HomeRestuarantDetail>() {
                    @Override
                    public void onResponse(HomeRestuarantDetail response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
                    }
                }, true, mRequestTag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareManager.getInstance().onEnd();
    }
}
