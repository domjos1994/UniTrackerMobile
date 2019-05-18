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

package de.domjos.unibuggermobile.activities;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.tasks.issues.GetIssueTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.PagerAdapter;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;

public class IssueActivity extends AbstractActivity {
    private BottomNavigationView navigationView;
    private PagerAdapter pagerAdapter;
    private String id;

    public IssueActivity() {
        super(R.layout.issue_activity);
    }

    @Override
    protected void initActions() {

    }

    @Override
    protected void initControls() {
        Intent intent = this.getIntent();
        this.id = intent.getStringExtra("id");
        boolean newItem = this.id.equals("");

        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.getMenu().getItem(0).setVisible(false);
        this.navigationView.getMenu().getItem(2).setVisible(false);
        this.navigationView.getMenu().getItem(1).setVisible(newItem);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navEdit:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navCancel:
                    this.setResult(RESULT_OK);
                    this.finish();
                    break;
                case R.id.navSave:
                    Issue issue = (Issue) this.pagerAdapter.getObject();
                    this.manageControls(false, true, false);
                    this.setResult(RESULT_OK);
                    this.finish();
                    break;
            }
            return false;
        });

        // init View-Pager
        this.pagerAdapter = new PagerAdapter(this, this.getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(this.pagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        try {
            this.pagerAdapter.setObject(new GetIssueTask(IssueActivity.this, Helper.getCurrentBugService(IssueActivity.this)).execute(this.id).get());
        } catch (Exception ex) {
            MessageHelper.printException(ex, IssueActivity.this);
        }
    }


    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.pagerAdapter.manageControls(editMode, reset, selected);

    }
}