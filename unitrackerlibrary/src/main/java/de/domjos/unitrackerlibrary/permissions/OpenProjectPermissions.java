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

package de.domjos.unitrackerlibrary.permissions;

import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;

public final class OpenProjectPermissions implements IFunctionImplemented {
    @Override
    public boolean listProjects() {
        return true;
    }

    @Override
    public boolean addProjects() {
        return false;
    }

    @Override
    public boolean updateProjects() {
        return false;
    }

    @Override
    public boolean deleteProjects() {
        return false;
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
        return true;
    }

    @Override
    public boolean addIssues() {
        return true;
    }

    @Override
    public boolean updateIssues() {
        return true;
    }

    @Override
    public boolean deleteIssues() {
        return true;
    }

    @Override
    public boolean listNotes() {
        return true;
    }

    @Override
    public boolean addNotes() {
        return true;
    }

    @Override
    public boolean updateNotes() {
        return true;
    }

    @Override
    public boolean deleteNotes() {
        return true;
    }

    @Override
    public boolean listAttachments() {
        return true;
    }

    @Override
    public boolean addAttachments() {
        return true;
    }

    @Override
    public boolean updateAttachments() {
        return true;
    }

    @Override
    public boolean deleteAttachments() {
        return true;
    }

    @Override
    public boolean listRelations() {
        return false;
    }

    @Override
    public boolean addRelation() {
        return false;
    }

    @Override
    public boolean updateRelation() {
        return false;
    }

    @Override
    public boolean deleteRelation() {
        return false;
    }

    @Override
    public boolean listUsers() {
        return true;
    }

    @Override
    public boolean addUsers() {
        return true;
    }

    @Override
    public boolean updateUsers() {
        return true;
    }

    @Override
    public boolean deleteUsers() {
        return true;
    }

    @Override
    public boolean listCustomFields() {
        return false;
    }

    @Override
    public boolean addCustomFields() {
        return false;
    }

    @Override
    public boolean updateCustomFields() {
        return false;
    }

    @Override
    public boolean deleteCustomFields() {
        return false;
    }

    @Override
    public boolean listHistory() {
        return true;
    }

    @Override
    public boolean listProfiles() {
        return false;
    }

    @Override
    public boolean news() {
        return true;
    }
}
