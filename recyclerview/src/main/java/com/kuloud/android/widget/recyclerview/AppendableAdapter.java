
package com.kuloud.android.widget.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class AppendableAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    protected List<T> mDataItems = new ArrayList<T>();

    public void setDataItems(List<T> items) {
        mDataItems.clear();
        if (items != null) {
            mDataItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void appendDataItems(List<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        if (mDataItems.isEmpty()) {
            mDataItems.addAll(items);
            notifyDataSetChanged();
        } else {
            int positionStart = mDataItems.size();
            mDataItems.addAll(items);
            notifyItemRangeInserted(positionStart, items.size());
        }
    }

    @Override
    public int getItemCount() {
        return mDataItems == null ? 0 : mDataItems.size();
    }

    public List<T> getDataItems() {
        return mDataItems;
    }

}
