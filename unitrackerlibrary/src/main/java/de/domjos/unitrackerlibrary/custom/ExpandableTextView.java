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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.domjos.unitrackerlibrary.R;

/** @noinspection unused*/
public class ExpandableTextView extends LinearLayout {
    private final Context context;
    private final AttributeSet attributeSet;
    private ImageView imageView;
    private TextView content, header;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.context = context;
        this.attributeSet = attributeSet;

        this.addControls();
    }

    public ExpandableTextView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);

        this.context = context;
        this.attributeSet = attributeSet;

        this.addControls();
    }

    public String getTitle() {
        return this.header.getText().toString();
    }

    public void setTitle(String title) {
        this.header.setText(title);
    }

    public String getContent() {
        return this.content.getText().toString();
    }

    public void setContent(CharSequence content) {
        this.content.setText(content);
    }

    public TextView getContextTextView() {
        return this.content;
    }

    private void addControls() {
        View view = inflate(this.context, R.layout.help_card, this);
        this.header = view.findViewById(R.id.lblHeader);
        this.content = view.findViewById(R.id.lblContent);

        this.header.setText(this.getContentFromAttr(R.styleable.ExpandableTextView_title));
        this.content.setText(this.getContentFromAttr(R.styleable.ExpandableTextView_text));
    }


    private String getContentFromAttr(int styleable) {
        if (this.attributeSet != null) {
            try(TypedArray array = this.context.obtainStyledAttributes(this.attributeSet, R.styleable.ExpandableTextView)) {
                for (int i = 0; i <= array.getIndexCount() - 1; i++) {
                    int attribute = array.getIndex(i);
                    if (attribute == styleable) {
                        return array.getString(attribute);
                    }
                }
            }
        }
        return "";
    }
}
