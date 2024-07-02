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

package de.domjos.unitrackerlibrary.model.projects;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;

/** @noinspection unused*/
public class Project<T> extends DescriptionObject<T> {
    private String alias;
    private boolean privateProject;
    private boolean enabled;
    private long releasedAt;
    private long createdAt;
    private long updatedAt;
    private List<Version<T>> versions;
    private String website;
    private String status;
    private int statusID;
    private List<Project<T>> subProjects;
    private String iconUrl;
    private String defaultVersion;

    public Project() {
        super();

        this.alias = "";
        this.privateProject = false;
        this.releasedAt = 0L;
        this.createdAt = 0L;
        this.updatedAt = 0L;
        this.versions = new LinkedList<>();
        this.subProjects = new LinkedList<>();
        this.status = "";
        this.statusID = 0;
        this.iconUrl = "";
    }

    public boolean isPrivateProject() {
        return this.privateProject;
    }

    public void setPrivateProject(boolean privateProject) {
        this.privateProject = privateProject;
    }

    public long getReleasedAt() {
        return this.releasedAt;
    }

    public void setReleasedAt(long releasedAt) {
        this.releasedAt = releasedAt;
    }

    public List<Version<T>> getVersions() {
        return this.versions;
    }

    public void setVersions(List<Version<T>> versions) {
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

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status, int id) {
        this.status = status;
        this.statusID = id;
    }

    public int getStatusID() {
        return this.statusID;
    }

    public List<Project<T>> getSubProjects() {
        return this.subProjects;
    }

    public void setSubProjects(List<Project<T>> subProjects) {
        this.subProjects = subProjects;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getDefaultVersion() {
        return this.defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }
}
