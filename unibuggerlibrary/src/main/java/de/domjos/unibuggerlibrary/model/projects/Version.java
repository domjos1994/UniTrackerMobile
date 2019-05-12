/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
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
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggerlibrary.model.projects;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class Version extends DescriptionObject {
    private boolean releasedVersion;
    private boolean deprecatedVersion;
    private long releasedVersionAt;

    public Version() {
        super();

        this.releasedVersion = false;
        this.deprecatedVersion = false;
        this.releasedVersionAt = 0L;
    }

    public boolean isReleasedVersion() {
        return this.releasedVersion;
    }

    public void setReleasedVersion(boolean releasedVersion) {
        this.releasedVersion = releasedVersion;
    }

    public boolean isDeprecatedVersion() {
        return this.deprecatedVersion;
    }

    public void setDeprecatedVersion(boolean deprecatedVersion) {
        this.deprecatedVersion = deprecatedVersion;
    }

    public long getReleasedVersionAt() {
        return this.releasedVersionAt;
    }

    public void setReleasedVersionAt(long releasedVersionAt) {
        this.releasedVersionAt = releasedVersionAt;
    }
}
