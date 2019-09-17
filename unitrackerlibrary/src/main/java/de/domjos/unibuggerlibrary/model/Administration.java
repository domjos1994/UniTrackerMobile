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

package de.domjos.unibuggerlibrary.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Project;

public class Administration {
    private AdminType adminType;
    private DataType dataType;
    private boolean withBugs;
    private boolean addToExistingProject;
    private IBugService fromBugService;
    private IBugService toBugService;
    private Project fromProject;
    private Project toProject;
    private DescriptionObject dataItem;

    private Map<String, Map<String, Long>> toArrays;

    public Administration() {
        this.adminType = AdminType.copy;
        this.dataType = DataType.Project;
        this.withBugs = false;
        this.addToExistingProject = false;
        this.fromBugService = null;
        this.toBugService = null;
        this.fromProject = null;
        this.toProject = null;
        this.dataItem = null;

        this.toArrays = new LinkedHashMap<>();
    }

    public void setArray(Map<String, Map<String, Long>> array) {
        this.toArrays = array;
    }

    public AdminType getAdminType() {
        return this.adminType;
    }

    public void setAdminType(AdminType adminType) {
        this.adminType = adminType;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isWithBugs() {
        return this.withBugs;
    }

    public void setWithBugs(boolean withBugs) {
        this.withBugs = withBugs;
    }

    public boolean isAddToExistingProject() {
        return this.addToExistingProject;
    }

    public void setAddToExistingProject(boolean addToExistingProject) {
        this.addToExistingProject = addToExistingProject;
    }

    public IBugService getFromBugService() {
        return this.fromBugService;
    }

    public void setFromBugService(IBugService fromBugService) {
        this.fromBugService = fromBugService;
    }

    public IBugService getToBugService() {
        return this.toBugService;
    }

    public void setToBugService(IBugService toBugService) {
        this.toBugService = toBugService;
    }

    public Project getFromProject() {
        return this.fromProject;
    }

    public void setFromProject(Project fromProject) {
        this.fromProject = fromProject;
    }


    public Project getToProject() {
        return this.toProject;
    }
    public void setToProject(Project toProject) {
        this.toProject = toProject;
    }

    public DescriptionObject getDataItem() {
        return this.dataItem;
    }

    public void setDataItem(DescriptionObject dataItem) {
        this.dataItem = dataItem;
    }

    public Project convertProjectToValidNewProject(Project project) {
        switch (this.toBugService.getAuthentication().getTracker()) {
            case Jira:
                // key in upper case and maximum length of 10
                String title = project.getTitle().replace(" ", "_").replace("-", "");
                project.setAlias(title.toUpperCase().substring(0, 9));
                break;
            case YouTrack:
                project.setAlias(project.getTitle());
                break;
        }
        return project;
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public Issue convertIssueToValidNewIssue(Issue issue) {
        switch (this.fromBugService.getAuthentication().getTracker()) {
            case MantisBT:
            case Local:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case MantisBT:
                        return issue;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:
                        this.connect(issue, "status", Arrays.asList(10L, 20L, 30L), 3);
                        this.connect(issue, "status", Arrays.asList(40L, 50L), 10000);
                        this.connect(issue, "status", Arrays.asList(80L, 90L), 10002);

                        this.connect(issue, "severity", 10, 10005);
                        this.connect(issue, "severity", Arrays.asList(20L, 30L, 40L), 10002);
                        this.connect(issue, "severity", Arrays.asList(50L, 60L), 10006);
                        this.connect(issue, "severity", Arrays.asList(70L, 80L), 10000);

                        this.connect(issue, "priority", Arrays.asList(10L, 20L), 5);
                        this.connect(issue, "priority", 30, 3);
                        this.connect(issue, "priority", 40, 2);
                        this.connect(issue, "priority", Arrays.asList(50L, 60L), 1);


                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case YouTrack:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case YouTrack:
                        return issue;
                    case MantisBT:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:

                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case RedMine:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case RedMine:
                        return issue;
                    case MantisBT:

                        break;
                    case YouTrack:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:

                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case Bugzilla:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Bugzilla:
                        return issue;
                    case MantisBT:

                        break;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Jira:

                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case Jira:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Jira:
                        return issue;
                    case MantisBT:
                        this.connect(issue, "status", 3, 30);
                        this.connect(issue, "status", 10000, 50);
                        this.connect(issue, "status", 10002, 80);

                        this.connect(issue, "severity", 10006, 50);
                        this.connect(issue, "severity", 10002, 40);
                        this.connect(issue, "severity", Arrays.asList(10005L, 10003L), 10);
                        this.connect(issue, "severity", 10000, 70);

                        this.connect(issue, "priority", Arrays.asList(4L, 5L), 20);
                        this.connect(issue, "priority", 3, 30);
                        this.connect(issue, "priority", 2, 40);
                        this.connect(issue, "priority", 1, 60);
                        break;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case OpenProject:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case OpenProject:
                        return issue;
                    case MantisBT:

                        break;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case PivotalTracker:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case OpenProject:
                        return issue;
                    case MantisBT:

                        break;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case Github:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Github:
                        return issue;
                    case MantisBT:

                        break;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:

                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Backlog:

                        break;
                }
                break;
            case Backlog:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Backlog:
                        return issue;
                    case MantisBT:

                        break;
                    case YouTrack:

                        break;
                    case RedMine:

                        break;
                    case Bugzilla:

                        break;
                    case Jira:

                        break;
                    case OpenProject:

                        break;
                    case PivotalTracker:

                        break;
                    case Github:

                        break;
                }
                break;
        }

        return issue;
    }

    private void connect(Issue issue, String key, List<Long> oldIds, long newId) {
        for(Long oldId : oldIds) {
            Map<String, Long> newEntries = this.toArrays.get(key);
            if(newEntries!=null) {
                switch (key) {
                    case "view":
                        if(issue.getState().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setState((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case "reproducibility":
                        if(issue.getReproducibility().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setReproducibility((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case "severity":
                        if(issue.getSeverity().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setSeverity((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case "priority":
                        if(issue.getPriority().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setPriority((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case "status":
                        if(issue.getStatus().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setStatus((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case "resolution":
                        if(issue.getResolution().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setResolution((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                }
            }
        }
    }

    private void connect(Issue issue, String key, long oldId, long newId) {
        this.connect(issue, key, Collections.singletonList(oldId), newId);
    }

    private String getString(Map<String, Long> entries, long id) {
        for(Map.Entry<String, Long> entry : entries.entrySet()) {
            if(entry.getValue()!=null) {
                if(entry.getValue().equals(id)) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

    public enum AdminType {
        copy,
        move
    }

    public enum DataType {
        Project,
        Bug,
        CustomField
    }
}
