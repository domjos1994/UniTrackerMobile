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

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.export.AbstractTracker;
import de.domjos.unibuggerlibrary.export.TrackerPDF;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class LocalSyncTask extends AbstractTask<Void, Void, Void> {
    private String path;
    private Object pid;

    public LocalSyncTask(Activity activity, IBugService bugService, boolean showNotifications, String path, Object pid) {
        super(activity, bugService, R.string.task_local_title, R.string.task_local_content, showNotifications, R.mipmap.ic_launcher_round);
        this.path = path;
        this.pid = pid;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected Void doInBackground(Void... voids) {
        try {
            // create base-directory
            File directory = new File(this.path);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    return null;
                }
            }

            // convert name to path
            String name = LocalSyncTask.renameToPathPart(super.bugService.getAuthentication().getTitle());
            File bugPath = new File(directory.getAbsolutePath() + File.separatorChar + name);
            if (!bugPath.exists()) {
                if (!bugPath.mkdirs()) {
                    return null;
                }
            }

            // sync one or many projects
            this.pid = super.returnTemp(this.pid);
            List<Project> projects = new LinkedList<>();
            if (this.pid != null) {
                projects.add(super.bugService.getProject(this.pid));
            } else {
                projects.addAll(super.bugService.getProjects());
            }

            for (Project project : projects) {
                String pName = LocalSyncTask.renameToPathPart(project.getTitle());
                File pPath = new File(bugPath.getAbsolutePath() + File.separatorChar + pName);
                if (!pPath.exists()) {
                    if (!pPath.mkdirs()) {
                        return null;
                    }
                }

                List<Issue> issues = super.bugService.getIssues(project.getId());
                for (Issue issue : issues) {
                    String iName = LocalSyncTask.renameToPathPart(issue.getTitle());
                    File iPath = new File(pPath.getAbsolutePath() + File.separatorChar + iName);
                    if (!iPath.exists()) {
                        if (!iPath.mkdirs()) {
                            return null;
                        }
                    }

                    List<Attachment> attachments = super.bugService.getIssue(issue.getId(), project.getId()).getAttachments();
                    if (attachments != null) {
                        if (!attachments.isEmpty()) {
                            File aPath = new File(iPath.getAbsolutePath() + File.separatorChar + "attachments");
                            if (!aPath.exists()) {
                                if (!aPath.mkdirs()) {
                                    return null;
                                }
                            }

                            for (Attachment attachment : attachments) {
                                Converter.convertByteArrayToFile(attachment.getContent(), new File(aPath.getAbsolutePath() + File.separatorChar + attachment.getFilename()));
                            }
                        }
                    }

                    TrackerPDF trackerPDF = new TrackerPDF(
                            super.bugService,
                            AbstractTracker.Type.Issues,
                            project.getId(),
                            Collections.singletonList(issue.getId()),
                            iPath.getAbsolutePath() + File.separatorChar + "issue.pdf"
                    );
                    trackerPDF.doExport();
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }

    public static String renameToPathPart(String name) {
        name = name.toLowerCase();
        name = name.replaceAll("ä", "ae");
        name = name.replaceAll("ö", "oe");
        name = name.replaceAll("ü", "ue");
        name = name.replaceAll("ß", "ss");
        name = name.replaceAll(" ", "");
        name = name.replaceAll("\\.", "");
        return name.replaceAll("-", "_");
    }
}
