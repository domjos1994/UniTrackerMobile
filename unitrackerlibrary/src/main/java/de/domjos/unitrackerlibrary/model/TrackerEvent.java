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

package de.domjos.unitrackerlibrary.model;

import de.domjos.customwidgets.widgets.calendar.Event;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;

public final class TrackerEvent extends Event {
    private int icon;
    private IBugService<?> bugTracker;
    private Project<?> project;
    private Version<?> version;
    private Issue<?> issue;

    public TrackerEvent() {
        super();

        this.icon = 0;
        this.bugTracker = null;
        this.project = null;
        this.version = null;
        this.issue = null;
    }

    public void setBugTracker(IBugService<?> bugTracker) {
        this.bugTracker = bugTracker;
    }

    public IBugService<?> getBugTracker() {
        return this.bugTracker;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public int getIcon() {
        return this.icon;
    }

    public Project<?> getProject() {
        return this.project;
    }

    public void setProject(Project<?> project) {
        this.project = project;
    }

    public Version<?> getVersion() {
        return this.version;
    }

    public void setVersion(Version<?> version) {
        this.version = version;
    }

    public Issue<?> getIssue() {
        return this.issue;
    }

    public void setIssue(Issue<?> issue) {
        this.issue = issue;
    }
}
