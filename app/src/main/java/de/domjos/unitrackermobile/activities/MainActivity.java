/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.model.issues.Relationship;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.permissions.NOPERMISSION;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unitrackermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.ArrayHelper;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.settings.Globals;
import de.domjos.unitrackermobile.settings.Settings;
import de.domjos.unitrackermobile.spotlight.OnBoardingHelper;

public final class MainActivity extends AbstractActivity implements OnNavigationItemSelectedListener {
    private FloatingActionButton cmdIssuesAdd;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView ivMainCover;
    private TextView lblMainCommand;
    private TextView lblAccountTitle;
    private Spinner spMainAccounts, spMainFilters, spMainProjects;
    private TableRow rowNoConnection;
    private SwipeRefreshDeleteList lvMainIssues;
    private LinearLayout pagination;
    private TextView lblItems;
    private ImageButton cmdPrevious, cmdNext;
    private ArrayAdapter<String> accountList;
    private ArrayAdapter<Project> projectList;
    private ArrayAdapter<String> filterAdapter;
    private IBugService bugService;
    private IFunctionImplemented permissions;
    private Settings settings;
    private SearchView cmdSearch;
    private Toolbar toolbar;
    private int page;

    private static final int RELOAD_PROJECTS = 98;
    private static final int RELOAD_ACCOUNTS = 99;
    private static final int RELOAD_ISSUES = 101;
    private static final int RELOAD_SETTINGS = 102;
    private static final int RELOAD_FILTERS = 103;
    public static final Globals GLOBALS = new Globals();
    private boolean firstLogIn = false;

    public MainActivity() {
        super(R.layout.main_activity);
        this.page = 1;

    }

