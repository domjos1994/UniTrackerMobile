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

import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.unitrackerlibrary.interfaces.IBugService;

/** @noinspection rawtypes*/
public abstract class CustomAbstractTask<Params, Progress, Result> extends AbstractTask<Params, Progress, Result> {
    protected IBugService bugService;

    public CustomAbstractTask(Activity activity, IBugService bugService, int title, int content, boolean showNotifications, int icon) {
        super(activity, title, content, showNotifications, icon);

        this.bugService = bugService;
    }

    protected Object returnTemp(Object o) {
        if (o == null) {
            return null;
        } else {
            if (o.equals(0) || o.equals("")) {
                return null;
            } else {
                Object tmp;
                try {
                    tmp = Long.parseLong(String.valueOf(o));
                } catch (Exception ex) {
                    tmp = o;
                }
                return tmp;
            }
        }
    }

    protected void printResult() {
        if(this.bugService.getCurrentState() >= 400) {
            if(!this.bugService.getCurrentMessage().trim().isEmpty()) {
                super.printMessage(this.bugService.getCurrentMessage());
            }
        }
    }
}
