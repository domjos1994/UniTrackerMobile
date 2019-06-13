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

package de.domjos.unitrackermobile.activities;

import android.content.Intent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.tasks.IssueTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.adapter.PagerAdapter;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.settings.Settings;

public final class IssueActivity extends AbstractActivity {
    private BottomNavigationView navigationView;
    private PagerAdapter pagerAdapter;
    private String id, pid;
    private Issue issue;
    private IBugService bugService;
    private Settings settings;

    public IssueActivity() {
        super(R.layout.issue_activity);
    }

    @Override
    protected void initActions() {
    }

    @Override
    protected void initControls() {
        try {
            this.settings = MainActivity.GLOBALS.getSettings(getApplicationContext());
            Intent intent = this.getIntent();
            this.id = intent.getStringExtra("id");
            this.pid = intent.getStringExtra("pid");
            this.bugService = Helper.getCurrentBugService(IssueActivity.this);
            if (this.id.equals("")) {
                List<Issue> issues = new IssueTask(IssueActivity.this, this.bugService, this.pid, false, true, this.settings.showNotifications()).execute(0).get();
                if (issues.size() >= 1) {
                    this.issue = issues.get(0);
                }
            } else {
                List<Issue> issues = new IssueTask(IssueActivity.this, this.bugService, this.pid, false, true, this.settings.showNotifications()).execute(this.id).get();
                if (issues.size() >= 1) {
                    this.issue = issues.get(0);
                }
            }

            if (this.issue == null) {
                this.issue = new Issue();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, IssueActivity.this);
        }

        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.hideFieldsOfNavView();
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
                    try {
                        if (this.pagerAdapter.validate()) {
                            this.issue = (Issue) this.pagerAdapter.getObject();
                            this.issue.setId(this.id.equals("") ? null : this.id);
                            new IssueTask(IssueActivity.this, this.bugService, pid, false, false, this.settings.showNotifications()).execute(this.issue).get();
                            this.manageControls(false, true, false);
                            this.setResult(RESULT_OK);
                            this.finish();
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), this.getApplicationContext());
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, this.getApplicationContext());
                    }
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
        this.pagerAdapter.setObject(this.issue);
        this.pagerAdapter.setPid(pid);
    }


    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        IFunctionImplemented iFunctionImplemented = this.bugService.getPermissions();
        boolean isVisible = this.navigationView.getMenu().getItem(1).isVisible();
        boolean canUpdate = iFunctionImplemented.updateIssues();
        this.navigationView.setVisibility(canUpdate ? View.VISIBLE : View.GONE);

        this.navigationView.getMenu().getItem(1).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(3).setEnabled(editMode || !isVisible);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode || !isVisible);

        this.pagerAdapter.manageControls(editMode || !isVisible);
    }

    private void hideFieldsOfNavView() {
        this.navigationView.getMenu().getItem(0).setVisible(false);
        this.navigationView.getMenu().getItem(2).setVisible(false);
        this.navigationView.getMenu().getItem(1).setVisible(!this.id.equals(""));
    }
}