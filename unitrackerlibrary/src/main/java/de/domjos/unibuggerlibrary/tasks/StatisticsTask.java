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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;

public final class StatisticsTask extends AbstractTask<Void, Void, Map<Authentication, Map<Project, List<Issue>>>> {
    private List<IBugService> bugServices;

    public StatisticsTask(Activity activity, List<IBugService> bugServices, boolean showNotifications, int icon) {
        super(activity, null, R.string.task_statistics_title, R.string.task_statistics_content, showNotifications, icon);
        this.bugServices = bugServices;
    }

    @Override
    protected void before() {

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
                    for (Issue issue : issues) {
                        try {
                            currentIssues.add(bugService.getIssue(issue.getId(), project.getId()));
                        } catch (Exception ex) {
                            super.printException(ex);
                        }
                    }
                    projectMap.put(project, currentIssues);
                }
                data.put(bugService.getAuthentication(), projectMap);
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return data;
    }
}
