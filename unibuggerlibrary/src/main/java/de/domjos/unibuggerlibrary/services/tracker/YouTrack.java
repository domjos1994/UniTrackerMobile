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
import java.util.LinkedHashMap;
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
    private final static String ISSUE_FIELDS = "id,summary,description,tags,created,updated,comments(id,text,created,updated),attachments(id,name,base64Content,url),customFields($type,id,projectCustomField($type,id,field($type,id,name)),value($type,avatarUrl,buildLink,color(id),fullName,id,isResolved,localizedName,login,minutes,name,presentation,text))";
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
    public List<Version<String>> getVersions(String pid, String filter) throws Exception {
        List<Version<String>> versions = new LinkedList<>();
        Project<String> project = this.getProject(pid);
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
                        version.setReleasedVersionAt(versionObject.getLong("releaseDate"));
                        version.setReleasedVersion(versionObject.getBoolean("released"));
                        version.setDeprecatedVersion(versionObject.getBoolean("archived"));
                        versions.add(version);
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
        return null;
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

    @Override
    public void deleteVersion(String id) throws Exception {
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

                if (fieldDescription.has("field")) {
                    JSONObject fieldObject = fieldDescription.getJSONObject("field");

                    String valueId = "", valueName = "";
                    if (customFieldObject.has("value")) {
                        if (!customFieldObject.isNull("value")) {
                            if (customFieldObject.get("value") instanceof JSONObject) {
                                JSONObject valueObject = customFieldObject.getJSONObject("value");
                                valueId = valueObject.getString("id");
                                valueName = valueObject.getString("name");
                            } else if (customFieldObject.get("value") instanceof JSONArray) {
                                JSONArray valueArray = customFieldObject.getJSONArray("value");
                                if (valueArray.length() >= 1) {
                                    JSONObject valueObject = valueArray.getJSONObject(0);
                                    valueId = valueObject.getString("id");
                                    valueName = valueObject.getString("name");
                                }
                            }
                        }
                    }


                    if (fieldObject.has("name")) {
                        String name = fieldObject.getString("name");
                        switch (name) {
                            case "Priority":
                                issue.setPriority(Integer.parseInt(valueId.split("-")[1]), valueName);
                                break;
                            case "Type":
                                issue.setSeverity(Integer.parseInt(valueId.split("-")[1]), valueName);
                                break;
                            case "State":
                                issue.setStatus(Integer.parseInt(valueId.split("-")[1]), valueName);
                                break;
                            case "Assignee":
                                if (!valueName.equals("")) {
                                    User<String> user = new User<>();
                                    user.setTitle(valueName);
                                    user.setId(valueId);
                                    issue.setHandler(user);
                                }
                                break;
                            case "Fix versions":
                                issue.setFixedInVersion(valueName);
                                break;
                            case "Affected versions":
                                issue.setVersion(valueName);
                                break;
                        }
                    }
                }
            }

            if (jsonObject.has("comments")) {
                JSONArray jsonArray = jsonObject.getJSONArray("comments");
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject commentObject = jsonArray.getJSONObject(i);
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
                    issue.getNotes().add(note);
                }
            }

            if (jsonObject.has("attachments")) {
                JSONArray jsonArray = jsonObject.getJSONArray("attachments");
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject attachmentObject = jsonArray.getJSONObject(i);
                    Attachment<String> attachment = new Attachment<>();
                    attachment.setId(attachmentObject.getString("id"));
                    attachment.setFilename(attachmentObject.getString("name"));
                    attachment.setDownloadUrl(this.authentication.getServer() + attachmentObject.getString("url"));
                    attachment.setContent(Base64.decode(attachmentObject.getString("base64Content"), Base64.DEFAULT));
                    issue.getAttachments().add(attachment);
                }
            }
        }
        return issue;
    }

    @Override
    public String insertOrUpdateIssue(String pid, Issue<String> issue) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject projectObject = new JSONObject();
        projectObject.put("id", pid);
        jsonObject.put("project", projectObject);
        jsonObject.put("summary", issue.getTitle());
        jsonObject.put("description", issue.getDescription());

        Map<String, String> fields = this.getCustomFields();
        Map<String, String> localized = this.getValuesFromLocalized();
        JSONArray customFieldsArray = new JSONArray();
        int i = 0;
        customFieldsArray.put(i++, this.putCustomField(localized.get(issue.getPriority().getValue()), "Priority", fields.get("Priority"), "SingleEnumIssueCustomField"));
        customFieldsArray.put(i++, this.putCustomField(localized.get(issue.getSeverity().getValue()), "Type", fields.get("Type"), "SingleEnumIssueCustomField"));
        //customFieldsArray.put(2, this.putCustomField(localized.get(issue.getPriority().getValue()), "State", fields.get("State"), "StateProjectCustomField"));
        if (issue.getHandler() != null) {
            if (!issue.getHandler().getTitle().isEmpty()) {
                customFieldsArray.put(i++, this.putCustomField(issue.getHandler().getTitle(), "Assignee", "", "SingleUserIssueCustomField"));
            }
        }
        if (!issue.getFixedInVersion().isEmpty()) {
            customFieldsArray.put(i++, this.putCustomField(issue.getFixedInVersion(), "Fix versions", "", "MultiVersionIssueCustomField"));
        }
        if (!issue.getVersion().isEmpty()) {
            customFieldsArray.put(i, this.putCustomField(issue.getVersion(), "Affected versions", "", "MultiVersionIssueCustomField"));
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

        Issue<String> oldIssue = this.getIssue(issue.getId());
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
                this.deleteRequest("/api/issues/" + issue.getId() + "/comments/" + note.getId());
            }
        }

        if (!issue.getNotes().isEmpty()) {
            for (Note<String> note : issue.getNotes()) {
                JSONObject noteObject = new JSONObject();
                noteObject.put("text", note.getDescription());

                if (note.getId() == null) {
                    this.executeRequest("/api/issues/" + issue.getId() + "/comments?fields=id", noteObject.toString(), "POST");
                } else {
                    this.executeRequest("/api/issues/" + issue.getId() + "/comments/" + note.getId() + "?fields=id", noteObject.toString(), "POST");
                }
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
                this.deleteRequest("/api/issues/" + issue.getId() + "/attachments/" + attachment.getId());
            }
        }

        if (!issue.getAttachments().isEmpty()) {
            for (Attachment<String> attachment : issue.getAttachments()) {
                JSONObject attachmentObject = new JSONObject();
                attachmentObject.put("name", UUID.randomUUID().toString());
                attachmentObject.put("base64Content", Base64.encodeToString(attachment.getContent(), Base64.DEFAULT));

                if (attachment.getId() == null) {
                    this.executeRequest("/api/issues/" + issue.getId() + "/attachments?fields=id", attachmentObject.toString(), "POST");
                } else {
                    this.executeRequest("/api/issues/" + issue.getId() + "/attachments/" + attachment.getId() + "?fields=id", attachmentObject.toString(), "POST");
                }
            }
        }

        return issue.getId();
    }

    private Map<String, String> getCustomFields() throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        int status = this.executeRequest("/api/admin/customFieldSettings/customFields?fields=id,name");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                status = this.executeRequest("/api/admin/customFieldSettings/customFields/" + jsonObject.getString("id") + "/instances?fields=id,name");
                if (status == 200 || status == 201) {
                    JSONArray realId = new JSONArray(this.getCurrentMessage());
                    if (realId.length() >= 1) {
                        fields.put(jsonObject.getString("name"), realId.getJSONObject(0).getString("id"));
                    }
                }
            }
        }
        return fields;
    }

    private Map<String, String> getValuesFromLocalized() throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        int status = this.executeRequest("/api/admin/customFieldSettings/bundles/enum?fields=values(name,localizedName)");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject valuesObject = jsonArray.getJSONObject(i);
                JSONArray valuesArray = valuesObject.getJSONArray("values");
                for (int j = 0; j <= valuesArray.length() - 1; j++) {
                    JSONObject valueObject = valuesArray.getJSONObject(j);
                    fields.put(valueObject.getString("localizedName"), valueObject.getString("name"));
                }
            }
        }
        return fields;
    }

    private JSONObject putCustomField(String valueId, String name, String id, String type) throws Exception {
        JSONObject customObject = new JSONObject();
        JSONObject valueObject = new JSONObject();
        switch (type) {
            case "SingleEnumIssueCustomField":
                valueObject.put("name", valueId);
                customObject.put("value", valueObject);
                break;
            case "SingleUserIssueCustomField":
                valueObject.put("login", valueId);
                customObject.put("value", valueObject);
                break;
            case "MultiVersionIssueCustomField":
                JSONArray arrayObject = new JSONArray();
                valueObject.put("name", valueId);
                arrayObject.put(0, valueObject);
                customObject.put("values", arrayObject);
        }

        customObject.put("name", name);
        if (!id.isEmpty()) {
            customObject.put("id", id);
        }
        customObject.put("$type", type);
        return customObject;
    }

    @Override
    public void deleteIssue(String id) throws Exception {
        this.deleteRequest("/api/issues/" + id);
    }

    @Override
    public List<String> getCategories(String pid) throws Exception {
        List<String> categories = new LinkedList<>();

        return categories;
    }

    @Override
    public List<User<String>> getUsers(String pid) throws Exception {
        List<User<String>> users = new LinkedList<>();
        int status = this.executeRequest("/rest/admin/user?" + pid);
        if (status == 200 || status == 201) {
            JSONArray usersArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= usersArray.length() - 1; i++) {
                User<String> user = new User<>();
                JSONObject jsonObject = usersArray.getJSONObject(i);
                user.setTitle(jsonObject.getString("login"));
                user.setId(jsonObject.getString("ringId"));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User<String> getUser(String id) throws Exception {
        return null;
    }

    @Override
    public String insertOrUpdateUser(User<String> user) throws Exception {
        return null;
    }

    @Override
    public void deleteUser(String id) throws Exception {

    }

    @Override
    public List<CustomField<String>> getCustomFields(String pid) throws Exception {
        return null;
    }

    @Override
    public CustomField<String> getCustomField(String id) throws Exception {
        return null;
    }

    @Override
    public String insertOrUpdateCustomField(CustomField<String> user) throws Exception {
        return null;
    }

    @Override
    public void deleteCustomField(String id) throws Exception {

    }

    @Override
    public List<Tag<String>> getTags() throws Exception {
        List<Tag<String>> tags = new LinkedList<>();

        return tags;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new YoutrackPermissions(this.authentication);
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
}
