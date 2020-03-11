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

package de.domjos.unitrackerlibrary.model.objects;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

import de.domjos.unitrackerlibrary.model.local.LocalObject;

public class DescriptionObject<T> extends LocalObject<T> {
    private String title;
    private String description;
    private Map<String, String> hints;

    public DescriptionObject() {
        super();
        this.title = "";
        this.description = "";
        this.setHints(new LinkedHashMap<>());
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    @NonNull
    public String toString() {
        return this.title;
    }

    public Map<String, String> getHints() {
        return this.hints;
    }

    public void setHints(Map<String, String> hints) {
        this.hints = hints;
    }
}
