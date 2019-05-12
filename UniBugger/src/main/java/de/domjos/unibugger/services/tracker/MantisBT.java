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

public final class MantisBT extends JSONEngine implements IBugService {

    public MantisBT(Authentication authentication) {
        super(authentication, "Authorization: " + authentication.getAPIKey());
    }

    @Override
    public List<Project> getProjects() throws Exception {
        List<Project> projects = new LinkedList<>();
        int status = this.executeRequest("/api/rest/projects");
        if(status == 201 || status == 200) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("projects");
            for(int i = 0; i<=jsonArray.length()-1; i++) {
                projects.add(this.jsonToProject(jsonArray.getJSONObject(i)));
            }
        }
        return projects;
    }

    @Override
    public Project getProject(String id) throws Exception {
        int status = this.executeRequest("/api/rest/projects/" + id);
        if(status == 201 || status == 200) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("projects");
            return this.jsonToProject(jsonArray.getJSONObject(0));
        }
        return null;
    }

    @Override
    public String insertOrUpdateProject(Project project) throws Exception {
        String method;
        String url;
        if(project.getId()!=0) {
            method = "PATCH";
            url = "/api/rest/projects/" + project.getId();
        } else {
            method = "POST";
            url = "/api/rest/projects/";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", project.getId()==0?1:project.getId());
        jsonObject.put("name", project.getTitle());
        if(project.getId()==0) {
            JSONObject statusObject = new JSONObject();
            statusObject.put("id", "10");
            statusObject.put("name", "development");
            statusObject.put("label", "development");
            jsonObject.put("status", statusObject);
        }

        jsonObject.put("description", project.getDescription());
        jsonObject.put("enabled", project.isEnabled());
        jsonObject.put("file_path", "/tmp/");
        if(!project.isPrivateProject()) {
            JSONObject privateProject = new JSONObject();
            privateProject.put("id", 10);
            privateProject.put("name", "public");
            privateProject.put("label", "public");
            jsonObject.put("view_state", privateProject);
        }

        int status = this.executeRequest(url, jsonObject.toString(), method);
        if(status == 201 || status == 200) {
            return new JSONObject(this.getCurrentMessage()).getJSONObject("project").getString("id");
        }
        return "";
    }

    @Override
    public void deleteProject(String id) throws Exception {
        this.deleteRequest("/api/rest/projects/" + id);
    }

    private Project jsonToProject(JSONObject projectObject) throws Exception {
        Project project = new Project();
        project.setId(projectObject.getInt("id"));
        project.setTitle(projectObject.getString("name"));
        project.setDescription(projectObject.getString("description"));
        project.setEnabled(projectObject.getBoolean("enabled"));
        JSONObject viewState = projectObject.getJSONObject("view_state");
        project.setPrivateProject(viewState.getInt("id")!=10);
        return project;
    }
}
