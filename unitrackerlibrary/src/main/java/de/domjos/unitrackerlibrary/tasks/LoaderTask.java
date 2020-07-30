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

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.interfaces.IBugService;

public final class LoaderTask extends CustomAbstractTask<Object, Void, Object> {
    private Type type;

    public LoaderTask(Activity activity, IBugService bugService, boolean showNotifications, Type type) {
        super(activity, bugService, R.string.task_loader_title, R.string.task_loader_content, showNotifications, R.mipmap.ic_launcher_round);
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object doInBackground(Object... objects) {
        try {
            if (objects.length != 0) {
                Object obj = objects[0];

                Object id = super.returnTemp(obj);
                switch (type) {
                    case Categories:
                        if (id != null) {
                            return super.bugService.getCategories(super.returnTemp(id));
                        }
                        break;
                    case Profiles:
                        return super.bugService.getProfiles();
                    case Tags:
                        if (id != null && !id.equals("null")) {
                            return super.bugService.getTags(super.returnTemp(id));
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }

    public enum Type {
        Categories,
        Profiles,
        Tags
    }
}
