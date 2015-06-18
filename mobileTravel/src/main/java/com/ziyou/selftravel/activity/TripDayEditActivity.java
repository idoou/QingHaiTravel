/**
 *
 */

package com.ziyou.selftravel.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemClickListener;
import com.qiniu.auth.Authorizer;
import com.qiniu.io.IO;
import com.qiniu.rs.CallBack;
import com.qiniu.rs.CallRet;
import com.qiniu.rs.PutExtra;
import com.qiniu.rs.UploadCallRet;
import com.qiniu.rs.UploadTaskExecutor;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.adapter.TripDayDropdownAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ServerAPI.Upload.Bucket;
import com.ziyou.selftravel.app.ServerAPI.Upload.Token.UploadParams;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.ImageLoaderManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.fragment.DatePickFragment;
import com.ziyou.selftravel.fragment.DatePickFragment.DateSetListener;
import com.ziyou.selftravel.model.CompoundImageEx;
import com.ziyou.selftravel.model.EmptyResponse;
import com.ziyou.selftravel.model.Image;
import com.ziyou.selftravel.model.ItineraryScenic;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.Trip;
import com.ziyou.selftravel.model.Trip.TripList;
import com.ziyou.selftravel.model.TripDay;
import com.ziyou.selftravel.model.TripDay.TripDayList;
import com.ziyou.selftravel.model.UploadToken;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.FileUtils;
import com.ziyou.selftravel.util.ImageHelper;
import com.ziyou.selftravel.util.LocationUtils;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.util.TimeUtils;
import com.ziyou.selftravel.util.ToastUtils;
import com.ziyou.selftravel.widget.CommonDialog;
import com.ziyou.selftravel.widget.CommonDialog.OnDialogViewClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author kuloud
 */
