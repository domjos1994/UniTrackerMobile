/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
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

package de.domjos.unitrackerlibrary.custom;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.model.BaseDescriptionObject;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;

/** @noinspection unused*/
public class SwipeRefreshDeleteList extends LinearLayout {
    private Context context;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private ReloadListener reloadListener;
    private DeleteListener deleteListener;
    private SingleClickListener clickListener;
    private LinearLayoutManager manager;
    private Drawable icon;
    private Drawable background, backgroundStatePositive, selectedBackground;
    private Drawable divider;
    private boolean readOnly;
    private int color, iconColor;
    private Snackbar snackbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout footerButtonsArea, footerArea;
    private boolean scrollList = false;
    boolean showCheckboxes = false;

    private SwipeToDeleteCallback callback;

    public SwipeRefreshDeleteList(@NonNull Context context) {
        super(context);

        this.icon = null;
        this.background = null;
        this.selectedBackground = null;
        this.divider = null;
        this.readOnly = false;
        this.context = context;
        this.initDefault();
        this.initAdapter();
    }

    public SwipeRefreshDeleteList(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;

        try(TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.SwipeRefreshDeleteList, 0, 0)) {
            try {
                this.icon = a.getDrawable(R.styleable.SwipeRefreshDeleteList_itemIcon);
            } catch (Exception ex) {
                this.icon = null;
            }

            try {
                this.background = a.getDrawable(R.styleable.SwipeRefreshDeleteList_listItemBackground);
            } catch (Exception ex) {
                this.background = null;
            }

            try {
                this.backgroundStatePositive = a.getDrawable(R.styleable.SwipeRefreshDeleteList_listItemBackgroundStatePositive);
            } catch (Exception ex) {
                if (this.background != null) {
                    this.backgroundStatePositive = this.background;
                }
            }

            try {
                this.selectedBackground = a.getDrawable(R.styleable.SwipeRefreshDeleteList_selectedListItemBackground);
            } catch (Exception ex) {
                this.selectedBackground = null;
            }

            try {
                this.divider = a.getDrawable(R.styleable.SwipeRefreshDeleteList_listItemDivider);
            } catch (Exception ex) {
                this.divider = null;
            }

            try {
                this.readOnly = a.getBoolean(R.styleable.SwipeRefreshDeleteList_readOnly, false);
            } catch (Exception ex) {
                this.readOnly = false;
            }

            try {
                this.color = a.getColor(R.styleable.SwipeRefreshDeleteList_listItemForeground, ContextCompat.getColor(this.context, android.R.color.white));
            } catch (Exception ex) {
                this.color = ContextCompat.getColor(this.context, android.R.color.white);
            }

            try {
                this.iconColor = a.getColor(R.styleable.SwipeRefreshDeleteList_iconColor, ContextCompat.getColor(this.context, android.R.color.white));
            } catch (Exception ex) {
                this.iconColor = ContextCompat.getColor(this.context, android.R.color.white);
            }
        }

