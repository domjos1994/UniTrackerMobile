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
import java.util.UUID;

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
import de.domjos.unibuggerlibrary.permissions.BacklogPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.JSONEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class Backlog extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;
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
                        Date dt = Converter.convertStringToDate(jsonObject.getString("releaseDueDate"), "yyyy-MM-dd");
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);

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
    public List<Issue<Long>> getIssues(Long project_id) throws Exception {
        return null;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, IssueFilter filter) throws Exception {
        return null;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems) throws Exception {
        return null;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems, IssueFilter filter) throws Exception {
        return null;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {

    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {

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
        return null;
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) throws Exception {
        return null;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        return null;
    }

    @Override
    public List<Profile<Long>> getProfiles() throws Exception {
        return null;
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
}
