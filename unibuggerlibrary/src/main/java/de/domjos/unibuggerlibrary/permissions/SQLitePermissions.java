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

public class SQLitePermissions implements IFunctionImplemented {

    public SQLitePermissions() {

    }

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
    public boolean editNotes() {
        return true;
    }

    @Override
    public boolean updateNotes() {
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
}
