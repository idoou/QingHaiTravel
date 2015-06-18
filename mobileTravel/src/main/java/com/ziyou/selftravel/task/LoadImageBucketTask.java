/**
 * 
 */

package com.ziyou.selftravel.task;

import android.content.Context;

import com.ziyou.selftravel.model.ImageBucket;
import com.ziyou.selftravel.util.ImageHelper;

import java.util.List;

/**
 * @author kuloud
 */
public class LoadImageBucketTask extends BaseTask<Boolean, Integer, List<ImageBucket>> {

    private Context mContext;

    public LoadImageBucketTask(Context context) {
        mContext = context;
    }

    @Override
    protected List<ImageBucket> doInBackground(Boolean... params) {
        boolean refresh = false;
        if (params != null) {
            refresh = params[0].booleanValue();
        }
        return new ImageHelper(mContext).getImagesBucketList(refresh);
    }

}