public class TripDayEditActivity extends BaseActivity implements OnClickListener,
        DataLoaderCallback<TripList> {
    private final String TAG = "TripDayEditActivity";

    private final int REQUEST_CODE_TAKE_PHOTO = 1;
    private final int REQUEST_CODE_PICK_SCENIC = 2;
    private final int REQUEST_CODE_PICK_PHOTO = 3;
    private final int REQUEST_CODE_ADD_TRIP = 4;

    private final int MSG_TAKE_PHOTO = 1;

    private final String EXTRA_POST = "extra_post";

    private Context mContext;
    private String mDateFormat;
    private int mTripId;
    private int mTripDayIndex;
    private TripDayDropdownAdapter mAdapter;

    private Request<UploadToken> mGetTokenRequest;
    private UploadTaskExecutor mImageUploadTask;
    private Request<EmptyResponse> mPostRequest;
    private Image mPendingUploadImage;
    private boolean mImageAdded;

    @InjectView(R.id.root_container)
    ViewGroup mRootContainer;
    private PopupWindow mDropdownWindow;
    private PopupWindow mPickPhotoWindow;
    private CommonDialog mInProgressDialog;
    @InjectView(R.id.action_bar_title)
    CheckedTextView mTitle;
    @InjectView(R.id.action_bar_right_text)
    TextView mFinish;
    private Trip mNewTrip;
    private TripDay mTripDay;
    @InjectView(R.id.time)
    TextView mTime;
    @InjectView(R.id.content)
    EditText mContent;
    @InjectView(R.id.content_length)
    TextView mContentLength;
    @InjectView(R.id.image)
    ImageView mImage;
    @InjectView(R.id.scenic_name)
    TextView mScenicName;
    private MapView mMapView;
    private Marker mLocationMarker;
    private Uri mTakePhotoUri;

    private boolean mTripDayPostSuccess;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TAKE_PHOTO:
                    if (mTakePhotoUri != null) {
                        ImageHelper.compressImageFile(mTakePhotoUri.getPath());
                        Image takenPhoto = new Image();
                        takenPhoto.imagePath = mTakePhotoUri;
                        mPendingUploadImage = takenPhoto;
                        mImageAdded = true;
                        ImageLoaderManager.getInstance().getLoader()
                                .displayImage(takenPhoto.imagePath.toString(), mImage);
                        MediaScannerConnection.scanFile(mContext, new String[]{
                                FileUtils.getExternalImageDir()
                        }, null, null);
                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mTripDay.content = s.toString();
            if (mTripDay.image == null && TextUtils.isEmpty(mTripDay.content)) {
                mFinish.setEnabled(false);
            } else {
                mFinish.setEnabled(true);
            }
            mContentLength.setText(s.length() == 0 ? "" : s.length() + "/500");
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_trip_day_edit);
        ButterKnife.inject(this);
        initViews();
        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(activity);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(activity);
        MobclickAgent.onPageEnd(TAG);
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void finish() {

        mHandler.removeCallbacksAndMessages(null);

        if (mTripDayPostSuccess) {
            Intent data = getIntent();
            mNewTrip.coverImage = mTripDay.image.largeImage;
            data.putExtra(Const.EXTRA_ID, mNewTrip.id);
            data.putExtra(Const.EXTRA_TRIP_DAY_DATA, mNewTrip);
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED);
        }

        super.finish();
    }

    private void initViews() {
        initActionBar();
        ViewGroup mapContainer = (ViewGroup) findViewById(R.id.mapview);

        mMapView = new MapView(this);
        mapContainer.addView(mMapView);
        mMapView.getMap().getUiSettings().setZoomControlsEnabled(false);
        mScenicName.bringToFront();

        mContent.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(500)
        });
        mContent.addTextChangedListener(mTextWatcher);

        mInProgressDialog = new CommonDialog(this);
        mInProgressDialog.getLeftBtn().setVisibility(View.GONE);
        mInProgressDialog.setTitleText(R.string.dialog_title_publishing_trips);
        mInProgressDialog.setContentText(R.string.dialog_content_publishing_trips);
        mInProgressDialog.getRightBtn().setText(R.string.dialog_cancel_sending);
        mInProgressDialog.getDialog().setCancelable(false);
        mInProgressDialog.getDialog().setCanceledOnTouchOutside(false);
        mInProgressDialog.setOnDialogViewClickListener(new OnDialogViewClickListener() {

            @Override
            public void onRightButtonClick() {
                cancelRequests();
            }

            @Override
            public void onLeftButtonClick() {
            }
        });
    }

    private void initData() {
        mTripId = getIntent().getIntExtra(Const.EXTRA_ID, -1);
        mTripDay = getIntent().getParcelableExtra(Const.EXTRA_TRIP_DAY_DATA);
        if (mTripDay == null) {
            Log.e("[initData] mTripDay NULL");
            mTripDay = new TripDay();
            mTripDay.time = TimeUtils.formatTime(Calendar.getInstance().getTimeInMillis(),
                    TimeUtils.DATE_TIME_FORMAT);
        }
        mTripDayIndex = getIntent().getIntExtra(Const.EXTRA_INDEX, 1);
        mDateFormat = String.format(Locale.CHINESE, getString(R.string.trip_format_date),
                mTripDayIndex);
        String date = TimeUtils.formatTime(mTripDay.time, mDateFormat);
        mTime.setText(date);
        if (mTripDay.location != null && mTripDay.location.isValid()) {
            updateLocation(mTripDay.location);
            mScenicName.setText(mTripDay.scenicName);
        } else {
            updateLocation(LocationUtils.getLastKnownLocation(activity));
            mScenicName.setText(R.string.trip_unknown_scenic);
        }
        if (mTripDay.image == null) {
            // takePhoto();
        } else {
            if (!TextUtils.isEmpty(mTripDay.image.smallImage)) {
                RequestManager
                        .getInstance()
                        .getImageLoader()
                        .get(mTripDay.image.smallImage,
                                ImageLoader.getImageListener(mImage,
                                        R.drawable.comment_edit_add_selecter,
                                        R.drawable.comment_edit_add_selecter));
            }
        }

        mAdapter = new TripDayDropdownAdapter();
        UrlListLoader<TripList> loader = new UrlListLoader<TripList>(
                requestTag, TripList.class);
        loader.setUrl(ServerAPI.User.URL_USER_TRIPS);
        // TODO
        loader.setPageLimit(100);
        loader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }

    private void initActionBar() {
        RelativeLayout actionBar = (RelativeLayout) findViewById(R.id.action_bar);
        ImageView left = (ImageView) actionBar.findViewById(R.id.action_bar_left);
        left.setImageResource(R.drawable.ic_action_bar_back_selecter);
        mTitle.setText(R.string.action_bar_title_trip_day_edit);
        mFinish.setText(R.string.finish);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO:
                if (resultCode == RESULT_OK && (data != null)) {
                    Message msg = mHandler.obtainMessage(MSG_TAKE_PHOTO);
                    msg.setData(data.getExtras());
                    msg.obj = data.getData();
                    msg.sendToTarget();
                }
                break;
            case REQUEST_CODE_PICK_SCENIC:
                if (resultCode == RESULT_OK) {
                    List<ItineraryScenic> scenics = data
                            .getParcelableArrayListExtra(Const.EXTRA_SCENIC_DATA);
                    if (scenics != null && !scenics.isEmpty()) {
                        ItineraryScenic scenic = scenics.get(0);
                        mScenicName.setText(scenic.scenicName);
                        updateLocation(scenic.location);
                    } else {
                        Log.e("Invalid Scenic " + scenics);
                    }
                }
                break;
            case REQUEST_CODE_PICK_PHOTO: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<Image> images = data
                            .getParcelableArrayListExtra(Const.EXTRA_IMAGE_DATA);
                    if (images != null && !images.isEmpty()) {
                        mPendingUploadImage = images.get(0);
                        mImageAdded = true;
                        if (images.get(0).bitmap != null) {
                            mImage.setImageBitmap(images.get(0).bitmap);
                        } else {
                            String path = images.get(0).imagePath.toString();
                            ImageLoaderManager.getInstance().getLoader().displayImage(path, mImage);
                        }
                    }
                }
                break;
            }
            case REQUEST_CODE_ADD_TRIP: {
                if (resultCode == RESULT_OK && data != null) {
                    setIntent(data);
                    mNewTrip = data.getParcelableExtra(Const.EXTRA_TRIP_DATA);
                    if (mNewTrip != null) {
                        mTripId = mNewTrip.id;
                        mTitle.setText(mNewTrip.title);
                        String date = TimeUtils.formatTime(mNewTrip.createTime, mDateFormat);
                        mTime.setText(date);
                        if (data.getBooleanExtra(EXTRA_POST, false)) {
                            mInProgressDialog.showDialog();
                            if (mPendingUploadImage == null) {
                                postTripDay();
                            } else {
                                getUploadToken();
                            }
                        } else {
                            // TODO add trip data
                            List<Trip> dataItems = new ArrayList<Trip>();
                            dataItems.add(mNewTrip);
                            mAdapter.instertDataItemsAhead(dataItems);
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    private String getCapturePath(Uri uri) {
        String[] columns = {
                MediaStore.Images.Media.DATA, MediaStore.MediaColumns.DATE_ADDED
        };

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(uri, columns, null, null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC");

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);

        cursor.close();

        return path;
    }

    @Override
    public void onLoadFinished(TripList tripList, int mode) {

        List<Trip> list = null;
        if (tripList != null) {
            list = tripList.list;
        }
        if (list != null && !list.isEmpty()) {
            mAdapter.setDataItems(list);
        }
    }

    @Override
    public void onLoadError(int mode) {
        // TODO
    }

    @OnClick(R.id.action_bar_left)
    void back() {
        finish();
    }

    @OnClick(R.id.action_bar_title)
    void toggleDays() {
        mTitle.setCheckMarkDrawable(R.drawable.ic_action_bar_arrow_collapse);
        popupDays();
    }

    @OnClick(R.id.action_bar_right_text)
    void sendTripDay() {
        if (mContent.getText().toString().trim().length() == 0) {
            Toast.makeText(activity, R.string.trip_content_edit_input_hint,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ScreenUtils.dissmissKeyboard(mContext, mContent);
        if (!mImageAdded) {
            ToastUtils.show(mContext, R.string.trip_day_post_failed_no_photo);
            return;
        }
        if (mTripId < 0) {
            // Trip not created, jump to add trip
            Intent intent = new Intent(activity, TripAddActivity.class);
            if (mTripDayIndex == 1 && mTripDay != null) {
                intent.putExtra(Const.EXTRA_DATE, mTripDay.time);
            }
            intent.putExtra(EXTRA_POST, true);
            startActivityForResult(intent, REQUEST_CODE_ADD_TRIP);
        } else {
            mInProgressDialog.showDialog();
            if (mPendingUploadImage == null) {
                postTripDay();
            } else {
                getUploadToken();
            }
        }
    }

    @OnClick(R.id.time)
    void pickDate() {
        ScreenUtils.dissmissKeyboard(mContext, mTime);
        String format = mTime.getText().toString();
        long milliseconds = TimeUtils.parseTime(format, mDateFormat).toMillis(true);
        DatePickFragment datePickDialog = DatePickFragment.newInstance(milliseconds);
        // Avoid dialog show twice.
        Fragment fragment = getFragmentManager().findFragmentByTag(
                DatePickFragment.TAG_DIALOG_PICK_DATE);
        if (fragment != null && fragment.isAdded()) {
            return;
        }
        datePickDialog.show(getFragmentManager(), DatePickFragment.TAG_DIALOG_PICK_DATE);
        datePickDialog.setDateSetListener(new DateSetListener() {

            @Override
            public void onDateSet(Calendar calendar) {
                String date = TimeUtils.formatTime(calendar.getTimeInMillis(), mDateFormat);
                mTime.setText(date);
                if (mTripDay != null) {
                    mTripDay.time = TimeUtils.formatTime(calendar.getTimeInMillis(),
                            TimeUtils.DATE_TIME_FORMAT);
                }
            }
        });
    }

    @OnClick(R.id.image)
    void popupPickPhotoMenu() {
        View menu = View.inflate(mContext, R.layout.menu_popup_select_image, null);
        if (mPendingUploadImage != null) {
            menu = View.inflate(mContext, R.layout.menu_popup_select_image2, null);
            menu.findViewById(R.id.item_popupwindow_preview).setOnClickListener(this);
        }
        menu.findViewById(R.id.item_popupwindow_camera).setOnClickListener(this);
        menu.findViewById(R.id.item_popupwindow_photo).setOnClickListener(this);
        menu.findViewById(R.id.item_popupwindow_cancel).setOnClickListener(this);
        if (mPickPhotoWindow == null) {
            mPickPhotoWindow = new PopupWindow(menu, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }
        mPickPhotoWindow.setContentView(menu);
        mPickPhotoWindow.showAtLocation(mRootContainer, Gravity.BOTTOM, 0, 0);
    }

    @OnClick(R.id.add_scenic_info)
    void pickScenic() {
        Intent pickScenic = new Intent(activity, SpotPickActivity.class);
        pickScenic.putExtra(Const.ARGS_THRESHOLD_COUNT, 1);
        startActivityForResult(pickScenic, REQUEST_CODE_PICK_SCENIC);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.item_popupwindow_preview:
                mPickPhotoWindow.dismiss();
                ArrayList<Image> imageList = new ArrayList<Image>();
                if (mPendingUploadImage != null) {
                    Image image = new Image();
                    image.imagePath = mPendingUploadImage.imagePath;
                    image.thumbnailPath = mPendingUploadImage.thumbnailPath;
                    imageList.add(image);
                }
                Intent preview = new Intent(TripDayEditActivity.this,
                        PhotoPreviewActivity.class);
                preview.putExtra(Const.EXTRA_IMAGE_DATA, imageList);
                preview.putExtra(Const.EXTRA_IMAGE_PREVIEW_START_INDEX, 0);
                startActivity(preview);
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

    private void popupDays() {
        if (mDropdownWindow == null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(this);
            View contentView = mLayoutInflater.inflate(R.layout.layout_dropdown_list,
                    mRootContainer, false);
            RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerview);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            ItemClickSupport clickSupport = ItemClickSupport.addTo(recyclerView);
            clickSupport.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(RecyclerView parent, View view, int position, long id) {
                    mDropdownWindow.dismiss();
                    if (position == 0) {
                        Intent intent = new Intent(activity, TripAddActivity.class);
                        intent.putExtra(EXTRA_POST, false);
                        if (mTripDayIndex == 1 && mTripDay != null) {
                            intent.putExtra(Const.EXTRA_DATE, mTripDay.time);
                        }
                        startActivityForResult(intent, REQUEST_CODE_ADD_TRIP);
                    } else {
                        Trip trip = (Trip) view.getTag();
                        if (trip != null) {
                            mNewTrip = trip;
                            mTitle.setText(trip.title);
                            String date = TimeUtils.formatTime(mNewTrip.createTime, mDateFormat);
                            mTime.setText(date);
                            mTripId = trip.id;
                        }
                    }
                }

            });
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            mDropdownWindow = new PopupWindow(contentView, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            mDropdownWindow.setOutsideTouchable(true);
            mDropdownWindow.setFocusable(true);
            mDropdownWindow
                    .setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
            mDropdownWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    mTitle.setCheckMarkDrawable(R.drawable.ic_action_bar_arrow_expand);
                }
            });
        }

        mDropdownWindow.showAsDropDown(mTitle, 0, ScreenUtils.dpToPxInt(getBaseContext(), 12));
    }

    private void getUploadToken() {
        UploadParams params = new UploadParams();
        params.bucket = Bucket.bucketImage;

        mGetTokenRequest = ServerAPI.Upload.Token.getUploadToken(getApplicationContext(), params,
                requestTag,
                new ServerAPI.Upload.Token.UploadTokenCallback() {

                    @Override
                    public void onTokenError() {
                        tripDayPostError();
                    }

                    @Override
                    public void onGetToken(String token) {
                        mGetTokenRequest = null;
                        uploadImages(token);
                    }
                });
    }

    private void tripDayPostError() {
        mInProgressDialog.dismissDialog();
        ToastUtils.show(mContext, R.string.trip_day_post_failed);
    }

    private void tripDayPostSuccess() {
        mInProgressDialog.dismissDialog();
        mTripDayPostSuccess = true;
        finish();
    }

    private void uploadImages(final String uploadToken) {
        if (mPendingUploadImage == null) {
            postTripDay();
            return;
        }

        Authorizer auth = new Authorizer();
        auth.setUploadToken(uploadToken);

        String key = IO.UNDEFINED_KEY;
        PutExtra extra = new PutExtra();
        extra.params = new HashMap<String, String>();

        final File file = new File(mPendingUploadImage.imagePath.getPath());
        Log.d("Uploading image %s", file);
        if (!file.exists()) {
            Log.d("Image %s does not exists, ignore it", file);
            mPendingUploadImage = null;
            postTripDay();
            return;
        }

        Uri uri = Uri.fromFile(file);
        Bitmap bitmap = null;
        try {
            bitmap = ImageHelper.revitionImageSize(mContext, uri, 800);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (bitmap == null) {
            return;
        }
        mInProgressDialog.setContentText(R.string.dialog_content_publishing_trips_image);
        mImageUploadTask = IO.put(auth, key, ImageHelper.bitmap2InputStreamAt(bitmap), extra,
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
                            tripDayPostError();
                        } else {
                            String key = ret.getKey();
                            String redirect = "http://"
                                    + ServerAPI.Upload.Bucket.bucketImage.value()
                                    + ".qiniudn.com/" + key;
                            CompoundImageEx compoundImage = new CompoundImageEx();
                            compoundImage.largeImage = redirect;
                            compoundImage.smallImage = ServerAPI.Upload
                                    .buildThumbnailPath(redirect);
                            mTripDay.image = compoundImage;
                            postTripDay();
                            Log.d("Image %s uploaded to %s", file, compoundImage);
                        }
                    }

                    @Override
                    public void onFailure(CallRet ret) {
                        tripDayPostError();
                    }
                });
    }

    private void postTripDay() {
        mInProgressDialog.setContentText(R.string.dialog_content_publishing_trips);

        Gson gson = new Gson();
        TripDayList list = new TripDayList();
        list.days = new ArrayList<TripDay>();
        list.days.add(mTripDay);
        list.tripId = mTripId;
        String jsonBody = gson.toJson(list);
        Log.d("posting TripDay, TripDayList=%s", jsonBody);

        mPostRequest = RequestManager.getInstance().sendGsonRequest(Method.POST,
                ServerAPI.Trips.TRIPS_ADD_DAY_URL,
                jsonBody,
                EmptyResponse.class, new Response.Listener<EmptyResponse>() {
                    @Override
                    public void onResponse(EmptyResponse result) {
                        Log.d("New trip day posted");
                        tripDayPostSuccess();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error post new trip day, error=%s", error);
                        tripDayPostError();
                    }
                }, requestTag);
    }

    private void cancelRequests() {
        if (mGetTokenRequest != null && !mGetTokenRequest.isCanceled()) {
            mGetTokenRequest.cancel();
        }

        if (mImageUploadTask != null) {
            mImageUploadTask.cancel();
            mImageUploadTask = null;
        }

        if (mPostRequest != null && !mPostRequest.isCanceled()) {
            mPostRequest.cancel();
        }

        if (mInProgressDialog != null && mInProgressDialog.getDialog().isShowing()) {
            mInProgressDialog.dismissDialog();
        }
    }

    private void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakePhotoUri = ImageHelper.getOutputImageUri();
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
        startActivityForResult(openCameraIntent, REQUEST_CODE_TAKE_PHOTO);
    }

    private void updateLocation(Location loc) {
        if (mLocationMarker == null) {
            mLocationMarker = mMapView.getMap().addMarker(new MarkerOptions()
                    .icon((BitmapDescriptorFactory.fromResource(R.drawable.ic_tracks_start))));
        }
        mLocationMarker.setPosition(loc.toLatLng());
        mMapView.getMap().animateCamera(CameraUpdateFactory.changeLatLng(loc
                .toLatLng()));
    }

}
