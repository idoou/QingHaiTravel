
package com.ziyou.selftravel.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.UIStartUpHelper;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.Splash;
import com.ziyou.selftravel.service.LocationService;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.FileUtils;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.util.TimeUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Splash page.
 * 
 * @author kuloud
 */
public class SplashActivity extends BaseActivity {
    /**
     * Duration for show Splash
     */
    private final long DURATION = 2000;

    private final int CODE_SHOW_INTRO = 1;

    private final String KEY_SPLASHS = "key_splashs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        setupBackground();

        // Start reqeust current location
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

        UIStartUpHelper.executeOnSplashScreen();

        if (AppUtils.firstLaunch(activity)) {
            startActivityForResult(new Intent(activity, IntroActivity.class), CODE_SHOW_INTRO);
        } else {
            delayedInto();
        }

    }

    private void setupBackground() {
        // load local splash
        ArrayList<Splash> splashs = loadSplashs();
        if (splashs != null) {
            List<Splash> refreshData = (List<Splash>) splashs.clone();
            long now = System.currentTimeMillis();
            for (Splash splash : splashs) {
                long endTime = TimeUtils.parseTimeToMills(splash.endTime);
                // Remove outdated data
                if (endTime < now) {
                    if (refreshData != null && refreshData.contains(splash)) {
                        FileUtils.cleanCacheBitmap(activity, splash.endTime);
                        refreshData.remove(splash);
                    }
                    continue;
                }
                // Load hit data
                long startTime = TimeUtils.parseTimeToMills(splash.startTime);
                if (startTime < now) {
                    ImageView bg = (ImageView) findViewById(R.id.root);
                    String url = FileUtils.getCachePath(activity, splash.endTime);
                    Bitmap bm = FileUtils.getLocalBitmap(url);
                    if (bm != null) {
                        bg.setImageBitmap(bm);
                    }
                    break;
                }
            }
            if (!refreshData.isEmpty()) {
                PreferencesUtils.putString(activity, KEY_SPLASHS, new Gson().toJson(refreshData));
            }
        }

        RequestManager.getInstance().sendGsonRequest(ServerAPI.Splash.buildUrl("beijing"),
                Splash.class,
                new Response.Listener<Splash>() {
                    @Override
                    public void onResponse(Splash splash) {
                        Log.d("onResponse, Splash=%s", splash);
                        onSplashLoaded(splash);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                    }
                }, "splash");
    }

    private ArrayList<Splash> loadSplashs() {
        // read local
        String splashsJson = PreferencesUtils.getString(activity, KEY_SPLASHS);
        if (!TextUtils.isEmpty(splashsJson)) {
            Type type = new TypeToken<ArrayList<Splash>>() {
            }.getType();
            ArrayList<Splash> splashs = null;
            try {
                splashs = new Gson().fromJson(splashsJson, type);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            return splashs;
        }
        return null;
    }

    protected void onSplashLoaded(final Splash splash) {
        ArrayList<Splash> tempSplashs = loadSplashs();
        if (tempSplashs == null) {
            tempSplashs = new ArrayList<Splash>();
        }
        // Get new data, update local records.
        if (tempSplashs != null && !tempSplashs.contains(splash)) {
            final ArrayList<Splash> splashs = tempSplashs;
            splashs.add(splash);
            int width = ScreenUtils.getScreenWidth(getBaseContext());
            int height = ScreenUtils.getScreenHeight(getBaseContext());
            RequestManager.getInstance().requestImage(splash.imageUrl, new Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    FileUtils.cacheBitmap(getBaseContext(), bitmap, splash.endTime);
                    PreferencesUtils.putString(activity, KEY_SPLASHS, new Gson().toJson(splashs));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(error, "onErrorResponse");
                }
            }, width, height, "splash");
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (CODE_SHOW_INTRO == arg0) {
            AppUtils.setFirstLaunch(activity);
            delayedInto();
        }
    }

    private void delayedInto() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, IndexActivity.class);
                startActivity(intent);
                finish();
            }
        }, DURATION);
    }
}
