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

package de.domjos.unibuggerlibrary.interfaces;

import java.util.List;

import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;

/**
 * Interface for all Bug-Tracker-Classes
 * NOTICE: Don't forget to add new Bug-Tracker-Classes to enum in Authentication-Class.
 *
 * @param <T> DataType of ID
 * @author Dominic Joas
 * @version 1.0
 * @see de.domjos.unibuggerlibrary.services.engine.Authentication
 * @see de.domjos.unibuggerlibrary.services
 */
public interface IBugService<T> {

    /**
     * The connection will be tested with the selected Authentication
     *
     * @return connection was successfully
     */
    boolean testConnection() throws Exception;

    /**
     * Returns the Version of the Tracker
     * @return the Tracker-Version
     */
    String getTrackerVersion() throws Exception;

    /**
     * Returns a List of Projects
     *
     * @return List of Project
     */
    List<Project<T>> getProjects() throws Exception;

    /**
     * Returns Data of a Project with the ID
     * @param id th ID of a Project
     * @return the Project
     */
    Project<T> getProject(T id) throws Exception;

    T insertOrUpdateProject(Project<T> project) throws Exception;

    void deleteProject(T id) throws Exception;

    List<Version<T>> getVersions(T pid, String filter) throws Exception;

    T insertOrUpdateVersion(T pid, Version<T> version) throws Exception;

    void deleteVersion(T id) throws Exception;

    int getCurrentState();

    String getCurrentMessage();

    List<Issue<T>> getIssues(T pid) throws Exception;

    Issue<T> getIssue(T id) throws Exception;

    T insertOrUpdateIssue(T pid, Issue<T> issue) throws Exception;

    void deleteIssue(T id) throws Exception;

    List<String> getCategories(T pid) throws Exception;

    List<User<T>> getUsers(T pid) throws Exception;

    User<T> getUser(T id) throws Exception;

    T insertOrUpdateUser(User<T> user) throws Exception;

    void deleteUser(T id) throws Exception;

    List<CustomField<T>> getCustomFields(T pid) throws Exception;

    CustomField<T> getCustomField(T id) throws Exception;

    T insertOrUpdateCustomField(CustomField<T> user) throws Exception;

    void deleteCustomField(T id) throws Exception;

    List<Tag<T>> getTags() throws Exception;

    IFunctionImplemented getPermissions();
}
