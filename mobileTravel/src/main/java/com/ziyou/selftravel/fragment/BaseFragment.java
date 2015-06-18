/**
 * 
 */

package com.ziyou.selftravel.fragment;


import android.app.Fragment;
import android.os.Bundle;


import com.ziyou.selftravel.app.Const;

/**
 * Base fragment, all fragment should extends it.
 * 
 * @author Kuloud
 */
public class BaseFragment extends Fragment {
    protected static final boolean DEBUG = Const.DEBUG;

    protected String mRequestTag = BaseFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey(Const.ARGS_REQUEST_TAG)) {
            mRequestTag = getArguments().getString(Const.ARGS_REQUEST_TAG);
        }
//        else if (DEBUG){
//            throw new RuntimeException("A volley request tag must be provided");
//        }

        super.onCreate(savedInstanceState);
    }

    /**
     * 
     */
    public BaseFragment() {
        // TODO Auto-generated constructor stub
    }

    public String getFragmentName(){
        return this.getClass().getSimpleName();
    }

}
