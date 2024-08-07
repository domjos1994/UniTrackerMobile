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

import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.export.TrackerXML;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.ExportTask;
import de.domjos.unitrackerlibrary.tasks.FieldTask;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public final class ExportActivity extends AbstractActivity {
    private MaterialButton cmdExport;
    private TextInputLayout txtExportPath, txtXSLTPath;
    private EditText etExportPath, etXSLTPath;
    private Spinner spBugTracker, spProjects, spData, spExportPath;
    private CheckBox chkShowBackground, chkShowIcon, chkCopyExampleData;
    private ArrayAdapter<String> dataAdapter;
    private ArrayAdapter<IBugService<?>> bugTrackerAdapter;
    private ArrayAdapter<Project<?>> projectAdapter;
    private Settings settings;
    private TableLayout tblControls;

    public ExportActivity() {
        super(R.layout.export_activity);
    }

    @Override
    protected void initActions() {
        this.spExportPath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = spExportPath.getSelectedItem().toString().trim().toLowerCase();

                for(int j = 0; j<=tblControls.getChildCount()-1; j++) {
                    TableRow tableRow = (TableRow) tblControls.getChildAt(j);
                    if(tableRow.getTag()!=null) {
                        if(tableRow.getTag() instanceof String) {
                            tableRow.setVisibility(View.GONE);
                            etXSLTPath.setText("");
                            if(tableRow.getTag().toString().equals(item)) {
                                tableRow.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.spBugTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    projectAdapter.clear();
                    IBugService<?> bugService = bugTrackerAdapter.getItem(position);
                    ProjectTask projectTask = new ProjectTask(ExportActivity.this, bugService, false, settings.showNotifications(), R.drawable.icon_projects);
                    List<Project<?>> projects = projectTask.execute(0).get();
                    projectAdapter.addAll(projects);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, ExportActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                projectAdapter.clear();
            }
        });

        this.txtExportPath.setEndIconOnClickListener(v ->{
            FilePickerDialog dialog = Helper.initFilePickerDialog(ExportActivity.this, true, null, this.getString(R.string.export_path_choose));
            dialog.setDialogSelectionListener(files -> {
                if(files!=null) {
                    if(files.length >= 1) {
                        String name = files[0] + File.separatorChar + this.createFileName();
                        this.etExportPath.setText(name);
                    }
                }
            });
            dialog.show();
        });

        this.chkCopyExampleData.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) {
                String download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                this.copyExampleContent(R.raw.example_projects, "example_projects.xslt", download);
                this.copyExampleContent(R.raw.example_issues, "example_issues.xslt", download);
                this.copyExampleContent(R.raw.example_custom_fields, "example_custom_fields.xslt", download);
                MessageHelper.printMessage(this.getString(R.string.export_extended_xml_xslt_example_success), R.mipmap.ic_launcher_round, ExportActivity.this);
            }
        });

        this.txtXSLTPath.setEndIconOnClickListener(v->{
            FilePickerDialog dialog = Helper.initFilePickerDialog(ExportActivity.this, false, new String[]{"xslt"}, this.getString(R.string.export_path_choose));
            dialog.setDialogSelectionListener(files -> {
                if(files!=null) {
                    if(files.length >= 1) {
                        this.etXSLTPath.setText(files[0]);
                    }
                }
            });
            dialog.setOnCancelListener(dialogInterface -> this.etXSLTPath.setText(""));
            dialog.show();
        });

        this.cmdExport.setOnClickListener(v -> {
            try {
                boolean notify = this.settings.showNotifications();
                IBugService<?> bugService = bugTrackerAdapter.getItem(this.spBugTracker.getSelectedItemPosition());
                Project<?> project = this.projectAdapter.getItem(this.spProjects.getSelectedItemPosition());
                TrackerXML.Type type = TrackerXML.Type.valueOf(this.dataAdapter.getItem(this.spData.getSelectedItemPosition()));
                String file = this.etExportPath.getText().toString() + "." + this.spExportPath.getSelectedItem().toString();

                if (bugService != null && project != null) {
                    byte[] background = null, icon = null;

                    if(this.chkShowBackground.isChecked()) {
                        background = ConvertHelper.convertDrawableToByteArray(Objects.requireNonNull(ResourcesCompat.getDrawable(this.getResources(), R.drawable.background, this.getTheme())));
                    }
                    if(this.chkShowIcon.isChecked()) {
                        icon = ConvertHelper.convertDrawableToByteArray(Objects.requireNonNull(ResourcesCompat.getDrawable(this.getResources(), R.drawable.icon, this.getTheme())));
                    }


                    String xslt = "";
                    if(this.etXSLTPath.getText() != null) {
                        xslt = this.etXSLTPath.getText().toString();
                    }

                    ExportTask exportTask = new ExportTask(
                            ExportActivity.this, bugService, type, project.getId(), file, notify,
                            R.drawable.icon_export, background, icon, xslt);
                    List<Object> objects = new LinkedList<>();
                    switch (type) {
                        case Projects:
                            ProjectTask projectTask = new ProjectTask(ExportActivity.this, bugService, false, notify, R.drawable.icon_projects);
                            objects.addAll(projectTask.execute(0).get());
                            break;
                        case Issues:
                            IssueTask issueTask = new IssueTask(ExportActivity.this, bugService, project.getId(), false, false, notify, R.drawable.icon_issues);
                            for (Issue<?> issue : issueTask.execute(0).get()) {
                                IssueTask loading = new IssueTask(ExportActivity.this, bugService, project.getId(), false, true, notify, R.drawable.icon);
                                objects.add(loading.execute(issue.getId()).get());
                            }
                            break;
                        case CustomFields:
                            FieldTask fieldTask = new FieldTask(ExportActivity.this, bugService, project.getId(), false, notify, R.drawable.icon_custom_fields);
                            objects.addAll(fieldTask.execute(0).get());
                            break;
                    }
                    exportTask.execute(objects.toArray()).get();
                    MessageHelper.printMessage(this.getString(R.string.export_success), R.mipmap.ic_launcher_round, ExportActivity.this);
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, ExportActivity.this);
            } catch (OutOfMemoryError error) {
                MessageHelper.printMessage(error.getMessage(), R.mipmap.ic_launcher_round, ExportActivity.this);
            }
        });
    }

    private void copyExampleContent(int resource, String name, String path) {
        try {
            String content = Helper.readStringFromRaw(resource, ExportActivity.this);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(path + File.separatorChar + name));
            outputStreamWriter.write(content);
            outputStreamWriter.close();
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, ExportActivity.this);
        }
    }

    @Override
    protected void initControls() {
        Helper.initToolbar(this);

        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        this.tblControls = this.findViewById(R.id.tblControls);

        this.cmdExport = this.findViewById(R.id.cmdExport);
        this.spExportPath = this.findViewById(R.id.spExportPath);
        this.txtExportPath = this.findViewById(R.id.txtExportPath);
        this.etExportPath = this.txtExportPath.getEditText();

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

        this.chkShowBackground = this.findViewById(R.id.chkShowBackground);
        this.chkShowIcon = this.findViewById(R.id.chkShowIcon);

        this.txtXSLTPath = this.findViewById(R.id.txtXSLTPath);
        this.etXSLTPath = this.txtXSLTPath.getEditText();
        this.chkCopyExampleData = this.findViewById(R.id.chkCopyExampleData);

        this.loadData();
        Helper.isStoragePermissionGranted(ExportActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void loadData() {
        List<Authentication> accounts = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("");
        if(accounts != null) {
            for (Authentication authentication : accounts) {
                IBugService<?> bugService = Helper.getCurrentBugService(authentication, this.getApplicationContext());
                this.bugTrackerAdapter.add(bugService);
            }
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
        this.etExportPath.setText(file.toString());
    }

    private String createFileName() {
        return "export_" + new Date().getTime();
    }
}
