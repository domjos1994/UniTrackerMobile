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

import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.export.BuggerXML;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.ExportTask;
import de.domjos.unibuggerlibrary.tasks.FieldTask;
import de.domjos.unibuggerlibrary.tasks.IssueTask;
import de.domjos.unibuggerlibrary.tasks.ProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public final class ExportActivity extends AbstractActivity {
    private Button cmdExport;
    private EditText txtExportPath;
    private Spinner spBugTracker, spProjects, spData;
    private ArrayAdapter<String> dataAdapter;
    private ArrayAdapter<IBugService> bugTrackerAdapter;
    private ArrayAdapter<Project> projectAdapter;
    private Settings settings;

    public ExportActivity() {
        super(R.layout.export_activity);
    }

    @Override
    protected void initActions() {
        this.spBugTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    projectAdapter.clear();
                    IBugService bugService = bugTrackerAdapter.getItem(position);
                    ProjectTask projectTask = new ProjectTask(ExportActivity.this, bugService, false, settings.showNotifications());
                    List<Project> projects = projectTask.execute(0).get();
                    projectAdapter.addAll(projects);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, ExportActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                projectAdapter.clear();
            }
        });

        this.cmdExport.setOnClickListener(v -> {
            try {
                boolean notify = this.settings.showNotifications();
                IBugService bugService = bugTrackerAdapter.getItem(this.spBugTracker.getSelectedItemPosition());
                Project project = this.projectAdapter.getItem(this.spProjects.getSelectedItemPosition());
                BuggerXML.Type type = BuggerXML.Type.valueOf(this.dataAdapter.getItem(this.spData.getSelectedItemPosition()));
                String file = this.txtExportPath.getText().toString();

                if (bugService != null && project != null) {
                    ExportTask exportTask = new ExportTask(ExportActivity.this, bugService, type, project.getId(), file, notify);
                    List<Object> objects = new LinkedList<>();
                    switch (type) {
                        case Projects:
                            ProjectTask projectTask = new ProjectTask(ExportActivity.this, bugService, false, notify);
                            for (Project projects : projectTask.execute(0).get()) {
                                objects.add(projects.getId());
                            }
                            break;
                        case Issues:
                            IssueTask issueTask = new IssueTask(ExportActivity.this, bugService, project.getId(), false, false, notify);
                            for (Issue issue : issueTask.execute(0).get()) {
                                objects.add(issue.getId());
                            }
                            break;
                        case CustomFields:
                            FieldTask fieldTask = new FieldTask(ExportActivity.this, bugService, project.getId(), false, notify);
                            for (CustomField customField : fieldTask.execute(0).get()) {
                                objects.add(customField.getId());
                            }
                            break;
                    }
                    exportTask.execute(objects.toArray()).get();
                    MessageHelper.printMessage(this.getString(R.string.export_success), ExportActivity.this);
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, ExportActivity.this);
            }
        });
    }

    @Override
    protected void initControls() {
        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        this.cmdExport = this.findViewById(R.id.cmdExport);
        this.txtExportPath = this.findViewById(R.id.txtExportPath);

        this.spBugTracker = this.findViewById(R.id.spBugTracker);
        this.bugTrackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
        this.spBugTracker.setAdapter(this.bugTrackerAdapter);
        this.bugTrackerAdapter.notifyDataSetChanged();

        this.spProjects = this.findViewById(R.id.spProjects);
        this.projectAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
        this.spProjects.setAdapter(this.projectAdapter);
        this.projectAdapter.notifyDataSetChanged();

        this.spData = this.findViewById(R.id.spData);
        this.dataAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
        this.spData.setAdapter(this.dataAdapter);
        this.dataAdapter.notifyDataSetChanged();

        this.loadData();
        Helper.isStoragePermissionGranted(ExportActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void loadData() {
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            IBugService bugService = Helper.getCurrentBugService(authentication, this.getApplicationContext());
            this.bugTrackerAdapter.add(bugService);
        }

        for (BuggerXML.Type type : BuggerXML.Type.values()) {
            this.dataAdapter.add(type.name());
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return;
            }
        }
        File file = new File(dir.getAbsolutePath() + File.separatorChar + "export_" + new Date().getTime() + ".xml");
        this.txtExportPath.setText(file.toString());
    }
}
