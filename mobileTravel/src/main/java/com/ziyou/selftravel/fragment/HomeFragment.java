
package com.ziyou.selftravel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemSubViewClickListener;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.CityListActivity;
import com.ziyou.selftravel.activity.HotelActivity;
import com.ziyou.selftravel.activity.IndexActivity;
import com.ziyou.selftravel.activity.QHNavigationActivity;
import com.ziyou.selftravel.activity.RestaurantActivity;
import com.ziyou.selftravel.activity.SearchActivity;
import com.ziyou.selftravel.activity.SearchListActivity;
import com.ziyou.selftravel.activity.SearchScenicActivity;
import com.ziyou.selftravel.activity.SecnicActivity;
import com.ziyou.selftravel.activity.SpecialActivity;
import com.ziyou.selftravel.activity.WaitActivity;
import com.ziyou.selftravel.activity.WebActivity;
import com.ziyou.selftravel.adapter.BannerPagerAdapter;
import com.ziyou.selftravel.adapter.HomeAdapter;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.app.ShareManager;
import com.ziyou.selftravel.data.DataLoader;
import com.ziyou.selftravel.data.DataLoader.DataLoaderCallback;
import com.ziyou.selftravel.data.GsonRequest;
import com.ziyou.selftravel.data.LocationDetector;
import com.ziyou.selftravel.data.LocationDetector.LocationListener;
import com.ziyou.selftravel.data.OfflinePackageManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.data.UrlListLoader;
import com.ziyou.selftravel.model.City;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.HomeBanner;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.QHHomeBanner;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.model.Scenic;
import com.ziyou.selftravel.model.ScenicDetails;
import com.ziyou.selftravel.util.ConstTools;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.FormatUtils;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.ActionBar;
import com.ziyou.selftravel.widget.PullToRefreshRecyclerView;

//import com.ziyou.selftravel.util.Log;

public class HomeFragment extends ServiceBaseFragment implements OnClickListener,    DataLoaderCallback<QHScenic.QHScenicList> {
    private final String TAG = "HomeFragment";

