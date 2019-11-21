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

import android.util.Base64;

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.model.issues.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.BugzillaPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.unitrackerlibrary.utils.Converter;

public final class Bugzilla extends JSONEngine implements IBugService<Long> {
    private final String loginParams;
    private final Authentication authentication;
    private final static String DATE_FORMAT = "yyyy-MM-dd";
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public Bugzilla(Authentication authentication) {
        super(authentication);
        super.addHeader(authentication.getAPIKey().isEmpty() ? "" : "X-BUGZILLA-API-KEY: " + authentication.getAPIKey());
        super.addHeader(authentication.getUserName().isEmpty() ? "" : "X-BUGZILLA-LOGIN: " + authentication.getUserName());
        super.addHeader(authentication.getPassword().isEmpty() ? "" : "X-BUGZILLA-PASSWORD: " + authentication.getPassword());
        this.loginParams = "login=" + authentication.getUserName() + "&password=" + authentication.getPassword();
        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() throws Exception {
        List<User<Long>> users = this.getUsers(0L);

        return !users.isEmpty();
    }

    @Override
    public String getTrackerVersion() throws Exception {
        int status = this.executeRequest("/rest/version");
        if (status == 200 || status == 201) {
            return new JSONObject(this.getCurrentMessage()).getString("version");
        }
        return null;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/rest/product_selectable?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("ids");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                projects.add(this.getProject((long) jsonArray.getInt(i)));
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        int status = this.executeRequest("/rest/product/" + id + "?" + this.loginParams);
        if (status == 200 || status == 201) {
            Project<Long> project = new Project<>();
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("products");
            if (jsonArray.length() == 1) {
                JSONObject projectObject = jsonArray.getJSONObject(0);
                project.setId((long) projectObject.getInt("id"));
                project.setTitle(projectObject.getString("name"));
                project.setDescription(projectObject.getString("description"));
                project.setEnabled(projectObject.getBoolean("is_active"));
                if (projectObject.has("components")) {
                    JSONArray componentArray = projectObject.getJSONArray("components");
                    if (componentArray.length() >= 1) {
                        JSONObject componentObject = componentArray.getJSONObject(0);
                        project.getHints().put("component", componentObject.getString("name"));
                    }
                }

                if (projectObject.has("version")) {
                    project.setDefaultVersion(projectObject.getString("version"));
                }

                if (projectObject.has("versions")) {
                    JSONArray versionArray = projectObject.getJSONArray("versions");
                    for (int i = 0; i <= versionArray.length() - 1; i++) {
                        JSONObject versionObject = versionArray.getJSONObject(i);
                        Version<Long> version = new Version<>();
                        version.setId(versionObject.getLong("id"));
                        version.setTitle(versionObject.getString("name"));
                        version.setReleasedVersion(!versionObject.getBoolean("is_active"));
                        project.setDefaultVersion(version.getTitle());
                    }
                }

                return project;
            }
        }
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        String url, method;

        JSONObject jsonObject = new JSONObject();
        if (project.getId() == null) {
            url = "/rest/product?" + this.loginParams;
            method = "POST";
            jsonObject.put("version", "unspecified");
        } else {
            url = "/rest/product/" + project.getId() + "?" + this.loginParams;
            method = "PUT";
        }

        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("is_open", project.isEnabled());
        jsonObject.put("has_unconfirmed", false);

        int status = this.executeRequest(url, jsonObject.toString(), method);
        if (status == 200 || status == 201) {
            if (project.getId() == null) {
                JSONObject object = new JSONObject(this.getCurrentMessage());
                return (long) object.getInt("id");
            } else {
                return project.getId();
            }
        }

        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/rest/product/" + id + "?" + this.loginParams);
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            return project.getVersions();
        }
        return null;
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) {
    }

    @Override
    public void deleteVersion(Long id, Long project_id) {
    }

