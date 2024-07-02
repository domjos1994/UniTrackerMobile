/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackerlibrary.export;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;

/** @noinspection rawtypes, unchecked */
public final class TrackerPDF<T> extends AbstractTracker<T> {
    private final byte[] array;
    private final byte[] icon;

    public TrackerPDF(IBugService<T> bugService, Type type, T pid, List<T> ids, String path) {
        super(bugService, type, pid, ids, path);
        this.array = null;
        this.icon = null;
    }

    public TrackerPDF(IBugService<T> bugService, Type type, T pid, List<T> ids, String path, byte[] array, byte[] icon) {
        super(bugService, type, pid, ids, path);
        this.array = array;
        this.icon = icon;
    }

    public void createChangeLog(Version version) throws Exception {
        List<Issue> issues = (List<Issue>) ids;
        ObjectPDF.createChangeLog(issues, path, array, icon, version);
    }

    @Override
    public void doExport() throws Exception {
        switch (this.type) {
            case Projects:
                List<Project> projects = new LinkedList<>();
                for (T id : this.ids) {
                    Project<T> project = this.bugService.getProject(id);
                    project.setVersions(this.bugService.getVersions("versions", id));
                    projects.add(project);
                }
                ObjectPDF.saveObjectToPDF(projects, this.path, this.array, this.icon);
                break;
            case Issues:
                List<Issue> issues = new LinkedList<>();
                for (T id : this.ids) {
                    issues.add(this.bugService.getIssue(id, this.pid));
                }
                ObjectPDF.saveObjectToPDF(issues, this.path, this.array, this.icon);
                break;
            case CustomFields:
                List<CustomField> customFields = new LinkedList<>();
                for (T id : this.ids) {
                    customFields.add(this.bugService.getCustomField(id, this.pid));
                }
                ObjectPDF.saveObjectToPDF(customFields, this.path, this.array, this.icon);
                break;
            default:
                return;
        }
        Log.v("Msg", "finish!");
    }
}
