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

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.History;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.RedminePermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Redmine extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;

    public Redmine(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() throws Exception {
        return false;
    }

    @Override
    public String getTrackerVersion() {
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
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        int status = this.executeRequest("/projects/" + project_id + "/versions.json");

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
                    if (!versionObject.isNull("due_date")) {
                        Date dt = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).parse(versionObject.getString("due_date"));
                        version.setReleasedVersionAt(dt.getTime());
                    }
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
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) throws Exception {
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
        projectObject.put("id", project_id);
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            projectObject.put("name", project.getTitle());
        }
        versionObject.put("project", projectObject);

        if (version.getId() == null) {
            object.put("version", versionObject);
            this.executeRequest("/projects/" + project + "/versions.json", object.toString(), "POST");
        } else {
            versionObject.put("id", version.getId());
            object.put("version", versionObject);
            this.executeRequest("/versions/" + version.getId() + ".json", object.toString(), "PUT");
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        this.deleteRequest("/versions/" + id + ".json");
    }

    @Override
    public List<Issue<Long>> getIssues(Long pid) throws Exception {
        List<Issue<Long>> issues = new LinkedList<>();
        int status = this.executeRequest("/issues.json?project_id=" + pid);
        if (status == 200 || status == 201) {
            JSONObject resultObject = new JSONObject(this.getCurrentMessage());
            JSONArray resultArray = resultObject.getJSONArray("issues");
            for (int i = 0; i <= resultArray.length() - 1; i++) {
                JSONObject issueObject = resultArray.getJSONObject(i);
                Issue<Long> issue = new Issue<>();
                issue.setId(issueObject.getLong("id"));
                issue.setTitle(issueObject.getString("subject"));
                issue.setDescription(issueObject.getString("description"));
                issues.add(issue);
            }
        }
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();
        int status = this.executeRequest("/issues/" + id + ".json?include=attachments,journals");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage()).getJSONObject("issue");
            issue.setId(jsonObject.getLong("id"));
            issue.setTitle(jsonObject.getString("subject"));
            if (jsonObject.has("description")) {
                issue.setDescription(jsonObject.getString("description"));
            }
            issue.setSubmitDate(Converter.convertStringToDate(jsonObject.getString("created_on"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
            issue.setLastUpdated(Converter.convertStringToDate(jsonObject.getString("updated_on"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
            if (jsonObject.has("due_date")) {
                if (!jsonObject.isNull("due_date")) {
                    issue.setDueDate(Converter.convertStringToDate(jsonObject.getString("due_date"), "yyyy-MM-dd"));
                }
            }
            if (jsonObject.has("status")) {
                if (!jsonObject.isNull("status")) {
                    JSONObject statusObject = jsonObject.getJSONObject("status");
                    issue.setStatus(statusObject.getInt("id"), statusObject.getString("name"));
                }
            }
            if (jsonObject.has("priority")) {
                if (!jsonObject.isNull("priority")) {
                    JSONObject priorityObject = jsonObject.getJSONObject("priority");
                    issue.setPriority(priorityObject.getInt("id"), priorityObject.getString("name"));
                }
            }
            if (jsonObject.has("tracker")) {
                if (!jsonObject.isNull("tracker")) {
                    JSONObject trackerObject = jsonObject.getJSONObject("tracker");
                    issue.setSeverity(trackerObject.getInt("id"), trackerObject.getString("name"));
                }
            }
            if (jsonObject.has("assigned_to")) {
                if (!jsonObject.isNull("assigned_to")) {
                    JSONObject assignedObject = jsonObject.getJSONObject("assigned_to");
                    List<User<Long>> users = this.getUsers(0L);
                    for (User<Long> user : users) {
                        if (user.getId() == assignedObject.getLong("id")) {
                            issue.setHandler(user);
                            break;
                        }
                    }
                }
            }
            if (jsonObject.has("fixed_version")) {
                if (!jsonObject.isNull("fixed_version")) {
                    JSONObject versionObject = jsonObject.getJSONObject("fixed_version");
                    issue.setFixedInVersion(versionObject.getString("name"));
                }
            }
            if (jsonObject.has("is_private")) {
                if (!jsonObject.isNull("is_private")) {
                    boolean isPrivate = jsonObject.getBoolean("is_private");
                    if (isPrivate) {
                        issue.setState(50, "private");
                    } else {
                        issue.setState(10, "public");
                    }
                }
            }
            if (jsonObject.has("category")) {
                if (!jsonObject.isNull("category")) {
                    issue.setCategory(jsonObject.getJSONObject("category").getString("name"));
                }
            }


            if (jsonObject.has("journals")) {
                if (!jsonObject.isNull("journals")) {
                    JSONArray journalArray = jsonObject.getJSONArray("journals");
                    for (int i = 0; i <= journalArray.length() - 1; i++) {
                        JSONObject journalObject = journalArray.getJSONObject(i);
                        if (journalObject.has("notes")) {
                            if (!journalObject.isNull("notes")) {
                                if (journalObject.get("notes") != null) {
                                    Note<Long> note = new Note<>();
                                    note.setId(journalObject.getLong("id"));
                                    String notes = journalObject.getString("notes");
                                    if (notes.length() >= 50) {
                                        note.setTitle(notes.substring(0, 50));
                                    } else {
                                        note.setTitle(notes);
                                    }
                                    note.setDescription(notes);
                                    issue.getNotes().add(note);
                                }
                            }
                        }
                    }
                }
            }

            if (jsonObject.has("attachments")) {
                if (!jsonObject.isNull("attachments")) {
                    JSONArray attachmentArray = jsonObject.getJSONArray("attachments");
                    for (int i = 0; i <= attachmentArray.length() - 1; i++) {
                        JSONObject attachmentObject = attachmentArray.getJSONObject(i);
                        Attachment<Long> attachment = new Attachment<>();
                        attachment.setId(attachmentObject.getLong("id"));
                        attachment.setFilename(attachmentObject.getString("filename"));
                        attachment.setDownloadUrl(attachmentObject.getString("content_url"));
                        attachment.setContent(Converter.convertStringToByteArray(attachment.getDownloadUrl()));
                        issue.getAttachments().add(attachment);
                    }
                }
            }
        }
        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        JSONObject requestObject = new JSONObject();
        JSONObject issueObject = new JSONObject();
        issueObject.put("project_id", project_id);
        if (issue.getSeverity() != null) {
            issueObject.put("tracker_id", issue.getSeverity().getKey());
            issueObject.put("tracker", issue.getSeverity().getValue());
        }
        if (issue.getStatus() != null) {
            issueObject.put("status_id", issue.getStatus().getKey());
        }
        if (issue.getPriority() != null) {
            issueObject.put("priority_id", issue.getPriority().getKey());
        }
        issueObject.put("subject", issue.getTitle());
        issueObject.put("description", issue.getDescription());
        if (issue.getState() != null) {
            issueObject.put("is_private", issue.getState().getKey() != 10);
        }
        if (issue.getHandler() != null) {
            issueObject.put("assigned_to_id", issue.getHandler().getId());
        }
        if (!issue.getCategory().equals("")) {
            issueObject.put("category_id", this.getCategoryId(issue.getCategory(), project_id));
        }
        if (!issue.getVersion().equals("")) {
            List<Version<Long>> versions = this.getVersions("", project_id);
            for (Version<Long> version : versions) {
                if (version.getTitle().equals(issue.getVersion())) {
                    issueObject.put("fixed_version_id", version.getId());
                }
            }
        }
        requestObject.put("issue", issueObject);

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/issues/" + issue.getId() + ".json", requestObject.toString(), "POST");
        } else {
            status = this.executeRequest("/issues.json", requestObject.toString(), "POST");
        }

        if (status == 200 || status == 201) {
            String content = this.getCurrentMessage();

            if (issue.getId() == null) {
                JSONObject result = new JSONObject(content);
                if (result.has("issue")) {
                    JSONObject resultProject = result.getJSONObject("issue");
                    issue.setId(resultProject.getLong("id"));
                }
            }
        }

        if (issue.getId() != null) {
            if (!issue.getNotes().isEmpty()) {
                for (Note<Long> note : issue.getNotes()) {
                    if (note.getId() == null) {
                        JSONObject jsonObject = new JSONObject();
                        JSONObject issueNoteObject = new JSONObject();
                        issueNoteObject.put("notes", note.getDescription());
                        jsonObject.put("issue", issueNoteObject);
                        this.executeRequest("/issues/" + issue.getId() + ".json", jsonObject.toString(), "PUT");
                    }
                }
            }

            if (!issue.getAttachments().isEmpty()) {

            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/issues/" + id + ".json");
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
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) throws Exception {

    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) throws Exception {

    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/users.json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("users");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject userobject = jsonArray.getJSONObject(i);
                User<Long> user = new User<>();
                user.setId(userobject.getLong("id"));
                user.setTitle(userobject.getString("login"));
                user.setRealName(userobject.getString("firstname") + " " + userobject.getString("lastname"));
                user.setEmail(userobject.getString("mail"));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void deleteUser(Long id, Long project_id) throws Exception {

    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) throws Exception {
        return null;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateCustomField(CustomField<Long> user, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) throws Exception {

    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public List<String> getCategories(Long pid) throws Exception {
        List<String> categories = new LinkedList<>();
        int status = this.executeRequest("/projects/" + pid + "/issue_categories.json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("issue_categories");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject category = jsonArray.getJSONObject(i);
                categories.add(category.getString("name"));
            }
        }
        return categories;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new RedminePermissions(this.authentication);
    }

    private Long getCategoryId(String name, Long pid) throws Exception {
        int status = this.executeRequest("/projects/" + pid + "/issue_categories.json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("issue_categories");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject category = jsonArray.getJSONObject(i);
                if (category.getString("name").equals(name)) {
                    return category.getLong("id");
                }
            }
        }
        return 0L;
    }

    private Project<Long> jsonObjectToProject(JSONObject obj) {
        Project<Long> project = new Project<>();
        try {
            project.setId((long) obj.getInt("id"));
            project.setTitle(obj.getString("name"));
            project.setAlias(obj.getString("identifier"));
            if (obj.has("description")) {
                project.setDescription(obj.getString("description"));
            }
            if (obj.has("homepage")) {
                project.setWebsite(obj.getString("homepage"));
            }
            project.setPrivateProject(!obj.getBoolean("is_public"));
            project.setCreatedAt(Converter.convertStringToDate(obj.getString("created_on"), "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime());
            project.setUpdatedAt(Converter.convertStringToDate(obj.getString("updated_on"), "yyyy-MM-dd'T'HH:mm:ss'Z'").getTime());
        } catch (Exception ex) {
            Log.e("error", "error", ex);
        }
        return project;
    }
}
