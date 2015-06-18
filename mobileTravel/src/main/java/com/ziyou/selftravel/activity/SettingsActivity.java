/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.CommonDialog;
import com.ziyou.selftravel.widget.CommonDialog.OnDialogViewClickListener;

/**
 * @author kuloud
 */
public class SettingsActivity extends BaseActivity implements OnClickListener {
    private final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initActionBar();
        initListItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(activity);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(activity);
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
    }

    private void initListItems() {
        setItem(R.id.item_qrcode, R.string.settings_item_qrcode);
        setItem(R.id.item_about, R.string.settings_item_about);
        View logoutItem = findViewById(R.id.item_logout);
        if (AppUtils.getUser(activity) != null) {
            logoutItem.setVisibility(View.VISIBLE);
            logoutItem.setOnClickListener(this);
        } else {
            logoutItem.setVisibility(View.GONE);
        }
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_settings);
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ExcessiveClickBlocker.isExcessiveClick()) {
                    return;
                }
                finish();
            }
        });
    }

    private void setItem(int id, int textId) {
        View item = findViewById(id);
        item.setOnClickListener(this);
        TextView text = (TextView) item.findViewById(R.id.item_text);
        text.setText(textId);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        Intent intent = null;
        switch (v.getId()) {
            case R.id.item_qrcode:
                MobclickAgent.onEvent(activity, MobConst.ID_SETTINGS_OFFICIAL);
                intent = new Intent(activity, OfficialActivity.class);
                break;
            case R.id.item_about:
                MobclickAgent.onEvent(activity, MobConst.ID_SETTINGS_ABOUT);
                intent = new Intent(activity, AboutActivity.class);
                break;
            case R.id.item_logout:
                showLogoutDialog();
                break;

            default:
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void showLogoutDialog() {
        CommonDialog logoutDialog = new CommonDialog(SettingsActivity.this);
        logoutDialog.setTitleText(R.string.settings_item_logout);
        logoutDialog.getDialog().setCancelable(true);
        logoutDialog.getDialog().setCanceledOnTouchOutside(true);
        logoutDialog.setContentText(R.string.settings_dialog_content_logout);
        logoutDialog.setOnDialogViewClickListener(new OnDialogViewClickListener() {

            @Override
            public void onRightButtonClick() {
            }

            @Override
            public void onLeftButtonClick() {
                MobclickAgent.onEvent(activity, MobConst.ID_SETTINGS_LOGOUT);
                logout();
            }
        });
        logoutDialog.showDialog();
    }

    private void logout() {
        AppUtils.clearUser(activity);
        AppUtils.clearToken(activity);
        setResult(RESULT_OK);
        findViewById(R.id.item_logout).setVisibility(View.GONE);
    }
}
