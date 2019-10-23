/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.custom.swiperefreshlist;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.google.android.material.snackbar.Snackbar;

import de.domjos.unitrackerlibrary.model.ListObject;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.settings.Settings;

public class SwipeRefreshDeleteList extends LinearLayout {
    private Context context;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private ReloadListener reloadListener;
    private DeleteListener deleteListener;
    private ClickListener clickListener;
    private LinearLayoutManager manager;
    private Snackbar snackbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout linearLayout;

    public SwipeRefreshDeleteList(@NonNull Context context) {
        super(context);
        this.context = context;
        this.initDefault();
        this.initAdapter();
    }

    public SwipeRefreshDeleteList(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        this.initDefault();
        this.initAdapter();
    }

    public RecyclerAdapter getAdapter() {
        return this.adapter;
    }

    private void initDefault() {
        this.setOrientation(VERTICAL);

        this.swipeRefreshLayout = new SwipeRefreshLayout(this.context);
        LinearLayout.LayoutParams layoutParamsForRefreshLayout =  new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParamsForRefreshLayout.weight = 10;
        this.swipeRefreshLayout.setLayoutParams(layoutParamsForRefreshLayout);

        this.recyclerView = new RecyclerView(this.context);
        this.recyclerView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.swipeRefreshLayout.addView(this.recyclerView);
        this.addView(this.swipeRefreshLayout);

        this.linearLayout = new LinearLayout(this.context);
        this.linearLayout.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsForControls = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
        this.linearLayout.setLayoutParams(layoutParamsForControls);
        this.linearLayout.setVisibility(GONE);

        ImageButton cmdDelete = new ImageButton(this.context);
        cmdDelete.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_delete_black_24dp, null));
        cmdDelete.setBackground(null);
        cmdDelete.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cmdDelete.setOnClickListener((event) -> {
            ReloadListener tmp = this.reloadListener;
            this.reloadListener = null;
            for(int i = 0; i<=this.adapter.getItemCount()-1; i++) {
                ListObject obj = this.adapter.getItem(i);
                if(obj.isSelected()) {
                    if(this.deleteListener!=null) {
                        this.deleteListener.onDelete(obj);
                    }
                }
            }
            this.reloadListener = tmp;
            if(this.reloadListener!=null) {
                this.reloadListener.onReload();
            }
        });
        this.linearLayout.addView(cmdDelete);

        ImageButton cmdTags = new ImageButton(this.context);
        cmdTags.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_style_black_24dp, null));
        cmdTags.setBackground(null);
        cmdTags.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cmdTags.setOnClickListener(event -> {
            Settings settings = MainActivity.GLOBALS.getSettings(context);
            String tags = Helper.showTagDialog(((Activity)context), Helper.getCurrentBugService(context), settings.showNotifications(), settings.getCurrentProjectId());

            for(int i = 0; i<=this.adapter.getItemCount()-1; i++) {
                ListObject obj = this.adapter.getItem(i);
                if(obj.isSelected()) {

                }
            }
        });
        this.linearLayout.addView(cmdTags);

        this.addView(this.linearLayout);

        this.snackbar = Snackbar.make(((Activity)context).findViewById(android.R.id.content), R.string.sys_item_deleted, Snackbar.LENGTH_SHORT);
    }

    private void initAdapter() {
        this.adapter = new RecyclerAdapter(this.recyclerView, (Activity) this.context, this.linearLayout);
        this.recyclerView.setAdapter(this.adapter);
        this.manager = new LinearLayoutManager(this.context);
        this.recyclerView.setLayoutManager(this.manager);
        this.adapter.notifyDataSetChanged();

        this.adapter.onSwipeListener(new SwipeToDeleteCallback(this.context) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final boolean[] rollBack = {false};
                ListObject listObject = getAdapter().getItem(viewHolder.getAdapterPosition());
                if (viewHolder.getAdapterPosition() != -1) {
                    getAdapter().deleteItem(viewHolder.getAdapterPosition());
                }
                snackbar.setAction(R.string.sys_undone, v -> {
                    getAdapter().add(listObject);
                    rollBack[0] = true;
                });
                Snackbar.Callback callback = new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if(!rollBack[0]) {
                            if (deleteListener != null) {
                                deleteListener.onDelete(listObject);
                            }
                            if (reloadListener != null) {
                                reloadListener.onReload();
                            }
                        }
                    }

                    @Override
                    public void onShown(Snackbar snackbar){}
                };
                snackbar.addCallback(callback);
                snackbar.show();
            }
        });

        this.adapter.setClickListener(v -> {
            int position = this.recyclerView.indexOfChild(v);
            int firstPosition = this.manager.findFirstVisibleItemPosition();
            if (clickListener != null) {
                clickListener.onClick(this.adapter.getItem(firstPosition + position));
            }
        });

        this.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (this.reloadListener != null) {
                this.reloadListener.onReload();
            }
            this.swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void reload(ReloadListener reloadListener) {
        this.reloadListener = reloadListener;
        this.adapter.reload(this.reloadListener);
    }

    public void delete(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void click(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setContextMenu(int menuId) {
        this.adapter.setContextMenu(menuId);
    }

    public abstract static class ReloadListener {
        public abstract void onReload();
    }

    public abstract static class DeleteListener {
        public abstract void onDelete(ListObject listObject);
    }

    public abstract static class ClickListener {
        public abstract void onClick(ListObject listObject);
    }
}
