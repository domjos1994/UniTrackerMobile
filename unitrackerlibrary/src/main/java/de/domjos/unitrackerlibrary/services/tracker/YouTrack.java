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

import android.util.Base64;

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.model.issues.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.YoutrackPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.customwidgets.utils.ConvertHelper;

public final class YouTrack extends JSONEngine implements IBugService<String> {
    private final static String PROJECT_FIELDS = "shortName,description,name,archived,id,leader,iconUrl";
    private final static String VERSION_FIELDS = "id,name,values(id,name,description,released,releaseDate,archived)";
    private final static String CUSTOM_FIELDS = "id,name,localizedName,fieldType(id),isAutoAttached,isDisplayedInIssueList,aliases,isUpdateable,fieldDefaults(id,canBeEmpty,emptyFieldText,isPublic),instances(id,project(id,name))";
    private final static String ISSUE_FIELDS = "id,summary,description,tags(id,name),created,updated,customFields(id,projectCustomField(field(id)),value(localizedName,name,text,id))";
    private final static String COMMENT_FIELDS = "id,text,created,updated";
    private final static String ATTACHMENT_FIELDS = "id,name,base64Content,url";
    private final static String USER_FIELDS = "id,login,fullName,email";
    private final static String DATE_TIME_FIELD = "dd-MM-yyyy HH:mm:ss";

    private Authentication authentication;
    private final String hubPart;

    public YouTrack(Authentication authentication) {
        super(authentication);
        super.addHeader("Authorization: Bearer " + authentication.getAPIKey());
        this.authentication = authentication;

        String hub = this.authentication.getHints().get("hub");
        if(hub!=null) {
            this.hubPart = hub.isEmpty() ? "/hub/" : hub.trim();
        } else {
            this.hubPart = "/hub/";
        }
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/api/admin/users/me?fields=" + YouTrack.USER_FIELDS);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return this.authentication.getUserName().equals(jsonObject.getString("login"));
        }
        return false;
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
        if (project.getAlias().trim().isEmpty()) {
            project.setAlias(project.getTitle().replace(" ", "_").toLowerCase().replace("-", "_"));
        }
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
    public List<Version<String>> getVersions(String filter, String project_id) throws Exception {
        List<Version<String>> versions = new LinkedList<>();
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            Map.Entry<String, JSONArray> entry = this.getBundle(project.getTitle(), false);
            if (entry != null) {
                JSONArray valueArray = entry.getValue();
                if (valueArray.length() != 0) {
                    for (int j = 0; j <= valueArray.length() - 1; j++) {
                        JSONObject versionObject = valueArray.getJSONObject(j);
                        Version<String> version = new Version<>();
                        version.setId(versionObject.getString("id"));
                        version.setTitle(versionObject.getString("name"));
                        version.setDescription(versionObject.getString("description"));
                        version.setReleasedVersionAt(this.getLong(versionObject, "releaseDate"));
                        version.setReleasedVersion(this.getBoolean(versionObject, "released"));
                        version.setDeprecatedVersion(this.getBoolean(versionObject, "archived"));
                        versions.add(version);
                    }
                }
            }
        }

