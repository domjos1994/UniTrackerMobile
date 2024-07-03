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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.unibuggermobile.sheets.BottomSheetIssue;
import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.cache.CacheGlobals;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.permissions.NoPermission;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.tracker.GithubSpecific.SearchAll;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unitrackerlibrary.services.ArrayHelper;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Globals;
import de.domjos.unibuggermobile.settings.Settings;

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
    private ArrayAdapter<Project<?>> projectList;
    private ArrayAdapter<String> filterAdapter;
    private IBugService<?> bugService;
    private IFunctionImplemented permissions;
    private Settings settings;
    private SearchView cmdSearch;
    private int page, currentNumberOfItems, notId;
    private long maximum;

    public static final Globals GLOBALS = new Globals();
    private boolean firstLogIn = false;

    private BottomSheetIssue modalBottomSheet = new BottomSheetIssue();

    final ActivityResultLauncher<Intent> reloadAccounts = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    this.reloadAccounts();
                    this.reloadProjects();
                    this.fillFields();
                }
            });
    final ActivityResultLauncher<Intent> reloadProjects = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
                    this.reloadProjects();
                    this.selectProject();
                }
            });
    final ActivityResultLauncher<Intent> reloadIssues = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    this.reload();
                }
            });
    final ActivityResultLauncher<Intent> reloadSettings = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    this.setCacheGlobals();
                    this.bugService = Helper.getCurrentBugService(MainActivity.this);
                    this.lvMainIssues.setScrollList(MainActivity.GLOBALS.getSettings(MainActivity.this).isScrollList());
                    this.reload();
                    this.changePagination();
                }
            });

    public MainActivity() {
        super(R.layout.main_activity);
        this.page = 1;
        this.maximum = -1L;
        this.currentNumberOfItems = -1;
        this.notId = -1;
    }

    @Override
    protected void initActions() {

        this.navigationView.getHeaderView(0).setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), AccountActivity.class);
            this.reloadAccounts.launch(intent);
        });

        this.lblMainCommand.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), AccountActivity.class);
            this.reloadAccounts.launch(intent);
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
                                Project<?> project = (Project<?>) spMainProjects.getSelectedItem();
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.spMainProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Project<?> project = projectList.getItem(position);
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
            try {
                Settings se = MainActivity.GLOBALS.getSettings(getApplicationContext());
                String pid = String.valueOf(se.getCurrentProjectId());
                String id = String.valueOf(((Issue<?>)listObject.getObject()).getId());
                boolean notify = se.showNotifications();

                IssueTask issueTask = new IssueTask(this, this.bugService, pid, false, true, notify, R.drawable.icon);
                Issue issue = issueTask.execute(id).get().get(0);
                this.modalBottomSheet.load(issue);
                this.modalBottomSheet.show(getSupportFragmentManager(), BottomSheetIssue.TAG);
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, getApplicationContext());
            }

            /*if (listObject != null) {
                Intent intent = new Intent(getApplicationContext(), IssueActivity.class);
                if(listObject.getObject()!=null) {
                    intent.putExtra("id", String.valueOf(((Issue<?>)listObject.getObject()).getId()));
                }
                intent.putExtra("pid", String.valueOf(MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentProjectId()));
                this.reloadIssues.launch(intent);
            }*/
        });

        this.lvMainIssues.setOnDeleteListener(listObject -> {
            try {
                if (listObject != null) {
                    if (listObject.getObject() != null) {
                        Project<?> project = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentProject(MainActivity.this, bugService);
                        new IssueTask(MainActivity.this, bugService, project.getId(), true, false, settings.showNotifications(), R.drawable.icon_issues).execute(((Issue<?>)listObject.getObject()).getId()).get();
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
            this.reloadIssues.launch(intent);
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
            if(this.currentNumberOfItems!=this.maximum) {
                this.page++;
                this.reload();
            }
        });

        this.lvMainIssues.setOnReloadListener(() -> {
            CacheGlobals.reload = true;
            this.reload();
        });
    }

    @Override
    protected void initControls() {
        try {
            SQLiteDatabase.loadLibs(this);
            // init Toolbar
            Toolbar toolbar = this.findViewById(R.id.toolbar);
            this.setSupportActionBar(toolbar);

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
            this.lvMainIssues.setScrollList(MainActivity.GLOBALS.getSettings(MainActivity.this).isScrollList());
            this.lvMainIssues.addButtonClick(R.drawable.icon_tags, this.getString(R.string.issues_general_tags), objectList -> {
                try {
                    Activity act = MainActivity.this;
                    boolean show = settings.showNotifications();
                    Object pid = settings.getCurrentProjectId();

                    Helper.showTagDialog(act, bugService, show, pid, objectList, notId);
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
            Helper.showPasswordDialog(this, this.firstLogIn, false, this::executeOnSuccess);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        try {
            Object pid = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).getCurrentProjectId();
            boolean show = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).showNotifications();
            BaseDescriptionObject currentObject = lvMainIssues.getAdapter().getObject();
            IssueTask issueTask = new IssueTask(MainActivity.this, this.bugService, pid, false, true, show, R.drawable.icon_issues);
            issueTask.setId(notId);
            Issue<?> issue = issueTask.execute(((Issue<?>)currentObject.getObject()).getId()).get().get(0);
            this.notId = issueTask.getId();

            if(item.getItemId() == R.id.ctxSolve) {
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
                    Helper.showResolveDialog(MainActivity.this, statusArray, position, issue, bugService, pid, show, this::reload, notId);
                }
            } else if(item.getItemId() == R.id.ctxClone) {
                issue.setId(null);
                issue.setTitle(issue.getTitle() + " - Copy");
                for(int i = 0; i<=issue.getAttachments().size() - 1; i++) {
                    issue.getAttachments().get(i).setId(null);
                }
                for(int i = 0; i<=issue.getNotes().size() - 1; i++) {
                    issue.getNotes().get(i).setId(null);
                }
                for(int i = 0; i<=issue.getRelations().size() - 1; i++) {
                    issue.getRelations().get(i).setId(null);
                }
                IssueTask task = new IssueTask(MainActivity.this, this.bugService, pid, false, false, show, R.drawable.icon_issues);
                task.setId(notId);
                task.execute(issue).get();
                this.notId = task.getId();
                reload();
            } else if(item.getItemId() == R.id.ctxShowAttachment) {
                if (issue != null) {
                    if (issue.getAttachments() != null) {
                        if (!issue.getAttachments().isEmpty()) {
                            List<Attachment<?>> attachments = new LinkedList<>(issue.getAttachments());
                            Helper.showAttachmentDialog(MainActivity.this, attachments);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }
        return super.onContextItemSelected(item);
    }

    private void executeOnSuccess() {
        try {
            this.setCacheGlobals();
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
                }, 0, (this.settings.getReload() * 1000L));
            }

            if(this.firstLogIn) {
                Helper.showWhatsNewDialog(this);
            }
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

            if (authentication.getServer().isEmpty()) {
                this.permissions = new NoPermission();
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
            this.navigationView.getMenu().findItem(R.id.navNews).setVisible(this.permissions.news());
            this.navigationView.getMenu().findItem(R.id.navProjects).setVisible((this.permissions.addProjects() || this.permissions.updateProjects() || this.permissions.deleteProjects()));
            this.navigationView.getMenu().findItem(R.id.navVersions).setVisible((this.permissions.addVersions() || this.permissions.updateVersions() || this.permissions.deleteVersions()) && this.spMainProjects.getSelectedItem() != null && this.spMainProjects.getSelectedItemPosition() != 0);
            this.navigationView.getMenu().findItem(R.id.navUsers).setVisible((this.permissions.addUsers() || this.permissions.updateUsers() || this.permissions.deleteUsers()));
            this.navigationView.getMenu().findItem(R.id.navFields).setVisible((this.permissions.addCustomFields() || this.permissions.updateCustomFields() || this.permissions.deleteCustomFields()) && this.spMainProjects.getSelectedItem() != null && this.spMainProjects.getSelectedItemPosition() != 0);
        } else {
            this.navigationView.getMenu().findItem(R.id.navNews).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navProjects).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navVersions).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navUsers).setVisible(false);
            this.navigationView.getMenu().findItem(R.id.navFields).setVisible(false);
            this.permissions = new NoPermission();
        }
    }

    @Override
    protected void reload() {
        this.reload("");
    }

    private void reload(String search) {
        try {
            this.settings = new Settings(getApplicationContext());
            if (!MainActivity.GLOBALS.getPassword().isEmpty()) {
                BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                baseDescriptionObject.setTitle(this.getString(R.string.task_loader_title));
                baseDescriptionObject.setDescription(this.getString(R.string.task_loader_title));
                this.lvMainIssues.getAdapter().clear();
                this.lvMainIssues.getAdapter().add(baseDescriptionObject);
                this.spMainFilters.setSelection(this.filterAdapter.getPosition(this.settings.getCurrentFilter().name()));
                this.changePagination();
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
                            Project<?> project = this.projectList.getItem(this.spMainProjects.getSelectedItemPosition());
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

                                IssueTask listIssueTask = new IssueTask(MainActivity.this, this.bugService, id, this.page, this.settings.getNumberOfItems(), filter, false, false, this.settings.showNotifications(), R.drawable.icon_issues);
                                listIssueTask.setId(notId);
                                listIssueTask.after((AbstractTask.PostExecuteListener<List<Issue<?>>>) issues -> {
                                    lvMainIssues.getAdapter().clear();
                                    for (Issue<?> issue : issues) {
                                        if (issue.getTitle().contains(search)) {
                                            BaseDescriptionObject baseDescriptionObject1 = new BaseDescriptionObject();
                                            baseDescriptionObject1.setObject(issue);
                                            String title;
                                            if(MainActivity.GLOBALS.getSettings(MainActivity.this).isShowID()) {
                                                title = issue.getId() + ": " + issue.getTitle();
                                            } else {
                                                title = issue.getTitle();
                                            }
                                            baseDescriptionObject1.setTitle(title);
                                            baseDescriptionObject1.setDescription(issue.getDescription());
                                            boolean resolved = false;
                                            if(issue.getHints().containsKey(Issue.RESOLVED)) {
                                                Object resolve = issue.getHints().get(Issue.RESOLVED);
                                                if(resolve != null) {
                                                    resolved = Boolean.parseBoolean(resolve.toString());
                                                }
                                            }
                                            baseDescriptionObject1.setState(resolved);
                                            lvMainIssues.getAdapter().add(baseDescriptionObject1);
                                            maximum = listIssueTask.getMaximum();
                                            reloadStateData();
                                        }
                                    }
                                });
                                listIssueTask.execute(0);
                                this.notId = listIssueTask.getId();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, MainActivity.this);
        }

        if(this.lvMainIssues.getAdapter().getItemCount() == 1) {
            BaseDescriptionObject baseDescriptionObject = this.lvMainIssues.getAdapter().getItem(0);
            if(baseDescriptionObject.getTitle().equals(this.getString(R.string.task_loader_title))) {
                this.lvMainIssues.getAdapter().clear();
            }
        }
    }

    private void reloadStateData() {
        int min = (this.page - 1) * this.settings.getNumberOfItems() + 1;
        this.currentNumberOfItems = this.lvMainIssues.getAdapter().getItemCount() <= this.settings.getNumberOfItems() ? (this.page - 1) * this.settings.getNumberOfItems() + this.lvMainIssues.getAdapter().getItemCount() : this.page * this.settings.getNumberOfItems();
        if(this.currentNumberOfItems==this.maximum) {
            if(this.currentNumberOfItems!=-1) {
                this.lblItems.setText(String.format(this.getString(R.string.messages_issues), min, this.currentNumberOfItems));
            }
        } else {
            if(this.currentNumberOfItems!=-1) {
                this.lblItems.setText(String.format(this.getString(R.string.messages_issues_with_max), min, this.currentNumberOfItems, this.maximum));
            } else {
                this.lblItems.setText(String.format(this.getString(R.string.messages_issues), min, this.maximum));
            }
        }
        this.lvMainIssues.getAdapter()
                .notifyItemRangeChanged(0, this.lvMainIssues.getChildCount()-1);
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
            this.projectList.add(new Project<>());

            ProjectTask task;
            if(this.bugService.getAuthentication().getHints().containsKey(SearchAll.SEARCH)) {
                String search = this.bugService.getAuthentication().getHints().get(SearchAll.SEARCH);
                task = new ProjectTask(search, MainActivity.this, this.bugService, false, this.settings.showNotifications(), R.drawable.icon_projects);
            } else {
                task = new ProjectTask(MainActivity.this, this.bugService, false, this.settings.showNotifications(), R.drawable.icon_projects);
            }
            task.setId(this.notId);
            List<Project<?>> projects = task.execute(0).get();
            this.notId = task.getId();
            if (projects != null) {
                for (Project<?> project : projects) {
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
        Intent intent = null;
        if(item.getItemId() == R.id.menSettings) {
            intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
        } else if(item.getItemId() == R.id.menHelp) {
            intent = new Intent(this.getApplicationContext(), HelpActivity.class);
        } else if(item.getItemId() == R.id.menAbout) {
            intent = new Intent(this.getApplicationContext(), InfoActivity.class);
            intent.putExtra(InfoActivity.CONTENT, String.format(this.getString(R.string.about_content), Helper.getVersion(this.getApplicationContext())));
            intent.putExtra(InfoActivity.ABOUT, true);
        }
        if (intent != null) {
            this.reloadSettings.launch(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if(item.getItemId() == R.id.navNews) {
            intent = new Intent(this.getApplicationContext(), NewsActivity.class);
        } else if(item.getItemId() == R.id.navProjects) {
            intent = new Intent(this.getApplicationContext(), ProjectActivity.class);
            this.reloadProjects.launch(intent);
        } else if(item.getItemId() == R.id.navVersions) {
            intent = new Intent(this.getApplicationContext(), VersionActivity.class);
        } else if(item.getItemId() == R.id.navUsers) {
            intent = new Intent(this.getApplicationContext(), UserActivity.class);
        } else if(item.getItemId() == R.id.navFields) {
            intent = new Intent(this.getApplicationContext(), FieldActivity.class);
        } else if(item.getItemId() == R.id.navStatistics) {
            intent = new Intent(this.getApplicationContext(), StatisticsActivity.class);
        } else if(item.getItemId() == R.id.navAdministration) {
            intent = new Intent(this.getApplicationContext(), AdministrationActivity.class);
            this.reloadProjects.launch(intent);
        } else if(item.getItemId() == R.id.navLocalSync) {
            intent = new Intent(this.getApplicationContext(), LocalSyncActivity.class);
        } else if(item.getItemId() == R.id.navExtendedSearch) {
            intent = new Intent(this.getApplicationContext(), SearchActivity.class);
        } else if(item.getItemId() == R.id.navCalendar) {
            intent = new Intent(this.getApplicationContext(), CalendarActivity.class);
        } else if(item.getItemId() == R.id.navExport) {
            intent = new Intent(this.getApplicationContext(), ExportActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fillFields() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(getApplicationContext()).getCurrentAuthentication();
        if (authentication != null) {
            if (authentication.getServer().isEmpty()) {
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
                        IBugService<?> bugService = Helper.getCurrentBugService(this.getApplicationContext());
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

    private Drawable getDrawable() {
        return AppCompatResources.getDrawable(getApplicationContext(), R.drawable.icon_accounts);
    }

    private void selectProject() {
        for (int i = 0; i <= this.projectList.getCount() - 1; i++) {
            Project<?> current = this.projectList.getItem(i);
            if (current != null) {
                if (String.valueOf(current.getId()).equals(String.valueOf(this.settings.getCurrentProjectId()))) {
                    this.spMainProjects.setSelection(i);
                    return;
                }
            }
        }
    }

    private void setCacheGlobals() {
        CacheGlobals.useCache = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).useCache();
        CacheGlobals.minutesToReload = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).getMinutesReload();
        CacheGlobals.reloadOnPullDown = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).pullDownReload();
    }
}