    @Override
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) throws Exception {
        if(filter==null) {
            filter = IssueFilter.all;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            filterQuery = "&is_open=" + (filter == IssueFilter.unresolved);
        }

        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/rest/bug?product=" + project.getTitle().replace(" ", "%20") + filterQuery + "&" + this.loginParams);
            if (status == 200 || status == 201) {
                JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                return jsonObject.getJSONArray("bugs").length();
            }
        }

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
        String limitation = "";
        if (numberOfItems != -1) {
            limitation = "&limit=" + numberOfItems + "&offset=" + ((page - 1) * numberOfItems);
        }
        String filterQuery = "";
        if (filter != IssueFilter.all) {
            filterQuery = "&is_open=" + (filter == IssueFilter.unresolved);
        }

        List<Issue<Long>> issues = new LinkedList<>();
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/rest/bug?product=" + project.getTitle().replace(" ", "%20") + limitation + filterQuery + "&" + this.loginParams);
            if (status == 200 || status == 201) {
                JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                JSONArray jsonArray = jsonObject.getJSONArray("bugs");
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject bugObject = jsonArray.getJSONObject(i);
                    Issue<Long> issue = new Issue<>();
                    issue.setId(bugObject.getLong("id"));
                    issue.setTitle(bugObject.getString("summary"));
                    if (bugObject.has("keywords")) {
                        if (bugObject.isNull("keywords")) {
                            JSONArray array = bugObject.getJSONArray("keywords");
                            StringBuilder builder = new StringBuilder();
                            for (int j = 0; j <= array.length() - 1; j++) {
                                builder.append(array.getString(j));
                                builder.append(",");
                            }
                            issue.setTags(builder.toString());
                        }
                    }
                    if (bugObject.has("version")) {
                        issue.getHints().put("version", bugObject.getString("version"));
                    }
                    issue.getHints().put("status", bugObject.getString("status"));
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();
        int status = this.executeRequest("/rest/bug/" + id + "?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("bugs");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject bugObject = jsonArray.getJSONObject(i);
                issue.setId(bugObject.getLong("id"));
                issue.setTitle(bugObject.getString("summary"));
                issue.setLastUpdated(Converter.convertStringToDate(bugObject.getString("last_change_time"), Bugzilla.DATE_TIME_FORMAT));
                issue.setSubmitDate(Converter.convertStringToDate(bugObject.getString("creation_time"), Bugzilla.DATE_TIME_FORMAT));
                issue.setDescription(bugObject.getString("url"));

                String priority = bugObject.getString("priority");
                issue.setPriority(this.getId("priority", priority), priority);

                String statusEnum = bugObject.getString("status");
                issue.setStatus(this.getId("bug_status", statusEnum), statusEnum);

                String severityEnum = bugObject.getString("severity");
                issue.setSeverity(this.getId("bug_severity", severityEnum), severityEnum);

                String resolutionEnum = bugObject.getString("resolution");
                issue.setResolution(this.getId("resolution", resolutionEnum), resolutionEnum);

                issue.setProfile(new Profile<>(bugObject.getString("platform"), bugObject.getString("op_sys"), ""));

                List<CustomField<Long>> customFields = this.getCustomFields(project_id);
                for (CustomField<Long> customField : customFields) {
                    String value = "";
                    if (bugObject.has(customField.getHints().get("name"))) {
                        if (!bugObject.isNull(customField.getHints().get("name"))) {
                            Map<String, String> hints = customField.getHints();
                            if(hints!=null) {
                                String content = hints.get("name");
                                if(content!=null) {
                                    value = bugObject.getString(content);
                                }
                            }
                        }
                    }
                    issue.getCustomFields().put(customField, value);
                }

                if (bugObject.has("assigned_to_detail")) {
                    if (!bugObject.isNull("assigned_to_detail")) {
                        JSONObject userObject = bugObject.getJSONObject("assigned_to_detail");
                        User<Long> user = new User<>();
                        user.setId(userObject.getLong("id"));
                        user.setEmail(userObject.getString("name"));
                        user.setTitle(userObject.getString("name"));
                        user.setRealName(userObject.getString("real_name"));
                        issue.setHandler(user);
                    }
                }

                if (bugObject.has("version")) {
                    issue.setVersion(bugObject.getString("version"));
                }

                if (bugObject.has("deadline")) {
                    if (!bugObject.isNull("deadline")) {
                        issue.setDueDate(Converter.convertStringToDate(bugObject.getString("deadline"), Bugzilla.DATE_FORMAT));
                    }
                }

                if (bugObject.has("keywords")) {
                    if (!bugObject.isNull("keywords")) {
                        JSONArray array = bugObject.getJSONArray("keywords");
                        StringBuilder builder = new StringBuilder();
                        for (int j = 0; j <= array.length() - 1; j++) {
                            builder.append(array.getString(j));
                            builder.append(",");
                        }
                        issue.setTags(builder.toString());
                    }
                }

                status = this.executeRequest("/rest/bug/" + issue.getId() + "/comment");
                if (status == 200 || status == 201) {
                    JSONObject rootObject = new JSONObject(this.getCurrentMessage());
                    JSONObject commentBugObject = rootObject.getJSONObject("bugs").getJSONObject(String.valueOf(issue.getId()));
                    JSONArray commentArray = commentBugObject.getJSONArray("comments");
                    for (int j = 0; j <= commentArray.length() - 1; j++) {
                        JSONObject commentObject = commentArray.getJSONObject(j);
                        Note<Long> note = new Note<>();
                        note.setId(commentObject.getLong("id"));

                        if (commentObject.getBoolean("is_private")) {
                            note.setState(50, "");
                        } else {
                            note.setState(10, "");
                        }

                        String text = commentObject.getString("text");
                        if (text.length() >= 50) {
                            note.setTitle(text.substring(0, 50));
                        } else {
                            note.setTitle(text);
                        }
                        note.setDescription(text);
                        note.setSubmitDate(Converter.convertStringToDate(commentObject.getString("creation_time"), Bugzilla.DATE_TIME_FORMAT));
                        note.setLastUpdated(Converter.convertStringToDate(commentObject.getString("time"), Bugzilla.DATE_TIME_FORMAT));
                        issue.getNotes().add(note);
                    }
                }

                status = this.executeRequest("/rest/bug/" + issue.getId() + "/attachment");
                if (status == 200 || status == 201) {
                    JSONObject rootObject = new JSONObject(this.getCurrentMessage());
                    JSONArray attachmentArray = rootObject.getJSONObject("bugs").getJSONArray(String.valueOf(issue.getId()));
                    for (int j = 0; j <= attachmentArray.length() - 1; j++) {
                        JSONObject attachmentObject = attachmentArray.getJSONObject(j);
                        Attachment<Long> attachment = new Attachment<>();
                        attachment.setId(attachmentObject.getLong("id"));
                        attachment.setTitle(attachmentObject.getString("file_name"));
                        attachment.setFilename(attachmentObject.getString("file_name"));
                        attachment.setContent(Base64.decode(attachmentObject.getString("data"), Base64.DEFAULT));
                        issue.getAttachments().add(attachment);
                    }
                }
            }
        }
        return issue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        JSONObject bugObject = new JSONObject();
        bugObject.put("summary", issue.getTitle());
        bugObject.put("description", issue.getDescription());
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            bugObject.put("product", project.getTitle());
            bugObject.put("component", project.getHints().get("component"));
            bugObject.put("version", project.getDefaultVersion());
        }
        bugObject.put("priority", issue.getPriority().getValue());
        bugObject.put("status", issue.getStatus().getValue());
        bugObject.put("severity", issue.getSeverity().getValue());
        bugObject.put("resolution", issue.getResolution().getValue());
        if (issue.getProfile() == null) {
            bugObject.put("op_sys", "All");
            bugObject.put("platform", "All");
        } else {
            if(issue.getProfile().getOs().trim().isEmpty()) {
                bugObject.put("op_sys", "All");
            } else {
                bugObject.put("op_sys", issue.getProfile().getOs());
            }
            if(issue.getProfile().getPlatform().trim().isEmpty()) {
                bugObject.put("platform", "All");
            } else {
                bugObject.put("platform", issue.getProfile().getPlatform());
            }
        }
        for (Map.Entry<CustomField<Long>, String> entry : issue.getCustomFields().entrySet()) {
            String name = entry.getKey().getHints().get("name");
            if(name!=null) {
                bugObject.put(name, entry.getValue());
            }
        }
        if (issue.getHandler() != null) {
            bugObject.put("assigned_to", issue.getHandler().getTitle());
        }

        if (issue.getDueDate() != null) {
            bugObject.put("deadline", new SimpleDateFormat(Bugzilla.DATE_FORMAT, Locale.GERMAN).format(issue.getDueDate()));
        }
        bugObject.put("keywords", this.getTagObject(issue));

        int status;
        if (issue.getId() != null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(Long.parseLong(String.valueOf(issue.getId())));
            bugObject.put("ids", jsonArray);
            JSONObject commentObject = new JSONObject();
            commentObject.put("body", "Updated by UniTrackerMobile!");
            bugObject.put("comment", commentObject);
            status = this.executeRequest("/rest/bug/" + issue.getId() + "?" + this.loginParams, bugObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/rest/bug?" + this.loginParams, bugObject.toString(), "POST");
            if (status == 200 || status == 201) {
                JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                issue.setId(jsonObject.getLong("id"));
            }
        }

        if (status == 200 || status == 201) {
            if (!issue.getNotes().isEmpty()) {
                for (Note<Long> note : issue.getNotes()) {
                    if (note.getId() == null) {
                        this.insertOrUpdateNote(note, Long.parseLong(String.valueOf(issue.getId())), project_id);
                    }
                }
            }

            Issue<Long> oldIssue = this.getIssue(Long.parseLong(String.valueOf(issue.getId())), project_id);
            for (Attachment<Long> oldAttachment : oldIssue.getAttachments()) {
                boolean contains = false;
                for (Attachment<Long> attachment : issue.getAttachments()) {
                    if (oldAttachment.getId().equals(attachment.getId())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    this.deleteAttachment(oldAttachment.getId(), Long.parseLong(String.valueOf(issue.getId())), project_id);
                }
            }

            if (!issue.getAttachments().isEmpty()) {
                for (DescriptionObject descriptionObject: issue.getAttachments()) {
                    if(descriptionObject instanceof Attachment) {
                        Attachment<Long> attachment = (Attachment<Long>) descriptionObject;
                        this.insertOrUpdateAttachment(attachment, Long.parseLong(String.valueOf(issue.getId())), project_id);
                    }
                }
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/rest/bug/" + id + "?" + this.loginParams);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        JSONObject noteObject = new JSONObject();
        noteObject.put("comment", note.getDescription());
        noteObject.put("is_private", note.getState().getKey() == 50);
        this.executeRequest("/rest/bug/" + issue_id + "/comment?" + this.loginParams, noteObject.toString(), "POST");
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) {
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) throws Exception {
        JSONObject attachmentObject = new JSONObject();
        JSONArray array = new JSONArray();
        array.put(issue_id);
        attachmentObject.put("ids", array);
        attachmentObject.put("file_name", attachment.getFilename());
        attachmentObject.put("content_type", "text/plain");
        attachmentObject.put("summary", "Add Attachment " + attachment.getFilename());
        attachmentObject.put("data", Base64.encodeToString(attachment.getContent(), Base64.DEFAULT));
        if (attachment.getId() != null) {
            this.executeRequest("/rest/bug/attachment/" + attachment.getId() + "?" + this.loginParams, attachmentObject.toString(), "PUT");
        } else {
            this.executeRequest("/rest/bug/" + issue_id + "/attachment?" + this.loginParams, attachmentObject.toString(), "POST");
        }
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) throws Exception {
        this.deleteRequest("/rest/bug/attachment/" + id + "?" + this.loginParams);
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
        int status = this.executeRequest("/rest/user?match=*&" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray userArray = jsonObject.getJSONArray("users");
            for (int i = 0; i <= userArray.length() - 1; i++) {
                users.add(this.getUser(userArray.getJSONObject(i).getLong("id"), project_id));
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        User<Long> user = new User<>();
        int status = this.executeRequest("/rest/user/" + id + "?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray userArray = jsonObject.getJSONArray("users");
            if (userArray.length() >= 1) {
                JSONObject userObject = userArray.getJSONObject(0);
                user.setId(userObject.getLong("id"));
                user.setEmail(userObject.getString("name"));
                user.setTitle(userObject.getString("name"));
                user.setRealName(userObject.getString("real_name"));
            }
        }
        return user;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", user.getTitle());
        jsonObject.put("email", user.getEmail());
        jsonObject.put("full_name", user.getRealName());
        jsonObject.put("password", user.getPassword());

        int status;
        if (user.getId() != null) {
            status = this.executeRequest("/rest/user/" + user.getId() + "?" + this.loginParams, jsonObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/rest/user?" + this.loginParams, jsonObject.toString(), "POST");
        }
        if (status == 200 || status == 201) {
            if (user.getId() == null) {
                JSONObject response = new JSONObject(this.getCurrentMessage());
                response.getLong("id");
            } else {
                JSONArray jsonArray = new JSONObject(this.getCurrentMessage()).getJSONArray("users");
                if (jsonArray.length() >= 1) {
                    jsonArray.getJSONObject(0).getLong("id");
                }
            }
        }
    }

    @Override
    public void deleteUser(Long id, Long project_id) {
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) throws Exception {
        List<CustomField<Long>> customFields = new LinkedList<>();
        int status = this.executeRequest("/rest/field/bug?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("fields");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject fieldObject = jsonArray.getJSONObject(i);
                if (fieldObject.getBoolean("is_custom")) {
                    customFields.add(this.getCustomField(fieldObject.getLong("id"), project_id));
                }
            }
        }
        return customFields;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) throws Exception {
        CustomField<Long> customField = new CustomField<>();
        int status = this.executeRequest("/rest/field/bug/" + id + "?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("fields");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject fieldObject = jsonArray.getJSONObject(i);
                customField.setId(fieldObject.getLong("id"));
                customField.setTitle(fieldObject.getString("display_name"));
                customField.setNullable(!fieldObject.getBoolean("is_mandatory"));
                customField.getHints().put("name", fieldObject.getString("name"));
                if (fieldObject.has("values")) {
                    if (!fieldObject.isNull("values")) {
                        JSONArray valueArray = fieldObject.getJSONArray("values");
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j <= valueArray.length() - 1; j++) {
                            JSONObject valueObject = valueArray.getJSONObject(j);
                            stringBuilder.append(valueObject.getString("name"));
                            stringBuilder.append("|");
                        }
                        customField.setPossibleValues(stringBuilder.toString());
                    }
                }

                int type = fieldObject.getInt("type");
                switch (type) {
                    case 0:
                    case 1:
                        customField.setType(CustomField.Type.TEXT);
                        break;
                    case 2:
                        customField.setType(CustomField.Type.LIST);
                        break;
                    case 3:
                        customField.setType(CustomField.Type.MULTI_SELECT_LIST);
                        break;
                    case 4:
                        customField.setType(CustomField.Type.TEXT_AREA);
                        break;
                    case 5:
                    case 9:
                        customField.setType(CustomField.Type.DATE);
                        break;
                    case 10:
                        customField.setType(CustomField.Type.NUMBER);
                        break;

                }
            }
        }

        return customField;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> field, Long project_id) {
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) {
    }

    @Override
    public List<String> getCategories(Long pid) {
        return new LinkedList<>();
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) throws Exception {
        List<Tag<Long>> tags = new LinkedList<>();
        List<Issue<Long>> issues = this.getIssues(project_id);
        for (Issue<Long> issue : issues) {
            String strTags = issue.getTags();
            for (String strTag : strTags.split(",")) {
                boolean contains = false;
                for (Tag<Long> tag : tags) {
                    if (strTag.equals(tag.getTitle())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    Tag<Long> tag = new Tag<>();
                    tag.setTitle(strTag);
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        List<History<Long>> historyItems = new LinkedList<>();
        int status = this.executeRequest("/rest/bug/" + issue_id + "/history?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray jsonArray = jsonObject.getJSONArray("bugs");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject bugObject = jsonArray.getJSONObject(i);
                JSONArray historyArray = bugObject.getJSONArray("history");
                for (int j = 0; j <= historyArray.length() - 1; j++) {
                    JSONObject historyObject = historyArray.getJSONObject(j);
                    JSONArray changeArray = historyObject.getJSONArray("changes");
                    for (int k = 0; k <= changeArray.length() - 1; k++) {
                        JSONObject changeObject = changeArray.getJSONObject(k);
                        History<Long> history = new History<>();
                        Date when = Converter.convertStringToDate(historyObject.getString("when"), Bugzilla.DATE_TIME_FORMAT);
                        history.setTime(when.getTime());
                        history.setUser(historyObject.getString("who"));
                        history.setField(changeObject.getString("field_name"));
                        history.setNewValue(changeObject.getString("added"));
                        history.setOldValue(changeObject.getString("removed"));
                        historyItems.add(history);
                    }
                }
            }
        }
        return historyItems;
    }

    @Override
    public List<Profile<Long>> getProfiles() {
        List<Profile<Long>> profiles = new LinkedList<>();
        profiles.add(new Profile<>("All", "All", ""));
        profiles.add(new Profile<>("PC", "Windows", ""));
        profiles.add(new Profile<>("PC", "Mac OS", ""));
        profiles.add(new Profile<>("PC", "Linux", ""));
        profiles.add(new Profile<>("PC", "Other", ""));
        profiles.add(new Profile<>("Smartphone", "Android", ""));
        profiles.add(new Profile<>("Smartphone", "IOS", ""));
        profiles.add(new Profile<>("Smartphone", "Other", ""));
        profiles.add(new Profile<>("Other", "", ""));
        return profiles;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new BugzillaPermissions(this.authentication);
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public List<String> getEnums(String title) {
        return null;
    }

    @Override
    @NonNull
    public String toString() {
        return this.getAuthentication().getTitle();
    }

    private JSONObject getTagObject(Issue<Long> issue) throws Exception {
        List<String> newTagList = new LinkedList<>();
        if(!issue.getTags().isEmpty()) {
            if(issue.getTags().contains(",")) {
                for(String tag : issue.getTags().split(",")) {
                    newTagList.add(tag.trim());
                }
            } else {
                newTagList.add(issue.getTags().trim());
            }
        }

        JSONObject jsonObject = new JSONObject();
        JSONArray setArray = new JSONArray();
        for(String tag : newTagList) {
            setArray.put(tag);
        }
        jsonObject.put("set", setArray);
        return jsonObject;
    }

    private Map<String, Integer> getEnumValues(String name) throws Exception {
        Map<String, Integer> entries = new LinkedHashMap<>();
        int status = this.executeRequest("/rest/field/bug/" + name + "?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject fieldObject = jsonObject.getJSONArray("fields").getJSONObject(0);
            JSONArray valueArray = fieldObject.getJSONArray("values");
            for (int i = 1; i <= valueArray.length(); i++) {
                entries.put(valueArray.getJSONObject(i - 1).getString("name"), i);
            }
        }
        return entries;
    }

    private int getId(String fieldType, String value) throws Exception {
        int id = 0;
        Map<String, Integer> map = this.getEnumValues(fieldType);
        if (map != null) {
            Integer val = map.get(value);
            if (val != null) {
                id = val;
            }
        }
        return id;
    }
}