        return versions;
    }

    @Override
    public void insertOrUpdateVersion(Version<String> version, String project_id) throws Exception {
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            Map.Entry<String, JSONArray> entry = this.getBundle(project.getTitle(), false);
            if (entry != null) {
                String id = entry.getKey();
                if (version.getId() == null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", version.getTitle());
                    jsonObject.put("description", version.getDescription());
                    jsonObject.put("released", version.isReleasedVersion());
                    jsonObject.put("archived", version.isDeprecatedVersion());
                    jsonObject.put("releaseDate", version.getReleasedVersionAt());
                    entry.getValue().put(jsonObject);
                } else {
                    for (int i = 0; i <= entry.getValue().length() - 1; i++) {
                        JSONObject jsonObject = entry.getValue().getJSONObject(i);
                        if (jsonObject.getString("id").equals(version.getId())) {
                            entry.getValue().getJSONObject(i).put("id", "");
                            entry.getValue().getJSONObject(i).put("name", version.getTitle());
                            entry.getValue().getJSONObject(i).put("description", version.getDescription());
                            entry.getValue().getJSONObject(i).put("released", version.isReleasedVersion());
                            entry.getValue().getJSONObject(i).put("archived", version.isDeprecatedVersion());
                            entry.getValue().getJSONObject(i).put("releaseDate", version.getReleasedVersionAt());
                        }
                    }
                }
                JSONObject rootObject = new JSONObject();
                rootObject.put("values", entry.getValue());
                this.executeRequest("/api/admin/customFieldSettings/bundles/version/" + id, rootObject.toString(), "POST");
            }
        }
    }

    @Override
    public void deleteVersion(String id, String project_id) throws Exception {
        Map.Entry<String, JSONArray> entry = this.getBundle(id, true);
        if (entry != null) {
            JSONArray array = new JSONArray();
            for (int i = 0; i <= entry.getValue().length() - 1; i++) {
                if (!entry.getValue().getJSONObject(i).getString("id").equals(id)) {
                    array.put(entry.getValue().getJSONObject(i));
                }
            }
            JSONObject rootObject = new JSONObject();
            rootObject.put("values", array);
            this.executeRequest("/api/admin/customFieldSettings/bundles/version/" + entry.getKey(), rootObject.toString(), "POST");
        }
    }

    @Override
    public long getMaximumNumberOfIssues(String project_id, IssueFilter filter) throws Exception {
        if(filter==null) {
            filter = IssueFilter.all;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = "%20State:%20Resolved";
            } else {
                filterQuery = "%20State:%20Unresolved";
            }
        }

        Project<String> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/api/issues?query=project:%20" + project.getTitle().replace(" ", "%20") + filterQuery);
            if (status == 200 || status == 201) {
                return new JSONArray(this.getCurrentMessage()).length();
            }
        }

        return 0;
    }

    @Override
    public List<Issue<String>> getIssues(String project_id) throws Exception {
        return this.getIssues(project_id, 1, 20, IssueFilter.all);
    }

    @Override
    public List<Issue<String>> getIssues(String project_id, IssueFilter filter) throws Exception {
        return this.getIssues(project_id, 1, 20, filter);
    }

    @Override
    public List<Issue<String>> getIssues(String project_id, int page, int numberOfItems) throws Exception {
        return this.getIssues(project_id, page, numberOfItems, IssueFilter.all);
    }

    @Override
    public List<Issue<String>> getIssues(String project_id, int page, int numberOfItems, IssueFilter filter) throws Exception {
        String limitation = "";
        if (numberOfItems != -1) {
            limitation = "&$skip=" + ((page - 1) * numberOfItems) + "&$top=" + numberOfItems;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = "%20State:%20Resolved";
            } else {
                filterQuery = "%20State:%20Unresolved";
            }
        }

        List<Issue<String>> issues = new LinkedList<>();
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/api/issues?query=project:%20" + project.getTitle().replace(" ", "%20") + filterQuery + "&fields=id,summary,description" + limitation);
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
    public Issue<String> getIssue(String id, String project_id) throws Exception {
        Issue<String> issue = new Issue<>();
        if (id != null) {
            if (!id.equals("")) {
                int status = this.executeRequest("/api/issues/" + id + "?fields=" + YouTrack.ISSUE_FIELDS);
                if (status == 200 || status == 201) {
                    JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                    issue.setId(jsonObject.getString("id"));
                    this.getHistory(issue.getId(), project_id);
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

                    if (jsonObject.has("tags")) {
                        if (!jsonObject.isNull("tags")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("tags");
                            StringBuilder tags = new StringBuilder();
                            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                                tags.append(jsonArray.getJSONObject(i).getString("name"));
                                tags.append(",");
                            }
                            issue.setTags(tags.toString());
                        }
                    }


                    JSONArray customFieldArray = jsonObject.getJSONArray("customFields");
                    for (int i = 0; i <= customFieldArray.length() - 1; i++) {
                        JSONObject customFieldObject = customFieldArray.getJSONObject(i);
                        if (customFieldObject.has("projectCustomField")) {
                            if (!customFieldObject.isNull("projectCustomField")) {
                                JSONObject projectCustomField = customFieldObject.getJSONObject("projectCustomField");
                                if (projectCustomField.has("field")) {
                                    if (!projectCustomField.isNull("field")) {
                                        String fieldId = projectCustomField.getJSONObject("field").getString("id");


                                        String valueName = "";
                                        if (customFieldObject.has("value")) {
                                            if (!customFieldObject.isNull("value")) {
                                                if (customFieldObject.get("value") instanceof Long) {
                                                    Date date = new Date();
                                                    date.setTime(customFieldObject.getLong("value"));
                                                    valueName = new SimpleDateFormat(YouTrack.DATE_TIME_FIELD, Locale.GERMAN).format(date);
                                                } else if (customFieldObject.get("value") instanceof Integer) {
                                                    valueName = customFieldObject.getString("value");
                                                } else if (customFieldObject.get("value") instanceof Float) {
                                                    valueName = customFieldObject.getString("value");
                                                } else if (customFieldObject.get("value") instanceof JSONObject) {
                                                    JSONObject valueObject = customFieldObject.getJSONObject("value");
                                                    valueName = this.getName(valueObject);
                                                    if (valueObject.has("text")) {
                                                        if (!valueObject.isNull("text")) {
                                                            valueName = valueObject.getString("text");
                                                        }
                                                    }
                                                } else if (customFieldObject.get("value") instanceof JSONArray) {
                                                    JSONArray valueArray = customFieldObject.getJSONArray("value");
                                                    if (valueArray.length() >= 1) {
                                                        JSONObject valueObject = valueArray.getJSONObject(0);
                                                        valueName = this.getName(valueObject);
                                                    }
                                                } else {
                                                    customFieldObject.getString("value");
                                                    valueName = customFieldObject.getString("value");
                                                }

                                            }
                                        }

                                        CustomField<String> customField = this.getCustomField(fieldId, project_id);
                                        if (customField != null) {
                                            customField.setDescription(customFieldObject.getString("$type"));
                                            issue.getCustomFields().put(customField, valueName);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    issue.getNotes().addAll(this.getNotes(issue.getId(), project_id));
                    issue.getAttachments().addAll(this.getAttachments(issue.getId(), project_id));
                }
            }
        }
        return issue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void insertOrUpdateIssue(Issue<String> issue, String project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject projectObject = new JSONObject();
        projectObject.put("id", project_id);
        jsonObject.put("project", projectObject);
        jsonObject.put("summary", issue.getTitle());
        jsonObject.put("description", issue.getDescription());
        jsonObject.put("tags", this.insertOrUpdateTags(issue.getTags(), project_id));
        jsonObject.put("customFields", this.insertOrUpdateCustomFieldValues(issue, project_id));

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/api/issues/" + issue.getId() + "?fields=idReadable", jsonObject.toString(), "POST");
        } else {
            status = this.executeRequest("/api/issues?fields=idReadable", jsonObject.toString(), "POST");
        }

        if (status == 200 || status == 201) {
            JSONObject response = new JSONObject(this.getCurrentMessage());
            issue.setId(response.getString("idReadable"));
        }

        Issue<String> oldIssue = this.getIssue(issue.getId(), project_id);
        List<Note<String>> notes = oldIssue.getNotes();
        List<Attachment<String>> attachments = oldIssue.getAttachments();
        for (Note<String> note : notes) {
            boolean available = false;
            for (Note<String> newNote : issue.getNotes()) {
                if (note.getId().equals(newNote.getId())) {
                    available = true;
                    break;
                }
            }
            if (!available) {
                this.deleteNote(note.getId(), issue.getId(), project_id);
            }
        }

        if (!issue.getNotes().isEmpty()) {
            for (Note<String> note : issue.getNotes()) {
                this.insertOrUpdateNote(note, issue.getId(), project_id);
            }
        }

        for (Attachment<String> attachment : attachments) {
            boolean available = false;
            for (Attachment<String> newAttachment : issue.getAttachments()) {
                if (attachment.getId().equals(newAttachment.getId())) {
                    available = true;
                    break;
                }
            }
            if (!available) {
                this.deleteAttachment(attachment.getId(), issue.getId(), project_id);
            }
        }

        if (!issue.getAttachments().isEmpty()) {
            for (DescriptionObject descriptionObject : issue.getAttachments()) {
                if (descriptionObject instanceof Attachment) {
                    this.insertOrUpdateAttachment((Attachment<String>) descriptionObject, issue.getId(), project_id);
                }
            }
        }
    }

    @Override
    public void deleteIssue(String id, String project_id) throws Exception {
        this.deleteRequest("/api/issues/" + id);
    }

    @Override
    public List<Note<String>> getNotes(String issue_id, String project_id) throws Exception {
        List<Note<String>> notes = new LinkedList<>();
        int status = this.executeRequest("/api/issues/" + issue_id + "/comments?fields=" + YouTrack.COMMENT_FIELDS);
        if (status == 200 || status == 201) {
            JSONArray array = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= array.length() - 1; i++) {
                JSONObject commentObject = array.getJSONObject(i);
                Note<String> note = new Note<>();
                note.setId(commentObject.getString("id"));
                note.setDescription(commentObject.getString("text"));
                if (note.getDescription().length() >= 50) {
                    note.setTitle(note.getDescription().substring(0, 50));
                } else {
                    note.setTitle(note.getDescription());
                }
                if (commentObject.has("created")) {
                    Date date = new Date();
                    date.setTime(commentObject.getLong("created"));
                    note.setSubmitDate(date);
                }
                if (commentObject.has("updated")) {
                    if (!commentObject.isNull("updated")) {
                        Date date = new Date();
                        date.setTime(commentObject.getLong("updated"));
                        note.setLastUpdated(date);
                    }
                }
                notes.add(note);
            }
        }
        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<String> note, String issue_id, String project_id) throws Exception {
        JSONObject noteObject = new JSONObject();
        noteObject.put("text", note.getDescription());

        if (note.getId() == null) {
            this.executeRequest("/api/issues/" + issue_id + "/comments?fields=id", noteObject.toString(), "POST");
        } else {
            this.executeRequest("/api/issues/" + issue_id + "/comments/" + note.getId() + "?fields=id", noteObject.toString(), "POST");
        }
    }

    @Override
    public void deleteNote(String id, String issue_id, String project_id) throws Exception {
        this.deleteRequest("/api/issues/" + issue_id + "/comments/" + id);
    }

    @Override
    public List<Attachment<String>> getAttachments(String issue_id, String project_id) throws Exception {
        List<Attachment<String>> attachments = new LinkedList<>();
        int status = this.executeRequest("/api/issues/" + issue_id + "/attachments?fields=" + YouTrack.ATTACHMENT_FIELDS);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject attachmentObject = jsonArray.getJSONObject(i);
                Attachment<String> attachment = new Attachment<>();
                attachment.setId(attachmentObject.getString("id"));
                attachment.setFilename(attachmentObject.getString("name"));
                attachment.setDownloadUrl(this.authentication.getServer() + attachmentObject.getString("url"));
                try {
                    attachment.setContent(Base64.decode(attachmentObject.getString("base64Content"), Base64.DEFAULT));
                } catch (Exception ignored) {
                }
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<String> attachment, String issue_id, String project_id) throws Exception {
        JSONObject attachmentObject = new JSONObject();
        attachmentObject.put("name", UUID.randomUUID().toString());
        attachmentObject.put("base64Content", Base64.encodeToString(attachment.getContent(), Base64.DEFAULT));

        if (attachment.getId() == null) {
            this.executeRequest("/api/issues/" + issue_id + "/attachments?fields=id", attachmentObject.toString(), "POST");
        } else {
            this.executeRequest("/api/issues/" + issue_id + "/attachments/" + attachment.getId() + "?fields=id", attachmentObject.toString(), "POST");
        }
    }

    @Override
    public void deleteAttachment(String id, String issue_id, String project_id) throws Exception {
        this.deleteRequest("/api/issues/" + issue_id + "/attachments/" + id);
    }

    @Override
    public List<Relationship<String>> getBugRelations(String issue_id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<String> relationship, String issue_id, String project_id) throws Exception {

    }

    @Override
    public void deleteBugRelation(Relationship<String> relationship, String issue_id, String project_id) throws Exception {

    }

    @Override
    public List<User<String>> getUsers(String project_id) throws Exception {
        List<User<String>> users = new LinkedList<>();
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest(this.hubPart + "api/rest/users?fields=id,name,login,email");
            if (status == 200 || status == 201) {
                JSONObject usersObject = new JSONObject(this.getCurrentMessage());
                JSONArray usersArray = usersObject.getJSONArray("users");
                for (int i = 0; i <= usersArray.length() - 1; i++) {
                    JSONObject jsonObject = usersArray.getJSONObject(i);
                    User<String> user = new User<>();
                    user.setId(jsonObject.getString("id"));
                    user.setTitle(jsonObject.getString("login"));
                    user.setRealName(jsonObject.getString("name"));
                    if (jsonObject.has("email")) {
                        user.setEmail(jsonObject.getString("email"));
                    }
                    users.add(user);
                }
            }
        }
        return users;
    }

    @Override
    public User<String> getUser(String id, String project_id) throws Exception {
        int status = this.executeRequest(this.hubPart + "api/rest/users/" + id + "?fields=id,name,login,email");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            User<String> user = new User<>();
            user.setId(jsonObject.getString("id"));
            user.setTitle(jsonObject.getString("login"));
            user.setRealName(jsonObject.getString("name"));
            if (jsonObject.has("email")) {
                user.setEmail(jsonObject.getString("email"));
            }
            this.executeRequest(this.hubPart + "api/rest/users/" + id + "/applicationPasswords?fields=id,name,password");

            return user;
        }
        return new User<>();
    }

    @Override
    public void insertOrUpdateUser(User<String> user, String project_id) throws Exception {
        if (user.getId() != null) {
            List<User<String>> users = this.getUsers(project_id);
            for (User<String> tmp : users) {
                if (tmp.getTitle().equals(user.getTitle())) {
                    user.setId(tmp.getId());
                    break;
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", user.getTitle());
        jsonObject.put("name", user.getRealName());

        int status;
        if (user.getId() == null) {
            status = this.executeRequest(this.hubPart + "api/rest/users?fields=id", jsonObject.toString(), "POST");
        } else {
            status = this.executeRequest(this.hubPart + "api/rest/users/" + user.getId() + "?fields=id", jsonObject.toString(), "POST");
        }
        if (status == 200 || status == 201) {
            if (user.getId() == null) {
                JSONObject response = new JSONObject(this.getCurrentMessage());
                user.setId(response.getString("id"));

                JSONObject object = new JSONObject();
                object.put("name", user.getTitle());
                object.put("password", user.getPassword());
                jsonObject.put("id", user.getId());
                object.put("user", jsonObject);
                this.executeRequest(this.hubPart + "api/rest/users/" + user.getId() + "/applicationPasswords", object.toString(), "POST");
            }

        }
    }

    @Override
    public void deleteUser(String id, String project_id) throws Exception {
        this.deleteRequest(this.hubPart + "api/rest/users/" + id);
    }

    @Override
    public List<CustomField<String>> getCustomFields(String project_id) throws Exception {
        List<CustomField<String>> customFields = new LinkedList<>();
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/api/admin/customFieldSettings/customFields?fields=id");
            if (status == 200 || status == 201) {
                JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    customFields.add(this.getCustomField(jsonObject.getString("id"), project.getTitle()));
                }
            }
        }
        return customFields;
    }

    @Override
    public CustomField<String> getCustomField(String id, String project_id) throws Exception {
        int status = this.executeRequest("/api/admin/customFieldSettings/customFields/" + id + "?fields=" + YouTrack.CUSTOM_FIELDS);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            CustomField<String> customField = new CustomField<>();
            customField.setId(jsonObject.getString("id"));
            if (jsonObject.has("localizedName")) {
                if (!jsonObject.isNull("localizedName")) {
                    customField.setTitle(jsonObject.getString("localizedName"));
                }
            }
            if (customField.getTitle().trim().isEmpty()) {
                customField.setTitle(jsonObject.getString("name"));
            }
            if (jsonObject.has("fieldDefaults")) {
                if (!jsonObject.isNull("fieldDefaults")) {
                    JSONObject fieldDefaults = jsonObject.getJSONObject("fieldDefaults");
                    customField.setDefaultValue(fieldDefaults.getString("emptyFieldText"));
                    customField.setNullable(fieldDefaults.getBoolean("canBeEmpty"));
                }
            }

            String field_id = "";
            if (jsonObject.has("fieldType")) {
                if (!jsonObject.isNull("fieldType")) {
                    field_id = jsonObject.getJSONObject("fieldType").getString("id");
                    if (field_id.contains("[")) {
                        field_id = field_id.substring(0, field_id.lastIndexOf("["));
                    }
                    StringBuilder possibleValues = new StringBuilder();
                    Project<String> project = this.getProject(project_id);
                    if (project != null) {
                        for (String item : this.getValuesByBundle(project.getTitle(), customField.getTitle(), field_id).values()) {
                            possibleValues.append(item);
                            possibleValues.append("|");
                        }
                        customField.setPossibleValues(possibleValues.toString());
                    }
                }
            }
            if (jsonObject.has("instances")) {
                if (!jsonObject.isNull("instances")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("instances");
                    for (int i = 0; i <= jsonArray.length() - 1; i++) {
                        JSONObject instanceObject = jsonArray.getJSONObject(i);
                        JSONObject projectObject = instanceObject.getJSONObject("project");
                        if (projectObject.getString("id").equals(project_id)) {
                            customField.setId(instanceObject.getString("id"));
                            customField.setDescription(jsonObject.getString("$type"));
                        }
                    }
                }
            }

            switch (field_id) {
                case "enum":
                case "state":
                case "user":
                case "ownedField":
                case "version":
                case "build":
                    customField.setType(CustomField.Type.LIST);
                    break;
                case "date":
                case "date and time":
                    customField.setType(CustomField.Type.DATE);
                    break;
                case "text":
                case "string":
                    customField.setType(CustomField.Type.TEXT);
                    break;
                case "integer":
                case "float":
                    customField.setType(CustomField.Type.NUMBER);
                    break;
            }


            return customField;
        }
        return null;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<String> customField, String project_id) throws Exception {
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            JSONObject fieldObject = new JSONObject();
            JSONObject customFieldObject = new JSONObject();

            JSONObject fieldTypeObject = new JSONObject();
            fieldTypeObject.put("$type", "FieldType");
            switch (customField.getType()) {
                case TEXT:
                case TEXT_AREA:
                    fieldTypeObject.put("id", "text");
                    fieldObject.put("$type", "TextProjectCustomField");
                    break;
                case NUMBER:
                    fieldTypeObject.put("id", "float");
                    fieldObject.put("$type", "SimpleProjectCustomField");
                    break;
                case DATE:
                    fieldTypeObject.put("id", "date and time");
                    fieldObject.put("$type", "SimpleProjectCustomField");
                    break;
            }
            customFieldObject.put("fieldType", fieldTypeObject);

            JSONObject fieldDefaultObject = new JSONObject();
            fieldDefaultObject.put("isPublic", true);
            fieldDefaultObject.put("canBeEmpty", customField.isNullable());
            fieldDefaultObject.put("$type", "CustomFieldDefaults");
            fieldDefaultObject.put("emptyFieldText", customField.getDefaultValue());
            customFieldObject.put("fieldDefaults", fieldDefaultObject);

            customFieldObject.put("isDisplayedInIssueList", true);
            customFieldObject.put("isAutoAttached", true);
            customFieldObject.put("name", customField.getTitle());
            customFieldObject.put("$type", "CustomField");

            int status;
            if (customField.getId() == null) {
                status = this.executeRequest("/api/admin/customFieldSettings/customFields?fields=id", customFieldObject.toString(), "POST");
            } else {
                status = this.executeRequest("/api/admin/customFieldSettings/customFields/" + customField.getId() + "?fields=id", customFieldObject.toString(), "POST");
            }

            if (status == 200 || status == 201) {
                if (customField.getId() == null) {
                    JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                    customFieldObject.put("id", jsonObject.getString("id"));
                    fieldObject.put("field", customFieldObject);

                    this.executeRequest("/api/admin/projects/" + project_id + "/customFields", fieldObject.toString(), "POST");
                }
            }
        }

    }

    @Override
    public void deleteCustomField(String id, String project_id) throws Exception {
        this.deleteRequest("/api/admin/customFieldSettings/customFields/" + id);
    }

    @Override
    public List<Tag<String>> getTags(String project_id) throws Exception {
        List<Tag<String>> tags = new LinkedList<>();
        int status = this.executeRequest("/api/issueTags?fields=id,name");
        if (status == 200 || status == 201) {
            JSONArray tagArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= tagArray.length() - 1; i++) {
                JSONObject jsonObject = tagArray.getJSONObject(i);
                Tag<String> tag = new Tag<>();
                tag.setId(jsonObject.getString("id"));
                tag.setTitle(jsonObject.getString("name"));
                tags.add(tag);
            }
        }
        return tags;
    }

    @Override
    public List<History<String>> getHistory(String issue_id, String project_id) {
        return new LinkedList<>();
    }

    @Override
    public List<Profile<String>> getProfiles() {
        return new LinkedList<>();
    }

    private JSONArray insertOrUpdateTags(String strTags, String project_id) throws Exception {
        JSONArray tagArray = new JSONArray();
        List<Tag<String>> tags = this.getTags(project_id);
        for (String tag : strTags.split(",")) {
            boolean found = false;
            for (Tag<String> current : tags) {
                if (current.getTitle().equals(tag.trim())) {
                    JSONObject tagObject = new JSONObject();
                    tagObject.put("id", current.getId());
                    tagObject.put("name", tag.trim());
                    tagArray.put(tagObject);
                    found = true;
                    break;
                }
            }

            if (!found) {
                JSONObject tagObject = new JSONObject();
                tagObject.put("name", tag.trim());
                int status = this.executeRequest("/api/issueTags", tagObject.toString(), "POST");
                if (status == 200 || status == 201) {
                    for (Tag<String> current : this.getTags(project_id)) {
                        if (current.getTitle().equals(tag.trim())) {
                            tagObject = new JSONObject();
                            tagObject.put("id", current.getId());
                            tagObject.put("name", tag.trim());
                            tagArray.put(tagObject);
                            break;
                        }
                    }
                }
            }
        }
        return tagArray;
    }

    @Override
    public List<String> getCategories(String project_id) {
        return new LinkedList<>();
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new YoutrackPermissions(this.authentication);
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

    private String getName(JSONObject object) throws Exception {
        String name = "";
        if (object.has("localizedName")) {
            if (!object.isNull("localizedName")) {
                name = object.getString("localizedName");
            }
        }
        if (name.trim().isEmpty()) {
            if (object.has("name")) {
                if (!object.isNull("name")) {
                    name = object.getString("name");
                }
            }
        }
        return name;
    }

    private JSONArray insertOrUpdateCustomFieldValues(Issue<String> issue, String project_id) throws Exception {
        JSONArray customFieldsArray = new JSONArray();
        for (Map.Entry<CustomField<String>, String> entry : issue.getCustomFields().entrySet()) {
            if(entry.getValue()!=null) {
                if(!entry.getValue().equals("") || !entry.getValue().equals("null")) {
                    if (entry.getValue().trim().equals(entry.getKey().getDefaultValue().trim())) {
                        continue;
                    }

                    JSONObject customFieldObject = new JSONObject();
                    JSONObject valueObject = new JSONObject();

                    switch (entry.getKey().getType()) {
                        case LIST:
                            boolean setField = false;
                            if(entry.getKey().getPossibleValues()!=null) {
                                for (String item : entry.getKey().getPossibleValues().split("\\|")) {
                                    if (item.split(":")[0].trim().equals(entry.getValue())) {
                                        valueObject.put("name", item.split(":")[1].trim());
                                        setField = true;
                                        break;
                                    }
                                }
                            }
                            if (!setField) {
                                valueObject = null;
                            }

                            if (valueObject != null || entry.getKey().getDescription().contains("MultiVersion")) {
                                if (entry.getKey().getDescription().contains("User")) {
                                    valueObject = this.convertUserToObject(valueObject, entry.getKey(), project_id);
                                }
                                if (entry.getKey().getDescription().contains("OwnedIssue")) {
                                    valueObject = this.convertOwnedFieldToObject(valueObject, entry.getKey(), project_id);
                                }

                                if (entry.getKey().getDescription().contains("MultiVersion")) {
                                    valueObject = new JSONObject();
                                    valueObject.put("name", "");
                                    customFieldObject.put("value", this.convertVersionToArray(valueObject, entry.getKey().getDefaultValue(), project_id));
                                } else {
                                    if (valueObject != null) {
                                        customFieldObject.put("value", valueObject);
                                    } else {
                                        customFieldObject.put("value", JSONObject.NULL);
                                    }
                                }
                            } else {
                                customFieldObject.put("value", JSONObject.NULL);
                            }
                            break;
                        case TEXT:
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("text", entry.getValue());
                            customFieldObject.put("value", jsonObject);
                            break;
                        case DATE:
                            if(entry.getValue()!=null) {
                                if(!entry.getValue().equals("null")) {
                                    Date dt = ConvertHelper.convertStringToDate(entry.getValue(), YouTrack.DATE_TIME_FIELD);
                                    if(dt!=null) {
                                        customFieldObject.put("value", dt.getTime());
                                    }
                                }
                            }
                            break;
                        case NUMBER:
                            try {
                                int val = Integer.parseInt(entry.getValue());
                                customFieldObject.put("value", val);
                            } catch (Exception ex) {
                                try {
                                    float val = Float.parseFloat(entry.getValue());
                                    customFieldObject.put("value", val);
                                } catch (Exception ignored) {
                                }
                            }
                    }

                    CustomField customField = this.getCustomField(entry.getKey().getId(), project_id);
                    if(customField!=null) {
                        customFieldObject.put("$type", customField.getDescription());
                    }
                    customFieldObject.put("name", entry.getKey().getTitle());
                    customFieldObject.put("id", entry.getKey().getId());
                    customFieldsArray.put(customFieldObject);
                }
            }
        }
        return customFieldsArray;
    }

    private JSONObject convertOwnedFieldToObject(JSONObject jsonObject, CustomField<String> field, String project_id) throws Exception {
        if (field.getDescription().contains("OwnedIssue")) {
            Project<String> project = this.getProject(project_id);
            if (project != null) {
                boolean found = false;
                Map<String, String> fields = this.getValuesByBundle(project.getTitle(), field.getTitle(), "ownedField");
                String name = jsonObject.getString("name");
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    String[] spl = entry.getValue().split(":");
                    if (name.contains(spl[0]) || name.contains(spl[1])) {
                        jsonObject.put("id", entry.getKey());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    jsonObject = null;
                }
            }
        }
        return jsonObject;
    }

    private JSONObject convertUserToObject(JSONObject jsonObject, CustomField<String> field, String project_id) throws Exception {
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            if (field.getDescription().contains("User")) {
                String name = jsonObject.getString("name");
                List<User<String>> users = this.getUsers(project_id);
                boolean found = false;
                for (User<String> user : users) {
                    if (name.equals(user.getRealName())) {
                        jsonObject.put("id", user.getId());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    jsonObject = null;
                }
            }
        }
        return jsonObject;
    }

    private JSONArray convertVersionToArray(JSONObject jsonObject, String defaultValue, String project_id) throws Exception {
        String name = jsonObject.getString("name");
        List<Version<String>> versions = this.getVersions("", project_id);
        for (Version<String> version : versions) {
            if (version.getTitle().equals(name)) {
                jsonObject.put("id", version.getId());
                break;
            }
        }

        JSONArray jsonArray = new JSONArray();
        if (!jsonObject.getString("name").isEmpty()) {
            if (!jsonObject.getString("name").equals(defaultValue)) {
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }

    private Map<String, String> getValuesByBundle(String projectTitle, String customFieldName, String enumField) throws Exception {
        Map<String, String> list = new LinkedHashMap<>();
        String val = "values";
        if (enumField.equals("user")) {
            val = "aggregatedUsers";
        }

        int status = this.executeRequest("/api/admin/customFieldSettings/bundles/" + enumField + "?fields=id,name," + val + "(id,name,localizedName)");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                boolean isField = false;
                if (name.contains(": ")) {
                    String[] spl = name.split(": ");

                    if (enumField.equals("version")) {
                        if (spl[0].trim().toLowerCase().contains(projectTitle.trim().toLowerCase()) &&
                                spl[1].trim().toLowerCase().contains(enumField.trim().toLowerCase())) {

                            isField = true;
                        }
                    } else {
                        if (spl[0].trim().toLowerCase().contains(projectTitle.trim().toLowerCase()) &&
                                spl[1].trim().toLowerCase().contains(customFieldName.trim().toLowerCase())) {

                            isField = true;
                        }
                    }
                } else {
                    if (enumField.equals("version")) {
                        if (name.trim().toLowerCase().contains(enumField.trim().toLowerCase())) {
                            isField = true;
                        }
                    } else {
                        if (name.trim().toLowerCase().contains(customFieldName.trim().toLowerCase())) {
                            isField = true;
                        }
                    }
                }

                if (isField) {
                    if (jsonObject.has(val)) {
                        if (!jsonObject.isNull(val)) {
                            for (int j = 0; j <= jsonObject.getJSONArray(val).length() - 1; j++) {
                                JSONObject valueObject = jsonObject.getJSONArray(val).getJSONObject(j);
                                String value = "";
                                if (valueObject.has("localizedName")) {
                                    if (!valueObject.isNull("localizedName")) {
                                        value = valueObject.getString("localizedName");
                                    }
                                }
                                if (value.isEmpty()) {
                                    if (valueObject.has("name")) {
                                        if (!valueObject.isNull("name")) {
                                            value = valueObject.getString("name");

                                        }
                                    }
                                }
                                list.put(valueObject.getString("id"), value + ":" + valueObject.getString("name"));
                            }
                        }
                    }
                }
            }
        }
        return list;
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
        String iconUrl = jsonObject.getString("iconUrl");
        iconUrl = iconUrl.trim();
        if (!iconUrl.isEmpty()) {
            if (iconUrl.startsWith("/")) {
                project.setIconUrl(this.authentication.getServer() + jsonObject.getString("iconUrl"));
            } else {
                project.setIconUrl(jsonObject.getString("iconUrl"));
            }
        }
        return project;
    }


    private Map.Entry<String, JSONArray> getBundle(String projectName, boolean id) throws Exception {
        int status = this.executeRequest("/api/admin/customFieldSettings/bundles/version?fields=" + YouTrack.VERSION_FIELDS);

        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());

            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String bundle = jsonObject.getString("name");
                String version = jsonObject.getString("id");
                if (bundle.contains(": ")) {
                    bundle = bundle.split(": ")[0].trim();
                }
                if (!id) {
                    if (bundle.equals(projectName)) {
                        if (jsonObject.has("values")) {
                            return new AbstractMap.SimpleEntry<>(version, jsonObject.getJSONArray("values"));
                        }
                    }
                } else {
                    JSONArray array = jsonObject.getJSONArray("values");
                    for (int j = 0; j <= array.length() - 1; j++) {
                        if (projectName.equals(array.getJSONObject(j).getString("id"))) {
                            return new AbstractMap.SimpleEntry<>(version, jsonObject.getJSONArray("values"));
                        }
                    }
                }
            }
        }
        return null;
    }
}
