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

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.YoutrackPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;

public final class YouTrack extends JSONEngine implements IBugService<String> {
    private final static String PROJECT_FIELDS = "shortName,description,name,archived,id,leader,iconUrl";
    private final static String VERSION_FIELDS = "id,name,values(id,name,description,released,releaseDate,archived)";
    private final static String CUSTOM_FIELDS = "id,name,localizedName,fieldType(id),isAutoAttached,isDisplayedInIssueList,aliases,isUpdateable,fieldDefaults(canBeEmpty,emptyFieldText,isPublic),instances(id,project(id,name))";
    private final static String ISSUE_FIELDS = "id,summary,description,tags(id,name),created,updated,customFields(id,projectCustomField(field(id)),value(localizedName,name,id))";
    private final static String COMMENT_FIELDS = "id,text,created,updated";
    private final static String ATTACHMENT_FIELDS = "id,name,base64Content,url";
    private final static String USER_FIELDS = "id,login,fullName,email";
    private Authentication authentication;

    public YouTrack(Authentication authentication) {
        super(authentication, "Authorization: Bearer " + authentication.getAPIKey());
        this.authentication = authentication;
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
    public List<Issue<String>> getIssues(String project_id) throws Exception {
        List<Issue<String>> issues = new LinkedList<>();
        Project<String> project = this.getProject(project_id);
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
    public Issue<String> getIssue(String id, String project_id) throws Exception {
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
                                        if (customFieldObject.get("value") instanceof JSONObject) {
                                            JSONObject valueObject = customFieldObject.getJSONObject("value");
                                            valueName = this.getName(valueObject);
                                        } else if (customFieldObject.get("value") instanceof JSONArray) {
                                            JSONArray valueArray = customFieldObject.getJSONArray("value");
                                            if (valueArray.length() >= 1) {
                                                JSONObject valueObject = valueArray.getJSONObject(0);
                                                valueName = this.getName(valueObject);
                                            }
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
        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<String> issue, String project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject projectObject = new JSONObject();
        projectObject.put("id", project_id);
        jsonObject.put("project", projectObject);
        jsonObject.put("summary", issue.getTitle());
        jsonObject.put("description", issue.getDescription());

        JSONArray tagArray = new JSONArray();
        List<Tag<String>> tags = this.getTags(project_id);
        for (String tag : issue.getTags().split(",")) {
            for (Tag<String> current : tags) {
                if (current.getTitle().equals(tag.trim())) {
                    JSONObject tagObject = new JSONObject();
                    tagObject.put("id", current.getId());
                    tagObject.put("name", tag.trim());
                    tagArray.put(tagObject);
                    break;
                }
            }
        }
        jsonObject.put("tags", tagArray);

        JSONArray customFieldsArray = new JSONArray();
        int i = 0;
        for (Map.Entry<CustomField<String>, String> entry : issue.getCustomFields().entrySet()) {
            if (i == 4 || i == 7) {
                i++;
                continue;
            }
            JSONObject customFieldObject = new JSONObject();
            JSONObject valueObject = new JSONObject();
            boolean setField = false;
            for (String item : entry.getKey().getPossibleValues().split("\\|")) {
                if (item.split(":")[0].trim().equals(entry.getValue())) {
                    valueObject.put("name", item.split(":")[1].trim());
                    setField = true;
                    break;
                }
            }
            if (!setField) {
                valueObject.put("name", entry.getKey().getDefaultValue());
            }
            if (entry.getKey().getDescription().contains("User")) {
                String name = valueObject.getString("name");
                List<User<String>> users = this.getUsers(project_id);
                for (User<String> user : users) {
                    if (name.equals(user.getRealName())) {
                        valueObject.put("id", user.getId());
                        break;
                    }
                }
            }
            if (entry.getKey().getDescription().contains("MultiVersion")) {
                String name = valueObject.getString("name");
                List<Version<String>> versions = this.getVersions("", project_id);
                for (Version<String> version : versions) {
                    if (version.getTitle().equals(name)) {
                        valueObject.put("id", version.getId());
                        break;
                    }
                }

                JSONArray jsonArray = new JSONArray();
                if (!valueObject.getString("name").equals(entry.getKey().getDefaultValue())) {
                    jsonArray.put(valueObject);
                }
                customFieldObject.put("value", jsonArray);
            } else {
                customFieldObject.put("value", valueObject);
            }

            customFieldObject.put("$type", entry.getKey().getDescription());
            customFieldObject.put("name", entry.getKey().getTitle());
            customFieldObject.put("id", entry.getKey().getId());
            customFieldsArray.put(customFieldObject);
            i++;
        }
        jsonObject.put("customFields", customFieldsArray);

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
            for (Attachment<String> attachment : issue.getAttachments()) {
                this.insertOrUpdateAttachment(attachment, issue.getId(), project_id);
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
                attachment.setContent(Base64.decode(attachmentObject.getString("base64Content"), Base64.DEFAULT));
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
    public List<User<String>> getUsers(String project_id) throws Exception {
        List<User<String>> users = new LinkedList<>();
        Project<String> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/api/admin/customFieldSettings/bundles/user?fields=name,aggregatedUsers(id,name,fullName,login,email)");
            if (status == 200 || status == 201) {
                JSONArray usersArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= usersArray.length() - 1; i++) {
                    JSONObject jsonObject = usersArray.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    if (name.contains(project.getTitle())) {
                        JSONArray jsonArray = jsonObject.getJSONArray("aggregatedUsers");
                        for (int j = 0; j <= jsonArray.length() - 1; j++) {
                            JSONObject userObject = jsonArray.getJSONObject(j);
                            User<String> user = new User<>();
                            user.setId(userObject.getString("id"));
                            user.setTitle(userObject.getString("login"));
                            if (userObject.has("fullName")) {
                                if (!userObject.isNull("fullName")) {
                                    user.setRealName(userObject.getString("fullName"));
                                }
                            }
                            if (userObject.has("email")) {
                                if (!userObject.isNull("email")) {
                                    user.setEmail(userObject.getString("email"));
                                }
                            }

                            boolean isInList = false;
                            for (User<String> tmp : users) {
                                if (tmp.getTitle().equals(user.getTitle())) {
                                    isInList = true;
                                    break;
                                }
                            }
                            if (!isInList) {
                                users.add(user);
                            }
                        }
                    }
                }
            }
        }
        return users;
    }

    @Override
    public User<String> getUser(String id, String project_id) throws Exception {
        return null;
    }

    @Override
    public String insertOrUpdateUser(User<String> user, String project_id) throws Exception {
        return null;
    }

    @Override
    public void deleteUser(String id, String project_id) throws Exception {

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
                    customField.setDefaultValue(jsonObject.getJSONObject("fieldDefaults").getString("emptyFieldText"));
                }
            }
            if (jsonObject.has("fieldType")) {
                if (!jsonObject.isNull("fieldType")) {
                    String field_id = jsonObject.getJSONObject("fieldType").getString("id");
                    field_id = field_id.substring(0, field_id.lastIndexOf("["));
                    StringBuilder possibleValues = new StringBuilder();
                    Project<String> project = this.getProject(project_id);
                    if (project != null) {
                        for (String item : this.getValuesByBundle(project.getTitle(), customField.getTitle(), field_id)) {
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
                            customField.setDescription(instanceObject.getString("$type"));
                        }
                    }
                }
            }
            customField.setType(CustomField.Type.LIST);

            return customField;
        }
        return null;
    }

    @Override
    public String insertOrUpdateCustomField(CustomField<String> user, String project_id) throws Exception {
        return null;
    }

    @Override
    public void deleteCustomField(String id, String project_id) throws Exception {

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
    public List<String> getCategories(String project_id) throws Exception {
        List<String> categories = new LinkedList<>();

        return categories;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new YoutrackPermissions(this.authentication);
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

    private List<String> getValuesByBundle(String projectTitle, String customFieldName, String enumField) throws Exception {
        List<String> list = new LinkedList<>();
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
                                list.add(value + ":" + valueObject.getString("name"));
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
        if (iconUrl != null) {
            iconUrl = iconUrl.trim();
            if (!iconUrl.isEmpty()) {
                if (iconUrl.startsWith("/")) {
                    project.setIconUrl(this.authentication.getServer() + jsonObject.getString("iconUrl"));
                } else {
                    project.setIconUrl(jsonObject.getString("iconUrl"));
                }
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
