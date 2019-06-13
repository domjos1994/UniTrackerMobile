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

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;

public final class ProjectTask extends AbstractTask<Object, Void, List<Project>> {
    private boolean delete;

    public ProjectTask(Activity activity, IBugService bugService, boolean delete, boolean showNotifications) {
        super(activity, bugService, R.string.task_project_list_title, R.string.task_project_content, showNotifications);
        this.delete = delete;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

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
                        result.addAll(super.bugService.getProjects());
                    }
                }
            }
            super.printMessage();
        } catch (Exception ex) {
            super.printException(ex);
        }
        return result;
    }
}
