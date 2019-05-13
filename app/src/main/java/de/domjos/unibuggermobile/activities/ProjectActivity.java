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
import android.widget.EditText;
import android.widget.ListView;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.tracker.Bugzilla;
import de.domjos.unibuggerlibrary.services.tracker.MantisBT;
import de.domjos.unibuggerlibrary.services.tracker.Redmine;
import de.domjos.unibuggerlibrary.services.tracker.SQLite;
import de.domjos.unibuggerlibrary.services.tracker.YouTrack;
import de.domjos.unibuggerlibrary.tasks.projects.ListProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;

public class ProjectActivity extends AbstractActivity {
    private BottomNavigationView navigationView;
    private ListView lvProjects;
    private ListAdapter listAdapter;

    private EditText txtProjectTitle, txtProjectDescription;

    private IBugService bugService;
    private Project currentProject;
    private Validator projectValidator;

    public ProjectActivity() {
        super(R.layout.project_activity);
    }

    @Override
    protected void initActions() {

    }

    @Override
    protected void initControls() {
        // init bottom-navigation
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navAdd:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navEdit:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navDelete:
                    this.manageControls(false, false, false);
                    break;
                case R.id.navCancel:
                    this.manageControls(false, false, false);
                    break;
                case R.id.navSave:
                    if (this.projectValidator.getState()) {
                        this.manageControls(false, false, false);
                    }
                    break;
            }
            return true;
        });

        // init controls
        this.lvProjects = this.findViewById(R.id.lvProjects);
        this.listAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_apps_black_24dp);
        this.lvProjects.setAdapter(this.listAdapter);
        this.listAdapter.notifyDataSetChanged();

        this.txtProjectTitle = this.findViewById(R.id.txtProjectTitle);
        this.txtProjectDescription = this.findViewById(R.id.txtProjectDescription);

        this.getCurrentBugService();
    }

    @Override
    protected void initValidators() {
        this.projectValidator = new Validator(this.getApplicationContext());
        this.projectValidator.addEmptyValidator(this.txtProjectTitle);
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.lvProjects.setEnabled(!editMode);
        this.txtProjectTitle.setEnabled(editMode);
        this.txtProjectDescription.setEnabled(editMode);

        if (reset) {
            this.currentProject = new Project();
            this.objectToControls();
        }
    }

    @Override
    protected void reload() {
        try {
            ListProjectTask task = new ListProjectTask(ProjectActivity.this, this.bugService);
            this.listAdapter.clear();
            for (Project project : task.execute().get()) {
                this.listAdapter.add(new ListObject(this.getApplicationContext(), null, project.getTitle(), project.getDescription()));
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }


    private void getCurrentBugService() {
        try {
            Authentication authentication = MainActivity.settings.getCurrentAuthentication();
            if (authentication != null) {
                switch (authentication.getTracker()) {
                    case MantisBT:
                        this.bugService = new MantisBT(authentication);
                        break;
                    case Bugzilla:
                        this.bugService = new Bugzilla(authentication);
                        break;
                    case YouTrack:
                        this.bugService = new YouTrack(authentication);
                        break;
                    case RedMine:
                        this.bugService = new Redmine(authentication);
                        break;
                    default:
                        this.bugService = new SQLite(this.getApplicationContext(), Helper.getVersionCode(this.getApplicationContext()));
                        break;
                }
            } else {
                this.bugService = new SQLite(this.getApplicationContext(), Helper.getVersionCode(this.getApplicationContext()));
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getApplicationContext());
        }
    }

    private void objectToControls() {
        if (this.currentProject != null) {
            this.txtProjectTitle.setText(this.currentProject.getTitle());
            this.txtProjectDescription.setText(this.currentProject.getDescription());
        }
    }

    private void controlsToObject() {
        if (this.currentProject != null) {
            this.currentProject.setTitle(this.txtProjectTitle.getText().toString());
            this.currentProject.setDescription(this.txtProjectDescription.getText().toString());
        }
    }
}
