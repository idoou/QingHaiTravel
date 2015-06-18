package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/26.
 */
public class QHUser implements Parcelable{
    public int id;
    public String url;
    public String username;
    public String email;
    public List<String> groups = new ArrayList();
    public QHUserInfo user_info;



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.url);
        dest.writeString(this.username);
        dest.writeString(this.email);
        dest.writeList(this.groups);
        dest.writeParcelable(this.user_info, flags);
    }

    public static final Parcelable.Creator<QHUser> CREATOR = new Parcelable.Creator<QHUser>() {
        public QHUser createFromParcel(Parcel source) {
            return new QHUser(source);
        }

        public QHUser[] newArray(int size) {
            return new QHUser[size];
        }
    };
    public String toString() {
        return "QHUser [id=" + id + ", url=" + url + ", username=" + username
                + ", email=" + email + ", user_info=" + user_info +"]";
    }

    public QHUser(Parcel in) {
        this.id = in.readInt();
        this.url = in.readString();
        this.username = in.readString();
        this.email = in.readString();
//        this.groups = in.readArrayList();
//        in.readList(groups,QHUser.class.getClassLoader());
        this.groups = in.createStringArrayList();
        this.user_info = in.readParcelable(QHUser.class.getClassLoader());
    }


}
