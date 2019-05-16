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

import android.support.design.widget.BottomNavigationView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.tasks.versions.ListVersionTask;
import de.domjos.unibuggerlibrary.tasks.versions.VersionTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;

public final class VersionActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private ListView lvVersions;
    private ListAdapter versionAdapter;
    private EditText txtVersionTitle, txtVersionDescription, txtVersionReleasedAt;
    private CheckBox chkVersionReleased, chkVersionDeprecated;
    private Spinner spVersionFilter;
    private TableRow rowVersionReleased, rowVersionDeprecated, rowVersionReleasedAt;
    private LinearLayout rowVersionFilter;

    private IBugService bugService;
    private Project currentProject;
    private Version currentVersion;

    private Validator versionValidator;

    private String filter;

    public VersionActivity() {
        super(R.layout.version_activity);
    }

    @Override
    protected void initActions() {
        this.lvVersions.setOnItemClickListener((parent, view, position, id) -> {
            try {
                ListObject listObject = this.versionAdapter.getItem(position);
                if (listObject != null) {
                    this.currentVersion = (Version) listObject.getDescriptionObject();
                    this.objectToControls();
                    this.manageControls(false, false, true);
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, VersionActivity.this);
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
                        new VersionTask(VersionActivity.this, this.bugService, this.currentProject.getId(), true).execute(this.currentVersion).get();
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
                            new VersionTask(VersionActivity.this, this.bugService, this.currentProject.getId(), false).execute(this.currentVersion).get();
                            this.reload();
                            this.manageControls(false, true, false);
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
        this.versionAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_update_black_24dp);
        this.lvVersions.setAdapter(this.versionAdapter);
        this.versionAdapter.notifyDataSetChanged();

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
        this.currentProject = MainActivity.settings.getCurrentProject(VersionActivity.this, this.bugService);
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidators() {
        this.versionValidator = new Validator(this.getApplicationContext());
        this.versionValidator.addEmptyValidator(this.txtVersionTitle);
    }

    @Override
    protected void reload() {
        try {
            if (this.currentProject != null) {
                if (this.currentProject.getId() != null) {
                    this.versionAdapter.clear();
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
                    ListVersionTask versionTask = new ListVersionTask(VersionActivity.this, this.bugService, this.currentProject.getId(), filterAction);
                    for (Version version : versionTask.execute().get()) {
                        ListObject listObject = new ListObject(this.getApplicationContext(), R.drawable.ic_update_black_24dp, version);
                        this.versionAdapter.add(listObject);
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected);
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
            this.txtVersionReleasedAt.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN).format(date));
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
                Date dt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN).parse(strDate);
                this.currentVersion.setReleasedVersionAt(dt.getTime());
                this.currentVersion.setReleasedVersion(this.chkVersionReleased.isChecked());
                this.currentVersion.setDeprecatedVersion(this.chkVersionDeprecated.isChecked());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, VersionActivity.this);
        }
    }

    private void updateUITrackerSpecific() {
        this.rowVersionReleasedAt.setVisibility(View.GONE);
        this.rowVersionReleased.setVisibility(View.GONE);
        this.rowVersionDeprecated.setVisibility(View.GONE);
        this.rowVersionFilter.setVisibility(View.GONE);

        switch (MainActivity.settings.getCurrentAuthentication().getTracker()) {
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

                break;
            case Bugzilla:

                break;
        }
    }
}
