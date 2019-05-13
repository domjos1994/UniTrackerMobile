/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
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
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggerlibrary.tasks.projects;

import android.app.Activity;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.tasks.general.AbstractTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;

public class ProjectTask extends AbstractTask<Project, Void, Void> {
    private boolean delete;

    public ProjectTask(Activity activity, IBugService bugService, boolean delete) {
        super(activity, bugService, R.string.task_project_list_title, R.string.task_project_content);
        this.delete = delete;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    protected Void doInBackground(Project... projects) {
        try {
            for (Project project : projects) {
                if (this.delete) {
                    super.bugService.deleteProject(String.valueOf(project.getId()));
                } else {
                    super.bugService.insertOrUpdateProject(project);
                }
            }
        } catch (Exception ex) {
            super.activity.runOnUiThread(() -> MessageHelper.printException(ex, super.activity));
        }
        return null;
    }
}
