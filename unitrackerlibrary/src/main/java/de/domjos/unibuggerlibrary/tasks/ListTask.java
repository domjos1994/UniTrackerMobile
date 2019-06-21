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

import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;

public final class ListTask<T> extends AbstractTask<Object, Void, List<T>> {
    private ListItem item;

    public ListTask(Activity activity, IBugService bugService, ListItem item, boolean showNotifications) {
        super(activity, bugService, R.string.task_list_title, R.string.task_list_content, showNotifications);
        this.item = item;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<T> doInBackground(Object... objects) {
        try {
            switch (this.item) {
                case Users:
                    return super.bugService.getUsers(objects[0]);
                case Tags:
                    return super.bugService.getTags(objects[0]);
                case Profiles:
                    return super.bugService.getProfiles();
                case Categories:
                    return this.bugService.getCategories(objects[0]);
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }

    public enum ListItem {
        Users,
        Tags,
        Profiles,
        Categories
    }
}
