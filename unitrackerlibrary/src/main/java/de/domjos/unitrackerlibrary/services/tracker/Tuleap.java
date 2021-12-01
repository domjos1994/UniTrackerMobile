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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.TuleapPermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;

@SuppressWarnings("unused")
public final class Tuleap extends JSONEngine implements IBugService<Long> {
    private Authentication authentication;

    public Tuleap(Authentication authentication) {
        super(authentication);
        super.addHeader("X-Auth-AccessKey: " + authentication.getAPIKey());

        this.authentication = authentication;
    }

    @Override
    public boolean testConnection() throws Exception {
        int status = super.executeRequest("/api/users?query=" + this.authentication.getUserName() + "&limit=1");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            if (jsonArray.length() >= 1) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                return jsonObject.has("real_name");
            }
        }
        return false;
    }

    @Override
    public String getTrackerVersion() {
        return "1";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        int status = this.executeRequest("/api/projects?query={\"is_member_of\": true}");
        if (status == 200 || status == 201) {
            JSONArray jsonArray = new JSONArray(this.getCurrentMessage());
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Project<Long> project = new Project<>();
                project.setId(jsonObject.getLong("id"));
                project.setTitle(jsonObject.getString("label"));
                project.setAlias(jsonObject.getString("shortname"));
                project.setEnabled(jsonObject.getString("status").equals("active"));
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        Project<Long> project = new Project<>();
        int status = this.executeRequest("/api/projects/" + id);
        if (status == 200 || status == 201) {
            JSONObject jsonObject = new JSONObject(this.getCurrentMessage());
            project.setId(jsonObject.getLong("id"));
            project.setTitle(jsonObject.getString("label"));
            project.setAlias(jsonObject.getString("shortname"));
            project.setEnabled(jsonObject.getString("status").equals("active"));
        }
        return project;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) {
        return null;
    }

    @Override
    public void deleteProject(Long id) {

    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) {

    }

    @Override
    public void deleteVersion(Long id, Long project_id) {

    }

    @Override
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) {
        return 0;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id) {
        return null;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, IssueFilter filter) {
        return null;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems) {
        return null;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems, IssueFilter filter) {
        return null;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) {

    }

    @Override
    public void deleteIssue(Long id, Long project_id) {

    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id){

    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) {

    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {

    }

    @Override
    public void deleteAttachment(Object id, Long issue_id, Long project_id) {

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
    public List<User<Long>> getUsers(Long project_id) {
        return null;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) {

    }

    @Override
    public void deleteUser(Long id, Long project_id) {

    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) {
        return null;
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
        return null;
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) {
        return null;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public List<Profile<Long>> getProfiles() {
        return null;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new TuleapPermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public Map<String, String> getEnums(Type type, Context context)  {
        return null;
    }

    @Override
    public List<History<Long>> getNews() {
        return new LinkedList<>();
    }
}
