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

package de.domjos.unibuggerlibrary.model.issues;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class Profile<T> extends DescriptionObject<T> {
    private String platform;
    private String os;
    private String os_build;

    public Profile() {
        this("", "", "");
    }

    public Profile(String platform, String os, String build) {
        super();

        this.platform = platform;
        this.os = os;
        this.os_build = build;
    }


    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOs_build() {
        return this.os_build;
    }

    public void setOs_build(String os_build) {
        this.os_build = os_build;
    }
}
