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

package de.domjos.unibuggermobile.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** @noinspection SameParameterValue*/
public abstract class AbstractDialog {
    private final MaterialAlertDialogBuilder builder;
    protected final Activity activity;
    private final View view;
    protected AlertDialog alertDialog;

    public AbstractDialog(Activity activity, @LayoutRes int id) {
        this.activity = activity;

        LayoutInflater inflater = this.activity.getLayoutInflater();
        this.view = inflater.inflate(id, null);

        this.builder = new MaterialAlertDialogBuilder(this.activity);
        this.builder.setView(this.view);
    }

    public void setTitle(String title) {
        this.builder.setTitle(title);
    }

    public void setTitle(@StringRes int title) {
        this.builder.setTitle(title);
    }

    public void show() {
        this.alertDialog = this.builder.create();
        this.init(this.view);
        this.alertDialog.show();
    }

    protected void setCancelable(boolean cancelable) {
        this.builder.setCancelable(cancelable);
    }

    protected void setOnSubmit(@StringRes int id, DialogInterface.OnClickListener onClickListener) {
        this.builder.setPositiveButton(id, onClickListener);
    }

    protected void setOnCancel(@StringRes int id, DialogInterface.OnClickListener onClickListener) {
        this.builder.setNegativeButton(id, onClickListener);
    }

    protected abstract void init(View view);
}
