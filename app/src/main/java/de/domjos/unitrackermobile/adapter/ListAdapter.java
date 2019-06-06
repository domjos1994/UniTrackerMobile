/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
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
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.helper.Helper;

public class ListAdapter extends ArrayAdapter<ListObject> {
    private Context context;
    private int backupIcon;

    public ListAdapter(@NonNull Context context, int backupIcon) {
        super(context, R.layout.list_item);
        this.context = context;
        this.backupIcon = backupIcon;
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = Helper.getRowView(this.context, parent, R.layout.list_item);
        ListObject listObject = this.getItem(position);

        ImageView ivIcon = rowView.findViewById(R.id.ivIcon);
        TextView lblTitle = rowView.findViewById(R.id.lblTitle);
        TextView lblSubTitle = rowView.findViewById(R.id.lblSubTitle);

        if (listObject != null) {
            if (ivIcon != null) {
                if (listObject.getIcon() != null) {
                    ivIcon.setImageDrawable(listObject.getIcon());
                } else {
                    ivIcon.setImageDrawable(this.context.getResources().getDrawable(this.backupIcon));
                }
            }

            if (listObject.getDescriptionObject() != null) {
                if (lblTitle != null) {
                    lblTitle.setText(listObject.getDescriptionObject().getTitle());
                }

                if (lblSubTitle != null) {
                    if (listObject.getDescriptionObject().getDescription() != null) {
                        lblSubTitle.setText(listObject.getDescriptionObject().getDescription());
                    }
                }
            }
        }

        return rowView;
    }
}
