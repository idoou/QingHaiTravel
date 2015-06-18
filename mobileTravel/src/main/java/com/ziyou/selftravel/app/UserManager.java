/**
 * 
 */

package com.ziyou.selftravel.app;

import android.app.Activity;
import android.content.Intent;

import com.ziyou.selftravel.activity.LoginActivity;
import com.ziyou.selftravel.util.AppUtils;

/**
 * handle operations of user.
 * 
 * @author kuloud
 */
public final class UserManager {
    /**
     * Handle with local action with user
     * 
     * @param activity
     * @return
     */
    public static boolean makeSureLogin(Activity activity, int requestCode) {
        if (AppUtils.getQHUser(activity) != null) {
            return false;
        } else {
            Intent login = new Intent(activity, LoginActivity.class);
            activity.startActivityForResult(login, requestCode);
            return true;
        }
    }

}
