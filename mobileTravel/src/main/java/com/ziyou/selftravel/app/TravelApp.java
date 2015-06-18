
package com.ziyou.selftravel.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.data.ImageLoaderManager;
import com.ziyou.selftravel.data.OfflinePackageManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.media.PlaybackService;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.QHUser;
import com.ziyou.selftravel.model.User;
import com.ziyou.selftravel.model.Version;
import com.ziyou.selftravel.service.LocationService;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.LocationUtils;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.widget.CommonDialog;
import com.ziyou.selftravel.widget.CommonDialog.OnDialogViewClickListener;

/**
 * Created by kuloud on 14-8-16.
 */
public class TravelApp extends Application {
    public static final String TAG = "Travel";
    public static Context mContext;

    //记录当前所在位置
    private Location mCurrentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Intent service = new Intent(getApplicationContext(),LocationService.class);
        startService(service);

        init();
    }
    public static Context getContext(){
        return mContext;
    }

    private void init() {
        ServerAPI.switchServer(PreferencesUtils.getBoolean(this, ServerAPI.KEY_DEBUG));
        RequestManager.getInstance().init(this);
        ImageLoaderManager.getInstance().init(this);
        OfflinePackageManager.getInstance().init(this);

        // TODO open Umeng log, remove it when release.
        com.umeng.socialize.utils.Log.LOG = Const.UMENG_DEBUG;
        MobclickAgent.setDebugMode(Const.UMENG_DEBUG);

        UIStartUpHelper.executeWhenIdle(new Runnable() {

            @Override
            public void run() {
                tryUpdateUserInfoFromNet();
            }
        });
    }

    private void tryUpdateUserInfoFromNet() {
        // if user info exist local, try to update once.
        if (AppUtils.getUser(getBaseContext()) != null) {
            String url = "http://182.92.218.24:9000/api/users/current/";
            RequestManager.getInstance().sendGsonRequest(url, QHUser.class,
                    new Response.Listener<QHUser>() {

                        @Override
                        public void onResponse(QHUser user) {
                            Log.e("onResponse, User: " + user);
                            // Delete previous user avatar if need.
                            QHUser oldUserInfo = AppUtils.getQHUser(getBaseContext());

                            if (oldUserInfo != null&&null!=user.user_info.avatar_url
                                    && !oldUserInfo.user_info.avatar_url.equals(user.user_info.avatar_url)) {
                                AppUtils.setOldAvatarUrl(getBaseContext(), oldUserInfo.user_info.avatar_url);
                            }
                            AppUtils.saveUser(getBaseContext(), user);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(error, "onErrorResponse");
                        }
                    }, TAG);
        }
    }

    private void checkVersion() {
        RequestManager.getInstance().sendGsonRequest(
                ServerAPI.Version.buildUrl(getCurrentVersion()),
                Version.class,
                new Response.Listener<Version>() {

                    @Override
                    public void onResponse(Version version) {
                        Log.d("onResponse, Version: " + version);
                        try {
                            if (version != null
                                    && Integer.parseInt(version.latest) > getCurrentVersion()) {
                                showUpdateDialog(version.message);
                            }
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse when checking new version");
                    }
                }, TAG);
    }

    private int getCurrentVersion() {
        int versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

    private void showUpdateDialog(String contentText) {
        CommonDialog downloadDialog = new CommonDialog(getApplicationContext());
        downloadDialog.setTitleText(R.string.software_update);
        downloadDialog.getDialog().setCancelable(true);
        downloadDialog.getDialog().setCanceledOnTouchOutside(true);
        downloadDialog.setContentText(contentText);
        downloadDialog.setOnDialogViewClickListener(new OnDialogViewClickListener() {

            @Override
            public void onRightButtonClick() {
            }

            @Override
            public void onLeftButtonClick() {

            }
        });
        downloadDialog.showDialog();
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public void setCurrentLocation(Location location) {
        mCurrentLocation = location;

        if (location != null && location.isValid()) {
            LocationUtils.setLastKnownLocation(getApplicationContext(), location);
        }
    }

    @Override
    public void onTerminate() {
        OfflinePackageManager.getInstance().destroy();
        super.onTerminate();
    }
}
