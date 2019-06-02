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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.FieldTask;
import de.domjos.unibuggerlibrary.tasks.IssueTask;
import de.domjos.unibuggerlibrary.tasks.ProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public final class AdministrationActivity extends AbstractActivity {
    private Button cmdCopy, cmdMove;
    private Spinner spBugTracker1, spBugTracker2, spProject1, spProject2, spData1, spDataItem1;
    private ArrayAdapter<Authentication> bugTrackerAdapter1, bugTrackerAdapter2;
    private ArrayAdapter<Project> projectAdapter1, projectAdapter2;
    private ArrayAdapter<DescriptionObject> dataItemAdapter1;
    private ArrayAdapter<String> dataAdapter1;
    private CheckBox chkWithIssues;
    private IBugService bugService1, bugService2;

    private Context ctx;
    private Settings settings;

    public AdministrationActivity() {
        super(R.layout.administration_activity);
    }

    @Override
    protected void initActions() {
        this.spBugTracker1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    projectAdapter1.clear();
                    Authentication authentication = bugTrackerAdapter1.getItem(position);
                    bugService1 = Helper.getCurrentBugService(authentication, ctx);

                    boolean showData = false;
                    ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, bugService1, false, settings.showNotifications());
                    for (Object object : projectTask.execute(0L).get()) {
                        projectAdapter1.add((Project) object);
                        showData = true;
                    }

                    if (showData) {
                        dataAdapter1.clear();
                        dataAdapter1.addAll(Arrays.asList(getResources().getStringArray(R.array.administration_data)));
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, AdministrationActivity.this);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spBugTracker2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    projectAdapter2.clear();
                    Authentication authentication = bugTrackerAdapter2.getItem(position);
                    bugService2 = Helper.getCurrentBugService(authentication, ctx);

                    ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, bugService2, false, settings.showNotifications());
                    for (Object object : projectTask.execute(0L).get()) {
                        projectAdapter2.add((Project) object);
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, AdministrationActivity.this);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spProject1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reloadData1(spData1.getSelectedItemPosition(), position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spData1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reloadData1(position, spProject1.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.cmdCopy.setOnClickListener((v) -> this.writeData(false));
        this.cmdMove.setOnClickListener((v) -> this.writeData(true));
    }

    private void reloadData1(int data, int projectPosition) {
        try {
            boolean notify = this.settings.showNotifications();
            Project project1 = this.projectAdapter1.getItem(projectPosition);
            if (project1 != null) {
                this.dataItemAdapter1.clear();
                switch (data) {
                    case 0:
                        ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, this.bugService1, false, notify);
                        this.dataItemAdapter1.addAll(projectTask.execute(0L).get());
                        break;
                    case 1:
                        IssueTask issueTask = new IssueTask(AdministrationActivity.this, this.bugService1, project1.getId(), false, false, notify);
                        this.dataItemAdapter1.addAll(issueTask.execute(0L).get());
                        break;
                    case 2:
                        FieldTask fieldTask = new FieldTask(AdministrationActivity.this, this.bugService1, project1.getId(), false, notify);
                        this.dataItemAdapter1.addAll(fieldTask.execute(0L).get());
                        break;
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }

    private void writeData(boolean move) {
        try {
            Activity act = AdministrationActivity.this;
            boolean notify = this.settings.showNotifications();

            Project project2 = this.projectAdapter2.getItem(this.spProject2.getSelectedItemPosition());
            Project project1 = this.projectAdapter1.getItem(this.spProject1.getSelectedItemPosition());
            DescriptionObject dataItem1 = this.dataItemAdapter1.getItem(this.spDataItem1.getSelectedItemPosition());
            int dataPosition = this.spData1.getSelectedItemPosition();

            if (project2 != null && project1 != null) {
                Object id;
                switch (dataPosition) {
                    case 0:
                        Project project = (Project) dataItem1;
                        if (project != null) {
                            id = project.getId();
                            project.setId(null);
                            for (int i = 0; i <= project.getVersions().size() - 1; i++) {
                                ((Version) project.getVersions().get(i)).setId(null);
                            }
                            ProjectTask projectTask = new ProjectTask(act, this.bugService2, false, notify);
                            projectTask.execute(project).get();

                            if (move) {
                                projectTask = new ProjectTask(act, this.bugService1, true, notify);
                                projectTask.execute(id).get();
                            }

                            if (chkWithIssues.isChecked()) {
                                Object newId = null;
                                List<Project> projects = projectTask.execute("").get();
                                for (Project newProject : projects) {
                                    if (newProject.getTitle().equals(project.getId())) {
                                        newId = newProject.getId();
                                    }
                                }

                                IssueTask issueTask = new IssueTask(act, this.bugService1, id, false, false, notify);
                                for (Issue issue : issueTask.execute("").get()) {
                                    issueTask = new IssueTask(act, this.bugService1, id, false, true, notify);
                                    issue = issueTask.execute("").get().get(0);

                                    IssueTask newTask = new IssueTask(act, this.bugService2, newId, false, false, notify);
                                    newTask.execute(issue).get();

                                    if (move) {
                                        issueTask = new IssueTask(act, this.bugService1, id, true, true, notify);
                                        issueTask.execute(issue.getId()).get().get(0);
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        Issue issue = (Issue) dataItem1;
                        if (issue != null) {
                            id = issue.getId();
                            issue.setId(null);
                            for (int i = 0; i <= issue.getAttachments().size() - 1; i++) {
                                ((Attachment) issue.getAttachments().get(i)).setId(null);
                            }
                            for (int i = 0; i <= issue.getNotes().size() - 1; i++) {
                                ((Note) issue.getNotes().get(i)).setId(null);
                            }

                            IssueTask issueTask = new IssueTask(act, this.bugService2, project2.getId(), false, false, notify);
                            issueTask.execute(issue).get();

                            if (move) {
                                issueTask = new IssueTask(act, this.bugService1, project1.getId(), true, false, notify);
                                issueTask.execute(id).get();
                            }
                        }
                        break;
                    case 2:
                        CustomField customField = (CustomField) dataItem1;
                        if (customField != null) {
                            id = customField.getId();
                            customField.setId(null);

                            FieldTask fieldTask = new FieldTask(act, this.bugService2, project2.getId(), false, notify);
                            fieldTask.execute(customField).get();

                            if (move) {
                                fieldTask = new FieldTask(act, this.bugService1, project1.getId(), false, notify);
                                fieldTask.execute(id).get();
                            }
                        }
                        break;
                }
            }

            this.reloadAuthentications();

            String message = String.format(
                    this.getString(R.string.administration_message),
                    this.getResources().getStringArray(R.array.administration_data)[this.spData1.getSelectedItemPosition()],
                    move ? this.getString(R.string.administration_move) : this.getString(R.string.administration_copy)
            );
            MessageHelper.printMessage(message, this.ctx);
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }


    private void reloadAuthentications() {
        List<Authentication> authentications = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("");

        this.bugTrackerAdapter1.clear();
        this.bugTrackerAdapter1.addAll(authentications);

        this.bugTrackerAdapter2.clear();
        this.bugTrackerAdapter2.addAll(authentications);
    }
    @Override
    protected void initControls() {
        int spinner = android.R.layout.simple_spinner_item;
        this.ctx = this.getApplicationContext();
        this.settings = MainActivity.GLOBALS.getSettings(this.ctx);
        this.cmdCopy = this.findViewById(R.id.cmdCopy);
        this.cmdMove = this.findViewById(R.id.cmdMove);



        this.spBugTracker1 = this.findViewById(R.id.spBugTracker1);
        this.bugTrackerAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spBugTracker1.setAdapter(this.bugTrackerAdapter1);
        this.bugTrackerAdapter1.notifyDataSetChanged();
        this.spBugTracker2 = this.findViewById(R.id.spBugTracker2);
        this.bugTrackerAdapter2 = new ArrayAdapter<>(ctx, spinner);
        this.spBugTracker2.setAdapter(this.bugTrackerAdapter2);
        this.bugTrackerAdapter2.notifyDataSetChanged();
        this.reloadAuthentications();

        this.spProject1 = this.findViewById(R.id.spProject1);
        this.projectAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spProject1.setAdapter(this.projectAdapter1);
        this.projectAdapter1.notifyDataSetChanged();
        this.spProject2 = this.findViewById(R.id.spProject2);
        this.projectAdapter2 = new ArrayAdapter<>(ctx, spinner);
        this.spProject2.setAdapter(this.projectAdapter2);
        this.projectAdapter2.notifyDataSetChanged();

        this.spData1 = this.findViewById(R.id.spData1);
        this.dataAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spData1.setAdapter(this.dataAdapter1);
        this.dataAdapter1.notifyDataSetChanged();

        this.spDataItem1 = this.findViewById(R.id.spDataItem1);
        this.dataItemAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spDataItem1.setAdapter(this.dataItemAdapter1);
        this.dataItemAdapter1.notifyDataSetChanged();

        this.chkWithIssues = this.findViewById(R.id.chkWithIssues);
    }
}
