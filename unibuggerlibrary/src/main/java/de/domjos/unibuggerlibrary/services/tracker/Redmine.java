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
    public List<Version<Long>> getVersions(Long pid, String filter) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        int status = this.executeRequest("/projects/" + pid + "/versions.json");

        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray array = jsonObject.getJSONArray("versions");
            for (int i = 0; i <= jsonObject.getInt("total_count") - 1; i++) {
                Version<Long> version = new Version<>();
                JSONObject versionObject = array.getJSONObject(i);
                version.setId(versionObject.getLong("id"));
                version.setTitle(versionObject.getString("name"));
                version.setDescription(versionObject.getString("description"));

                if (versionObject.has("due_date")) {
                    Date dt = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).parse(versionObject.getString("due_date"));
                    version.setReleasedVersionAt(dt.getTime());
                }

                String state = versionObject.getString("status");
                if (state.equals("locked")) {
                    version.setDeprecatedVersion(true);
                    version.setReleasedVersion(true);
                } else {
                    version.setDeprecatedVersion(false);
                    version.setReleasedVersion(!state.equals("open"));
                }
                versions.add(version);
            }
        }

        return versions;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
        JSONObject object = new JSONObject();
        JSONObject versionObject = new JSONObject();
        versionObject.put("name", version.getTitle());
        versionObject.put("description", version.getDescription());
        if (version.isDeprecatedVersion()) {
            versionObject.put("status", "locked");
        } else {
            versionObject.put("status", version.isReleasedVersion() ? "closed" : "open");
        }

        if (version.getReleasedVersionAt() != 0) {
            Date date = new Date();
            date.setTime(version.getReleasedVersionAt());
            versionObject.put("due_date", new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).format(date));
        }
        JSONObject projectObject = new JSONObject();
        projectObject.put("id", pid);
        Project<Long> project = this.getProject(pid);
        if (project != null) {
            projectObject.put("name", project.getTitle());
        }
        versionObject.put("project", projectObject);

        int status;
        if (version.getId() == null) {
            object.put("version", versionObject);
            status = this.executeRequest("/projects/" + pid + "/versions.json", object.toString(), "POST");
        } else {
            versionObject.put("id", version.getId());
            object.put("version", versionObject);
            status = this.executeRequest("/versions/" + version.getId() + ".json", object.toString(), "PUT");
        }

        if (status == 200 || status == 201) {
            String content = this.getCurrentMessage();

            if (version.getId() == null) {
                JSONObject result = new JSONObject(content);
                if (result.has("version")) {
                    JSONObject resultProject = result.getJSONObject("version");
                    return (long) resultProject.getInt("id");
                }
            } else {
                return version.getId();
            }
            return null;
        }

        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {
        this.deleteRequest("/versions/" + id + ".json");
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
