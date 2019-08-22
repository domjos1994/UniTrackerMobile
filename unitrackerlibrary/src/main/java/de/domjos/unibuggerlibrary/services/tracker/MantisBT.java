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

import androidx.annotation.NonNull;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

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
import de.domjos.unibuggerlibrary.permissions.MantisBTPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.SoapEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public final class MantisBT extends SoapEngine implements IBugService<Long> {
    private String currentMessage;
    private Authentication authentication;
    private int state;

    public MantisBT(Authentication authentication) {
        super(authentication, "/api/soap/mantisconnect.php");
        this.authentication = authentication;
        this.currentMessage = "";
        this.state = 0;
    }

    @Override
    public boolean testConnection() throws Exception {
        if (super.isWSDLAvailable()) {
            SoapObject request = new SoapObject(super.soapPath, "mc_login");
            Object object = this.executeAction(request, "mc_login", true);
            return object instanceof SoapObject;
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
            Vector vector = (Vector) result;
            for (int i = 0; i <= vector.size() - 1; i++) {
                SoapObject soapObject = (SoapObject) vector.get(i);
                projects.add(this.soapToProject(soapObject, null));
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
                return (Long) object;
            } else {
                return project.getId();
            }
        }
        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {
        SoapObject request = new SoapObject();
        request.addProperty("project_id", id);
        this.executeAction(request, "mc_project_delete", true);
    }

    @Override
    public List<Version<Long>> getVersions(String filter, Long project_id) throws Exception {
        return this.getVersionsByFilter("mc_project_get_" + filter, project_id);
    }

    @Override
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) throws Exception {
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN);
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

    @Override
    public void deleteVersion(Long id, Long project_id) throws Exception {
        SoapObject request = new SoapObject(super.soapPath, "mc_project_version_delete");
        request.addProperty("version_id", String.valueOf(id));
        this.executeAction(request, "mc_project_version_delete", true);
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

        SoapObject request = new SoapObject(super.soapPath, "mc_project_get_issue_headers");
        request.addProperty("project_id", Integer.parseInt(String.valueOf(pid)));
        request.addProperty("page_number", page);
        request.addProperty("per_page", numberOfItems);

        List<String> enumView = this.getEnums("view_states");
        List<String> enumStatus = this.getEnums("status");
        Object object = this.executeAction(request, "mc_project_get_issue_headers", true);
        object = this.getResult(object);
        if (object instanceof Vector) {
            Vector vector = (Vector) object;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN);
            for (int i = 0; i <= vector.size() - 1; i++) {
                if (vector.get(i) instanceof SoapObject) {
                    SoapObject soapObject = (SoapObject) vector.get(i);
                    String id = soapObject.getPropertyAsString("id");
                    String category = soapObject.getPropertyAsString("category");
                    String summary = soapObject.getPropertyAsString("summary");
                    String view = soapObject.getPropertyAsString("view_state");
                    Date dt = new Date();
                    if (soapObject.hasProperty("last_updated")) {
                        dt = sdf.parse(soapObject.getPropertyAsString("last_updated"));
                    }

                    for (String enumViewItem : enumView) {
                        String[] spl = enumViewItem.split(":");

                        if (view.equals(spl[0])) {
                            view = spl[1].trim();
                            break;
                        }
                    }

                    boolean fitsInFilter = true;
                    String status = soapObject.getPropertyAsString("status");
                    switch (filter) {
                        case resolved:
                            if (Integer.parseInt(status) < 80) {
                                fitsInFilter = false;
                            }
                            break;
                        case unresolved:
                            if (Integer.parseInt(status) >= 80) {
                                fitsInFilter = false;
                            }
                            break;
                    }
                    if (!fitsInFilter) {
                        continue;
                    }

                    for (String enumStatusItem : enumStatus) {
                        String[] spl = enumStatusItem.split(":");

                        if (status.equals(spl[0])) {
                            status = spl[1].trim();
                            break;
                        }
                    }

                    Issue<Long> issue = new Issue<>();
                    issue.setId(Long.parseLong(id));
                    issue.setTitle(summary);
                    issue.setDescription(String.format("%s - %s - %s", category, view, status));
                    issue.setLastUpdated(dt);
                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) throws Exception {
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
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN);
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
                        issue.setReproducibility(Integer.parseInt(resolutionObject.getPropertyAsString("id")), resolutionObject.getPropertyAsString("name"));
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
                            Vector vector = (Vector) soapObject.getProperty("notes");
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
                                        SoapObject viewObject = (SoapObject) soapObject.getProperty("view_state");
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
                            Vector vector = (Vector) soapObject.getProperty("attachments");
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
                        Vector vector = (Vector) soapObject.getProperty("custom_fields");
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
                        Vector vector = (Vector) soapObject.getProperty("tags");
                        for (int i = 0; i <= vector.size() - 1; i++) {
                            issue.setTags(issue.getTags() + ((SoapObject) vector.get(i)).getPropertyAsString("name") + ", ");
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
            request.addProperty("id", 0);
        }
        SoapObject issueObject = new SoapObject(NAMESPACE, "IssueData");
        if (action.equals("mc_issue_add")) {
            issueObject.addProperty("id", 0);
        }
        issueObject.addProperty("category", issue.getCategory());
        issueObject.addProperty("summary", issue.getTitle());
        issueObject.addProperty("description", issue.getDescription());
        issueObject.addProperty("steps_to_reproduce", issue.getStepsToReproduce());
        issueObject.addProperty("additional_information", issue.getAdditionalInformation());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN);
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
        } else {
            issueObject.addProperty("target_version", "null");
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
                for (Tag tag : tags) {
                    if (strTag.trim().equals(tag.getTitle())) {
                        SoapObject tagObject = new SoapObject(NAMESPACE, "ObjectRef");
                        tagObject.addProperty("id", tag.getId());
                        tagObject.addProperty("name", tag.getTitle());
                        tagVector.add(tagObject);
                    }
                }
            }
            issueObject.addProperty("tags", tagVector);
        }

        request.addProperty("issue", issueObject);
        Object object = this.executeAction(request, action, true);
        object = this.getResult(object);

        long id;
        if (issue.getId() != null) {
            id = Long.parseLong(String.valueOf(issue.getId()));
        } else {
            id = Long.parseLong(String.valueOf(object));
        }

        List<Note<Long>> oldNotes = new LinkedList<>();
        List<Attachment<Long>> oldAttachments = new LinkedList<>();
        Issue<Long> oldIssue = this.getIssue(id, project_id);
        if (oldIssue != null) {
            oldNotes = oldIssue.getNotes();
            oldAttachments = oldIssue.getAttachments();
        }

        for (Note<Long> oldNote : oldNotes) {
            boolean available = false;
            for (Note note : issue.getNotes()) {
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
            for (Note<Long> note : issue.getNotes()) {
                this.insertOrUpdateNote(note, id, project_id);
            }
        }

        for (Attachment<Long> oldAttachment : oldAttachments) {
            this.deleteAttachment(oldAttachment.getId(), id, project_id);
        }

        if (!issue.getAttachments().isEmpty()) {
            for (Attachment<Long> attachment : issue.getAttachments()) {
                this.insertOrUpdateAttachment(attachment, id, project_id);
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
        SoapObject deleteRequest = new SoapObject(super.soapPath, "mc_issue_attachment_delete");
        deleteRequest.addProperty("issue_attachment_id", id);
        Object deleteObject = this.executeAction(deleteRequest, "mc_issue_attachment_delete", true);
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
            Vector vector = (Vector) object;
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
            Vector vector = (Vector) object;
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
        Vector vector = (Vector) soapObject.getProperty("results");
        for (int i = 0; i <= vector.size() - 1; i++) {
            SoapObject tagObject = (SoapObject) vector.get(i);
            Tag<Long> tag = new Tag<>();
            tag.setId(Long.parseLong(tagObject.getPropertyAsString("id")));
            tag.setTitle(tagObject.getPropertyAsString("name"));
            tag.setDescription(tagObject.getPropertyAsString("description"));
            tags.add(tag);
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
            Vector vector = (Vector) object;
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
                Vector vector = (Vector) resultObject.getProperty("results");
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
        request.addProperty("project_id", project_id);
        Object object = this.executeAction(request, "mc_project_get_categories", true);
        object = this.getResult(object);

        if (object instanceof Vector) {
            Vector vector = (Vector) object;
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
    public List<String> getEnums(String title) throws Exception {
        List<String> lsEnum = new LinkedList<>();
        String action = "mc_enum_" + title;
        SoapObject request = new SoapObject(super.soapPath, action);
        Object object = this.executeAction(request, action, true);
        object = this.getResult(object);

        if (object instanceof Vector) {
            Vector vector = (Vector) object;
            for (int i = 0; i <= vector.size() - 1; i++) {
                SoapObject soapObject = (SoapObject) vector.get(i);
                lsEnum.add(String.format("%s:%s", soapObject.getPropertyAsString("id"), soapObject.getPropertyAsString("name")));
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
            Vector vector = (Vector) soapObject.getProperty("subprojects");

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
            Vector vector = (Vector) result;
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
                version.setReleasedVersionAt(Converter.convertStringToDate(soapObject.getPropertyAsString("date_order"), "yyyy-MM-dd'T'HH:mm:ss").getTime());
                versions.add(version);
            }
        }
        return versions;
    }
}
