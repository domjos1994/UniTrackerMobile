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

package de.domjos.unitrackerlibrary.interfaces;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.model.issues.*;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

/**
 * Interface for all Bug-Tracker-Classes
 * NOTICE: Don't forget to add new Bug-Tracker-Classes to enum in Authentication-Class.
 *
 * @param <T> DataType of ID
 * @author Dominic Joas
 * @version 1.0
 * @see de.domjos.unitrackerlibrary.services.engine.Authentication
 * @see de.domjos.unitrackerlibrary.services
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
     * @return List of Project
     */
    List<Project<T>> getProjects() throws Exception;

    /**
     * Returns Data of a Project with the ID
     * @param id th ID of a Project
     * @return the Project
     */
    Project<T> getProject(T id) throws Exception;

    /**
     * Inserts or updates a Project
     * @param project the Project to insert or update
     * @return ID of Project
     */
    T insertOrUpdateProject(Project<T> project) throws Exception;

    /**
     * Deletes a Project
     * @param id the ID of a Project
     */
    void deleteProject(T id) throws Exception;


    /**
     * Returns a List of Versions
     * @param filter Filter-String
     * @param project_id the ID of the Project
     * @return List of Version
     */
    List<Version<T>> getVersions(String filter, T project_id) throws Exception;

    /**
     * Inserts or updates a Version
     * @param version the Version to insert or update
     * @param project_id the ID of the Project
     */
    void insertOrUpdateVersion(Version<T> version, T project_id) throws Exception;

    /**
     * Deletes a Version
     * @param id the ID of a Version
     * @param project_id the ID of the Project
     */
    void deleteVersion(T id, T project_id) throws Exception;

    /**
     * Returns the number of bugs in the project
     * @param project_id the ID of the Project
     * @param filter the IssueFilter
     * @return the number of bugs in the project
     */
    long getMaximumNumberOfIssues(T project_id, IssueFilter filter) throws Exception;

    /**
     * Returns a List of Issues
     * @param project_id the ID of the Project
     * @return List of Issues
     */
    List<Issue<T>> getIssues(T project_id) throws Exception;

    /**
     * Returns a List of Issues
     * @param project_id the ID of the Project
     * @param filter the IssueFilter
     * @return List of Issues
     */
    List<Issue<T>> getIssues(T project_id, IssueFilter filter) throws Exception;

    /**
     * Returns a List of Issues
     * @param project_id the ID of the Project
     * @param page the current Page
     * @param numberOfItems the Number of Item
     * @return List of Issues
     */
    List<Issue<T>> getIssues(T project_id, int page, int numberOfItems) throws Exception;

    /**
     * Returns a List of Issues
     * @param project_id the ID of the Project
     * @param page the current Page
     * @param numberOfItems the Number of Item
     * @param filter the Filter
     * @return List of Issues
     */
    List<Issue<T>> getIssues(T project_id, int page, int numberOfItems, IssueFilter filter) throws Exception;

    /**
     * Returns an Issue
     * @param id the ID of an Issue
     * @param project_id the ID of the Project
     * @return the Issue
     */
    Issue<T> getIssue(T id, T project_id) throws Exception;

    /**
     * Inserts or updates an Issue
     * @param issue the Issue to insert or update
     * @param project_id the ID of the project
     */
    void insertOrUpdateIssue(Issue<T> issue, T project_id) throws Exception;

    /**
     * Deletes an Issue
     * @param id the ID of an Issue
     * @param project_id the ID of the Project
     */
    void deleteIssue(T id, T project_id) throws Exception;


    /**
     * Returns a List of Notes
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     * @return List of Notes
     */
    List<Note<T>> getNotes(T issue_id, T project_id) throws Exception;

    /**
     * Inserts or updates a Note
     * @param note the Note to insert or update
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     */
    void insertOrUpdateNote(Note<T> note, T issue_id, T project_id) throws Exception;

    /**
     * Deletes a Note
     * @param id the ID of a Note
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     */
    void deleteNote(T id, T issue_id, T project_id) throws Exception;


    /**
     * Returns a List of Attachments
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     * @return List of Attachments
     */
    List<Attachment<T>> getAttachments(T issue_id, T project_id) throws Exception;

    /**
     * Inserts or updates an Attachment
     * @param attachment the Attachment to insert or update
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     */
    void insertOrUpdateAttachment(Attachment<T> attachment, T issue_id, T project_id) throws Exception;

    /**
     * Deletes an Attachment
     * @param id the ID of an Attachment
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     */
    void deleteAttachment(Object id, T issue_id, T project_id) throws Exception;


    /**
     * Returns a list of Bug-Relations
     * @param issue_id the ID of the issue
     * @param project_id the ID of the project
     * @return List of Bug-Relations
     */
    List<Relationship<T>> getBugRelations(T issue_id, T project_id) throws Exception;

    /**
     * Inserts of Updates Bug-Relations
     * @param relationship the Relationship
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     */
    void insertOrUpdateBugRelations(Relationship<T> relationship, T issue_id, T project_id) throws Exception;

    /**
     * Delete a Bug-Relation
     * @param relationship the Relationship
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     */
    void deleteBugRelation(Relationship<T> relationship, T issue_id, T project_id) throws Exception;

    /**
     * Returns a List of Users
     * @param project_id the ID of the Project
     * @return List of User
     */
    List<User<T>> getUsers(T project_id) throws Exception;

    /**
     * Returns an User
     * @param id the ID of an User
     * @param project_id the ID of the Project
     * @return an User
     */
    User<T> getUser(T id, T project_id) throws Exception;

    /**
     * Inserts or updates an User
     * @param user the User to insert or update
     * @param project_id the ID of the Project
     */
    void insertOrUpdateUser(User<T> user, T project_id) throws Exception;

    /**
     * Deletes an User
     * @param id the ID of an User
     * @param project_id the ID of the Project
     */
    void deleteUser(T id, T project_id) throws Exception;


    /**
     * Returns a List of Custom-Fields
     * @param project_id the ID of the Project
     * @return List of Custom-Fields
     */
    List<CustomField<T>> getCustomFields(T project_id) throws Exception;

    /**
     * Returns a Custom-Field
     * @param id the ID of a Custom-Field
     * @param project_id the ID of the Project
     * @return a Custom-Field
     */
    CustomField<T> getCustomField(T id, T project_id) throws Exception;

    /**
     * Inserts or updates a Custom-Field
     * @param customField the Custom-Field to insert or update
     * @param project_id the ID of the Project
     */
    void insertOrUpdateCustomField(CustomField<T> customField, T project_id) throws Exception;

    /**
     * Deletes a Custom-Field
     * @param id the ID of a Custom-Field
     * @param project_id the ID of the Project
     */
    void deleteCustomField(T id, T project_id) throws Exception;


    /**
     * Returns the current HTML-Status
     * @return the current HTML-Status
     */
    int getCurrentState();

    /**
     * Returns the current Message
     * @return the current Message
     */
    String getCurrentMessage();

    /**
     * Returns a List of Categories
     * @param project_id the ID of the Project
     * @return List of Categories
     */
    List<String> getCategories(T project_id) throws Exception;

    /**
     * Returns a List of Tags
     * @param project_id the ID of the Project
     * @return List of Tags
     */
    List<Tag<T>> getTags(T project_id) throws Exception;

    /**
     * Returns a List of History-Items
     * @param issue_id the ID of the Issue
     * @param project_id the ID of the Project
     * @return List of History-Items
     */
    List<History<T>> getHistory(T issue_id, T project_id) throws Exception;

    /**
     * Returns a List of Profiles
     * @return List of Profiles
     */
    List<Profile<T>> getProfiles() throws Exception;

    /**
     * Returns the Permission-Object of current BugTracker
     * @see IFunctionImplemented
     * @return the Permission-Object of current BugTracker
     */
    IFunctionImplemented getPermissions();

    /**
     * Returns the Authentication-Object of current BugTracker
     * @see de.domjos.unitrackerlibrary.services.engine.Authentication
     * @return the Authentication-Object of current BugTracker
     */
    Authentication getAuthentication();

    /**
     * Returns a List of Enum-Items by Title
     * @param type the type
     * @return Map of Enum-Items
     */
    Map<String, String> getEnums(Type type, Context context) throws Exception;

    /**
     * Returns a List of News
     * @return List of News
     */
    List<History<T>> getNews() throws Exception;

    /**
     * Converts Object to String
     * @return the String
     */
    @Override
    @NonNull
    String toString();

    enum IssueFilter {
        all,
        resolved,
        unresolved
    }

    enum Type {
        view_state,
        reproducibility,
        severity,
        priority,
        status,
        resolution,
        relation
    }
}
