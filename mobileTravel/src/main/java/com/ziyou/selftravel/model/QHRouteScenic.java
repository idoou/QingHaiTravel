package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/5/21.
 */
public class QHRouteScenic implements Parcelable {

//    "intro_text": "\u5854\u5c14\u5bfa\u662f\u6211\u56fd\u8457\u540d\u7684\u5587\u561b\u5bfa\u9662\uff0c\u662f\u5587\u561b\u6559\u9ec4\u6559\u521b\u59cb\u4eba\u5b97\u5580\u5df4\u8bde\u751f\u5730\uff0c\u4ea6\u662f\u897f\u5317\u5730\u533a\u4f5b\u6559\u6d3b\u52a8\u7684\u4e2d\u5fc3\u3002\u8be5\u5bfa\u89c4\u6a21\u5b8f\u4f1f\uff0c\u6700\u76db\u65f6\u6709\u6bbf\u5802\u516b\u767e\u591a\u95f4\uff0c\u662f\u6211\u56fd\u8457\u540d\u7684\u516d\u5927\u5587\u561b\u5bfa\u4e4b\u4e00\uff08\u5176\u4f59\u4e94\u5bfa\u4e3a\u897f\u85cf\u7684\u8272\u62c9\u5bfa\u3001\u54f2\u868c\u5bfa\u3001\u624e\u4ec0\u4f26\u5e03\u5bfa\u3001\u7518\u4e39\u5bfa\u548c\u7518\u8083\u7684\u62c9\u535c\u695e\u5bfa\uff09\uff0c\u5728\u5168\u56fd\u548c\u4e1c\u5357\u4e9a\u4e00\u5e26\u4eab\u6709\u76db\u540d\u3002\u3000\u5854\u5c14\u5bfa\u81f3\u4eca\u5df2\u6709400\u591a\u5e74\u5386\u53f2\u3002\u5854\u5c14\u5bfa\u4f9d\u5c71\u52bf\u8d77\u4f0f\uff0c\u662f\u7531\u5927\u91d1\u74e6\u5bfa\u3001\u5c0f\u91d1\u74e6\u5bfa\u3001\u5927\u7ecf\u5802\u3001\u5927\u53a8\u623f\u3001\u4e5d\u95f4\u6bbf\u3001\u5927\u62c9\u6d6a\u3001\u5982\u610f\u5b9d\u5854\u3001\u592a\u5e73\u5854\u3001\u83e9\u63d0\u5854\u3001\u8fc7\u95e8\u5854\u7b49\u8bb8\u591a\u5bab\u6bbf\u3001\u7ecf\u5802\u3001\u4f5b\u5854\u5bfa\u7ec4\u6210\u7684\u4e00\u4e2a\u6c14\u52bf\u5b8f\u4f1f\uff0c\u85cf\u6c49\u827a\u672f\u98ce\u683c\u76f8\u7ed3\u5408\u7684\u53e4\u5efa\u7b51\u7fa4\u3002 \u5bfa\u9662\u6bbf\u5b87\u76f8\u8fde\uff0c\u767d\u5854\u6797\u7acb\uff0c\u6574\u5ea7\u5bfa\u4e0d\u4ec5\u9020\u578b\u72ec\u7279\uff0c\u5bcc\u4e8e\u521b\u9020\u6027\uff0c\u800c\u4e14\u7ec6\u90e8\u88c5\u9970\u4e5f\u8fbe\u5230\u4e86\u9ad8\u8d85\u7684\u827a\u672f\u6c34\u5e73\u3002\u5bfa\u5185\u7684\u9165\u6cb9\u82b1\u3001\u58c1\u753b\u548c\u5806\u7ee3\uff0c\u88ab\u79f0\u4e3a\u201c\u5854\u5c14\u5bfa\u4e09\u7edd\u201d\uff0c\u5177\u72ec\u7279\u7684\u6c11\u65cf\u98ce\u683c\u548c\u5f88\u9ad8\u7684\u827a\u672f\u4ef7\u503c\u3002\u6bcf\u5e74\u519c\u5386\u6b63\u6708\u3001\u56db\u6708\u3001\u516d\u6708\u3001\u4e5d\u6708\uff0c\u5854\u5c14\u5bfa\u4e3e\u884c\u56db\u5927\u6cd5\u4f1a\u3002\u5341\u6708\u3001\u4e8c\u6708\u4e3e\u884c\u4e24\u4e2a\u5c0f\u6cd5\u4f1a\u3002\u5854\u5c14\u5bfa\u5185\u4e0d\u5141\u8bb8\u6e38\u5ba2\u62cd\u7167\u3002",
//            "image_url": "http://182.92.218.24:9000/media/scenic/fc07ef4dc83dd305b30c9acf8fa7e60b.jpg",
//            "id": 2,
//            "intro_title": "\u5854\u5c14\u5bfa",
//            "name": "\u5854\u5c14\u5bfa"

    public String intro_text;
    public String image_url;
    public int id;
    public String intro_title;
    public String name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.intro_text);
        dest.writeString(this.image_url);
        dest.writeInt(this.id);
        dest.writeString(this.intro_title);
        dest.writeString(this.name);
    }

    public QHRouteScenic(Parcel in) {
        this.intro_text = in.readString();
        this.image_url = in.readString();
        this.id = in.readInt();
        this.intro_title = in.readString();
        this.name = in.readString();
    }

    public static final Creator<QHRouteScenic> CREATOR = new Creator<QHRouteScenic>() {
        public QHRouteScenic createFromParcel(Parcel source) {
            return new QHRouteScenic(source);
        }

        public QHRouteScenic[] newArray(int size) {
            return new QHRouteScenic[size];
        }
    };

}
