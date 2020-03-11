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

import java.util.Arrays;
import java.util.List;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.export.TrackerCSV;
import de.domjos.unitrackerlibrary.export.TrackerPDF;
import de.domjos.unitrackerlibrary.export.TrackerXML;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Version;

public final class ExportTask extends AbstractTask<Object, Void, Void> {
    private String path, xslt;
    private TrackerXML.Type type;
    private Object project_id, version_id;
    private byte[] array, icon;

    public ExportTask(Activity activity, IBugService bugService, TrackerXML.Type type, Object project_id, String path, boolean showNotifications, int icon, byte[] array, byte[] appIcon, String xslt) {
        this(activity, bugService, type, project_id, path, showNotifications, icon, array, appIcon, xslt, null);
    }

    public ExportTask(Activity activity, IBugService bugService, TrackerXML.Type type, Object project_id, String path, boolean showNotifications, int icon, byte[] array, byte[] appIcon, String xslt, Object versionId) {
        super(activity, bugService, R.string.task_export_title, R.string.task_export_contet, showNotifications, icon);
        this.path = path;
        this.type = type;
        this.project_id = project_id;
        this.array = array;
        this.icon = appIcon;
        this.xslt = xslt;
        this.version_id = versionId;
    }

    @Override
    protected void before() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected Void doInBackground(Object... objects) {
        try {
            if(this.version_id == null) {
                List<Object> objectList = Arrays.asList(objects);
                String[] splPath = this.path.split("\\.");
                String extension = splPath[splPath.length - 1];

                switch (extension.trim().toLowerCase()) {
                    case "xml":
                        TrackerXML buggerXML = new TrackerXML(super.bugService, this.type, this.project_id, objectList, this.path, this.xslt);
                        buggerXML.doExport();
                        break;
                    case "txt":
                    case "csv":
                        TrackerCSV buggerCSV = new TrackerCSV(super.bugService, this.type, this.project_id, objectList, this.path);
                        buggerCSV.doExport();
                        break;
                    case "pdf":
                        TrackerPDF buggerPDF = new TrackerPDF(super.bugService, this.type, this.project_id, objectList, this.path, this.array, this.icon);
                        buggerPDF.doExport();
                        break;
                }
            } else {
                List<Issue> issues = super.bugService.getIssues(super.returnTemp(this.project_id));
                for(int i = 0; i<=issues.size()-1; i++) {
                    issues.set(i, super.bugService.getIssue(issues.get(i).getId(), super.returnTemp(project_id)));
                }

                List<Version> versions = super.bugService.getVersions("versions", super.returnTemp(this.project_id));
                TrackerPDF<Issue> buggerPDF = new TrackerPDF(super.bugService, this.type, this.project_id, issues, this.path, this.array, this.icon);
                for(Version current : versions) {
                    if(current.getId().equals(this.version_id)) {
                        buggerPDF.createChangeLog(current);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }
}
