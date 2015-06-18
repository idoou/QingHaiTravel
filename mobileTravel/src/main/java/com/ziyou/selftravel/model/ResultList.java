
package com.ziyou.selftravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ResultList<T> {
    public int count;
    public String next;
    public String previous;
    public List<T> results = new ArrayList<T>();

    @SerializedName("pagination")
    public Pagination pagination;

    @SerializedName("list")
    public List<T> list = new ArrayList<T>();


}
