/**
 *
 */

package com.ziyou.selftravel.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.fragment.FeedbackFragment;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.GuiderCollectionActivity;
import com.ziyou.selftravel.activity.FavoritesActivity;
import com.ziyou.selftravel.activity.FeedbackActivity;
import com.ziyou.selftravel.activity.GuiderMessageListActivity;
import com.ziyou.selftravel.activity.GuiderPublishActivity;
import com.ziyou.selftravel.activity.GuiderRecommandActivity;
import com.ziyou.selftravel.activity.LoginActivity;
import com.ziyou.selftravel.activity.PublishActivity;
import com.ziyou.selftravel.activity.SettingsActivity;
import com.ziyou.selftravel.activity.UserProfileActivity;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.app.UserManager;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.manager.DataCleanManager;
import com.ziyou.selftravel.model.QHUser;
import com.ziyou.selftravel.model.User;
import com.ziyou.selftravel.util.AppUtils;
import com.ziyou.selftravel.util.ExcessiveClickBlocker;
import com.ziyou.selftravel.util.FileUtils;
import com.ziyou.selftravel.widget.ActionBar;

/**
 * @author kuloud
 */
public class MeFragment extends BaseFragment implements OnClickListener {
    private final String TAG = "MeFragment";

    private final int REQUEST_CODE_PUBLISH = 1;
    private final int REQUEST_CODE_FAVORITES = 2;
    private final int REQUEST_CODE_SETTINGS = 4;
    private final int REQUEST_CODE_COLLECT = 5;

    private NetworkImageView mAvatar = null;
    private TextView mName = null;
    private View me_header;

    public MeFragment() {
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initActionBar(view);
        me_header = view.findViewById(R.id.me_header);
        mAvatar = (NetworkImageView) view.findViewById(R.id.iv_me_avata);
        mName = (TextView) view.findViewById(R.id.tv_me_name);
        me_header.setOnClickListener(this);
        initListItems(view);
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        bindUserInfo(AppUtils.getQHUser(getActivity()));
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd(TAG);
        super.onPause();
    }

    private void initListItems(View container) {
        setItem(container, R.id.item_publish, R.drawable.publish, R.string.me_item_publish);
        setItem(container, R.id.item_collect, R.drawable.collect, R.string.me_item_collect);
        setItem(container, R.id.item_message, R.drawable.message, R.string.me_item_message);
        setItem(container, R.id.item_share, R.drawable.share, R.string.me_item_share);
        setItem(container, R.id.item_renovate, R.drawable.renovate, R.string.me_item_renovate);
        setItem(container, R.id.item_clear, R.drawable.delete, R.string.me_item_clear);
        setItem(container, R.id.item_feedback, R.drawable.feedback, R.string.settings_item_feedback);
    }

    private void setItem(View container, int id, int iconId, int textId) {
        View item = container.findViewById(id);
        item.setOnClickListener(this);
        ImageView icon = (ImageView) item.findViewById(R.id.item_icon);
        icon.setImageResource(iconId);
        TextView text = (TextView) item.findViewById(R.id.item_text);
        text.setText(textId);
    }

    private void initActionBar(View view) {
        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setTitle(R.string.action_bar_title_me);
        actionBar.setBackgroundResource(R.color.title_bg);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SETTINGS:
                    mAvatar.setImageResource(R.drawable.bg_avata_hint);
                    mName.setText("");
                    break;
                case REQUEST_CODE_PUBLISH:
                    Intent publish = new Intent(getActivity(), PublishActivity.class);
                    startActivity(publish);
                    break;
                case REQUEST_CODE_FAVORITES:
                    Intent favorites = new Intent(getActivity(), FavoritesActivity.class);
                    startActivity(favorites);
                    break;

                default:
                    break;
            }
        }
    }

    private void bindUserInfo(QHUser user) {
        if (user == null) {
            return;
        }
        boolean localLoaded = false;
        if(null!=user.user_info.avatar_url) {
            if (FileUtils.fileCached(getActivity(), user.user_info.avatar_url)) {
                String url = FileUtils
                        .getCachePath(getActivity(), user.user_info.avatar_url);
                Bitmap bm = FileUtils.getLocalBitmap(url);
                if (bm != null) {
                    mAvatar.setImageBitmap(bm);
                    localLoaded = true;
                }
            }
        }
        if (!localLoaded) {
            // If not find the target avatar, it means that the avatar has been updated. so we should delete the old one.
            String oldAvatarUrl = AppUtils.getOldAvatarUrl(getActivity());
            if (!TextUtils.isEmpty(oldAvatarUrl)) {
                FileUtils.delFile(FileUtils.getCachePath(getActivity(), oldAvatarUrl));
            }
            mAvatar.setDefaultImageResId(R.drawable.bg_avata_hint);
            mAvatar.setErrorImageResId(R.drawable.bg_avata_hint);
            mAvatar.setImageUrl(user.user_info.avatar_url, RequestManager.getInstance().getImageLoader());

        }
        mName.setText(user.username);
    }

    @Override
    public void onClick(View v) {
        if (ExcessiveClickBlocker.isExcessiveClick()) {
            return;
        }
        Intent intent = null;
        switch (v.getId()) {
            case R.id.me_header:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_ME_AVATA);
                QHUser user = AppUtils.getQHUser(getActivity());
                if (user == null) {
                    Intent login = new Intent(getActivity(), LoginActivity.class);
                    startActivity(login);
                } else {
                    Intent info = new Intent(getActivity(), UserProfileActivity.class);
                    info.putExtra("user", user);
                    startActivity(info);
                }
                break;
            case R.id.item_publish:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_ME_TRIP);
                if (!UserManager.makeSureLogin(getActivity(), REQUEST_CODE_PUBLISH)) {
                    intent = new Intent(getActivity(), GuiderPublishActivity.class);
                }
                break;
            case R.id.item_feedback:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_ME_FEEDBACK);
                intent = new Intent(getActivity(), FeedbackActivity.class);
                String id = new FeedbackAgent(getActivity()).getDefaultConversation().getId();
                intent.putExtra(FeedbackFragment.BUNDLE_KEY_CONVERSATION_ID, id);
                break;
            case R.id.action_bar_right:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_ME_SETTINGS);
                Intent settings = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(settings, REQUEST_CODE_SETTINGS);
                break;
            case R.id.item_collect:
                Intent collect = new Intent(getActivity(), GuiderCollectionActivity.class);
                startActivity(collect);
                break;
             case R.id.item_share:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_ME_SETTINGS);
                Intent recommend = new Intent(getActivity(), GuiderRecommandActivity.class);
                startActivity(recommend);
                break;
            case R.id.item_message:
                MobclickAgent.onEvent(getActivity(), MobConst.ID_INDEX_ME_SETTINGS);
                Intent message = new Intent(getActivity(), GuiderMessageListActivity.class);
                startActivity(message);
                break;
            case R.id.item_clear:
                boolean b = DataCleanManager.cleanInternalCache(getActivity());
                if(b){
                    Toast.makeText(getActivity(), "清除缓存成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(),"清除缓存出错",Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
