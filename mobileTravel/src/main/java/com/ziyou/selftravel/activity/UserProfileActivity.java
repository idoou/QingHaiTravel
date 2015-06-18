/**
 * 
 */

package com.ziyou.selftravel.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.qiniu.auth.Authorizer;
import com.qiniu.io.IO;
import com.qiniu.rs.CallBack;
import com.qiniu.rs.CallRet;
import com.qiniu.rs.PutExtra;
import com.qiniu.rs.UploadCallRet;
import com.qiniu.rs.UploadTaskExecutor;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.Upload.Bucket;
import com.ziyou.selftravel.app.ServerAPI.Upload.Token.UploadParams;
import com.ziyou.selftravel.data.ImageLoaderManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.StringConverter;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.EmptyResponse;
import com.ziyou.selftravel.model.Image;
import com.ziyou.selftravel.model.QHUser;
import com.ziyou.selftravel.model.UploadToken;
import com.ziyou.selftravel.model.User;
import com.ziyou.selftravel.upyun.UploadTask;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.FileUtils;
import com.ziyou.selftravel.util.ImageHelper;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.ActionBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kuloud
 */
public class UserProfileActivity extends BaseActivity implements OnClickListener ,UploadTask.UploadCallBack{
    private final int REQUEST_CODE_NAME = 1;
    private final int REQUEST_CODE_TAKE_PHOTO = 2;
    private final int REQUEST_CODE_PICK_PHOTO = 3;
    private final int REQUEST_CODE_CUT_PHOTO = 4;

    // Cache avata in temp dir, when avata updated, make it effect avata
    public static final String PATH_NAME_AVATAR_TEMP = "avatar_temp";

    private Context mContext;
    private QHUser mUser;
    // Store the previous avatar url.
    private String mPreviousAvatarUrl;

    private ViewGroup mRootContainer;
    private PopupWindow mPickPhotoWindow;
    private NetworkImageView mAvatar;
    private TextView mName;
    private TextView mPhone;

