/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggerlibrary.services.tracker;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import de.domjos.unibuggerlibrary.permissions.JiraPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Jira extends JSONEngine implements IBugService<Long> {
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final static String DATE_FORMAT = "yyyy-MM-dd";
    private Authentication authentication;
    private Map<Long, User<Long>> map;

    public Jira(Authentication authentication) {
        super(authentication, "Authorization: Basic " + Base64.encodeToString((authentication.getUserName() + ":" + authentication.getPassword()).getBytes(), Base64.NO_WRAP));
        this.authentication = authentication;
        this.map = new LinkedHashMap<>();
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/rest/api/2/mypermissions");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return jsonObject.has("permissions");
        }
        return false;
    }

    @Override
    public String getTrackerVersion() throws Exception {
        int status = this.executeRequest("/rest/api/2/serverInfo");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return jsonObject.getString("version");
        }
        return null;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/project");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                projects.add(this.getProject(jsonArray.getJSONObject(i).getLong("id")));
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        Project<Long> project = new Project<>();
        int status = this.executeRequest("/rest/api/2/project/" + id);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            project.setId(jsonObject.getLong("id"));
            project.setTitle(jsonObject.getString("name"));
            project.setAlias(jsonObject.getString("key"));
            if (jsonObject.has("url")) {
                project.setWebsite(jsonObject.getString("url"));
            }
            project.setEnabled(jsonObject.getBoolean("archived"));
            if (jsonObject.has("avatar")) {
                JSONObject avatar = jsonObject.getJSONObject("avatar");
                if (avatar.has("48x48")) {
                    project.setIconUrl(avatar.getString("48x48"));
                }
            }
            project.setDescription(jsonObject.getString("description"));
        }
        return project;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", project.getAlias());
        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("url", project.getWebsite());
        jsonObject.put("lead", this.authentication.getUserName());
        jsonObject.put("projectTypeKey", "software");

        int status;
        if (project.getId() != null) {
            status = this.executeRequest("/rest/api/2/project/" + project.getId(), jsonObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/rest/api/2/project", jsonObject.toString(), "POST");
        }

        if (status == 200 || status == 201) {
            JSONObject result = new JSONObject(this.getCurrentMessage());
            return result.getLong("id");
        }

        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/rest/api/2/project/" + id);
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/project/" + project_id + "/versions");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Version<Long> version = new Version<>();
                version.setId(jsonObject.getLong("id"));
                version.setTitle(jsonObject.getString("name"));
                if (jsonObject.has("description")) {
                    version.setDescription(jsonObject.getString("description"));
                }
                version.setReleasedVersion(jsonObject.getBoolean("released"));
                version.setDeprecatedVersion(jsonObject.getBoolean("archived"));
                if (jsonObject.has("releaseDate")) {
                    version.setReleasedVersionAt(Converter.convertStringToDate(jsonObject.getString("releaseDate"), Jira.DATE_FORMAT).getTime());
                }
                versions.add(version);
            }
        }
        return versions;
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", version.getTitle());
        jsonObject.put("description", version.getDescription());
        jsonObject.put("projectId", project_id);
        jsonObject.put("released", version.isReleasedVersion());
        jsonObject.put("archived", version.isDeprecatedVersion());
        if (version.getReleasedVersionAt() != 0) {
            Date dt = new Date();
            dt.setTime(version.getReleasedVersionAt());
            jsonObject.put("releaseDate", new SimpleDateFormat(Jira.DATE_FORMAT, Locale.GERMAN).format(dt));
        }

        if (version.getId() != null) {
            this.executeRequest("/rest/api/2/version/" + version.getId(), jsonObject.toString(), "PUT");
        } else {
            this.executeRequest("/rest/api/2/version", jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        this.deleteRequest("/rest/api/2/version/" + id);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id) throws Exception {
        return this.getIssues(project_id, 1, -1, IssueFilter.all);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, IssueFilter filter) throws Exception {
        return this.getIssues(project_id, 1, -1, filter);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems) throws Exception {
        return this.getIssues(project_id, page, numberOfItems, IssueFilter.all);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems, IssueFilter filter) throws Exception {
        String pagination = "";
        if (numberOfItems != -1) {
            pagination = "&startAt=" + ((numberOfItems * page) - 1) + "&maxResults" + numberOfItems;
        }

        String query = "project=\"" + project_id + "\"";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                query = "(" + query + "%20AND%20status=10002)";
            } else {
                query = "(" + query + "%20AND%20(status=3%20OR%20status=10000))";
            }
        }

        List<Issue<Long>> issues = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/search?jql=" + query + pagination);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("issues");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                Issue<Long> issue = new Issue<>();
                JSONObject issueObject = jsonArray.getJSONObject(i);
                JSONObject fieldsObject = issueObject.getJSONObject("fields");
                issue.setId(issueObject.getLong("id"));
                issue.setTitle(fieldsObject.getString("summary"));
                if (fieldsObject.has("description")) {
                    if (!fieldsObject.isNull("description")) {
                        issue.setDescription(fieldsObject.getString("description"));
                    }
                }
                issues.add(issue);
            }
        }
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();
        int status = this.executeRequest("/rest/api/2/issue/" + id);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject fieldsObject = jsonObject.getJSONObject("fields");
            issue.setId(jsonObject.getLong("id"));
            issue.setTitle(fieldsObject.getString("summary"));
            if (fieldsObject.has("description")) {
                if (!fieldsObject.isNull("description")) {
                    issue.setDescription(fieldsObject.getString("description"));
                }
            }
            if(fieldsObject.has("issuetype")) {
                if(!fieldsObject.isNull("issuetype")) {
                    JSONObject typeObject = fieldsObject.getJSONObject("issuetype");
                    issue.setSeverity(typeObject.getInt("id"), typeObject.getString("name"));
                }
            }
            if (fieldsObject.has("priority")) {
                if (!fieldsObject.isNull("priority")) {
                    JSONObject priorityObject = fieldsObject.getJSONObject("priority");
                    issue.setPriority(priorityObject.getInt("id"), priorityObject.getString("name"));
                }
            }
            if (fieldsObject.has("duedate")) {
                if (!fieldsObject.isNull("duedate")) {
                    issue.setDueDate(Converter.convertStringToDate(fieldsObject.getString("duedate"), Jira.DATE_FORMAT));
                }
            }
            if (fieldsObject.has("status")) {
                if (!fieldsObject.isNull("status")) {
                    JSONObject statusObject = fieldsObject.getJSONObject("status");
                    issue.setStatus(statusObject.getInt("id"), statusObject.getString("name"));
                }
            }
            if (fieldsObject.has("assignee")) {
                if (!fieldsObject.isNull("assignee")) {
                    JSONObject handlerObject = fieldsObject.getJSONObject("assignee");
                    User<Long> user = new User<>();
                    user.setTitle(handlerObject.getString("displayName"));
                    user.setEmail(handlerObject.getString("emailAddress"));
                    user.setRealName(handlerObject.getString("name"));
                    issue.setHandler(user);
                }
            }
            if (fieldsObject.has("labels")) {
                if (!fieldsObject.isNull("labels")) {
                    JSONArray jsonArray = fieldsObject.getJSONArray("labels");
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i <= jsonArray.length() - 1; i++) {
                        builder.append(jsonArray.getString(i));
                        builder.append(",");
                    }
                    issue.setTags(builder.toString());
                }
            }
            if (fieldsObject.has("versions")) {
                if (!fieldsObject.isNull("versions")) {
                    JSONArray jsonArray = fieldsObject.getJSONArray("versions");
                    for (int i = 0; i <= jsonArray.length() - 1; i++) {
                        JSONObject versionObject = jsonArray.getJSONObject(i);
                        issue.setVersion(this.getVersions("", versionObject.getLong("id")).get(0).getTitle());
                    }
                }
            }
            if (fieldsObject.has("fixVersions")) {
                if (!fieldsObject.isNull("fixVersions")) {
                    JSONArray jsonArray = fieldsObject.getJSONArray("fixVersions");
                    for (int i = 0; i <= jsonArray.length() - 1; i++) {
                        JSONObject versionObject = jsonArray.getJSONObject(i);
                        issue.setFixedInVersion(this.getVersions("", versionObject.getLong("id")).get(0).getTitle());
                    }
                }
            }

            List<CustomField<Long>> customFields = this.getCustomFields(project_id);
            for (CustomField<Long> customField : customFields) {
                if (fieldsObject.has(customField.getHints().get("id"))) {
                    if (!fieldsObject.isNull(customField.getHints().get("id"))) {
                        try {
                            JSONObject customFieldObject = fieldsObject.getJSONObject(customField.getHints().get("id"));
                            issue.getCustomFields().put(customField, customFieldObject.getString("value"));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            issue.setSubmitDate(Converter.convertStringToDate(fieldsObject.getString("created"), Jira.DATE_TIME_FORMAT));
            issue.setLastUpdated(Converter.convertStringToDate(fieldsObject.getString("updated"), Jira.DATE_TIME_FORMAT));
            issue.getNotes().addAll(this.getNotes(issue.getId(), project_id));
            issue.getAttachments().addAll(this.getAttachments(issue.getId(), project_id));
        }
        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject fieldsObject = new JSONObject();

        JSONObject projectObject = new JSONObject();
        projectObject.put("id", String.valueOf(project_id));
        fieldsObject.put("project", projectObject);

        fieldsObject.put("summary", issue.getTitle());
        fieldsObject.put("description", issue.getDescription());
        if (issue.getDueDate() != null) {
            fieldsObject.put("duedate", new SimpleDateFormat(Jira.DATE_FORMAT, Locale.GERMAN).format(issue.getDueDate()));
        }

        if (issue.getHandler() != null) {
            JSONObject userObject = new JSONObject();
            userObject.put("id", issue.getHandler().getId());
            userObject.put("name", issue.getHandler().getTitle());
            fieldsObject.put("assignee", userObject);
        }

        JSONObject priorityObject = new JSONObject();
        priorityObject.put("id", String.valueOf(issue.getPriority().getKey()));
        priorityObject.put("name", issue.getPriority().getValue());
        fieldsObject.put("priority", priorityObject);

        JSONObject typeObject = new JSONObject();
        typeObject.put("id", String.valueOf(issue.getSeverity().getKey()));
        typeObject.put("name", issue.getSeverity().getValue());
        fieldsObject.put("issuetype", typeObject);

        if (!issue.getTags().trim().isEmpty()) {
            JSONArray tagsArray = new JSONArray();
            for (String tag : issue.getTags().split(",")) {
                tagsArray.put(tag.trim());
            }
            fieldsObject.put("labels", tagsArray);
        }

        List<Version<Long>> versions = this.getVersions("", project_id);
        if (!issue.getVersion().isEmpty()) {
            JSONArray versionArray = new JSONArray();
            for (Version<Long> version : versions) {
                if (version.getTitle().equals(issue.getVersion().trim())) {
                    JSONObject versionObject = new JSONObject();
                    versionObject.put("id", String.valueOf(version.getId()));
                    versionArray.put(versionObject);
                    break;
                }
            }

            if(issue.getId()==null && issue.getSeverity().getKey()==10006) {
                fieldsObject.put("versions", versionArray);
            }
        }

        if (!issue.getFixedInVersion().isEmpty()) {
            JSONArray versionArray = new JSONArray();
            for (Version<Long> version : versions) {
                if (version.getTitle().equals(issue.getFixedInVersion().trim())) {
                    JSONObject versionObject = new JSONObject();
                    versionObject.put("id", String.valueOf(version.getId()));
                    versionArray.put(versionObject);
                    break;
                }
            }
            fieldsObject.put("fixVersions", versionArray);
        }

        for (Map.Entry<CustomField<Long>, String> entry : issue.getCustomFields().entrySet()) {
            JSONObject customFieldObject = new JSONObject();
            customFieldObject.put("value", entry.getValue());
            fieldsObject.put(entry.getKey().getHints().get("id"), customFieldObject);
        }

        jsonObject.put("fields", fieldsObject);

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/rest/api/2/issue/" + issue.getId(), jsonObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/rest/api/2/issue", jsonObject.toString(), "POST");
        }

        if (status == 200 || status == 201 || status == 204) {
            if (issue.getId() == null) {
                JSONObject result = new JSONObject(this.getCurrentMessage());
                issue.setId(result.getLong("id"));
            }

            List<Note<Long>> oldNotes = this.getNotes(Long.parseLong(String.valueOf(issue.getId())), project_id);
            for (Note<Long> oldNote : oldNotes) {
                boolean exists = false;
                for (Note<Long> note : issue.getNotes()) {
                    if (oldNote.getId().equals(note.getId())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    this.deleteNote(oldNote.getId(), Long.parseLong(String.valueOf(issue.getId())), project_id);
                }
            }

            for (Note<Long> note : issue.getNotes()) {
                this.insertOrUpdateNote(note, Long.parseLong(String.valueOf(issue.getId())), project_id);
            }

            List<Attachment<Long>> oldAttachments = this.getAttachments(Long.parseLong(String.valueOf(issue.getId())), project_id);
            for (Attachment<Long> oldAttachment : oldAttachments) {
                this.deleteAttachment(oldAttachment.getId(), Long.parseLong(String.valueOf(issue.getId())), project_id);
            }

            for (Attachment<Long> attachment : issue.getAttachments()) {
                attachment.setId(null);
                this.insertOrUpdateAttachment(attachment, Long.parseLong(String.valueOf(issue.getId())), project_id);
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/rest/api/2/issue/" + id);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) throws Exception {
        List<Note<Long>> notes = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/issue/" + issue_id + "/comment");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONObject(this.getCurrentMessage()).getJSONArray("comments");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                Note<Long> note = new Note<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String body = jsonObject.getString("body");
                if (body.length() >= 50) {
                    note.setTitle(body.substring(0, 50));
                } else {
                    note.setTitle(body);
                }
                note.setDescription(body);
                note.setId(jsonObject.getLong("id"));
                notes.add(note);
            }
        }
        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("body", note.getDescription());
        JSONObject roleObject = new JSONObject();
        roleObject.put("type", "role");
        roleObject.put("value", "Administrators");
        jsonObject.put("visibility", roleObject);

        if (note.getId() != null) {
            this.executeRequest("/rest/api/2/issue/" + issue_id + "/comment/" + note.getId(), jsonObject.toString(), "PUT");
        } else {
            this.executeRequest("/rest/api/2/issue/" + issue_id + "/comment", jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {
        this.deleteRequest("/rest/api/2/issue/" + issue_id + "/comment/" + id);
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) throws Exception {
        List<Attachment<Long>> attachments = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/issue/" + project_id);
        if (status == 200 || status == 201) {
            JSONObject fieldsObject = new JSONObject(this.getCurrentMessage()).getJSONObject("fields");
            JSONArray attachmentObjects = fieldsObject.getJSONArray("attachment");
            for (int i = 0; i <= attachmentObjects.length() - 1; i++) {
                JSONObject jsonObject = attachmentObjects.getJSONObject(i);
                Attachment<Long> attachment = new Attachment<>();
                attachment.setFilename(jsonObject.getString("filename"));
                attachment.setContent(Converter.convertStringToByteArray(jsonObject.getString("content")));
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) {
    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        this.map.clear();
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/user/search?username=.&startAt=0&maxResults=2000");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (long i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject((int) i);
                User<Long> user = new User<>();
                user.setId(i);
                user.setTitle(jsonObject.getString("name"));
                user.setRealName(jsonObject.getString("displayName"));
                user.setEmail(jsonObject.getString("emailAddress"));
                users.add(user);
                this.map.put(i, user);
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        if (this.map.isEmpty()) {
            this.getUsers(project_id);
        }
        return this.map.get(id);
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", user.getTitle());
        jsonObject.put("displayName", user.getRealName());
        jsonObject.put("emailAddress", user.getEmail());
        jsonObject.put("password", user.getPassword());

        if (user.getId() != null) {
            List<User<Long>> users = this.getUsers(project_id);
            for (User<Long> tmp : users) {
                if (tmp.getId().equals(user.getId())) {
                    this.executeRequest("/rest/api/2/user?username=" + tmp.getTitle(), jsonObject.toString(), "PUT");
                    JSONObject passwordObject = new JSONObject();
                    passwordObject.put("password", user.getPassword());
                    this.executeRequest("/rest/api/2/user/password?username=" + user.getTitle(), passwordObject.toString(), "PUT");
                }
            }
        } else {
            this.executeRequest("/rest/api/2/user", jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteUser(Long id, Long project_id) throws Exception {
        if (this.map.isEmpty()) {
            this.getUsers(project_id);
        }
        User<Long> user = this.map.get(id);
        if (user != null) {
            this.deleteRequest("/rest/api/2/user?username=" + user.getRealName());
        }
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) throws Exception {
        List<CustomField<Long>> customFields = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/field");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getBoolean("custom")) {
                    CustomField<Long> customField = new CustomField<>();
                    customField.getHints().put("id", jsonObject.getString("id"));
                    customField.setTitle(jsonObject.getString("name"));

                    JSONObject schemaObject = jsonObject.getJSONObject("schema");
                    customField.setId(schemaObject.getLong("customId"));
                    switch (schemaObject.getString("type")) {
                        case "number":
                            customField.setType(CustomField.Type.NUMBER);
                            break;
                        case "string":
                            customField.setType(CustomField.Type.TEXT);
                            break;
                        case "array":
                        case "option":
                            customField.setType(CustomField.Type.LIST);
                            break;
                        case "datetime":
                            customField.setType(CustomField.Type.DATE);
                            break;
                    }

                    StringBuilder possibleValues = new StringBuilder();
                    status = this.executeRequest("/rest/api/2/issue/createmeta?projectIds=" + project_id + "&expand=projects.issuetypes.fields");
                    if (status == 200 || status == 201) {
                        JSONObject customObject = new JSONObject(this.getCurrentMessage());
                        JSONArray projectsArray = customObject.getJSONArray("projects");
                        for (int j = 0; j <= projectsArray.length() - 1; j++) {
                            JSONObject projectsObject = projectsArray.getJSONObject(j);
                            JSONArray issueTypes = projectsObject.getJSONArray("issuetypes");
                            for (int k = 0; k <= issueTypes.length() - 1; k++) {
                                JSONObject issueObjects = issueTypes.getJSONObject(k);
                                JSONObject fieldsObject = issueObjects.getJSONObject("fields");

                                if (fieldsObject.has(jsonObject.getString("id"))) {
                                    if (!fieldsObject.isNull(jsonObject.getString("id"))) {
                                        JSONObject customFieldObject = fieldsObject.getJSONObject(jsonObject.getString("id"));
                                        if(customFieldObject.has("allowedValues")) {
                                            if(!customFieldObject.isNull("allowedValues")) {
                                                JSONArray customFieldValues = customFieldObject.getJSONArray("allowedValues");
                                                for (int x = 0; x <= customFieldValues.length() - 1; x++) {
                                                    JSONObject fieldValue = customFieldValues.getJSONObject(x);
                                                    possibleValues.append(fieldValue.getString("value"));
                                                    possibleValues.append("|");
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!possibleValues.toString().isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                    if (!possibleValues.toString().isEmpty()) {
                        customField.setPossibleValues(possibleValues.toString());
                    } else {
                        customField.setPossibleValues("");
                    }
                    customField.setDefaultValue("");

                    customFields.add(customField);
                }
            }
        }

        return customFields;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) {
        return new CustomField<>();
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> customField, Long project_id) {
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) {
    }

    @Override
    public List<String> getCategories(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        List<History<Long>> histories = new LinkedList<>();
        int status = this.executeRequest("/rest/api/2/issue/" + issue_id + "?expand=changelog&fields=\"\"");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONObject(this.getCurrentMessage()).getJSONObject("changelog").getJSONArray("histories");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                History<Long> history = new History<>();
                JSONObject historyObject = jsonArray.getJSONObject(i);
                JSONObject userObject = historyObject.getJSONObject("author");
                history.setUser(userObject.getString("name"));
                history.setTime(Converter.convertStringToDate(historyObject.getString("created"), Jira.DATE_TIME_FORMAT).getTime());
                JSONObject fieldObject = historyObject.getJSONArray("items").getJSONObject(0);
                history.setField(fieldObject.getString("field"));
                if (fieldObject.isNull("fromString")) {
                    history.setOldValue("");
                } else {
                    history.setOldValue(fieldObject.getString("fromString"));
                }
                if (fieldObject.isNull("toString")) {
                    history.setNewValue("");
                } else {
                    history.setOldValue(fieldObject.getString("toString"));
                }
                histories.add(history);
            }
        }
        return histories;
    }

    @Override
    public List<Profile<Long>> getProfiles() {
        return new LinkedList<>();
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new JiraPermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public List<String> getEnums(String title) {
        return new LinkedList<>();
    }
}
