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

import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.History;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;

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


    List<Version<T>> getVersions(String filter, T project_id) throws Exception;

    void insertOrUpdateVersion(Version<T> version, T project_id) throws Exception;

    void deleteVersion(T id, T project_id) throws Exception;


    List<Issue<T>> getIssues(T project_id) throws Exception;

    Issue<T> getIssue(T id, T project_id) throws Exception;

    void insertOrUpdateIssue(Issue<T> issue, T project_id) throws Exception;

    void deleteIssue(T id, T project_id) throws Exception;


    List<Note<T>> getNotes(T issue_id, T project_id) throws Exception;

    void insertOrUpdateNote(Note<T> note, T issue_id, T project_id) throws Exception;

    void deleteNote(T id, T issue_id, T project_id) throws Exception;


    List<Attachment<T>> getAttachments(T issue_id, T project_id) throws Exception;

    void insertOrUpdateAttachment(Attachment<T> attachment, T issue_id, T project_id) throws Exception;

    void deleteAttachment(T id, T issue_id, T project_id) throws Exception;


    List<User<T>> getUsers(T project_id) throws Exception;

    User<T> getUser(T id, T project_id) throws Exception;

    T insertOrUpdateUser(User<T> user, T project_id) throws Exception;

    void deleteUser(T id, T project_id) throws Exception;


    List<CustomField<T>> getCustomFields(T project_id) throws Exception;

    CustomField<T> getCustomField(T id, T project_id) throws Exception;

    T insertOrUpdateCustomField(CustomField<T> user, T project_id) throws Exception;

    void deleteCustomField(T id, T project_id) throws Exception;


    int getCurrentState();

    String getCurrentMessage();

    List<String> getCategories(T project_id) throws Exception;

    List<Tag<T>> getTags(T project_id) throws Exception;

    List<History<T>> getHistory(T issue_id, T project_id) throws Exception;

    IFunctionImplemented getPermissions();

    Authentication getAuthentication();
}
