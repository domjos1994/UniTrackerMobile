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

package de.domjos.unibuggerlibrary.services.tracker.MantisBTSpecific;

import org.json.JSONObject;

import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;

public class SubProject extends JSONEngine {

    public SubProject(Authentication authentication) {
        super(authentication, "Authorization: " + authentication.getAPIKey());
    }

    public void addSubProject(Project current, Project subProject) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject subProjectObject = new JSONObject();
        subProjectObject.put("name", subProject.getTitle());
        jsonObject.put("project", subProjectObject);
        jsonObject.put("inherit_parent", true);
        this.executeRequest("/api/rest/projects/" + current.getId() + "/subprojects", jsonObject.toString(), "POST");
    }

    public void deleteSubProject(Project current, Project subProject) throws Exception {
        this.deleteRequest("/api/rest/projects/" + current.getId() + "/subprojects/" + subProject.getId());
    }
}
