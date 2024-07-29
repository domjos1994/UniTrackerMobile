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

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.export.AbstractTracker;
import de.domjos.unitrackerlibrary.export.TrackerPDF;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;

/** @noinspection rawtypes*/
public final class LocalSyncTask extends CustomProgressBarTask<Void, String> {
    private final String path;
    private Object pid;

    public LocalSyncTask(Activity activity, IBugService bugService, boolean showNotifications, String path, Object pid, ProgressBar pbProcess) {
        super(activity, bugService, R.string.task_local_title, R.string.task_local_content, showNotifications, R.mipmap.ic_launcher_round, pbProcess);
        this.path = path;
        this.pid = pid;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected String doInBackground(Void... voids) {
        try {
            // create base-directory
            File directory = new File(this.path);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    return String.format(this.getContext().getString(R.string.messages_local_sync_dir), directory.getAbsolutePath());
                }
            }

            // convert name to path
            String name = LocalSyncTask.renameToPathPart(super.bugService.getAuthentication().getTitle());
            File bugPath = new File(directory.getAbsolutePath() + File.separatorChar + name);
            if (!bugPath.exists()) {
                if (!bugPath.mkdirs()) {
                    return String.format(this.getContext().getString(R.string.messages_local_sync_dir), bugPath.getAbsolutePath());
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
                        return String.format(this.getContext().getString(R.string.messages_local_sync_dir), pPath.getAbsolutePath());
                    }
                }

                List<Issue> issues = super.bugService.getIssues(project.getId());
                this.max = issues.size();
                int current = 1;
                for (Issue issue : issues) {
                    try {
                        String iName = LocalSyncTask.renameToPathPart(issue.getTitle());
                        File iPath = new File(pPath.getAbsolutePath() + File.separatorChar + iName);
                        if (!iPath.exists()) {
                            if (!iPath.mkdirs()) {
                                return String.format(this.getContext().getString(R.string.messages_local_sync_dir), iPath.getAbsolutePath());
                            }
                        }

                        List<Attachment> attachments = super.bugService.getIssue(issue.getId(), project.getId()).getAttachments();
                        if (attachments != null) {
                            if (!attachments.isEmpty()) {
                                File aPath = new File(iPath.getAbsolutePath() + File.separatorChar + "attachments");
                                if (!aPath.exists()) {
                                    if (!aPath.mkdirs()) {
                                        return String.format(this.getContext().getString(R.string.messages_local_sync_dir), aPath.getAbsolutePath());
                                    }
                                }

                                for (Attachment attachment : attachments) {
                                    ConvertHelper.convertByteArrayToFile(attachment.getContent(), new File(aPath.getAbsolutePath() + File.separatorChar + attachment.getFilename()));
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
                    } catch (Exception ex) {
                        super.printException(ex);
                    } finally {
                        this.publishProgress(current);
                        current++;
                    }
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return this.getContext().getString(R.string.messages_local_sync_success);
    }

    public static String renameToPathPart(String name) {
        name = name.replaceAll("ä", "ae");
        name = name.replaceAll("Ä", "#Ae#");
        name = name.replaceAll("ö", "#oe#");
        name = name.replaceAll("Ö", "#Oe#");
        name = name.replaceAll("ü", "#ue#");
        name = name.replaceAll("Ü", "#Ue#");
        name = name.replaceAll("ß", "#ss#");
        name = name.replaceAll(" ", "#_#");
        return name.replaceAll("\\.", "#-#");
    }

    public static String pathPartToContent(String name) {
        name = name.replaceAll("#ae#", "ä");
        name = name.replaceAll("#Ae#", "Ä");
        name = name.replaceAll("#oe#", "ö");
        name = name.replaceAll("#Oe#", "Ö");
        name = name.replaceAll("#ue#", "ü");
        name = name.replaceAll("#Ue#", "Ü");
        name = name.replaceAll("#ss#", "ß");
        name = name.replaceAll("#_#", " ");
        return name.replaceAll("#-#", ".");
    }
}
