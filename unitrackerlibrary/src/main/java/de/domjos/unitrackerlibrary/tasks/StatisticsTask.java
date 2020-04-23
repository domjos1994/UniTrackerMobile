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

package de.domjos.unitrackerlibrary.tasks;

import android.app.Activity;
import android.widget.ProgressBar;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.customwidgets.model.tasks.ProgressBarTask;
import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public final class StatisticsTask extends ProgressBarTask<Void, Map<Authentication, Map<Project, List<Issue>>>> {
    private List<IBugService> bugServices;
    private Update update;

    public StatisticsTask(Activity activity, List<IBugService> bugServices, boolean showNotifications, int icon, ProgressBar progressBar) {
        super(activity, R.string.task_statistics_title, R.string.task_statistics_content, showNotifications, icon, progressBar);

        this.bugServices = bugServices;
    }

    public void setOnUpdate(Update update) {
        this.update = update;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<Authentication, Map<Project, List<Issue>>> doInBackground(Void... voids) {
        Map<Authentication, Map<Project, List<Issue>>> data = new LinkedHashMap<>();
        try {
            for (IBugService bugService : this.bugServices) {
                Map<Project, List<Issue>> projectMap = new LinkedHashMap<>();
                List<Project> projects = bugService.getProjects();
                for (Project project : projects) {
                    List<Issue> currentIssues = new LinkedList<>();
                    List<Issue> issues = bugService.getIssues(project.getId());
                    this.max = issues.size();
                    int counter = 0;
                    for (Issue issue : issues) {
                        try {
                            if (issue.getLastUpdated() == null || issue.getHandler() == null) {
                                currentIssues.add(bugService.getIssue(issue.getId(), project.getId()));
                            } else {
                                currentIssues.add(issue);
                            }
                            this.publishProgress(counter);
                        } catch (Exception ex) {
                            super.printException(ex);
                        } finally {
                            counter++;
                        }
                    }
                    projectMap.put(project, currentIssues);
                }
                data.put(bugService.getAuthentication(), projectMap);
                this.update.onUpdate(bugService.getAuthentication(), projectMap);
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return data;
    }

    @FunctionalInterface
    public interface Update {
        void onUpdate(Authentication authentication, Map<Project, List<Issue>> data);
    }
}
