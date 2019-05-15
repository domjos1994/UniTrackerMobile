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

package de.domjos.unibuggerlibrary.services.tracker;

import org.w3c.dom.Element;

import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.SoapEngine;
import okhttp3.Call;
import okhttp3.Response;

public class MantisBT extends SoapEngine implements IBugService<Long> {


    public MantisBT(Authentication authentication) {
        super(authentication, "/api/soap/mantisconnect.php");
    }

    @Override
    public String getTrackerVersion() throws Exception {
        Element element = this.startDocument();
        Element sub = super.document.createElement("n0:mc_version");
        sub.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:n0", super.soapPath);
        element.appendChild(sub);
        Call call = this.closeDocumentAndSend(element, "mc_version");
        Response response = call.execute();
        return response.message();
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        this.getTrackerVersion();
        return null;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {

    }

    @Override
    public List<Version<Long>> getVersions(Long pid) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {

    }

    @Override
    public int getCurrentState() {
        return 0;
    }

    @Override
    public String getCurrentMessage() {
        return null;
    }
}
