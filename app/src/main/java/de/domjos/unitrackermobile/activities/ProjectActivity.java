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

import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.ListObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unitrackerlibrary.utils.Converter;
import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.custom.CommaTokenizer;
import de.domjos.unitrackermobile.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.DateConverter;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;
import de.domjos.unitrackermobile.settings.Settings;

@SuppressWarnings("unchecked")
public final class ProjectActivity extends AbstractActivity {
    private BottomNavigationView navigationView;
    private SwipeRefreshDeleteList lvProjects;

    private EditText txtProjectTitle, txtProjectAlias, txtProjectDescription, txtProjectWebsite;
    private EditText txtProjectIconUrl, txtProjectVersion;
    private TextView lblCreatedAt, lblUpdatedAt;
    private CheckBox chkProjectEnabled, chkProjectPrivate;
    private MultiAutoCompleteTextView txtProjectsSubProject;
    private Spinner spProjectsState;
    private ArrayAdapter<String> stateAdapter;

    private TableRow rowProjectState, rowSubProjects, rowTimestamps, rowProjectAlias, rowProjectWebsite;
    private TableRow rowProjectEnabled, rowProjectIcon, rowProjectVersion, rowProjectPrivate;

    private IBugService bugService;
    private IFunctionImplemented permissions;
    private Project currentProject;
    private Validator projectValidator;
    private Settings settings;

    public ProjectActivity() {
        super(R.layout.project_activity);
    }

    @Override
    protected void initActions() {

        this.lvProjects.click(new SwipeRefreshDeleteList.ClickListener() {
            @Override
            public void onClick(ListObject listObject) {
                if (listObject != null) {
                    currentProject = (Project) listObject.getDescriptionObject();
                    objectToControls();
                    manageControls(false, false, true);
                }
            }
        });

        this.lvProjects.deleteItem(new SwipeRefreshDeleteList.DeleteListener() {
            @Override
            public void onDelete(ListObject listObject) {
                if(bugService.getPermissions().deleteProjects()) {
                    try {
                        final ProjectTask[] task = new ProjectTask[1];
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
                        builder.setTitle(R.string.sys_delete).setMessage(R.string.projects_msg);
                        builder.setPositiveButton(R.string.projects_msg_positive, (dialog, which) -> {
                            try {
                                task[0] = new ProjectTask(ProjectActivity.this, bugService, true, settings.showNotifications(), R.drawable.ic_apps_black_24dp);
                                task[0].execute(listObject.getDescriptionObject().getId()).get();
                                if (bugService.getCurrentState() != 200 && bugService.getCurrentState() != 201 && bugService.getCurrentState() != 204) {
                                    MessageHelper.printMessage(bugService.getCurrentMessage(), getApplicationContext());
                                } else {
                                    reload();
                                    manageControls(false, false, false);
                                }
                            } catch (Exception ex) {
                                MessageHelper.printException(ex, getApplicationContext());
                            }
                        });
                        builder.create().show();
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, getApplicationContext());
                    }
                }
            }
        });

