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

import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.tracker.GithubSpecific.SearchAll;

public final class ProjectTask extends CustomAbstractTask<Object, Void, List<Project>> {
    private boolean delete;
    private String search;

    public ProjectTask(Activity activity, IBugService bugService, boolean delete, boolean showNotifications, int icon) {
        super(activity, bugService, R.string.task_project_list_title, R.string.task_project_content, showNotifications, icon);
        this.delete = delete;
        this.search = "";
    }

    public ProjectTask(String search, Activity activity, IBugService bugService, boolean delete, boolean showNotifications, int icon) {
        super(activity, bugService, R.string.task_project_list_title, R.string.task_project_content, showNotifications, icon);
        this.delete = delete;
        this.search = search;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Project> doInBackground(Object... projects) {
        List<Project> result = new LinkedList<>();
        try {
            for (Object project : projects) {
                if (project instanceof Project) {
                    super.bugService.insertOrUpdateProject((Project) project);
                } else {
                    if (this.delete) {
                        super.bugService.deleteProject(super.returnTemp(project));
                    } else {
                        if(super.bugService.getAuthentication().getTracker() != Authentication.Tracker.Github) {
                            result.addAll(super.bugService.getProjects());
                        } else {
                            if(!this.search.isEmpty()) {
                                result.addAll(new SearchAll(super.bugService.getAuthentication()).getProjects(this.search));
                            }
                            result.addAll(super.bugService.getProjects());
                        }
                    }
                }
            }
            super.printResult();
        } catch (Exception ex) {
            super.printException(ex);
        }
        return result;
    }
}
