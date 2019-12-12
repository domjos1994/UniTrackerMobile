/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackerlibrary.services.tracker;

import android.util.Log;

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.model.issues.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.RedminePermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.customwidgets.utils.Converter;

public final class Redmine extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;
    private final static String DATE_FORMAT = "yyyy-MM-dd";
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public Redmine(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/users/current.json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject userObject = jsonObject.getJSONObject("user");
            return this.authentication.getUserName().equals(userObject.getString("login"));
        }
        return false;
    }

    @Override
    public String getTrackerVersion() {
        return "";
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
                        Date dt = new SimpleDateFormat(Redmine.DATE_FORMAT, Locale.GERMAN).parse(versionObject.getString("due_date"));
                        if(dt!=null) {
                            version.setReleasedVersionAt(dt.getTime());
                        }
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
            versionObject.put("due_date", new SimpleDateFormat(Redmine.DATE_FORMAT, Locale.GERMAN).format(date));
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
            this.executeRequest("/projects/" + project_id + "/versions.json", object.toString(), "POST");
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
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) throws Exception {
        if(filter==null) {
            filter = IssueFilter.all;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.unresolved) {
                filterQuery = "&status_id=open";
            } else {
                filterQuery = "&status_id=closed,resolved";
            }
        }

        int status = this.executeRequest("/issues.json?project_id=" + project_id + filterQuery + "&limit=1");
        if (status == 200 || status == 201) {
            return new JSONObject(this.getCurrentMessage()).getLong("total_count");
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
    public List<Issue<Long>> getIssues(Long pid, int page, int numberOfItems, IssueFilter filter) throws Exception {
        String limitation = "";
        if (numberOfItems != -1) {
            limitation = "&limit=" + numberOfItems + "&offset=" + ((page - 1) * numberOfItems);
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.unresolved) {
                filterQuery = "&status_id=open";
            } else {
                filterQuery = "&status_id=closed,resolved";
            }
        }

        List<Issue<Long>> issues = new LinkedList<>();
        int status = this.executeRequest("/issues.json?project_id=" + pid + limitation + filterQuery);
        if (status == 200 || status == 201) {
            JSONObject resultObject = new JSONObject(this.getCurrentMessage());
            JSONArray resultArray = resultObject.getJSONArray("issues");
            for (int i = 0; i <= resultArray.length() - 1; i++) {
                JSONObject issueObject = resultArray.getJSONObject(i);
                Issue<Long> issue = new Issue<>();
                issue.setId(issueObject.getLong("id"));
                issue.setTitle(issueObject.getString("subject"));
                issue.setDescription(issueObject.getString("description"));

                if (issueObject.has("fixed_version")) {
                    if (!issueObject.isNull("fixed_version")) {
                        JSONObject versionObject = issueObject.getJSONObject("fixed_version");
                        issue.getHints().put("version", versionObject.getString("name"));
                    }
                }
                if (issueObject.has("is_private")) {
                    if (!issueObject.isNull("is_private")) {
                        boolean isPrivate = issueObject.getBoolean("is_private");
                        String state;
                        if (isPrivate) {
                            state = "private";
                        } else {
                            state = "public";
                        }
                        issue.getHints().put("view", state);
                    }
                }
                if (issueObject.has("status")) {
                    if (!issueObject.isNull("status")) {
                        JSONObject statusObject = issueObject.getJSONObject("status");
                        issue.getHints().put("status", statusObject.getString("name"));
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
        int status = this.executeRequest("/issues/" + id + ".json?include=attachments,journals");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage()).getJSONObject("issue");
            issue.setId(jsonObject.getLong("id"));
            issue.setTitle(jsonObject.getString("subject"));
            if (jsonObject.has("description")) {
                issue.setDescription(jsonObject.getString("description"));
            }
            issue.setSubmitDate(Converter.convertStringToDate(jsonObject.getString("created_on"), Redmine.DATE_TIME_FORMAT));
            issue.setLastUpdated(Converter.convertStringToDate(jsonObject.getString("updated_on"), Redmine.DATE_TIME_FORMAT));
            if (jsonObject.has("due_date")) {
                if (!jsonObject.isNull("due_date")) {
                    issue.setDueDate(Converter.convertStringToDate(jsonObject.getString("due_date"), Redmine.DATE_FORMAT));
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
                    issue.setTargetVersion(versionObject.getString("name"));
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
                                journalObject.get("notes");
                                Note<Long> note = new Note<>();
                                note.setId(journalObject.getLong("id"));
                                String notes = journalObject.getString("notes");
                                if (notes.length() >= 50) {
                                    note.setTitle(notes.substring(0, 50));
                                } else {
                                    note.setTitle(notes);
                                }
                                note.setDescription(notes);
                                if (note.getTitle().trim().isEmpty()) {
                                    continue;
                                }
                                issue.getNotes().add(note);
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

            if (jsonObject.has("custom_fields")) {
                if (!jsonObject.isNull("custom_fields")) {
                    JSONArray customFieldArray = jsonObject.getJSONArray("custom_fields");
                    List<CustomField<Long>> customFields = this.getCustomFields(project_id);
                    if (customFields != null) {
                        for (int i = 0; i <= customFieldArray.length() - 1; i++) {
                            JSONObject fieldObject = customFieldArray.getJSONObject(i);
                            long field_id = fieldObject.getLong("id");
                            for (CustomField<Long> customField : customFields) {
                                if (customField.getId() == field_id) {
                                    if (fieldObject.has("value")) {
                                        if (!fieldObject.isNull("value")) {
                                            issue.getCustomFields().put(customField, fieldObject.getString("value"));
                                            break;
                                        }
                                    }
                                    issue.getCustomFields().put(customField, "");
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
    @SuppressWarnings("unchecked")
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
        if (issue.getDueDate() != null) {
            issueObject.put("due_date", new SimpleDateFormat(Redmine.DATE_FORMAT, Locale.GERMAN).format(issue.getDueDate()));
        }
        if (!issue.getTargetVersion().equals("")) {
            List<Version<Long>> versions = this.getVersions("", project_id);
            for (Version<Long> version : versions) {
                if (version.getTitle().equals(issue.getTargetVersion())) {
                    issueObject.put("fixed_version_id", version.getId());
                }
            }
        }
        if (!issue.getCustomFields().isEmpty()) {
            JSONArray customFieldArray = new JSONArray();
            for (Map.Entry<CustomField<Long>, String> entry : issue.getCustomFields().entrySet()) {
                JSONObject customFieldObject = new JSONObject();
                customFieldObject.put("id", entry.getKey().getId());
                customFieldObject.put("name", entry.getKey().getTitle());
                customFieldObject.put("value", entry.getValue());
                customFieldArray.put(customFieldObject);
            }
            issueObject.put("custom_fields", customFieldArray);
        }

        requestObject.put("issue", issueObject);

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/issues/" + issue.getId() + ".json", requestObject.toString(), "PUT");
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
                for (DescriptionObject descriptionObject : issue.getNotes()) {
                    if (descriptionObject instanceof Note) {
                        Note<Long> note = (Note<Long>) descriptionObject;
                        if (note.getId() == null) {
                            JSONObject jsonObject = new JSONObject();
                            JSONObject issueNoteObject = new JSONObject();
                            issueNoteObject.put("notes", note.getDescription());
                            jsonObject.put("issue", issueNoteObject);
                            this.executeRequest("/issues/" + issue.getId() + ".json", jsonObject.toString(), "PUT");
                        }
                    }
                }
            }

            if (!issue.getAttachments().isEmpty()) {
                for (DescriptionObject descriptionObject : issue.getAttachments()) {
                    if (descriptionObject instanceof Attachment) {
                        Attachment<Long> attachment = (Attachment<Long>) descriptionObject;
                        if (attachment.getId() == null) {
                            attachment.setFilename(attachment.getFilename().replace(":", "_").replace("/", "_"));
                            status = this.executeRequest("/uploads.json?filename=" + attachment.getFilename(), attachment.getContent(), "POST");
                            if (status == 200 || status == 201) {
                                JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                                JSONObject uploadObject = jsonObject.getJSONObject("upload");
                                String token = uploadObject.getString("token");

                                issueObject = new JSONObject();
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("id", issue.getId());
                                jsonObj.put("subject", issue.getTitle());
                                issueObject.put("project_id", project_id);
                                if (issue.getSeverity() != null) {
                                    issueObject.put("tracker_id", issue.getSeverity().getKey());
                                    issueObject.put("tracker", issue.getSeverity().getValue());
                                }
                                if (issue.getStatus() != null) {
                                    issueObject.put("status_id", issue.getStatus().getKey());
                                }
                                JSONArray jsonArray = new JSONArray();
                                JSONObject uploadObj = new JSONObject();
                                uploadObj.put("token", token);
                                uploadObj.put("filename", attachment.getFilename());
                                uploadObj.put("content_type", "");
                                jsonArray.put(uploadObj);
                                jsonObj.put("uploads", jsonArray);
                                issueObject.put("issue", jsonObj);
                                this.executeRequest("/issues/" + issue.getId() + ".json", issueObject.toString(), "PUT");
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/issues/" + id + ".json");
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) {
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) {
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) {
    }

    @Override
    public List<Relationship<Long>> getBugRelations(Long issue_id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<Long> relationship, Long issue_id, Long project_id) throws Exception {

    }

    @Override
    public void deleteBugRelation(Relationship<Long> relationship, Long issue_id, Long project_id) throws Exception {

    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/users.json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("users");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject userObject = jsonArray.getJSONObject(i);
                User<Long> user = new User<>();
                user.setId(userObject.getLong("id"));
                if (userObject.has("login")) {
                    user.setTitle(userObject.getString("login"));
                }

                user.setRealName(
                        userObject.getString("firstname") + " " +
                                userObject.getString("lastname")
                );
                if (userObject.has("mail")) {
                    user.setEmail(userObject.getString("mail"));
                }
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        User<Long> user = new User<>();
        int status = this.executeRequest("/users/" + id + ".json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject userObject = jsonObject.getJSONObject("user");
            user.setId(userObject.getLong("id"));
            if (userObject.has("login")) {
                user.setTitle(userObject.getString("login"));
            }

            user.setRealName(
                    userObject.getString("firstname") + " " +
                            userObject.getString("lastname")
            );
            if (userObject.has("mail")) {
                user.setEmail(userObject.getString("mail"));
            }
        }
        return user;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject userObject = new JSONObject();
        userObject.put("login", user.getTitle());
        String[] spl = user.getRealName().split(" ");
        if (spl.length == 2) {
            userObject.put("firstname", spl[0]);
            userObject.put("lastname", spl[1]);
        }
        if (!user.getEmail().isEmpty()) {
            userObject.put("mail", user.getEmail());
        }
        userObject.put("password", user.getPassword());
        jsonObject.put("user", userObject);

        if (user.getId() != null) {
            this.executeRequest("/users/" + user.getId() + ".json", jsonObject.toString(), "PUT");
        } else {
            this.executeRequest("/users.json", jsonObject.toString(), "POST");
        }

    }

    @Override
    public void deleteUser(Long id, Long project_id) throws Exception {
        this.deleteRequest("/users/" + id + ".json");
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) throws Exception {
        List<CustomField<Long>> customFields = new LinkedList<>();
        int status = this.executeRequest("/custom_fields.json");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray customFieldArray = jsonObject.getJSONArray("custom_fields");
            for (int i = 0; i <= customFieldArray.length() - 1; i++) {
                JSONObject fieldObject = customFieldArray.getJSONObject(i);
                if (fieldObject.getString("customized_type").equals("issue")) {
                    CustomField<Long> customField = new CustomField<>();
                    customField.setId(fieldObject.getLong("id"));
                    customField.setTitle(fieldObject.getString("name"));
                    if (fieldObject.has("description")) {
                        if (!fieldObject.isNull("description")) {
                            customField.setDescription(fieldObject.getString("description"));
                        }
                    }
                    if (fieldObject.has("min_length")) {
                        if (!fieldObject.isNull("min_length")) {
                            customField.setMinLength(fieldObject.getInt("min_length"));
                        }
                    }
                    if (fieldObject.has("max_length")) {
                        if (!fieldObject.isNull("max_length")) {
                            customField.setMaxLength(fieldObject.getInt("max_length"));
                        }
                    }
                    customField.setNullable(fieldObject.getBoolean("is_required"));
                    if (fieldObject.has("default_value")) {
                        if (!fieldObject.isNull("default_value")) {
                            customField.setDefaultValue(fieldObject.getString("default_value"));
                        }
                    }
                    StringBuilder possibleValues = new StringBuilder();
                    if (fieldObject.has("possible_values")) {
                        if (!fieldObject.isNull("possible_values")) {
                            JSONArray array = fieldObject.getJSONArray("possible_values");
                            for (int j = 0; j <= array.length() - 1; j++) {
                                JSONObject possibleObject = array.getJSONObject(j);
                                possibleValues.append(possibleObject.getString("value"));
                                possibleValues.append("|");
                            }
                        }
                    }
                    customField.setPossibleValues(possibleValues.toString());
                    String format = fieldObject.getString("field_format");
                    switch (format.toLowerCase()) {
                        case "string":
                            customField.setType(CustomField.Type.TEXT);
                            break;
                        case "text":
                            customField.setType(CustomField.Type.TEXT_AREA);
                            break;
                        case "int":
                            customField.setType(CustomField.Type.NUMBER);
                            break;
                        case "date":
                            customField.setType(CustomField.Type.DATE);
                            break;
                        case "bool":
                            customField.setType(CustomField.Type.CHECKBOX);
                            break;
                        case "list":
                            customField.setType(CustomField.Type.LIST);
                            break;
                    }

                    customFields.add(customField);
                }
            }
        }

        return customFields;
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
    public List<Tag<Long>> getTags(Long project_id) {
        return new LinkedList<>();
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
    public List<String> getCategories(Long pid) {
        List<String> categories = new LinkedList<>();
        try {
            int status = this.executeRequest("/projects/" + pid + "/issue_categories.json");
            if (status == 200 || status == 201) {
                JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                JSONArray jsonArray = jsonObject.getJSONArray("issue_categories");
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject category = jsonArray.getJSONObject(i);
                    categories.add(category.getString("name"));
                }
            }
        } catch (Exception ignored) {
        }
        return categories;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new RedminePermissions(this.authentication);
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

        JSONObject categoryObject = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        categoryObject.put("issue_category", jsonObject);
        status = this.executeRequest("/projects/" + pid + "/issue_categories.json", categoryObject.toString(), "POST");
        if (status == 201) {
            JSONObject jsonObject1 = new JSONObject(this.getCurrentMessage());
            return jsonObject1.getJSONObject("issue_category").getLong("id");
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

            Date dt = Converter.convertStringToDate(obj.getString("created_on"), Redmine.DATE_TIME_FORMAT);

            if (dt != null) {
                project.setCreatedAt(dt.getTime());
                project.setUpdatedAt(dt.getTime());
            }
        } catch (Exception ex) {
            Log.e("error", "error", ex);
        }
        return project;
    }
}
