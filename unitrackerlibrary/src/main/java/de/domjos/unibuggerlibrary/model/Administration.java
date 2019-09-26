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
    private List<String> categories;

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

    @SuppressWarnings("unchecked")
    public void loadCategories(Object projectId) throws Exception {
        this.categories = this.toBugService.getCategories(projectId);
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
            case MantisBT:
                project.setEnabled(true);
                break;
        }
        return project;
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public Issue convertIssueToValidNewIssue(Issue issue) {
        issue.setTags(issue.getTags().replace(" ", ""));

        switch (this.fromBugService.getAuthentication().getTracker()) {
            case MantisBT:
            case Local:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case MantisBT:
                        boolean hasCategory = false;
                        for (String category : this.categories) {
                            if (issue.getCategory().toLowerCase().equals(category.toLowerCase())) {
                                hasCategory = true;
                                break;
                            }
                        }
                        if (!hasCategory) {
                            issue.setCategory(categories.isEmpty() ? "" : categories.get(0));
                        }
                        return issue;
                    case YouTrack:
                        this.connect(issue, "status", 10, 0);
                        this.connect(issue, "status", 20, 1);
                        this.connect(issue, "status", Arrays.asList(30L, 40L, 50L), 2);
                        this.connect(issue, "status", Arrays.asList(80L, 90L), 7);

                        this.connect(issue, "severity", 10, 8);
                        this.connect(issue, "severity", Arrays.asList(20L, 30L, 40L), 6);
                        this.connect(issue, "severity", Arrays.asList(50L, 60L), 7);
                        this.connect(issue, "severity", Arrays.asList(70L, 80L), 12);

                        this.connect(issue, "priority", Arrays.asList(10L, 20L), 4);
                        this.connect(issue, "priority", 30, 3);
                        this.connect(issue, "priority", 40, 2);
                        this.connect(issue, "priority", 50, 1);
                        this.connect(issue, "priority", 60, 0);
                        break;
                    case RedMine:
                        this.connect(issue, "status", 10, 1);
                        this.connect(issue, "status", Arrays.asList(20L, 30L, 40L, 50L), 2);
                        this.connect(issue, "status", 80, 3);
                        this.connect(issue, "status", 90, 5);

                        this.connect(issue, "severity", 10, 2);
                        this.connect(issue, "severity", Arrays.asList(20L, 30L, 40L), 3);
                        this.connect(issue, "severity", Arrays.asList(50L, 60L, 70L, 80L), 1);

                        this.connect(issue, "priority", Arrays.asList(10L, 20L), 1);
                        this.connect(issue, "priority", 30, 2);
                        this.connect(issue, "priority", 40, 3);
                        this.connect(issue, "priority", 50, 4);
                        this.connect(issue, "priority", 60, 5);
                        break;
                    case Bugzilla:
                        this.connect(issue, "status", Arrays.asList(10L, 20L, 30L, 40L), 3);
                        this.connect(issue, "status", 50, 4);
                        this.connect(issue, "status", Arrays.asList(80L, 90L), 5);

                        this.connect(issue, "severity", 10, 7);
                        this.connect(issue, "severity", Arrays.asList(40L, 30L, 20L), 6);
                        this.connect(issue, "severity", 50, 5);
                        this.connect(issue, "severity", 60, 3);
                        this.connect(issue, "severity", 70, 2);
                        this.connect(issue, "severity", 80, 1);

                        this.connect(issue, "priority", 10, 6);
                        this.connect(issue, "priority", 20, 5);
                        this.connect(issue, "priority", 30, 3);
                        this.connect(issue, "priority", 40, 2);
                        this.connect(issue, "priority", 60, 1);

                        this.connect(issue, "resolution", Arrays.asList(10L, 20L, 30L), 1);
                        this.connect(issue, "resolution", Arrays.asList(40L, 50L), 6);
                        this.connect(issue, "resolution", 60, 5);
                        this.connect(issue, "resolution", Arrays.asList(70L, 80L, 90L), 4);
                        this.connect(issue, "resolution", 10, 1);
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
                        this.connect(issue, "status", 10, 1);
                        this.connect(issue, "status", 20, 2);
                        this.connect(issue, "status", 30, 3);
                        this.connect(issue, "status", 40, 4);
                        this.connect(issue, "status", 50, 7);
                        this.connect(issue, "status", 80, 11);
                        this.connect(issue, "status", 90, 13);

                        this.connect(issue, "severity", 10L, 4);
                        this.connect(issue, "severity", Arrays.asList(20L, 30L, 40L, 50L, 60L, 70L, 80L), 7);

                        this.connect(issue, "priority", Arrays.asList(10L, 20L), 7);
                        this.connect(issue, "priority", 30, 8);
                        this.connect(issue, "priority", 40, 9);
                        this.connect(issue, "priority", Arrays.asList(50L, 60L), 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, "status", Arrays.asList(10L, 20L), 0);
                        this.connect(issue, "status", Arrays.asList(30L, 40L, 50L), 1);
                        this.connect(issue, "status", Arrays.asList(80L, 90L), 2);

                        this.connect(issue, "severity", Arrays.asList(20L, 30L, 40L, 50L), 0);
                        this.connect(issue, "severity", 10, 1);
                        this.connect(issue, "severity", Arrays.asList(60L, 70L, 80L), 2);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, "status", Arrays.asList(10L, 20L, 30L, 40L), 1);
                        this.connect(issue, "status", 50, 2);
                        this.connect(issue, "status", 80, 3);
                        this.connect(issue, "status", 90, 4);

                        this.connect(issue, "severity", 10, 90201);
                        this.connect(issue, "severity", Arrays.asList(40L, 50L, 60L), 90200);
                        this.connect(issue, "severity", Arrays.asList(30L, 20L), 90202);
                        this.connect(issue, "severity", Arrays.asList(70L, 80L), 90203);

                        this.connect(issue, "priority", Arrays.asList(40L, 50L, 60L), 2);
                        this.connect(issue, "priority", 30, 3);
                        this.connect(issue, "priority", Arrays.asList(10L, 20L), 4);
                        break;
                }
                break;
            case YouTrack:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case YouTrack:
                        return issue;
                    case MantisBT:
                        this.connect(issue, "status", 0, 10);
                        this.connect(issue, "status", 1, 20);
                        this.connect(issue, "status", Arrays.asList(2L, 3L, 4L, 5L, 6L), 40);
                        this.connect(issue, "status", Arrays.asList(7L, 8L, 9L, 10L, 11L), 80L);

                        this.connect(issue, "severity", Arrays.asList(8L, 9L), 10);
                        this.connect(issue, "severity", Arrays.asList(5L, 10L, 11L), 50);
                        this.connect(issue, "severity", 6, 40);
                        this.connect(issue, "severity", 7, 60);
                        this.connect(issue, "severity", 12, 70);

                        this.connect(issue, "priority", 4, 10);
                        this.connect(issue, "priority", 3, 30);
                        this.connect(issue, "priority", 2, 40);
                        this.connect(issue, "priority", 1, 50);
                        this.connect(issue, "priority", 0, 60);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
                        break;
                    case RedMine:
                        this.connect(issue, "status", Arrays.asList(0L, 1L), 1);
                        this.connect(issue, "status", Arrays.asList(2L, 3L, 4L), 2);
                        this.connect(issue, "status", Arrays.asList(5L, 6L), 3);
                        this.connect(issue, "status", Arrays.asList(8L, 9L), 4);
                        this.connect(issue, "status", 7, 5);
                        this.connect(issue, "status", Arrays.asList(10L, 11L), 6);

                        this.connect(issue, "severity", Arrays.asList(5L, 6L, 7L, 12L), 1);
                        this.connect(issue, "severity", 8, 2);
                        this.connect(issue, "severity", Arrays.asList(9L, 10L, 11L), 3);

                        this.connect(issue, "priority", 4, 1);
                        this.connect(issue, "priority", 3, 2);
                        this.connect(issue, "priority", 2, 3);
                        this.connect(issue, "priority", 1, 4);
                        this.connect(issue, "priority", 0, 5);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        break;
                    case Bugzilla:
                        this.connect(issue, "status", Arrays.asList(0L, 1L), 3);
                        this.connect(issue, "status", 2, 4);
                        this.connect(issue, "status", Arrays.asList(3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L), 5);


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
                        this.connect(issue, "status", 1, 10);
                        this.connect(issue, "status", Arrays.asList(2L, 4L), 50);
                        this.connect(issue, "status", 3, 80);
                        this.connect(issue, "status", Arrays.asList(5L, 6L), 90);

                        this.connect(issue, "severity", 1, 50);
                        this.connect(issue, "severity", 2, 10);
                        this.connect(issue, "severity", 3, 40);

                        this.connect(issue, "priority", 1, 20);
                        this.connect(issue, "priority", 2, 30);
                        this.connect(issue, "priority", 3, 40);
                        this.connect(issue, "priority", 4, 50);
                        this.connect(issue, "priority", 5, 60);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, "status", Arrays.asList(0L, 1L), 1);
                        this.connect(issue, "status", 2, 2);
                        this.connect(issue, "status", Arrays.asList(3L, 5L), 7);
                        this.connect(issue, "status", 4, 3);
                        this.connect(issue, "status", 5, 5);
                        this.connect(issue, "status", 6, 11);

                        this.connect(issue, "severity", 1, 5);
                        this.connect(issue, "severity", 2, 8);
                        this.connect(issue, "severity", 3, 10);

                        this.connect(issue, "priority", 1, 4);
                        this.connect(issue, "priority", 2, 3);
                        this.connect(issue, "priority", 3, 2);
                        this.connect(issue, "priority", 4, 1);
                        this.connect(issue, "priority", 5, 0);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
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
                        this.connect(issue, "status", 3, 30);
                        this.connect(issue, "status", 4, 50);
                        this.connect(issue, "status", 5, 80);

                        this.connect(issue, "severity", 7, 10);
                        this.connect(issue, "severity", 6, 20);
                        this.connect(issue, "severity", 5, 50);
                        this.connect(issue, "severity", 3, 60);
                        this.connect(issue, "severity", 2, 2);
                        this.connect(issue, "severity", 80, 1);

                        this.connect(issue, "priority", 6, 10);
                        this.connect(issue, "priority", 5, 20);
                        this.connect(issue, "priority", 3, 30);
                        this.connect(issue, "priority", 2, 70);
                        this.connect(issue, "priority", 1, 80);

                        this.connect(issue, "resolution", 1, 10);
                        this.connect(issue, "resolution", 2, 20);
                        this.connect(issue, "resolution", 3, 40);
                        this.connect(issue, "resolution", 4, 90);
                        this.connect(issue, "resolution", 5, 60);
                        this.connect(issue, "resolution", 6, 70);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        this.getDefaultCategory(issue);
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

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
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
                        this.connect(issue, "status", 1, 10);
                        this.connect(issue, "status", Arrays.asList(2L, 3L, 4L), 20);
                        this.connect(issue, "status", Arrays.asList(5L, 6L, 7L), 30);
                        this.connect(issue, "status", Arrays.asList(8L, 9L), 40);
                        this.connect(issue, "status", Arrays.asList(10L, 11L), 50);
                        this.connect(issue, "status", 12, 80);
                        this.connect(issue, "status", Arrays.asList(13L, 14L, 15L), 90);

                        this.connect(issue, "severity", 4, 10);
                        this.connect(issue, "severity", 7, 50);

                        this.connect(issue, "priority", 7, 20);
                        this.connect(issue, "priority", 8, 30);
                        this.connect(issue, "priority", 9, 40);
                        this.connect(issue, "priority", 10, 60);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
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
                        this.connect(issue, "status", 0, 10);
                        this.connect(issue, "status", 1, 30);
                        this.connect(issue, "status", Arrays.asList(2L, 3L, 4L), 80);

                        this.connect(issue, "severity", 0, 20);
                        this.connect(issue, "severity", 1, 10);
                        this.connect(issue, "severity", 2, 2);

                        issue.setPriority(10, this.getValue("priority", 10));
                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
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
                        issue.setStatus(10, this.getValue("state", 10));
                        issue.setPriority(10, this.getValue("priority", 10));
                        issue.setSeverity(10, this.getValue("severity", 10));
                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
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
                        this.connect(issue, "status", 1, 20);
                        this.connect(issue, "status", 2, 50);
                        this.connect(issue, "status", 3, 80);
                        this.connect(issue, "status", 4, 90);

                        this.connect(issue, "severity", 90201, 40);
                        this.connect(issue, "severity", 90200, 50);
                        this.connect(issue, "severity", 90202, 20);
                        this.connect(issue, "severity", 90203, 30);

                        this.connect(issue, "priority", 2, 40);
                        this.connect(issue, "priority", 3, 30);
                        this.connect(issue, "priority", 4, 20);

                        issue.setState(10, this.getValue("view", 10));
                        issue.setReproducibility(100, this.getValue("reproducibility", 100));
                        issue.setResolution(10, this.getValue("resolution", 10));
                        this.getDefaultCategory(issue);
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

    private String getValue(String key, long id) {
        Map<String, Long> mp = this.toArrays.get(key);
        if (mp != null) {
            for (Map.Entry<String, Long> entry : mp.entrySet()) {
                if (entry.getValue().equals(id)) {
                    return entry.getKey();
                }
            }
        }
        return "";
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

    private void getDefaultCategory(Issue issue) {
        try {
            List<String> categories = this.categories;
            if (issue.getCategory() != null) {
                if (issue.getCategory().isEmpty()) {
                    if (categories != null) {
                        if (!categories.isEmpty()) {
                            issue.setCategory(categories.get(0));
                        }
                    }
                }
            } else {
                if (categories != null) {
                    if (!categories.isEmpty()) {
                        issue.setCategory(categories.get(0));
                    }
                }
            }
        } catch (Exception ignored) {
        }
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
