package com.ziyou.selftravel.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ziyou.selftravel.util.PreferencesUtils;

/**
 * Created by Administrator on 2015/5/29.
 */
public class QHToken {
    private static final String KEY_TOKEN_INFO = "key_token_info";

    public String token;

    public static QHToken getTokenInfo(Context context) {
        String json = PreferencesUtils.getString(context, KEY_TOKEN_INFO);
        QHToken info = null;
        try {
            info = new Gson().fromJson(json, QHToken.class);
        } catch (JsonSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return info;
    }
}
