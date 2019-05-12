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

package de.domjos.unibuggermobile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class ListObject extends DescriptionObject {
    private byte[] icon;
    private Drawable drawable;
    private Context context;

    public ListObject(Context context, int resID, String title, String description) {
        super();

        this.context = context;
        this.icon = null;
        this.drawable = this.context.getResources().getDrawable(resID);
        super.setTitle(title);
        super.setDescription(description);
    }

    public ListObject(Context context, byte[] img, String title, String description) {
        super();

        this.context = context;
        this.icon = img;

        if (this.icon != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(this.icon, 0, this.icon.length);
            this.drawable = new BitmapDrawable(this.context.getResources(), bitmap);
        } else {
            this.drawable = null;
        }

        super.setTitle(title);
        super.setDescription(description);
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
        Bitmap bitmap = BitmapFactory.decodeByteArray(this.icon, 0, this.icon.length);
        this.drawable = new BitmapDrawable(this.context.getResources(), bitmap);
    }

    public Drawable getIcon() {
        return this.drawable;
    }
}
