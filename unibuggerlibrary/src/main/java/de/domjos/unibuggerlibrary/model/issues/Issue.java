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

package de.domjos.unibuggerlibrary.model.issues;

import java.util.AbstractMap;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class Issue<T> extends DescriptionObject<T> {
    private String category;
    private Map.Entry<Integer, String> state, priority, severity, status, reproducibility, resolution;
    private Date lastUpdated;
    private Date submitDate;
    private Date dueDate;
    private String version;
    private String fixedInVersion;
    private String targetVersion;
    private List<Note<T>> notes;
    private List<Attachment<T>> attachments;

    public Issue() {
        super();
        this.category = "";
        this.state = new AbstractMap.SimpleEntry<>(0, "");
        this.priority = new AbstractMap.SimpleEntry<>(0, "");
        this.severity = new AbstractMap.SimpleEntry<>(0, "");
        this.status = new AbstractMap.SimpleEntry<>(0, "");
        this.reproducibility = new AbstractMap.SimpleEntry<>(0, "");
        this.resolution = new AbstractMap.SimpleEntry<>(0, "");
        this.version = "";
        this.fixedInVersion = "";
        this.targetVersion = "";
        this.lastUpdated = null;
        this.submitDate = null;
        this.dueDate = null;
        this.notes = new LinkedList<>();
        this.attachments = new LinkedList<>();
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map.Entry<Integer, String> getState() {
        return this.state;
    }

    public void setState(int id, String name) {
        this.state = new AbstractMap.SimpleEntry<>(id, name);
    }

    public Map.Entry<Integer, String> getPriority() {
        return this.priority;
    }

    public void setPriority(int id, String name) {
        this.priority = new AbstractMap.SimpleEntry<>(id, name);
    }

    public Map.Entry<Integer, String> getSeverity() {
        return this.severity;
    }

    public void setSeverity(int id, String name) {
        this.severity = new AbstractMap.SimpleEntry<>(id, name);
    }

    public Map.Entry<Integer, String> getStatus() {
        return this.status;
    }

    public void setStatus(int id, String name) {
        this.status = new AbstractMap.SimpleEntry<>(id, name);
    }

    public Map.Entry<Integer, String> getReproducibility() {
        return this.reproducibility;
    }

    public void setReproducibility(int id, String name) {
        this.reproducibility = new AbstractMap.SimpleEntry<>(id, name);
    }

    public Map.Entry<Integer, String> getResolution() {
        return this.resolution;
    }

    public void setResolution(int id, String name) {
        this.resolution = new AbstractMap.SimpleEntry<>(id, name);
    }

    public String getFixedInVersion() {
        return fixedInVersion;
    }

    public void setFixedInVersion(String fixedInVersion) {
        this.fixedInVersion = fixedInVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public List<Note<T>> getNotes() {
        return this.notes;
    }

    public List<Attachment<T>> getAttachments() {
        return this.attachments;
    }
}
