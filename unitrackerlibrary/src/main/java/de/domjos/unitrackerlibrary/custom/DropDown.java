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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import de.domjos.unitrackerlibrary.R;

public class DropDown<T> extends LinearLayout {
    private String title;
    private TextInputLayout inputLayout;
    private AutoCompleteTextView txt;
    private OnSelectionChange onSelectionChange;

    public DropDown(@NonNull Context context) {
        super(context);

        this.initControls(context);
        this.setTitle(context, null);
        this.setData();
    }

    public DropDown(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initControls(context);
        this.setTitle(context, attrs);
        this.setData();
    }

    public DropDown(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.initControls(context);
        this.setTitle(context, attrs);
        this.setData();
    }

    public void setCustomIcon(int drawable, String descr, Runnable action) {
        this.inputLayout.setStartIconDrawable(drawable);
        this.inputLayout.setStartIconContentDescription(descr);
        this.inputLayout.setStartIconVisible(true);
        if(action != null) {
            this.inputLayout.setStartIconOnClickListener(view -> action.run());
        }
    }

    public final void setAdapter(DropDownAdapter<T> adapter) {
        this.txt.setAdapter(adapter);
    }

    public final void setSelection(int position) {
        ListAdapter adapter = this.txt.getAdapter();
        if(position<adapter.getCount()) {
            String item = adapter.getItem(position).toString();
            if(!this.txt.getText().toString().equals(item)) {
                this.txt.setText(item, false);
                if (this.onSelectionChange != null) {
                    this.onSelectionChange.onChange(position);
                }
            }
        }
    }

    public final int getSelectedItemPosition() {
        ListAdapter adapter = this.txt.getAdapter();
        String item = this.txt.getText().toString();
        for(int i = 0; i<=adapter.getCount()-1; i++) {
            if(item.equals(adapter.getItem(i).toString())) {
                return i;
            }
        }
        return -1;
    }

    /** @noinspection unchecked*/
    public final T getSelectedItem() {
        if(!txt.getText().toString().isEmpty()) {
            ListAdapter adapter = txt.getAdapter();
            for(int i = 0; i<=adapter.getCount() - 1; i++) {
                String item = adapter.getItem(i).toString();
                if(txt.getText().toString().equals(item)) {
                    return (T) txt.getAdapter().getItem(i);
                }
            }
        }
        return null;
    }

    public final void setOnItemSelectedListener(OnSelectionChange onSelectionChange) {
        this.onSelectionChange = onSelectionChange;
        this.txt.setOnItemClickListener((adapterView, view, i, l) -> onSelectionChange.onChange(i));
        this.txt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onSelectionChange.onChange(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void setData() {
        this.inputLayout.setContentDescription(this.title);
        this.txt.setHint(this.title);
    }

    private void initControls(Context context) {
        View view = inflate(context, R.layout.drop_down, this);
        this.inputLayout = view.findViewById(R.id.input_layout);
        this.txt = view.findViewById(R.id.filled_exposed_dropdown);
    }

    private void setTitle(Context context, @Nullable AttributeSet attrs) {
        this.title = "";
        if(attrs != null) {
            try(TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DropDown, 0, 0)) {
                String tmp = a.getString(R.styleable.DropDown_dropdown_title);
                if(tmp != null) {
                    this.title = tmp;
                }
            }
        }
    }

    @FunctionalInterface
    public interface OnSelectionChange {
        void onChange(int position);
    }
}
