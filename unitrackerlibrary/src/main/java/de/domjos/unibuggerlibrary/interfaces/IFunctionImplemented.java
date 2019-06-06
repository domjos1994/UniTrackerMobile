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

public interface IFunctionImplemented {

    boolean listProjects();

    boolean addProjects();

    boolean updateProjects();

    boolean deleteProjects();


    boolean listVersions();

    boolean addVersions();

    boolean updateVersions();

    boolean deleteVersions();


    boolean listIssues();

    boolean addIssues();

    boolean updateIssues();

    boolean deleteIssues();


    boolean listNotes();

    boolean addNotes();

    boolean updateNotes();

    boolean deleteNotes();


    boolean listAttachments();

    boolean addAttachments();

    boolean updateAttachments();

    boolean deleteAttachments();


    boolean listUsers();

    boolean addUsers();

    boolean updateUsers();

    boolean deleteUsers();


    boolean listCustomFields();

    boolean addCustomFields();

    boolean updateCustomFields();

    boolean deleteCustomFields();

    boolean listHistory();

    boolean listProfiles();
}
