
package com.ziyou.selftravel.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.kuloud.android.widget.recyclerview.BaseHeaderAdapter;
import com.kuloud.android.widget.recyclerview.DividerItemDecoration;
import com.kuloud.android.widget.recyclerview.ItemClickSupport;
import com.kuloud.android.widget.recyclerview.ItemClickSupport.OnItemClickListener;
import com.umeng.analytics.MobclickAgent;
import com.ziyou.selftravel.R;
import com.ziyou.selftravel.activity.PhotoPreviewActivity;
import com.ziyou.selftravel.app.Const;
import com.ziyou.selftravel.app.MobConst;
import com.ziyou.selftravel.data.RequestManager;
import com.ziyou.selftravel.model.Comment;
import com.ziyou.selftravel.model.CompoundImage;
import com.ziyou.selftravel.model.CompoundImage.TextImage;
import com.ziyou.selftravel.model.Image;
import com.ziyou.selftravel.model.QHScenic;
import com.ziyou.selftravel.model.ScenicDetails;
import com.ziyou.selftravel.model.Weather.WeatherDay;
import com.ziyou.selftravel.util.Log;
import com.ziyou.selftravel.util.ScreenUtils;
import com.ziyou.selftravel.widget.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

public class ScenicDetailCommentAdapter extends BaseHeaderAdapter<QHScenic, QHScenic> {
    private final String TAG = "ScenicDetailCommentAdapter";
    private Activity mActivity;
    private List<Comment> items;

    public ScenicDetailCommentAdapter(Activity activity) {
        mActivity = activity;
    }

    public ScenicDetailCommentAdapter(Activity activity, List<QHScenic> items) {
        this(activity);
        setDataItems(items);
    }

    static class NearbyViewHolder extends RecyclerView.ViewHolder {

        View first_scenic;
        NetworkImageView first_image;
        TextView first_name;

//        View second_scenic;
//        NetworkImageView second_image;
//        TextView second_name;

        public NearbyViewHolder(View itemView) {
            super(itemView);
            first_scenic = itemView.findViewById(R.id.first_scenic);
            first_image = (NetworkImageView)itemView.findViewById(R.id.first_image);
            first_name = (TextView)itemView.findViewById(R.id.first_name);
//
//            second_scenic = itemView.findViewById(R.id.second_scenic);
//            second_image = (NetworkImageView)itemView.findViewById(R.id.second_image);
//            second_name = (TextView)itemView.findViewById(R.id.second_name);
        }
    }
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView authorAvatar;
        TextView author;
        TextView content;
        TextView commentCount;
        TextView voteCount;
        RatingBar rating;
        ImageView share;
        RecyclerView imagesRecyclerView;


