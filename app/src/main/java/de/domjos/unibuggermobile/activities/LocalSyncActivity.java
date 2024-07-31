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


import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;

import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.unitrackerlibrary.custom.DropDown;
import de.domjos.unitrackerlibrary.custom.DropDownAdapter;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.LocalSyncTask;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.LocalSyncAdapter;
import de.domjos.unitrackerlibrary.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;
import de.domjos.unitrackerlibrary.tools.Notifications;

public final class LocalSyncActivity extends AbstractActivity {
    private ExpandableListView expLvLocalSync;
    private DropDown<Authentication> spLocalSyncBugTracker;
    private DropDown<Project<?>> spLocalSyncProjects;
    private DropDownAdapter<Authentication> bugTrackerArrayAdapter;
    private DropDownAdapter<Project<?>> projectArrayAdapter;
    private Settings settings;
    private Activity activity;

    private EditText txtLocalSyncSearch;
    private TextInputLayout tilLocalSyncSearch;
    private ImageButton cmdSync;
    private ProgressBar pbProcess;

    public LocalSyncActivity() {
        super(R.layout.local_sync_activity);
    }

    @Override
    protected void initActions() {
        this.spLocalSyncBugTracker.setOnItemSelectedListener(position -> {
            try {
                reloadProjects(position);
            } catch (Exception ex) {
                Notifications.printException(LocalSyncActivity.this, ex, R.mipmap.ic_launcher_round);
            }
        });

        this.spLocalSyncProjects.setOnItemSelectedListener(position -> reload());

        this.tilLocalSyncSearch.setEndIconOnClickListener(v -> {
            String search = this.txtLocalSyncSearch.getText().toString().trim();
            if (!search.isEmpty()) {
                this.reload(search);
            } else {
                this.reload();
            }
        });

        this.cmdSync.setOnClickListener(view -> this.sync());
    }

    @Override
    protected void initControls() {
        this.activity = LocalSyncActivity.this;
        this.settings = MainActivity.GLOBALS.getSettings(this.activity);

        this.spLocalSyncBugTracker = this.findViewById(R.id.spLocalSyncBugTracker);
        this.bugTrackerArrayAdapter = new DropDownAdapter<>(this.activity, MainActivity.GLOBALS.getSqLiteGeneral().getAccounts(""));
        this.spLocalSyncBugTracker.setAdapter(this.bugTrackerArrayAdapter);
        this.bugTrackerArrayAdapter.notifyDataSetChanged();

        this.spLocalSyncProjects = this.findViewById(R.id.spLocalSyncProjects);
        this.projectArrayAdapter = new DropDownAdapter<>(this.activity, new ArrayList<>());
        this.spLocalSyncProjects.setAdapter(this.projectArrayAdapter);
        this.projectArrayAdapter.notifyDataSetChanged();

        this.expLvLocalSync = this.findViewById(R.id.expLvLocalSync);

        this.txtLocalSyncSearch = this.findViewById(R.id.txtLocalSyncSearch);
        this.tilLocalSyncSearch = this.findViewById(R.id.tilLocalSync);
        this.cmdSync = this.findViewById(R.id.cmdSync);
        this.pbProcess = this.findViewById(R.id.pbProcess);

        try {
            Helper.isStoragePermissionGranted(LocalSyncActivity.this);
            this.reloadProjects(this.spLocalSyncBugTracker.getSelectedItemPosition());

            if (this.settings.isLocalSyncAutomatically()) {
                this.sync();
            }
        } catch (Exception ex) {
            Notifications.printException(LocalSyncActivity.this, ex, R.mipmap.ic_launcher_round);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_OK);
            finish();
        }
    }


    private void sync() {
        try {
            IBugService<?> bugService = Helper.getCurrentBugService(this.bugTrackerArrayAdapter.getItem(this.spLocalSyncBugTracker.getSelectedItemPosition()), this.activity);
            Object pid = null;
            if (this.spLocalSyncProjects.getSelectedItem() != null) {
                Project<?> project = this.projectArrayAdapter.getItem(this.spLocalSyncProjects.getSelectedItemPosition());
                if (project != null) {
                    pid = project.getId();
                }
            }

            LocalSyncTask localSyncTask = new LocalSyncTask(this.activity, bugService, this.settings.showNotifications(), this.settings.getLocalSyncPath(), pid, this.pbProcess);
            localSyncTask.after((AbstractTask.PostExecuteListener<String>) result -> {
                Notifications.printMessage(LocalSyncActivity.this, result, R.mipmap.ic_launcher_round);
                reload();
            });
            localSyncTask.execute();
        } catch (Exception ex) {
            Notifications.printException(LocalSyncActivity.this, ex, R.mipmap.ic_launcher_round);
        }
    }

    private void reloadProjects(int position) throws Exception {
        this.projectArrayAdapter.clear();
        if (position != -1) {
            this.projectArrayAdapter.add(new Project<>());
            IBugService<?> bugService = Helper.getCurrentBugService(this.bugTrackerArrayAdapter.getItem(position), getApplicationContext());
            ProjectTask projectTask = new ProjectTask(activity, bugService, false, settings.showNotifications(), R.drawable.icon_projects);
            for (Project<?> project : projectTask.execute(0).get()) {
                this.projectArrayAdapter.add(project);
            }
        }
        reload();
    }

    @Override
    protected void reload() {
        this.reload("");
    }


    private void reload(String search) {
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
            Project<?> pro = this.projectArrayAdapter.getItem(this.spLocalSyncProjects.getSelectedItemPosition());
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

            LocalSyncAdapter localSyncAdapter = new LocalSyncAdapter(this.settings.getLocalSyncPath() + File.separatorChar + bugTracker + File.separatorChar + project, LocalSyncActivity.this, search);
            this.expLvLocalSync.setAdapter(localSyncAdapter);
            localSyncAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            Notifications.printException(LocalSyncActivity.this, ex, R.mipmap.ic_launcher_round);
        }
    }
}
