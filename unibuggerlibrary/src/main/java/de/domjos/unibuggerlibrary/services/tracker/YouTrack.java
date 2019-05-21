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

import java.util.Date;
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

public final class YouTrack extends JSONEngine implements IBugService<String> {
    private final static String PROJECT_FIELDS = "shortName,description,name,archived,id,leader,iconUrl";
    private final static String VERSION_FIELDS = "name,values(id,name,color(id,background,foreground),description)";
    private final static String ISSUE_FIELDS = "id,summary,description,tags,created,updated,customFields($type,id,projectCustomField($type,id,field($type,id,name)),value($type,avatarUrl,buildLink,color(id),fullName,id,isResolved,localizedName,login,minutes,name,presentation,text))";

    public YouTrack(Authentication authentication) {
        super(authentication, "Authorization: Bearer " + authentication.getAPIKey());
    }

    @Override
    public String getTrackerVersion() throws Exception {
        int status = this.executeRequest("/rest/workflow/version");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return String.format("v %s build %s", jsonObject.getString("version"), jsonObject.getString("build"));
        }
        return null;
    }

    @Override
    public List<Project<String>> getProjects() throws Exception {
        List<Project<String>> projects = new LinkedList<>();
        this.getTrackerVersion();
        int status = this.executeRequest("/api/admin/projects?fields=" + YouTrack.PROJECT_FIELDS);
        if (status == 201 || status == 200) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                projects.add(this.jsonObjectToProject(jsonObject));
            }
        }
        return projects;
    }

    @Override
    public Project<String> getProject(String id) throws Exception {
        int status = this.executeRequest("/api/admin/projects/" + id + "?fields=" + YouTrack.PROJECT_FIELDS);
        if (status == 201 || status == 200) {
            JSONObject projectObject = new JSONObject(this.getCurrentMessage());
            return this.jsonObjectToProject(projectObject);
        }
        return null;
    }

    @Override
    public String insertOrUpdateProject(Project<String> project) throws Exception {
        String url, method;
        if (project.getId() != null) {
            url = "/api/admin/projects/" + project.getId() + "?fields=" + YouTrack.PROJECT_FIELDS;
            method = "POST";
        } else {
            url = "/api/admin/projects?fields=" + YouTrack.PROJECT_FIELDS;
            method = "POST";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shortName", project.getAlias());
        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("archived", !project.isEnabled());
        jsonObject.put("iconUrl", project.getIconUrl());

        int userStatus = this.executeRequest("/api/admin/users/me?fields=id,login,name,email");
        if (userStatus == 200 || userStatus == 201) {
            jsonObject.put("leader", new JSONObject(this.getCurrentMessage()));
        }

        int status = this.executeRequest(url, jsonObject.toString(), method);
        if (status == 200 || status == 201) {
            if (project.getId() != null) {
                return project.getId();
            } else {
                JSONObject obj = new JSONObject(this.getCurrentMessage());
                return obj.getString("id");
            }
        } else {
            return "";
        }
    }

    @Override
    public void deleteProject(String id) throws Exception {
        this.deleteRequest("/api/admin/projects/" + id);
    }

    @Override
    public List<Version<String>> getVersions(String pid, String filter) throws Exception {
        List<Version<String>> versions = new LinkedList<>();
        int status = this.executeRequest("/api/admin/customFieldSettings/bundles/version?fields=" + YouTrack.VERSION_FIELDS);

        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());

            String name = "";
            Project<String> project = this.getProject(pid);
            if (project != null) {
                name = project.getTitle();
            }

            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String bundle = jsonObject.getString("name");
                if (bundle.contains(": ")) {
                    bundle = bundle.split(": ")[0].trim();
                }
                if (bundle.equals(name)) {
                    if (jsonObject.has("values")) {
                        JSONArray valueArray = jsonObject.getJSONArray("values");
                        if (valueArray.length() != 0) {
                            for (int j = 0; j <= valueArray.length() - 1; j++) {
                                JSONObject versionObject = valueArray.getJSONObject(j);
                                Version<String> version = new Version<>();
                                version.setId(versionObject.getString("id"));
                                version.setTitle(versionObject.getString("name"));
                                version.setDescription(versionObject.getString("description"));
                                versions.add(version);
                            }
                        }
                    }
                }
            }
        }

        return versions;
    }

    @Override
    public String insertOrUpdateVersion(String pid, Version<String> version) throws Exception {
        Project<String> project = this.getProject(pid);
        if (project != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", project.getTitle() + ": Versions");
            jsonObject.put("$type", "VersionBundle");
            JSONArray jsonArray = new JSONArray();
            JSONObject versionObject = new JSONObject();
            versionObject.put("name", version.getTitle());
            versionObject.put("description", version.getDescription());
            jsonArray.put(versionObject);
            jsonObject.put("values", jsonArray);

            String url;
            if (version.getId() == null) {
                url = "/api/admin/customFieldSettings/bundles/version?fields=id,name,fieldType(presentation,id),values(id,name,description,$type)";
            } else {
                url = "/api/admin/customFieldSettings/bundles/version/" + version.getId() + "?fields=id,name,fieldType(presentation,id),values(id,name,description,$type)";
            }
            int status = this.executeRequest(url, jsonObject.toString(), "POST");
            if (status == 200 || status == 201) {

            }

        }
        return null;
    }

    @Override
    public void deleteVersion(String id) throws Exception {
        this.deleteRequest("/api/admin/customFieldSettings/bundles/version/" + id);
    }

    @Override
    public List<Issue<String>> getIssues(String pid) throws Exception {
        List<Issue<String>> issues = new LinkedList<>();
        Project<String> project = this.getProject(pid);
        if (project != null) {
            int status = this.executeRequest("/api/issues?query=project:%20" + project.getTitle().replace(" ", "%20") + "&fields=id,summary,description");
            if (status == 200 || status == 201) {
                JSONArray response = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= response.length() - 1; i++) {
                    JSONObject jsonObject = response.getJSONObject(i);
                    Issue<String> issue = new Issue<>();
                    issue.setId(jsonObject.getString("id"));
                    issue.setDescription(jsonObject.getString("description"));
                    issue.setTitle(jsonObject.getString("summary"));
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    @Override
    public Issue<String> getIssue(String id) throws Exception {
        Issue<String> issue = new Issue<>();
        int status = this.executeRequest("/api/issues/" + id + "?fields=" + YouTrack.ISSUE_FIELDS);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            issue.setId(jsonObject.getString("id"));
            issue.setTitle(jsonObject.getString("summary"));
            issue.setDescription(jsonObject.getString("description"));

            if (jsonObject.has("created")) {
                long created = jsonObject.getLong("created");
                if (created != 0) {
                    Date date = new Date();
                    date.setTime(created);
                    issue.setSubmitDate(date);
                }
            }
            if (jsonObject.has("updated")) {
                long updated = jsonObject.getLong("updated");
                if (updated != 0) {
                    Date date = new Date();
                    date.setTime(updated);
                    issue.setLastUpdated(date);
                }
            }

            JSONArray customFieldArray = jsonObject.getJSONArray("customFields");
            for (int i = 0; i <= customFieldArray.length() - 1; i++) {
                JSONObject customFieldObject = customFieldArray.getJSONObject(i);
                JSONObject fieldDescription = customFieldObject.getJSONObject("projectCustomField");

                if (customFieldObject.has("value")) {
                    if (!customFieldObject.isNull("value")) {
                        if (customFieldObject.get("value") instanceof JSONObject) {
                            JSONObject valueObject = customFieldObject.getJSONObject("value");

                            if (fieldDescription.has("name")) {
                                String name = fieldDescription.getString("name");
                                switch (name) {
                                    case "Priority":

                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return issue;
    }

    @Override
    public String insertOrUpdateIssue(String pid, Issue<String> issue) throws Exception {
        return null;
    }

    @Override
    public void deleteIssue(String id) throws Exception {

    }

    @Override
    public List<String> getCategories(String pid) throws Exception {
        List<String> categories = new LinkedList<>();

        return categories;
    }

    @Override
    public List<User<String>> getUsers(String pid) throws Exception {
        List<User<String>> users = new LinkedList<>();

        return users;
    }

    @Override
    public List<Tag<String>> getTags() throws Exception {
        List<Tag<String>> tags = new LinkedList<>();

        return tags;
    }

    private Project<String> jsonObjectToProject(JSONObject jsonObject) throws Exception {
        Project<String> project = new Project<>();
        project.setId(jsonObject.getString("id"));
        project.setTitle(jsonObject.getString("name"));
        project.setDescription(jsonObject.getString("description"));
        project.setAlias(jsonObject.getString("shortName"));
        if (jsonObject.has("archived")) {
            project.setEnabled(!jsonObject.getBoolean("archived"));
        }
        project.setIconUrl(jsonObject.getString("iconUrl"));
        return project;
    }
}
