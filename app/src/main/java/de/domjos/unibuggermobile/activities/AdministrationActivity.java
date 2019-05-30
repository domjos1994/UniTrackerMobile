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

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.ProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public final class AdministrationActivity extends AbstractActivity {
    private Button cmdCopy, cmdMove;
    private Spinner spBugTracker1, spBugTracker2, spProject1, spProject2, spData1, spData2, spDataItem1, spDataItem2;
    private ArrayAdapter<Authentication> bugTrackerAdapter1, bugTrackerAdapter2;
    private ArrayAdapter<Project> projectAdapter1, projectAdapter2;
    private ArrayAdapter<DescriptionObject> dataItemAdapter1, dataItemAdapter2;
    private ArrayAdapter<String> dataAdapter1, dataAdapter2;
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
                    ProjectTask projectTask = new ProjectTask(AdministrationActivity.this, bugService1, false, settings.showNotifications());
                    for (Object object : projectTask.execute(0L).get()) {
                        projectAdapter1.add((Project) object);
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
                try {
                    Project project = projectAdapter1.getItem(position);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, AdministrationActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spProject2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Project project = projectAdapter2.getItem(position);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, AdministrationActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void initControls() {
        int spinner = android.R.layout.simple_spinner_item;
        this.ctx = this.getApplicationContext();
        this.settings = MainActivity.GLOBALS.getSettings(this.ctx);
        this.cmdCopy = this.findViewById(R.id.cmdCopy);
        this.cmdMove = this.findViewById(R.id.cmdMove);


        List<Authentication> authentications = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("");
        this.spBugTracker1 = this.findViewById(R.id.spBugTracker1);
        this.bugTrackerAdapter1 = new ArrayAdapter<>(ctx, spinner, authentications);
        this.spBugTracker1.setAdapter(this.bugTrackerAdapter1);
        this.bugTrackerAdapter1.notifyDataSetChanged();
        this.spBugTracker2 = this.findViewById(R.id.spBugTracker2);
        this.bugTrackerAdapter2 = new ArrayAdapter<>(ctx, spinner, authentications);
        this.spBugTracker2.setAdapter(this.bugTrackerAdapter2);
        this.bugTrackerAdapter2.notifyDataSetChanged();

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
        this.spData2 = this.findViewById(R.id.spData2);
        this.dataAdapter2 = new ArrayAdapter<>(ctx, spinner);
        this.spData2.setAdapter(this.dataAdapter2);
        this.dataAdapter2.notifyDataSetChanged();

        this.spDataItem1 = this.findViewById(R.id.spDataItem1);
        this.dataItemAdapter1 = new ArrayAdapter<>(ctx, spinner);
        this.spDataItem1.setAdapter(this.dataItemAdapter1);
        this.dataItemAdapter1.notifyDataSetChanged();
        this.spDataItem2 = this.findViewById(R.id.spDataItem2);
        this.dataItemAdapter2 = new ArrayAdapter<>(ctx, spinner);
        this.spDataItem2.setAdapter(this.dataItemAdapter2);
        this.dataItemAdapter2.notifyDataSetChanged();
    }
}
