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

import java.text.SimpleDateFormat;
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
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.BugzillaPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Bugzilla extends JSONEngine implements IBugService<Long> {
    private final String loginParams;
    private final Authentication authentication;

    public Bugzilla(Authentication authentication) {
        super(
                authentication,
                authentication.getAPIKey().isEmpty() ? "" : "X-BUGZILLA-API-KEY: " + authentication.getAPIKey(),
                authentication.getUserName().isEmpty() ? "" : "X-BUGZILLA-LOGIN: " + authentication.getUserName(),
                authentication.getPassword().isEmpty() ? "" : "X-BUGZILLA-PASSWORD: " + authentication.getPassword());
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
                        project.getVersions().add(version);
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
        if (project.getId() == 0L) {
            url = "/rest/product";
            method = "POST";
        } else {
            url = "/rest/product/" + project.getId();
            method = "PUT";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("version", project.getDefaultVersion());
        jsonObject.put("is_open", project.isEnabled());
        jsonObject.put("has_unconfirmed", false);
        int status = this.executeRequest(url, jsonObject.toString(), method);

        if (status == 200 || status == 201) {
            if (project.getId() == 0) {
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
        this.deleteRequest("/rest/product/" + id);
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
    public List<Issue<Long>> getIssues(Long project_id) throws Exception {
        List<Issue<Long>> issues = new LinkedList<>();
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            int status = this.executeRequest("/rest/bug?product=" + project.getTitle().replace(" ", "%20") + "&" + this.loginParams);
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
                issue.setLastUpdated(Converter.convertStringToDate(bugObject.getString("last_change_time"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
                issue.setSubmitDate(Converter.convertStringToDate(bugObject.getString("creation_time"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
                issue.setDescription(bugObject.getString("url"));

                String priority = bugObject.getString("priority");
                issue.setSeverity(this.getId("priority", priority), priority);

                String statusEnum = bugObject.getString("status");
                issue.setStatus(this.getId("status", statusEnum), statusEnum);

                String severityEnum = bugObject.getString("severity");
                issue.setSeverity(this.getId("severity", severityEnum), severityEnum);

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
                        issue.setDueDate(Converter.convertStringToDate(bugObject.getString("deadline"), "yyyy-MM-dd"));
                    }
                }

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
                        note.setSubmitDate(Converter.convertStringToDate(commentObject.getString("creation_time"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
                        note.setLastUpdated(Converter.convertStringToDate(commentObject.getString("time"), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
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
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        JSONObject bugObject = new JSONObject();
        bugObject.put("summary", issue.getTitle());
        bugObject.put("url", issue.getDescription());
        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            bugObject.put("product", project.getTitle());
        }
        bugObject.put("priority", issue.getPriority().getValue());
        bugObject.put("status", issue.getStatus().getValue());
        bugObject.put("severity", issue.getSeverity().getValue());
        if (issue.getHandler() != null) {
            bugObject.put("assigned_to", issue.getHandler().getTitle());
        }
        bugObject.put("version", issue.getVersion());
        if (issue.getDueDate() != null) {
            bugObject.put("deadline", new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).format(issue.getDueDate()));
        }
        if (!issue.getTags().trim().equals("")) {
            JSONArray jsonArray = new JSONArray();
            for (String tag : issue.getTags().split(",")) {
                jsonArray.put(tag.trim());
            }
            bugObject.put("keywords", jsonArray);
        }

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/rest/bug/" + issue.getId(), bugObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/rest/bug", bugObject.toString(), "POST");
            if (status == 200 || status == 201) {
                JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
                issue.setId(jsonObject.getLong("id"));
            }
        }

        if (status == 200 || status == 201) {
            if (!issue.getNotes().isEmpty()) {
                for (Note<Long> note : issue.getNotes()) {
                    if (note.getId() == null) {
                        JSONObject noteObject = new JSONObject();
                        noteObject.put("comment", note.getDescription());
                        noteObject.put("is_private", note.getState().getKey() == 50);
                        this.executeRequest("/rest/bug/" + issue.getId() + "/comment", noteObject.toString(), "POST");
                    }
                }
            }

            Issue<Long> oldIssue = this.getIssue(issue.getId(), project_id);
            for (Attachment<Long> oldAttachment : oldIssue.getAttachments()) {
                boolean contains = false;
                for (Attachment<Long> attachment : issue.getAttachments()) {
                    if (oldAttachment.getId().equals(attachment.getId())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    this.deleteRequest("/rest/bug/attachment/" + oldAttachment.getId());
                }
            }

            if (!issue.getAttachments().isEmpty()) {
                for (Attachment<Long> attachment : issue.getAttachments()) {
                    JSONObject attachmentObject = new JSONObject();
                    JSONArray array = new JSONArray();
                    array.put(issue.getId());
                    attachmentObject.put("ids", array);
                    attachmentObject.put("file_name", attachment.getFilename());
                    attachmentObject.put("content_type", "text/plain");
                    attachmentObject.put("summary", "Add Attachment " + attachment.getFilename());
                    attachmentObject.put("data", Base64.encodeToString(attachment.getContent(), Base64.DEFAULT));
                    if (attachment.getId() != null) {
                        this.executeRequest("/rest/bug/attachment/" + attachment.getId(), attachmentObject.toString(), "PUT");
                    } else {
                        this.executeRequest("/rest/bug/" + issue.getId() + "/attachment", attachmentObject.toString(), "POST");
                    }
                }
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/rest/bug/" + id);
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
        int status = this.executeRequest("/rest/user/" + this.authentication.getUserName() + "?" + this.loginParams);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray userArray = jsonObject.getJSONArray("users");
            JSONObject userObject = userArray.getJSONObject(0);
            User<Long> user = new User<>();
            user.setId(userObject.getLong("id"));
            user.setEmail(userObject.getString("name"));
            user.setTitle(userObject.getString("name"));
            user.setRealName(userObject.getString("real_name"));
            users.add(user);
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
    public Long insertOrUpdateCustomField(CustomField<Long> field, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) throws Exception {

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
        return null;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new BugzillaPermissions(this.authentication);
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
