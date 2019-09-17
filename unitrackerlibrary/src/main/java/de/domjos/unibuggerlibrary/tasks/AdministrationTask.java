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
import de.domjos.unibuggerlibrary.model.Administration;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;

public final class AdministrationTask extends AbstractTask<Administration, Void, Void> {
    private int icon;

    public AdministrationTask(Activity activity, boolean showNotifications, int icon) {
        super(activity, null, R.string.task_administration_title, R.string.task_administration_contet, showNotifications, icon);
        this.icon = icon;
    }

    @Override
    protected void before() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected Void doInBackground(Administration... administrations) {
        try {
            Administration administration = administrations[0];

            if (administration.getFromProject() != null && administration.getToProject() != null) {
                Object id;
                switch (administration.getDataType()) {
                    case Project:
                        Project project = (Project) administration.getDataItem();
                        if (project != null) {
                            id = project.getId();
                            Object objId = this.insertOrUpdateProject(project, administration);

                            if (administration.isWithBugs()) {
                                Object newId = null;

                                List<Project> projects = administration.getToBugService().getProjects();
                                for (Project newProject : projects) {
                                    if (newProject.getId().equals(objId)) {
                                        newId = newProject.getId();
                                    }
                                }

                                List<Issue> issues = administration.getFromBugService().getIssues(id);
                                for (Issue issue : issues) {
                                    try {
                                        issue = administration.getFromBugService().getIssue(issue.getId(), id);
                                        issue.setId(null);
                                        for (int i = 0; i <= issue.getAttachments().size() - 1; i++) {
                                            ((Attachment) issue.getAttachments().get(i)).setId(null);
                                        }
                                        for (int i = 0; i <= issue.getNotes().size() - 1; i++) {
                                            ((Note) issue.getNotes().get(i)).setId(null);
                                        }

                                        issue.getCustomFields().clear();
                                        issue.setHandler(null);
                                        issue.setVersion(this.insertOrUpdateVersion(issue.getVersion(), administration.getToProject(), administration.getToBugService()));
                                        issue.setTargetVersion(this.insertOrUpdateVersion(issue.getTargetVersion(), administration.getToProject(), administration.getToBugService()));
                                        issue.setFixedInVersion(this.insertOrUpdateVersion(issue.getFixedInVersion(), administration.getToProject(), administration.getToBugService()));

                                        issue = administration.convertIssueToValidNewIssue(issue);

                                        administration.getToBugService().insertOrUpdateIssue(issue, newId);

                                        if (administration.getAdminType()== Administration.AdminType.move) {
                                            administration.getFromBugService().deleteIssue(issue.getId(), id);
                                        }
                                    } catch (Exception ex) {
                                        super.printException(ex);
                                    }
                                }
                            }
                        }
                        break;
                    case Bug:
                        Issue issue = (Issue) administration.getDataItem();
                        if (issue != null) {
                            id = issue.getId();
                            issue.setId(null);
                            for (int i = 0; i <= issue.getAttachments().size() - 1; i++) {
                                ((Attachment) issue.getAttachments().get(i)).setId(null);
                            }
                            for (int i = 0; i <= issue.getNotes().size() - 1; i++) {
                                ((Note) issue.getNotes().get(i)).setId(null);
                            }

                            issue = administration.convertIssueToValidNewIssue(issue);

                            administration.getToBugService().insertOrUpdateIssue(issue, administration.getToProject().getId());

                            if (administration.getAdminType()== Administration.AdminType.move) {
                                administration.getFromBugService().deleteIssue(id, administration.getFromProject().getId());
                            }
                        }
                        break;
                    case CustomField:
                        CustomField customField = (CustomField) administration.getDataItem();
                        if (customField != null) {
                            id = customField.getId();
                            customField.setId(null);

                            administration.getToBugService().insertOrUpdateCustomField(customField, administration.getToProject().getId());

                            if (administration.getAdminType()== Administration.AdminType.move) {
                                administration.getFromBugService().deleteCustomField(id, administration.getFromProject().getId());
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

    private String insertOrUpdateVersion(String version, Object projectId, IBugService bugService) {
        try {
            if(bugService.getPermissions().addVersions()) {

                Version oldVersion = new Version();
                oldVersion.setTitle(version);
                List<Version> versions = bugService.getVersions("", projectId);
                for(Version tmp : versions) {
                    if(tmp.getTitle().equals(version)) {
                        oldVersion = tmp;
                        break;
                    }
                }

                bugService.insertOrUpdateVersion(oldVersion, projectId);

                return version;
            }
        } catch (Exception ignored) {}
        return "";
    }

    @SuppressWarnings("unchecked")
    private Object insertOrUpdateProject(Project project, Administration administration) throws Exception {
        Object id = project.getId();
        project = administration.convertProjectToValidNewProject(project);
        project.setTitle(project.getTitle().replace("-", ""));

        Object newId = null;
        if(administration.isAddToExistingProject()) {
            List<Project> projects = administration.getToBugService().getProjects();
            for(Project temp : projects) {
                if(temp.getTitle().toLowerCase().equals(project.getTitle().toLowerCase())) {
                    newId = temp.getId();
                }
            }
        }
        project.setId(newId);

        for (int i = 0; i <= project.getVersions().size() - 1; i++) {
            ((Version) project.getVersions().get(i)).setId(null);
        }
        Object objId = administration.getToBugService().insertOrUpdateProject(project);

        if (administration.getAdminType() == Administration.AdminType.move) {
            administration.getFromBugService().deleteProject(id);
        }
        return objId;
    }
}
