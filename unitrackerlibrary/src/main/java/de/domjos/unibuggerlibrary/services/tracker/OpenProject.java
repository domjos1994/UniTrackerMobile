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
import de.domjos.unibuggerlibrary.model.issues.Profile;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.OpenProjectPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;
import okhttp3.Credentials;

public final class OpenProject extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String OTHER_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    public OpenProject(Authentication authentication) {
        super(authentication, "Content-Type: application/hal+json", "Authorization: " + Credentials.basic("apikey", authentication.getPassword()));
        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/api/v3/users/me");
        return status == 200 || status == 201;
    }

    @Override
    public String getTrackerVersion() {
        return "v3";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/api/v3/projects");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                Project<Long> project = new Project<>();
                project.setId(elementObject.optLong("id"));
                project.setAlias(elementObject.getString("identifier"));
                project.setTitle(elementObject.getString("name"));
                project.setDescription(elementObject.getString("description"));
                project.setCreatedAt(Converter.convertStringToDate(elementObject.getString("createdAt"), OpenProject.DATE_TIME_FORMAT).getTime());
                project.setUpdatedAt(Converter.convertStringToDate(elementObject.getString("updatedAt"), OpenProject.DATE_TIME_FORMAT).getTime());
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        Project<Long> project = new Project<>();
        int status = this.executeRequest("/api/v3/projects/" + id);
        if (status == 200 || status == 201) {
            JSONObject elementObject = new JSONObject(this.getCurrentMessage());
            project.setId(elementObject.optLong("id"));
            project.setAlias(elementObject.getString("identifier"));
            project.setTitle(elementObject.getString("name"));
            project.setDescription(elementObject.getString("description"));
            project.setCreatedAt(Converter.convertStringToDate(elementObject.getString("createdAt"), OpenProject.DATE_TIME_FORMAT).getTime());
            project.setUpdatedAt(Converter.convertStringToDate(elementObject.getString("updatedAt"), OpenProject.DATE_TIME_FORMAT).getTime());
        }
        return project;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) {
        return 0L;
    }

    @Override
    public void deleteProject(Long id) {
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        int status = this.executeRequest("/api/v3/projects/" + project_id + "/versions");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                Version<Long> version = new Version<>();
                version.setId(elementObject.getLong("id"));
                version.setTitle(elementObject.getString("name"));
                if (elementObject.has("description")) {
                    if (!elementObject.isNull("description")) {
                        version.setDescription(elementObject.getJSONObject("description").getString("raw"));
                    }
                }
                if (elementObject.has("startDate")) {
                    if (!elementObject.isNull("startDate")) {
                        version.setReleasedVersionAt(Converter.convertStringToDate(elementObject.getString("startDate"), OpenProject.DATE_FORMAT).getTime());
                    }
                }
                if (elementObject.has("status")) {
                    if (!elementObject.isNull("status")) {
                        switch (elementObject.getString("status").toLowerCase()) {
                            case "open":
                                version.setReleasedVersion(false);
                                version.setDeprecatedVersion(false);
                                break;
                            case "locked":
                                version.setReleasedVersion(true);
                                version.setDeprecatedVersion(true);
                                break;
                            case "closed":
                                version.setReleasedVersion(true);
                                version.setDeprecatedVersion(false);
                                break;
                        }
                    }
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
        JSONObject descriptionObject = new JSONObject();
        descriptionObject.put("raw", version.getDescription());
        jsonObject.put("description", descriptionObject);
        if (version.getReleasedVersionAt() != 0) {
            Date dt = new Date();
            dt.setTime(version.getReleasedVersionAt());
            jsonObject.put("startDate", new SimpleDateFormat(OpenProject.DATE_FORMAT, Locale.GERMAN).format(dt));
        }
        if (!version.isReleasedVersion() && !version.isDeprecatedVersion()) {
            jsonObject.put("status", "open");
        } else if (version.isReleasedVersion() && !version.isDeprecatedVersion()) {
            jsonObject.put("status", "closed");
        } else {
            jsonObject.put("status", "locked");
        }
        JSONObject links = new JSONObject();
        JSONObject definingObject = new JSONObject();
        definingObject.put("href", "/api/v3/projects/" + project_id);
        links.put("definingProject", definingObject);
        jsonObject.put("_links", links);

        if (version.getId() == null) {
            this.executeRequest("/api/v3/versions", jsonObject.toString(), "POST");
        } else {
            this.executeRequest("/api/v3/versions/" + version.getId(), jsonObject.toString(), "PATCH");
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v3/versions/" + id);
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

        String pagination = "?";
        if (numberOfItems != -1) {
            pagination = "?offset=" + (page * numberOfItems) + "&pageSize=" + numberOfItems;
        }

        String filterQuery = "&filters=[{\"type_id\":{\"operator\":\"=\",\"values\":[\"4\",\"7\"]}}]";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = "&filters=[{\"type_id\":{\"operator\":\"=\",\"values\":[\"4\",\"7\"]}},{\"status_id\":{\"operator\":\"=\",\"values\":[\"12\",\"13\",\"14\"]}}]";
            } else {
                filterQuery = "&filters=[{\"type_id\":{\"operator\":\"=\",\"values\":[\"4\",\"7\"]}},{\"status_id\":{\"operator\":\"!\",\"values\":[\"12\",\"13\",\"14\"]}}]";
            }
        }

        int status = this.executeRequest("/api/v3/projects/" + project_id + "/work_packages" + pagination + filterQuery);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                Issue<Long> issue = new Issue<>();
                issue.setId(elementObject.getLong("id"));
                issue.setTitle(elementObject.getString("subject"));
                issues.add(issue);
            }
        }

        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();
        int status = this.executeRequest("/api/v3/work_packages/" + id);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            issue.getHints().put("lockVersion", jsonObject.getString("lockVersion"));
            issue.setId(jsonObject.getLong("id"));
            issue.setTitle(jsonObject.getString("subject"));
            if (jsonObject.has("description")) {
                if (!jsonObject.isNull("description")) {
                    JSONObject description = jsonObject.getJSONObject("description");
                    issue.setDescription(description.getString("raw"));
                }
            }
            if (jsonObject.has("dueDate")) {
                if (!jsonObject.isNull("dueDate")) {
                    issue.setDueDate(Converter.convertStringToDate(jsonObject.getString("dueDate"), OpenProject.DATE_FORMAT));
                }
            }
            issue.setSubmitDate(Converter.convertStringToDate(jsonObject.getString("createdAt"), OpenProject.OTHER_DATE_TIME_FORMAT));
            issue.setLastUpdated(Converter.convertStringToDate(jsonObject.getString("updatedAt"), OpenProject.OTHER_DATE_TIME_FORMAT));

            JSONObject linkObject = jsonObject.getJSONObject("_links");
            if (linkObject.has("assignee")) {
                if (!linkObject.isNull("assignee")) {
                    String href = linkObject.getJSONObject("assignee").getString("href");
                    String user_id = href.substring(href.lastIndexOf("/") + 1);
                    if(!user_id.equals("null")) {
                        issue.setHandler(this.getUser(Long.parseLong(user_id), project_id));
                    }
                }
            }

            if (linkObject.has("priority")) {
                if (!linkObject.isNull("priority")) {
                    String href = linkObject.getJSONObject("priority").getString("href");
                    String title = linkObject.getJSONObject("priority").getString("title");
                    String priority_id = href.substring(href.lastIndexOf("/") + 1);
                    issue.setPriority(Integer.parseInt(priority_id), title);
                }
            }

            if (linkObject.has("status")) {
                if (!linkObject.isNull("status")) {
                    String href = linkObject.getJSONObject("status").getString("href");
                    String title = linkObject.getJSONObject("status").getString("title");
                    String status_id = href.substring(href.lastIndexOf("/") + 1);
                    issue.setStatus(Integer.parseInt(status_id), title);
                }
            }

            if (linkObject.has("type")) {
                if (!linkObject.isNull("type")) {
                    String href = linkObject.getJSONObject("type").getString("href");
                    String title = linkObject.getJSONObject("type").getString("title");
                    String type_id = href.substring(href.lastIndexOf("/") + 1);
                    issue.setSeverity(Integer.parseInt(type_id), title);
                }
            }

            if (linkObject.has("category")) {
                if (!linkObject.isNull("category")) {
                    JSONObject categoryObject = linkObject.getJSONObject("category");
                    if (categoryObject.has("title")) {
                        String title = categoryObject.getString("title");
                        issue.setCategory(title);
                    }
                }
            }

            if (linkObject.has("version")) {
                if (!linkObject.isNull("version")) {
                    JSONObject versionObject = linkObject.getJSONObject("version");
                    if (versionObject.has("title")) {
                        String title = versionObject.getString("title");
                        issue.setVersion(title);
                    }
                }
            }

            issue.getNotes().addAll(this.getNotes(issue.getId(), project_id));
            issue.getAttachments().addAll(this.getAttachments(issue.getId(), project_id));
        }
        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(OpenProject.DATE_FORMAT, Locale.GERMAN);

        JSONObject jsonObject = new JSONObject();
        if (issue.getId() != null) {
            jsonObject.put("lockVersion", this.getIssue(Long.parseLong(String.valueOf(issue.getId())), project_id).getHints().get("lockVersion"));
        }
        jsonObject.put("subject", issue.getTitle());
        JSONObject descriptionObject = new JSONObject();
        descriptionObject.put("raw", issue.getDescription());
        jsonObject.put("description", descriptionObject);

        if (issue.getDueDate() != null) {
            jsonObject.put("dueDate", sdf.format(issue.getDueDate()));
        }

        JSONObject linksObject = new JSONObject();

        JSONObject definingObject = new JSONObject();
        definingObject.put("href", "/api/v3/projects/" + project_id);
        linksObject.put("project", definingObject);

        if (issue.getHandler() != null) {
            if(issue.getHandler().getId()!=null) {
                if(issue.getHandler().getId()!=0L) {
                    JSONObject handlerObject = new JSONObject();
                    handlerObject.put("href", "/api/v3/users/" + issue.getHandler().getId());
                    linksObject.put("assignee", handlerObject);
                }
            }
        }

        if (issue.getPriority().getKey() != 0) {
            JSONObject priorityObject = new JSONObject();
            priorityObject.put("href", "/api/v3/priorities/" + issue.getPriority().getKey());
            linksObject.put("priority", priorityObject);
        }

        if (issue.getStatus().getKey() != 0) {
            JSONObject statusObject = new JSONObject();
            statusObject.put("href", "/api/v3/statuses/" + issue.getStatus().getKey());
            linksObject.put("status", statusObject);
        }

        if (issue.getSeverity().getKey() != 0) {
            JSONObject severityObject = new JSONObject();
            severityObject.put("href", "/api/v3/types/" + issue.getSeverity().getKey());
            linksObject.put("type", severityObject);
        }

        if (issue.getCategory() != null) {
            if (!issue.getCategory().isEmpty()) {
                JSONObject categoryObject = new JSONObject();
                categoryObject.put("title", issue.getCategory());
                linksObject.put("category", categoryObject);
            }
        }

        if (issue.getVersion() != null) {
            if (!issue.getVersion().isEmpty()) {
                List<Version<Long>> versions = this.getVersions("", project_id);
                for(Version<Long> version : versions) {
                    if(version.getTitle().equals(issue.getVersion())) {
                        JSONObject versionObject = new JSONObject();
                        versionObject.put("href", "/api/v3/versions/" + version.getId());
                        linksObject.put("version", versionObject);
                        break;
                    }
                }
            }
        }
        jsonObject.put("_links", linksObject);

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/api/v3/work_packages/" + issue.getId(), jsonObject.toString(), "PATCH");
        } else {
            status = this.executeRequest("/api/v3/work_packages", jsonObject.toString(), "POST");
        }

        if (status == 200 || status == 201) {
            if (issue.getId() == null) {
                JSONObject result = new JSONObject(this.getCurrentMessage());
                issue.setId(result.getLong("id"));
            }

            List<Note<Long>> notes = this.getNotes(Long.parseLong(String.valueOf(issue.getId())), project_id);
            for (Note<Long> note : notes) {
                boolean available = false;
                for (Note<Long> newNote : issue.getNotes()) {
                    if (note.getId().equals(newNote.getId())) {
                        available = true;
                        break;
                    }
                }
                if (!available) {
                    this.deleteNote(Long.parseLong(String.valueOf(note.getId())), Long.parseLong(String.valueOf(issue.getId())), project_id);
                }
            }

            if (!issue.getNotes().isEmpty()) {
                for (DescriptionObject descrObj : issue.getNotes()) {
                    if(descrObj instanceof Note) {
                        Note<Long> note = (Note<Long>) descrObj;
                        this.insertOrUpdateNote(note, Long.parseLong(String.valueOf(issue.getId())), project_id);
                    }
                }
            }

            List<Attachment<Long>> attachments = this.getAttachments(Long.parseLong(String.valueOf(issue.getId())), project_id);
            for (Attachment<Long> attachment : attachments) {
                boolean available = false;
                for (Attachment<Long> newAttachment : issue.getAttachments()) {
                    if (attachment.getId().equals(newAttachment.getId())) {
                        available = true;
                        break;
                    }
                }
                if (!available) {
                    this.deleteAttachment(Long.parseLong(String.valueOf(attachment.getId())), Long.parseLong(String.valueOf(issue.getId())), project_id);
                }
            }

            if (!issue.getAttachments().isEmpty()) {
                for (DescriptionObject descrObj : issue.getAttachments()) {
                    if(descrObj instanceof Attachment) {
                        Attachment<Long> attachment = (Attachment<Long>) descrObj;
                        this.insertOrUpdateAttachment(attachment, Long.parseLong(String.valueOf(issue.getId())), project_id);
                    }
                }
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v3/work_packages/" + id);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) throws Exception {
        List<Note<Long>> notes = new LinkedList<>();
        int status = this.executeRequest("/api/v3/work_packages/" + issue_id + "/activities");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                JSONObject noteObject = elementObject.getJSONObject("comment");
                Note<Long> note = new Note<>();
                note.setId(elementObject.getLong("id"));
                String content = noteObject.getString("raw");
                if (content != null) {
                    if (!content.isEmpty()) {
                        if (content.length() >= 50) {
                            note.setTitle(content.substring(50));
                        } else {
                            note.setTitle(content);
                        }
                        note.setDescription(content);
                        notes.add(note);
                    }
                }
            }
        }
        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject commentObject = new JSONObject();
        commentObject.put("raw", note.getDescription());
        jsonObject.put("comment", commentObject);

        if (note.getId() == null) {
            this.executeRequest("/api/v3/work_packages/" + issue_id + "/activities", jsonObject.toString(), "POST");
        } else {
            this.executeRequest("/api/v3/activities/" + note.getId(), jsonObject.toString(), "PATCH");
        }
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {
        List<Note<Long>> notes = this.getNotes(issue_id, project_id);
        for (Note<Long> note : notes) {
            if (note.getId().equals(id)) {
                note.setDescription("");
                this.insertOrUpdateNote(note, issue_id, project_id);
                return;
            }
        }
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) throws Exception {
        List<Attachment<Long>> attachments = new LinkedList<>();
        int status = this.executeRequest("/api/v3/work_packages/" + issue_id + "/attachments");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                Attachment<Long> attachment = new Attachment<>();
                attachment.setId(elementObject.getLong("id"));
                attachment.setFilename(elementObject.getString("fileName"));
                attachment.setContentType(elementObject.getString("contentType"));

                JSONObject linkObject = elementObject.getJSONObject("_links");
                JSONObject downloadObject = linkObject.getJSONObject("downloadLocation");
                attachment.setDownloadUrl(downloadObject.getString("href"));
                attachment.setContent(Converter.convertStringToByteArray(attachment.getDownloadUrl()));
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) throws Exception {
        if (attachment.getId() != null) {
            this.deleteAttachment(attachment.getId(), issue_id, project_id);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", attachment.getFilename());
        JSONObject descriptionObject = new JSONObject();
        descriptionObject.put("raw", attachment.getDescription());
        jsonObject.put("description", descriptionObject);

        this.addMultiPart("/api/v3/work_packages/" + issue_id + "/attachments", jsonObject.toString(), attachment.getContentType(), attachment.getContent(), "POST");
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) throws Exception {
        this.deleteRequest("/api/v3/attachments/" + id);
    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/api/v3/users");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                User<Long> user = new User<>();
                user.setId(elementObject.getLong("id"));
                user.setTitle(elementObject.getString("login"));
                user.setRealName(elementObject.getString("name"));
                user.setEmail(elementObject.getString("email"));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) throws Exception {
        User<Long> user = new User<>();
        int status = this.executeRequest("/api/v3/users/" + id);
        if (status == 200 || status == 201) {
            JSONObject elementObject = new JSONObject(this.getCurrentMessage());
            user.setId(elementObject.getLong("id"));
            user.setTitle(elementObject.getString("login"));
            user.setRealName(elementObject.getString("firstName") + " " + elementObject.getString("lastName"));
            user.setEmail(elementObject.getString("email"));
        }
        return user;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        JSONObject userObject = new JSONObject();
        userObject.put("login", user.getTitle());
        userObject.put("email", user.getEmail());
        String name = user.getRealName();
        if (name.contains(" ")) {
            userObject.put("firstName", name.split(" ")[0]);
            userObject.put("lastName", name.replace(name.split(" ")[0], "").trim());
        } else {
            userObject.put("lastName", name);
        }
        userObject.put("admin", true);
        userObject.put("language", Locale.getDefault().getLanguage());
        userObject.put("status", "active");
        userObject.put("password", user.getPassword());

        if (user.getId() != null) {
            this.executeRequest("/api/v3/users/" + user.getId(), userObject.toString(), "PATCH");
        } else {
            this.executeRequest("/api/v3/users", userObject.toString(), "POST");
        }
    }

    @Override
    public void deleteUser(Long id, Long project_id) throws Exception {
        this.deleteRequest("/api/v3/users/" + id);
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) {
        return new LinkedList<>();
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
    public List<String> getCategories(Long project_id) throws Exception {
        List<String> categories = new LinkedList<>();
        int status = this.executeRequest("/api/v3/projects/" + project_id + "/categories");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject elementObject = elementArray.getJSONObject(i);
                categories.add(elementObject.getString("name"));
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
        int status = this.executeRequest("/api/v3/work_packages/" + project_id + "/activities");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONObject embeddedObject = jsonObject.getJSONObject("_embedded");
            JSONArray elementArray = embeddedObject.getJSONArray("elements");
            for (int i = 0; i <= elementArray.length() - 1; i++) {
                JSONObject historyObject = elementArray.getJSONObject(i);

                JSONArray detailsArray = historyObject.getJSONArray("details");
                for (int j = 0; j <= detailsArray.length() - 1; j++) {
                    JSONObject detailsObject = detailsArray.getJSONObject(j);
                    History<Long> history = new History<>();
                    history.setId(historyObject.getLong("id"));
                    history.setTime(Converter.convertStringToDate(historyObject.getString("createdAt"), OTHER_DATE_TIME_FORMAT).getTime());

                    JSONObject linksObject = historyObject.getJSONObject("_links");
                    JSONObject userObject = linksObject.getJSONObject("user");
                    String userLink = userObject.getString("href");
                    long id = Long.parseLong(userLink.substring(userLink.lastIndexOf("/") + 1));
                    history.setUser(this.getUser(id, project_id).getTitle());

                    history.setField(detailsObject.getString("format"));
                    history.setNewValue(detailsObject.getString("raw"));
                    histories.add(history);
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
        return new OpenProjectPermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public List<String> getEnums(String title) {
        return null;
    }
}
