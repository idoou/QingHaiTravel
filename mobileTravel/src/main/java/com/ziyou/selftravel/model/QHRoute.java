package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/25.
 */
public class QHRoute implements Parcelable {
    public int id;
    public String title;
    public int ctype;
    public String intro_text;
    public String cover_image;
    public int vote_count;
    public int comment_count;
    public int voted;
    public int collected;
    public List<QHRouteDay> route_day = new ArrayList<QHRouteDay>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.ctype);
        dest.writeString(this.intro_text);
        dest.writeString(this.cover_image);
        dest.writeInt(this.vote_count);
        dest.writeInt(this.comment_count);
        dest.writeInt(this.voted);
        dest.writeInt(this.collected);
        dest.writeList(this.route_day);

    }

    public static final Parcelable.Creator<QHRoute> CREATOR = new Parcelable.Creator<QHRoute>() {
        public QHRoute createFromParcel(Parcel source) {
            return new QHRoute(source);
        }

        public QHRoute[] newArray(int size) {
            return new QHRoute[size];
        }
    };

    public QHRoute(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.ctype = in.readInt();
        this.intro_text = in.readString();
        this.cover_image = in.readString();
        this.vote_count = in.readInt();
        this.comment_count = in.readInt();
        this.voted = in.readInt();
        this.collected = in.readInt();
        in.readList(route_day,QHRoute.class.getClassLoader());
    }

    public class QHRouteList extends ResultList<QHRoute> {

    }

//    public static class QHRouteDay  implements Parcelable{
//        public int id;
//        public int route;
//        public int scenic;
//        public int day;
//        public String name;
//        public String image_url;
//        public String intro_title;
//        public String intro_text;
//
//        public int itemType = 0;//0 item,1 header ,2 date
//        public String date  ="";//日期
//        public int  index = -1;//第几个item
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeInt(this.id);
//            dest.writeInt(this.route);
//            dest.writeInt(this.scenic);
//            dest.writeInt(this.day);
//            dest.writeString(this.name);
//            dest.writeString(this.image_url);
//            dest.writeString(this.intro_title);
//            dest.writeString(this.intro_text);
//        }
//        public QHRouteDay(){}
//
//        public QHRouteDay(Parcel in) {
//            this.id = in.readInt();
//            this.route = in.readInt();
//            this.scenic = in.readInt();
//            this.day = in.readInt();
//            this.name = in.readString();
//            this.image_url = in.readString();
//            this.intro_title = in.readString();
//            this.intro_text = in.readString();
//        }
//
//        public static final Parcelable.Creator<QHRouteDay> CREATOR = new Parcelable.Creator<QHRouteDay>() {
//            public QHRouteDay createFromParcel(Parcel source) {
//                return new QHRouteDay(source);
//            }
//
//            public QHRouteDay[] newArray(int size) {
//                return new QHRouteDay[size];
//            }
//        };
//    }
}

