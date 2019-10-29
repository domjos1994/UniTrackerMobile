/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

package de.domjos.unitrackermobile.activities;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.export.TrackerXML;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.ExportTask;
import de.domjos.unitrackerlibrary.tasks.FieldTask;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unitrackerlibrary.utils.Converter;
import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.settings.Settings;

public final class ExportActivity extends AbstractActivity {
    private Button cmdExport;
    private ImageButton cmdExportPath;
    private EditText txtExportPath;
    private Spinner spBugTracker, spProjects, spData, spExportPath;
    private ArrayAdapter<String> dataAdapter;
    private ArrayAdapter<IBugService> bugTrackerAdapter;
    private ArrayAdapter<Project> projectAdapter;
    private Settings settings;
    private FilePickerDialog dialog;

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
                    ProjectTask projectTask = new ProjectTask(ExportActivity.this, bugService, false, settings.showNotifications(), R.drawable.ic_apps_black_24dp);
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

        this.cmdExportPath.setOnClickListener(v ->{
            this.dialog.setDialogSelectionListener(files -> {
                if(files!=null) {
                    if(files.length >= 1) {
                        String name = files[0] + File.separatorChar + this.createFileName();
                        this.txtExportPath.setText(name);
                    }
                }
            });
            this.dialog.show();
        });

        this.cmdExport.setOnClickListener(v -> {
            try {
                boolean notify = this.settings.showNotifications();
                IBugService bugService = bugTrackerAdapter.getItem(this.spBugTracker.getSelectedItemPosition());
                Project project = this.projectAdapter.getItem(this.spProjects.getSelectedItemPosition());
                TrackerXML.Type type = TrackerXML.Type.valueOf(this.dataAdapter.getItem(this.spData.getSelectedItemPosition()));
                String file = this.txtExportPath.getText().toString() + "." + this.spExportPath.getSelectedItem().toString();

                if (bugService != null && project != null) {
                    Drawable drawable = this.getResources().getDrawable(R.drawable.background);
                    ExportTask exportTask = new ExportTask(
                            ExportActivity.this, bugService, type, project.getId(), file, notify,
                            R.drawable.ic_import_export_black_24dp,
                            Converter.convertDrawableToByteArray(drawable),
                            Converter.convertDrawableToByteArray(this.getResources().getDrawable(R.drawable.ic_launcher_web)));
                    List<Object> objects = new LinkedList<>();
                    switch (type) {
                        case Projects:
                            ProjectTask projectTask = new ProjectTask(ExportActivity.this, bugService, false, notify, R.drawable.ic_apps_black_24dp);
                            for (Project projects : projectTask.execute(0).get()) {
                                objects.add(projects.getId());
                            }
                            break;
                        case Issues:
                            IssueTask issueTask = new IssueTask(ExportActivity.this, bugService, project.getId(), false, false, notify, R.drawable.ic_bug_report_black_24dp);
                            for (Issue issue : issueTask.execute(0).get()) {
                                objects.add(issue.getId());
                            }
                            break;
                        case CustomFields:
                            FieldTask fieldTask = new FieldTask(ExportActivity.this, bugService, project.getId(), false, notify, R.drawable.ic_text_fields_black_24dp);
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
            } catch (OutOfMemoryError error) {
                MessageHelper.printMessage(error.getMessage(), ExportActivity.this);
            }
        });
    }

    @Override
    protected void initControls() {
        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        this.cmdExport = this.findViewById(R.id.cmdExport);
        this.cmdExportPath = this.findViewById(R.id.cmdExportPath);
        this.spExportPath = this.findViewById(R.id.spExportPath);
        this.txtExportPath = this.findViewById(R.id.txtExportPath);

        this.spBugTracker = this.findViewById(R.id.spBugTracker);
        this.bugTrackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
        this.spBugTracker.setAdapter(this.bugTrackerAdapter);
        this.bugTrackerAdapter.notifyDataSetChanged();

        this.spProjects = this.findViewById(R.id.spProjects);
        this.projectAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
        this.spProjects.setAdapter(this.projectAdapter);
        this.projectAdapter.notifyDataSetChanged();

        this.spData = this.findViewById(R.id.spData);
        this.dataAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
        this.spData.setAdapter(this.dataAdapter);
        this.dataAdapter.notifyDataSetChanged();

        this.loadData();
        Helper.isStoragePermissionGranted(ExportActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

        for (TrackerXML.Type type : TrackerXML.Type.values()) {
            this.dataAdapter.add(type.name());
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return;
            }
        }
        File file = new File(dir.getAbsolutePath() + File.separatorChar + this.createFileName());
        this.txtExportPath.setText(file.toString());

        this.initDialog();
    }

    private String createFileName() {
        return "export_" + new Date().getTime();
    }

    private void initDialog() {
        DialogProperties dialogProperties = new DialogProperties();
        dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
        dialogProperties.root = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.offset = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.selection_type = DialogConfigs.DIR_SELECT;
        dialogProperties.extensions = null;

        this.dialog = new FilePickerDialog(ExportActivity.this, dialogProperties);
        this.dialog.setCancelable(true);
        this.dialog.setTitle(this.getString(R.string.export_path_choose));
    }
}
