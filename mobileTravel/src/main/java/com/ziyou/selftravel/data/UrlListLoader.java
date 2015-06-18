
package com.ziyou.selftravel.data;

import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.model.Paginator;
import com.ziyou.selftravel.model.QHResultList;
import com.ziyou.selftravel.model.ResultList;
import com.ziyou.selftravel.util.CommonUtils;
import com.ziyou.selftravel.util.Log;

public class UrlListLoader<T extends ResultList> implements DataLoader<T> {
    private String mRequestTag;
    private Paginator mPaginator = new Paginator();
    private String mUrl;
    private GsonRequest<T> mRequestInFly;
    private Class<T> mClazz;
    private int mPageLimit = Const.LIST_DEFAULT_LOAD_ITEMS;
    private boolean mUseCacae = false;
    protected boolean hasNext = true;
    protected String nextUrl;

    public UrlListLoader(String requestTag, Class<T> clazz) {
        mRequestTag = requestTag;
        mClazz = clazz;
    } public UrlListLoader(String requestTag, Class<T> clazz,Paginator mPaginator ) {
        mRequestTag = requestTag;
        mClazz = clazz;
        this.mPaginator = mPaginator;
    }

    public void setUseCache(boolean cache) {
        mUseCacae = cache;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setPageLimit(int limit) {
        if (limit >= 0) {
            mPageLimit = limit;
        }
    }

    public Paginator getPaginator() {
        return mPaginator;
    }

    @Override
    public void loadMoreData(final DataLoader.DataLoaderCallback<T> cb, final int mode) {
        if (mode == DataLoader.MODE_REFRESH) {
            mPaginator.reset();
        } else if (!mPaginator.hasMorePages()) {
            cb.onLoadFinished(null, mode);
            return;
        }
        if (!CommonUtils.isValidUrl(mUrl)) {
            throw new IllegalArgumentException("Illegal url:" + mUrl);
        }
        checkUrlIsSpecial();
        final String url = ServerAPI.appendListParams(mUrl, mPageLimit, mPaginator.nextLoadOffset());
        Log.d("Loading list from %s", url);
        mRequestInFly = RequestManager.getInstance().sendGsonRequest(Method.GET, url, mClazz, null,
                new Response.Listener<T>() {
                    @Override
                    public void onResponse(T responseList) {
                        Log.d("***** xxxxxx list loaded, response=%s",
                                responseList);
                        mPaginator.addPage(responseList.pagination);
                        cb.onLoadFinished(responseList, mode);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Load list error, error=%s", error);
                        cb.onLoadError(mode);
                        if (mRequestInFly != null) {
                            mRequestInFly.markDelivered();
                        }
                    }
                }, mUseCacae, mRequestTag);
    }

    public void loadMoreQHData(final DataLoader.DataLoaderCallback<T> cb, final int mode){

        if(hasNext != true){
            cb.onLoadFinished(null, mode);
            return;
        }

        if (!CommonUtils.isValidUrl(mUrl)) {
            throw new IllegalArgumentException("Illegal url:" + mUrl);
        }
        checkUrlIsSpecial();
//        final String url = ServerAPI.appendListParams(mUrl, mPageLimit, mPaginator.nextLoadOffset());
        String url = mUrl;
        if(mode == MODE_LOAD_MORE && !TextUtils.isEmpty(nextUrl)){
            url = nextUrl;
        }
        Log.d("Loading list from %s", url);
        mRequestInFly = RequestManager.getInstance().sendGsonRequest(Method.GET, url, mClazz, null,
                new Response.Listener<T>() {
                    @Override
                    public void onResponse(T responseList) {
                        Log.d("***** xxxxxx list loaded, response=%s",
                                responseList);
                        nextUrl = responseList.next;
                        if(TextUtils.isEmpty(nextUrl)){
                            hasNext = false;
                        }
//                        mPaginator.addPage(responseList.next);
                        cb.onLoadFinished(responseList, mode);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Load list error, error=%s", error);
                        cb.onLoadError(mode);
                        if (mRequestInFly != null) {
                            mRequestInFly.markDelivered();
                        }
                    }
                }, mUseCacae, mRequestTag);
    }


    public boolean delivered() {
        if (mRequestInFly != null) {
            return mRequestInFly.hasHadResponseDelivered();
        }
        return false;
    }

    public void cancel() {
        if (mRequestInFly != null && !mRequestInFly.isCanceled()) {
            mRequestInFly.cancel();
        }
    }

    @Override
    public void onLoaderDestory() {
        cancel();
    }

    /*判断是否是特殊接口*/
    public void checkUrlIsSpecial(){
        if(mUrl.contains("scenic_spots")){//景点列表默认一次加载200
            mPageLimit = Const.LIST_SECNIC_SPOTS_LOAD_ITEMS;
        }
    }

}
