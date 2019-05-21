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

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.issues.IssuesTask;
import de.domjos.unibuggerlibrary.tasks.issues.ListIssueTask;
import de.domjos.unibuggerlibrary.tasks.projects.ListProjectTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.SQLiteGeneral;
import de.domjos.unibuggermobile.settings.Globals;
import de.domjos.unibuggermobile.settings.Settings;

public final class MainActivity extends AbstractActivity implements OnNavigationItemSelectedListener {
    private FloatingActionButton cmdIssuesAdd;
    private DrawerLayout drawerLayout;
    private ImageView ivMainCover;
    private TextView lblMainCommand;
    private TextView lblAccountTitle;
    private Spinner spMainAccounts, spMainProjects;
    private ListView lvMainIssues;
    private ListAdapter issueAdapter;
    private ArrayAdapter<String> accountList, projectList;
    private IBugService bugService;
    private IFunctionImplemented permissions;

    private static final int RELOAD_PROJECTS = 98;
    private static final int RELOAD_ACCOUNTS = 99;
    private static final int RELOAD_ISSUES = 101;
    public static final Globals globals = new Globals();
    public static Settings settings;


    public MainActivity() {
        super(R.layout.main_activity);
    }

    @Override
    protected void initActions() {
        this.lblMainCommand.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), AccountActivity.class);
            startActivityForResult(intent, MainActivity.RELOAD_ACCOUNTS);
        });

        this.spMainAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = accountList.getItem(position);
                if (item != null) {
                    if (!item.trim().isEmpty()) {
                        Authentication authentication = MainActivity.globals.getSqLiteGeneral().getAccounts("title='" + item + "'").get(0);
                        if (authentication != null) {
                            MainActivity.settings.setCurrentAuthentication(authentication);
                        } else {
                            MainActivity.settings.setCurrentAuthentication(null);
                        }
                    } else {
                        MainActivity.settings.setCurrentAuthentication(null);
                    }
                } else {
                    MainActivity.settings.setCurrentAuthentication(null);
                }
                fillFields();
                reloadProjects();
                selectProject();
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spMainProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setCurrentProject(projectList.getItem(position));
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.lvMainIssues.setOnItemClickListener((parent, view, position, id) -> {
            ListObject ls = this.issueAdapter.getItem(position);
            if (ls != null) {
                Intent intent = new Intent(this.getApplicationContext(), IssueActivity.class);
                intent.putExtra("id", String.valueOf(ls.getDescriptionObject().getId()));
                intent.putExtra("pid", String.valueOf(MainActivity.settings.getCurrentProject(MainActivity.this, this.bugService).getId()));
                this.startActivityForResult(intent, MainActivity.RELOAD_ISSUES);
            }
        });

        this.lvMainIssues.setOnItemLongClickListener((parent, view, position, id) -> {
            try {
                if (this.permissions.deleteIssues()) {
                    ListObject listObject = this.issueAdapter.getItem(position);
                    if (listObject != null) {
                        if (listObject.getDescriptionObject() != null) {
                            Project project = MainActivity.settings.getCurrentProject(MainActivity.this, this.bugService);
                            new IssuesTask(MainActivity.this, this.bugService, project.getId(), true).execute((Issue) listObject.getDescriptionObject()).get();
                            reload();
                        }
                    }
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, this.getApplicationContext());
            }
            return true;
        });

        this.cmdIssuesAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), IssueActivity.class);
            intent.putExtra("id", "");
            intent.putExtra("pid", String.valueOf(MainActivity.settings.getCurrentProject(MainActivity.this, this.bugService).getId()));
            this.startActivityForResult(intent, MainActivity.RELOAD_ISSUES);
        });
    }

    @Override
    protected void initControls() {
        try {
            // init Toolbar
            Toolbar toolbar = this.findViewById(R.id.toolbar);
            this.setSupportActionBar(toolbar);

            // init Drawer-Layout
            this.drawerLayout = this.findViewById(R.id.drawer_layout);

            // init Navigation-View
            NavigationView navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.drawerLayout, toolbar, R.string.app_name, R.string.app_name);
            this.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(this);

            this.ivMainCover = navigationView.getHeaderView(0).findViewById(R.id.ivMainCover);
            this.lblMainCommand = navigationView.getHeaderView(0).findViewById(R.id.lblMainCommand);
            this.lblAccountTitle = navigationView.getHeaderView(0).findViewById(R.id.lblAccountTitle);

            this.cmdIssuesAdd = this.findViewById(R.id.cmdIssueAdd);
            this.spMainAccounts = navigationView.getHeaderView(0).findViewById(R.id.spMainAccounts);
            this.accountList = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
            this.spMainAccounts.setAdapter(this.accountList);
            this.accountList.notifyDataSetChanged();

            this.spMainProjects = this.findViewById(R.id.spMainProjects);
            this.projectList = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
            this.spMainProjects.setAdapter(this.projectList);
            this.projectList.notifyDataSetChanged();

            this.lvMainIssues = this.findViewById(R.id.lvMainIssues);
            this.issueAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_bug_report_black_24dp);
            this.lvMainIssues.setAdapter(this.issueAdapter);
            this.issueAdapter.notifyDataSetChanged();

            MainActivity.globals.setSqLiteGeneral(new SQLiteGeneral(this.getApplicationContext()));

            this.reloadAccounts();
            MainActivity.settings = new Settings(getApplicationContext());
            Authentication authentication = MainActivity.settings.getCurrentAuthentication();
            if (authentication != null) {
                this.spMainAccounts.setSelection(this.accountList.getPosition(authentication.getTitle()));
            } else {
                this.spMainAccounts.setSelection(this.accountList.getPosition(""));
            }
            this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
            this.permissions = Helper.getCurrentPermissions(this.getApplicationContext());
            if (this.permissions.addIssues()) {
                this.cmdIssuesAdd.show();
            } else {
                this.cmdIssuesAdd.hide();
            }


            this.reloadProjects();
            this.selectProject();
        } catch (Exception ex) {
            MessageHelper.printException(ex, MainActivity.this);
        }
    }

    @Override
    protected void reload() {
        try {
            this.issueAdapter.clear();
            if (this.permissions.listIssues()) {
                Project project = MainActivity.settings.getCurrentProject(MainActivity.this, this.bugService);
                if (project != null) {
                    ListIssueTask listIssueTask = new ListIssueTask(MainActivity.this, this.bugService, project.getId());
                    for (Object issue : listIssueTask.execute().get()) {
                        this.issueAdapter.add(new ListObject(MainActivity.this, R.drawable.ic_bug_report_black_24dp, (Issue) issue));
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, MainActivity.this);
        }
    }

    private void reloadAccounts() {
        this.accountList.clear();
        this.accountList.add("");
        for (Authentication authentication : MainActivity.globals.getSqLiteGeneral().getAccounts("")) {
            this.accountList.add(authentication.getTitle());
        }
    }

    private void reloadProjects() {
        try {
            this.projectList.clear();
            this.projectList.add("");
            this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
            this.permissions = Helper.getCurrentPermissions(this.getApplicationContext());
            if (this.permissions.addIssues()) {
                this.cmdIssuesAdd.show();
            } else {
                this.cmdIssuesAdd.hide();
            }
            for (Project project : new ListProjectTask(MainActivity.this, this.bugService).execute().get()) {
                this.projectList.add(project.getTitle());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, MainActivity.this);
        }
    }

    @Override
    public void onActivityResult(int resultCode, int requestCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_ACCOUNTS) {
            this.reloadAccounts();
            this.fillFields();
        }
        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_PROJECTS) {
            this.reloadProjects();
            this.selectProject();
        }

        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_ISSUES) {
            this.reload();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menSettings:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        int reload = 0;
        switch (item.getItemId()) {
            case R.id.navProjects:
                intent = new Intent(this.getApplicationContext(), ProjectActivity.class);
                reload = MainActivity.RELOAD_PROJECTS;
                break;
            case R.id.navVersions:
                intent = new Intent(this.getApplicationContext(), VersionActivity.class);
                break;
            default:
                intent = null;
                break;
        }

        if (intent != null) {
            startActivityForResult(intent, reload);
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fillFields() {
        Authentication authentication = MainActivity.settings.getCurrentAuthentication();
        if (authentication != null) {
            if (authentication.getCover() != null) {
                ivMainCover.setImageBitmap(BitmapFactory.decodeByteArray(authentication.getCover(), 0, authentication.getCover().length));
            } else {
                ivMainCover.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
            }
            lblAccountTitle.setText(authentication.getTitle());
            lblMainCommand.setText(R.string.accounts_change);
        } else {
            ivMainCover.setImageDrawable(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp));
            lblAccountTitle.setText(R.string.accounts_noAccount);
            lblMainCommand.setText(R.string.accounts_add);
        }
    }

    private void selectProject() {
        Project project = MainActivity.settings.getCurrentProject(MainActivity.this, this.bugService);
        if (project != null) {
            this.spMainProjects.setSelection(this.projectList.getPosition(project.getTitle()));
        }
    }
}
