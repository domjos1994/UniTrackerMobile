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

package de.domjos.unitrackerlibrary.tasks;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.cache.CacheGlobals;
import de.domjos.unitrackerlibrary.cache.UserCache;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.User;

public final class UserTask extends CustomAbstractTask<Object, Void, List<User<?>>> {
    private final Object project_id;
    private final boolean delete;

    public UserTask(Activity activity, IBugService<?> bugService, Object project_id, boolean delete, boolean showNotifications, int icon) {
        super(activity, bugService, R.string.task_user_list_title, R.string.task_user_content, showNotifications, icon);
        this.project_id = project_id;
        this.delete = delete;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<User<?>> doInBackground(Object... objects) {
        List<User<?>> result = new LinkedList<>();
        try {
            Object pid = this.returnTemp(this.project_id);
            if (pid != null) {
                for (Object user : objects) {
                    if (user instanceof User) {
                        super.bugService.insertOrUpdateUser((User<?>) user, pid);
                        CacheGlobals.reload = true;
                    } else {
                        if (this.delete) {
                            super.bugService.deleteUser(super.returnTemp(user), pid);
                            CacheGlobals.reload = true;
                        } else {
                            if(UserCache.mustReload(this.bugService.getAuthentication(), this.project_id)) {
                                result.addAll(super.bugService.getUsers(pid));
                                UserCache.setData(result, this.bugService.getAuthentication(), this.project_id);
                            } else {
                                return UserCache.getUsers();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return result;
    }
}
