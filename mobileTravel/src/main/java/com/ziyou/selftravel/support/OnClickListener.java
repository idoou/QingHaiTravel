/**
 * 
 */

package com.ziyou.selftravel.support;

import android.view.View;

import com.ziyou.selftravel.util.ExcessiveClickBlocker;

/**
 * Samsung devices, click event trigger twice, so override the listener to avoid
 * this case.
 * 
 * @author Kuloud
 */
public abstract class OnClickListener implements android.view.View.OnClickListener {
    public abstract void onValidClick(View v);

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        onValidClick(v);
    }
}
