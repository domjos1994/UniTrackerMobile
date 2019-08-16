/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.activities;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.LocalSyncTask;
import de.domjos.unibuggerlibrary.tasks.ProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.adapter.LocalSyncAdapter;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.settings.Settings;

public final class LocalSyncActivity extends AbstractActivity {
    private ExpandableListView expLvLocalSync;
    private Spinner spLocalSyncBugTracker, spLocalSyncProjects;
    private ArrayAdapter<Authentication> bugTrackerArrayAdapter;
    private ArrayAdapter<Project> projectArrayAdapter;
    private Settings settings;
    private Activity activity;

    public LocalSyncActivity() {
        super(R.layout.local_sync_activity);
    }

    @Override
    protected void initActions() {
        this.spLocalSyncBugTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    reloadProjects(position);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, activity);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spLocalSyncProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void initControls() {
        BottomNavigationView bottomNavigationView = this.findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.navLocalSync) {
                this.sync();
            }
            return true;
        });

        this.activity = LocalSyncActivity.this;
        this.settings = MainActivity.GLOBALS.getSettings(this.activity);
        int item = android.R.layout.simple_spinner_item;

        this.spLocalSyncBugTracker = this.findViewById(R.id.spLocalSyncBugTracker);
        this.bugTrackerArrayAdapter = new ArrayAdapter<>(this.activity, item, MainActivity.GLOBALS.getSqLiteGeneral().getAccounts(""));
        this.spLocalSyncBugTracker.setAdapter(this.bugTrackerArrayAdapter);
        this.bugTrackerArrayAdapter.notifyDataSetChanged();

        this.spLocalSyncProjects = this.findViewById(R.id.spLocalSyncProjects);
        this.projectArrayAdapter = new ArrayAdapter<>(this.activity, item, new ArrayList<>());
        this.spLocalSyncProjects.setAdapter(this.projectArrayAdapter);
        this.projectArrayAdapter.notifyDataSetChanged();

        this.expLvLocalSync = this.findViewById(R.id.expLvLocalSync);

        try {
            Helper.isStoragePermissionGranted(LocalSyncActivity.this);
            this.reloadProjects(this.spLocalSyncBugTracker.getSelectedItemPosition());

            if (this.settings.isLocalSyncAutomatically()) {
                this.sync();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.activity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_OK);
            finish();
        }
    }


    private void sync() {
        try {
            IBugService bugService = Helper.getCurrentBugService(this.bugTrackerArrayAdapter.getItem(this.spLocalSyncBugTracker.getSelectedItemPosition()), this.activity);
            Object pid = null;
            if (this.spLocalSyncProjects.getSelectedItem() != null) {
                Project project = this.projectArrayAdapter.getItem(this.spLocalSyncProjects.getSelectedItemPosition());
                if (project != null) {
                    pid = project.getId();
                }
            }

            LocalSyncTask localSyncTask = new LocalSyncTask(this.activity, bugService, this.settings.showNotifications(), this.settings.getLocalSyncPath(), pid);
            MessageHelper.printMessage(localSyncTask.execute().get(), LocalSyncActivity.this);
            this.reload();
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.activity);
        }
    }

    private void reloadProjects(int position) throws Exception {
        this.projectArrayAdapter.clear();
        if (position != -1) {
            this.projectArrayAdapter.add(new Project());
            IBugService bugService = Helper.getCurrentBugService(this.bugTrackerArrayAdapter.getItem(position), getApplicationContext());
            ProjectTask projectTask = new ProjectTask(activity, bugService, false, settings.showNotifications(), R.drawable.ic_apps_black_24dp);
            for (Project project : projectTask.execute(0).get()) {
                this.projectArrayAdapter.add(project);
            }
        }
        reload();
    }

    @Override
    protected void reload() {
        try {
            String bugTracker;
            String project;

            Authentication authentication = this.bugTrackerArrayAdapter.getItem(this.spLocalSyncBugTracker.getSelectedItemPosition());
            if (authentication == null) {
                return;
            }
            bugTracker = LocalSyncTask.renameToPathPart(authentication.getTitle());


            if (this.spLocalSyncProjects.getSelectedItemPosition() == -1) {
                return;
            }
            Project pro = this.projectArrayAdapter.getItem(this.spLocalSyncProjects.getSelectedItemPosition());
            if (pro == null) {
                return;
            } else {
                if (pro.getId() == null) {
                    return;
                } else {
                    if (pro.getId().equals(0) || pro.getId().equals("")) {
                        return;
                    }
                }
            }
            project = LocalSyncTask.renameToPathPart(pro.getTitle());

            LocalSyncAdapter localSyncAdapter = new LocalSyncAdapter(this.settings.getLocalSyncPath() + File.separatorChar + bugTracker + File.separatorChar + project, LocalSyncActivity.this);
            this.expLvLocalSync.setAdapter(localSyncAdapter);
            localSyncAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }
}
