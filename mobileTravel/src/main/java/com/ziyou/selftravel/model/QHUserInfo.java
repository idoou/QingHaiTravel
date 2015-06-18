package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/6/1.
 */
public class QHUserInfo implements Parcelable{

    public String nickname ="";
    public int gender;
    public String avatar_url="";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nickname);
        dest.writeInt(this.gender);
        dest.writeString(this.avatar_url);
        if(null==this.avatar_url)this.avatar_url= "";
    }
    public static final Parcelable.Creator<QHUserInfo> CREATOR = new Parcelable.Creator<QHUserInfo>() {
        public QHUserInfo createFromParcel(Parcel source) {
            return new QHUserInfo(source);
        }

        public QHUserInfo[] newArray(int size) {
            return new QHUserInfo[size];
        }
    };
    public QHUserInfo(Parcel in) {
        this.nickname = in.readString();
        this.gender = in.readInt();
        this.avatar_url = in.readString();
        if(null==this.avatar_url)this.avatar_url= "";
    }
}