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

import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;

public final class AdministrationTask extends AbstractTask<IBugService, Void, Void> {
    private final boolean move, withIssues;
    private final Project project1, project2;
    private final DescriptionObject dataItem;
    private final int dataPosition;

    public AdministrationTask(Activity activity, boolean showNotifications, boolean move, boolean withIssues, Project project1, Project project2, DescriptionObject dataItem, int dataPosition) {
        super(activity, null, R.string.task_administration_title, R.string.task_administration_contet, showNotifications);
        this.move = move;
        this.withIssues = withIssues;
        this.project1 = project1;
        this.project2 = project2;
        this.dataItem = dataItem;
        this.dataPosition = dataPosition;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected Void doInBackground(IBugService... iBugServices) {
        try {
            IBugService bugService1 = iBugServices[0];
            IBugService bugService2 = iBugServices[1];

            if (project2 != null && project1 != null) {
                Object id;
                switch (dataPosition) {
                    case 0:
                        Project project = (Project) dataItem;
                        if (project != null) {
                            id = project.getId();
                            project.setId(null);
                            for (int i = 0; i <= project.getVersions().size() - 1; i++) {
                                ((Version) project.getVersions().get(i)).setId(null);
                            }
                            bugService2.insertOrUpdateProject(project);

                            if (move) {
                                bugService1.deleteProject(id);
                            }

                            if (this.withIssues) {
                                Object newId = null;

                                List<Project> projects = bugService2.getProjects();
                                for (Project newProject : projects) {
                                    if (newProject.getId().equals(project.getId())) {
                                        newId = newProject.getId();
                                    }
                                }

                                List<Issue> issues = bugService1.getIssues(id);
                                for (Issue issue : issues) {
                                    issue = bugService1.getIssue(issue.getId(), id);
                                    issue.setId(null);
                                    for (int i = 0; i <= issue.getAttachments().size() - 1; i++) {
                                        ((Attachment) issue.getAttachments().get(i)).setId(null);
                                    }
                                    for (int i = 0; i <= issue.getNotes().size() - 1; i++) {
                                        ((Note) issue.getNotes().get(i)).setId(null);
                                    }

                                    bugService2.insertOrUpdateIssue(issue, newId);

                                    if (move) {
                                        bugService1.deleteIssue(issue.getId(), id);
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        Issue issue = (Issue) dataItem;
                        if (issue != null) {
                            id = issue.getId();
                            issue.setId(null);
                            for (int i = 0; i <= issue.getAttachments().size() - 1; i++) {
                                ((Attachment) issue.getAttachments().get(i)).setId(null);
                            }
                            for (int i = 0; i <= issue.getNotes().size() - 1; i++) {
                                ((Note) issue.getNotes().get(i)).setId(null);
                            }

                            bugService2.insertOrUpdateIssue(issue, project2.getId());

                            if (move) {
                                bugService1.deleteIssue(id, project1.getId());
                            }
                        }
                        break;
                    case 2:
                        CustomField customField = (CustomField) dataItem;
                        if (customField != null) {
                            id = customField.getId();
                            customField.setId(null);

                            bugService2.insertOrUpdateCustomField(customField, project2.getId());

                            if (move) {
                                bugService1.deleteCustomField(id, project1.getId());
                            }
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }
}
