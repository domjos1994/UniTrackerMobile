/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
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

import android.content.Context;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.Attachment;
import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.History;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.model.issues.Profile;
import de.domjos.unitrackerlibrary.model.issues.Relationship;
import de.domjos.unitrackerlibrary.model.issues.Tag;
import de.domjos.unitrackerlibrary.model.issues.User;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.AzureDevOpsPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;
import kotlin.text.Charsets;

public final class AzureDevOps extends JSONEngine implements IBugService<String> {
    private final Authentication authentication;
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:SS'Z'";

    public AzureDevOps(Authentication authentication) {
        super(authentication, (route, response) -> response.request().newBuilder().header("Authorization", "Basic " + Base64.encodeToString((":" + authentication.getAPIKey()).getBytes(Charsets.US_ASCII), Base64.DEFAULT)).build());
        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() {
        try {
            List<Project<String>> projects = this.getProjects();
            if(projects.isEmpty()) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public String getTrackerVersion() {
        return "5.0";
    }

    @Override
    public List<Project<String>> getProjects() throws Exception {
        List<Project<String>> projects = new LinkedList<>();
        int status = this.executeRequest("/_apis/projects?api-version=5.1");
        if(status == 203) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            JSONArray projectArray = jsonObject.getJSONArray("value");
            for(int i = 0; i<=projectArray.length()-1; i++) {
                JSONObject projectObject = projectArray.getJSONObject(i);
                Project<String> project = new Project<>();
                project.setId(projectObject.getString("id"));
                project.setTitle(projectObject.getString("name"));
                project.setEnabled(!projectObject.getString("visibility").equals("private"));
                project.setUpdatedAt(ConvertHelper.convertStringToDate(projectObject.getString("lastUpdateTime"), AzureDevOps.DATE_TIME_FORMAT).getTime());
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Project<String> getProject(String id) throws Exception {
        return null;
    }

    @Override
    public String insertOrUpdateProject(Project<String> project) throws Exception {
        return null;
    }

    @Override
    public void deleteProject(String id) throws Exception {

    }

    @Override
    public List<Version<String>> getVersions(String filter, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateVersion(Version<String> version, String project_id) throws Exception {

    }

    @Override
    public void deleteVersion(String id, String project_id) throws Exception {

    }

    @Override
    public long getMaximumNumberOfIssues(String project_id, IssueFilter filter) throws Exception {
        return 0;
    }

    @Override
    public List<Issue<String>> getIssues(String project_id) throws Exception {
        return null;
    }

    @Override
    public List<Issue<String>> getIssues(String project_id, IssueFilter filter) throws Exception {
        return null;
    }

    @Override
    public List<Issue<String>> getIssues(String project_id, int page, int numberOfItems) throws Exception {
        return null;
    }

    @Override
    public List<Issue<String>> getIssues(String project_id, int page, int numberOfItems, IssueFilter filter) throws Exception {
        return null;
    }

    @Override
    public Issue<String> getIssue(String id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateIssue(Issue<String> issue, String project_id) throws Exception {

    }

    @Override
    public void deleteIssue(String id, String project_id) throws Exception {

    }

    @Override
    public List<Note<String>> getNotes(String issue_id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateNote(Note<String> note, String issue_id, String project_id) throws Exception {

    }

    @Override
    public void deleteNote(String id, String issue_id, String project_id) throws Exception {

    }

    @Override
    public List<Attachment<String>> getAttachments(String issue_id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<String> attachment, String issue_id, String project_id) throws Exception {

    }

    @Override
    public void deleteAttachment(Object id, String issue_id, String project_id) throws Exception {

    }

    @Override
    public List<Relationship<String>> getBugRelations(String issue_id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<String> relationship, String issue_id, String project_id) throws Exception {

    }

    @Override
    public void deleteBugRelation(Relationship<String> relationship, String issue_id, String project_id) throws Exception {

    }

    @Override
    public List<User<String>> getUsers(String project_id) throws Exception {
        return null;
    }

    @Override
    public User<String> getUser(String id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateUser(User<String> user, String project_id) throws Exception {

    }

    @Override
    public void deleteUser(String id, String project_id) throws Exception {

    }

    @Override
    public List<CustomField<String>> getCustomFields(String project_id) throws Exception {
        return null;
    }

    @Override
    public CustomField<String> getCustomField(String id, String project_id) throws Exception {
        return null;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<String> customField, String project_id) throws Exception {

    }

    @Override
    public void deleteCustomField(String id, String project_id) throws Exception {

    }

    @Override
    public List<String> getCategories(String project_id) throws Exception {
        return null;
    }

    @Override
    public List<Tag<String>> getTags(String project_id) throws Exception {
        return null;
    }

    @Override
    public List<History<String>> getHistory(String issue_id, String project_id) throws Exception {
        return null;
    }

    @Override
    public List<Profile<String>> getProfiles() throws Exception {
        return null;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new AzureDevOpsPermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public Map<String, String> getEnums(Type type, Context context) throws Exception {
        return null;
    }

    @Override
    public List<History<String>> getNews() {
        return new LinkedList<>();
    }
}
