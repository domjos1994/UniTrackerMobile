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

package de.domjos.unitrackerlibrary.model.issues;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;

public class Note<T> extends DescriptionObject<T> {
    private Map.Entry<Integer, String> state;
    private Date lastUpdated;
    private Date submitDate;

    public Note() {
        super();

        this.state = new AbstractMap.SimpleEntry<>(0, "");
        this.lastUpdated = null;
        this.submitDate = null;
    }

    public Map.Entry<Integer, String> getState() {
        return this.state;
    }

    public void setState(int id, String name) {
        this.state = new AbstractMap.SimpleEntry<>(id, name);
    }

    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getSubmitDate() {
        return this.submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }
}
