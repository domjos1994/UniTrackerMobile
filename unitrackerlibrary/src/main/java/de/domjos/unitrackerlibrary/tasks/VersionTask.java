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
import de.domjos.unitrackerlibrary.cache.CacheGlobals;
import de.domjos.unitrackerlibrary.cache.VersionCache;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.projects.Version;

public final class VersionTask extends CustomAbstractTask<Object, Void, List<Version<?>>> {
    private boolean delete;
    private Object project_id;
    private String filter;

    public VersionTask(Activity activity, IBugService<?> bugService, Object project_id, boolean delete, boolean showNotifications, String filter, int icon) {
        super(activity, bugService, R.string.task_version_list_title, R.string.task_version_content, showNotifications, icon);
        this.delete = delete;
        this.project_id = project_id;
        this.filter = filter;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Version<?>> doInBackground(Object... versions) {
        List<Version<?>> result = new LinkedList<>();
        try {
            Object pid = super.returnTemp(this.project_id);
            if (pid != null) {
                for (Object version : versions) {
                    if (version instanceof Version) {
                        super.bugService.insertOrUpdateVersion((Version<?>) version, pid);
                        CacheGlobals.reload = true;
                    } else {
                        if (this.delete) {
                            super.bugService.deleteVersion(super.returnTemp(version), pid);
                            CacheGlobals.reload = true;
                        } else {
                            if(VersionCache.mustReload(bugService.getAuthentication(), pid, this.filter)) {
                                result.addAll(super.bugService.getVersions(this.filter, pid));
                                VersionCache.setData(result, bugService.getAuthentication(), pid, filter);
                            } else {
                                return VersionCache.getVersions();
                            }
                        }
                    }
                }
                super.printResult();
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return result;
    }
}
