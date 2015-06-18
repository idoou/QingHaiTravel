/**
 *
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.ErrorCode;
import com.ziyou.selftravel.app.ServerAPI.User.VerifyCodeType;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.ResponseError;
import com.ziyou.selftravel.model.ActiviteResponse;
import com.ziyou.selftravel.model.QHActiviteResponse;
import com.ziyou.selftravel.model.QHToken;
import com.ziyou.selftravel.model.QHUser;
import com.ziyou.selftravel.model.TokenInfo;
import com.ziyou.selftravel.model.User;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.PreferencesUtils;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.circularprogressbar.CircularProgressBar;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * @author kuloud
 */
public class LoginActivity extends BaseActivity {
    private static final String KEY_PHONE = "key_phone";
    private static final String EXTRA_PHONE = "extra_phone";
    private static final String EXTRA_CODE = "extra_code";

    @InjectView(R.id.action_bar_progress)
    CircularProgressBar progressBar;

    @InjectView(R.id.et_login_phone)
    EditText phoneEditText;

    @InjectView(R.id.tv_login_valid_code)
    TextView sendValidCode;

    @InjectView(R.id.et_login_pwd)
    EditText confirmCodeEditText;

    @InjectView(R.id.tv_login)
    TextView activiteButton;

    private Context mContext;
    private String mPhone;
    private String mConfirmCode;

    private int mCount;
    private Timer mTimer;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what > 0) {
                sendValidCode.setText(getString(R.string.register_btn_count_time, msg.what));
            } else {
                sendValidCode.setEnabled(true);
                sendValidCode.setText(R.string.register_btn_security_code_resend);
                mTimer.cancel();
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_login);
        ButterKnife.inject(activity);
        mPhone = PreferencesUtils.getString(mContext, KEY_PHONE);
        phoneEditText.setText(mPhone);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            phoneEditText.setText(savedInstanceState.getString(EXTRA_PHONE));
            confirmCodeEditText.setText(savedInstanceState.getString(EXTRA_CODE));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_PHONE, phoneEditText.getText().toString());
        outState.putString(EXTRA_CODE, confirmCodeEditText.getText().toString());
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);

        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroy();
    }

    @OnTextChanged(R.id.et_login_phone)
    void onPhoneChanged(CharSequence phoneNum) {
        CharSequence confirmCode = confirmCodeEditText.getText();
        onTextChanged(phoneNum, confirmCode);
    }

    @OnTextChanged(R.id.et_login_pwd)
    void onConfirmCodeChanged(CharSequence confirmCode) {
        CharSequence phone = phoneEditText.getText();
        onTextChanged(phone, confirmCode);
    }

    private void onTextChanged(CharSequence phone, CharSequence confirmCode) {
        if (TextUtils.getTrimmedLength(phone) == 11) {
            if (mCount <= 0) {
                sendValidCode.setEnabled(true);
            }
            if (TextUtils.getTrimmedLength(confirmCode) == 6) {
                activiteButton.setEnabled(true);
            } else {
                activiteButton.setEnabled(false);
            }
        } else {
            sendValidCode.setEnabled(false);
            activiteButton.setEnabled(false);
        }
    }

    @OnClick(R.id.action_bar_left)
    void onBackClick() {
        finish();
    }

    @OnClick(R.id.tv_login_valid_code)
    void sendValidCode() {
        clearErrors();

        boolean cancel = false;
        View focusView = null;

        // Store values at the time of the login attempt.
        mPhone = phoneEditText.getText().toString();

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhone)) {
            phoneEditText.setError(getString(R.string.error_phone_empty));
            focusView = phoneEditText;
            cancel = true;
        } else if (mPhone.length() != 11) {
            phoneEditText.setError(getString(R.string.error_invalid_phone));
            focusView = phoneEditText;
            cancel = true;
        }else if(!ConstTools.checkTelPhone(mPhone)){
            phoneEditText.setError(getString(R.string.error_invalid_phone));
            focusView = phoneEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            sendValidCode.setEnabled(false);
            sendValidCode.setText(R.string.register_btn_security_code_sending);
            progressBar.setVisibility(View.VISIBLE);

            String url = "http://182.92.218.24:9000/api/send_code/";
            ValidCode obj = new ValidCode(mPhone,1);
            Gson gson = new Gson();
            String jsonBody = gson.toJson(obj);

            RequestManager.getInstance().sendGsonRequest(Request.Method.POST, url,
                    jsonBody,
                    QHActiviteResponse.class, new Response.Listener<QHActiviteResponse>() {
                        @Override
                        public void onResponse(QHActiviteResponse response) {
                            progressBar.setVisibility(View.GONE);
                            Log.e("onResponse, response nextSendDelay: " + response.details);
                            int time = 2000;
                            mCount = time / 1000;
                            TimerTask task = new TimerTask() {

                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    msg.what = mCount--;
                                    mHandler.sendMessage(msg);
                                }
                            };
                            if (mTimer != null) {
                                mTimer.purge();
                                mTimer.cancel();
                            }
                            mTimer = new Timer();
                            mTimer.schedule(task, 1000, 1000);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(error, "onErrorResponse");
                            handleResponseError(error);
                            sendValidCode.setText(R.string.register_btn_security_code_resend);
                            sendValidCode.setEnabled(true);
                        }
                    }, requestTag);
