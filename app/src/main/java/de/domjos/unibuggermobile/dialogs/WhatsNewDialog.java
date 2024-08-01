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
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public class WhatsNewDialog extends AbstractDialog {
    private TextView lblContent;

    public WhatsNewDialog(Activity activity) {
        super(activity, R.layout.whats_new_dialog);

        String version = Helper.getVersion(activity);
        String content = this.getStringResourceByName(activity, "whats_new_" + version);
        if(!content.isEmpty()) {
            this.setTitle(version);
            this.lblContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));

            Settings settings = MainActivity.GLOBALS.getSettings(activity);

            if(!settings.getWhatsNewVersion().isEmpty()) {
                if(!settings.getWhatsNewVersion().equals(version)) {
                    settings.setWhatsNewVersion();
                }
            } else {
                settings.setWhatsNewVersion();
            }
        }

        this.setTitle(R.string.help_key_whats_new_title);
    }

    @Override
    protected void init(View view) {
        this.lblContent = view.findViewById(R.id.lblWhatsNewContent);
    }

    private String getStringResourceByName(Activity activity, String aString) {
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