        this.initDefault();
        this.initAdapter();
    }

    public void setScrollList(boolean scrollList) {
        this.scrollList = scrollList;
        this.adapter.setScrollList(this.scrollList);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        this.recyclerView.setEnabled(!this.readOnly);
        this.callback.setReadOnly(this.readOnly);
    }

    public RecyclerAdapter getAdapter() {
        return this.adapter;
    }

    private Activity scanForActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    private void initDefault() {
        this.showCheckboxes = false;

        this.setOrientation(VERTICAL);
        this.context = this.scanForActivity(this.context);

        this.swipeRefreshLayout = new SwipeRefreshLayout(Objects.requireNonNull(this.context));
        LinearLayout.LayoutParams layoutParamsForRefreshLayout =  new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParamsForRefreshLayout.weight = 10;
        this.swipeRefreshLayout.setLayoutParams(layoutParamsForRefreshLayout);

        this.recyclerView = new RecyclerView(this.context);
        this.recyclerView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.swipeRefreshLayout.addView(this.recyclerView);
        this.addView(this.swipeRefreshLayout);

        this.footerArea = new LinearLayout(this.context);
        this.footerArea.setOrientation(HORIZONTAL);
        this.footerArea.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ConvertHelper.convertDPToPixels(48, this.context)));
        this.footerArea.setWeightSum(10);
        this.footerArea.setVisibility(GONE);

        this.footerButtonsArea = new LinearLayout(this.context);
        this.footerButtonsArea.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsForControls = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        layoutParamsForControls.weight = 9;
        this.footerButtonsArea.setLayoutParams(layoutParamsForControls);

        if(this.getContentDescription() != null) {
            this.swipeRefreshLayout.setContentDescription(this.getContentDescription());
            this.recyclerView.setContentDescription(this.getContentDescription());
        }

        ImageButton cmdDelete = new ImageButton(this.context);
        cmdDelete.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_delete, null));
        cmdDelete.setColorFilter(this.iconColor);
        cmdDelete.setBackground(null);
        cmdDelete.setContentDescription(this.context.getString(R.string.sys_no_entry));
        cmdDelete.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cmdDelete.setOnClickListener((event) -> {
            ReloadListener tmp = this.reloadListener;
            this.reloadListener = null;
            List<BaseDescriptionObject> baseDescriptionObjects = new LinkedList<>();
            for(int i = 0; i<=this.adapter.getItemCount()-1; i++) {
                BaseDescriptionObject obj = this.adapter.getItem(i);
                if(obj != null && obj.isSelected() && this.deleteListener!=null) {
                    baseDescriptionObjects.add(obj);
                }
            }
            for(BaseDescriptionObject obj : baseDescriptionObjects) {
                this.deleteListener.onDelete(obj);
            }

            this.reloadListener = tmp;
            if(this.reloadListener!=null) {
                this.reloadListener.onReload();
            }
        });
        cmdDelete.setVisibility(this.readOnly ? GONE : VISIBLE);


        this.footerButtonsArea.addView(cmdDelete);
        this.footerArea.addView(this.footerButtonsArea);

        CheckBox chkSelectAll = new CheckBox(this.context);
        chkSelectAll.setContentDescription(this.context.getString(R.string.sys_multiple));
        LinearLayout.LayoutParams layoutParamsForAll = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        layoutParamsForAll.weight = 1;
        chkSelectAll.setLayoutParams(layoutParamsForAll);
        this.footerArea.addView(chkSelectAll);

        chkSelectAll.setOnCheckedChangeListener((compoundButton, b) -> this.adapter.selectAll(b));

        this.addView(this.footerArea);

        try {
            this.snackbar = Snackbar.make(((Activity) context).findViewById(android.R.id.content), R.string.sys_item_deleted, Snackbar.LENGTH_SHORT);
        } catch (Exception ex) {
            this.snackbar = null;
        }

        try {
            this.addLabels();
        } catch (Exception ignored) {}
    }

    private void addLabels() {
        CharSequence description = this.getContentDescription();
        if(description != null && !description.toString().trim().isEmpty()) {
            this.swipeRefreshLayout.setContentDescription(description);
            this.recyclerView.setContentDescription(description);
            this.footerButtonsArea.setContentDescription(description);
            this.footerArea.setContentDescription(description);
        }
    }

    private void initAdapter() {
        this.adapter = new RecyclerAdapter(this.recyclerView, (Activity) this.context, this.icon, this.background, this.backgroundStatePositive, this.footerArea, this.readOnly, this.color, this.showCheckboxes, this.scrollList);
        this.recyclerView.setAdapter(this.adapter);
        this.manager = new LinearLayoutManager(this.context);
        this.recyclerView.setLayoutManager(this.manager);
        if(this.divider!=null) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.context, this.manager.getOrientation());
            dividerItemDecoration.setDrawable(this.divider);
            this.recyclerView.addItemDecoration(dividerItemDecoration);
        }
        this.recyclerView.setEnabled(!this.readOnly);
        this.adapter.notifyItemRangeChanged(0, this.adapter.getItemCount());

        this.callback = new SwipeToDeleteCallback(this.context, this.readOnly) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final boolean[] rollBack = {false};
                BaseDescriptionObject baseDescriptionObject = getAdapter().getItem(viewHolder.getAdapterPosition());
                if (viewHolder.getAdapterPosition() != -1) {
                    getAdapter().deleteItem(viewHolder.getAdapterPosition());
                }
                if(snackbar != null) {
                    snackbar.setAction(R.string.sys_item_undo, v -> {
                        getAdapter().add(baseDescriptionObject);
                        rollBack[0] = true;
                    });
                    Snackbar.Callback callback = new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (!rollBack[0] && deleteListener != null) {
                                deleteListener.onDelete(baseDescriptionObject);
                            }
                        }
                    };
                    snackbar.addCallback(callback);
                    snackbar.show();
                }
            }
        };

        if(this.readOnly) {
            this.adapter.onSwipeListener(null);
        } else {
            this.adapter.onSwipeListener(this.callback);
        }

        this.adapter.setClickListener(v -> {
            int position = this.recyclerView.indexOfChild(v);
            int firstPosition = this.manager.findFirstVisibleItemPosition();
            int currentPosition = firstPosition + position;

            if(this.selectedBackground != null) {
                this.adapter.resetLastView();

                if(currentPosition!=this.adapter.noEntryItem) {
                    this.adapter.setSelectedView(v, this.adapter.getItem(currentPosition).isState());
                } else {
                    this.adapter.setSelectedView(v, false);
                }

                v.setBackground(this.selectedBackground);
            }

            if (clickListener != null && currentPosition!=this.adapter.noEntryItem) {
                clickListener.onClick(this.adapter.getItem(currentPosition));
            }
        });

        this.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (this.reloadListener != null) {
                this.reloadListener.onReload();
            }
            this.swipeRefreshLayout.setRefreshing(false);
        });
    }

    public void select(BaseDescriptionObject baseDescriptionObject) {
        int position = this.adapter.getItemPosition(baseDescriptionObject);
        this.recyclerView.scrollToPosition(position);

        this.postDelayed(() -> {
            View view = this.manager.findViewByPosition(position);
            if(view != null) {
                view.performClick();
            }
        }, 50);
    }

    public void setOnReloadListener(ReloadListener reloadListener) {
        this.reloadListener = reloadListener;
        this.adapter.reload(this.reloadListener);
    }

    public void setOnDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setOnClickListener(SingleClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addButtonClick(int drawableId, String hint, MultiClickListener clickListener) {
        ImageButton imageButton = new ImageButton(this.context);
        imageButton.setImageDrawable(VectorDrawableCompat.create(context.getResources(), drawableId, null));
        imageButton.setBackground(null);
        imageButton.setContentDescription(hint);
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageButton.setOnClickListener(event -> {
            List<BaseDescriptionObject> listObjects = new LinkedList<>();
            for(int i = 0; i<=this.adapter.getItemCount()-1; i++) {
                BaseDescriptionObject obj = this.adapter.getItem(i);
                if(obj != null && obj.isSelected()) {
                    listObjects.add(obj);
                }
            }
            if(clickListener!=null) {
                clickListener.onClick(listObjects);
            }
        });
        this.footerButtonsArea.addView(imageButton);
    }

    public void setContextMenu(int menuId) {
        this.adapter.setContextMenu(menuId);
    }

    @FunctionalInterface
    public interface ReloadListener {
        void onReload();
    }

    @FunctionalInterface
    public interface DeleteListener {
        void onDelete(BaseDescriptionObject listObject);
    }

    @FunctionalInterface
    public interface SingleClickListener {
        void onClick(BaseDescriptionObject listObject);
    }

    @FunctionalInterface
    public interface MultiClickListener {
        void onClick(List<BaseDescriptionObject> objectList);
    }
}
