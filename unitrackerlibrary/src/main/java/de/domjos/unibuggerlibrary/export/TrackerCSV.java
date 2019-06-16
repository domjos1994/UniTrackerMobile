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

package de.domjos.unibuggerlibrary.export;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;

public final class TrackerCSV<T> extends AbstractTracker<T> {

    public TrackerCSV(IBugService<T> bugService, Type type, T pid, List<T> ids, String path) {
        super(bugService, type, pid, ids, path);
    }

    @Override
    public void doExport() throws Exception {
        switch (this.type) {
            case Projects:
                List<Project> projects = new LinkedList<>();
                for (T id : this.ids) {
                    projects.add(this.bugService.getProject(id));
                }
                ObjectCSV.saveObjectToCSV(projects, this.path);
                break;
            case Issues:
                List<Issue> issues = new LinkedList<>();
                for (T id : this.ids) {
                    issues.add(this.bugService.getIssue(id, this.pid));
                }
                ObjectCSV.saveObjectToCSV(issues, this.path);
                break;
            case CustomFields:
                List<CustomField> customFields = new LinkedList<>();
                for (T id : this.ids) {
                    customFields.add(this.bugService.getCustomField(id, this.pid));
                }
                ObjectCSV.saveObjectToCSV(customFields, this.path);
                break;
            default:
                return;
        }
        Log.v("Msg", "finish!");
    }
}
