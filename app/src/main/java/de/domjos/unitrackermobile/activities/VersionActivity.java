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

import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.ListObject;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.VersionTask;
import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.custom.swiperefreshlist.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.DateConverter;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;
import de.domjos.unitrackermobile.settings.Settings;

public final class VersionActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private SwipeRefreshDeleteList lvVersions;
    private EditText txtVersionTitle, txtVersionDescription, txtVersionReleasedAt;
    private CheckBox chkVersionReleased, chkVersionDeprecated;
    private Spinner spVersionFilter;
    private TableRow rowVersionReleased, rowVersionDeprecated, rowVersionReleasedAt;
    private LinearLayout rowVersionFilter;

    private IBugService bugService;
    private IFunctionImplemented permissions;
    private Object currentProject;
    private Version currentVersion;

    private Validator versionValidator;
    private Settings settings;

    private String filter;

    public VersionActivity() {
        super(R.layout.version_activity);
    }

    @Override
    protected void initActions() {
        this.lvVersions.delete(new SwipeRefreshDeleteList.DeleteListener() {
            @Override
            public void onDelete(ListObject listObject) {
                try {
                    new VersionTask(VersionActivity.this, bugService, currentProject, true, settings.showNotifications(), "", R.drawable.ic_update_black_24dp).execute(listObject.getDescriptionObject().getId()).get();
                } catch (Exception ex) {
                    MessageHelper.printException(ex, VersionActivity.this);
                }
            }
        });

        this.lvVersions.click(new SwipeRefreshDeleteList.ClickListener() {
            @Override
            public void onClick(ListObject listObject) {
                currentVersion = (Version) listObject.getDescriptionObject();
                objectToControls();
                manageControls(false, false, true);
            }
        });

        this.lvVersions.reload(new SwipeRefreshDeleteList.ReloadListener() {
            @Override
            public void onReload() {
                reload();
            }
        });

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
        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navAdd:
                    this.manageControls(true, true, false);
                    break;
                case R.id.navEdit:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navDelete:
                    try {
                        new VersionTask(VersionActivity.this, this.bugService, this.currentProject, true, this.settings.showNotifications(), "", R.drawable.ic_update_black_24dp).execute(this.currentVersion.getId()).get();
                        this.reload();
                        this.manageControls(false, true, false);
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, VersionActivity.this);
                    }
                    break;
                case R.id.navCancel:
                    this.manageControls(false, true, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.versionValidator.getState()) {
                            this.controlsToObject();
                            new VersionTask(VersionActivity.this, this.bugService, this.currentProject, false, this.settings.showNotifications(), "", R.drawable.ic_update_black_24dp).execute(this.currentVersion).get();
                            this.reload();
                            this.manageControls(false, true, false);
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), this.getApplicationContext());
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, VersionActivity.this);
                    }
                    break;
            }
            return true;
        });

        // init controls
        this.lvVersions = this.findViewById(R.id.lvVersions);
        this.txtVersionTitle = this.findViewById(R.id.txtVersionTitle);
        this.txtVersionDescription = this.findViewById(R.id.txtVersionDescription);
        this.txtVersionReleasedAt = this.findViewById(R.id.txtVersionReleasedAt);
        this.chkVersionReleased = this.findViewById(R.id.chkVersionReleased);
        this.chkVersionDeprecated = this.findViewById(R.id.chkVersionDeprecated);
        this.spVersionFilter = this.findViewById(R.id.spVersionFilter);

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
    protected void initValidators() {
        this.versionValidator = new Validator(this.getApplicationContext());
        this.versionValidator.addEmptyValidator(this.txtVersionTitle);
        this.versionValidator.addValueEqualsDate(this.txtVersionReleasedAt);
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
                    VersionTask versionTask = new VersionTask(VersionActivity.this, this.bugService, this.currentProject, false, this.settings.showNotifications(), filterAction, R.drawable.ic_update_black_24dp);
                    for (Version version : versionTask.execute(0).get()) {
                        ListObject listObject = new ListObject(this.getApplicationContext(), R.drawable.ic_update_black_24dp, version);
                        this.lvVersions.getAdapter().add(listObject);
                    }

                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
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
            this.currentVersion = new Version();
            this.objectToControls();
        }
    }

    private void objectToControls() {
        if (this.currentVersion != null) {
            this.txtVersionTitle.setText(this.currentVersion.getTitle());
            this.txtVersionDescription.setText(this.currentVersion.getDescription());
            Date date = new Date();
            date.setTime(this.currentVersion.getReleasedVersionAt());
            this.txtVersionReleasedAt.setText(DateConverter.convertDateTimeToString(date, this.getApplicationContext()));
            this.chkVersionDeprecated.setChecked(this.currentVersion.isDeprecatedVersion());
            this.chkVersionReleased.setChecked(this.currentVersion.isReleasedVersion());
        }
    }

    private void controlsToObject() {
        try {
            if (this.currentVersion != null) {
                this.currentVersion.setTitle(this.txtVersionTitle.getText().toString());
                this.currentVersion.setDescription(this.txtVersionDescription.getText().toString());
                String strDate = this.txtVersionReleasedAt.getText().toString();
                this.currentVersion.setReleasedVersionAt(DateConverter.convertStringToDate(strDate, this.getApplicationContext()).getTime());
                this.currentVersion.setReleasedVersion(this.chkVersionReleased.isChecked());
                this.currentVersion.setDeprecatedVersion(this.chkVersionDeprecated.isChecked());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, VersionActivity.this);
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
                case OpenProject:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    break;
                case Backlog:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    break;
                case Local:
                    this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                    this.rowVersionReleased.setVisibility(View.VISIBLE);
                    this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                    this.rowVersionFilter.setVisibility(View.VISIBLE);
                    break;

            }
        }
    }
}
