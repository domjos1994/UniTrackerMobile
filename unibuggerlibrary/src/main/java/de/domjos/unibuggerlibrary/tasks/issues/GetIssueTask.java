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

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.services.tracker.Github;
import de.domjos.unibuggerlibrary.tasks.general.AbstractTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;

public class GetIssueTask extends AbstractTask<String, Void, Issue> {
    private String title;

    public GetIssueTask(Activity activity, IBugService bugService, boolean showNotifications) {
        super(activity, bugService, R.string.task_issues_list_title, R.string.task_issue_load, showNotifications);
        this.title = "";
    }

    public GetIssueTask(Activity activity, IBugService bugService, boolean showNotifications, String title) {
        super(activity, bugService, R.string.task_issues_list_title, R.string.task_issue_load, showNotifications);
        this.title = title;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    protected Issue doInBackground(String[] objects) {
        if (!objects[0].equals("")) {
            try {
                if (this.title.isEmpty()) {
                    return super.bugService.getIssue(objects[0]);
                } else {
                    return ((Github) super.bugService).getIssue(Long.parseLong(objects[0]), this.title);
                }
            } catch (Exception ex) {
                try {
                    if (this.title.isEmpty()) {
                        return super.bugService.getIssue(Long.parseLong(objects[0]));
                    } else {
                        return ((Github) super.bugService).getIssue(Long.parseLong(objects[0]), this.title);
                    }
                } catch (Exception e) {
                    super.activity.runOnUiThread(() -> MessageHelper.printException(e, super.activity));
                    return null;
                }
            }
        } else {
            return null;
        }
    }
}
