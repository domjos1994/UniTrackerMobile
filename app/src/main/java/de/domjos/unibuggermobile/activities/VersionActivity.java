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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableRow;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.tasks.versions.ListVersionTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;

public final class VersionActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private ListView lvVersions;
    private ListAdapter versionAdapter;
    private EditText txtVersionTitle, txtVersionDescription, txtVersionReleasedAt;
    private CheckBox chkVersionReleased, chkVersionDeprecated;
    private TableRow rowVersionReleased, rowVersionDeprecated, rowVersionReleasedAt;

    private IBugService bugService;
    private Project currentProject;
    private Version currentVersion;

    public VersionActivity() {
        super(R.layout.version_activity);
    }

    @Override
    protected void initActions() {

    }

    @Override
    protected void initControls() {
        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navAdd:

                    break;
                case R.id.navEdit:

                    break;
                case R.id.navDelete:

                    break;
                case R.id.navCancel:

                    break;
                case R.id.navSave:

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

        this.rowVersionReleased = this.findViewById(R.id.rowVersionReleased);
        this.rowVersionDeprecated = this.findViewById(R.id.rowVersionDeprecated);
        this.rowVersionReleasedAt = this.findViewById(R.id.rowVersionReleasedAt);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.currentProject = MainActivity.settings.getCurrentProject(VersionActivity.this, this.bugService);
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidators() {

    }

    @Override
    protected void reload() {
        try {
            this.versionAdapter.clear();
            ListVersionTask versionTask = new ListVersionTask(VersionActivity.this, this.bugService, this.currentProject.getId());
            for (Version version : versionTask.execute().get()) {
                ListObject listObject = new ListObject(this.getApplicationContext(), R.drawable.ic_update_black_24dp, version.getTitle(), version.getDescription());
                listObject.setId(String.valueOf(version.getId()));
                this.versionAdapter.add(listObject);
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
        this.txtVersionTitle.setEnabled(editMode);
        this.txtVersionDescription.setEnabled(editMode);
        this.txtVersionReleasedAt.setEnabled(editMode);
        this.chkVersionReleased.setEnabled(editMode);
        this.chkVersionDeprecated.setEnabled(editMode);

        this.rowVersionReleasedAt = this.findViewById(R.id.rowVersionReleasedAt);
        this.rowVersionReleased = this.findViewById(R.id.rowVersionReleased);
        this.rowVersionDeprecated = this.findViewById(R.id.rowVersionDeprecated);
    }

    private void updateUITrackerSpecific() {
        this.rowVersionReleasedAt.setVisibility(View.GONE);
        this.rowVersionReleased.setVisibility(View.GONE);
        this.rowVersionDeprecated.setVisibility(View.GONE);

        switch (MainActivity.settings.getCurrentAuthentication().getTracker()) {
            case MantisBT:
                this.rowVersionReleasedAt.setVisibility(View.VISIBLE);
                this.rowVersionReleased.setVisibility(View.VISIBLE);
                this.rowVersionDeprecated.setVisibility(View.VISIBLE);
                break;
            case RedMine:

                break;
            case YouTrack:

                break;
            case Bugzilla:

                break;
        }
    }
}