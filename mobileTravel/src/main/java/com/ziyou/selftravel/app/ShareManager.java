/**
 * 
 */

package com.ziyou.selftravel.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMShareBoardListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMusic;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.model.AudioIntro;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.Location;
import com.ziyou.selftravel.model.TripDetail;

import java.util.Locale;

/**
 * @author Kuloud
 */
public final class ShareManager {
    private Activity mActivity;
    private static final String HOME_PAGE = "http://ziyou01.com/";
    private static final String LISTEN_PAGE_FORMAT = "http://webapp.selftravel.com.cn/scenic_audio.html?title=%s&scenic_id=%d&spot_id=%d&lat=%f&lng=%f&url=%s";
    private static final String COMMENT_PAGE_FORMAT = "http://webapp.selftravel.com.cn/comment_detail.html?author=%s&id=%d&share=true";
    private static final String WECHAT_APP_ID = "wx23bd207fa365b28f";
    private static final String WECHAT_APP_SECRET = "20df99e061761ec5025f8f648876761f";
    private static final String QQ_APP_ID = "1103841897";
    private static final String QQ_APP_KEY = "k0lGrR7J6427pYuA";
    private static final String SHARE_DESCRIPTOR = "SelfTravel";

    private UMSocialService mController = UMServiceFactory
            .getUMSocialService(SHARE_DESCRIPTOR);
    private static ShareManager sShareManager;

    private boolean mBoardOpened = false;

    private ShareManager() {
    }

    synchronized public static ShareManager getInstance() {
        if (sShareManager == null) {
            sShareManager = new ShareManager();
        }
        return sShareManager;
    }

    public void share() {

    }

    public void onStart(Activity activity) {
        mActivity = activity;
        configPlatforms();
    }

    public void onEnd() {
        hideBorad();
        mActivity = null;
        mController.dismissShareBoard();
    }

    private void configPlatforms() {
        // Setup Sina SSO
        mController.getConfig().setSsoHandler(new SinaSsoHandler());

        // add QQ、QZone
        addQQQZonePlatform();

        // add wechat
        addWXPlatform();
    }

