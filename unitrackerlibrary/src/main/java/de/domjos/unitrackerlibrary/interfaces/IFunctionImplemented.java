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

package de.domjos.unitrackerlibrary.interfaces;

/**
 * Interface for the Permissions of the Tracker
 * @see de.domjos.unitrackerlibrary.interfaces.IBugService
 * @author Dominic Joas
 * @version 0.1
 */
public interface IFunctionImplemented {

    /**
     * Permission to list Projects
     * @return Permission
     */
    boolean listProjects();

    /**
     * Permission to add Projects
     * @return Permission
     */
    boolean addProjects();

    /**
     * Permission to update Projects
     * @return Permission
     */
    boolean updateProjects();

    /**
     * Permissions to delete Projects
     * @return Permission
     */
    boolean deleteProjects();


    /**
     * Permissions to list Versions
     * @return Permission
     */
    boolean listVersions();

    /**
     * Permissions to add Versions
     * @return Permission
     */
    boolean addVersions();

    /**
     * Permissions to update Versions
     * @return Permission
     */
    boolean updateVersions();

    /**
     * Permissions to delete Versions
     * @return Permission
     */
    boolean deleteVersions();


    /**
     * Permissions to list Issues
     * @return Permission
     */
    boolean listIssues();

    /**
     * Permissions to add Issues
     * @return Permission
     */
    boolean addIssues();

    /**
     * Permissions to update Issues
     * @return Permission
     */
    boolean updateIssues();

    /**
     * Permission to delete Issues
     * @return Permission
     */
    boolean deleteIssues();


    /**
     * Permissions to list Notes
     * @return Permission
     */
    boolean listNotes();

    /**
     * Permissions to add Notes
     * @return Permission
     */
    boolean addNotes();

    /**
     * Permissions to update Notes
     * @return Permission
     */
    boolean updateNotes();

    /**
     * Permissions to delete Notes
     * @return Permission
     */
    boolean deleteNotes();


    /**
     * Permissions to list Attachments
     * @return Permission
     */
    boolean listAttachments();

    /**
     * Permission to add Attachments
     * @return Permission
     */
    boolean addAttachments();

    /**
     * Permissions to update Attachments
     * @return Permission
     */
    boolean updateAttachments();

    /**
     * Permissions to delete Attachments
     * @return Permission
     */
    boolean deleteAttachments();


    /**
     * Permissions to list Relations
     * @return Permission
     */
    boolean listRelations();

    /**
     * Permission to add Relations
     * @return Permission
     */
    boolean addRelation();

    /**
     * Permission to update Relations
     * @return Permission
     */
    boolean updateRelation();

    /**
     * Permission to delete Relations
     * @return Permission
     */
    boolean deleteRelation();


    /**
     * Permission to list Users
     * @return Permission
     */
    boolean listUsers();

    /**
     * Permission to add User
     * @return Permission
     */
    boolean addUsers();

    /**
     * Permission to update User
     * @return Permission
     */
    boolean updateUsers();

    /**
     * Permission to delete User
     * @return Permission
     */
    boolean deleteUsers();


    /**
     * Permission to list Custom-Fields
     * @return Permission
     */
    boolean listCustomFields();

    /**
     * Permission to add Custom-Fields
     * @return Permission
     */
    boolean addCustomFields();

    /**
     * Permission to update Custom-Fields
     * @return Permission
     */
    boolean updateCustomFields();

    /**
     * Permissions to delete Custom-Fields
     * @return Permission
     */
    boolean deleteCustomFields();


    /**
     * Permission to list History
     * @return Permission
     */
    boolean listHistory();

    /**
     * Permission to list Profiles
     * @return Permission
     */
    boolean listProfiles();

    /**
     * Permission to show news
     * @return Permission
     */
    boolean news();
}