//            RequestManager.getInstance().sendGsonRequest(/*ServerAPI.User.buildActiviteUrl()*/url,
//                    QHActiviteResponse.class, new Response.Listener<QHActiviteResponse>() {
//
//                        @Override
//                        public void onResponse(QHActiviteResponse response) {
//                            progressBar.setVisibility(View.GONE);
//                            Log.e("onResponse, response nextSendDelay: " + response.details);
//                            int time = 2000;
//                            mCount = time / 1000;
//                            TimerTask task = new TimerTask() {
//
//                                @Override
//                                public void run() {
//                                    Message msg = new Message();
//                                    msg.what = mCount--;
//                                    mHandler.sendMessage(msg);
//                                }
//                            };
//                            if (mTimer != null) {
//                                mTimer.purge();
//                                mTimer.cancel();
//                            }
//                            mTimer = new Timer();
//                            mTimer.schedule(task, 1000, 1000);
//                        }
//                    }, new Response.ErrorListener() {
//
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            Log.e(error, "onErrorResponse");
//                            handleResponseError(error);
//                            sendValidCode.setText(R.string.register_btn_security_code_resend);
//                            sendValidCode.setEnabled(true);
//                        }
//                    }, false,
//                    ServerAPI.User.buildActiviteParams(mPhone, VerifyCodeType.USER_REGISTER),
//                    requestTag);
        }
    }
    public class ValidCode implements Parcelable {
        public  String phone;
        public  int type;

        ValidCode (String phone,int type){
            this.phone = phone;
            this.type = type;
        }
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    @OnClick(R.id.tv_login)
    void login() {
        clearErrors();

        boolean cancel = false;
        View focusView = null;

        // Store values at the time of the login attempt.
        mPhone = phoneEditText.getText().toString();
        mConfirmCode = confirmCodeEditText.getText().toString();

        // Check for a valid confirm code.
        if (TextUtils.isEmpty(mConfirmCode)) {
            confirmCodeEditText.setError(getString(R.string.error_field_required));
            focusView = confirmCodeEditText;
            cancel = true;
        } else if (mConfirmCode.length() != 6) {
            confirmCodeEditText.setError(getString(R.string.error_invalid_confirm_code));
            focusView = confirmCodeEditText;
            cancel = true;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhone)) {
            phoneEditText.setError(getString(R.string.error_phone_empty));
            focusView = phoneEditText;
            cancel = true;
        } else if (mPhone.length() != 11) {
            phoneEditText.setError(getString(R.string.error_invalid_phone));
            focusView = phoneEditText;
            cancel = true;
        }
        else if(!ConstTools.checkTelPhone(mPhone)){
            phoneEditText.setError(getString(R.string.error_invalid_phone));
            focusView = phoneEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            handleLoginStart();
        }

    }

    private void handleLoginStart(){
        LoginObj obj = new LoginObj(mPhone,mConfirmCode);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(obj);
        String url =  "http://182.92.218.24:9000/api/login/";

        RequestManager.getInstance().sendGsonRequest(Request.Method.POST, url,
                jsonBody,
                QHToken.class, new Response.Listener<QHToken>() {
                    @Override
                    public void onResponse(QHToken response) {
                        Log.e("onResponse, response: " + response);
                        progressBar.setVisibility(View.GONE);
//                        AppUtils.saveTokenInfo(mContext, response);
                        PreferencesUtils.putString(mContext, KEY_PHONE, mPhone);
                        AppUtils.saveQHToken(mContext,response);
                        getUserInfo();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                        progressBar.setVisibility(View.GONE);
                        handleResponseError(error);

                    }
                }, requestTag);
    }
    public class LoginObj implements Parcelable {
        public  String phone;
        public  String activation_code;

        LoginObj (String p,String code){
            phone = p;
            activation_code = code;
        }
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    private void handleLoginStart11() {
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://182.92.218.24:9000/api/user/login/";
        url =  ServerAPI.User.test("http://182.92.218.24:9000/api/user/login/");
        RequestManager.getInstance().sendGsonRequest(url/*ServerAPI.User.buildLoginUrl()*/,
                QHToken.class,
                new Response.Listener<QHToken>() {

                    @Override
                    public void onResponse(QHToken response) {
                        Log.e("onResponse, response: " + response);
                        progressBar.setVisibility(View.GONE);
//                        AppUtils.saveTokenInfo(mContext, response);
                        PreferencesUtils.putString(mContext, KEY_PHONE, mPhone);
                        getUserInfo();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                        progressBar.setVisibility(View.GONE);
                        handleResponseError(error);
                    }
                }, false, ServerAPI.User.buildLoginParams(mContext, mPhone, mConfirmCode), requestTag);

    }

    private void handleResponseError(VolleyError error) {
        progressBar.setVisibility(View.GONE);
        if (error instanceof ResponseError) {
            ResponseError response = (ResponseError) error;
            switch (response.errCode) {
                case ErrorCode.ERROR_INVALID_PHONE:
                    phoneEditText.setError(getString(R.string.error_invalid_phone));
                    phoneEditText.requestFocus();
                    break;
                case ErrorCode.ERROR_INVALID_CODE:
                    confirmCodeEditText.setError(getString(R.string.error_invalid_confirm_code));
                    confirmCodeEditText.requestFocus();
                    break;
                case ErrorCode.ERROR_CODE_EXPIRE:
                    confirmCodeEditText.setError(getString(R.string.error_security_code_expire));
                    confirmCodeEditText.requestFocus();
                    break;
                default:
                    ToastUtils.show(mContext, R.string.error_unknown);
                    break;
            }
        } else {
            ToastUtils.show(mContext, R.string.error_unknown);
        }
    }

    private void clearErrors() {
        phoneEditText.setError(null);
        confirmCodeEditText.setError(null);
    }

    private void getUserInfo() {
        String url = "http://182.92.218.24:9000/api/users/current/";
        RequestManager.getInstance().sendGsonRequest(url, QHUser.class,
                new Response.Listener<QHUser>() {

                    @Override
                    public void onResponse(QHUser user) {
                        Log.d("onResponse, User: " + user);
                        if(null==user.user_info.avatar_url)
                            user.user_info.avatar_url ="";
                        AppUtils.saveUser(mContext, user);
                        setResult(RESULT_OK);
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                        handleResponseError(error);
                    }
                }, requestTag);
    }
}
