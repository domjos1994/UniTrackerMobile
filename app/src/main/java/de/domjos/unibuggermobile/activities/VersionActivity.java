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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unibuggermobile.custom.DatePickerField;
import de.domjos.unibuggermobile.dialogs.RoadMapDialog;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.ExportTask;
import de.domjos.unitrackerlibrary.tasks.VersionTask;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.customwidgets.utils.Validator;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;

public final class VersionActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private SwipeRefreshDeleteList lvVersions;
    private EditText txtVersionTitle, txtVersionDescription;
    private DatePickerField txtVersionReleasedAt;
    private ProgressBar pbVersion;
    private CheckBox chkVersionReleased, chkVersionDeprecated;
    private Spinner spVersionFilter;
    private TableRow rowVersionReleased, rowVersionDeprecated, rowVersionReleasedAt;
    private LinearLayout rowVersionFilter;

    private IBugService<?> bugService;
    private IFunctionImplemented permissions;
    private Object currentProject;
    private Version<?> currentVersion;

    private Validator versionValidator;
    private Settings settings;

    private String filter;

    private byte[] bg;
    private byte[] icon;
    private Activity act;
    private boolean notification;

    public VersionActivity() {
        super(R.layout.version_activity);
    }

    @Override
    protected void initActions() {
        this.icon = Helper.getBytesFromIcon(this, R.drawable.icon);
        this.act = VersionActivity.this;
        this.notification = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).showNotifications();
        this.bg = ConvertHelper.convertDrawableToByteArray(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.background)));

        this.lvVersions.setOnDeleteListener(listObject -> {
            try {
                new VersionTask(VersionActivity.this, bugService, currentProject, true, settings.showNotifications(), "", R.drawable.icon_versions).execute(((Version<?>)listObject.getObject()).getId()).get();
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, VersionActivity.this);
            }
        });

        this.lvVersions.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            currentVersion = (Version<?>) listObject.getObject();
            objectToControls();
            manageControls(false, false, true);
        });

        this.lvVersions.addButtonClick(R.drawable.icon_versions, this.getString(R.string.versions_menu_changelog), list -> {
            AtomicReference<String> path = new AtomicReference<>("");
            AtomicBoolean success = new AtomicBoolean(false);
            FilePickerDialog dialog = Helper.initFilePickerDialog(VersionActivity.this, true, null, this.getString(R.string.versions_menu_changelog_dir));
            dialog.setDialogSelectionListener(files -> {
                if(files != null) {
                    if(files.length != 0) {
                        path.set(files[0]);
                        success.set(true);
                        dialog.dismiss();
                    }
                }
            });
            dialog.setOnDismissListener(dialogInterface -> {
                try {
                    if(success.get()) {
                        Object pid = this.currentProject;

                        this.pbVersion.setVisibility(View.VISIBLE);
                        ExportTask exportTask = new ExportTask(this.act, this.bugService, pid, path.get(), this.notification, R.mipmap.ic_launcher_round, this.bg, this.icon, this.pbVersion);
                        exportTask.after(o -> {
                            pbVersion.setVisibility(View.GONE);
                            MessageHelper.printMessage(getString(R.string.versions_menu_changelog_created), R.mipmap.ic_launcher_round, VersionActivity.this);
                        });
                        exportTask.execute(list);
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, VersionActivity.this);
                }
            });
            dialog.show();
        });

        this.lvVersions.setOnReloadListener(this::reload);

        this.spVersionFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filter = spVersionFilter.getSelectedItem().toString();
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void initControls() {
        Helper.initToolbar(this);

        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.navAdd) {
                this.manageControls(true, true, false);
            } else if(menuItem.getItemId() == R.id.navEdit) {
                this.manageControls(true, false, false);
            } else if(menuItem.getItemId() == R.id.navDelete) {
                try {
                    new VersionTask(VersionActivity.this, this.bugService, this.currentProject, true, this.settings.showNotifications(), "", R.drawable.icon_versions).execute(this.currentVersion.getId()).get();
                    this.reload();
                    this.manageControls(false, true, false);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, VersionActivity.this);
                }
            } else if(menuItem.getItemId() == R.id.navCancel) {
                this.manageControls(false, true, false);
            } else if(menuItem.getItemId() == R.id.navSave) {
                try {
                    if (this.versionValidator.getState()) {
                        this.controlsToObject();
                        new VersionTask(VersionActivity.this, this.bugService, this.currentProject, false, this.settings.showNotifications(), "", R.drawable.icon_versions).execute(this.currentVersion).get();
                        this.reload();
                        this.manageControls(false, true, false);
                    } else {
                        super.createSnackBar(this.versionValidator.getResult());
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, VersionActivity.this);
                }
            }
            return true;
        });

        // init controls
        this.lvVersions = this.findViewById(R.id.lvVersions);
        this.txtVersionTitle = this.findViewById(R.id.txtVersionTitle);
        this.txtVersionDescription = this.findViewById(R.id.txtVersionDescription);
        this.txtVersionReleasedAt = this.findViewById(R.id.txtVersionReleasedAt);
        this.txtVersionReleasedAt.setTimePicker(true);
        this.chkVersionReleased = this.findViewById(R.id.chkVersionReleased);
        this.chkVersionDeprecated = this.findViewById(R.id.chkVersionDeprecated);
        this.spVersionFilter = this.findViewById(R.id.spVersionFilter);
        this.pbVersion = this.findViewById(R.id.pbVersion);
        this.pbVersion.setVisibility(View.GONE);
        this.pbVersion.setMax(100);

        this.rowVersionReleased = this.findViewById(R.id.rowVersionReleased);
        this.rowVersionDeprecated = this.findViewById(R.id.rowVersionDeprecated);
        this.rowVersionReleasedAt = this.findViewById(R.id.rowVersionReleasedAt);
        this.rowVersionFilter = this.findViewById(R.id.rowVersionFilter);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.permissions = this.bugService.getPermissions();
        this.currentProject = this.settings.getCurrentProjectId();
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidator() {
        this.versionValidator = new Validator(this.getApplicationContext(), R.mipmap.ic_launcher_round);
        this.versionValidator.addEmptyValidator(this.txtVersionTitle);
        this.versionValidator.addDateValidator(this.txtVersionReleasedAt, this.settings.getDateFormat(), false);
    }

    @Override
    protected void reload() {
        try {
            if (this.permissions.listVersions()) {
                if (this.currentProject != null) {
                    this.lvVersions.getAdapter().clear();
                    String filterAction = "versions";
                    if (filter != null) {
                        if (this.filter.equals(getString(R.string.versions_released))) {
                            filterAction = "released_versions";
                        } else if (this.filter.equals(getString(R.string.versions_unReleased))) {
                            filterAction = "unreleased_versions";
                        } else {
                            filterAction = "versions";
                        }
                    }
                    VersionTask versionTask = new VersionTask(VersionActivity.this, this.bugService, this.currentProject, false, this.settings.showNotifications(), filterAction, R.drawable.icon_versions);
                    for (Version<?> version : versionTask.execute(0).get()) {
                        BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                        baseDescriptionObject.setObject(version);
                        baseDescriptionObject.setTitle(version.getTitle());
                        baseDescriptionObject.setDescription(version.getDescription());
                        baseDescriptionObject.setState(version.isReleasedVersion());
                        this.lvVersions.getAdapter().add(baseDescriptionObject);
                    }

                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getApplicationContext());
        }
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode && this.permissions.addVersions());
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected && this.permissions.updateVersions());
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected && this.permissions.deleteVersions());
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.lvVersions.setEnabled(!editMode);
        this.spVersionFilter.setEnabled(!editMode);
        this.txtVersionTitle.setEnabled(editMode);
        this.txtVersionDescription.setEnabled(editMode);
        this.txtVersionReleasedAt.setEnabled(editMode);
        this.chkVersionReleased.setEnabled(editMode);
        this.chkVersionDeprecated.setEnabled(editMode);

        if (reset) {
            this.currentVersion = new Version<>();
            this.objectToControls();
        }
    }

    private void objectToControls() {
        if (this.currentVersion != null) {
            this.txtVersionTitle.setText(this.currentVersion.getTitle());
            this.txtVersionDescription.setText(this.currentVersion.getDescription());
            Date date = new Date();
            date.setTime(this.currentVersion.getReleasedVersionAt());
            this.txtVersionReleasedAt.setText(ConvertHelper.convertDateToString(date, this.settings.getDateFormat() + " " + this.settings.getTimeFormat()));
            this.chkVersionDeprecated.setChecked(this.currentVersion.isDeprecatedVersion());
            this.chkVersionReleased.setChecked(this.currentVersion.isReleasedVersion());
        }
    }

    private void controlsToObject() {
        try {
            if (this.currentVersion != null) {
                this.currentVersion.setTitle(this.txtVersionTitle.getText().toString());
                this.currentVersion.setDescription(this.txtVersionDescription.getText().toString());
                this.currentVersion.setReleasedVersionAt(Helper.checkDateAndReturn(this.txtVersionReleasedAt, this.getApplicationContext()).getTime());
                this.currentVersion.setReleasedVersion(this.chkVersionReleased.isChecked());
                this.currentVersion.setDeprecatedVersion(this.chkVersionDeprecated.isChecked());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, VersionActivity.this);
        }
    }

    private void updateUITrackerSpecific() {
        Authentication.Tracker tracker;
        if (this.settings.getCurrentAuthentication() != null) {
            tracker = this.settings.getCurrentAuthentication().getTracker();
        } else {
            return;
        }

        this.rowVersionReleasedAt.setVisibility(View.GONE);
        this.rowVersionReleased.setVisibility(View.GONE);
        this.rowVersionDeprecated.setVisibility(View.GONE);
        this.rowVersionFilter.setVisibility(View.GONE);

        if (tracker != null) {
            switch (tracker) {
                case MantisBT:
                case Local:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    this.rowVersionFilter.setVisibility(View.VISIBLE);
                    break;
                case RedMine:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    this.chkVersionDeprecated.setText(this.getString(R.string.versions_deprecated_redmine));
                    this.chkVersionDeprecated.setOnCheckedChangeListener((buttonView, isChecked) -> this.chkVersionReleased.setVisibility(isChecked ? View.GONE : View.VISIBLE));
                    break;
                case YouTrack:
                case OpenProject:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    break;
                case Bugzilla:
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    break;
                case Github:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    break;
                case Jira:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    break;
                case Backlog:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_versions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        RoadMapDialog roadMapDialog;
        if(item.getItemId() == R.id.menChangelog) {
            FilePickerDialog dialog = Helper.initFilePickerDialog(VersionActivity.this, true, null, this.getString(R.string.versions_menu_changelog_dir));
            dialog.setDialogSelectionListener(files -> {
                try {
                    if(files != null) {
                        if (this.currentVersion != null) {
                            Object vid = this.currentVersion.getId();
                            Object pid = this.currentProject;

                            this.pbVersion.setVisibility(View.VISIBLE);
                            ExportTask exportTask = new ExportTask(this.act, this.bugService, pid, files[0], this.notification, R.mipmap.ic_launcher_round, this.bg, this.icon, this.pbVersion);
                            exportTask.after(o -> {
                                pbVersion.setVisibility(View.GONE);
                                MessageHelper.printMessage(getString(R.string.versions_menu_changelog_created), R.mipmap.ic_launcher_round, VersionActivity.this);
                            });
                            exportTask.execute(vid);
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.versions_menu_changelog_no_selected), R.mipmap.ic_launcher_round, VersionActivity.this);
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, VersionActivity.this);
                }
            });
            dialog.show();
        } else if(item.getItemId() == R.id.menShowChangelog) {
            roadMapDialog = RoadMapDialog.newInstance(false, this.currentProject, this.currentVersion.getId());
            roadMapDialog.show(this.getSupportFragmentManager(), "roadMapDialog");
        } else if(item.getItemId() == R.id.menShowRoadMap) {
            roadMapDialog = RoadMapDialog.newInstance(true, this.currentProject, this.currentVersion.getId());
            roadMapDialog.show(this.getSupportFragmentManager(), "roadMapDialog");
        }
        return super.onOptionsItemSelected(item);
    }
}
