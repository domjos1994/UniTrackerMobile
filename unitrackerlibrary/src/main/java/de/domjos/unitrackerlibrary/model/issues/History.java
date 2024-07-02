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

package de.domjos.unitrackerlibrary.model.issues;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;

public class History<T> extends DescriptionObject<T> {
    private String field;
    private String newValue;
    private String oldValue;
    private long time;
    private String user;
    private Project<T> project;
    private Version<T> version;
    private Issue<T> issue;

    public History() {
        super();

        this.newValue = "";
        this.oldValue = "";
        this.time = 0L;
        this.user = "";
        this.field = "";
        this.project = null;
        this.version = null;
        this.issue = null;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return this.oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Project<T> getProject() {
        return this.project;
    }

    public void setProject(Project<T> project) {
        this.project = project;
    }

    public Version<T> getVersion() {
        return this.version;
    }

    public void setVersion(Version<T> version) {
        this.version = version;
    }

    public Issue<T> getIssue() {
        return this.issue;
    }

    public void setIssue(Issue<T> issue) {
        this.issue = issue;
    }
}
