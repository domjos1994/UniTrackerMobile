/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;

import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unitrackermobile.R;

public class RecyclerAdapter extends Adapter<RecyclerAdapter.RecycleViewHolder> {
    private ArrayList<ListObject> data;
    private View.OnClickListener mClickListener;
    private RecyclerView recyclerView;

    class RecycleViewHolder extends ViewHolder {
        private TextView mTitle, mSubTitle;
        private ImageView ivIcon;

        RecycleViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.lblTitle);
            mSubTitle = itemView.findViewById(R.id.lblSubTitle);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }

    RecyclerAdapter(RecyclerView recyclerView) {
        this.data = new ArrayList<>();
        this.recyclerView = recyclerView;
    }

    @Override
    @NonNull
    public RecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new RecycleViewHolder(itemView);
    }

    void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

    void onSwipeListener(SwipeToDeleteCallback callback) {
        new ItemTouchHelper(callback).attachToRecyclerView(this.recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).getDescriptionObject().getTitle());
        holder.mSubTitle.setText(data.get(position).getDescriptionObject().getDescription());
        holder.ivIcon.setImageDrawable(data.get(position).getIcon());
        holder.itemView.setOnClickListener(view -> {
            if (mClickListener != null) {
                mClickListener.onClick(view);
            }
        });
    }

    public ListObject getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void deleteItem(int position) {
        this.data.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        int size = data.size();
        if (size > 0) {
            data.subList(0, size).clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    public void add(ListObject object) {
        data.add(object);

        synchronized (this) {
            notify();
        }
    }
}
