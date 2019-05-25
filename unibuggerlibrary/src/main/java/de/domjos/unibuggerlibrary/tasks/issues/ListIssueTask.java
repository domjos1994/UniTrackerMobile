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

package de.domjos.unibuggerlibrary.tasks.issues;

import android.app.Activity;

import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.tasks.general.AbstractTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;

public class ListIssueTask extends AbstractTask<Void, Void, List<Issue>> {
    private Object pid;

    public ListIssueTask(Activity activity, IBugService bugService, Object pid, boolean showNotifications) {
        super(activity, bugService, R.string.task_issues_list_title, R.string.task_issues_list_content, showNotifications);
        this.pid = pid;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    protected List<Issue> doInBackground(Void... voids) {
        try {
            List<Issue> issues = super.bugService.getIssues(pid);
            super.printMessage();
            return issues;
        } catch (Exception ex) {
            super.activity.runOnUiThread(() -> MessageHelper.printException(ex, super.activity));
            return null;
        }
    }
}
