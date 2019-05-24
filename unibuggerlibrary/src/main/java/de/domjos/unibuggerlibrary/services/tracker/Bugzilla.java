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
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;

public final class Bugzilla extends JSONEngine implements IBugService<Long> {

    public Bugzilla(Authentication authentication) {
        super(authentication, "X-BUGZILLA-API-KEY: " + authentication.getAPIKey());
    }

    @Override
    public boolean testConnection() throws Exception {
        return false;
    }

    @Override
    public String getTrackerVersion() throws Exception {
        int status = this.executeRequest("/rest/version");
        if (status == 200 || status == 201) {
            return new JSONObject(this.getCurrentMessage()).getString("version");
        }
        return null;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/rest/product_selectable");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("ids");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                projects.add(this.getProject((long) jsonArray.getInt(i)));
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        int status = this.executeRequest("/rest/product/" + id);
        if (status == 200 || status == 201) {
            Project<Long> project = new Project<>();
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("products");
            if (jsonArray.length() == 1) {
                JSONObject projectObject = jsonArray.getJSONObject(0);
                project.setId((long) projectObject.getInt("id"));
                project.setTitle(projectObject.getString("name"));
                project.setDescription(projectObject.getString("description"));
                project.setEnabled(projectObject.getBoolean("is_active"));

                if (projectObject.has("version")) {
                    project.setDefaultVersion(projectObject.getString("version"));
                }

                if (projectObject.has("versions")) {
                    JSONArray versionArray = projectObject.getJSONArray("versions");
                    for (int i = 0; i <= versionArray.length() - 1; i++) {
                        JSONObject versionObject = versionArray.getJSONObject(i);
                        Version<Long> version = new Version<>();
                        version.setId(versionObject.getLong("id"));
                        version.setTitle(versionObject.getString("name"));
                        version.setReleasedVersion(!versionObject.getBoolean("is_active"));
                        project.getVersions().add(version);
                    }
                }

                return project;
            }
        }
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        String url, method;
        if (project.getId() == 0L) {
            url = "/rest/product";
            method = "POST";
        } else {
            url = "/rest/product/" + project.getId();
            method = "PUT";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("version", project.getDefaultVersion());
        jsonObject.put("is_open", project.isEnabled());
        jsonObject.put("has_unconfirmed", false);
        int status = this.executeRequest(url, jsonObject.toString(), method);

        if (status == 200 || status == 201) {
            if (project.getId() == 0) {
                JSONObject object = new JSONObject(this.getCurrentMessage());
                return (long) object.getInt("id");
            } else {
                return project.getId();
            }
        }

        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/rest/product/" + id);
    }

    @Override
    public List<Version<Long>> getVersions(Long pid, String filter) throws Exception {
        Project<Long> project = this.getProject(pid);
        if (project != null) {
            return project.getVersions();
        }
        return null;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
//        Long id = null;
//        Project<Long> project = this.getProject(pid);
//        if(project!=null) {
//            if(version.getId()==null) {
//                project.getVersions().add(version);
//            } else {
//                for (int i = 0; i <= project.getVersions().size() - 1; i++) {
//                    if(version.getId().equals(project.getVersions().get(i).getId())) {
//                        project.getVersions().set(i, version);
//                        break;
//                    }
//                }
//            }
//            project = this.getProject(this.insertOrUpdateProject(project));
//            if(project!=null) {
//                for (int i = 0; i <= project.getVersions().size() - 1; i++) {
//                    if(version.getTitle().equals(project.getVersions().get(i).getTitle())) {
//                        id = project.getVersions().get(i).getId();
//                        break;
//                    }
//                }
//            }
//        }
//
//
//        return id;
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

    @Override
    public List<String> getCategories(Long pid) throws Exception {
        return null;
    }

    @Override
    public List<User<Long>> getUsers(Long pid) throws Exception {
        return null;
    }

    @Override
    public List<Tag<Long>> getTags() throws Exception {
        return null;
    }
}
