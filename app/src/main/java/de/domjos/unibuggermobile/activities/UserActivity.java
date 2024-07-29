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

import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.domjos.unitrackerlibrary.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackerlibrary.model.BaseDescriptionObject;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.User;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.UserTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unitrackerlibrary.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;
import de.domjos.unitrackerlibrary.tools.Notifications;
import de.domjos.unitrackerlibrary.tools.Validator;

public final class UserActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private SwipeRefreshDeleteList lvUsers;
    private EditText txtUserPassword, txtUserPasswordRepeat;
    private EditText txtUserName, txtUserFullName, txtUserEmail;

    private IBugService<?> bugService;
    private IFunctionImplemented permissions;
    private Project<?> currentProject;
    private User<?> currentUser;

    private Validator userValidator;
    private Settings settings;

    public UserActivity() {
        super(R.layout.user_activity);
    }

    @Override
    protected void initActions() {
        this.lvUsers.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            if (listObject != null) {
                if (listObject.getObject() instanceof User) {
                    currentUser = (User<?>) listObject.getObject();
                    objectToControls();
                    manageControls(false, false, true);
                }
            }
        });

        this.lvUsers.setOnReloadListener(this::reload);

        this.lvUsers.setOnDeleteListener(listObject -> {
            if(bugService.getPermissions().deleteUsers()) {
                try {
                    new UserTask(UserActivity.this, bugService, currentProject.getId(), true, settings.showNotifications(), R.drawable.icon_users).execute(((User<?>)listObject.getObject()).getId()).get();
                    manageControls(false, true, false);
                } catch (Exception ex) {
                    Notifications.printException(UserActivity.this, ex, R.mipmap.ic_launcher_round);
                }
            }
        });
    }

    @Override
    protected void reload() {
        try {
            this.lvUsers.getAdapter().clear();
            if (this.currentProject != null) {
                if (this.permissions.listUsers()) {
                    for (User<?> user : new UserTask(UserActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications(), R.drawable.icon_users).execute(0).get()) {
                        BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                        baseDescriptionObject.setObject(user);
                        baseDescriptionObject.setTitle(user.getTitle());
                        baseDescriptionObject.setDescription(user.getDescription());
                        this.lvUsers.getAdapter().add(baseDescriptionObject);
                    }
                }
            }
        } catch (Exception ex) {
            Notifications.printException(UserActivity.this, ex, R.mipmap.ic_launcher_round);
        }
    }

    @Override
    protected void initControls() {
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
                    new UserTask(UserActivity.this, this.bugService, this.currentProject.getId(), true, this.settings.showNotifications(), R.drawable.icon_users).execute(this.currentUser.getId()).get();
                    this.reload();
                    this.manageControls(false, true, false);
                } catch (Exception ex) {
                    Notifications.printException(UserActivity.this, ex, R.mipmap.ic_launcher_round);
                }
            } else if(menuItem.getItemId() == R.id.navCancel) {
                this.manageControls(false, true, false);
            } else if(menuItem.getItemId() == R.id.navSave) {
                try {
                    if (this.userValidator.getState()) {
                        this.controlsToObject();
                        new UserTask(UserActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications(), R.drawable.icon_users).execute(this.currentUser).get();
                        this.reload();
                        this.manageControls(false, true, false);
                    } else {
                        super.createSnackBar(this.userValidator.getResult());
                    }
                } catch (Exception ex) {
                    Notifications.printException(UserActivity.this, ex, R.mipmap.ic_launcher_round);
                }
            }
            return true;
        });

        // init controls
        this.lvUsers = this.findViewById(R.id.lvUsers);
        this.txtUserName = this.findViewById(R.id.txtUserName);
        this.txtUserFullName = this.findViewById(R.id.txtUserFullName);
        this.txtUserEmail = this.findViewById(R.id.txtUserEmail);
        this.txtUserPassword = this.findViewById(R.id.txtUserPassword);
        this.txtUserPasswordRepeat = this.findViewById(R.id.txtUserPasswordRepeat);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.permissions = this.bugService.getPermissions();
        this.currentProject = this.settings.getCurrentProject(UserActivity.this, this.bugService);
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidator() {
        this.userValidator = new Validator(this.getApplicationContext(), R.mipmap.ic_launcher_round);
        this.userValidator.addEmptyValidator(this.txtUserName);
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode && this.permissions.addUsers());
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected && this.permissions.updateUsers());
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected && this.permissions.deleteUsers() && !this.currentUser.getTitle().equals(this.settings.getCurrentAuthentication().getUserName()));
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.lvUsers.setEnabled(!editMode);

        this.txtUserName.setEnabled(editMode);
        this.txtUserFullName.setEnabled(editMode);
        this.txtUserEmail.setEnabled(editMode);
        this.txtUserPassword.setEnabled(editMode);
        this.txtUserPasswordRepeat.setEnabled(editMode);

        if (reset) {
            this.currentUser = new User<>();
            this.objectToControls();
        }
    }

    private void objectToControls() {
        this.txtUserName.setText(this.currentUser.getTitle());
        this.txtUserFullName.setText(this.currentUser.getRealName());
        this.txtUserEmail.setText(this.currentUser.getEmail());
        this.txtUserPassword.setText(this.currentUser.getPassword());
    }

    private void controlsToObject() {
        this.currentUser.setTitle(this.txtUserName.getText().toString());
        this.currentUser.setRealName(this.txtUserFullName.getText().toString());
        this.currentUser.setEmail(this.txtUserEmail.getText().toString());
        if (this.txtUserPassword.getText().toString().equals(this.txtUserPasswordRepeat.getText().toString())) {
            this.currentUser.setPassword(this.txtUserPassword.getText().toString());
        }
    }

    private void updateUITrackerSpecific() {
        Authentication.Tracker tracker;
        if (this.settings.getCurrentAuthentication() != null) {
            tracker = this.settings.getCurrentAuthentication().getTracker();
        } else {
            return;
        }

        System.out.println(tracker);
    }
}
