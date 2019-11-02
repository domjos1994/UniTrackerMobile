/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

import java.util.Map;

public class Relationship<T> extends DescriptionObject<T> {
    private Issue<T> issue;
    private Map.Entry<String, Integer> type;

    public Relationship() {
        super();

        this.issue = null;
        this.type = null;
    }

    public Issue<T> getIssue() {
        return this.issue;
    }

    public void setIssue(Issue<T> issue) {
        this.setTitle(issue.getTitle());
        this.issue = issue;
    }

    public Map.Entry<String, Integer> getType() {
        return this.type;
    }

    public void setType(Map.Entry<String, Integer> type) {
        this.setDescription(type.getKey());
        this.type = type;
    }
}
