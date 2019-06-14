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

import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;

public abstract class AbstractBugger<T> {
    protected IBugService<T> bugService;
    protected Type type;
    protected String path;
    protected T pid;
    List<T> ids;

    AbstractBugger(IBugService<T> bugService, Type type, T pid, List<T> ids, String path) {
        this.bugService = bugService;
        this.type = type;
        this.pid = pid;
        this.ids = ids;
        this.path = path;
    }

    protected abstract void doExport() throws Exception;

    public enum Type {
        Projects,
        Issues,
        CustomFields
    }
}
