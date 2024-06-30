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

import android.content.Intent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.VersionTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.PagerAdapter;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public final class IssueActivity extends AbstractActivity {
    private BottomNavigationView navigationView;
    private PagerAdapter pagerAdapter;
    private String id, pid;
    private Issue<?> issue;
    private IBugService<?> bugService;
    private Settings settings;

    public IssueActivity() {
        super(R.layout.issue_activity);
    }

    @Override
    protected void initActions() {
    }

    @Override
    protected void initControls() {
        int notificationId = 0;
        try {
            this.settings = MainActivity.GLOBALS.getSettings(getApplicationContext());
            Intent intent = this.getIntent();
            this.id = intent.getStringExtra("id");
            this.pid = intent.getStringExtra("pid");
            this.bugService = Helper.getCurrentBugService(IssueActivity.this);
            IssueTask task = new IssueTask(IssueActivity.this, this.bugService, this.pid, false, true, this.settings.showNotifications(), R.drawable.icon_issues);
            List<Issue<?>> issues;
            if (this.id.isEmpty()) {
                issues = task.execute(0).get();
            } else {
                issues = task.execute(this.id).get();
            }
            notificationId = task.getId();
            if (!issues.isEmpty()) {
                this.issue = issues.get(0);
            }

            if (this.issue == null) {
                this.issue = new Issue<>();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, IssueActivity.this);
        }

        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.hideFieldsOfNavView();
        int finalNotificationId = notificationId;
        this.navigationView.setOnItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.navEdit) {
                this.manageControls(true, false, false);
            } else if(menuItem.getItemId() == R.id.navCancel) {
                this.setResult(RESULT_OK);
                this.finish();
            } else if(menuItem.getItemId() == R.id.navSave) {
                try {
                    if (this.pagerAdapter.validate()) {
                        this.issue = (Issue<?>) this.pagerAdapter.getObject();
                        ((Issue)this.issue).setId(this.id.isEmpty() ? null : this.id);
                        VersionTask versionTask = new VersionTask(IssueActivity.this, this.bugService, pid, false, this.settings.showNotifications(), "versions", R.drawable.icon_issues);
                        List<Version<?>> versions = versionTask.execute(0).get();
                        boolean exists1 = false, exists2 = false, exists3 = false;
                        for(Version<?> version : versions) {
                            if(version.getTitle().trim().equals(issue.getVersion())) {
                                exists1 = true;
                            }
                            if(version.getTitle().trim().equals(issue.getFixedInVersion())) {
                                exists2 = true;
                            }
                            if(version.getTitle().trim().equals(issue.getTargetVersion())) {
                                exists3 = true;
                            }
                        }

                        if(!exists1) {
                            versionTask = new VersionTask(IssueActivity.this, this.bugService, pid, false, this.settings.showNotifications(), "versions", R.drawable.icon_issues);
                            versionTask.setId(finalNotificationId);
                            Version<?> current = new Version<>();
                            current.setTitle(issue.getVersion());
                            versionTask.execute(current).get();
                        }
                        if(!exists2) {
                            versionTask = new VersionTask(IssueActivity.this, this.bugService, pid, false, this.settings.showNotifications(), "versions", R.drawable.icon_issues);
                            versionTask.setId(finalNotificationId);
                            Version<?> current = new Version<>();
                            current.setTitle(issue.getFixedInVersion());
                            versionTask.execute(current).get();
                        }
                        if(!exists3) {
                            versionTask = new VersionTask(IssueActivity.this, this.bugService, pid, false, this.settings.showNotifications(), "versions", R.drawable.icon_issues);
                            versionTask.setId(finalNotificationId);
                            Version<?> current = new Version<>();
                            current.setTitle(issue.getTargetVersion());
                            versionTask.execute(current).get();
                        }

                        IssueTask issueTask = new IssueTask(IssueActivity.this, this.bugService, pid, false, false, this.settings.showNotifications(), R.drawable.icon_issues);
                        issueTask.setId(finalNotificationId);
                        issueTask.execute(this.issue).get();
                        this.manageControls(false, true, false);
                        this.setResult(RESULT_OK);
                        this.finish();
                    } else {
                        super.createSnackBar(this.pagerAdapter.getResult());
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getApplicationContext());
                }
            }
            return false;
        });

        // init View-Pager
        this.pagerAdapter = new PagerAdapter(this, this.getSupportFragmentManager());
        this.pagerAdapter.setNotificationId(notificationId);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(this.pagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        this.pagerAdapter.setObject(this.issue);
        this.pagerAdapter.setPid(pid);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                IssueActivity.this.setTitle(pagerAdapter.getTitle(position));
            }

            @Override
            public void onPageSelected(int position) {
                IssueActivity.this.setTitle(pagerAdapter.getTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        IFunctionImplemented iFunctionImplemented = this.bugService.getPermissions();
        boolean isVisible = this.navigationView.getMenu().getItem(1).isVisible();
        boolean canUpdate = iFunctionImplemented.updateIssues();
        boolean canAdd = iFunctionImplemented.addIssues();
        if(this.issue != null) {
            if (this.issue.getId() == null) {
                this.navigationView.setVisibility(canAdd ? View.VISIBLE : View.GONE);
            } else {
                this.navigationView.setVisibility(canUpdate ? View.VISIBLE : View.GONE);
            }
        }

        this.navigationView.getMenu().getItem(1).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(3).setEnabled(editMode || !isVisible);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode || !isVisible);

        this.pagerAdapter.manageControls(editMode || !isVisible);
    }

    private void hideFieldsOfNavView() {
        this.navigationView.getMenu().getItem(0).setVisible(false);
        this.navigationView.getMenu().getItem(2).setVisible(false);
        if(this.id != null) {
            this.navigationView.getMenu().getItem(1).setVisible(!this.id.isEmpty());
        }
    }
}