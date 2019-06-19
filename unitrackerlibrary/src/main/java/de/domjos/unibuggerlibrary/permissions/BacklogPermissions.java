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

package de.domjos.unibuggerlibrary.permissions;

import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;

public final class BacklogPermissions implements IFunctionImplemented {
    @Override
    public boolean listProjects() {
        return true;
    }

    @Override
    public boolean addProjects() {
        return true;
    }

    @Override
    public boolean updateProjects() {
        return true;
    }

    @Override
    public boolean deleteProjects() {
        return true;
    }

    @Override
    public boolean listVersions() {
        return true;
    }

    @Override
    public boolean addVersions() {
        return true;
    }

    @Override
    public boolean updateVersions() {
        return true;
    }

    @Override
    public boolean deleteVersions() {
        return true;
    }

    @Override
    public boolean listIssues() {
        return false;
    }

    @Override
    public boolean addIssues() {
        return false;
    }

    @Override
    public boolean updateIssues() {
        return false;
    }

    @Override
    public boolean deleteIssues() {
        return false;
    }

    @Override
    public boolean listNotes() {
        return false;
    }

    @Override
    public boolean addNotes() {
        return false;
    }

    @Override
    public boolean updateNotes() {
        return false;
    }

    @Override
    public boolean deleteNotes() {
        return false;
    }

    @Override
    public boolean listAttachments() {
        return false;
    }

    @Override
    public boolean addAttachments() {
        return false;
    }

    @Override
    public boolean updateAttachments() {
        return false;
    }

    @Override
    public boolean deleteAttachments() {
        return false;
    }

    @Override
    public boolean listUsers() {
        return true;
    }

    @Override
    public boolean addUsers() {
        return false;
    }

    @Override
    public boolean updateUsers() {
        return false;
    }

    @Override
    public boolean deleteUsers() {
        return false;
    }

    @Override
    public boolean listCustomFields() {
        return true;
    }

    @Override
    public boolean addCustomFields() {
        return true;
    }

    @Override
    public boolean updateCustomFields() {
        return true;
    }

    @Override
    public boolean deleteCustomFields() {
        return true;
    }

    @Override
    public boolean listHistory() {
        return false;
    }

    @Override
    public boolean listProfiles() {
        return false;
    }
}
