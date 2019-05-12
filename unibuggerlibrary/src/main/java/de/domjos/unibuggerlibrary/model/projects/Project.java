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

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class Project extends DescriptionObject {
    private String alias;
    private boolean privateProject;
    private boolean releasedProject;
    private boolean enabled;
    private long releasedAt;
    private long createdAt;
    private long updatedAt;
    private List<Version> versions;
    private String website;

    public Project() {
        super();

        this.alias = "";
        this.privateProject = false;
        this.releasedProject = false;
        this.releasedAt = 0L;
        this.createdAt = 0L;
        this.updatedAt = 0L;
        this.versions = new LinkedList<>();
    }

    public boolean isPrivateProject() {
        return this.privateProject;
    }

    public void setPrivateProject(boolean privateProject) {
        this.privateProject = privateProject;
    }

    public boolean isReleasedProject() {
        return this.releasedProject;
    }

    public void setReleasedProject(boolean releasedProject) {
        this.releasedProject = releasedProject;
    }

    public long getReleasedAt() {
        return this.releasedAt;
    }

    public void setReleasedAt(long releasedAt) {
        this.releasedAt = releasedAt;
    }

    public List<Version> getVersions() {
        return this.versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWebsite() {
        return this.website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
