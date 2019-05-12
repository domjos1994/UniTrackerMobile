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

package de.domjos.unibugger.services.tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibugger.interfaces.IBugService;
import de.domjos.unibugger.model.projects.Project;
import de.domjos.unibugger.services.engine.Authentication;
import de.domjos.unibugger.services.engine.JSONEngine;

public final class Bugzilla extends JSONEngine implements IBugService {

    public Bugzilla(Authentication authentication) {
        super(authentication, "X-BUGZILLA-API-KEY: " + authentication.getAPIKey());
    }

    @Override
    public List<Project> getProjects() throws Exception {
        List<Project> projects = new LinkedList<>();
        int status = this.executeRequest("/rest/product_selectable");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("ids");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                projects.add(this.getProject(String.valueOf(jsonArray.getInt(i))));
            }
        }
        return projects;
    }

    @Override
    public Project getProject(String id) throws Exception {
        int status = this.executeRequest("/rest/product/" + id);
        if (status == 200 || status == 201) {
            Project project = new Project();
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("products");
            if (jsonArray.length() == 1) {
                JSONObject projectObject = jsonArray.getJSONObject(0);
                project.setId(projectObject.getInt("id"));
                project.setTitle(projectObject.getString("name"));
                project.setDescription(projectObject.getString("description"));
                project.setEnabled(projectObject.getBoolean("is_active"));
                return project;
            }
        }
        return null;
    }

    @Override
    public String insertOrUpdateProject(Project project) throws Exception {
        String url, method;
        if (project.getId() == 0) {
            url = "/rest/product";
            method = "POST";
        } else {
            url = "/rest/product/" + project.getId();
            method = "PUT";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("version", "unspecified");
        jsonObject.put("is_open", project.isEnabled());
        jsonObject.put("has_unconfirmed", false);
        int status = this.executeRequest(url, jsonObject.toString(), method);

        if (status == 200 || status == 201) {
            if (project.getId() == 0) {
                JSONObject object = new JSONObject(this.getCurrentMessage());
                return String.valueOf(object.getInt("id"));
            } else {
                return String.valueOf(project.getId());
            }
        }

        return null;
    }

    @Override
    public void deleteProject(String id) throws Exception {
        this.deleteRequest("/rest/product/" + id);
    }
}
