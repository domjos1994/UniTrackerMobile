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

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.model.issues.*;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.text.SimpleDateFormat;
import java.util.*;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.MantisBTPermissions;
import de.domjos.unitrackerlibrary.services.ArrayHelper;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.SoapEngine;
import de.domjos.customwidgets.utils.ConvertHelper;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public final class MantisBT extends SoapEngine implements IBugService<Long> {
    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private String currentMessage;
    private final String LIST_ISSUE_ACTION;
    private Authentication authentication;
    private int state;
    private boolean showSub, showFilter;

    public MantisBT(Authentication authentication, boolean showSub, boolean showFilter) {
        super(authentication, "/api/soap/mantisconnect.php");
        this.authentication = authentication;
        this.currentMessage = "";
        this.state = 0;
        this.showSub = showSub;
        this.showFilter = showFilter;

        if(this.showFilter) {
            this.LIST_ISSUE_ACTION = "mc_project_get_issue_headers";
        } else {
            this.LIST_ISSUE_ACTION = "mc_filter_search_issue_headers";
        }
    }

    @Override
    public boolean testConnection() throws Exception {
        if (super.isWSDLAvailable()) {
            SoapObject request = new SoapObject(super.soapPath, "mc_login");
            return this.executeAction(request, "mc_login", true) instanceof SoapObject;
        }
        return false;
    }

    @Override
    public String getTrackerVersion() throws Exception {
        SoapObject request = new SoapObject(super.soapPath, "mc_version");
        Object object = this.executeAction(request, "mc_version", false);
        Object result = this.getResult(object);
        if (result != null) {
            return result.toString();
        }
        return "";
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, "mc_projects_get_user_accessible");
        Object object = this.executeAction(request, "mc_projects_get_user_accessible", true);
        Object result = this.getResult(object);
        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) result;
            if (vector != null) {
                for (int i = 0; i <= vector.size() - 1; i++) {
                    SoapObject soapObject = (SoapObject) vector.get(i);
                    projects.add(this.soapToProject(soapObject, null));
                }
            }
        }

        List<Project<Long>> projectsAndSubs = new LinkedList<>();
        for (int i = 0; i <= projects.size() - 1; i++) {
            projectsAndSubs = this.getProject(projects.get(i), "", projectsAndSubs);
        }

        return projectsAndSubs;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        List<Project<Long>> projects = this.getProjects();
        for (int i = 0; i <= projects.size() - 1; i++) {
            if (projects.get(i).getId().equals(id)) {
                return projects.get(i);
            }
        }
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        String action;
        SoapObject request;
        if (project.getId() == null) {
            action = "mc_project_add";
            request = new SoapObject(super.soapPath, action);
        } else {
            action = "mc_project_update";
            request = new SoapObject(super.soapPath, action);
            request.addProperty("project_id", project.getId());
        }

        SoapObject soapObject = this.projectToSoap(project);
        Vector<SoapObject> vector = new Vector<>();
        int i = 0;
        for (Project<Long> sub : project.getSubProjects()) {
            vector.add(i, this.projectToSoap(sub));
            i++;
        }
        soapObject.addProperty("subprojects", vector);
        request.addProperty("project", soapObject);

        Object object = this.executeAction(request, action, true);
        object = this.getResult(object);

        if (object != null) {
            this.state = 200;
            if (project.getId() == null) {
                try {
                    SoapPrimitive soapPrimitive = (SoapPrimitive) object;
                    return Long.parseLong(String.valueOf(soapPrimitive.getValue()));
                } catch (Exception ignored) {
                }

                return (long) object;
            } else {
                return project.getId();
            }
        }
        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        SoapObject request = new SoapObject(super.soapPath, "mc_project_delete");
        request.addProperty("project_id", id);
        this.executeAction(request, "mc_project_delete", true);
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        return this.getVersionsByFilter("mc_project_get_" + filter, project_id);
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) throws Exception {
        if(!version.getTitle().trim().isEmpty()) {
            String action;
            SoapObject request;
            if (version.getId() == null) {
                action = "mc_project_version_add";
                request = new SoapObject(super.soapPath, action);
            } else {
                action = "mc_project_version_update";
                request = new SoapObject(super.soapPath, action);
                request.addProperty("version_id", version.getId());
            }

            SoapObject projectData = new SoapObject(NAMESPACE, "ProjectVersionData");
            projectData.addProperty("id", version.getId());
            projectData.addProperty("name", version.getTitle());
            projectData.addProperty("project_id", project_id);

            if (version.getReleasedVersionAt() != 0) {
                Date dt = new Date();
                dt.setTime(version.getReleasedVersionAt());
                SimpleDateFormat sdf = new SimpleDateFormat(MantisBT.DATE_TIME_FORMAT, Locale.GERMAN);
                projectData.addProperty("date_order", sdf.format(dt));
            } else {
                projectData.addProperty("date_order", null);
            }
            projectData.addProperty("description", version.getDescription());
            projectData.addProperty("released", version.isReleasedVersion());
            projectData.addProperty("obsolete", version.isDeprecatedVersion());
            request.addProperty("version", projectData);

            Object object = this.executeAction(request, action, true);
            this.getResult(object);
        }
    }

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        SoapObject request = new SoapObject(super.soapPath, "mc_project_version_delete");
        request.addProperty("version_id", String.valueOf(id));
        this.executeAction(request, "mc_project_version_delete", true);
    }

    @Override
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) throws Exception {
        SoapObject request = new SoapObject(super.soapPath, this.LIST_ISSUE_ACTION);
        if(this.showFilter) {
            request.addProperty("project_id", project_id);
        } else {
            SoapObject filterObject = new SoapObject(NAMESPACE, "FilterSearchData");
            Vector<Integer> projects = new Vector<>();
            projects.add(Integer.parseInt(String.valueOf(project_id)));
            filterObject.addProperty("project_id", projects);
            request.addProperty("filter", filterObject);
        }
        request.addProperty("page_number", 1);
        request.addProperty("per_page", -1);
        Object object = this.executeAction(request, this.LIST_ISSUE_ACTION, true);
        object = this.getResult(object);
        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) object;
            return vector.size();
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
        List<Issue<Long>> issues = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, this.LIST_ISSUE_ACTION);
        if(this.showFilter) {
            request.addProperty("project_id", pid);
        } else {
            SoapObject filterObject = new SoapObject(NAMESPACE, "FilterSearchData");
            Vector<Integer> projects = new Vector<>();
            projects.add(Integer.parseInt(String.valueOf(pid)));
            filterObject.addProperty("project_id", projects);
            request.addProperty("filter", filterObject);
        }
        request.addProperty("page_number", page);
        request.addProperty("per_page", numberOfItems);

        Map<String, String> enumView = this.getEnums(Type.view_state, null);
        Map<String, String> enumStatus = this.getEnums(Type.status, null);
        Object object = this.executeAction(request, this.LIST_ISSUE_ACTION, true);
        object = this.getResult(object);
        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) object;
            SimpleDateFormat sdf = new SimpleDateFormat(MantisBT.DATE_TIME_FORMAT, Locale.GERMAN);
            for (int i = 0; i <= vector.size() - 1; i++) {
                if (vector.get(i) instanceof SoapObject) {
                    SoapObject soapObject = (SoapObject) vector.get(i);
                    String id = soapObject.getPropertyAsString("id");

                    if (!this.showSub) {
                        try {
                            String project = soapObject.getPropertyAsString("project");
                            Long project_id = Long.parseLong(project);
                            if (!pid.equals(project_id)) {
                                continue;
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    String category = soapObject.getPropertyAsString("category");
                    String summary = soapObject.getPropertyAsString("summary");
                    String view = soapObject.getPropertyAsString("view_state");
                    Date dt = new Date();
                    if (soapObject.hasProperty("last_updated")) {
                        dt = sdf.parse(soapObject.getPropertyAsString("last_updated"));
                    }

                    for (Map.Entry<String, String> entry : enumView.entrySet()) {
                        if (view.equals(entry.getKey())) {
                            view = entry.getValue().trim();
                            break;
                        }
                    }

                    boolean fitsInFilter = true;
                    String status = soapObject.getPropertyAsString("status");
                    boolean resolved = Integer.parseInt(status) >= 80;
                    switch (filter) {
                        case resolved:
                            if (!resolved) {
                                fitsInFilter = false;
                            }
                            break;
                        case unresolved:
                            if (resolved) {
                                fitsInFilter = false;
                            }
                            break;
                    }
                    if (!fitsInFilter) {
                        continue;
                    }

                    for (Map.Entry<String, String> entry : enumStatus.entrySet()) {
                        if (status.equals(entry.getKey())) {
                            status = entry.getValue().trim();
                            break;
                        }
                    }

                    Issue<Long> issue = new Issue<>();
                    issue.setId(Long.parseLong(id));
                    issue.setTitle(summary);
                    issue.setDescription(String.format("%s - %s - %s", category, view, status));
                    issue.setLastUpdated(dt);
                    issue.getHints().put(Issue.RESOLVED, String.valueOf(resolved));
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
        return this.getIssue(id, true);
    }

    public Issue<Long> getIssue(Long id, boolean showRelations) throws Exception {
        Issue<Long> issue = new Issue<>();

        if (id != null) {
            if (id != 0) {
                SoapObject request = new SoapObject(super.soapPath, "mc_issue_get");
                request.addProperty("issue_id", Integer.parseInt(String.valueOf(id)));
                Object object = this.executeAction(request, "mc_issue_get", true);
                object = this.getResult(object);
                if (object instanceof SoapObject) {
                    SoapObject soapObject = (SoapObject) object;
                    issue.setTitle(soapObject.getPropertyAsString("summary"));
                    issue.setDescription(soapObject.getPropertyAsString("description"));
                    issue.setCategory(soapObject.getPropertyAsString("category"));
                    issue.setId(Long.parseLong(soapObject.getPropertyAsString("id")));
                    if (soapObject.hasProperty("version")) {
                        issue.setVersion(soapObject.getPropertyAsString("version"));
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat(MantisBT.DATE_TIME_FORMAT, Locale.GERMAN);
                    if (soapObject.hasProperty("date_submitted")) {
                        issue.setSubmitDate(sdf.parse(soapObject.getPropertyAsString("date_submitted")));
                    }
                    if (soapObject.hasProperty("last_updated")) {
                        issue.setLastUpdated(sdf.parse(soapObject.getPropertyAsString("last_updated")));
                    }
                    try {
                        if (soapObject.hasProperty("due_date")) {
                            issue.setDueDate(sdf.parse(soapObject.getPropertyAsString("due_date")));
                        }
                    } catch (Exception ex) {
                        issue.setDueDate(null);
                    }

                    if (soapObject.hasProperty("fixed_in_version")) {
                        issue.setFixedInVersion(soapObject.getPropertyAsString("fixed_in_version"));
                    }
                    if (soapObject.hasProperty("target_version")) {
                        issue.setTargetVersion(soapObject.getPropertyAsString("target_version"));
                    }

                    if (soapObject.hasProperty("view_state")) {
                        SoapObject viewObject = (SoapObject) soapObject.getProperty("view_state");
                        issue.setState(Integer.parseInt(viewObject.getPropertyAsString("id")), viewObject.getPropertyAsString("name"));
                    }

                    if (soapObject.hasProperty("priority")) {
                        SoapObject priorityObject = (SoapObject) soapObject.getProperty("priority");
                        issue.setPriority(Integer.parseInt(priorityObject.getPropertyAsString("id")), priorityObject.getPropertyAsString("name"));
                    }

                    if (soapObject.hasProperty("severity")) {
                        SoapObject severityObject = (SoapObject) soapObject.getProperty("severity");
                        issue.setSeverity(Integer.parseInt(severityObject.getPropertyAsString("id")), severityObject.getPropertyAsString("name"));
                    }

                    if (soapObject.hasProperty("status")) {
                        SoapObject statusObject = (SoapObject) soapObject.getProperty("status");
                        issue.setStatus(Integer.parseInt(statusObject.getPropertyAsString("id")), statusObject.getPropertyAsString("name"));
                    }

                    if (soapObject.hasProperty("reproducibility")) {
                        SoapObject reproducibilityObject = (SoapObject) soapObject.getProperty("reproducibility");
                        issue.setReproducibility(Integer.parseInt(reproducibilityObject.getPropertyAsString("id")), reproducibilityObject.getPropertyAsString("name"));
                    }

                    if (soapObject.hasProperty("resolution")) {
                        SoapObject resolutionObject = (SoapObject) soapObject.getProperty("resolution");
                        issue.setResolution(Integer.parseInt(resolutionObject.getPropertyAsString("id")), resolutionObject.getPropertyAsString("name"));
                    }

                    if (soapObject.hasProperty("steps_to_reproduce")) {
                        issue.setStepsToReproduce(soapObject.getPropertyAsString("steps_to_reproduce"));
                    }

                    if (soapObject.hasProperty("additional_information")) {
                        issue.setAdditionalInformation(soapObject.getPropertyAsString("additional_information"));
                    }

                    Profile<Long> profile = new Profile<>();
                    if (soapObject.hasProperty("platform")) {
                        profile.setPlatform(soapObject.getPropertyAsString("platform"));
                    }
                    if (soapObject.hasProperty("os")) {
                        profile.setOs(soapObject.getPropertyAsString("os"));
                    }
                    if (soapObject.hasProperty("os_build")) {
                        profile.setOs_build(soapObject.getPropertyAsString("os_build"));
                    }
                    issue.setProfile(profile);

                    if (soapObject.hasProperty("handler")) {
                        SoapObject handlerObject = (SoapObject) soapObject.getProperty("handler");
                        User<Long> user = new User<>();
                        user.setId(Long.parseLong(handlerObject.getPropertyAsString("id")));
                        user.setTitle(handlerObject.getPropertyAsString("name"));
                        if (handlerObject.hasProperty("real_name")) {
                            user.setRealName(handlerObject.getPropertyAsString("real_name"));
                        }
                        if (handlerObject.hasProperty("email")) {
                            user.setEmail(handlerObject.getPropertyAsString("email"));
                        }
                        issue.setHandler(user);
                    }

                    if (soapObject.hasProperty("notes")) {
                        if (soapObject.getProperty("notes") instanceof Vector) {
                            Vector<?> vector = (Vector<?>) soapObject.getProperty("notes");
                            for (int i = 0; i <= vector.size() - 1; i++) {
                                if (vector.get(i) instanceof SoapObject) {
                                    SoapObject noteObject = (SoapObject) vector.get(i);
                                    Note<Long> note = new Note<>();
                                    note.setId(Long.parseLong(noteObject.getPropertyAsString("id")));
                                    note.setDescription(noteObject.getPropertyAsString("text"));
                                    if (note.getDescription().length() > 50) {
                                        note.setTitle(note.getDescription().substring(0, 50));
                                    } else {
                                        note.setTitle(note.getDescription());
                                    }
                                    if (noteObject.hasProperty("view_state")) {
                                        SoapObject viewObject = (SoapObject) noteObject.getProperty("view_state");
                                        note.setState(Integer.parseInt(viewObject.getPropertyAsString("id")), viewObject.getPropertyAsString("name"));
                                    }
                                    if (noteObject.hasProperty("date_submitted")) {
                                        note.setSubmitDate(sdf.parse(noteObject.getPropertyAsString("date_submitted")));
                                    }
                                    if (noteObject.hasProperty("last_modified")) {
                                        note.setLastUpdated(sdf.parse(noteObject.getPropertyAsString("last_modified")));
                                    }
                                    issue.getNotes().add(note);
                                }
                            }
                        }
                    }

                    if (soapObject.hasProperty("attachments")) {
                        if (soapObject.getProperty("attachments") instanceof Vector) {
                            Vector<?> vector = (Vector<?>) soapObject.getProperty("attachments");
                            for (int i = 0; i <= vector.size() - 1; i++) {
                                if (vector.get(i) instanceof SoapObject) {
                                    SoapObject attachmentObject = (SoapObject) vector.get(i);
                                    Attachment<Long> attachment = new Attachment<>();
                                    attachment.setId(Long.parseLong(attachmentObject.getPropertyAsString("id")));
                                    attachment.setFilename(attachmentObject.getPropertyAsString("filename"));
                                    attachment.setDownloadUrl(attachmentObject.getPropertyAsString("download_url"));
                                    issue.getAttachments().add(attachment);
                                }
                            }
                        }
                    }

                    for (int i = 0; i <= issue.getAttachments().size() - 1; i++) {
                        Attachment<Long> attachment = issue.getAttachments().get(i);
                        SoapObject getAttachmentObject = new SoapObject(super.soapPath, "mc_issue_attachment_get");
                        getAttachmentObject.addProperty("issue_attachment_id", Long.parseLong(String.valueOf(attachment.getId())));
                        Object getObject = this.executeAction(getAttachmentObject, "mc_issue_attachment_get", true);
                        if (getObject instanceof byte[]) {
                            attachment.setContent((byte[]) getObject);
                        }
                        issue.getAttachments().set(i, attachment);
                    }


                    if (soapObject.hasProperty("custom_fields")) {
                        SoapObject projectObject = (SoapObject) soapObject.getProperty("project");
                        List<CustomField<Long>> customFields = this.getCustomFields(Long.parseLong(projectObject.getPropertyAsString("id")));
                        Vector<?> vector = (Vector<?>) soapObject.getProperty("custom_fields");
                        for (int i = 0; i <= vector.size() - 1; i++) {
                            SoapObject fieldObject = (SoapObject) vector.get(i);
                            SoapObject fieldData = (SoapObject) fieldObject.getProperty("field");
                            Long fieldId = Long.parseLong(fieldData.getPropertyAsString("id"));
                            String value = fieldObject.getPropertyAsString("value");

                            for (CustomField<Long> customField : customFields) {
                                if (customField.getId().equals(fieldId)) {
                                    issue.getCustomFields().put(customField, value);
                                    break;
                                }
                            }
                        }
                    }

                    if (soapObject.hasProperty("tags")) {
                        Vector<?> vector = (Vector<?>) soapObject.getProperty("tags");
                        for (int i = 0; i <= vector.size() - 1; i++) {
                            issue.setTags(issue.getTags() + ((SoapObject) vector.get(i)).getPropertyAsString("name") + ", ");
                        }
                    }

                    if(showRelations) {
                        if(soapObject.hasProperty("relationships")) {
                            Vector<?> vector = (Vector<?>) soapObject.getProperty("relationships");
                            for(int i = 0; i<= vector.size() - 1; i++) {
                                SoapObject obj = (SoapObject) vector.get(i);
                                SoapObject typeObject = (SoapObject)obj.getProperty("type");
                                Issue<Long> targetIssue = this.getIssue(Long.parseLong(obj.getPropertyAsString("target_id")), false);
                                int type = Integer.parseInt(typeObject.getPropertyAsString("id"));
                                Relationship<Long> relationship = new Relationship<>();
                                relationship.setId(Long.parseLong(obj.getPropertyAsString("id")));
                                relationship.setIssue(targetIssue);
                                relationship.setType(new AbstractMap.SimpleEntry<>(typeObject.getPropertyAsString("name"), type));
                                issue.getRelations().add(relationship);
                            }
                        }
                    }
                }
            }
        }

        return issue;
    }

    @Override
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) throws Exception {
        String action;
        SoapObject request;
        if (issue.getId() != null) {
            action = "mc_issue_update";
            request = new SoapObject(super.soapPath, action);
            request.addProperty("issueId", Integer.parseInt(String.valueOf(issue.getId())));
        } else {
            action = "mc_issue_add";
            request = new SoapObject(super.soapPath, action);
        }
        SoapObject issueObject = new SoapObject(NAMESPACE, "IssueData");
        issueObject.addProperty("category", issue.getCategory());
        issueObject.addProperty("summary", issue.getTitle());
        issueObject.addProperty("description", issue.getDescription());
        issueObject.addProperty("steps_to_reproduce", issue.getStepsToReproduce());
        issueObject.addProperty("additional_information", issue.getAdditionalInformation());
        SimpleDateFormat sdf = new SimpleDateFormat(MantisBT.DATE_TIME_FORMAT, Locale.GERMAN);
        if (issue.getDueDate() != null) {
            issueObject.addProperty("due_date", sdf.format(issue.getDueDate()));
        }

        if (!issue.getVersion().equals("")) {
            issueObject.addProperty("version", issue.getVersion());
        }
        if (!issue.getFixedInVersion().equals("")) {
            issueObject.addProperty("fixed_in_version", issue.getFixedInVersion());
        }
        if (!issue.getTargetVersion().equals("")) {
            issueObject.addProperty("target_version", issue.getTargetVersion());
        }

        Project<Long> project = this.getProject(project_id);
        if (project != null) {
            SoapObject projectObject = new SoapObject(NAMESPACE, "ObjectRef");
            projectObject.addProperty("id", project_id);
            projectObject.addProperty("name", project.getTitle());
            issueObject.addProperty("project", projectObject);
        }

        SoapObject viewObject = new SoapObject(NAMESPACE, "ObjectRef");
        viewObject.addProperty("id", issue.getState().getKey());
        viewObject.addProperty("name", issue.getState().getValue());
        issueObject.addProperty("view_state", viewObject);

        SoapObject severityObject = new SoapObject(NAMESPACE, "ObjectRef");
        severityObject.addProperty("id", issue.getSeverity().getKey());
        severityObject.addProperty("name", issue.getSeverity().getValue());
        issueObject.addProperty("severity", severityObject);

        SoapObject priorityObject = new SoapObject(NAMESPACE, "ObjectRef");
        priorityObject.addProperty("id", issue.getPriority().getKey());
        priorityObject.addProperty("name", issue.getPriority().getValue());
        issueObject.addProperty("priority", priorityObject);

        SoapObject statusObject = new SoapObject(NAMESPACE, "ObjectRef");
        statusObject.addProperty("id", issue.getStatus().getKey());
        statusObject.addProperty("name", issue.getStatus().getValue());
        issueObject.addProperty("status", statusObject);

        SoapObject reproducibilityObject = new SoapObject(NAMESPACE, "ObjectRef");
        reproducibilityObject.addProperty("id", issue.getReproducibility().getKey());
        reproducibilityObject.addProperty("name", issue.getReproducibility().getValue());
        issueObject.addProperty("reproducibility", reproducibilityObject);

        SoapObject resolutionObject = new SoapObject(NAMESPACE, "ObjectRef");
        resolutionObject.addProperty("id", issue.getResolution().getKey());
        resolutionObject.addProperty("name", issue.getResolution().getValue());
        issueObject.addProperty("resolution", resolutionObject);

        if (issue.getProfile() != null) {
            issueObject.addProperty("platform", issue.getProfile().getPlatform());
            issueObject.addProperty("os", issue.getProfile().getOs());
            issueObject.addProperty("os_build", issue.getProfile().getOs_build());
        }

        if (issue.getHandler() != null) {
            SoapObject handlerObject = new SoapObject(NAMESPACE, "AccountData");
            handlerObject.addProperty("id", issue.getHandler().getId());
            handlerObject.addProperty("name", issue.getHandler().getTitle());
            handlerObject.addProperty("real_name", issue.getHandler().getRealName());
            handlerObject.addProperty("email", issue.getHandler().getEmail());
            issueObject.addProperty("handler", handlerObject);
        }

        if (!issue.getCustomFields().isEmpty()) {
            Vector<SoapObject> customFieldVector = new Vector<>();
            for (Map.Entry<CustomField<Long>, String> entry : issue.getCustomFields().entrySet()) {
                SoapObject soapObject = new SoapObject(NAMESPACE, "CustomFieldValueForIssueData");
                SoapObject fieldObject = new SoapObject(NAMESPACE, "ObjectRef");
                fieldObject.addProperty("id", entry.getKey().getId());
                fieldObject.addProperty("name", entry.getKey().getTitle());
                soapObject.addProperty("field", fieldObject);
                soapObject.addProperty("value", entry.getValue());
                customFieldVector.add(soapObject);
            }
            issueObject.addProperty("custom_fields", customFieldVector);
        }

        if (!issue.getTags().isEmpty()) {
            List<Tag<Long>> tags = this.getTags(project_id);
            Vector<SoapObject> tagVector = new Vector<>();
            for (String strTag : issue.getTags().split(",")) {
                if(!strTag.trim().isEmpty()) {
                    if(!tags.isEmpty()) {
                        for (Tag<Long> tag : tags) {
                            if (strTag.trim().equals(tag.getTitle())) {
                                SoapObject tagObject = new SoapObject(NAMESPACE, "ObjectRef");
                                tagObject.addProperty("id", tag.getId());
                                tagObject.addProperty("name", tag.getTitle());
                                tagVector.add(tagObject);
                            }
                        }
                    }
                }
            }
            issueObject.addProperty("tags", tagVector);
        }

        Object object = null;
        try {
            request.addProperty("issue", issueObject);
            object = this.executeAction(request, action, true);
            object = this.getResult(object);
        } catch (Exception ignored) {}

        long id = 0;
        if (issue.getId() != null) {
            id = Long.parseLong(String.valueOf(issue.getId()));
        } else {
            if(object != null) {
                id = Long.parseLong(String.valueOf(object));
            } else {
                List<Issue<Long>> issues = this.getIssues(project_id);
                for(Issue<Long> tmp : issues) {
                    if(tmp.getTitle().equals(issue.getTitle())) {
                        id = tmp.getId();
                        break;
                    }
                }
                if(id == 0) {
                    return;
                }
            }
        }

        if(id != 0) {
            Issue<Long> oldIssue = this.getIssue(id, project_id);
            List<Note<Long>> oldNotes = oldIssue.getNotes();
            List<Attachment<Long>> oldAttachments = oldIssue.getAttachments();

            for (Note<Long> oldNote : oldNotes) {
                boolean available = false;
                for (Note<Long> note : issue.getNotes()) {
                    if (oldNote.getId().equals(note.getId())) {
                        available = true;
                        break;
                    }
                }
                if (!available) {
                    this.deleteNote(oldNote.getId(), id, project_id);
                }
            }

            if (!issue.getNotes().isEmpty()) {
                for (DescriptionObject<Long> descriptionObject : issue.getNotes()) {
                    if(descriptionObject instanceof Note) {
                        Note<Long> note = (Note<Long>) descriptionObject;
                        this.insertOrUpdateNote(note, id, project_id);
                    }
                }
            }

            for (Attachment<Long> oldAttachment : oldAttachments) {
                this.deleteAttachment(oldAttachment.getId(), id, project_id);
            }

            if (!issue.getAttachments().isEmpty()) {
                for (DescriptionObject<Long> descriptionObject : issue.getAttachments()) {
                    if(descriptionObject instanceof Attachment) {
                        Attachment<Long> attachment = (Attachment<Long>) descriptionObject;
                        this.insertOrUpdateAttachment(attachment, id, project_id);
                    }
                }
            }

            if(!issue.getRelations().isEmpty()) {
                for(Relationship<Long> relationship : issue.getRelations()) {
                    if(issue.getId()!=null) {
                        this.insertOrUpdateBugRelations(relationship, Long.parseLong(String.valueOf(issue.getId())), project_id);
                    }
                }
            }
        }
    }

    @Override
    public void deleteIssue(Long id, Long project_id) throws Exception {
        SoapObject request = new SoapObject(super.soapPath, "mc_issue_delete");
        request.addProperty("issue_id", id);
        Object object = this.executeAction(request, "mc_issue_delete", true);
        this.getResult(object);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) throws Exception {
        String noteAction;
        SoapObject noteRequestObject;
        if (note.getId() != null) {
            noteAction = "mc_issue_note_update";
            noteRequestObject = new SoapObject(super.soapPath, noteAction);
        } else {
            noteAction = "mc_issue_note_add";
            noteRequestObject = new SoapObject(super.soapPath, noteAction);
            noteRequestObject.addProperty("issue_id", issue_id);
        }
        SoapObject noteObject = new SoapObject(NAMESPACE, "IssueNoteData");
        if (note.getId() != null) {
            noteObject.addProperty("id", note.getId());
        }
        noteObject.addProperty("text", note.getDescription());

        SoapObject viewNoteObject = new SoapObject(NAMESPACE, "ObjectRef");
        viewNoteObject.addProperty("id", note.getState().getKey());
        viewNoteObject.addProperty("name", note.getState().getValue());
        noteObject.addProperty("view_state", viewNoteObject);
        noteRequestObject.addProperty("note", noteObject);
        Object noteResult = this.executeAction(noteRequestObject, noteAction, true);
        this.getResult(noteResult);
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) throws Exception {
        SoapObject deleteRequest = new SoapObject(super.soapPath, "mc_issue_note_delete");
        deleteRequest.addProperty("issue_note_id", id);
        Object deleteObject = this.executeAction(deleteRequest, "mc_issue_note_delete", true);
        this.getResult(deleteObject);
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) throws Exception {
        SoapObject attachmentObject = new SoapObject(super.soapPath, "mc_issue_attachment_add");
        attachmentObject.addProperty("issue_id", issue_id);
        attachmentObject.addProperty("name", attachment.getFilename());
        attachmentObject.addProperty("file_type", "text");
        attachmentObject.addProperty("content", attachment.getContent());

        Object noteResult = this.executeAction(attachmentObject, "mc_issue_attachment_add", true);
        this.getResult(noteResult);
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) throws Exception {
        if(id != 0) {
            SoapObject deleteRequest = new SoapObject(super.soapPath, "mc_issue_attachment_delete");
            deleteRequest.addProperty("issue_attachment_id", id);
            Object deleteObject = this.executeAction(deleteRequest, "mc_issue_attachment_delete", true);
            this.getResult(deleteObject);
        }
    }

    @Override
    public List<Relationship<Long>> getBugRelations(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<Long> relationship, Long issue_id, Long project_id) throws Exception {
        SoapObject relationshipObject = new SoapObject(super.soapPath, "mc_issue_relationship_add");
        relationshipObject.addProperty("issue_id", issue_id);

        SoapObject soapObject = new SoapObject(NAMESPACE, "RelationshipData");
        soapObject.addProperty("id", relationship.getId());
        soapObject.addProperty("target_id", relationship.getIssue().getId());

        SoapObject objectRef = new SoapObject(NAMESPACE, "ObjectRef");
        objectRef.addProperty("id", relationship.getType().getValue());
        objectRef.addProperty("name", relationship.getType().getKey());
        soapObject.addProperty("type", objectRef);

        relationshipObject.addProperty("relationship", soapObject);
        Object noteResult = this.executeAction(relationshipObject, "mc_issue_relationship_add", true);
        this.getResult(noteResult);
    }

    @Override
    public void deleteBugRelation(Relationship<Long> relationship, Long issue_id, Long project_id) throws Exception {
        SoapObject relationshipObject = new SoapObject(super.soapPath, "mc_issue_relationship_delete");
        relationshipObject.addProperty("issue_id", issue_id);
        relationshipObject.addProperty("relationship_id", relationship.getId());
        Object deleteObject = this.executeAction(relationshipObject, "mc_issue_relationship_delete", true);
        this.getResult(deleteObject);
    }

    @Override
    public List<User<Long>> getUsers(Long pid) throws Exception {
        List<User<Long>> users = new LinkedList<>();
        SoapObject request = new SoapObject(NAMESPACE, "mc_project_get_users");
        request.addProperty("project_id", pid);
        request.addProperty("access", 25);
        Object object = this.executeAction(request, "mc_project_get_users", true);
        object = this.getResult(object);
        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) object;
            for (int i = 0; i <= vector.size() - 1; i++) {
                SoapObject soapObject = (SoapObject) vector.get(i);
                User<Long> user = new User<>();
                user.setId(Long.parseLong(soapObject.getPropertyAsString("id")));
                user.setTitle(soapObject.getPropertyAsString("name"));
                if (soapObject.hasProperty("real_name")) {
                    user.setRealName(soapObject.getPropertyAsString("real_name"));
                }
                if (soapObject.hasProperty("email")) {
                    user.setEmail(soapObject.getPropertyAsString("email"));
                }
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) {
        return new User<>();
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) {
    }

    @Override
    public void deleteUser(Long id, Long project_id) {
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long pid) throws Exception {
        List<CustomField<Long>> customFields = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, "mc_project_get_custom_fields");
        request.addProperty("project_id", pid);
        Object object = this.executeAction(request, "mc_project_get_custom_fields", true);
        object = this.getResult(object);

        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) object;
            for (int i = 0; i <= vector.size() - 1; i++) {
                Object field = vector.get(i);
                if (field instanceof SoapObject) {
                    CustomField<Long> customField = new CustomField<>();
                    SoapObject fieldObject = (SoapObject) field;
                    SoapObject fld = (SoapObject) fieldObject.getProperty("field");
                    customField.setId(Long.parseLong(fld.getPropertyAsString("id")));
                    customField.setTitle(fld.getPropertyAsString("name"));
                    customField.setType(Integer.parseInt(fieldObject.getPropertyAsString("type")));
                    customField.setPossibleValues(fieldObject.getPropertyAsString("possible_values"));
                    customField.setDefaultValue(fieldObject.getPropertyAsString("default_value"));
                    customField.setMinLength(Integer.parseInt(fieldObject.getPropertyAsString("length_min")));
                    customField.setMaxLength(Integer.parseInt(fieldObject.getPropertyAsString("length_max")));
                    customFields.add(customField);
                }
            }
        }
        return customFields;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) {
        return new CustomField<>();
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> user, Long project_id) {
    }

    @Override
    public void deleteCustomField(Long id, Long project_id) {
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) throws Exception {
        List<Tag<Long>> tags = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, "mc_tag_get_all");
        request.addProperty("page_number", 1);
        request.addProperty("per_page", -1);
        Object object = this.executeAction(request, "mc_tag_get_all", true);
        object = this.getResult(object);
        SoapObject soapObject = (SoapObject) object;
        if(soapObject!=null) {
            Vector<?> vector = (Vector<?>) soapObject.getProperty("results");
            for (int i = 0; i <= vector.size() - 1; i++) {
                SoapObject tagObject = (SoapObject) vector.get(i);
                Tag<Long> tag = new Tag<>();
                tag.setId(Long.parseLong(tagObject.getPropertyAsString("id")));
                tag.setTitle(tagObject.getPropertyAsString("name"));
                tag.setDescription(tagObject.getPropertyAsString("description"));
                tags.add(tag);
            }

        }

        return tags;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) throws Exception {
        List<History<Long>> histories = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, "mc_issue_get_history");
        request.addProperty("issue_id", issue_id);
        Object object = this.executeAction(request, "mc_issue_get_history", true);
        object = this.getResult(object);

        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) object;
            for (int i = 0; i <= vector.size() - 1; i++) {
                SoapObject soapObject = (SoapObject) vector.get(i);
                History<Long> history = new History<>();
                history.setTitle(
                        soapObject.getPropertyAsString("field") + ": " +
                                soapObject.getPropertyAsString("old_value") + " -> " +
                                soapObject.getPropertyAsString("new_value")
                );
                history.setOldValue(soapObject.getPropertyAsString("old_value"));
                history.setNewValue(soapObject.getPropertyAsString("new_value"));
                history.setField(soapObject.getPropertyAsString("field"));
                history.setUser(soapObject.getPropertyAsString("username"));
                history.setTime(Long.parseLong(soapObject.getPropertyAsString("date")));
                histories.add(history);
            }
        }

        return histories;
    }

    @Override
    public List<Profile<Long>> getProfiles() throws Exception {
        List<Profile<Long>> profiles = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, "mc_user_profiles_get_all");
        request.addProperty("page_number", 1);
        request.addProperty("per_page", 100);
        Object object = this.executeAction(request, "mc_user_profiles_get_all", true);
        object = this.getResult(object);

        if (object instanceof SoapObject) {
            SoapObject resultObject = (SoapObject) object;
            if (resultObject.getProperty("results") instanceof Vector) {
                Vector<?> vector = (Vector<?>) resultObject.getProperty("results");
                for (int i = 0; i <= vector.size() - 1; i++) {
                    SoapObject soapObject = (SoapObject) vector.get(i);
                    Profile<Long> profile = new Profile<>();
                    profile.setPlatform(soapObject.getPropertyAsString("platform"));
                    profile.setOs(soapObject.getPropertyAsString("os"));
                    profile.setOs_build(soapObject.getPropertyAsString("os_build"));
                    profiles.add(profile);
                }
            }
        }

        return profiles;
    }

    @Override
    public List<String> getCategories(Long project_id) throws Exception {
        List<String> categories = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, "mc_project_get_categories");
        request.addProperty("project_id", project_id == null ? 0 : project_id);
        Object object = this.executeAction(request, "mc_project_get_categories", true);
        object = this.getResult(object);

        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) object;
            for (int i = 0; i <= vector.size() - 1; i++) {
                Object obj = vector.get(i);
                if (obj instanceof String) {
                    categories.add((String) obj);
                }
            }
        }

        return categories;
    }

    @Override
    public int getCurrentState() {
        return this.state;
    }

    @Override
    public String getCurrentMessage() {
        return this.currentMessage;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new MantisBTPermissions(this.authentication);
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public Map<String, String> getEnums(Type type, Context context) {
        Map<String, String> lsEnum = new LinkedHashMap<>();
        try {
            String action;
            if(type.name().endsWith("y")) {
                action = "mc_enum_" + type.name().replace("y", "ies");
            } else if(!type.name().endsWith("s")) {
                action = "mc_enum_" + type.name() + "s";
            } else {
                action = "mc_enum_" + type.name();
            }
            SoapObject request = new SoapObject(super.soapPath, action);
            Object object = this.executeAction(request, action, true);
            object = this.getResult(object);

            if (object instanceof Vector) {
                Vector<?> vector = (Vector<?>) object;
                for (int i = 0; i <= vector.size() - 1; i++) {
                    SoapObject soapObject = (SoapObject) vector.get(i);
                    lsEnum.put(soapObject.getPropertyAsString("id"), soapObject.getPropertyAsString("name"));
                }
            }
        } catch (Exception ex) {
            if(context != null) {
                return ArrayHelper.getEnums(context, type, this);
            }
        }

        return lsEnum;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getAuthentication().getTitle();
    }

    private Object getResult(Object object) {
        if (object instanceof SoapFault) {
            SoapFault soapFault = (SoapFault) object;
            this.currentMessage = soapFault.faultstring;
            this.currentMessage = soapFault.faultcode;
            return null;
        }
        return object;
    }

    private Project<Long> soapToProject(SoapObject soapObject, Project<Long> project) {
        if (project == null) {
            project = new Project<>();
        }

        project.setId(Long.parseLong(soapObject.getPropertyAsString("id")));
        project.setTitle(soapObject.getPropertyAsString("name"));
        project.setDescription(soapObject.getPropertyAsString("description"));
        project.setEnabled(soapObject.getPropertyAsString("enabled").equals("true"));

        SoapObject view = (SoapObject) soapObject.getProperty("view_state");
        String name = view.getPropertyAsString("name").trim().toLowerCase();
        project.setPrivateProject(!name.equals("public"));

        SoapObject status = (SoapObject) soapObject.getProperty("status");
        name = status.getPropertyAsString("name").trim().toLowerCase();
        int id = Integer.parseInt(status.getPropertyAsString("id").trim().toLowerCase());
        project.setStatus(name, id);

        if (soapObject.hasProperty("subprojects")) {
            Vector<?> vector = (Vector<?>) soapObject.getProperty("subprojects");

            for (int i = 0; i <= vector.size() - 1; i++) {
                project.getSubProjects().add(new Project<>());
                project.getSubProjects().set(i, this.soapToProject((SoapObject) vector.get(i), project.getSubProjects().get(i)));
            }
        }

        return project;
    }

    private List<Project<Long>> getProject(Project<Long> project, String path, List<Project<Long>> ls) {
        project.setTitle(path + project.getTitle());
        ls.add(project);
        path = "-" + path;

        for (Project<Long> subProject : project.getSubProjects()) {
            ls = this.getProject(subProject, path, ls);
        }
        return ls;
    }

    private SoapObject projectToSoap(Project<Long> project) {
        SoapObject projectData = new SoapObject(NAMESPACE, "ProjectData");
        projectData.addProperty("id", project.getId());
        projectData.addProperty("name", project.getTitle());
        projectData.addProperty("enabled", project.isEnabled());
        projectData.addProperty("description", project.getDescription());

        SoapObject projectStatus = new SoapObject(NAMESPACE, "ObjectRef");
        projectStatus.addProperty("id", project.getStatusID());
        projectStatus.addProperty("name", project.getStatus());
        projectData.addProperty("status", projectStatus);

        SoapObject projectView = new SoapObject(NAMESPACE, "ObjectRef");
        projectView.addProperty("id", project.isPrivateProject() ? 50 : 10);
        projectView.addProperty("name", project.isPrivateProject());
        projectData.addProperty("view_state", projectView);

        return projectData;
    }

    private List<Version<Long>> getVersionsByFilter(String action, Long project_id) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, action);
        request.addProperty("project_id", project_id);
        Object object = this.executeAction(request, action, true);
        Object result = this.getResult(object);
        if (object instanceof Vector) {
            Vector<?> vector = (Vector<?>) result;
            if(vector != null) {
                for (int i = 0; i <= vector.size() - 1; i++) {
                    SoapObject soapObject = (SoapObject) vector.get(i);
                    Version<Long> version = new Version<>();
                    version.setId(Long.parseLong(soapObject.getPropertyAsString("id")));
                    if (soapObject.hasProperty("name")) {
                        version.setTitle(soapObject.getPropertyAsString("name"));
                    }
                    if (soapObject.hasProperty("description")) {
                        version.setDescription(soapObject.getPropertyAsString("description"));
                    }
                    version.setDeprecatedVersion(Boolean.parseBoolean(soapObject.getPropertyAsString("obsolete")));
                    version.setReleasedVersion(Boolean.parseBoolean(soapObject.getPropertyAsString("released")));
                    version.setReleasedVersionAt(ConvertHelper.convertStringToDate(soapObject.getPropertyAsString("date_order"), MantisBT.DATE_TIME_FORMAT).getTime());
                    versions.add(version);
                }
            }
        }
        return versions;
    }

    @Override
    public List<History<Long>> getNews() {
        return new LinkedList<>();
    }
}
