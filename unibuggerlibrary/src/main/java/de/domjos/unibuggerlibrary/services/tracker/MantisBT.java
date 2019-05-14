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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.services.tracker.MantisBTSpecific.SubProject;

public final class MantisBT extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;

    public MantisBT(Authentication authentication) {
        super(authentication, "Authorization: " + authentication.getAPIKey());
        this.authentication = authentication;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/api/rest/projects");
        if (status == 201 || status == 200) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("projects");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                projects.add(this.jsonToProject(jsonArray.getJSONObject(i)));
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        int status = this.executeRequest("/api/rest/projects/" + id);
        if (status == 201 || status == 200) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("projects");
            return this.jsonToProject(jsonArray.getJSONObject(0));
        }
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        String method;
        String url;
        if (project.getId() != 0) {
            method = "PATCH";
            url = "/api/rest/projects/" + project.getId();
        } else {
            method = "POST";
            url = "/api/rest/projects/";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", project.getId() == 0 ? 1 : project.getId());
        jsonObject.put("name", project.getTitle());
        if (project.getStatusID() != 0 && !project.getStatus().equals("")) {
            JSONObject statusObject = new JSONObject();
            statusObject.put("id", project.getStatusID());
            statusObject.put("name", project.getStatus().toLowerCase());
            statusObject.put("label", project.getStatus().toLowerCase());
            jsonObject.put("status", statusObject);
        }

        jsonObject.put("description", project.getDescription());
        jsonObject.put("enabled", project.isEnabled());
        jsonObject.put("file_path", "/tmp/");
        JSONObject privateProject = new JSONObject();
        if (!project.isPrivateProject()) {
            privateProject.put("id", 10);
            privateProject.put("name", "public");
            privateProject.put("label", "public");
        } else {
            privateProject.put("id", 50);
            privateProject.put("name", "private");
            privateProject.put("label", "private");
        }
        jsonObject.put("view_state", privateProject);


        List<Project<Long>> oldSubs = new LinkedList<>();
        if (project.getId() != 0) {
            Project<Long> tmp = this.getProject(project.getId());
            if (tmp != null) {
                oldSubs = tmp.getSubProjects();
            }
        }

        int status = this.executeRequest(url, jsonObject.toString(), method);
        if (status == 201 || status == 200) {
            project.setId(Long.parseLong(new JSONObject(this.getCurrentMessage()).getJSONObject("project").getString("id")));

            if (project.getId() != 0) {
                SubProject subProject = new SubProject(this.authentication);

                for (Project sub : oldSubs) {
                    subProject.deleteSubProject(project, sub);
                }
                for (Project sub : project.getSubProjects()) {
                    subProject.addSubProject(project, sub);
                }
            }

            return project.getId();
        }
        return 0L;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/api/rest/projects/" + id);
    }

    @Override
    public List<Version<Long>> getVersions(Long pid) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        return null;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {

    }

    private Project<Long> jsonToProject(JSONObject projectObject) throws Exception {
        Project<Long> project = new Project<>();
        project.setId((long) projectObject.getInt("id"));
        project.setTitle(projectObject.getString("name"));
        project.setDescription(projectObject.getString("description"));
        project.setEnabled(projectObject.getBoolean("enabled"));
        if (projectObject.has("subProjects")) {
            JSONArray array = projectObject.getJSONArray("subProjects");
            List<Project<Long>> projects = new LinkedList<>();
            for (int i = 0; i <= array.length() - 1; i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                projects.add(this.getProject((long) jsonObject.getInt("id")));
            }
            project.setSubProjects(projects);
        }

        JSONObject viewState = projectObject.getJSONObject("view_state");
        project.setPrivateProject(viewState.getInt("id") != 10);
        return project;
    }
}
