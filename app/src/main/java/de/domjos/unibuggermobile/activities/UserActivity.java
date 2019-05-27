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
import android.widget.ListView;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;
import de.domjos.unibuggermobile.settings.Settings;

public final class UserActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private ListView lvUsers;
    private ListAdapter userAdapter;

    private IBugService bugService;
    private IFunctionImplemented permissions;
    private Project currentProject;
    private User currentUser;

    private Validator userValidator;
    private Settings settings;

    protected UserActivity() {
        super(R.layout.user_activity);
    }

    @Override
    protected void initActions() {

    }

    @Override
    protected void reload() {
        try {
            if (this.permissions.listUsers()) {

            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, UserActivity.this);
        }
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
                        this.reload();
                        this.manageControls(false, true, false);
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, UserActivity.this);
                    }
                    break;
                case R.id.navCancel:
                    this.manageControls(false, true, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.userValidator.getState()) {

                            this.reload();
                            this.manageControls(false, true, false);
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, UserActivity.this);
                    }
                    break;
            }
            return true;
        });

        // init controls
        this.lvUsers = this.findViewById(R.id.lvUsers);
        this.userAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_person_black_24dp);
        this.lvUsers.setAdapter(this.userAdapter);
        this.userAdapter.notifyDataSetChanged();

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.permissions = this.bugService.getPermissions();
        this.currentProject = this.settings.getCurrentProject(UserActivity.this, this.bugService);
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidators() {
        this.userValidator = new Validator(this.getApplicationContext());
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode && this.permissions.addUsers());
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected && this.permissions.updateUsers());
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected && this.permissions.deleteUsers());
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);
    }

    private void objectToControls() {

    }

    private void controlsToObject() {

    }

    private void updateUITrackerSpecific() {
        Authentication.Tracker tracker;
        if (this.settings.getCurrentAuthentication() != null) {
            tracker = this.settings.getCurrentAuthentication().getTracker();
        } else {
            return;
        }
    }
}
