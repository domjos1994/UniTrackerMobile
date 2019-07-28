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

package de.domjos.unibuggerlibrary.tasks;

import android.app.Activity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;

public final class SearchTask extends AbstractTask<Integer, Void, List<ListObject>> {
    private String search, projects, versions;
    private boolean summary, description;
    private final List<ListObject> issues;
    private final List<IBugService> bugServices;

    public SearchTask(Activity activity, String search, boolean summary, boolean description, String projects, String versions, List<IBugService> bugServices, boolean notify, int icon) {
        super(activity, bugServices.get(0), R.string.task_search_title, R.string.task_search_content, notify, icon);
        this.issues = new LinkedList<>();
        this.bugServices = bugServices;
        this.search = search;
        this.projects = projects;
        this.versions = versions;
        this.summary = summary;
        this.description = description;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<ListObject> doInBackground(Integer... issues) {
        try {
            List<String> projects = new LinkedList<>(Arrays.asList(this.projects.split(",")));
            List<String> versions = new LinkedList<>(Arrays.asList(this.versions.split(",")));


            for (IBugService service : this.bugServices) {
                for (Object object : service.getProjects()) {
                    Project project = (Project) object;

                    boolean contains = false;
                    if (!this.projects.trim().isEmpty()) {
                        for (String strProject : projects) {
                            if (project.getTitle().equals(strProject.trim())) {
                                if (!this.versions.trim().isEmpty()) {
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
                                ListObject listObject = new ListObject(this.getContext(), issues[0], issue);
                                listObject.getDescriptionObject().setDescription(service.getAuthentication().getTracker().name());
                                listObject.getDescriptionObject().getHints().put("title", service.getAuthentication().getTitle());
                                listObject.getDescriptionObject().getHints().put("project", project.getId().toString());
                                this.issues.add(listObject);
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            this.printException(ex);
        }
        return this.issues;
    }
}
