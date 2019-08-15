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
import de.domjos.unibuggerlibrary.model.issues.Issue;

public final class IssueTask extends AbstractTask<Object, Void, List<Issue>> {
    private boolean delete;
    private boolean oneDetailed;
    private Object project_id;
    private int numberOfItems, page;
    private String filter;

    public IssueTask(Activity activity, IBugService bugService, Object project_id, boolean delete, boolean oneDetailed, boolean showNotifications, int icon) {
        this(activity, bugService, project_id, 1, -1, "", delete, oneDetailed, showNotifications, icon);
    }

    public IssueTask(Activity activity, IBugService bugService, Object project_id, int page, int numberOfItems, String filter, boolean delete, boolean oneDetailed, boolean showNotifications, int icon) {
        super(activity, bugService, R.string.task_version_list_title, R.string.task_version_content, showNotifications, icon);
        this.delete = delete;
        this.oneDetailed = oneDetailed;
        this.project_id = project_id;
        this.numberOfItems = numberOfItems;
        this.page = page;
        this.filter = filter;
    }



    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Issue> doInBackground(Object... issues) {
        List<Issue> result = new LinkedList<>();
        try {
            if (this.project_id != null) {
                if (!this.project_id.equals("null")) {
                    for (Object issue : issues) {
                        if (issue instanceof Issue) {
                            super.bugService.insertOrUpdateIssue((Issue) issue, super.returnTemp(this.project_id));
                        } else {
                            if (this.delete) {
                                super.bugService.deleteIssue(super.returnTemp(issue), super.returnTemp(this.project_id));
                            } else {
                                if (this.oneDetailed) {
                                    result.add(super.bugService.getIssue(super.returnTemp(issue), super.returnTemp(this.project_id)));
                                } else {
                                    if (this.filter.isEmpty()) {
                                        result.addAll(super.bugService.getIssues(super.returnTemp(this.project_id), this.page, this.numberOfItems));
                                    } else {
                                        result.addAll(super.bugService.getIssues(super.returnTemp(this.project_id), this.page, this.numberOfItems, IBugService.IssueFilter.valueOf(this.filter)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (!ex.getMessage().contains("Undefined variable: p_bug_id")) {
                super.printException(ex);
            }
        }
        return result;
    }
}
