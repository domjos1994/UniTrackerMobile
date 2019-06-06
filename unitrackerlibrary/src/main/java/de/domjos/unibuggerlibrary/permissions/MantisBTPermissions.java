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

package de.domjos.unibuggerlibrary.permissions;

import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.services.engine.Authentication;

public class MantisBTPermissions implements IFunctionImplemented {
    private Authentication authentication;

    public MantisBTPermissions(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public boolean listProjects() {
        return true;
    }

    @Override
    public boolean addProjects() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean updateProjects() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean deleteProjects() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean listVersions() {
        return true;
    }

    @Override
    public boolean addVersions() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean updateVersions() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean deleteVersions() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean listIssues() {
        return true;
    }

    @Override
    public boolean addIssues() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean updateIssues() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean deleteIssues() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean listNotes() {
        return true;
    }

    @Override
    public boolean addNotes() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean updateNotes() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean deleteNotes() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean listAttachments() {
        return true;
    }

    @Override
    public boolean addAttachments() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean updateAttachments() {
        return !this.authentication.isGuest();
    }

    @Override
    public boolean deleteAttachments() {
        return !this.authentication.isGuest();
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
        return !this.authentication.isGuest();
    }
}
