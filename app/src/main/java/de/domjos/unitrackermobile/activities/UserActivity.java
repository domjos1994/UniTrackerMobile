/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.activities;

import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.UserTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;
import de.domjos.unitrackermobile.settings.Settings;

public final class UserActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private SwipeRefreshDeleteList lvUsers;
    private EditText txtUserPassword, txtUserPasswordRepeat;
    private EditText txtUserName, txtUserFullName, txtUserEmail;

    private IBugService bugService;
    private IFunctionImplemented permissions;
    private Project currentProject;
    private User currentUser;

    private Validator userValidator;
    private Settings settings;

    public UserActivity() {
        super(R.layout.user_activity);
    }

    @Override
    protected void initActions() {
        this.lvUsers.click(new SwipeRefreshDeleteList.ClickListener() {
            @Override
            public void onClick(ListObject listObject) {
                if (listObject != null) {
                    currentUser = (User) listObject.getDescriptionObject();
                    objectToControls();
                    manageControls(false, false, true);
                }
            }
        });

        this.lvUsers.reload(new SwipeRefreshDeleteList.ReloadListener() {
            @Override
            public void onReload() {
                reload();
            }
        });

        this.lvUsers.deleteItem(new SwipeRefreshDeleteList.DeleteListener() {
            @Override
            public void onDelete(ListObject listObject) {
                try {
                    new UserTask(UserActivity.this, bugService, currentProject.getId(), true, settings.showNotifications(), R.drawable.ic_person_black_24dp).execute(listObject.getDescriptionObject().getId()).get();
                    reload();
                    manageControls(false, true, false);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, UserActivity.this);
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
                    for (User user : new UserTask(UserActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications(), R.drawable.ic_person_black_24dp).execute(0).get()) {
                        this.lvUsers.getAdapter().add(new ListObject(this.getApplicationContext(), R.drawable.ic_person_black_24dp, user));
                    }
                }
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
                        new UserTask(UserActivity.this, this.bugService, this.currentProject.getId(), true, this.settings.showNotifications(), R.drawable.ic_person_black_24dp).execute(this.currentUser.getId()).get();
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
                            this.controlsToObject();
                            new UserTask(UserActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications(), R.drawable.ic_person_black_24dp).execute(this.currentUser).get();
                            this.reload();
                            this.manageControls(false, true, false);
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), this.getApplicationContext());
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
    protected void initValidators() {
        this.userValidator = new Validator(this.getApplicationContext());
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
            this.currentUser = new User();
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
