/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ziyou.selftravel.R;
import com.ziyou.selftravel.support.OnClickListener;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author kuloud
 */
public class TextFieldEditActivity extends BaseActivity {
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_TEXT = "key_text";
    public static final String KEY_CAN_EMPTY = "key_can_empty";

    private Context mContext;
    private TextView mFinishBtn;
    private String mOriginText;
    private String mText;
    private boolean mCanEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_text_field_edit);
        initActionBar();
        mOriginText = getIntent().getStringExtra(KEY_TEXT);
        mCanEmpty = getIntent().getBooleanExtra(KEY_CAN_EMPTY, false);
        final EditText editText = ((EditText) findViewById(R.id.et_text));
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean disable = (mCanEmpty ? false : TextUtils.isEmpty(s))
                        || s.toString().equals(mOriginText);
                mFinishBtn.setEnabled(!disable);
                mText = s.toString().trim();
                if (mText.length() >= 16) {
                    ToastUtils.show(mContext, "用户名最多12个字");
                    editText.setText(mText.substring(0, 12));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setText(mOriginText);
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(getIntent().getStringExtra(KEY_TITLE));
        actionBar.getLeftView().setImageResource(R.drawable.ic_action_bar_back_selecter);
        actionBar.getLeftView().setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                finish();
            }
        });
        actionBar.setRightMode(true);
        mFinishBtn = actionBar.getRightTextView();
        mFinishBtn.setText(R.string.finish);
        mFinishBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onValidClick(View v) {
                commitChange();
            }
        });
    }

    protected void commitChange() {
        Intent result = new Intent();
        result.putExtra(KEY_TEXT, mText);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
