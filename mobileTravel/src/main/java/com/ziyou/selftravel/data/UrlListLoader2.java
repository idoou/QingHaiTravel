
package com.ziyou.selftravel.data;

import android.content.Context;
import android.content.Loader;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ziyou.selftravel.app.ServerAPI;
import com.ziyou.selftravel.model.Paginator;
import com.ziyou.selftravel.model.ResultList;
import com.ziyou.selftravel.util.CommonUtils;
import com.ziyou.selftravel.util.Log;

public class UrlListLoader2<T extends ResultList> extends Loader<T> {
    private String mRequestTag;
    private Paginator mPaginator = new Paginator();
    private String mUrl;
    private boolean mIsForceLoad;
    private GsonRequest<T> mRequestInFly;
    private Class<T> mClazz;

    public UrlListLoader2(Context context, String requestTag, Class<T> clazz) {
        super(context);
        mRequestTag = requestTag;
        mClazz = clazz;
    }

    public void setUrl(String url) {
        if (!CommonUtils.isValidUrl(url)) {
            throw new IllegalArgumentException("Illegal url:" + url);
        }
        mUrl = url;
    }

    public boolean isForceLoad() {
        return mIsForceLoad;
    }

    public void loadMorePages() {
        doLoad();
    }

    @Override
    protected void onStartLoading() {
        Log.d("onStartLoading");
        if (TextUtils.isEmpty(mUrl)) {
            throw new RuntimeException("Set url before init this loader");
        }

        mIsForceLoad = false;// ??????????

        doLoad();
        super.onStartLoading();
    }

    /*
     * @Override protected boolean onCancelLoad() { if (mRequestInFly != null) {
     * mRequestInFly.cancel(); } return super.onCancelLoad(); }
     */

    @Override
    protected void onForceLoad() {
        Log.d("onStartLoading");
        mPaginator.reset();
        mIsForceLoad = true;
        doLoad();
        super.onForceLoad();
    }

    @Override
    protected void onStopLoading() {
        Log.d("onStartLoading");
        super.onStopLoading();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
    }

    @Override
    protected void onReset() {
        Log.d("onStartLoading");
        mIsForceLoad = false;
        mPaginator.reset();
        super.onReset();
    }

    private void doLoad() {
        if (!mPaginator.hasMorePages()) {
            deliverResult(null);
            return;
        }

        final String url = ServerAPI.appendListParams(mUrl, 1,
                mPaginator.nextLoadOffset());
        Log.d("Loading list from %s", url);
        mRequestInFly = RequestManager.getInstance().sendGsonRequest(url,
                mClazz,
                new Response.Listener<T>() {
                    @Override
                    public void onResponse(T responseList) {
                        Log.d("***** xxxxxx list loaded, response=%s",
                                responseList);
                        mPaginator.addPage(responseList.pagination);
                        deliverResult(responseList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(error, "Load video list error, error=%s", error);
                        /*
                         * T errorT; try { errorT = mClazz.newInstance();
                         * errorT.error = error; deliverResult(errorT); } catch
                         * (Exception e) { e.printStackTrace(); }
                         */
                    }
                }, mRequestTag);
    }

}
