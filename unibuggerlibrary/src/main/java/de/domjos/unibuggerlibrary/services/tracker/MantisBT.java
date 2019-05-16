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

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.SoapEngine;
import de.domjos.unibuggerlibrary.utils.Converter;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public final class MantisBT extends SoapEngine implements IBugService<Long> {
    private String currentMessage;
    private int state;

    public MantisBT(Authentication authentication) {
        super(authentication, "/api/soap/mantisconnect.php");
        this.currentMessage = "";
        this.state = 0;
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
                Project<Long> project = this.soapToProject(soapObject);

                Vector subProjects = (Vector) soapObject.getProperty("subprojects");
                for (int j = 0; j <= subProjects.size() - 1; j++) {
                    project.getSubProjects().add(this.soapToProject((SoapObject) subProjects.get(j)));
                }

                projects.add(project);
            }
        }

        List<Project<Long>> projectsAndSubs = new LinkedList<>();
        int counter = 0;
        for (int i = 0; i <= projects.size() - 1; i++) {
            projectsAndSubs.add(counter, projects.get(i));
            counter++;
            for (Project<Long> sub : projects.get(i).getSubProjects()) {
                sub.setTitle("--- " + sub.getTitle());
                projectsAndSubs.add(counter, sub);
                counter++;
            }
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
    public List<Version<Long>> getVersions(Long pid, String filter) throws Exception {
        return this.getVersions("mc_project_get_" + filter, pid);
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
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
        projectData.addProperty("project_id", pid);

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
        object = this.getResult(object);

        if (object != null) {
            if (version.getId() == null) {
                return (Long) object;
            } else {
                return version.getId();
            }
        }
        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {
        SoapObject request = new SoapObject();
        request.addProperty("version_id", String.valueOf(id));
        this.executeAction(request, "mc_project_version_delete", true);
    }

    @Override
    public int getCurrentState() {
        return this.state;
    }

    @Override
    public String getCurrentMessage() {
        return this.currentMessage;
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

    private Project<Long> soapToProject(SoapObject soapObject) {
        Project<Long> project = new Project<>();
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
        return project;
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

    private List<Version<Long>> getVersions(String action, Long pid) throws Exception {
        List<Version<Long>> versions = new LinkedList<>();
        SoapObject request = new SoapObject(super.soapPath, action);
        request.addProperty("project_id", pid);
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