        public CommentViewHolder(View itemView) {
            super(itemView);
            authorAvatar = (ImageView) itemView.findViewById(R.id.author_avatar);
            author = (TextView) itemView.findViewById(R.id.comment_author_name);
            content = (TextView) itemView.findViewById(R.id.comment_content);
            rating = (RatingBar) itemView.findViewById(R.id.comment_rating);
            commentCount = (TextView) itemView.findViewById(R.id.comment_count);
            voteCount = (TextView) itemView.findViewById(R.id.comment_vote_count);
            share = (ImageView) itemView.findViewById(R.id.comment_share);
            imagesRecyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerview);
        }

        public void setDataTag(Comment comment) {
            itemView.setTag(comment);
            commentCount.setTag(comment);
            voteCount.setTag(comment);
            share.setTag(comment);
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView coverImage;
        TextView introTitle;
        TextView introSummary;
        TextView wishToGo;
        View introContainer;
        ExpandableTextView scenicInfo;
        TextView surveyDetail;
        TextView comment;
        RelativeLayout comment_total;

        TextView scenicName;
        RatingBar scenicRating;

        View downloadPackage;
        View funcListen;
        View funcNavi;
        View funcLive;
        View funcService;

        View newComment;
        TextView nearbyCount;

        View weatherContainer;
        ImageView weatherIcon;
        TextView weatherText;
        TextView tempRange;
        TextView pm25;

        RecyclerView nearby_scenic;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            coverImage = (NetworkImageView) itemView.findViewById(R.id.scenic_cover_image);
            introTitle = (TextView) itemView.findViewById(R.id.scenic_intro_title);
            introSummary = (TextView) itemView.findViewById(R.id.scenic_intro_summary);
            wishToGo = (TextView) itemView.findViewById(R.id.scenic_wish_to_go);
            scenicInfo = (ExpandableTextView) itemView.findViewById(R.id.scenic_info);
            surveyDetail = (TextView) itemView.findViewById(R.id.survey_detail_tv);
            comment = (TextView) itemView.findViewById(R.id.comment);
            comment_total = (RelativeLayout) itemView.findViewById(R.id.comment_total);
            scenicName = (TextView) itemView.findViewById(R.id.scenic_name);
            scenicRating = (RatingBar) itemView.findViewById(R.id.scenic_rating);

            downloadPackage = itemView.findViewById(R.id.scenic_detail_download);
            funcListen = itemView.findViewById(R.id.scenic_detail_func_listen);
            funcNavi = itemView.findViewById(R.id.scenic_detail_func_navi);
            funcLive = itemView.findViewById(R.id.scenic_detail_func_live);
            funcService = itemView.findViewById(R.id.scenic_detail_func_service);
            introContainer = itemView.findViewById(R.id.scenic_intro_container);

            weatherContainer = itemView.findViewById(R.id.weather_container);
            weatherIcon = (ImageView) itemView.findViewById(R.id.weather_icon);
            weatherText = (TextView) itemView.findViewById(R.id.weather_text);
            tempRange = (TextView) itemView.findViewById(R.id.temp_range);
            pm25 = (TextView) itemView.findViewById(R.id.pm2_5);

            newComment = itemView.findViewById(R.id.scenic_new_comment);
            nearbyCount = (TextView) itemView.findViewById(R.id.nearby_count);

//            nearby_scenic = (RecyclerView) itemView.findViewById(R.id.nearby_scenic);

        }
    }

    @Override
    public ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_scenic_detail_header, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBinderHeaderViewHolder(ViewHolder holder) {
        QHScenic scenic = mHeader;
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        if (scenic.image_url != null) {
            headerHolder.coverImage.setDefaultImageResId(R.drawable.bg_image_hint)
                    .setErrorImageResId(R.drawable.bg_image_hint);
            headerHolder.coverImage.setImageUrl(scenic.image_url, RequestManager.getInstance()
                    .getImageLoader());
        }

        headerHolder.scenicName.setText(scenic.name);

        // headerHolder.introTitle.setText(scenic.introTitle);
        headerHolder.introTitle.setText(R.string.scenery_detail_play_title);
        String introText = scenic.intro_text+'\n'+"门票价格："+scenic.ticket_price+'\n'+"地址信息："+scenic.address;
        if (!TextUtils.isEmpty(introText)) {
            introText = introText.trim();
        }
        headerHolder.introSummary.setText("");
        headerHolder.scenicInfo.setText("");

        headerHolder.surveyDetail.setText(introText);
        headerHolder.comment.setText("评论"+mHeader.comment_count+"条");

//        GridLayoutManager layoutManager = new GridLayoutManager(mActivity,2);
//        headerHolder.nearby_scenic.setLayoutManager(layoutManager);
//        NearbyScenicAdapter nearbyScenicAdapter = new NearbyScenicAdapter(mActivity, mHeader.nearby_scenic);
//        headerHolder.nearby_scenic.setAdapter(nearbyScenicAdapter);
//        nearbyScenicAdapter.setDataItems(mHeader.nearby_scenic);
//        nearbyScenicAdapter.notifyDataSetChanged();

        attachClickListener(headerHolder, headerHolder.coverImage, 0);
        attachClickListener(headerHolder, headerHolder.wishToGo, 0);
        attachClickListener(headerHolder, headerHolder.downloadPackage, 0);
        attachClickListener(headerHolder, headerHolder.comment_total, 0);
        attachClickListener(headerHolder, headerHolder.newComment, 0);
        attachClickListener(headerHolder, headerHolder.funcListen, 0);
        attachClickListener(headerHolder, headerHolder.funcNavi, 0);
        attachClickListener(headerHolder, headerHolder.funcLive, 0);
        attachClickListener(headerHolder, headerHolder.funcService, 0);
        attachClickListener(headerHolder, headerHolder.introContainer, 0);
        attachClickListener(headerHolder, headerHolder.weatherContainer, 0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext().getApplicationContext();
        View view = View.inflate(context, R.layout.item_scenic_nearby, null);
        NearbyViewHolder holder = new NearbyViewHolder(view);
        return holder;

//        View v = LayoutInflater.from(context).inflate(R.layout.item_scenic_comment, parent, false);
//        CommentViewHolder holder = new CommentViewHolder(v);
//        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
//        holder.imagesRecyclerView.setLayoutManager(layoutManager);
//        int scap = ScreenUtils.dpToPxInt(context, 3);
//        DividerItemDecoration decor = new DividerItemDecoration(scap, scap);
//        decor.initWithRecyclerView(holder.imagesRecyclerView);
//        holder.imagesRecyclerView.addItemDecoration(decor);
//        holder.imagesRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        ImageGalleryAdapter adapter = new ImageGalleryAdapter(mActivity);
//        int screenPadding = ScreenUtils.getDimenPx(mActivity,
//                R.dimen.scenic_details_comment_image_padding);
//        adapter.setScreenPadding(screenPadding);
//        holder.imagesRecyclerView.setAdapter(adapter);
    }



    @Override
    public void onBinderItemViewHolder(ViewHolder holder, int position) {
        if (mDataItems.size() <= position) {
            Log.e(TAG, "[onBinderItemViewHolder] position out of bound");
            return;
        }

        final NearbyViewHolder nearbyViewHolder = (NearbyViewHolder) holder;
        String image_url = mDataItems.get(position).image_url;
        if(!TextUtils.isEmpty(image_url)){
            nearbyViewHolder.first_image.setDefaultImageResId(R.drawable.bg_image_hint);
            nearbyViewHolder.first_image.setDefaultImageResId(R.drawable.bg_image_hint);
            nearbyViewHolder.first_image.setImageUrl(image_url,RequestManager.getInstance().getImageLoader());
        }
        if(!TextUtils.isEmpty(mDataItems.get(position).name)){
            nearbyViewHolder.first_name.setText(mHeader.nearby_scenic.get(position).name);
        }
//
//        if (comment == null || comment.author == null) {
//            Log.e(TAG, "[onBinderItemViewHolder] comment: " + comment);
//            return;
//        }
//        final CommentViewHolder commentHolder = (CommentViewHolder) holder;
//        commentHolder.setDataTag(comment);
//        if (!TextUtils.isEmpty(comment.author.avatarUrl)) {
//            RequestManager
//                    .getInstance()
//                    .getImageLoader()
//                    .get(comment.author.avatarUrl,
//                            ImageLoader.getImageListener(commentHolder.authorAvatar,
//                                    R.drawable.bg_avata_hint,
//                                    R.drawable.bg_avata_hint));
//        }
//        commentHolder.author.setText(comment.author.name);
//        commentHolder.content.setText(comment.content);
//        commentHolder.commentCount.setText(String.valueOf(comment.commentCount));
//        commentHolder.voteCount.setText(String.valueOf(comment.voteCount));
//        if (comment.isVoted) {
//            commentHolder.voteCount.getCompoundDrawables()[0].setLevel(2);
//            commentHolder.voteCount.setEnabled(false);
//        } else {
//            commentHolder.voteCount.setEnabled(true);
//            commentHolder.voteCount.getCompoundDrawables()[0].setLevel(1);
//            attachClickListener(commentHolder, commentHolder.voteCount, position);
//        }
//        commentHolder.rating.setRating(comment.rating);
//
//        bindCommentImageRecyclerView(commentHolder.imagesRecyclerView, comment);
//        commentHolder.itemView.requestLayout();
//        attachClickListener(commentHolder, commentHolder.commentCount, position);
//        attachClickListener(commentHolder, commentHolder.itemView, position);
//        attachClickListener(commentHolder, commentHolder.share, position);
    }

    private void bindCommentImageRecyclerView(RecyclerView tWayView, Comment comment) {
        if (comment.images == null || comment.images.isEmpty()) {
            tWayView.setVisibility(View.GONE);
            return;
        }

        tWayView.setVisibility(View.VISIBLE);

        ItemClickSupport clickSupport = ItemClickSupport.addTo(tWayView);
        clickSupport.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                MobclickAgent.onEvent(mActivity, MobConst.ID_INDEX_SCENIC_COMMENT_IMAGE);
                List<TextImage> items = (List<TextImage>) parent.getTag();
                ArrayList<Image> imageList = new ArrayList<Image>();
                for (TextImage textImage : items) {
                    if (textImage != null && textImage.image != null) {
                        Image image = new Image();
                        image.imagePath = Uri.parse(textImage.image.largeImage);
                        image.thumbnailPath = Uri.parse(textImage.image.smallImage);
                        imageList.add(image);
                    }
                }

                Intent intent = new Intent(mActivity,
                        PhotoPreviewActivity.class);
                intent.putExtra(Const.EXTRA_IMAGE_DATA, imageList);
                intent.putExtra(Const.EXTRA_IMAGE_PREVIEW_START_INDEX,
                        position);
                mActivity.startActivity(intent);
            }
        });

        List<TextImage> imageList = new ArrayList<TextImage>();
        for (CompoundImage i : comment.images) {
            TextImage textImage = new TextImage(i, null, null);
            imageList.add(textImage);
        }

        ImageGalleryAdapter adapter = (ImageGalleryAdapter) tWayView.getAdapter();
        adapter.setDataItems(imageList);
        tWayView.setTag(imageList);

        int scap = ScreenUtils.dpToPxInt(mActivity, 3);
        int padding = ScreenUtils.getDimenPx(mActivity,
                R.dimen.scenic_details_comment_image_padding);
        int itemHeight = ((ScreenUtils.getScreenWidth(mActivity) - scap * 2)
                - padding * 2) / 3;
        int rowCount = (imageList.size() + 2) / 3;
        int height = itemHeight * rowCount + scap * (rowCount - 1)
                + padding * 2;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                height);
        tWayView.setLayoutParams(lp);
        tWayView.setPadding(padding, padding, padding, padding);
    }
}
