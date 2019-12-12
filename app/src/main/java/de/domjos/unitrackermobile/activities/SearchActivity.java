/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

package de.domjos.unitrackermobile.activities;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.objects.BaseDescriptionObject;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.SearchTask;
import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.tokenizer.CommaTokenizer;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unitrackermobile.helper.Helper;

public final class SearchActivity extends AbstractActivity {
    private SwipeRefreshDeleteList lvSearchResults;

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
            this.lvSearchResults.getAdapter().clear();
            this.search();
        });

        this.lvSearchResults.click(new SwipeRefreshDeleteList.ClickListener() {
            @Override
            public void onClick(BaseDescriptionObject listObject) {
                if (listObject != null) {
                    for (IBugService bugService : bugServices) {
                        Object title = ((DescriptionObject) listObject.getObject()).getHints().get("title");
                        if (title != null) {
                            if (bugService.getAuthentication().getTitle().trim().equals(title.toString().trim())) {
                                MainActivity.GLOBALS.getSettings(getApplicationContext()).setCurrentAuthentication(bugService.getAuthentication());
                                Object project = ((DescriptionObject) listObject.getObject()).getHints().get("project");
                                if (project != null) {
                                    MainActivity.GLOBALS.getSettings(getApplicationContext()).setCurrentProject(project.toString());
                                    Intent intent = new Intent(getApplicationContext(), IssueActivity.class);
                                    intent.putExtra("id", ((DescriptionObject)listObject.getObject()).getId().toString());
                                    intent.putExtra("pid", project.toString());
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                }
            }
        });

        this.lvSearchResults.reload(new SwipeRefreshDeleteList.ReloadListener() {
            @Override
            public void onReload() {
                search();
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
        try {
            SearchTask searchTask =
                    new SearchTask(
                            SearchActivity.this,
                            this.txtSearch.getText().toString(),
                            this.chkSearchSummary.isChecked(),
                            this.chkSearchDescription.isChecked(),
                            this.txtSearchProjects.getText().toString(),
                            this.txtSearchVersions.getText().toString(),
                            this.bugServices,
                            MainActivity.GLOBALS.getSettings(this.getApplicationContext()).showNotifications(),
                            R.drawable.ic_search_black_24dp
                    );
            for (DescriptionObject descriptionObject : searchTask.execute(R.drawable.ic_search_black_24dp).get()) {
                BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject(descriptionObject);
                baseDescriptionObject.setDescription(descriptionObject.getDescription());
                baseDescriptionObject.setTitle(descriptionObject.getTitle());
                this.lvSearchResults.getAdapter().add(baseDescriptionObject);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, SearchActivity.this);
        }
    }

    @Override
    protected void initControls() {
        this.lvSearchResults = this.findViewById(R.id.lvSearchResults);
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
