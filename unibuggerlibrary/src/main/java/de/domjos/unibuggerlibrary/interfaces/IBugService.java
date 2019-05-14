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

import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;

public interface IBugService<T> {

    List<Project<T>> getProjects() throws Exception;

    Project<T> getProject(T id) throws Exception;

    T insertOrUpdateProject(Project<T> project) throws Exception;

    void deleteProject(T id) throws Exception;

    List<Version<T>> getVersions(T pid) throws Exception;

    T insertOrUpdateVersion(T pid, Version<T> version) throws Exception;

    void deleteVersion(T id) throws Exception;

    int getCurrentState();

    String getCurrentMessage();
}
