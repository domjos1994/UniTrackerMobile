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

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.History;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.issues.Profile;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.GithubPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Github extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;

    public Github(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;

    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/user");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return jsonObject.has("plan");
        }
        return false;
    }

    @Override
    public String getTrackerVersion() {
        return "v3";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/users/" + this.authentication.getUserName() + "/repos");

        if (status == 200 || status == 201) {
            JSONArray versionArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= versionArray.length() - 1; i++) {
                Project<Long> project = new Project<>();
                JSONObject jsonObject = versionArray.getJSONObject(i);
                project.setId(jsonObject.getLong("id"));
                project.setTitle(jsonObject.getString("full_name"));
                project.setAlias(jsonObject.getString("name"));
                project.setPrivateProject(jsonObject.getBoolean("private"));
                project.setDescription(jsonObject.getString("description"));
                project.setWebsite(jsonObject.getString("homepage"));
                project.setEnabled(!jsonObject.getBoolean("disabled"));

                if (jsonObject.has("created_at")) {
                    Date dt = Converter.convertStringToDate(jsonObject.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'");
                    if (dt != null) {
                        project.setCreatedAt(dt.getTime());
                    }
                }
                if (jsonObject.has("updated_at")) {
                    Date dt = Converter.convertStringToDate(jsonObject.getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'");
                    if (dt != null) {
                        project.setUpdatedAt(dt.getTime());
                    }
                }
                projects.add(project);
            }
        }

        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        List<Project<Long>> projects = this.getProjects();
        for (Project<Long> project : projects) {
            if (project.getId().equals(id)) {
                return project;
            }
        }
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", project.getAlias());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("homepage", project.getWebsite());
        jsonObject.put("private", project.isPrivateProject());
        jsonObject.put("has_issues", true);
        jsonObject.put("has_projects", false);
        jsonObject.put("has_wiki", false);

        String method;
        String url;
        if (project.getId() == null) {
            url = "/user/repos";
            method = "POST";
        } else {
            url = "/repos/" + project.getTitle();
            method = "POST";
        }

        int status = this.executeRequest(url, jsonObject.toString(), method);
        if (status == 200 || status == 201) {
            JSONObject response = new JSONObject(this.getCurrentMessage());
            return response.getLong("id");
        }

        return 0L;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        Project<Long> project = this.getProject(id);
        if (project != null) {
            this.deleteRequest("/repos/" + project.getTitle());
        }
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/repos/" + this.authentication.getUserName() + "/" + project.getAlias() + "/releases");

            if (status == 200 || status == 201) {
                JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Version<Long> version = new Version<>();
                    version.setTitle(jsonObject.getString("name"));
                    version.setDescription(jsonObject.getString("body"));
                    version.setReleasedVersionAt(Converter.convertStringToDate(jsonObject.getString("published_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime());
                    version.setId(jsonObject.getLong("id"));
                    version.setReleasedVersion(jsonObject.getBoolean("prerelease"));
                    versions.add(version);
                }
            }
        }

        return versions;
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tag_name", version.getTitle());
        jsonObject.put("name", version.getTitle());
        jsonObject.put("body", version.getDescription());
        jsonObject.put("draft", true);
        jsonObject.put("prerelease", version.isReleasedVersion());

        String method;
        String url;
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            if (version.getId() == null) {
                url = "/repos/" + this.authentication.getUserName() + "/" + project.getAlias() + "/releases";
                method = "POST";
            } else {
                url = "/repos/" + this.authentication.getUserName() + "/" + project.getAlias() + "/releases/" + version.getId();
                method = "PATCH";
            }

            this.executeRequest(url, jsonObject.toString(), method);
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            this.deleteRequest("/repos/" + this.authentication.getUserName() + "/" + project.getTitle() + "/releases/" + id);
        }
    }

    @Override
    public List<Issue<Long>> getIssues(Long pid) throws Exception {
        return this.getIssues(pid, 1, -1, IssueFilter.all);
    }

    @Override
    public List<Issue<Long>> getIssues(Long pid, IssueFilter filter) throws Exception {
        return this.getIssues(pid, 1, -1, filter);
    }

    @Override
    public List<Issue<Long>> getIssues(Long pid, int page, int numberOfItems) throws Exception {
        return this.getIssues(pid, page, numberOfItems, IssueFilter.all);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems, IssueFilter filter) throws Exception {
        List<Issue<Long>> issues = new LinkedList<>();
        Project<Long> project = this.getProject(project_id);

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = "?state=closed";
            } else {
                filterQuery = "?state=open";
            }
        }

        if (project != null) {
            int status = this.executeRequest("/repos/" + project.getTitle() + "/issues" + filterQuery);
            if (status == 200 || status == 201) {
                JSONArray issueArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= issueArray.length() - 1; i++) {
                    Issue<Long> issue = new Issue<>();
                    JSONObject issueObject = issueArray.getJSONObject(i);
                    issue.setId(issueObject.getLong("number"));
                    issue.setTitle(issueObject.getString("title"));
                    issue.setDescription(issueObject.getString("body"));
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/repos/" + project.getTitle() + "/issues/" + id);
            if (status == 200 || status == 201) {
                JSONObject issueObject = new JSONObject(this.getCurrentMessage());
                issue.setId(issueObject.getLong("number"));
                issue.setTitle(issueObject.getString("title"));
                issue.setDescription(issueObject.getString("body"));
                issue.setLastUpdated(Converter.convertStringToDate(issueObject.getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
                issue.setSubmitDate(Converter.convertStringToDate(issueObject.getString("created_at"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
                issue.setHandler(this.getUser(issueObject.getJSONObject("user")));
            }
        }
        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", issue.getTitle());
            jsonObject.put("body", issue.getDescription());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(this.authentication.getUserName());
            jsonObject.put("assignees", jsonArray);
            this.executeRequest("/repos/" + project.getTitle() + "/issues", jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) {

    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {

    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {

    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) {
    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/user/followers");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                users.add(this.getUser(jsonArray.getJSONObject(i)));
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        int status = this.executeRequest("/user");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return this.getUser(jsonObject);
        }
        return null;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) {
    }

    @Override
    public void deleteUser(Long id, Long project_id) {
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) {
        return null;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> user, Long project_id) {
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) {
    }


    @Override
    public List<String> getCategories(Long project_id) {
        return null;
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) {
        return null;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public List<Profile<Long>> getProfiles() {
        return new LinkedList<>();
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new GithubPermissions(this.authentication);
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public List<String> getEnums(String title) {
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getAuthentication().getTitle();
    }

    private User<Long> getUser(JSONObject jsonObject) throws Exception {
        User<Long> user = new User<>();
        user.setId(jsonObject.getLong("id"));
        user.setTitle(jsonObject.getString("login"));
        return user;
    }
}