    private final int REQUEST_CODE_LOGIN_NEW_COMMENT = 1;
    private final int REQUEST_CODE_LOGIN_VOTE = 2;
    private final int REQUEST_CODE_LOGIN_COMMENT_DETAIL = 3;
    private final int REQUEST_CODE_SEARCH = 4;
    private final int REQUEST_CODE_COMMENT_DETAIL = 5;
    private final int REQUEST_CODE_POST_COMMENT = 6;
    private final int REQUEST_CODE_SEARCH_ET = 7;//
    private static final int ACCELERATION_THRESOLD = 22;
    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private HomeAdapter mAdapter;
    private int mId = -1;
//    116.397228,39.908815
    private Location mLocation = new Location(39.908815,116.397228);//我当前loc
    private Location secnicLoc = new Location(39.908815,116.397228);//当前景区loc
    private View mLoadingProgress;
    private View mReloadView;
    private PullToRefreshRecyclerView mPullToRefreshView;
    private UrlListLoader<Scenic.ScenicList> mScenicListLoader;
    private UrlListLoader<QHScenic.QHScenicList> mScenicQHListLoader;
//    private Request<HomeBanner.HomeBannerLists> mBannerRequest;
    private GsonRequest<QHHomeBanner.QHHomeBannerLists> mBannerRequest;
    private ViewGroup mRootView;
    private TextView mVoteText;
    private OfflinePackageManager mOfflineManager;
    private Vibrator mVibrator;
    private boolean mRandomLoad;
    private QHScenic mScenic;
    private LocationDetector mLocationDetector;
    private int HTTP_GET_PARAM_LIMIT = 10;
//    private City dectedCity;

    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onReceivedLocation(Location loc) {
           Log.d("onReceivedLocation, location=%s", loc);
            mLocation = loc;
            secnicLoc = loc;
            ConstTools.myCurrentLoacation = new  Location(loc.latitude,loc.longitude);
            ConstTools.myCurrentLoacation.city = loc.city;
            ConstTools.myCurrentLoacation.city_en = loc.city_en;
            Log.e("-------","onReceivedLocation--");
            loadScenicDetails();
        }
        @Override
        public void onLocationTimeout() {
            Log.d("onLocationTimeout");
            ((IndexActivity) getActivity()).showLocationSelector();
            mLocation = null;
            mLocation = new Location(39.908815,116.397228);
            secnicLoc = new Location(39.908815,116.397228);
            ConstTools.myCurrentLoacation = new  Location(39.908815,116.397228);
            ConstTools.myCurrentLoacation.city ="北京";
            ConstTools.myCurrentLoacation.city_en = "beijing";
            loadHomeBanner();
        }
        @Override
        public void onLocationError() {
            mLocation = null;
            mLocation = new Location(39.908815,116.397228);
            secnicLoc = new Location(39.908815,116.397228);
            ConstTools.myCurrentLoacation = new  Location(39.908815,116.397228);
            ConstTools.myCurrentLoacation.city ="北京";
            ConstTools.myCurrentLoacation.city_en = "beijing";
            loadHomeBanner();
            Log.d("onLocationError");
            // TODO
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOfflineManager = OfflinePackageManager.getInstance();
        mLocationDetector = new LocationDetector(getActivity().getApplicationContext());
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        ShareManager.getInstance().onStart(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container,false);
        initViews();
        reload();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        // hookSensorListener(!isHidden());
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
        // hookSensorListener(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mId = getArguments().getInt(Const.EXTRA_ID);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d("onHiddenChanged, hidden=%s", hidden);
        // hookSensorListener(!hidden);
        super.onHiddenChanged(hidden);
    }
    private void reload() {
        Log.d("reload", "\\\\\\\\");
        mPullToRefreshView.setMode(Mode.PULL_FROM_END);
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.VISIBLE);
        mReloadView.setVisibility(View.GONE);
        mAdapter.setHeader(null);
        mAdapter.setDataItems(null);
        mScenicListLoader = null;
        RequestManager.getInstance().getRequestQueue().cancelAll(mRequestTag);
       if (mRandomLoad) {
//            loadHomeBanner();
            return;
        }
        if (mId > 0) {
            ScenicDetails offlineData = mOfflineManager.getOfflineData(mId, ScenicDetails.class);
            if (offlineData != null) {
                Log.d("Using offline package data for scenic %d", mId);
                onScenicDetailsLoaded(offlineData);
            } else {
                loadHomeBanner();
            }
        } else {
//            mLocation = getArguments().getParcelable(Const.EXTRA_COORDINATES);
//            if (mLocation != null && mLocation.isValid()) {
                loadHomeBanner();
//            } else {
//                mLocationDetector.detectLocation(mLocationListener, true, true);
//            }
        }
        mLocationDetector.detectLocation(mLocationListener, true, true);
    }

    private void initViews() {
        initActionBar();
        initListView();
        initPullToRefresh();

        mLoadingProgress = mRootView.findViewById(R.id.loading_progress);
        mReloadView = mRootView.findViewById(R.id.reload_view);
        mReloadView.setOnClickListener(this);
    }

    private void initPullToRefresh() {
        mPullToRefreshView = (PullToRefreshRecyclerView) mRootView.findViewById(R.id.pulltorefresh_twowayview);
        mPullToRefreshView.setOnRefreshListener(new OnRefreshListener<RecyclerView>() {

            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                if (refreshView.getCurrentMode() == Mode.PULL_FROM_START) {
                    reload();
                } else {
                    //下拉加载
                    loadNearScenics();
                }
            }
        });
    }

    private void initListView() {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new HomeAdapter(getActivity(),
            new BannerPagerAdapter.IActionCallback<QHHomeBanner>() {
                @Override
                public void doAction(QHHomeBanner data) {
                    if (data == null) {
                        return;
                    }
                    Intent intent = new Intent();
                    if(QHHomeBanner.SCENIC==data.stype){//H5 景区 视频
                        MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_PPT1);
                        intent.setClass(getActivity(), SecnicActivity.class);
//                        intent.putExtra(Const.EXTRA_NAME,data.title);
                        intent.putExtra(Const.EXTRA_ID,Integer.parseInt(data.action));
                    }
                    else if(QHHomeBanner.VIDEO==data.stype){//活动
                        MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_ACT);
                        intent.setClass(getActivity(), WebActivity.class);
//                        intent.putExtra(Const.EXTRA_NAME,data.title);
                        intent.putExtra(Const.EXTRA_URI,data.action);
                    }else if (QHHomeBanner.H5 == data.stype){
                        MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_ACT);
                        intent.setClass(getActivity(), WebActivity.class);
//                        intent.putExtra(Const.EXTRA_NAME,data.title);
                        intent.putExtra(Const.EXTRA_URI,data.action);
                    }
//                    else if("2".equals(data.action)){//游记
//                        /*MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_ACT);
//                        intent.setClass(getActivity(), WebActivity.class);
//                        intent.putExtra(Const.EXTRA_NAME,data.title);
//                        intent.putExtra(Const.EXTRA_URI,data.action);*/
//                    }
                    startActivity(intent);
                }


            }
        );
        ItemClickSupport clickSupport = ItemClickSupport.addTo(mRecyclerView);
        clickSupport.setOnItemSubViewClickListener(new OnItemSubViewClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position == 0) {
                    onHeaderClicked(view);
                } else {
                    onItemClicked(view);
                }
            }
        });
        int scap = ScreenUtils.dpToPxInt(getActivity(), 13);
        DividerItemDecoration decor = new DividerItemDecoration(scap);
        decor.initWithRecyclerView(mRecyclerView);
        mRecyclerView.addItemDecoration(decor);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initActionBar() {
        mActionBar = (ActionBar) mRootView.findViewById(R.id.action_bar);
        mActionBar.setBackgroundResource(R.drawable.fg_top_shadow);

        mActionBar.getTitleView().setText("青海");
        mActionBar.getBarDivide().setVisibility(View.GONE);
    }
    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.action_bar_left:
                getActivity().finish();
                break;
            case R.id.action_bar_left_text: {
                Intent intent = new Intent(getActivity(), /*VideoPlayer*//*MediaPlayerDemo_Video*/CityListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SEARCH);
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_SCENIC_SEARCH);
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                String type = "video*//* ";
                Uri uri = Uri.parse("http://forum.ea3w.com/coll_ea3w/attach/2008_10/12237832415.3gp");
                intent.setDataAndType(uri, type);
                startActivity(intent);*/
                break;
            }
            case R.id.reload_view: {
                reload();
                break;
            }
            default:
                break;
        }
    }

    private void loadHomeBanner() {
        final String url;
        url = "http://182.92.218.24:9000/api/banners?format=json";
//        url = ServerAPI.ScenicDetails.buildQinghaiUrl("json");
        Log.e("Loading scenic details from %s", url);
        mBannerRequest = RequestManager.getInstance().sendGsonRequest(Method.GET, url,
                QHHomeBanner.QHHomeBannerLists.class, null,
                new Response.Listener<QHHomeBanner.QHHomeBannerLists>() {
                    @Override
                    public void onResponse(QHHomeBanner.QHHomeBannerLists response) {
                        mPullToRefreshView.onRefreshComplete();
                        mLoadingProgress.setVisibility(View.GONE);
                        Log.e("loadHomeBanner, ScenicDetails=%s,mAdapter.size=%d", response, mAdapter.getDataItems().size());
                        mRandomLoad = false;
                        mAdapter.setDataItems(null);
                        mAdapter.setHeader(response);
                        mAdapter.setMyLocation(mLocation);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataSetChanged();

                        loadNearScenics();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Error loading scenic details from %s", url);
                        showReloadView();
                        /*if (mScenicDetailsModel == null) {
                            showReloadView();
                        }
                        if (mBannerRequest != null) {
                            mBannerRequest.markDelivered();
                        }*/
                        mRandomLoad = false;
                        mPullToRefreshView.onRefreshComplete();
                    }
                }, true, mRequestTag);

    }

    private void showReloadView() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingProgress.setVisibility(View.GONE);
        mReloadView.setVisibility(View.VISIBLE);
    }

    private void onScenicDetailsLoaded(ScenicDetails data) {
        Log.e("-------","onScenicDetailsLoaded--");
        mScenicDetailsModel = data;
        mId = data.id;
        mActionBar.getLeftTextView().setText( FormatUtils.cutStringStartBy(mScenicDetailsModel.cityZh, 3));
        loadHomeBanner();

        // Check if we already have an offline archive
        if (!data.isOfflinePackage) {
            ScenicDetails offlineData = mOfflineManager.getOfflineData(data.id, ScenicDetails.class);
            if (offlineData != null) {
                mScenicDetailsModel = offlineData;
                Log.d("Replacing scenic %s with offline data", data.name);
            }
        }
    }
    private void loadNearScenics(String cityName) {
        Log.e("-------","loadNearScenics--");
        mLoadingProgress.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);

        if (mScenicListLoader == null) {
            mScenicListLoader = new UrlListLoader<Scenic.ScenicList>( mRequestTag, Scenic.ScenicList.class);
            String  url;
            url = ServerAPI.ScenicList.buildUrl(ServerAPI.ScenicList.Type.NEARBY,  cityName, HTTP_GET_PARAM_LIMIT,0) ;
            mScenicListLoader.setUrl(url);
        }

