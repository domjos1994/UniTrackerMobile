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

import java.util.Arrays;
import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.export.BuggerXML;
import de.domjos.unibuggerlibrary.interfaces.IBugService;

public final class ExportTask extends AbstractTask<Object, Void, Void> {
    private String path;
    private BuggerXML.Type type;
    private Object project_id;

    public ExportTask(Activity activity, IBugService bugService, BuggerXML.Type type, Object project_id, String path, boolean showNotifications) {
        super(activity, bugService, R.string.task_export_title, R.string.task_export_contet, showNotifications);
        this.path = path;
        this.type = type;
        this.project_id = project_id;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected Void doInBackground(Object... objects) {
        try {
            List<Object> objectList = Arrays.asList(objects);
            BuggerXML buggerXML = new BuggerXML(super.bugService, this.type, this.project_id, objectList, this.path);
            buggerXML.doExport();
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }
}
