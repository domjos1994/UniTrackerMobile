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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.custom.CommaTokenizer;
import de.domjos.unibuggermobile.helper.Helper;

public final class SearchActivity extends AbstractActivity {
    private ListView lvSearchResults;
    private ListAdapter searchResultAdapter;

    private ImageButton cmdSearch;
    private EditText txtSearch;
    private CheckBox chkSearchSummary, chkSearchDescription;
    private MultiAutoCompleteTextView txtSearchProjects, txtSearchVersions;
    private List<IBugService> bugServices;

    public SearchActivity() {
        super(R.layout.search_activity);
    }

    @Override
    protected void initActions() {
        this.cmdSearch.setOnClickListener(v -> {
            this.searchResultAdapter.clear();
            this.search();
        });

        this.lvSearchResults.setOnItemClickListener((parent, view, position, id) -> {
            ListObject listObject = this.searchResultAdapter.getItem(position);
            if (listObject != null) {
                for (IBugService bugService : this.bugServices) {
                    Object title = listObject.getDescriptionObject().getHints().get("title");
                    if (title != null) {
                        if (bugService.getAuthentication().getTitle().trim().equals(title.toString().trim())) {
                            MainActivity.GLOBALS.getSettings(this.getApplicationContext()).setCurrentAuthentication(bugService.getAuthentication());
                            Object project = listObject.getDescriptionObject().getHints().get("project");
                            if (project != null) {
                                MainActivity.GLOBALS.getSettings(this.getApplicationContext()).setCurrentProject(project.toString());
                                Intent intent = new Intent(this.getApplicationContext(), IssueActivity.class);
                                intent.putExtra("id", listObject.getDescriptionObject().getId().toString());
                                intent.putExtra("pid", project.toString());
                                startActivity(intent);
                            }
                        }
                    }
                }
            }
        });

        this.txtSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadVersions();
            }
        });
    }

    private void search() {
        new Thread(() -> {
            try {
                String search = this.txtSearch.getText().toString();
                boolean summary = this.chkSearchSummary.isChecked();
                boolean description = this.chkSearchDescription.isChecked();
                List<String> projects = new LinkedList<>(Arrays.asList(this.txtSearchProjects.getText().toString().split(",")));
                List<String> versions = new LinkedList<>(Arrays.asList(this.txtSearchVersions.getText().toString().split(",")));


                for (IBugService service : this.bugServices) {
                    for (Object object : service.getProjects()) {
                        Project project = (Project) object;

                        boolean contains = false;
                        if (!this.txtSearchProjects.getText().toString().trim().isEmpty()) {
                            for (String strProject : projects) {
                                if (project.getTitle().equals(strProject.trim())) {
                                    if (!this.txtSearchVersions.getText().toString().trim().isEmpty()) {
                                        for (String strVersion : versions) {
                                            for (Object objVersion : project.getVersions()) {
                                                if (((Version) objVersion).getTitle().equals(strVersion.trim())) {
                                                    contains = true;
                                                }
                                            }
                                        }
                                    } else {
                                        contains = true;
                                    }
                                }
                            }
                        } else {
                            contains = true;
                        }

                        if (contains) {
                            for (Object objIssue : service.getIssues(project.getId())) {
                                Issue issue = (Issue) objIssue;
                                boolean searchSuccess = false;

                                if (summary) {
                                    if (issue.getTitle().contains(search)) {
                                        searchSuccess = true;
                                    }
                                }
                                if (description) {
                                    if (issue.getDescription().contains(search)) {
                                        searchSuccess = true;
                                    }
                                }

                                if (searchSuccess) {
                                    ListObject listObject = new ListObject(this.getApplicationContext(), R.drawable.ic_search_black_24dp, issue);
                                    listObject.getDescriptionObject().setDescription(service.getAuthentication().getTracker().name());
                                    listObject.getDescriptionObject().getHints().put("title", service.getAuthentication().getTitle());
                                    listObject.getDescriptionObject().getHints().put("project", project.getId().toString());
                                    SearchActivity.this.runOnUiThread(() -> this.searchResultAdapter.add(listObject));
                                }
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                SearchActivity.this.runOnUiThread(() -> MessageHelper.printException(ex, this.getApplicationContext()));
            }
        }).start();
    }

    @Override
    protected void initControls() {
        this.lvSearchResults = this.findViewById(R.id.lvSearchResults);
        this.searchResultAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_search_black_24dp);
        this.lvSearchResults.setAdapter(this.searchResultAdapter);
        this.searchResultAdapter.notifyDataSetChanged();

        this.cmdSearch = this.findViewById(R.id.cmdSearch);
        this.txtSearch = this.findViewById(R.id.txtSearch);
        this.chkSearchSummary = this.findViewById(R.id.chkSearchSummary);
        this.chkSearchDescription = this.findViewById(R.id.chkSearchDescription);

        this.txtSearchProjects = this.findViewById(R.id.txtSearchProjects);
        this.txtSearchProjects.setTokenizer(new CommaTokenizer());
        this.txtSearchVersions = this.findViewById(R.id.txtSearchVersions);
        this.txtSearchVersions.setTokenizer(new CommaTokenizer());
        this.bugServices = new LinkedList<>();

        this.loadProjects();
        this.loadVersions();
    }

    private void loadProjects() {
        new Thread(() -> {
            try {
                ArrayAdapter<String> projects = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1);
                for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
                    IBugService bugService = Helper.getCurrentBugService(authentication, this.getApplicationContext());
                    for (Object object : bugService.getProjects()) {
                        projects.add(((Project) object).getTitle());
                    }
                    this.bugServices.add(bugService);
                }
                SearchActivity.this.runOnUiThread(() -> this.txtSearchProjects.setAdapter(projects));
            } catch (Exception ex) {
                SearchActivity.this.runOnUiThread(() -> MessageHelper.printException(ex, this.getApplicationContext()));
            }
        }).start();
    }

    private void loadVersions() {
        new Thread(() -> {
            try {
                ArrayAdapter<String> versions = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1);
                if (!this.txtSearchProjects.getText().toString().trim().isEmpty()) {
                    List<String> projects = new LinkedList<>(Arrays.asList(this.txtSearchProjects.getText().toString().split(",")));
                    for (IBugService bugService : this.bugServices) {
                        for (Object object : bugService.getProjects()) {
                            Project project = (Project) object;
                            for (String title : projects) {
                                if (project.getTitle().equals(title.trim())) {
                                    for (Object objVersion : project.getVersions()) {
                                        versions.add(((Version) objVersion).getTitle());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (IBugService bugService : this.bugServices) {
                        for (Object object : bugService.getProjects()) {
                            Project project = (Project) object;
                            for (Object objVersion : project.getVersions()) {
                                versions.add(((Version) objVersion).getTitle());
                            }
                        }
                    }
                }

                SearchActivity.this.runOnUiThread(() -> this.txtSearchVersions.setAdapter(versions));
            } catch (Exception ex) {
                SearchActivity.this.runOnUiThread(() -> MessageHelper.printException(ex, this.getApplicationContext()));
            }
        }).start();
    }
}
