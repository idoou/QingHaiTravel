package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/5/21.
 */
public class QHScenic implements Parcelable {

    public int id;
    public String name;
    public String image_url;
    public float longitude;
    public float latitude;
    public String intro_title;
    public String intro_text;
    public int comment_count;
    public String address;
    public String ticket_price;
    public List<QHVideo> scenic_video = new ArrayList<QHVideo>();
    public List<QHScenic> nearby_scenic = new ArrayList<QHScenic>();

    @Override
    public int describeContents() {
        return 0;
    }

    public  QHScenic(){

     }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.image_url);
        dest.writeFloat(this.longitude);
        dest.writeFloat(this.latitude);
        dest.writeString(this.intro_title);
        dest.writeString(this.intro_text);
        dest.writeInt(this.comment_count);
        dest.writeString(this.address);
        dest.writeString(this.ticket_price);
        dest.writeList(this.scenic_video);
        dest.writeList(this.nearby_scenic);
    }
    public QHScenic(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.image_url = in.readString();
        this.longitude = in.readFloat();
        this.latitude = in.readFloat();
        this.intro_title = in.readString();
        this.intro_text = in.readString();
        this.comment_count = in.readInt();
        this.address = in.readString();
        this.ticket_price = in.readString();
        in.readList(scenic_video,QHScenic.class.getClassLoader());
        in.readList(nearby_scenic,QHScenic.class.getClassLoader());
    }


    public static class QHVideo implements Parcelable{

        public int id;
        public String video_day;
        public String video_night;
        public String image_url;

        public QHVideo() {

        }

        public class QHVideoList extends ResultList<QHVideo> {

        }

        public QHVideo(Parcel in) {
            this.id = in.readInt();
            this.video_day = in.readString();
            this.video_night = in.readString();
            this.image_url = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.id);
            dest.writeString(this.video_day);
            dest.writeString(this.video_night);
            dest.writeString(this.image_url);
        }

        public static final Parcelable.Creator<QHVideo> CREATOR = new Parcelable.Creator<QHVideo>() {
            public QHVideo createFromParcel(Parcel source) {
                return new QHVideo(source);
            }

            public QHVideo[] newArray(int size) {
                return new QHVideo[size];
            }
        };

        @Override
        public String toString() {
            return "Trip [id=" + id + ", video_day=" + video_day + ", video_night=" + video_night
                    + ", image_url=" + image_url + "]";
        }
    }
    public class QHScenicList extends ResultList<QHScenic> {

    }

    @Override
    public String toString() {
        return "Trip [id=" + id + ", name=" + name + ", image_url=" + image_url
                + ", longitude=" + longitude + ", latitude=" + latitude + ", intro_title="
                + intro_title + ", intro_text=" + intro_text + ", address=" + address + ", ticket_price=" + ticket_price+ ", scenic_video=" + scenic_video+"]";
    }

    public static final Parcelable.Creator<QHScenic> CREATOR = new Parcelable.Creator<QHScenic>() {
        public QHScenic createFromParcel(Parcel source) {
            return new QHScenic(source);
        }

        public QHScenic[] newArray(int size) {
            return new QHScenic[size];
        }
    };

}
