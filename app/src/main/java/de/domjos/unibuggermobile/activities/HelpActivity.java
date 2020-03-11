/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.activities;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.tracker.MantisBTSpecific.ChangeLog;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.widgets.ExpandableTextView;

public final class HelpActivity extends AbstractActivity {
    private ExpandableTextView lblWhatsNew;
    private LinearLayout pnlQuestions;
    private EditText txtSearch;

    /**
     * Constructor with the Layout-Resource-ID
     */
    public HelpActivity() {
        super(R.layout.help_activity);
    }

    @Override
    protected void initActions() {
        this.txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });
    }

    private void search(String search) {
        for (int i = 0; i <= this.pnlQuestions.getChildCount() - 1; i++) {
            this.pnlQuestions.getChildAt(i).setVisibility(View.VISIBLE);
        }

        for (int i = 0; i <= this.pnlQuestions.getChildCount() - 1; i++) {
            if (this.pnlQuestions.getChildAt(i) instanceof ExpandableTextView) {
                ExpandableTextView txt = (ExpandableTextView) this.pnlQuestions.getChildAt(i);

                if (txt.getTitle().toLowerCase().trim().contains(search.toLowerCase().trim())) {
                    continue;
                }
                if (!txt.getContent().toLowerCase().trim().contains(search.toLowerCase().trim())) {
                    txt.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void initControls() {
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        this.pnlQuestions = this.findViewById(R.id.pnlQuestions);
        this.lblWhatsNew = this.findViewById(R.id.lblWhatsNew);
        ExpandableTextView lblNeedsHelp = this.findViewById(R.id.lblNeedsHelp);
        this.txtSearch = this.findViewById(R.id.txtSearch);

        try {
            PackageInfo packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            final String version = packageInfo.versionName;

            new Thread(() -> {
                Authentication authentication = new Authentication();
                authentication.setServer("https://mantis.dojodev.de/");
                authentication.setTracker(Authentication.Tracker.MantisBT);
                authentication.setUserName("PUBLIC");
                String content = new ChangeLog(authentication).getChangeLog(version);
                runOnUiThread(() -> lblWhatsNew.setContent(content));

            }).start();
        } catch (Exception ignored) {
        }

        try {
            String content = this.getString(R.string.help_need_help_text);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                lblNeedsHelp.setContent(Html.fromHtml("<a href=\"https://unitrackermobile.domjos.de\" title=\"UniTrackerMobile\">" + content + "<\\a>", Html.FROM_HTML_MODE_LEGACY));
            } else {
                lblNeedsHelp.setContent(HtmlCompat.fromHtml("<a href=\"https://unitrackermobile.domjos.de\" title=\"UniTrackerMobile\">" + content + "<\\a>", HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            lblNeedsHelp.getContextTextView().setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception ignored) {
        }
    }
}
