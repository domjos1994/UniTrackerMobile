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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.History;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.model.issues.Profile;
import de.domjos.unitrackerlibrary.model.issues.Tag;
import de.domjos.unitrackerlibrary.model.issues.User;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.BacklogPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.unitrackerlibrary.utils.Converter;

public final class Backlog extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static String DATE_FORMAT = "yyyy-MM-dd";
    private final String authParams;

    public Backlog(Authentication authentication) {
        super(authentication);
        this.authentication = authentication;
        this.authParams = "apiKey=" + this.authentication.getAPIKey();
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/api/v2/users/myself?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            if (jsonObject.has("userId")) {
                return !jsonObject.getString("userId").isEmpty();
            }
        }
        return false;
    }

    @Override
    public String getTrackerVersion() {
        return "v2";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/api/v2/projects?" + this.authParams + "&all=true");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Project<Long> project = new Project<>();
                project.setId(jsonObject.getLong("id"));
                project.setAlias(jsonObject.getString("projectKey"));
                project.setTitle(jsonObject.getString("name"));
                project.setEnabled(!jsonObject.getBoolean("archived"));
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        Project<Long> project = new Project<>();
        int status = this.executeRequest("/api/v2/projects/" + id + "?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            project.setId(jsonObject.getLong("id"));
            project.setAlias(jsonObject.getString("projectKey"));
            project.setTitle(jsonObject.getString("name"));
            project.setEnabled(!jsonObject.getBoolean("archived"));
        }
        return project;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", project.getAlias().replace(" ", "_").replace("-", "_").toUpperCase());
        jsonObject.put("name", project.getTitle());
        jsonObject.put("chartEnabled", false);
        jsonObject.put("subtaskingEnabled", false);
        jsonObject.put("projectLeaderCanEditProjectLeader", false);
        jsonObject.put("textFormattingRule", "markdown");

        if (project.getId() != null) {
            jsonObject.put("archived", !project.isEnabled());
            this.executeRequest("/api/v2/projects/" + project.getId() + "?" + this.authParams, jsonObject.toString(), "PATCH");
        } else {
            int status = this.executeRequest("/api/v2/projects?" + this.authParams, jsonObject.toString(), "POST");
            if (status == 201) {
                JSONObject response = new JSONObject(this.getCurrentMessage());
                project.setId(response.getLong("id"));
            }
        }
        return project.getId();
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/api/v2/projects/" + id + "?" + this.authParams);
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        int status = this.executeRequest("/api/v2/projects/" + project_id + "/versions?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Version<Long> version = new Version<>();
                version.setId(jsonObject.getLong("id"));
                version.setTitle(jsonObject.getString("name"));
                version.setDescription(jsonObject.getString("description"));
                version.setReleasedVersion(false);
                if (jsonObject.has("releaseDueDate")) {
                    if (!jsonObject.isNull("releaseDueDate")) {
                        Date dt = Converter.convertStringToDate(jsonObject.getString("releaseDueDate"), Backlog.DATE_FORMAT);
                        if (dt != null) {
                            version.setReleasedVersionAt(dt.getTime());
                            version.setReleasedVersion(dt.before(new Date()));
                        }
                    }
                }
                version.setDeprecatedVersion(jsonObject.getBoolean("archived"));
                versions.add(version);
            }
        }
        return versions;
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(Backlog.DATE_FORMAT, Locale.GERMAN);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", version.getTitle());
        jsonObject.put("description", version.getDescription());

        if (version.getReleasedVersionAt() != 0L) {
            Date releaseDate = new Date();
            releaseDate.setTime(version.getReleasedVersionAt());
            jsonObject.put("releaseDueDate", sdf.format(releaseDate));
        }

        if (version.getId() != null) {
            jsonObject.put("archived", version.isDeprecatedVersion());
            this.executeRequest("/api/v2/projects/" + project_id + "/versions/" + version.getId() + "?" + this.authParams, jsonObject.toString(), "PATCH");
        } else {
            this.executeRequest("/api/v2/projects/" + project_id + "/versions?" + this.authParams, jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v2/projects/" + project_id + "/versions/" + id + "?" + this.authParams);
    }

    @Override
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) {
        return 0;
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
        List<Issue<Long>> issues = new LinkedList<>();

        String pagination = "";
        if (numberOfItems != -1) {
            pagination = "&offset=" + (page * numberOfItems) + "&count=" + numberOfItems;
        }

        String query = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                query = "&statusId[]=3&statusId[]=4";
            } else {
                query = "&statusId[]=1&statusId[]=2";
            }
        }

        int status = this.executeRequest("/api/v2/issues?" + this.authParams + pagination + query);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Issue<Long> issue = new Issue<>();
                issue.setId(jsonObject.getLong("id"));
                issue.setTitle(jsonObject.getString("summary"));
                issue.setDescription(jsonObject.getString("description"));
                issues.add(issue);
            }
        }

        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();

        int status = this.executeRequest("/api/v2/issues/" + id + "?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            issue.setId(jsonObject.getLong("id"));
            issue.setTitle(jsonObject.getString("summary"));
            issue.setDescription(jsonObject.getString("description"));
            issue.setSubmitDate(Converter.convertStringToDate(jsonObject.getString("created"), Backlog.DATE_TIME_FORMAT));
            issue.setLastUpdated(Converter.convertStringToDate(jsonObject.getString("updated"), Backlog.DATE_TIME_FORMAT));

            if (!jsonObject.isNull("issueType")) {
                JSONObject typeObject = jsonObject.getJSONObject("issueType");
                issue.setSeverity(typeObject.getInt("id"), typeObject.getString("name"));
            }

            if (!jsonObject.isNull("priority")) {
                JSONObject priorityObject = jsonObject.getJSONObject("priority");
                issue.setPriority(priorityObject.getInt("id"), priorityObject.getString("name"));
            }

            if (!jsonObject.isNull("resolution")) {
                JSONObject resolutionObject = jsonObject.getJSONObject("resolution");
                issue.setStatus(resolutionObject.getInt("id"), resolutionObject.getString("name"));
            }

            if (!jsonObject.isNull("status")) {
                JSONObject statusObject = jsonObject.getJSONObject("status");
                issue.setStatus(statusObject.getInt("id"), statusObject.getString("name"));
            }

            if (!jsonObject.isNull("category")) {
                if (jsonObject.getJSONArray("category").length() >= 1) {
                    JSONObject categoryObject = jsonObject.getJSONArray("category").getJSONObject(0);
                    issue.setCategory(categoryObject.getString("name"));
                }
            }

            if (!jsonObject.isNull("assignee")) {
                JSONObject assignee = jsonObject.getJSONObject("assignee");
                issue.setHandler(this.getUser(assignee.getLong("id"), project_id));
            }

            if (!jsonObject.isNull("versions")) {
                if (jsonObject.getJSONArray("versions").length() >= 1) {
                    JSONObject versionsObject = jsonObject.getJSONArray("versions").getJSONObject(0);
                    issue.setVersion(versionsObject.getString("name"));
                }
            }

            if (!jsonObject.isNull("dueDate")) {
                String dueDate = jsonObject.getString("dueDate");
                issue.setDueDate(Converter.convertStringToDate(dueDate, Backlog.DATE_TIME_FORMAT));
            }

            if (!jsonObject.isNull("customFields")) {
                JSONArray customFieldArray = jsonObject.getJSONArray("customFields");
                List<CustomField<Long>> customFields = this.getCustomFields(project_id);
                for (int i = 0; i <= customFieldArray.length() - 1; i++) {
                    JSONObject customFieldObject = customFieldArray.getJSONObject(i);
                    for (CustomField<Long> customField : customFields) {
                        if (customField.getId() == customFieldObject.getLong("id")) {
                            issue.getCustomFields().put(customField, customFieldObject.getString("value"));
                            break;
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
    @SuppressWarnings("unchecked")
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(Backlog.DATE_FORMAT, Locale.GERMAN);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("summary", issue.getTitle());
        jsonObject.put("description", issue.getDescription());
        if (issue.getDueDate() != null) {
            jsonObject.put("dueDate", sdf.format(issue.getDueDate()));
        }
        jsonObject.put("issueTypeId", this.getIdOfIssueType(project_id, issue.getSeverity().getValue()));
        jsonObject.put("priorityId", issue.getPriority().getKey());

        if (issue.getHandler() != null) {
            jsonObject.put("assigneeId", issue.getHandler().getId());
        }

        if (!issue.getCategory().isEmpty()) {
            String category = issue.getCategory();
            long id = this.getCategoryOrAdd(project_id, category);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(id);
            jsonObject.put("categoryId", jsonArray);
        }

        if (!issue.getVersion().isEmpty()) {
            String version = issue.getVersion();
            List<Version<Long>> versions = this.getVersions("", project_id);
            for (Version<Long> tmp : versions) {
                if (tmp.getTitle().equals(version)) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(tmp.getId());
                    jsonObject.put("versionId", jsonArray);
                    break;
                }
            }
        }

        if (!issue.getCustomFields().isEmpty()) {
            for (Map.Entry<CustomField<Long>, String> entry : issue.getCustomFields().entrySet()) {
                JSONObject fieldObject = new JSONObject();
                fieldObject.put("value", entry.getValue());
                jsonObject.put("customField_" + entry.getKey().getId(), fieldObject);
            }
        }

        if (issue.getId() != null) {
            for (Attachment<Long> attachment : this.getAttachments(Long.parseLong(String.valueOf(issue.getId())), project_id)) {
                this.deleteAttachment(attachment.getId(), Long.parseLong(String.valueOf(issue.getId())), project_id);
            }
        }

        if (!issue.getAttachments().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (DescriptionObject descriptionObject : issue.getAttachments()) {
                if (descriptionObject instanceof Attachment) {
                    Attachment<Long> attachment = (Attachment<Long>) descriptionObject;
                    jsonArray.put(this.uploadAttachment(attachment));
                }
            }
            jsonObject.put("attachmentId", jsonArray);
        }

        if (issue.getHandler() != null) {
            jsonObject.put("assigneeId", issue.getHandler().getId());
        }

        int status;
        if (issue.getId() == null) {
            jsonObject.put("projectId", project_id);
            status = this.executeRequest("/api/v2/issues?" + this.authParams, jsonObject.toString(), "POST");
        } else {
            jsonObject.put("statusId", issue.getStatus().getKey());
            jsonObject.put("resolutionId", issue.getResolution().getKey());
            jsonObject.put("comment", "Updated by UniTrackerMobileApp");
            status = this.executeRequest("/api/v2/issues/" + issue.getId() + "?" + this.authParams, jsonObject.toString(), "PATCH");
        }

        if (status == 200 || status == 201) {
            JSONObject response = new JSONObject(this.getCurrentMessage());
            if (issue.getId() == null) {
                issue.setId(response.getLong("id"));
            }
            issue.setId(Long.parseLong(String.valueOf(issue.getId())));

            if (!issue.getNotes().isEmpty()) {
                List<Note<Long>> oldNotes = this.getNotes(issue.getId(), project_id);
                for (Note<Long> oldNote : oldNotes) {
                    boolean contains = false;
                    for (Note<Long> note : issue.getNotes()) {
                        if (oldNote.getId().equals(note.getId())) {
                            contains = true;
                            break;
                        }
                    }

                    if (!contains) {
                        this.deleteNote(oldNote.getId(), issue.getId(), project_id);
                    }
                }

                for (Note<Long> note : issue.getNotes()) {
                    this.insertOrUpdateNote(note, issue.getId(), project_id);
                }
            }
        }
    }

    private Long uploadAttachment(Attachment<Long> attachment) {
        long id = 0L;
        try {
            JSONObject jsonObject = new JSONObject();
            int status = this.addMultiPart("/api/v2/space/attachment?" + this.authParams, jsonObject.toString(), attachment.getContentType(), attachment.getContent(), "POST");


            if (status == 200 || status == 201) {
                JSONObject response = new JSONObject(this.getCurrentMessage());
                id = response.getLong("id");
            }
        } catch (Exception ignored) {
        }
        return id;
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v2/issues/" + id + "?" + this.authParams);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) throws Exception {
        List<Note<Long>> notes = new LinkedList<>();

        int status = this.executeRequest("/api/v2/issues/" + issue_id + "/comments?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Note<Long> note = new Note<>();
                note.setId(jsonObject.getLong("id"));
                String content = jsonObject.getString("content");
                if (!content.equals("null")) {
                    if (content.length() >= 50) {
                        note.setTitle(content.substring(0, 50));
                    } else {
                        note.setTitle(content);
                    }
                    note.setDescription(content);
                    note.setSubmitDate(Converter.convertStringToDate(jsonObject.getString("created"), Backlog.DATE_TIME_FORMAT));
                    note.setLastUpdated(Converter.convertStringToDate(jsonObject.getString("updated"), Backlog.DATE_TIME_FORMAT));
                    notes.add(note);
                }
            }
        }

        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", note.getDescription());

        if (note.getId() != null) {
            this.executeRequest("/api/v2/issues/" + issue_id + "/comments/" + note.getId() + "?" + this.authParams, jsonObject.toString(), "PATCH");
        } else {
            this.executeRequest("/api/v2/issues/" + issue_id + "/comments?" + this.authParams, jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {
        this.deleteRequest("/api/v2/issues/" + issue_id + "/comments/" + id + "?" + this.authParams);
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) throws Exception {
        List<Attachment<Long>> attachments = new LinkedList<>();

        int status = this.executeRequest("/api/v2/issues/" + issue_id + "/attachments?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Attachment<Long> attachment = new Attachment<>();
                attachment.setId(jsonObject.getLong("id"));
                attachment.setFilename(jsonObject.getString("name"));

                attachments.add(attachment);
            }
        }

        return attachments;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) throws Exception {
        this.deleteRequest("/api/v2/issues/" + issue_id + "/attachments/" + id + "?" + this.authParams);
    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/api/v2/users?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                User<Long> user = new User<>();
                user.setId(jsonObject.getLong("id"));
                user.setTitle(jsonObject.getString("name"));
                user.setRealName(jsonObject.getString("name"));
                user.setEmail(jsonObject.getString("mailAddress"));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        User<Long> user = new User<>();
        int status = this.executeRequest("/api/v2/users/" + id + "?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            user.setId(jsonObject.getLong("id"));
            user.setTitle(jsonObject.getString("name"));
            user.setRealName(jsonObject.getString("name"));
            user.setEmail(jsonObject.getString("mailAddress"));
        }
        return user;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", user.getTitle());
        jsonObject.put("password", user.getPassword());
        jsonObject.put("mailAddress", user.getEmail());
        jsonObject.put("roleType", "Administrator");

        if (user.getId() == null) {
            jsonObject.put("userId", UUID.randomUUID().toString());
            this.executeRequest("/api/v2/users?" + this.authParams, jsonObject.toString(), "POST");
        } else {
            this.executeRequest("/api/v2/users/" + user.getId() + "?" + this.authParams, jsonObject.toString(), "PATCH");
        }
    }

    @Override
    public void deleteUser(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v2/users/" + id + "?" + this.authParams);
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) throws Exception {
        List<CustomField<Long>> customFields = new LinkedList<>();
        int status = this.executeRequest("/api/v2/projects/" + project_id + "/customFields?" + this.authParams);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CustomField<Long> customField = new CustomField<>();
                customField.setId(jsonObject.getLong("id"));
                customField.setTitle(jsonObject.getString("name"));
                customField.setDescription(jsonObject.getString("description"));
                customField.setNullable(jsonObject.getBoolean("required"));

                StringBuilder defaultValues = new StringBuilder();
                if (jsonObject.has("items")) {
                    JSONArray itemsArray = jsonObject.getJSONArray("items");
                    for (int j = 0; j <= itemsArray.length() - 1; j++) {
                        JSONObject itemsObject = itemsArray.getJSONObject(j);
                        defaultValues.append(itemsObject.getString("name")).append("|");
                    }
                }

                switch (jsonObject.getInt("typeId")) {
                    case 1:
                        customField.setType(CustomField.Type.TEXT);
                        break;
                    case 2:
                        customField.setType(CustomField.Type.TEXT_AREA);
                        break;
                    case 3:
                        customField.setType(CustomField.Type.NUMBER);
                        customField.setMinLength(jsonObject.getInt("min"));
                        customField.setMaxLength(jsonObject.getInt("max"));
                        customField.setDefaultValue(jsonObject.getString("initialValue"));
                        break;
                    case 4:
                        customField.setType(CustomField.Type.DATE);
                        break;
                    case 5:
                        customField.setType(CustomField.Type.LIST);
                        customField.setPossibleValues(defaultValues.toString());
                        break;
                    case 6:
                        customField.setType(CustomField.Type.MULTI_SELECT_LIST);
                        customField.setPossibleValues(defaultValues.toString());
                        break;
                }
                customFields.add(customField);
            }
        }
        return customFields;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> customField, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", customField.getTitle());
        jsonObject.put("description", customField.getDescription());
        jsonObject.put("required", !customField.isNullable());
        switch (customField.getType()) {
            case TEXT:
                jsonObject.put("typeId", 1);
                break;
            case TEXT_AREA:
                jsonObject.put("typeId", 2);
                break;
            case NUMBER:
                jsonObject.put("min", customField.getMinLength());
                jsonObject.put("max", customField.getMaxLength());
                jsonObject.put("initialValue", customField.getDefaultValue());
                jsonObject.put("typeId", 3);
                break;
            case DATE:
                jsonObject.put("typeId", 4);
                JSONArray jsonArray = new JSONArray();
                for (String item : customField.getPossibleValues().split("\\|")) {
                    if (!item.isEmpty()) {
                        jsonArray.put(item);
                    }
                }
                jsonObject.put("items", jsonArray);
                break;
            case LIST:
                jsonObject.put("typeId", 5);
                JSONArray array = new JSONArray();
                for (String item : customField.getPossibleValues().split("\\|")) {
                    if (!item.isEmpty()) {
                        array.put(item);
                    }
                }
                jsonObject.put("items", array);
                break;
        }

        if (customField.getId() != null) {
            jsonObject.remove("typeId");
            this.executeRequest("/api/v2/projects/" + project_id + "/customFields/" + customField.getId() + "?" + this.authParams, jsonObject.toString(), "PATCH");
        } else {
            this.executeRequest("/api/v2/projects/" + project_id + "/customFields?" + this.authParams, jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v2/projects/" + project_id + "/customFields/" + id + "?" + this.authParams);
    }

    @Override
    public List<String> getCategories(Long project_id) throws Exception {
        List<String> categories = new LinkedList<>();

        int status = this.executeRequest("/api/v2/projects/" + project_id + "/categories?" + this.authParams);
        if (status == 200 || status == 201) {

            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                categories.add(jsonObject.getString("name"));
            }
        }

        return categories;
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        List<History<Long>> histories = new LinkedList<>();

        int status = this.executeRequest("/api/v2/projects/" + project_id + "/activities?" + this.authParams + "&activityTypeId[]=1&activityTypeId[]=2&activityTypeId[]=3&activityTypeId[]=4");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject contentObject = jsonObject.getJSONObject("content");
                JSONObject createdUserObject = jsonObject.getJSONObject("createdUser");
                Date dt = Converter.convertStringToDate(jsonObject.getString("created"), Backlog.DATE_TIME_FORMAT);

                if (contentObject.has("changes")) {
                    if (!contentObject.isNull("changes")) {
                        JSONArray changeArray = contentObject.getJSONArray("changes");
                        for (int j = 0; j <= changeArray.length() - 1; j++) {
                            JSONObject changeObject = changeArray.getJSONObject(j);
                            History<Long> history = new History<>();
                            history.setUser(createdUserObject.getString("name"));
                            if (dt != null) {
                                history.setTime(dt.getTime());
                            }
                            history.setField(changeObject.getString("field"));
                            history.setNewValue(changeObject.getString("new_value"));
                            history.setOldValue(changeObject.getString("old_value"));
                            histories.add(history);
                        }
                    }
                }
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
        return new BacklogPermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public List<String> getEnums(String title) {
        return null;
    }


    private Long getIdOfIssueType(Long project_id, String name) throws Exception {
        int status = this.executeRequest("/api/v2/projects/" + project_id + "/issueTypes?" + this.authParams);
        if (status == 200) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("name").equals(name)) {
                    return jsonObject.getLong("id");
                }
            }
        }
        return 0L;
    }

    private Long getCategoryOrAdd(Long project_id, String category) throws Exception {
        long id = -1;

        int status = this.executeRequest("/api/v2/projects/" + project_id + "/categories?" + this.authParams);
        if (status == 200) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("name").equals(category)) {
                    id = jsonObject.getLong("id");
                    break;
                }
            }
        }

        if (id == -1) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", category);
            status = this.executeRequest("/api/v2/projects/" + project_id + "/categories?" + this.authParams, jsonObject.toString(), "POST");
            if (status == 200) {
                JSONObject object = new JSONObject(this.getCurrentMessage());
                id = object.getLong("id");
            }
        }

        return id;
    }
}
