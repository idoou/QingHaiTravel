package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/25.
 */
public class QHCollectionStrategy implements Parcelable {

    public int id;
    public int obj_id;
    public int obj_type;
    public String created;
    public int user;
    public String title;
    public String image_url;
    public int vote_count;




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.obj_id);
        dest.writeInt(this.obj_type);
        dest.writeString(this.created);
        dest.writeInt(this.user);
        dest.writeString(this.title);
        dest.writeString(this.image_url);
        dest.writeInt(this.vote_count);
    }
    public QHCollectionStrategy(Parcel in) {
        this.id = in.readInt();
        this.obj_id = in.readInt();
        this.obj_type = in.readInt();
        this.created = in.readString();
        this.user = in.readInt();
        this.title = in.readString();
        this.image_url = in.readString();
        this.vote_count = in.readInt();
    }

    public static final Creator<QHCollectionStrategy> CREATOR = new Creator<QHCollectionStrategy>() {
        public QHCollectionStrategy createFromParcel(Parcel source) {
            return new QHCollectionStrategy(source);
        }

        public QHCollectionStrategy[] newArray(int size) {
            return new QHCollectionStrategy[size];
        }
    };

    public class List extends ResultList<QHCollectionStrategy> {

    }

}
