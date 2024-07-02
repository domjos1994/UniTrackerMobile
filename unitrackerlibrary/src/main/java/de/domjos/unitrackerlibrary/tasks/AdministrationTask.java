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

package de.domjos.unitrackerlibrary.tasks;

import android.app.Activity;
import android.widget.ProgressBar;

import java.util.List;

import de.domjos.unitrackerlibrary.custom.ProgressBarTask;
import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.Administration;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;

/** @noinspection rawtypes, unchecked , unchecked */
public final class AdministrationTask extends ProgressBarTask<Administration, String> {
    private final StringBuilder message;
    private final boolean debug;

    public AdministrationTask(Activity activity, boolean showNotifications, boolean debug, int icon, ProgressBar progressBar) {
        super(activity, R.string.task_administration_title, R.string.task_administration_contet, showNotifications, icon, progressBar);
        this.message = new StringBuilder();
        this.debug = debug;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected String doInBackground(Administration... administrations) {
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
                            administration.loadCategories(objId);

                            if (administration.isWithBugs()) {
                                if (objId != null) {
                                    List<Issue> issues = administration.getFromBugService().getIssues(id);
                                    int counter = 0, max = issues.size();
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
                                            issue.setVersion(this.insertOrUpdateVersion(issue.getVersion(), objId, administration.getToBugService()));
                                            issue.setTargetVersion(this.insertOrUpdateVersion(issue.getTargetVersion(), objId, administration.getToBugService()));
                                            issue.setFixedInVersion(this.insertOrUpdateVersion(issue.getFixedInVersion(), objId, administration.getToBugService()));

                                            issue = administration.convertIssueToValidNewIssue(issue);

                                            administration.getToBugService().insertOrUpdateIssue(issue, objId);
                                            if (administration.getToBugService().getCurrentState() >= 300) {
                                                this.message.append(administration.getToBugService().getCurrentMessage()).append("\n");
                                            }

                                            if (administration.getAdminType() == Administration.AdminType.move) {
                                                administration.getFromBugService().deleteIssue(issue.getId(), id);
                                                if (administration.getFromBugService().getCurrentState() >= 300) {
                                                    this.message.append(administration.getFromBugService().getCurrentMessage()).append("\n");
                                                }
                                            }
                                            this.publishProgress((int) ((100.0 / max) * counter));
                                            counter++;
                                        } catch (Exception ex) {
                                            this.message.append(ex).append("\n");
                                            if(this.debug) {
                                                StringBuilder msg = new StringBuilder();
                                                for (StackTraceElement element : ex.getStackTrace()) {
                                                    msg.append(element.getFileName()).append(".").append(element.getClassName()).append("#").append(element.getMethodName()).append("(").append(element.getLineNumber()).append(")\n");
                                                }
                                                this.message.append(msg).append("\n");
                                            }
                                            this.message.append("----------------------------------------------------------------------------------").append("\n");
                                        }
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
                            if (administration.getToBugService().getCurrentState() >= 300) {
                                this.message.append(administration.getToBugService().getCurrentMessage()).append("\n");
                            }

                            if (administration.getAdminType()== Administration.AdminType.move) {
                                administration.getFromBugService().deleteIssue(id, administration.getFromProject().getId());
                                if (administration.getFromBugService().getCurrentState() >= 300) {
                                    this.message.append(administration.getFromBugService().getCurrentMessage()).append("\n");
                                }
                            }
                        }
                        break;
                    case CustomField:
                        CustomField customField = (CustomField) administration.getDataItem();
                        if (customField != null) {
                            id = customField.getId();
                            customField.setId(null);

                            administration.getToBugService().insertOrUpdateCustomField(customField, administration.getToProject().getId());
                            if (administration.getToBugService().getCurrentState() >= 300) {
                                this.message.append(administration.getToBugService().getCurrentMessage()).append("\n");
                            }

                            if (administration.getAdminType()== Administration.AdminType.move) {
                                administration.getFromBugService().deleteCustomField(id, administration.getFromProject().getId());
                                if (administration.getFromBugService().getCurrentState() >= 300) {
                                    this.message.append(administration.getFromBugService().getCurrentMessage()).append("\n");
                                }
                            }
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return this.message.toString();
    }

    @SuppressWarnings("unchecked")
    private String insertOrUpdateVersion(String version, Object projectId, IBugService bugService) {
        try {
            if(bugService.getPermissions().addVersions()) {

                Version oldVersion = new Version();
                oldVersion.setTitle(version);
                List<Version> versions = bugService.getVersions("versions", projectId);
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

    private Object insertOrUpdateProject(Project project, Administration administration) throws Exception {
        Object id = project.getId();
        project = administration.convertProjectToValidNewProject(project);
        project.setTitle(project.getTitle().replace("-", ""));

        Object newId = null;
        if(administration.isAddToExistingProject()) {
            List projects = administration.getToBugService().getProjects();
            for(Object temp : projects) {
                if(temp instanceof Project proj) {
                    if (proj.getTitle().replace("-", "").equalsIgnoreCase(project.getTitle())) {
                        newId = proj.getId();
                    }
                }
            }
        }
        project.setId(newId);

        for (int i = 0; i <= project.getVersions().size() - 1; i++) {
            ((Version) project.getVersions().get(i)).setId(null);
        }
        Object objId = administration.getToBugService().insertOrUpdateProject(project);
        if (administration.getToBugService().getCurrentState() >= 300) {
            this.message.append(administration.getToBugService().getCurrentMessage()).append("\n");
        }

        if (administration.getAdminType() == Administration.AdminType.move) {
            administration.getFromBugService().deleteProject(id);
            if (administration.getFromBugService().getCurrentState() >= 300) {
                this.message.append(administration.getFromBugService().getCurrentMessage()).append("\n");
            }
        }
        return objId;
    }
}