//        mScenicListLoader.loadMoreData(this, DataLoader.MODE_LOAD_MORE);
    }

    private void loadNearScenics(){
        mLoadingProgress.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        if (mScenicQHListLoader == null) {
            mScenicQHListLoader = new UrlListLoader<QHScenic.QHScenicList>( mRequestTag, QHScenic.QHScenicList.class);
            String  url;
            url = "http://182.92.218.24:9000/api/scenics/?format=json";
            mScenicQHListLoader.setUrl(url);
        }
        //加载更多
        mScenicQHListLoader.loadMoreQHData(this, DataLoader.MODE_LOAD_MORE);
    }



    @Override
    public void onLoadFinished(QHScenic.QHScenicList list, int mode) {
        mPullToRefreshView.onRefreshComplete();
        onListLoaded(list);
    }

    @Override
    public void onLoadError(int mode) {
        Log.e("Error loading scenic details comment for %d", mId);
        mPullToRefreshView.onRefreshComplete();
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void onListLoaded(QHScenic.QHScenicList data) {
        mLoadingProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (data == null || data.results == null|| mScenicListLoader != null) {
            // Disable PULL_FROM_END
            mPullToRefreshView.setMode(Mode.PULL_FROM_END);
            Toast.makeText(getActivity(),"没有更多数据",Toast.LENGTH_SHORT).show();
            return;
        }

        if (data != null) {
            if (data.pagination != null && data.pagination.offset == 0) {
                mAdapter.setDataItems(null);
            }
            mAdapter.appendDataItems(data.results);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void onItemClicked(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }

        mScenic = (QHScenic) v.getTag();
        if (mScenic == null) {
            Log.e("NULL mScenic");
            return;
        }

        switch (v.getId()) {
            case R.id.iv_cover_image:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_SECNIC);
                Intent intent = new Intent(getActivity(), SecnicActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putExtra("scenic",mScenic);
                int mId = mScenic.id;
                intent.putExtra(Const.QH_SECNIC,mScenic);
                intent.putExtra(Const.QH_SECNIC_ID,mId);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    private void onHeaderClicked(View v) {
        /*if (mScenicDetailsModel == null) {
            return;
        }*/
        switch (v.getId()) {
            case R.id.scenic_cover_image:
                break;
            case R.id.home_search_et:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_BTN_CITY);
                //跳转到搜索页面
                Intent intent = new Intent(getActivity(), SearchListActivity.class);
                startActivity(intent);
//                Intent intent = new Intent(getActivity(), SearchScenicActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SEARCH_ET);
                break;
            case R.id.home_tab_food:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_HOME_BTN_BBQ);
                Intent intent3 = new Intent(getActivity(), RestaurantActivity.class);
                if(null!=mScenicDetailsModel){
                    intent3.putExtra(Const.CITYNAME_EN,mScenicDetailsModel.city);
                    intent3.putExtra(Const.CITYNAME,mScenicDetailsModel.cityZh);
                }else {
                    intent3.putExtra(Const.CITYNAME_EN, "beijing");
                    intent3.putExtra(Const.CITYNAME,"北京");
                }
//                intent3.putExtra(Const.EXTRA_COORDINATES,mLocation);
                intent3.putExtra(Const.LAT,""+secnicLoc.latitude);
                intent3.putExtra(Const.LON,""+secnicLoc.longitude);
                startActivity(intent3);
                break;
           case R.id.home_tab_hotel:

                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_BTN_HOTEL);
//                Intent intent1 = new Intent(getActivity(), HotelActivity.class);
               Intent intent1 = new Intent(getActivity(), QHNavigationActivity.class);
//               if(null!=mScenicDetailsModel){
//                   intent1.putExtra(Const.CITYNAME_EN,mScenicDetailsModel.city);
//                   intent1.putExtra(Const.CITYNAME,mScenicDetailsModel.cityZh);
//               }else{
//                   intent1.putExtra(Const.CITYNAME_EN,"beijing");
//                   intent1.putExtra(Const.CITYNAME,"北京");
//               }
//                intent1.putExtra(Const.EXTRA_COORDINATES,mLocation);
                startActivity(intent1);
                break;
           case R.id.home_tab_special:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_HOME_BTN_SPECIALTY);
                Intent intent2 = new Intent(getActivity(), SpecialActivity.class);

               if(null!=mScenicDetailsModel){
                   intent2.putExtra(Const.CITYNAME_EN,mScenicDetailsModel.city);
                   intent2.putExtra(Const.CITYNAME,mScenicDetailsModel.cityZh);
               }else{
                   intent2.putExtra(Const.CITYNAME_EN,"beijing");
                   intent2.putExtra(Const.CITYNAME,"北京");
               }
                startActivity(intent2);
                break;
            case R.id.home_tab_guide:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_GUIDE);
                Intent intentWati = new Intent();
                intentWati.setClass(getActivity(), WaitActivity.class);
                startActivity(intentWati);
                break;
            default:
                break;
            }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOGIN_NEW_COMMENT:
                    break;
                case REQUEST_CODE_LOGIN_VOTE:
                    break;
                case REQUEST_CODE_LOGIN_COMMENT_DETAIL:
                    break;
                case REQUEST_CODE_SEARCH:
                    City city = data.getParcelableExtra(Const.CITYMODE);
                    if (null!=city) {
                        mId = 1;

                        if(null==mScenicDetailsModel){
                            mLocation.latitude = city.location.latitude;
                            mLocation.longitude = city.location.longitude;
                            mScenicDetailsModel = new ScenicDetails();
                        }
                        else {
                               mScenicDetailsModel.city = city.code;
                               mScenicDetailsModel.cityZh = city.name;
                               mScenicDetailsModel.location.latitude = city.location.latitude;
                               mScenicDetailsModel.location.longitude = city.location.longitude;
                           }
                        mActionBar.getLeftTextView().setText(FormatUtils.cutStringStartBy(city.name, 3));
                        secnicLoc.latitude = city.location.latitude;
                        secnicLoc.longitude = city.location.longitude;
                        secnicLoc.city = city.name;
                        secnicLoc.city_en = city.code;

                        mAdapter.setDataItems(null);
                        mScenicListLoader = null;
                        loadNearScenics(city.name);
//                        reload();

                        /*if(null==mScenicDetailsModel){
                            mLocation.latitude = city.location.latitude;
                            mLocation.longitude = city.location.longitude;
//                            loadScenicDetails();
                        }
                        else{
                        mScenicDetailsModel.city = city.code;
                        mScenicDetailsModel.cityZh = city.name;
                        mScenicDetailsModel.location.latitude = city.location.latitude;
                        mScenicDetailsModel.location.longitude = city.location.longitude;
                        mActionBar.getLeftTextView().setText(FormatUtils.cutStringStartBy(city.name, 3));
                        mLocation.latitude = city.location.latitude;
                        mLocation.longitude = city.location.longitude;
                        mLocation.city = city.name;
                        mLocation.city_en = city.code;
//                        reload();
                        }*/
                    } else {
                        Log.e("error onActivityResult, id = ", mId);
                    }
                    break;
                case REQUEST_CODE_SEARCH_ET:
                    int id = data.getIntExtra(Const.EXTRA_ID, -1);
                    if (id > -1 && mId != id) {
                        MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_HOME_SECNIC);
                        Intent intent = new Intent(getActivity(), SecnicActivity.class);
                        intent.putExtra(Const.EXTRA_ID,id);
                        intent.putExtra(Const.EXTRA_COORDINATES,mLocation);
                        startActivity(intent);
                    } else {
                        Log.e("error onActivityResult, id = ", id);
                    }
                    break;
                case REQUEST_CODE_COMMENT_DETAIL:
                    break;
                case REQUEST_CODE_POST_COMMENT: {
                    Comment c = (Comment) data.getParcelableExtra(Const.EXTRA_COMMENT_DATA);
                    if (c != null) {
//                        mAdapter.getDataItems().add(0, c);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
    @Override
    public void onDestroy() {
        if (mLocationDetector != null) {
            mLocationDetector.close();
        }
        ShareManager.getInstance().onEnd();
        super.onDestroy();
    }

    private void loadScenicDetails() {
        final String url;
        url = "http://182.92.218.24:9000/api/scenics/?format=json";
//        url = ServerAPI.ScenicDetails.buildQinghaiUrl("json");
        RequestManager.getInstance().sendGsonRequest(Method.GET, url,
                QHScenic.QHScenicList.class, null,
                new Response.Listener<QHScenic.QHScenicList>() {
                    @Override
                    public void onResponse(QHScenic.QHScenicList response) {
                        Log.d("onResponse, ScenicDetails=%s,mAdapter.size=%d", response, mAdapter.getDataItems().size());
                        mRandomLoad = false;
//                        mAdapter.setDataItems(null);
//                        onScenicDetailsLoaded(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLoadingProgress.setVisibility(View.GONE);
                        mReloadView.setVisibility(View.VISIBLE);
//                        showReloadView();
                    }
                }, true, mRequestTag);
    }


}