    @Override
    protected void initActions() {

        this.navigationView.getHeaderView(0).setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), AccountActivity.class);
            startActivityForResult(intent, MainActivity.RELOAD_ACCOUNTS);
        });

        this.lblMainCommand.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), AccountActivity.class);
            startActivityForResult(intent, MainActivity.RELOAD_ACCOUNTS);
        });

        this.spMainAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings settings = MainActivity.GLOBALS.getSettings(getApplicationContext());
                String item = accountList.getItem(position);
                if (item != null) {
                    if (!item.trim().isEmpty()) {
                        Authentication authentication = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("title='" + item + "'").get(0);
                        if (authentication != null) {
                            Authentication selected = settings.getCurrentAuthentication();
                            if (!selected.getTitle().equals(authentication.getTitle())) {
                                settings.setCurrentProject("");
                            }
                            settings.setCurrentAuthentication(authentication);

                            if (projectList.getCount() < spMainProjects.getSelectedItemPosition()) {
                                spMainProjects.setSelection(0);
                                Project project = (Project) spMainProjects.getSelectedItem();
                                settings.setCurrentProject(String.valueOf(project.getId()));
                            }
                        } else {
                            settings.setCurrentAuthentication(null);
                        }
                    } else {
                        settings.setCurrentAuthentication(null);
                    }
                } else {
                    settings.setCurrentAuthentication(null);
                }

                changeAuthentication();
                fillFields();
                reload();

                OnBoardingHelper.tutorialStep5(MainActivity.this, spMainProjects, drawerLayout, navigationView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spMainProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Project project = projectList.getItem(position);
                if (project != null) {
                    MainActivity.GLOBALS.getSettings(getApplicationContext()).setCurrentProject(String.valueOf(project.getId()));
                    changePermissions();
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.lvMainIssues.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            if (listObject != null) {
                Intent intent = new Intent(getApplicationContext(), IssueActivity.class);
                intent.putExtra("id", String.valueOf(((Issue)listObject.getObject()).getId()));
                intent.putExtra("pid", String.valueOf(MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentProjectId()));
                startActivityForResult(intent, MainActivity.RELOAD_ISSUES);
            }
        });

        this.lvMainIssues.setOnDeleteListener(listObject -> {
            try {
                if (listObject != null) {
                    if (listObject.getObject() != null) {
                        Project project = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentProject(MainActivity.this, bugService);
                        new IssueTask(MainActivity.this, bugService, project.getId(), true, false, settings.showNotifications(), R.drawable.ic_bug_report_black_24dp).execute(((Issue)listObject.getObject()).getId()).get();
                    }
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, getApplicationContext());
            }
        });

        this.cmdIssuesAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), IssueActivity.class);
            intent.putExtra("id", "");
            intent.putExtra("pid", String.valueOf(MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentProjectId()));
            this.startActivityForResult(intent, MainActivity.RELOAD_ISSUES);
        });

        this.spMainFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setCurrentFilter(IBugService.IssueFilter.valueOf(filterAdapter.getItem(position)));
                page = 1;
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.cmdSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                reload(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    reload();
                }
                return true;
            }
        });

        this.cmdSearch.setOnCloseListener(() -> {
            this.reload();
            return true;
        });

        this.cmdPrevious.setOnLongClickListener(v -> {
            this.page = 1;
            this.reload();
            return true;
        });

        this.cmdPrevious.setOnClickListener(v -> {
            if (this.page > 1) {
                this.page--;
                this.reload();
            }
        });

        this.cmdNext.setOnLongClickListener(v -> {
            while (this.lvMainIssues.getAdapter().getItemCount() == this.settings.getNumberOfItems()) {
                this.page++;
                this.reload();
            }

            return true;
        });

        this.cmdNext.setOnClickListener(v -> {
            this.page++;
            this.reload();
        });

        this.lvMainIssues.setOnReloadListener(this::reload);
    }

    @Override
    protected void initControls() {
        try {
            SQLiteDatabase.loadLibs(this);
            // init Toolbar
            this.toolbar = this.findViewById(R.id.toolbar);
            this.setSupportActionBar(this.toolbar);

            // init Drawer-Layout
            this.drawerLayout = this.findViewById(R.id.drawer_layout);

            // init Navigation-View
            this.navigationView = findViewById(R.id.nav_view);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, this.drawerLayout, toolbar, R.string.app_name, R.string.app_name);
            this.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            this.navigationView.setNavigationItemSelectedListener(this);

            this.cmdSearch = this.findViewById(R.id.cmdSearch);

            this.ivMainCover = this.navigationView.getHeaderView(0).findViewById(R.id.ivMainCover);
            this.lblMainCommand = this.navigationView.getHeaderView(0).findViewById(R.id.lblMainCommand);
            this.lblAccountTitle = this.navigationView.getHeaderView(0).findViewById(R.id.lblAccountTitle);

            this.cmdIssuesAdd = this.findViewById(R.id.cmdIssueAdd);
            this.spMainAccounts = this.navigationView.getHeaderView(0).findViewById(R.id.spMainAccounts);
            this.accountList = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
            this.spMainAccounts.setAdapter(this.accountList);
            this.accountList.notifyDataSetChanged();

            this.spMainProjects = this.findViewById(R.id.spMainProjects);
            this.projectList = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
            this.spMainProjects.setAdapter(this.projectList);
            this.projectList.notifyDataSetChanged();

            this.lvMainIssues = this.findViewById(R.id.lvMainIssues);
            this.lvMainIssues.setContextMenu(R.menu.context_main);
            this.lvMainIssues.addButtonClick(R.drawable.ic_style_black_24dp, this.getString(R.string.issues_general_tags), objectList -> {
                try {
                    Activity act = MainActivity.this;
                    boolean show = settings.showNotifications();
                    Object pid = settings.getCurrentProjectId();

                    String tags = Helper.showTagDialog(act, bugService, show, pid);

                    for(BaseDescriptionObject listObject : objectList) {
                        IssueTask issueTask = new IssueTask(act, bugService, pid, false, true, show, R.drawable.ic_bug_report_black_24dp);
                        List<Issue> issues = issueTask.execute(((Issue)listObject.getObject()).getId()).get();

                        if(issues!=null) {
                            if(!issues.isEmpty()) {
                                issues.get(0).setTags(tags);
                                issueTask = new IssueTask(act, bugService, pid, false, false, show, R.drawable.ic_bug_report_black_24dp);
                                issueTask.execute(issues.get(0)).get();
                            }
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
                }
            });

            this.spMainFilters = this.findViewById(R.id.spMainFilters);
            this.filterAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
            this.spMainFilters.setAdapter(this.filterAdapter);
            this.filterAdapter.notifyDataSetChanged();

            this.lblItems = this.findViewById(R.id.lblItems);
            this.pagination = this.findViewById(R.id.pagination);
            this.cmdPrevious = this.findViewById(R.id.cmdBefore);
            this.cmdNext = this.findViewById(R.id.cmdNext);

            this.rowNoConnection = this.findViewById(R.id.rowNoConnection);
            this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());
            this.firstLogIn = this.settings.isFirstLogin(false);

            Helper.showPasswordDialog(MainActivity.this, this.firstLogIn, false, this::executeOnSuccess);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            Object pid = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).getCurrentProjectId();
            boolean show = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).showNotifications();
            BaseDescriptionObject currentObject = lvMainIssues.getAdapter().getObject();
            IssueTask issueTask = new IssueTask(MainActivity.this, this.bugService, pid, false, true, show, R.drawable.ic_bug_report_black_24dp);
            Issue issue = issueTask.execute(((Issue)currentObject.getObject()).getId()).get().get(0);

            switch (item.getItemId()) {
                case R.id.ctxSolve:
                    String statusArray = "";
                    int position = -1;
                    Authentication authentication = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentAuthentication();
                    switch (authentication.getTracker()) {
                        case MantisBT:
                            position = 5;
                            statusArray = "issues_general_status_mantisbt_values";
                            List<String> items = ArrayHelper.getValues(MainActivity.this, "issues_general_resolution_values");
                            int id = ArrayHelper.getIdOfEnum(MainActivity.this, 5, "issues_general_resolution_values");
                            issue.setResolution(id, items.get(5));
                            break;
                        case YouTrack:
                            statusArray = "issues_general_status_youtrack_values";
                            position = 7;
                            break;
                        case RedMine:
                            statusArray = "issues_general_status_redmine_values";
                            position = 2;
                            break;
                        case Bugzilla:
                            statusArray = "issues_general_status_bugzilla_values";
                            position = 2;
                            List<String> resItems = ArrayHelper.getValues(MainActivity.this, "issues_general_resolution_bugzilla_values");
                            int resId = ArrayHelper.getIdOfEnum(MainActivity.this, 1, "issues_general_resolution_bugzilla_values");
                            issue.setResolution(resId, resItems.get(1));
                            break;
                        case Jira:
                            statusArray = "issues_general_status_jira_values";
                            position = 2;
                            break;
                        case PivotalTracker:
                            statusArray = "issues_general_status_pivotal_values";
                            position = 2;
                            break;
                        case OpenProject:
                            statusArray = "issues_general_status_openproject_values";
                            position = 12;
                            break;
                        case Backlog:
                            statusArray = "issues_general_status_backlog_values";
                            position = 2;
                            break;
                        case Local:
                            statusArray = "issues_general_status_mantisbt_values";
                            position = 5;
                            break;
                    }

                    if (!statusArray.isEmpty()) {
                        Helper.showResolveDialog(MainActivity.this, statusArray, position, issue, bugService, pid, show, this::reload);
                    }
                    break;
                case R.id.ctxClone:
                    issue.setId(null);
                    issue.setTitle(issue.getTitle() + " - Copy");
                    for(int i = 0; i<=issue.getAttachments().size() - 1; i++) {
                        ((Attachment) issue.getAttachments().get(i)).setId(null);
                    }
                    for(int i = 0; i<=issue.getNotes().size() - 1; i++) {
                        ((Note) issue.getNotes().get(i)).setId(null);
                    }
                    for(int i = 0; i<=issue.getRelations().size() - 1; i++) {
                        ((Relationship) issue.getRelations().get(i)).setId(null);
                    }
                    new IssueTask(MainActivity.this, this.bugService, pid, false, false, show, R.drawable.ic_bug_report_black_24dp).execute(issue).get();
                    reload();
                    break;
                case R.id.ctxShowAttachment:
                    if (issue != null) {
                        if (issue.getAttachments() != null) {
                            if (!issue.getAttachments().isEmpty()) {
                                Helper.showAttachmentDialog(MainActivity.this, issue.getAttachments());
                            }
                        }
                    }
                    break;
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
        return super.onContextItemSelected(item);
    }

    private void executeOnSuccess() {
        try {
            this.reloadAccounts();
            this.changeAuthentication();
            this.reloadFilters();
            this.reload();

            Timer timer = new Timer();
            if (this.settings.getReload() != -1) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (settings.getReload() == -1) {
                            timer.cancel();
                        }
                        runOnUiThread(() -> reload());
                    }
                }, 0, (this.settings.getReload() * 1000));
            }

            if(this.firstLogIn) {
                Helper.showWhatsNewDialog(this);
            }
            OnBoardingHelper.startTutorial(this.firstLogIn, MainActivity.this, this.toolbar, this.drawerLayout, this.navigationView, this.ivMainCover);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
    }

    private void changeAuthentication() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentAuthentication();
        if (authentication != null) {
            this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
            if (this.accountList.getPosition(authentication.getTitle()) == -1) {
                this.spMainAccounts.setSelection(0);
                MainActivity.GLOBALS.getSettings(this.getApplicationContext()).setCurrentAuthentication(null);
                this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
            } else {
                this.spMainAccounts.setSelection(this.accountList.getPosition(authentication.getTitle()));
            }

            this.reloadProjects();
            this.selectProject();
        } else {
            this.spMainAccounts.setSelection(this.accountList.getPosition(""));
        }
        this.changePermissions();
    }

    private void changePermissions() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentAuthentication();
        if (authentication != null) {

            if (authentication.getServer().equals("")) {
                this.permissions = new NOPERMISSION();
            } else {
                this.permissions = this.bugService.getPermissions();
            }

            if (this.projectList.getCount() - 1 < this.spMainProjects.getSelectedItemPosition()) {
                this.spMainProjects.setSelection(0);
            }
            if (this.permissions.addIssues() && this.spMainProjects.getSelectedItem() != null && this.spMainProjects.getSelectedItemPosition() != 0) {
                this.cmdIssuesAdd.show();
            } else {
                this.cmdIssuesAdd.hide();
            }
            this.navigationView.getMenu().findItem(R.id.navProjects).setVisible((this.permissions.addProjects() || this.permissions.updateProjects() || this.permissions.deleteProjects()));
            this.navigationView.getMenu().findItem(R.id.navVersions).setVisible((this.permissions.addVersions() || this.permissions.updateVersions() || this.permissions.deleteVersions()) && this.spMainProjects.getSelectedItem() != null && this.spMainProjects.getSelectedItemPosition() != 0);
            this.navigationView.getMenu().findItem(R.id.navUsers).setVisible((this.permissions.addUsers() || this.permissions.updateUsers() || this.permissions.deleteUsers()));
            this.navigationView.getMenu().findItem(R.id.navFields).setVisible((this.permissions.addCustomFields() || this.permissions.updateCustomFields() || this.permissions.deleteCustomFields()) && this.spMainProjects.getSelectedItem() != null && this.spMainProjects.getSelectedItemPosition() != 0);
        } else {
            this.navigationView.getMenu().findItem(R.id.navProjects).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navVersions).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navUsers).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navFields).setVisible(false);
            this.permissions = new NOPERMISSION();
        }
    }

    @Override
    protected void reload() {
        this.reload("");
    }

    private void reload(String search) {
        try {
            this.settings = new Settings(getApplicationContext());
            long maximum = 0;
            if (!MainActivity.GLOBALS.getPassword().isEmpty()) {
                this.spMainFilters.setSelection(this.filterAdapter.getPosition(this.settings.getCurrentFilter().name()));
                this.changePagination();
                this.lvMainIssues.getAdapter().clear();
                boolean isLocal = true;
                if (this.bugService != null) {
                    if (this.bugService.getAuthentication() != null) {
                        isLocal = this.bugService.getAuthentication().getTracker() == Authentication.Tracker.Local;
                    }
                }

                if (isLocal || Helper.isNetworkAvailable(MainActivity.this)) {
                    if (this.permissions.listIssues()) {
                        String id = "";
                        if (this.spMainProjects.getSelectedItem() != null) {
                            Project project = this.projectList.getItem(this.spMainProjects.getSelectedItemPosition());
                            if (project != null) {
                                id = String.valueOf(project.getId());
                            }
                        } else {
                            id = String.valueOf(MainActivity.GLOBALS.getSettings(this.getApplicationContext()).getCurrentProjectId());
                        }
                        if (!id.isEmpty()) {
                            if (!id.equals("0")) {
                                String filter = "";
                                if (this.spMainFilters.getSelectedItem() != null) {
                                    filter = this.spMainFilters.getSelectedItem().toString();
                                }

                                IssueTask listIssueTask = new IssueTask(MainActivity.this, this.bugService, id, this.page, this.settings.getNumberOfItems(), filter, false, false, this.settings.showNotifications(), R.drawable.ic_bug_report_black_24dp);
                                for (Object issue : listIssueTask.execute(0).get()) {
                                    Issue tmp = (Issue) issue;
                                    if (tmp.getTitle().contains(search)) {
                                        BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                                        baseDescriptionObject.setObject(tmp);
                                        baseDescriptionObject.setTitle(tmp.getTitle());
                                        baseDescriptionObject.setDescription(tmp.getDescription());
                                        this.lvMainIssues.getAdapter().add(baseDescriptionObject);
                                    }
                                }
                                maximum = listIssueTask.getMaximum();
                            }
                        }
                    }
                }
            }

            int min = (this.page - 1) * this.settings.getNumberOfItems() + 1;
            int max = this.lvMainIssues.getAdapter().getItemCount() <= this.settings.getNumberOfItems() ? (this.page - 1) * this.settings.getNumberOfItems() + this.lvMainIssues.getAdapter().getItemCount() : this.page * this.settings.getNumberOfItems();
            if(max==maximum) {
                if(max!=-1) {
                    this.lblItems.setText(String.format(this.getString(R.string.messages_issues), String.valueOf(min), String.valueOf(max)));
                }
            } else {
                if(max!=-1) {
                    this.lblItems.setText(String.format(this.getString(R.string.messages_issues_with_max), String.valueOf(min), String.valueOf(max), String.valueOf(maximum)));
                } else {
                    this.lblItems.setText(String.format(this.getString(R.string.messages_issues), String.valueOf(min), String.valueOf(maximum)));
                }
            }
            this.lvMainIssues.getAdapter().notifyDataSetChanged();
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
    }

    private void reloadAccounts() {
        this.accountList.clear();
        this.accountList.add("");
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            if (Helper.isNetworkAvailable(MainActivity.this)) {
                this.accountList.add(authentication.getTitle());
                this.rowNoConnection.setVisibility(View.GONE);
            } else {
                this.rowNoConnection.setVisibility(View.VISIBLE);
                if (authentication.getTracker() == Authentication.Tracker.Local) {
                    this.accountList.add(authentication.getTitle());
                }
            }
        }
    }

    private void reloadProjects() {
        try {
            this.projectList.clear();
            this.projectList.add(new Project());

            List<Project> projects = new ProjectTask(MainActivity.this, this.bugService, false, this.settings.showNotifications(), R.drawable.ic_apps_black_24dp).execute(0).get();
            if (projects != null) {
                for (Project project : projects) {
                    this.projectList.add(project);
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
    }

    private void reloadFilters() {
        this.filterAdapter.clear();
        for (IBugService.IssueFilter filter : IBugService.IssueFilter.values()) {
            this.filterAdapter.add(filter.name());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_ACCOUNTS) {
            this.reloadAccounts();
            this.reloadProjects();
            this.fillFields();
        }
        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_PROJECTS) {
            this.reloadProjects();
            this.selectProject();
        }

        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_ISSUES) {
            this.reload();
        }

        if (resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_FILTERS) {
            this.reloadFilters();
        }

        if(resultCode == RESULT_OK && requestCode == MainActivity.RELOAD_SETTINGS) {
            this.bugService = Helper.getCurrentBugService(MainActivity.this);
            this.reload();
            this.changePagination();
        }

        OnBoardingHelper.tutorialStep4(resultCode, requestCode, MainActivity.this, this.spMainAccounts, this::reloadAccounts);
    }

    private void changePagination() {
        if (this.settings.getNumberOfItems() == -1) {
            this.page = 1;
            this.pagination.setVisibility(View.INVISIBLE);
            this.pagination.getLayoutParams().height = 0;
        } else {
            this.pagination.setVisibility(View.VISIBLE);
            this.pagination.getLayoutParams().height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT;
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menSettings:
                intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
                break;
            case R.id.menHelp:
                intent = new Intent(this.getApplicationContext(), HelpActivity.class);
                break;
            default:
                intent = null;
        }

        if (intent != null) {
            startActivityForResult(intent, MainActivity.RELOAD_SETTINGS);
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
            case R.id.navUsers:
                intent = new Intent(this.getApplicationContext(), UserActivity.class);
                break;
            case R.id.navFields:
                intent = new Intent(this.getApplicationContext(), FieldActivity.class);
                break;
            case R.id.navStatistics:
                intent = new Intent(this.getApplicationContext(), StatisticsActivity.class);
                break;
            case R.id.navAdministration:
                intent = new Intent(this.getApplicationContext(), AdministrationActivity.class);
                reload = MainActivity.RELOAD_PROJECTS;
                break;
            case R.id.navLocalSync:
                intent = new Intent(this.getApplicationContext(), LocalSyncActivity.class);
                break;
            case R.id.navExtendedSearch:
                intent = new Intent(this.getApplicationContext(), SearchActivity.class);
                break;
            case R.id.navCalendar:
                intent = new Intent(this.getApplicationContext(), CalendarActivity.class);
                break;
            case R.id.navExport:
                intent = new Intent(this.getApplicationContext(), ExportActivity.class);
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
        Authentication authentication = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentAuthentication();
        if (authentication != null) {
            if (authentication.getServer().equals("")) {
                ivMainCover.setImageDrawable(this.getDrawable());
                lblAccountTitle.setText(R.string.accounts_noAccount);
                lblMainCommand.setText(R.string.accounts_add);
            } else {
                if (authentication.getCover() != null) {
                    ivMainCover.setImageBitmap(BitmapFactory.decodeByteArray(authentication.getCover(), 0, authentication.getCover().length));
                } else {
                    ivMainCover.setImageDrawable(this.getDrawable());
                }

                final StringBuilder tracker = new StringBuilder();
                tracker.append(authentication.getTitle());
                tracker.append(" (");
                tracker.append(authentication.getTracker().name());
                tracker.append(" ");
                new Thread(() -> {
                    try {
                        IBugService bugService = Helper.getCurrentBugService(this.getApplicationContext());
                        tracker.append(bugService.getTrackerVersion());
                        tracker.append(")");
                        MainActivity.this.runOnUiThread(() -> lblAccountTitle.setText(tracker.toString()));
                    } catch (Exception ignored) {
                    }
                }).start();

                lblMainCommand.setText(R.string.accounts_change);
            }
        } else {
            ivMainCover.setImageDrawable(this.getDrawable());
            lblAccountTitle.setText(R.string.accounts_noAccount);
            lblMainCommand.setText(R.string.accounts_add);
        }
    }

    @SuppressWarnings("deprecation")
    private Drawable getDrawable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getDrawable(R.drawable.ic_account_circle_black_24dp);
        } else {
            return getResources().getDrawable(R.drawable.ic_account_circle_black_24dp);
        }
    }

    private void selectProject() {
        for (int i = 0; i <= this.projectList.getCount() - 1; i++) {
            Project current = this.projectList.getItem(i);
            if (current != null) {
                if (String.valueOf(current.getId()).equals(String.valueOf(this.settings.getCurrentProjectId()))) {
                    this.spMainProjects.setSelection(i);
                    return;
                }
            }
        }
    }
}