        this.lvProjects.reload(new SwipeRefreshDeleteList.ReloadListener() {
            @Override
            public void onReload() {
                reload();
            }
        });
    }

    @Override
    protected void initControls() {
        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        // init bottom-navigation
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            final ProjectTask[] task = new ProjectTask[1];
            switch (menuItem.getItemId()) {
                case R.id.navAdd:
                    this.manageControls(true, true, false);
                    break;
                case R.id.navEdit:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navDelete:
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);
                        builder.setTitle(R.string.sys_delete).setMessage(R.string.projects_msg);
                        builder.setPositiveButton(R.string.projects_msg_positive, (dialog, which) -> {
                            try {
                                task[0] = new ProjectTask(ProjectActivity.this, this.bugService, true, this.settings.showNotifications(), R.drawable.ic_apps_black_24dp);
                                task[0].execute(this.currentProject.getId()).get();
                                if (this.bugService.getCurrentState() != 200 && this.bugService.getCurrentState() != 201 && this.bugService.getCurrentState() != 204) {
                                    MessageHelper.printMessage(this.bugService.getCurrentMessage(), this.getApplicationContext());
                                } else {
                                    this.reload();
                                    this.manageControls(false, false, false);
                                }
                            } catch (Exception ex) {
                                MessageHelper.printException(ex, this.getApplicationContext());
                            }
                        });
                        builder.create().show();
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, this.getApplicationContext());
                    }
                    break;
                case R.id.navCancel:
                    this.manageControls(false, false, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.projectValidator.getState()) {
                            this.controlsToObject();
                            task[0] = new ProjectTask(ProjectActivity.this, this.bugService, false, this.settings.showNotifications(), R.drawable.ic_apps_black_24dp);
                            task[0].execute(this.currentProject).get();
                            if (this.bugService.getCurrentState() != 200 && this.bugService.getCurrentState() != 201) {
                                MessageHelper.printMessage(this.bugService.getCurrentMessage(), this.getApplicationContext());
                            } else {
                                this.reload();
                                this.manageControls(false, false, false);
                            }
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), this.getApplicationContext());
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, this.getApplicationContext());
                    }
                    break;
            }
            return true;
        });

        // init controls
        this.lvProjects = this.findViewById(R.id.lvProjects);
        this.txtProjectTitle = this.findViewById(R.id.txtProjectTitle);
        this.txtProjectAlias = this.findViewById(R.id.txtProjectAlias);
        this.txtProjectDescription = this.findViewById(R.id.txtProjectDescription);
        this.txtProjectWebsite = this.findViewById(R.id.txtProjectWebsite);
        this.txtProjectIconUrl = this.findViewById(R.id.txtProjectIcon);
        this.txtProjectVersion = this.findViewById(R.id.txtProjectVersion);
        this.lblCreatedAt = this.findViewById(R.id.lblCreatedAt);
        this.lblUpdatedAt = this.findViewById(R.id.lblUpdatedAt);
        this.chkProjectEnabled = this.findViewById(R.id.chkProjectEnabled);
        this.chkProjectPrivate = this.findViewById(R.id.chkProjectPrivate);
        this.spProjectsState = this.findViewById(R.id.spProjectsState);
        this.stateAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item, this.getResources().getStringArray(R.array.project_state_mantis_label));
        this.spProjectsState.setAdapter(this.stateAdapter);
        this.stateAdapter.notifyDataSetChanged();

        this.txtProjectsSubProject = this.findViewById(R.id.txtSubProjects);
        this.txtProjectsSubProject.setTokenizer(new CommaTokenizer());

        this.rowProjectState = this.findViewById(R.id.rowProjectState);
        this.rowSubProjects = this.findViewById(R.id.rowSubProjects);
        this.rowTimestamps = this.findViewById(R.id.rowTimestamps);
        this.rowProjectAlias = this.findViewById(R.id.rowProjectAlias);
        this.rowProjectWebsite = this.findViewById(R.id.rowProjectWebsite);
        this.rowProjectEnabled = this.findViewById(R.id.rowProjectEnabled);
        this.rowProjectVersion = this.findViewById(R.id.rowProjectVersion);
        this.rowProjectIcon = this.findViewById(R.id.rowProjectIcon);
        this.rowProjectPrivate = this.findViewById(R.id.rowProjectPrivate);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.permissions = this.bugService.getPermissions();

        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidators() {
        this.projectValidator = new Validator(this.getApplicationContext());
        this.projectValidator.addEmptyValidator(this.txtProjectTitle);

        switch (this.settings.getCurrentAuthentication().getTracker()) {
            case RedMine:
            case Backlog:
                this.projectValidator.addEmptyValidator(this.txtProjectAlias);
                break;
            case Bugzilla:
                this.projectValidator.addEmptyValidator(this.txtProjectDescription);
                break;
            case YouTrack:
                this.projectValidator.addValueEqualsRegex(this.txtProjectAlias, "^[a-zA-Z0-9_]{1,}$");
                break;
        }
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode && this.permissions.addProjects());
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected && this.permissions.updateProjects());
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected && this.permissions.deleteProjects());
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.lvProjects.setEnabled(!editMode);
        this.txtProjectTitle.setEnabled(editMode);
        this.txtProjectAlias.setEnabled(editMode);
        this.txtProjectDescription.setEnabled(editMode);
        this.txtProjectWebsite.setEnabled(editMode);
        this.chkProjectPrivate.setEnabled(editMode);
        this.chkProjectEnabled.setEnabled(editMode);
        this.spProjectsState.setEnabled(editMode);
        this.txtProjectsSubProject.setEnabled(editMode);
        this.txtProjectVersion.setEnabled(editMode);
        this.txtProjectIconUrl.setEnabled(editMode);

        if (reset) {
            this.currentProject = new Project();
            this.objectToControls();
        }
    }

    @Override
    protected void reload() {
        try {
            if (this.permissions.listProjects()) {
                ProjectTask task = new ProjectTask(ProjectActivity.this, this.bugService, false, this.settings.showNotifications(), R.drawable.ic_apps_black_24dp);
                this.lvProjects.getAdapter().clear();
                ArrayAdapter<String> subProjects = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1);
                for (Project project : task.execute(0).get()) {
                    ListObject listObject = new ListObject(this.getApplicationContext(), R.drawable.ic_apps_black_24dp, project);
                    if (project.getIconUrl() != null) {
                        if (!project.getIconUrl().isEmpty()) {
                            try {
                                new Thread(() -> {
                                    try {
                                        listObject.setIcon(Converter.convertStringToByteArray(project.getIconUrl()));
                                        if (listObject.getIcon() != null) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                listObject.setIcon(Converter.convertSVGToByteArray(getApplicationContext(), project.getIconUrl()));
                                            } else {
                                                listObject.setIcon(Converter.convertDrawableToByteArray(this.getResources().getDrawable(R.drawable.ic_apps_black_24dp)));
                                            }
                                        }
                                    } catch (Exception ex) {
                                        runOnUiThread(() -> MessageHelper.printException(ex, getApplicationContext()));
                                    }
                                }).start();
                            } catch (Exception ex) {
                                MessageHelper.printException(ex, this.getApplicationContext());
                            }
                        }
                    }
                    subProjects.add(listObject.getDescriptionObject().getTitle());
                    this.lvProjects.getAdapter().add(listObject);
                }
                this.txtProjectsSubProject.setAdapter(subProjects);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }

    private void objectToControls() {
        if (this.currentProject != null) {
            String title = this.currentProject.getTitle();
            while (title.startsWith("-")) {
                title = title.substring(1);
            }

            this.txtProjectTitle.setText(title);
            this.txtProjectAlias.setText(this.currentProject.getAlias());
            this.txtProjectDescription.setText(this.currentProject.getDescription());
            this.chkProjectEnabled.setChecked(this.currentProject.isEnabled());
            this.spProjectsState.setSelection(this.stateAdapter.getPosition(this.convertStateToLabel(this.currentProject.getStatus())));
            this.chkProjectPrivate.setChecked(this.currentProject.isPrivateProject());
            this.txtProjectWebsite.setText(this.currentProject.getWebsite());
            this.txtProjectIconUrl.setText(this.currentProject.getIconUrl());
            this.txtProjectVersion.setText(this.currentProject.getDefaultVersion());

            if (this.currentProject.getCreatedAt() != 0) {
                this.lblCreatedAt.setText(DateConverter.convertLongToString(this.currentProject.getCreatedAt(), this.getApplicationContext()));
            }
            if (this.currentProject.getUpdatedAt() != 0) {
                this.lblUpdatedAt.setText(DateConverter.convertLongToString(this.currentProject.getUpdatedAt(), this.getApplicationContext()));
            }

            StringBuilder builder = new StringBuilder();
            this.txtProjectsSubProject.setText("");
            for (Object project : this.currentProject.getSubProjects()) {
                if (project instanceof Project) {
                    builder.append(((Project) project).getTitle()).append(",");
                }
            }
            this.txtProjectsSubProject.setText(builder.toString());
        }
    }


    private void controlsToObject() {
        if (this.currentProject != null) {
            this.currentProject.setTitle(this.txtProjectTitle.getText().toString());
            this.currentProject.setAlias(this.txtProjectAlias.getText().toString());
            this.currentProject.setDescription(this.txtProjectDescription.getText().toString());
            this.currentProject.setEnabled(this.chkProjectEnabled.isChecked());
            this.currentProject.setPrivateProject(this.chkProjectPrivate.isChecked());
            this.currentProject.setWebsite(this.txtProjectWebsite.getText().toString());
            this.currentProject.setDefaultVersion(this.txtProjectVersion.getText().toString());
            this.currentProject.setIconUrl(this.txtProjectIconUrl.getText().toString());

            this.currentProject.getSubProjects().clear();
            if (this.txtProjectsSubProject.getText().toString().contains(",")) {
                for (String text : this.txtProjectsSubProject.getText().toString().split(",")) {
                    text = text.trim();
                    for (int i = 0; i <= this.lvProjects.getAdapter().getItemCount() - 1; i++) {
                        ListObject object = this.lvProjects.getAdapter().getItem(i);
                        if (object != null) {
                            if (text.equals(object.getDescriptionObject().getTitle())) {
                                Project project = new Project();
                                project.setTitle(text);
                                project.setId(object.getDescriptionObject().getId());
                                this.currentProject.getSubProjects().add(project);
                                break;
                            }
                        }
                    }
                }
            } else {
                String text = this.txtProjectsSubProject.getText().toString().trim();
                for (int i = 0; i <= this.lvProjects.getAdapter().getItemCount() - 1; i++) {
                    ListObject object = this.lvProjects.getAdapter().getItem(i);
                    if (object != null) {
                        if (text.equals(object.getDescriptionObject().getTitle())) {
                            Project project = new Project();
                            project.setTitle(text);
                            project.setId(object.getDescriptionObject().getId());
                            this.currentProject.getSubProjects().add(project);
                            break;
                        }
                    }
                }
            }

            try {
                String state = this.convertLabelToState(this.stateAdapter.getItem(this.spProjectsState.getSelectedItemPosition()));
                if (!state.equals("")) {
                    this.currentProject.setStatus(state.split(":")[1], Integer.parseInt(state.split(":")[0]));
                }
            } catch (Exception ex) {
                this.currentProject.setStatus("", 0);
            }
        }
    }

    private String convertStateToLabel(String state) {
        String[] labels = this.getResources().getStringArray(R.array.project_state_mantis_label);
        String[] states = this.getResources().getStringArray(R.array.project_state_mantis);
        for (int i = 0; i <= states.length - 1; i++) {
            if (states[i].equals(state)) {
                return labels[i];
            }
        }
        return "";
    }

    private String convertLabelToState(String label) {
        String[] labels = this.getResources().getStringArray(R.array.project_state_mantis_label);
        String[] states = this.getResources().getStringArray(R.array.project_state_mantis);
        for (int i = 0; i <= labels.length - 1; i++) {
            if (labels[i].equals(label)) {
                switch (states[i]) {
                    case "Development":
                        return 10 + ":" + states[i];
                    case "Release":
                        return 30 + ":" + states[i];
                    case "Stable":
                        return 50 + ":" + states[i];
                    case "Deprecated":
                        return 70 + ":" + states[i];
                }
            }
        }
        return "";
    }

    private void updateUITrackerSpecific() {
        Authentication.Tracker tracker;
        if (this.settings.getCurrentAuthentication() != null) {
            tracker = this.settings.getCurrentAuthentication().getTracker();
        } else {
            return;
        }

        this.rowProjectState.setVisibility(View.GONE);
        this.rowSubProjects.setVisibility(View.GONE);
        this.rowTimestamps.setVisibility(View.GONE);
        this.rowProjectAlias.setVisibility(View.GONE);
        this.rowProjectWebsite.setVisibility(View.GONE);
        this.rowProjectEnabled.setVisibility(View.GONE);
        this.rowProjectIcon.setVisibility(View.GONE);
        this.rowProjectVersion.setVisibility(View.GONE);
        this.rowProjectPrivate.setVisibility(View.GONE);
        this.txtProjectDescription.setVisibility(View.VISIBLE);

        if (tracker != null) {
            switch (tracker) {
                case MantisBT:
                    this.rowProjectState.setVisibility(View.VISIBLE);
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    this.rowProjectPrivate.setVisibility(View.VISIBLE);
                    break;
                case RedMine:
                    this.rowProjectAlias.setVisibility(View.VISIBLE);
                    this.rowTimestamps.setVisibility(View.VISIBLE);
                    this.rowProjectWebsite.setVisibility(View.VISIBLE);
                    this.rowProjectPrivate.setVisibility(View.VISIBLE);
                    break;
                case YouTrack:
                    this.rowProjectAlias.setVisibility(View.VISIBLE);
                    this.rowProjectIcon.setVisibility(View.VISIBLE);
                    this.txtProjectIconUrl.setInputType(InputType.TYPE_NULL);
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    break;
                case Bugzilla:
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    break;
                case Github:
                    this.rowProjectPrivate.setVisibility(View.VISIBLE);
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    this.rowProjectWebsite.setVisibility(View.VISIBLE);
                    this.rowTimestamps.setVisibility(View.VISIBLE);
                    this.rowProjectAlias.setVisibility(View.VISIBLE);
                    break;
                case Jira:
                    this.rowProjectIcon.setVisibility(View.VISIBLE);
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    this.rowProjectAlias.setVisibility(View.VISIBLE);
                    this.rowProjectWebsite.setVisibility(View.VISIBLE);
                    break;
                case PivotalTracker:
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    this.rowTimestamps.setVisibility(View.VISIBLE);
                    break;
                case Backlog:
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    this.rowProjectAlias.setVisibility(View.VISIBLE);
                    this.txtProjectDescription.setVisibility(View.GONE);
                    break;
                case Local:
                    this.rowProjectState.setVisibility(View.VISIBLE);
                    this.rowSubProjects.setVisibility(View.VISIBLE);
                    this.rowTimestamps.setVisibility(View.VISIBLE);
                    this.rowProjectAlias.setVisibility(View.VISIBLE);
                    this.rowProjectWebsite.setVisibility(View.VISIBLE);
                    this.rowProjectEnabled.setVisibility(View.VISIBLE);
                    this.rowProjectIcon.setVisibility(View.VISIBLE);
                    this.rowProjectVersion.setVisibility(View.VISIBLE);
                    this.rowProjectPrivate.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
