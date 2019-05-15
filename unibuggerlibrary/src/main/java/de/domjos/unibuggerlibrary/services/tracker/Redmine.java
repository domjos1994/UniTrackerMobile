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
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Redmine extends JSONEngine implements IBugService<Long> {

    public Redmine(Authentication authentication) {
        super(authentication);
    }

    @Override
    public String getTrackerVersion() throws Exception {
        return null;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/projects.json");

        if (status == 201 || status == 200) {
            String json = this.getCurrentMessage();
            if (json != null) {
                if (!json.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(json);
                    int count = jsonObject.getInt("total_count");
                    if (count != 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("projects");
                        for (int i = 0; i <= count - 1; i++) {
                            try {
                                JSONObject projectObject = jsonArray.getJSONObject(i);
                                projects.add(this.jsonObjectToProject(projectObject));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }


        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        int status = this.executeRequest("/projects/" + id + ".json");

        if (status == 201 || status == 200) {
            String json = this.getCurrentMessage();
            if (json != null) {
                if (!json.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject projectObject = jsonObject.getJSONObject("project");
                    return this.jsonObjectToProject(projectObject);
                }
            }
        }

        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        if (project != null) {
            JSONObject jsonObject = new JSONObject();
            JSONObject projectObject = new JSONObject();
            projectObject.put("name", project.getTitle());
            projectObject.put("identifier", project.getAlias());
            projectObject.put("description", project.getDescription());
            projectObject.put("is_public", !project.isPrivateProject());
            projectObject.put("homepage", project.getWebsite());
            jsonObject.put("project", projectObject);

            if (project.getId() == null) {
                int status = this.executeRequest("/projects.json", jsonObject.toString(), "POST");

                if (status == 201 || status == 200) {
                    String content = this.getCurrentMessage();
                    JSONObject result = new JSONObject(content);
                    if (result.has("project")) {
                        JSONObject resultProject = result.getJSONObject("project");
                        return (long) resultProject.getInt("id");
                    }
                }
            } else {
                int status = this.executeRequest("/projects/" + project.getId() + ".json", jsonObject.toString(), "PUT");

                if (status == 201 || status == 200) {
                    return project.getId();
                }
            }
        }
        return 0L;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/projects/" + id + ".json");
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

    private Project<Long> jsonObjectToProject(JSONObject obj) throws Exception {
        Project<Long> project = new Project<>();
        project.setId((long) obj.getInt("id"));
        project.setTitle(obj.getString("name"));
        project.setAlias(obj.getString("identifier"));
        project.setDescription(obj.getString("description"));
        if (obj.has("homepage")) {
            project.setWebsite(obj.getString("homepage"));
        }
        project.setPrivateProject(!obj.getBoolean("is_public"));
        project.setCreatedAt(Converter.convertStringToDate(obj.getString("created_on"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").getTime());
        project.setUpdatedAt(Converter.convertStringToDate(obj.getString("updated_on"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").getTime());
        return project;
    }
}
