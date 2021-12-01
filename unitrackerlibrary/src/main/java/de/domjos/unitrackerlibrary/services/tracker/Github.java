/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackerlibrary.services.tracker;

import android.content.Context;

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.model.issues.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.GithubPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.unitrackerlibrary.services.tracker.GithubSpecific.SearchAll;

public final class Github extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;
    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private String username;

    public Github(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;

        if(this.authentication.getHints().containsKey("userName")) {
            this.username = this.authentication.getHints().get("userName");
        } else {
            this.username = this.authentication.getUserName();
        }
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/user");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            this.authentication.getHints().put("userName", jsonObject.getString("login"));
            if(jsonObject.has("avatar_url")) {
                if(!jsonObject.isNull("avatar_url")) {
                    String url = jsonObject.getString("avatar_url");
                    this.authentication.setCover(ConvertHelper.convertStringToByteArray(url));
                }
            }
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
        return this.getProjects(this.username);
    }

    public List<Project<Long>> getProjects(String name) throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/users/" + name + "/repos");

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
                    Date dt = ConvertHelper.convertStringToDate(jsonObject.getString("created_at"), Github.DATE_TIME_FORMAT);
                    if (dt != null) {
                        project.setCreatedAt(dt.getTime());
                    }
                }
                if (jsonObject.has("updated_at")) {
                    Date dt = ConvertHelper.convertStringToDate(jsonObject.getString("updated_at"), Github.DATE_TIME_FORMAT);
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
        projects = this.getProjects(this.getAuthentication().getHints().get(SearchAll.SEARCH));
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
            method = "PATCH";
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
            int status = this.executeRequest("/repos/" + project.getTitle() + "/releases");

            if (status == 200 || status == 201) {
                JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Version<Long> version = new Version<>();
                    version.setTitle(jsonObject.getString("name"));
                    version.setDescription(jsonObject.getString("body"));


                    if(!jsonObject.isNull("published_at")) {
                        String dt = jsonObject.getString("published_at");
                        if(!dt.equals("")) {
                            version.setReleasedVersionAt(ConvertHelper.convertStringToDate(dt, Github.DATE_TIME_FORMAT).getTime());
                        }
                    }
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
                url = "/repos/" + this.username + "/" + project.getAlias() + "/releases";
                method = "POST";
            } else {
                url = "/repos/" + this.username + "/" + project.getAlias() + "/releases/" + version.getId();
                method = "PATCH";
            }

            this.executeRequest(url, jsonObject.toString(), method);
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            this.deleteRequest("/repos/" + project.getTitle() + "/releases/" + id);
        }
    }

    @Override
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) throws Exception {
        if(filter==null) {
            filter = IssueFilter.all;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = "?state=closed";
            } else {
                filterQuery = "?state=open";
            }
        }

        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/repos/" + project.getTitle() + "/issues" + filterQuery);
            if (status == 200 || status == 201) {
                return new JSONArray(this.getCurrentMessage()).length();
            }
        }

        return 0;
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

        String filterQuery;
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = "?state=closed";
            } else {
                filterQuery = "?state=open";
            }
        } else {
            filterQuery = "?state=all";
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

                int state = issueObject.getString("state").equals("open") ? 10 : 20;
                issue.setStatus(state, issueObject.getString("state"));

                issue.setLastUpdated(ConvertHelper.convertStringToDate(issueObject.getString("updated_at"), Github.DATE_TIME_FORMAT));
                issue.setSubmitDate(ConvertHelper.convertStringToDate(issueObject.getString("created_at"), Github.DATE_TIME_FORMAT));
                issue.setHandler(this.getUser(issueObject.getJSONObject("user")));
                issue.getNotes().addAll(this.getNotes(id, project_id));
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
            jsonArray.put(this.username);
            jsonObject.put("assignees", jsonArray);
            if(issue.getId() == null) {
                int status = this.executeRequest("/repos/" + project.getTitle() + "/issues", jsonObject.toString(), "POST");
                if(status == 201) {
                    JSONObject result = new JSONObject(this.getCurrentMessage());
                    issue.setId(result.getLong("id"));
                }
            } else {
                jsonObject.put("state", issue.getStatus().getKey()==10 ? "open" : "closed");
                this.executeRequest("/repos/" + project.getTitle() + "/issues/" + issue.getId(), jsonObject.toString(), "PATCH");
            }
            issue.setId(Long.parseLong(String.valueOf(issue.getId())));

            Issue<Long> oldIssue = this.getIssue(issue.getId(), project_id);
            for(Note<Long> note : oldIssue.getNotes()) {
                boolean exists = false;
                for(Note<Long> newNote : issue.getNotes()) {
                    if(newNote.getId().equals(note.getId())) {
                        exists = true;
                        break;
                    }
                }

                if(!exists) {
                    this.deleteNote(note.getId(), issue.getId(), project_id);
                }
            }

            for(Note<Long> note : issue.getNotes()) {
                boolean exists = false;
                boolean bodyChanged = false;
                for(Note<Long> oldNote : oldIssue.getNotes()) {
                    if(oldNote.getId().equals(note.getId())) {
                        exists = true;
                        bodyChanged = !oldNote.getDescription().equals(note.getDescription());
                        break;
                    }
                }

                if(!exists || bodyChanged) {
                    this.insertOrUpdateNote(note, issue.getId(), project_id);
                }
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) {

    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) throws Exception {
        List<Note<Long>> notes = new LinkedList<>();
        Project<Long> project = this.getProject(project_id);

        if (project != null) {
            int status = this.executeRequest("/repos/" + project.getTitle() + "/issues/" + issue_id + "/comments");
            if (status == 200 || status == 201) {
                JSONArray noteArray = new JSONArray(this.getCurrentMessage());
                for(int i = 0; i<=noteArray.length()-1; i++) {
                    JSONObject noteObject = noteArray.getJSONObject(i);

                    Note<Long> note  = new Note<>();
                    note.setId(noteObject.getLong("id"));
                    note.setDescription(noteObject.getString("body"));

                    String createdAt = noteObject.getString("created_at");
                    if(!createdAt.trim().isEmpty()) {
                        note.setSubmitDate(ConvertHelper.convertStringToDate(createdAt, Github.DATE_TIME_FORMAT));
                    }

                    String updatedAt = noteObject.getString("updated_at");
                    if(!updatedAt.trim().isEmpty()) {
                        note.setSubmitDate(ConvertHelper.convertStringToDate(updatedAt, Github.DATE_TIME_FORMAT));
                    }
                    notes.add(note);
                }
            }
        }
        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        Project<Long> project = this.getProject(project_id);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("body", note.getDescription());

        if(project != null) {
            if(note.getId() == null) {
                this.executeRequest("/repos/" + project.getTitle() + "/issues/" + issue_id + "/comments", jsonObject.toString(), "POST");
            } else {
                this.executeRequest("/repos/" + project.getTitle() + "/issues/comments/" + note.getId(), jsonObject.toString(), "POST");
            }
        }
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {
        Project<Long> project = this.getProject(project_id);

        if(project != null) {
            this.deleteRequest("/repos/" + project.getTitle() + "/issues/comments/" + id);
        }
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {
    }

    @Override
    public void deleteAttachment(Object id, Long issue_id, Long project_id) {
    }

    @Override
    public List<Relationship<Long>> getBugRelations(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<Long> relationship, Long issue_id, Long project_id) {

    }

    @Override
    public void deleteBugRelation(Relationship<Long> relationship, Long issue_id, Long project_id) {

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
    public List<History<Long>> getHistory(Long issue_id, Long project_id) {
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
    public Map<String, String> getEnums(Type type, Context context)  {
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return this.authentication.getTitle();
    }

    private User<Long> getUser(JSONObject jsonObject) throws Exception {
        User<Long> user = new User<>();
        user.setId(jsonObject.getLong("id"));
        user.setTitle(jsonObject.getString("login"));
        return user;
    }

    @Override
    public List<History<Long>> getNews() {
        return new LinkedList<>();
    }
}