    private UploadTaskExecutor mImageUploadTask;
    private Request<UploadToken> mGetTokenRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_user_profile);
        mRootContainer = (ViewGroup) findViewById(R.id.root_container);
        initActionBar();
        initListItems();
        mUser = getIntent().getParcelableExtra("user");
        if (mUser == null) {
            finish();
        } else {
            bindUserInfo(mUser);
        }
    }

    private void bindUserInfo(QHUser user) {
        boolean localLoaded = false;
        if (FileUtils.fileCached(activity, user.user_info.avatar_url)) {
            String url = FileUtils.getCachePath(activity, user.user_info.avatar_url);
            Bitmap bm = FileUtils.getLocalBitmap(url);
            if (bm != null) {
                mAvatar.setImageBitmap(bm);
                localLoaded = true;
            }
        }

        if (!localLoaded) {
            mAvatar.setErrorImageResId(R.drawable.bg_avata_hint)
                    .setDefaultImageResId(R.drawable.bg_avata_hint)
                    .setImageUrl(user.user_info.avatar_url, RequestManager.getInstance()
                            .getImageLoader());
        }

        mName.setText(user.user_info.nickname);
        mPhone.setText(user.username);
    }

    private void initActionBar() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_user_profile);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_NAME:
                    String name = data.getStringExtra(TextFieldEditActivity.KEY_TEXT);
                    /*Map<String, String> params = new HashMap<String, String>();
                    String name = data.getStringExtra(TextFieldEditActivity.KEY_TEXT);
                    params.put(ServerAPI.User.PARAM_NAME, name);
                    mUser.user_info.nickname = name;
                    updateUserInfo(params);*/

                    List<File> imgFiles = new ArrayList<File>();
                    QHUser usr = AppUtils.getQHUser(getApplicationContext());
                    String url = ServerAPI.User.buildUpdateUserInfoUrl(usr.id);
                    new UploadTask(getApplicationContext(), url,1,imgFiles , name, 1 ,this).execute("", "");
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            onAvataUrlLoaded(uri.getPath());
                        } else if (data.getExtras() != null) {
                            Bitmap bm = (Bitmap) data.getExtras().get("data");
                            startPhotoZoom(bm, 200);
                        }
                        MediaScannerConnection.scanFile(mContext, new String[] {
                            FileUtils.getExternalImageDir()
                        }, null, null);
                    }
                    break;
                case REQUEST_CODE_PICK_PHOTO: {
                    if (resultCode == RESULT_OK && data != null) {
                        ArrayList<Image> images = data
                                .getParcelableArrayListExtra(Const.EXTRA_IMAGE_DATA);
                        if (images != null && !images.isEmpty()) {
                            if (images.get(0).bitmap != null) {
                                startPhotoZoom(images.get(0).bitmap, 200);
                            } else {
                                String path = images.get(0).imagePath.toString();
                                onAvataUrlLoaded(path);
                            }
                        }
                    }
                    break;
                }
                case REQUEST_CODE_CUT_PHOTO: {
                    if(data != null && data.getExtras() != null) {
                        Bitmap photo = data.getExtras().getParcelable("data");
                        onAvataLoaded(photo);
                    }
                    break;
                }

                default:
                    break;
            }
        }
    }

    private void onAvataUrlLoaded(String url) {
        ImageLoaderManager.getInstance().getLoader()
                .loadImage(url, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                            FailReason failReason) {
                        onUploadError();
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                            Bitmap loadedImage) {
                        startPhotoZoom(loadedImage, 150);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        onUploadError();
                    }
                });
    }

    private void onAvataLoaded(Bitmap bp) {
        try {
            if (bp != null) {
                FileUtils.cacheBitmap(mContext, bp, PATH_NAME_AVATAR_TEMP);
                List<File> imgFiles = new ArrayList<File>();
                File file = ConstTools.saveBitmap(bp,"user_icon",getApplicationContext());

               /* String path = bp.getp();
                ImageHelper.compressImageFile(path);
                File file = new File(path);*/
                imgFiles.add(file);

                QHUser usr = AppUtils.getQHUser(getApplicationContext());
                String url = ServerAPI.User.buildUpdateUserInfoUrl(usr.id);
                new UploadTask(getApplicationContext(), url,1,imgFiles  , usr.user_info.nickname, 1 ,this).execute("", "");

//             getUploadToken();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onUploadError();
        }
    }

    private void updateUserInfo(final Map<String, String> params) {
        RequestManager.getInstance().sendGsonRequest(Method.PUT, ServerAPI.User.buildSelfUrl(),
                EmptyResponse.class,
                null, new Response.Listener<EmptyResponse>() {
                    @Override
                    public void onResponse(EmptyResponse response) {
                        Log.d("onResponse");
                        String url = FileUtils.getCachePath(activity, PATH_NAME_AVATAR_TEMP);
                        Bitmap bm = FileUtils.getLocalBitmap(url);
                        FileUtils.cacheBitmap(mContext, bm, mUser.user_info.avatar_url);
                        // Delete temp avatar
                        FileUtils.delFile(url);
                        // Delete previous avatar
                        if (!TextUtils.isEmpty(mPreviousAvatarUrl)) {
                            FileUtils.delFile(FileUtils.getCachePath(activity, mPreviousAvatarUrl));
                        }
                        AppUtils.saveUser(activity, mUser);
                        bindUserInfo(mUser);
                        if (params.containsKey(ServerAPI.User.PARAM_NAME)) {
                            ToastUtils.show(mContext, R.string.msg_user_name_change_success);
                        } else if (params.containsKey(ServerAPI.User.PARAM_AVATAR_URL)) {
                            ToastUtils.show(mContext, R.string.msg_user_avata_change_success);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "onErrorResponse");
                        if (params.containsKey(ServerAPI.User.PARAM_NAME)) {
                            ToastUtils.show(mContext, R.string.msg_user_name_change_failed);
                        } else if (params.containsKey(ServerAPI.User.PARAM_AVATAR_URL)) {
                            ToastUtils.show(mContext, R.string.msg_user_avata_change_failed);
                        }
                    }
                }, true, params, "user_self");
    }

    private void initListItems() {
        setItem(R.id.item_avatar, R.string.me_item_avatar, true);
        setItem(R.id.item_name, R.string.me_item_name, true);
        setItem(R.id.item_phone, R.string.me_item_phone, false);
        mAvatar = (NetworkImageView) findViewById(R.id.item_avatar).findViewById(R.id.item_image);
        mAvatar.setVisibility(View.VISIBLE);
        mName = (TextView) findViewById(R.id.item_name).findViewById(R.id.item_summary);
        mPhone = (TextView) findViewById(R.id.item_phone).findViewById(R.id.item_summary);
        mPhone.setCompoundDrawables(null, null, null, null);
        mPhone.setEnabled(false);
    }

    private void setItem(int id, int textId, boolean enable) {
        View item = findViewById(id);
        if (enable)
            item.setOnClickListener(this);
        TextView text = (TextView) item.findViewById(R.id.item_text);
        text.setText(textId);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.item_avatar:
                // pickPhotoFromGallery();
                popupPickPhotoMenu();
                break;
            case R.id.item_name:
                Intent name = new Intent(activity, TextFieldEditActivity.class);
                name.putExtra(TextFieldEditActivity.KEY_TITLE, getString(R.string.me_item_name));
                name.putExtra(TextFieldEditActivity.KEY_TEXT, mName.getText());
                startActivityForResult(name, REQUEST_CODE_NAME);
                break;
            case R.id.item_popupwindow_camera:
                mPickPhotoWindow.dismiss();
                takePhoto();
                break;
            case R.id.item_popupwindow_photo:
                mPickPhotoWindow.dismiss();
                Intent intent = new Intent(activity, PhotoPickActivity.class);
                intent.putExtra(Const.EXTRA_PICK_IMAGE_COUNT, 1);
                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
                break;
            case R.id.item_popupwindow_cancel:
                mPickPhotoWindow.dismiss();
                break;
            default:
                break;
        }
    }

    private void popupPickPhotoMenu() {
        if (mPickPhotoWindow == null) {
            View menu = View.inflate(activity, R.layout.menu_popup_select_image, null);
            menu.findViewById(R.id.item_popupwindow_camera).setOnClickListener(this);
            menu.findViewById(R.id.item_popupwindow_photo).setOnClickListener(this);
            menu.findViewById(R.id.item_popupwindow_cancel).setOnClickListener(this);
            mPickPhotoWindow = new PopupWindow(menu, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mPickPhotoWindow.setContentView(menu);
        }
        mPickPhotoWindow.showAtLocation(mRootContainer, Gravity.BOTTOM, 0, 0);
    }

    private void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCameraIntent, REQUEST_CODE_TAKE_PHOTO);
    }

    private void getUploadToken() {
        UploadParams params = new UploadParams();
        params.bucket = Bucket.bucketImage;

        mGetTokenRequest = ServerAPI.Upload.Token.getUploadToken(getApplicationContext(), params,
                requestTag,
                new ServerAPI.Upload.Token.UploadTokenCallback() {

                    @Override
                    public void onTokenError() {
                        onUploadError();
                    }

                    @Override
                    public void onGetToken(String token) {
                        mGetTokenRequest = null;
                        uploadImages(token);
                    }
                });
    }

    private void uploadImages(final String uploadToken) {
        Authorizer auth = new Authorizer();
        auth.setUploadToken(uploadToken);

        String key = IO.UNDEFINED_KEY;
        PutExtra extra = new PutExtra();
        extra.params = new HashMap<String, String>();

        final String imagePath = FileUtils.getCachePath(activity, PATH_NAME_AVATAR_TEMP);
        Log.d("Uploading image %s", imagePath);
        mImageUploadTask = IO.putFile(getApplicationContext(), auth, key,
                Uri.fromFile(new File(imagePath)), extra,
                new CallBack() {

                    @Override
                    public void onProcess(long current, long total) {
                        int percent = (int) (current * 100 / total);
                        Log.d("Upload percent %d", percent);
                    }

                    @Override
                    public void onSuccess(UploadCallRet ret) {
                        if (ret.getException() != null) {
                            Log.e(ret.getException(), "Upload error");
                            onUploadError();
                        } else {
                            String key = ret.getKey();
                            String redirect = "http://"
                                    + ServerAPI.Upload.Bucket.bucketImage.value()
                                    + ".qiniudn.com/" + key;
                            mPreviousAvatarUrl = mUser.user_info.avatar_url;
                            mUser.user_info.avatar_url = ServerAPI.Upload.buildThumbnailPath(redirect);
                            Log.d("Image %s uploaded to %s", imagePath, redirect);

                            Map<String, String> params = new HashMap<String, String>();
                            params.put(ServerAPI.User.PARAM_AVATAR_URL, mUser.user_info.avatar_url);
                            updateUserInfo(params);
                        }
                    }

                    @Override
                    public void onFailure(CallRet ret) {
                        onUploadError();
                    }
                });
    }

    private void onUploadError() {
        ToastUtils.show(mContext, R.string.error_avata_update_failed);
    }

    private void startPhotoZoom(Bitmap bp, int size) {
        FileUtils.cacheBitmap(mContext, bp, PATH_NAME_AVATAR_TEMP);
        String url = FileUtils.getCachePath(activity, PATH_NAME_AVATAR_TEMP);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
        intent.putExtra("crop", "true");

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, REQUEST_CODE_CUT_PHOTO);
    }

    @Override
    public void onSuccess(String result) {

        Gson mGson = new Gson();
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(String.class, new StringConverter());
        mGson = gb.create();
        QHUser user =  mGson.fromJson(result.toString(), QHUser.class);
        if(user.user_info.avatar_url==null)user.user_info.avatar_url = mUser.user_info.avatar_url;

        mUser.user_info.avatar_url = user.user_info.avatar_url;
        if(user.user_info.nickname!=null) mUser.user_info.nickname = user.user_info.nickname;
        AppUtils.saveUser(activity, mUser);
        bindUserInfo(mUser);

       ToastUtils.show(mContext, "用户信息更新成功");
    }

    @Override
    public void onFailed() {

    }
}
