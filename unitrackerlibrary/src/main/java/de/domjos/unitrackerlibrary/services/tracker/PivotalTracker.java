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

import android.content.Context;

import de.domjos.unitrackerlibrary.model.issues.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.PivotalTrackerPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.customwidgets.utils.ConvertHelper;

public final class PivotalTracker extends JSONEngine implements IBugService<Long> {
    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private Authentication authentication;

    public PivotalTracker(Authentication authentication) {
        super(authentication);
        super.addHeader("X-TrackerToken: " + authentication.getAPIKey().trim());
        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = this.executeRequest("/services/v5/me");
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            return jsonObject.getString("api_token").trim().equals(this.authentication.getAPIKey().trim());
        }
        return false;
    }

    @Override
    public String getTrackerVersion() {
        return "v5";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/services/v5/projects");
        if (status == 200 || status == 201) {
            JSONArray projectsArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= projectsArray.length() - 1; i++) {
                Project<Long> project = new Project<>();
                JSONObject projectsObject = projectsArray.getJSONObject(i);
                project.setId(projectsObject.getLong("id"));
                project.setTitle(projectsObject.getString("name"));
                if (projectsObject.has("description")) {
                    project.setDescription(projectsObject.getString("description"));
                }
                project.setEnabled(projectsObject.getBoolean("public"));
                project.setCreatedAt(this.getDate("created_at", projectsObject));
                project.setUpdatedAt(this.getDate("updated_at", projectsObject));
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        Project<Long> project = new Project<>();
        int status = this.executeRequest("/services/v5/projects/" + id);
        if (status == 200 || status == 201) {
            JSONObject projectsObject = new JSONObject(this.getCurrentMessage());
            project.setId(projectsObject.getLong("id"));
            project.setTitle(projectsObject.getString("name"));
            if (projectsObject.has("description")) {
                project.setDescription(projectsObject.getString("description"));
            }
            project.setEnabled(projectsObject.getBoolean("public"));
            project.setCreatedAt(this.getDate("created_at", projectsObject));
            project.setUpdatedAt(this.getDate("updated_at", projectsObject));
        }
        return project;
    }

    private long getDate(String key, JSONObject object) throws Exception {
        Date dt = ConvertHelper.convertStringToDate(object.getString(key), PivotalTracker.DATE_FORMAT);
        if(dt!=null) {
            return dt.getTime();
        }
        return 0;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", project.getTitle());
        jsonObject.put("description", project.getDescription());
        jsonObject.put("public", project.isEnabled());

        int status;
        if (project.getId() != null) {
            status = this.executeRequest("/services/v5/projects/" + project.getId(), jsonObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/services/v5/projects", jsonObject.toString(), "POST");
        }

        if (status == 200 || status == 201) {
            JSONObject response = new JSONObject(this.getCurrentMessage());
            return response.getLong("id");
        }

        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        this.deleteRequest("/services/v5/projects/" + id);
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        int status = this.executeRequest("/services/v5/projects/" + project_id + "/releases");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                Version<Long> version = new Version<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                version.setId(jsonObject.getLong("id"));
                version.setTitle(jsonObject.getString("name"));
                version.setReleasedVersionAt(ConvertHelper.convertStringToDate(jsonObject.getString("deadline"), PivotalTracker.DATE_FORMAT).getTime());
                versions.add(version);
            }
        }
        return versions;
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

        String state = "?filter=-type:release&";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                state = "?filter=(state:delivered%20OR%20state:finished%20OR%20state:rejected)%20AND%20-type:release&";
            } else {
                state = "?filter=(state:accepted%20OR%20state:started%20OR%20state:planned%20OR%20state:unstarted%20OR%20state:unscheduled)%20AND%20-type:release&";
            }
        }

        int status = this.executeRequest("/services/v5/projects/" + project_id + "/stories" + state);
        if (status == 200 || status == 201) {
            return new JSONArray(this.getCurrentMessage()).length();
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
        List<Issue<Long>> issues = new LinkedList<>();
        String pagination = "";
        if (numberOfItems != -1) {
            pagination = "offset=" + ((page * numberOfItems) - 1) + "&limit=" + page;
        }

        String state = "?filter=-type:release&";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                state = "?filter=(state:delivered%20OR%20state:finished%20OR%20state:rejected)%20AND%20-type:release&";
            } else {
                state = "?filter=(state:accepted%20OR%20state:started%20OR%20state:planned%20OR%20state:unstarted%20OR%20state:unscheduled)%20AND%20-type:release&";
            }
        }

        int status = this.executeRequest("/services/v5/projects/" + project_id + "/stories" + state + pagination);
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                Issue<Long> issue = new Issue<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                issue.setId(jsonObject.getLong("id"));
                issue.setTitle(jsonObject.getString("name"));
                if (jsonObject.has("description")) {
                    issue.setDescription(jsonObject.getString("description"));
                }
                String st = jsonObject.getString("current_state").toLowerCase();
                issue.getHints().put(Issue.RESOLVED, String.valueOf(st.equals("delivered")||st.equals("finished")||st.equals("rejected")));

                issues.add(issue);
            }
        }

        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        Issue<Long> issue = new Issue<>();
        int status = this.executeRequest("/services/v5/projects/" + project_id + "/stories/" + id);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            issue.setId(jsonObject.getLong("id"));
            issue.setTitle(jsonObject.getString("name"));
            if (jsonObject.has("description")) {
                issue.setDescription(jsonObject.getString("description"));
            }
            issue.setSubmitDate(ConvertHelper.convertStringToDate(jsonObject.getString("created_at"), PivotalTracker.DATE_FORMAT));
            issue.setLastUpdated(ConvertHelper.convertStringToDate(jsonObject.getString("updated_at"), PivotalTracker.DATE_FORMAT));

            String state = jsonObject.getString("current_state");
            state = state.substring(0, 1).toUpperCase() + state.substring(1);
            List<String> states = Arrays.asList("Unstarted", "Started", "Finished", "Delivered", "Rejected", "Accepted");
            issue.setStatus(states.indexOf(state), state);

            String severity = jsonObject.getString("story_type");
            severity = severity.substring(0, 1).toUpperCase() + severity.substring(1);
            List<String> severities = Arrays.asList("Bug", "Feature", "Chore");
            issue.setSeverity(severities.indexOf(severity), severity);

            JSONArray tagArray = jsonObject.getJSONArray("labels");
            StringBuilder tagBuilder = new StringBuilder();
            for (int i = 0; i <= tagArray.length() - 1; i++) {
                JSONObject tagObject = tagArray.getJSONObject(i);
                tagBuilder.append(tagObject.getString("name"));
                tagBuilder.append(",");
            }
            issue.setTags(tagBuilder.toString());

            List<Note<Long>> notes = this.getNotes(issue.getId(), project_id);
            if (!notes.isEmpty()) {
                issue.getNotes().addAll(notes);
            }
        }
        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kind", "history");
        jsonObject.put("name", issue.getTitle());
        jsonObject.put("description", issue.getDescription());
        jsonObject.put("story_type", issue.getSeverity().getValue());

        if(issue.getSeverity().getKey() == 2) {
            if(issue.getStatus().getKey() == 2 || issue.getStatus().getKey() == 3 || issue.getStatus().getKey() == 4) {
                jsonObject.put("current_state", "Accepted");
            } else {
                jsonObject.put("current_state", issue.getStatus().getValue());
            }
        } else {
            jsonObject.put("current_state", issue.getStatus().getValue());
        }


        if(issue.getSeverity().getKey() == 1) {
            if (issue.getStatus().getKey() <= 2) {
                jsonObject.put("estimate", issue.getStatus().getKey());
            } else {
                jsonObject.put("estimate", 3);
            }
        }

        if(!issue.getTags().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String tag : issue.getTags().split(",")) {
                JSONObject tagObject = new JSONObject();
                tagObject.put("name", tag.trim());
                jsonArray.put(tagObject);
            }
            jsonObject.put("labels", jsonArray);
        }

        int status;
        if (issue.getId() != null) {
            status = this.executeRequest("/services/v5/projects/" + project_id + "/stories/" + issue.getId(), jsonObject.toString(), "PUT");
        } else {
            status = this.executeRequest("/services/v5/projects/" + project_id + "/stories", jsonObject.toString(), "POST");
        }

        if (status == 200 || status == 201) {
            if (issue.getId() == null) {
                JSONObject response = new JSONObject(this.getCurrentMessage());
                issue.setId(response.getLong("id"));
            }

            List<Note<Long>> oldNotes = this.getNotes(Long.parseLong(String.valueOf(issue.getId())), project_id);
            for (Note<Long> oldNote : oldNotes) {
                boolean exists = false;
                for (Note<Long> newNote : issue.getNotes()) {
                    if (oldNote.getId().equals(newNote.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    this.deleteNote(oldNote.getId(), Long.parseLong(String.valueOf(issue.getId())), project_id);
                }
            }

            for (Note<Long> note : issue.getNotes()) {
                this.insertOrUpdateNote(note, Long.parseLong(String.valueOf(issue.getId())), project_id);
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        this.deleteRequest("/services/v5/projects/" + project_id + "/stories/" + id);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) throws Exception {
        List<Note<Long>> notes = new LinkedList<>();
        int status = this.executeRequest("/services/v5/projects/" + project_id + "/stories/" + issue_id + "/comments");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Note<Long> note = new Note<>();
                note.setId(jsonObject.getLong("id"));
                String text = jsonObject.getString("text");
                if (text.length() >= 50) {
                    note.setTitle(text.substring(0, 50));
                } else {
                    note.setTitle(text);
                }
                note.setDescription(text);
                note.setSubmitDate(ConvertHelper.convertStringToDate(jsonObject.getString("created_at"), PivotalTracker.DATE_FORMAT));
                note.setLastUpdated(ConvertHelper.convertStringToDate(jsonObject.getString("updated_at"), PivotalTracker.DATE_FORMAT));
                notes.add(note);
            }
        }
        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("project_id", project_id);
        jsonObject.put("story_id", issue_id);
        jsonObject.put("text", note.getDescription());

        if (note.getId() != null) {
            this.executeRequest("/services/v5/projects/" + project_id + "/stories/" + issue_id + "/comments/" + note.getId(), jsonObject.toString(), "PUT");
        } else {
            this.executeRequest("/services/v5/projects/" + project_id + "/stories/" + issue_id + "/comments", jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {
        this.deleteRequest("/services/v5/projects/" + project_id + "/stories/" + issue_id + "/comments/" + id);
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
    public List<Relationship<Long>> getBugRelations(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<Long> relationship, Long issue_id, Long project_id) {

    }

    @Override
    public void deleteBugRelation(Relationship<Long> relationship, Long issue_id, Long project_id) {

    }

    @Override
    public List<User<Long>> getUsers(Long project_id) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        int status = this.executeRequest("/services/v5/account_summaries");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                status = this.executeRequest("/services/v5/accounts/" + jsonArray.getJSONObject(i).getString("id") + "/memberships");
                if (status == 200 || status == 201) {
                    JSONArray array = new JSONArray(this.getCurrentMessage());
                    for (int j = 0; j <= array.length() - 1; j++) {
                        JSONObject object = array.getJSONObject(j).getJSONObject("person");
                        User<Long> user = new User<>();
                        user.setId(object.getLong("id"));
                        user.setTitle(object.getString("username"));
                        user.setRealName(object.getString("name"));
                        user.setEmail(object.getString("email"));
                        user.getHints().put("id", jsonArray.getJSONObject(i).getString("id"));
                        users.add(user);
                    }
                }
            }
        }

        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) {
        return new User<>();
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account_id", user.getHints().get("id"));
        jsonObject.put("person_id", user.getId());
        jsonObject.put("name", user.getRealName());
        jsonObject.put("email", user.getEmail());
        jsonObject.put("admin", true);

        if (user.getId() == null) {
            this.executeRequest("/services/v5/accounts/" + user.getHints().get("id") + "/memberships", jsonObject.toString(), "POST");
        }
    }

    @Override
    public void deleteUser(Long id, Long project_id) throws Exception {
        List<User<Long>> users = this.getUsers(project_id);
        for (User<Long> user : users) {
            if (user.getId().equals(id)) {
                this.deleteRequest("/services/v5/accounts/" + user.getHints().get("id") + "/memberships/" + user.getId());
                break;
            }
        }
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> customField, Long project_id) {
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) {
    }

    @Override
    public List<String> getCategories(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) throws Exception {
        List<Tag<Long>> tags = new LinkedList<>();
        int status = this.executeRequest("/service/v5/projects/" + project_id + "/labels");
        if (status == 200 || status == 201) {
            try {
                JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
                for (int i = 0; i <= jsonArray.length() - 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Tag<Long> tag = new Tag<>();
                    tag.setId(jsonObject.getLong("id"));
                    tag.setTitle(jsonObject.getString("name"));
                    tags.add(tag);
                }
            } catch (Exception ignored) {
            }
        }

        return tags;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        List<History<Long>> histories = new LinkedList<>();
        int status = this.executeRequest("/services/v5/projects/" + project_id + "/stories/" + issue_id + "/activity");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());

            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                History<Long> history = new History<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject changeObject = jsonObject.getJSONArray("changes").getJSONObject(0);

                if (changeObject.has("original_values")) {
                    JSONObject originalObject = changeObject.getJSONObject("original_values");
                    JSONObject newObject = changeObject.getJSONObject("new_values");
                    JSONObject personObject = jsonObject.getJSONObject("performed_by");

                    JSONArray nameArray = originalObject.names();
                    String field = "";
                    if (nameArray != null) {
                        for (int k = 0; k <= nameArray.length() - 1; k++) {
                            String value = nameArray.getString(k);
                            if (!value.equals("updated_at")) {
                                field = value;
                                break;
                            }
                        }
                    }
                    if (!field.isEmpty()) {
                        history.setField(field);
                        history.setOldValue(originalObject.getString(field));
                        history.setNewValue(newObject.getString(field));
                        if (jsonObject.has("occured_at")) {
                            history.setTime(ConvertHelper.convertStringToDate(jsonObject.getString("occured_at"), PivotalTracker.DATE_FORMAT).getTime());
                        }
                        history.setUser(personObject.getString("initials"));
                        histories.add(history);
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
        return new PivotalTrackerPermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public Map<String, String> getEnums(Type type, Context context)  {
        return new LinkedHashMap<>();
    }

    @NotNull
    @Override
    public String toString() {
        return this.authentication.getTitle();
    }

    @Override
    public List<History<Long>> getNews() throws Exception {
        List<History<Long>> histories = new LinkedList<>();
        int status = this.executeRequest("/services/v5/my/activity");
        if(status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for(int i = 0; i<=jsonArray.length()-1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                History<Long> history = new History<>();
                history.setTime(this.getTime(jsonObject));
                history.setProject(this.getProject(jsonObject));
                history.setTitle(this.getTitle(jsonObject));
                history.setDescription(jsonObject.getString("message"));
                histories.add(history);
            }
        }
        return histories;
    }

    private long getTime(JSONObject jsonObject) {
        try {
            if(jsonObject.has("occurred_at")) {
                String date = jsonObject.getString("occurred_at");
                Date dt = ConvertHelper.convertStringToDate(date, PivotalTracker.DATE_FORMAT);
                if(dt != null) {
                    return dt.getTime();
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private Project<Long> getProject(JSONObject jsonObject) {
        try {
            if(jsonObject.has("project")) {
                JSONObject projectObject = jsonObject.getJSONObject("project");
                if(projectObject.has("name")) {
                    Project<Long> project = new Project<>();
                    project.setTitle(projectObject.getString("name"));
                    return project;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getTitle(JSONObject jsonObject) {
        try {
            StringBuilder title = new StringBuilder();
            if(jsonObject.has("primary_resources")) {
                JSONArray jsonArray = jsonObject.getJSONArray("primary_resources");
                for(int i = 0; i<=jsonArray.length()-1; i++) {
                    JSONObject itemObject = jsonArray.getJSONObject(i);
                    if(itemObject.has("name")) {
                        title.append(itemObject.getString("name")).append(", ");
                    }
                }
            }
            return (title.toString() + "))").replace(", ))", "").trim();
        } catch (Exception ignored) {}
        return "";
    }
}
