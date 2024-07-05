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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Objects;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public class WhatsNewDialog extends DialogFragment {

    public static void newInstance(Activity activity, FragmentManager manager) {
        Settings settings = MainActivity.GLOBALS.getSettings(activity);
        String version = Helper.getVersion(activity);

        WhatsNewDialog whatsNewDialog = new WhatsNewDialog();
        if(!settings.getWhatsNewVersion().isEmpty()) {
            if(!settings.getWhatsNewVersion().equals(version)) {
                whatsNewDialog.show(manager, "whatsNewDialog");
                settings.setWhatsNewVersion();
            }
        } else {
            whatsNewDialog.show(manager, "whatsNewDialog");
            settings.setWhatsNewVersion();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.whats_new_dialog, container, false);
        Objects.requireNonNull(this.getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);

        Activity activity = this.requireActivity();

        TextView lblTitle = view.findViewById(R.id.lblTitle);
        TextView lblContent = view.findViewById(R.id.lblWhatsNewContent);
        String version = Helper.getVersion(activity);

        String content = getStringResourceByName(activity, "whats_new_" + version);
        if(!content.isEmpty()) {
            lblTitle.setText(version);
            lblContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        }

        return view;
    }

    public static String getStringResourceByName(Activity activity, String aString) {
        try {
            String packageName = activity.getPackageName();
            @SuppressLint("DiscouragedApi")
            int resId = activity.getResources().getIdentifier(aString, "string", packageName);
            return activity.getString(resId);
        } catch (Exception ex) {
            return "";
        }
    }
}
