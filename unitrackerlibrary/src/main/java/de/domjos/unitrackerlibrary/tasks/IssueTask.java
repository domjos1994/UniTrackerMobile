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
import de.domjos.unitrackerlibrary.model.issues.Issue;

public final class IssueTask extends CustomAbstractTask<Object, Void, List<Issue>> {
    private boolean delete;
    private boolean oneDetailed;
    private Object project_id;
    private int numberOfItems, page;
    private String filter;
    private long maximum;

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
        this.maximum = 0;
    }

    public long getMaximum() {
        return this.maximum;
    }

    @Override
    @SuppressWarnings({"unchecked", "CatchMayIgnoreException"})
    protected List<Issue> doInBackground(Object... issues) {
        List<Issue> result = new LinkedList<>();
        try {
            if (this.project_id != null) {
                if (!this.project_id.equals("null")) {
                    for (Object issue : issues) {
                        if (issue instanceof Issue) {
                            super.bugService.insertOrUpdateIssue((Issue) issue, super.returnTemp(this.project_id));
                        } else {
                            if(issue instanceof Boolean) {
                                if (this.filter.isEmpty()) {
                                    this.maximum = super.bugService.getMaximumNumberOfIssues(super.returnTemp(this.project_id), null);
                                } else {
                                    this.maximum = super.bugService.getMaximumNumberOfIssues(super.returnTemp(this.project_id), IBugService.IssueFilter.valueOf(this.filter));
                                }
                            } else {
                                if (this.delete) {
                                    super.bugService.deleteIssue(super.returnTemp(issue), super.returnTemp(this.project_id));
                                } else {
                                    if (this.oneDetailed) {
                                        result.add(super.bugService.getIssue(super.returnTemp(issue), super.returnTemp(this.project_id)));
                                    } else {
                                        if (this.filter.isEmpty()) {
                                            result.addAll(super.bugService.getIssues(super.returnTemp(this.project_id), this.page, this.numberOfItems));
                                            this.maximum = super.bugService.getMaximumNumberOfIssues(super.returnTemp(this.project_id), null);
                                        } else {
                                            result.addAll(super.bugService.getIssues(super.returnTemp(this.project_id), this.page, this.numberOfItems, IBugService.IssueFilter.valueOf(this.filter)));
                                            this.maximum = super.bugService.getMaximumNumberOfIssues(super.returnTemp(this.project_id), IBugService.IssueFilter.valueOf(this.filter));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if(ex.getMessage()!=null) {
                if (!ex.getMessage().contains("Undefined variable: p_bug_id")) {
                    super.printException(ex);
                }
            }
        }
        return result;
    }
}
