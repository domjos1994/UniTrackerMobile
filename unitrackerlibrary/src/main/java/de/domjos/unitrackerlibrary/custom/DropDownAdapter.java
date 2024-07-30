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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import de.domjos.unitrackerlibrary.R;

public class DropDownAdapter<T> extends ArrayAdapter<T> {
    public DropDownAdapter(@NonNull Context context) {
        super(context, R.layout.drop_down_item);
    }

    public DropDownAdapter(@NonNull Context context, int textViewResourceId) {
        super(context, R.layout.drop_down_item, textViewResourceId);
    }

    public DropDownAdapter(@NonNull Context context, @NonNull T[] objects) {
        super(context, R.layout.drop_down_item, objects);
    }

    public DropDownAdapter(@NonNull Context context, int textViewResourceId, @NonNull T[] objects) {
        super(context, R.layout.drop_down_item, textViewResourceId, objects);
    }

    public DropDownAdapter(@NonNull Context context, @NonNull List<T> objects) {
        super(context, R.layout.drop_down_item, objects);
    }

    public DropDownAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<T> objects) {
        super(context, R.layout.drop_down_item, textViewResourceId, objects);
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }

}
