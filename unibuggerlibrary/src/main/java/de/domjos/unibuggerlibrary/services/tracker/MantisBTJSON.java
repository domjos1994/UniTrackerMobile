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

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.services.tracker.MantisBTSpecific.SubProject;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class MantisBTJSON extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;

    public MantisBTJSON(Authentication authentication) {
        super(authentication, "Authorization: " + authentication.getAPIKey());
        this.authentication = authentication;
    }

    @Override
    public String getTrackerVersion() throws Exception {
        int state = this.executeRequest("/api/rest/internal");
        String content = this.getCurrentMessage();
        return null;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        this.getTrackerVersion();
        int status = this.executeRequest("/api/rest/projects");
        if (status == 201 || status == 200) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("projects");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                projects.add(this.jsonToProject(jsonArray.getJSONObject(i), false));
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        return this.getProject(id, false);
    }

    private Project<Long> getProject(Long id, boolean version) throws Exception {
        int status = this.executeRequest("/api/rest/projects/" + id);
        if (status == 201 || status == 200) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("projects");
            return this.jsonToProject(jsonArray.getJSONObject(0), version);
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

        List<Version<Long>> versions = project.getVersions();
        if (versions != null) {
            if (!versions.isEmpty()) {
                JSONArray array = new JSONArray();
                for (Version<Long> version : versions) {
                    JSONObject object = new JSONObject();
                    object.put("id", version.getId());
                    object.put("name", version.getTitle());
                    object.put("description", version.getDescription());
                    object.put("released", version.isReleasedVersion());
                    object.put("obsolete", version.isDeprecatedVersion());
                    Date date = new Date();
                    date.setTime(version.getReleasedVersionAt());
                    object.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN).format(date));
                    array.put(object);
                }
                jsonObject.put("versions", array);
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
    public List<Version<Long>> getVersions(Long pid, String filter) throws Exception {
        Project<Long> project = this.getProject(pid, true);
        if (project == null) {
            return new LinkedList<>();
        } else {
            return project.getVersions();
        }
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
        if (version.getId() != null) {
            Project<Long> tmp = this.getProject(pid, true);
            if (tmp != null) {
                for (int i = 0; i <= tmp.getVersions().size() - 1; i++) {
                    if (tmp.getVersions().get(i).getId().equals(version.getId())) {
                        tmp.getVersions().set(i, version);
                        break;
                    }
                }
                this.insertOrUpdateProject(tmp);
                return version.getId();
            }
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", version.getTitle());
            jsonObject.put("released", version.isReleasedVersion());
            jsonObject.put("obsolete", version.isDeprecatedVersion());
            Date date = new Date();
            date.setTime(version.getReleasedVersionAt());
            jsonObject.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN).format(date));
            int state = this.executeRequest("/api/rest/projects/" + pid + "/versions", jsonObject.toString(), "POST");
            if (state == 200 || state == 201) {
                String msg = this.getCurrentMessage();
                Log.v("t", msg);
            }
        }
        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {

    }

    @Override
    public List<Issue<Long>> getIssues(Long pid) throws Exception {
        return null;
    }

    @Override
    public Issue<Long> getIssue(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateIssue(Long pid, Issue<Long> issue) throws Exception {
        return null;
    }

    @Override
    public void deleteIssue(Long id) throws Exception {

    }

    private Project<Long> jsonToProject(JSONObject projectObject, boolean versions) throws Exception {
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
        if (versions) {
            project.setVersions(new LinkedList<>());
            if (projectObject.has("versions")) {
                JSONArray array = projectObject.getJSONArray("versions");
                for (int i = 0; i <= array.length() - 1; i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    Version<Long> version = new Version<>();
                    version.setId(jsonObject.getLong("id"));
                    version.setTitle(jsonObject.getString("name"));
                    version.setDescription(jsonObject.getString("description"));
                    version.setDeprecatedVersion(jsonObject.getBoolean("obsolete"));
                    version.setReleasedVersion(jsonObject.getBoolean("released"));
                    version.setReleasedVersionAt(Converter.convertStringToDate(jsonObject.getString("timestamp"), "yyyy-MM-dd'T'HH:mm:dd").getTime());
                    project.getVersions().add(version);
                }
            }
        }

        JSONObject viewState = projectObject.getJSONObject("view_state");
        project.setPrivateProject(viewState.getInt("id") != 10);
        return project;
    }
}