    private void addQQQZonePlatform() {
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mActivity,
                QQ_APP_ID, QQ_APP_KEY);
        qqSsoHandler.setTargetUrl("http://www.umeng.com/social"); // TODO
        qqSsoHandler.addToSocialSDK();

        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(mActivity, QQ_APP_ID, QQ_APP_KEY);
        qZoneSsoHandler.addToSocialSDK();
    }

    private void addWXPlatform() {
        UMWXHandler wxHandler = new UMWXHandler(mActivity, WECHAT_APP_ID, WECHAT_APP_SECRET);
        wxHandler.addToSocialSDK();

        UMWXHandler wxCircleHandler = new UMWXHandler(mActivity, WECHAT_APP_ID, WECHAT_APP_SECRET);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    public void openShare(Bitmap bitmap, String content) {
        mController.getConfig().enableSIMCheck(false);
        mController.setShareContent(content);
        UMImage imgBitmap = (bitmap == null) ? new UMImage(mActivity, R.drawable.ic_launcher)
                : new UMImage(mActivity, bitmap);
        mController.setShareMedia(imgBitmap);
        openShareBoard();
    }

    public void shareTripDetail(TripDetail tripDetail) {
        // TODO
    }

    public void shareAudio(int scenic_id, AudioIntro audioIntro, Location loc) {
        shareAudio(scenic_id, -1, audioIntro, loc);
    }

    public void shareAudio(int scenic_id, int spot_id, AudioIntro audioIntro, Location loc) {
        if (audioIntro == null) {
            return;
        }
        String appName = mActivity.getString(R.string.app_name);
        String audioUrl = audioIntro.url;
        String audioTitle = TextUtils.isEmpty(audioIntro.title) ? appName : audioIntro.title;
        Bitmap bitmap = audioIntro.imageBitmap;
        String content = audioIntro.content;

        // setup image
        UMImage resImage = null;
        if (bitmap == null) {
            resImage = new UMImage(mActivity, R.drawable.ic_launcher);
        } else {
            resImage = new UMImage(mActivity, bitmap);
        }

        // setup audio
        UMusic uMusic = null;
        if (!TextUtils.isEmpty(audioUrl)) {
            uMusic = new UMusic(audioUrl);
            uMusic.setAuthor(appName);
            uMusic.setTitle(audioTitle);
            uMusic.setThumb(resImage);
        }

        String targetUrl = getAudioTargetUrl(audioTitle, scenic_id, spot_id, loc, audioUrl);
        // Separate setup each platform, for customize according to the
        // situation diversification.
        mController.getConfig().setSsoHandler(new SinaSsoHandler());
        SinaShareContent sinaContent = new SinaShareContent();
        setupSinaShareContent(sinaContent, audioTitle, content, resImage, uMusic, targetUrl);

        WeiXinShareContent wechatContent = new WeiXinShareContent();
        setupWeChatShareContent(wechatContent, audioTitle, content, resImage, uMusic, targetUrl);
        CircleShareContent circleContent = new CircleShareContent();
        setupCircleShareContent(circleContent, audioTitle, content, resImage, uMusic, targetUrl);

        QQShareContent qqShareContent = new QQShareContent();
        setupQQShareContent(qqShareContent, audioTitle, content, resImage, uMusic, targetUrl);
        QZoneShareContent qzoneConent = new QZoneShareContent();
        setupQzoneShareContent(qzoneConent, audioTitle, content, resImage, uMusic, targetUrl);

        openShareBoard();
    }

    private void openShareBoard() {
        mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN_CIRCLE,
                 SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN);
        mController.openShare(mActivity, false);
        mController.setShareBoardListener(new UMShareBoardListener() {

            @Override
            public void onShow() {
                mBoardOpened = true;
            }

            @Override
            public void onDismiss() {
                mBoardOpened = false;
            }
        });
    }

    private String getAudioTargetUrl(String audioTitle, int scenic_id, int spot_id, Location loc,
            String audioUrl) {
        return String.format(Locale.CHINESE, LISTEN_PAGE_FORMAT, audioTitle, scenic_id, spot_id,
                loc.latitude,
                loc.longitude, audioUrl);
    }

    private void setupSinaShareContent(SinaShareContent mediaContent, String title, String content,
            UMImage resImage, UMusic uMusic, String targetUrl) {
        if (!TextUtils.isEmpty(content)) {
            mediaContent.setShareContent(content);
        }

        mediaContent.setTitle(title);
        mediaContent.setShareImage(resImage);
        if (uMusic != null) {
            mediaContent.setShareMedia(uMusic);
        }

        mediaContent.setTargetUrl(targetUrl);

        mController.setShareMedia(mediaContent);
    }

    private void setupWeChatShareContent(WeiXinShareContent mediaContent, String title,
            String content,
            UMImage resImage, UMusic uMusic, String targetUrl) {
        if (!TextUtils.isEmpty(content)) {
            mediaContent.setShareContent(content);
        }

        mediaContent.setTitle(title);
        if (resImage != null) {
            mediaContent.setShareImage(resImage);
        }
        if (uMusic != null) {
            mediaContent.setShareMedia(uMusic);
        }

        mediaContent.setTargetUrl(targetUrl);

        mController.setShareMedia(mediaContent);
    }

    private void setupCircleShareContent(CircleShareContent mediaContent, String title,
            String content,
            UMImage resImage, UMusic uMusic, String targetUrl) {
        if (!TextUtils.isEmpty(content)) {
            mediaContent.setShareContent(content);
        }

        mediaContent.setTitle(title);
        mediaContent.setShareImage(resImage);

        mediaContent.setTargetUrl(targetUrl);

        mController.setShareMedia(mediaContent);
    }

    private void setupQQShareContent(QQShareContent mediaContent, String title, String content,
            UMImage resImage, UMusic uMusic, String targetUrl) {
        if (!TextUtils.isEmpty(content)) {
            mediaContent.setShareContent(content);
        }

        mediaContent.setTitle(title);
        mediaContent.setShareImage(resImage);

        mediaContent.setTargetUrl(targetUrl);

        mController.setShareMedia(mediaContent);
    }

    private void setupQzoneShareContent(QZoneShareContent mediaContent, String title,
            String content,
            UMImage resImage, UMusic uMusic, String targetUrl) {
        if (!TextUtils.isEmpty(content)) {
            mediaContent.setShareContent(content);
        }

        mediaContent.setTitle(title);
        mediaContent.setShareImage(resImage);

        mediaContent.setTargetUrl(targetUrl);

        mController.setShareMedia(mediaContent);
    }

    /**
     * @return
     */
    public boolean hideBorad() {
        if (mBoardOpened) {
            mController.dismissShareBoard();
            mBoardOpened = false;
            return true;
        }
        return false;
    }

    /**
     * @param id
     */
    public void shareComment(Comment comment) {
        String authorName = comment.author.name;
        int id = comment.id;
        String content = comment.content;
        String targetUrl = getCommentTargetUrl(authorName, id);

        UMImage resImage = null;
        if (comment.images == null || comment.images.size() == 0) {
            resImage = new UMImage(mActivity, R.drawable.ic_launcher);
        } else {
            resImage = new UMImage(mActivity, comment.images.get(0).smallImage);
        }

        mController.getConfig().setSsoHandler(new SinaSsoHandler());
        SinaShareContent sinaContent = new SinaShareContent();
        setupSinaShareContent(sinaContent, authorName, comment.content, resImage, null, targetUrl);

        WeiXinShareContent wechatContent = new WeiXinShareContent();
        setupWeChatShareContent(wechatContent, authorName, content, resImage, null, targetUrl);
        CircleShareContent circleContent = new CircleShareContent();
        setupCircleShareContent(circleContent, authorName, content, resImage, null, targetUrl);

        QQShareContent qqShareContent = new QQShareContent();
        setupQQShareContent(qqShareContent, authorName, content, resImage, null, targetUrl);
        QZoneShareContent qzoneConent = new QZoneShareContent();
        setupQzoneShareContent(qzoneConent, authorName, content, resImage, null, targetUrl);

        openShareBoard();
    }

    public void shareApp() {

        String authorName = "楚楚";
        String content = "分享下载";
        String targetUrl = "www.baidu.com";

        UMImage resImage = new UMImage(mActivity, R.drawable.ic_launcher);

        WeiXinShareContent wechatContent = new WeiXinShareContent();
        setupWeChatShareContent(wechatContent, authorName, content, resImage, null, targetUrl);
        CircleShareContent circleContent = new CircleShareContent();
        setupCircleShareContent(circleContent, authorName, content, resImage, null, targetUrl);

        QQShareContent qqShareContent = new QQShareContent();
        setupQQShareContent(qqShareContent, authorName, content, resImage, null, targetUrl);
        QZoneShareContent qzoneConent = new QZoneShareContent();
        setupQzoneShareContent(qzoneConent, authorName, content, resImage, null, targetUrl);

        openShareBoard();
    }

    private String getCommentTargetUrl(String authorName, int id) {
        return String.format(Locale.CHINESE, COMMENT_PAGE_FORMAT, authorName, id);
    }
}
