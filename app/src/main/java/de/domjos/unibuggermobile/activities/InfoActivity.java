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

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.helper.Helper;

public final class InfoActivity extends AbstractActivity {
    public final static String TITLE = "title";
    public final static String CONTENT = "content";
    public final static String ABOUT = "about";

    private FloatingActionButton floatingActionButton;

    public InfoActivity() {
        super(R.layout.info_activity);
    }

    @Override
    protected void initActions() {
        this.floatingActionButton.setOnClickListener(view -> finish());
    }

    @Override
    protected void initControls() {
        Helper.initToolbar(this);

        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        boolean about = false;
        String content = "";
        if(this.getIntent() != null) {
            if(this.getIntent().hasExtra(InfoActivity.TITLE)) {
                this.setTitle(this.getIntent().getStringExtra(InfoActivity.TITLE));
            }
            if(this.getIntent().hasExtra(InfoActivity.CONTENT)) {
                content = this.getIntent().getStringExtra(InfoActivity.CONTENT);
            }
            if(this.getIntent().hasExtra(InfoActivity.ABOUT)) {
                about = this.getIntent().getBooleanExtra(InfoActivity.ABOUT, false);
            }
        }

        ((TextView) this.findViewById(R.id.lblContent)).setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        this.floatingActionButton = this.findViewById(R.id.fab);
        this.floatingActionButton.setVisibility(about ? View.VISIBLE : View.GONE);
    }
}