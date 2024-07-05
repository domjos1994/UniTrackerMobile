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

package de.domjos.unibuggermobile.activities;

import android.content.pm.PackageInfo;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.tracker.MantisBTSpecific.ChangeLog;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;

public final class HelpActivity extends AbstractActivity {
    private TextView lblWhatsNew;
    private LinearLayout pnlQuestions;
    private TextInputLayout txtSearch;

    /**
     * Constructor with the Layout-Resource-ID
     */
    public HelpActivity() {
        super(R.layout.help_activity);
    }

    @Override
    protected void initActions() {
        Objects.requireNonNull(this.txtSearch.getEditText()).addTextChangedListener(new TextWatcher() {
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
            boolean containsSearch = false;
            if (this.pnlQuestions.getChildAt(i) instanceof MaterialCardView mcv) {
                if(mcv.getChildAt(0) instanceof LinearLayout ll) {
                    for(int child = 0; child<=ll.getChildCount() - 1; child++) {
                        if(ll.getChildAt(child) instanceof TextView txt) {
                            if (txt.getText().toString().toLowerCase().trim().contains(search.toLowerCase().trim())) {
                                containsSearch = true;
                                break;
                            }
                        }
                    }
                }
                if(!containsSearch) {
                    mcv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void initControls() {Helper.initToolbar(this);

        this.pnlQuestions = this.findViewById(R.id.pnlQuestions);
        this.lblWhatsNew = this.findViewById(R.id.lblWhatsNew);
        TextView lblNeedsHelp = this.findViewById(R.id.lblNeedsHelp);
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
                runOnUiThread(() -> lblWhatsNew.setText(content));

            }).start();
        } catch (Exception ignored) {
        }

        try {
            String content = this.getString(R.string.help_need_help_text);
            lblNeedsHelp.setText(Html.fromHtml("<a href=\"https://github.com/domjos1994/UniTrackerMobile\" title=\"UniTrackerMobile\">" + content + "<\\a>", Html.FROM_HTML_MODE_LEGACY));
        } catch (Exception ignored) {
        }
    }
}
