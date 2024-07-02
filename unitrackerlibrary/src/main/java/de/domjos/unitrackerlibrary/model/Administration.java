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

package de.domjos.unitrackerlibrary.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;

/** @noinspection rawtypes*/
public class Administration {
    private final static String STATUS = "status";
    private final static String SEVERITY = "severity";
    private final static String PRIORITY = "priority";
    private final static String RESOLUTION = "resolution";
    private final static String VIEW = "view";
    private final static String REPRODUCIBILITY = "reproducibility";
    private final static String STATE = "state";
    
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
    private boolean status, severity, priority, resolution;
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
        if(project.getTitle().startsWith("-")) {
            while (project.getTitle().startsWith("-")) {
                project.setTitle(project.getTitle().replaceFirst("-", ""));
            }
        }

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
            case RedMine:
                project.setAlias(project.getTitle().replace(" ", "-").toLowerCase());
                break;
            case Backlog:
                project.setAlias(project.getTitle().replace(" ", "_").toUpperCase());
                break;
        }
        return project;
    }

    public Issue convertIssueToValidNewIssue(Issue issue) {
        this.status = false;
        this.resolution = false;
        this.severity = false;
        this.priority = false;

        issue.setTags(issue.getTags().replace(" ", ""));
        issue.getRelations().clear();

        switch (this.fromBugService.getAuthentication().getTracker()) {
            case MantisBT:
            case Local:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case MantisBT:
                        boolean hasCategory = false;
                        for (String category : this.categories) {
                            if (issue.getCategory().equalsIgnoreCase(category)) {
                                hasCategory = true;
                                break;
                            }
                        }
                        if (!hasCategory) {
                            issue.setCategory(categories.isEmpty() ? "" : categories.get(0));
                        }
                        return issue;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, 10, 0);
                        this.connect(issue, Administration.STATUS, 20, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(30L, 40L, 50L), 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(80L, 90L), 7);

                        this.connect(issue, Administration.SEVERITY, 10, 8);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(20L, 30L, 40L), 6);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(50L, 60L), 7);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(70L, 80L), 12);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(10L, 20L), 4);
                        this.connect(issue, Administration.PRIORITY, 30, 3);
                        this.connect(issue, Administration.PRIORITY, 40, 2);
                        this.connect(issue, Administration.PRIORITY, 50, 1);
                        this.connect(issue, Administration.PRIORITY, 60, 0);
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, 10, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(20L, 30L, 40L, 50L), 2);
                        this.connect(issue, Administration.STATUS, 80, 3);
                        this.connect(issue, Administration.STATUS, 90, 5);

                        this.connect(issue, Administration.SEVERITY, 10, 2);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(20L, 30L, 40L), 3);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(50L, 60L, 70L, 80L), 1);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(10L, 20L), 1);
                        this.connect(issue, Administration.PRIORITY, 30, 2);
                        this.connect(issue, Administration.PRIORITY, 40, 3);
                        this.connect(issue, Administration.PRIORITY, 50, 4);
                        this.connect(issue, Administration.PRIORITY, 60, 5);
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, Arrays.asList(10L, 20L, 30L, 40L), 3);
                        this.connect(issue, Administration.STATUS, 50, 4);
                        this.connect(issue, Administration.STATUS, Arrays.asList(80L, 90L), 5);

                        this.connect(issue, Administration.SEVERITY, 10, 7);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(40L, 30L, 20L), 6);
                        this.connect(issue, Administration.SEVERITY, 50, 5);
                        this.connect(issue, Administration.SEVERITY, 60, 3);
                        this.connect(issue, Administration.SEVERITY, 70, 2);
                        this.connect(issue, Administration.SEVERITY, 80, 1);

                        this.connect(issue, Administration.PRIORITY, 10, 6);
                        this.connect(issue, Administration.PRIORITY, 20, 5);
                        this.connect(issue, Administration.PRIORITY, 30, 3);
                        this.connect(issue, Administration.PRIORITY, 40, 2);
                        this.connect(issue, Administration.PRIORITY, 60, 1);

                        this.connect(issue, Administration.RESOLUTION, Arrays.asList(10L, 20L, 30L), 1);
                        this.connect(issue, Administration.RESOLUTION, Arrays.asList(40L, 50L), 6);
                        this.connect(issue, Administration.RESOLUTION, 60, 5);
                        this.connect(issue, Administration.RESOLUTION, Arrays.asList(70L, 80L, 90L), 4);
                        this.connect(issue, Administration.RESOLUTION, 10, 1);
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, Arrays.asList(10L, 20L, 30L), 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(40L, 50L), 10000);
                        this.connect(issue, Administration.STATUS, Arrays.asList(80L, 90L), 10002);

                        this.connect(issue, Administration.SEVERITY, 10, 10005);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(20L, 30L, 40L), 10002);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(50L, 60L), 10006);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(70L, 80L), 10000);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(10L, 20L), 5);
                        this.connect(issue, Administration.PRIORITY, 30, 3);
                        this.connect(issue, Administration.PRIORITY, 40, 2);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(50L, 60L), 1);
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, 10, 1);
                        this.connect(issue, Administration.STATUS, 20, 2);
                        this.connect(issue, Administration.STATUS, 30, 3);
                        this.connect(issue, Administration.STATUS, 40, 4);
                        this.connect(issue, Administration.STATUS, 50, 7);
                        this.connect(issue, Administration.STATUS, 80, 11);
                        this.connect(issue, Administration.STATUS, 90, 13);

                        this.connect(issue, Administration.SEVERITY, 10L, 4);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(20L, 30L, 40L, 50L, 60L, 70L, 80L), 7);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(10L, 20L), 7);
                        this.connect(issue, Administration.PRIORITY, 30, 8);
                        this.connect(issue, Administration.PRIORITY, 40, 9);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(50L, 60L), 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, Arrays.asList(10L, 20L), 0);
                        this.connect(issue, Administration.STATUS, Arrays.asList(30L, 40L, 50L), 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(80L, 90L), 2);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(20L, 30L, 40L, 50L), 0);
                        this.connect(issue, Administration.SEVERITY, 10, 1);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(60L, 70L, 80L), 2);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, Arrays.asList(10L, 20L, 30L, 40L), 1);
                        this.connect(issue, Administration.STATUS, 50, 2);
                        this.connect(issue, Administration.STATUS, 80, 3);
                        this.connect(issue, Administration.STATUS, 90, 4);

                        this.connect(issue, Administration.SEVERITY, 10, 90201);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(40L, 50L, 60L), 90200);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(30L, 20L), 90202);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(70L, 80L), 90203);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(40L, 50L, 60L), 2);
                        this.connect(issue, Administration.PRIORITY, 30, 3);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(10L, 20L), 4);
                        break;
                }
                break;
            case YouTrack:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case YouTrack:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 0, 10);
                        this.connect(issue, Administration.STATUS, 1, 20);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L, 4L, 5L, 6L), 40);
                        this.connect(issue, Administration.STATUS, Arrays.asList(7L, 8L, 9L, 10L, 11L), 80L);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(8L, 9L), 10);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(5L, 10L, 11L), 50);
                        this.connect(issue, Administration.SEVERITY, 6, 40);
                        this.connect(issue, Administration.SEVERITY, 7, 60);
                        this.connect(issue, Administration.SEVERITY, 12, 70);

                        this.connect(issue, Administration.PRIORITY, 4, 10);
                        this.connect(issue, Administration.PRIORITY, 3, 30);
                        this.connect(issue, Administration.PRIORITY, 2, 40);
                        this.connect(issue, Administration.PRIORITY, 1, 50);
                        this.connect(issue, Administration.PRIORITY, 0, 60);

                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L), 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L, 4L), 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L), 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(8L, 9L), 4);
                        this.connect(issue, Administration.STATUS, 7, 5);
                        this.connect(issue, Administration.STATUS, Arrays.asList(10L, 11L), 6);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(5L, 6L, 7L, 12L), 1);
                        this.connect(issue, Administration.SEVERITY, 8, 2);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(9L, 10L, 11L), 3);

                        this.connect(issue, Administration.PRIORITY, 4, 1);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, 1, 4);
                        this.connect(issue, Administration.PRIORITY, 0, 5);
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L), 3);
                        this.connect(issue, Administration.STATUS, 2, 4);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L), 5);

                        this.connect(issue, Administration.SEVERITY, 12, 1);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(11L, 10L), 5);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(9L, 8L, 7L, 6L, 5L), 6);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(0L, 1L), 1);
                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 4, 4);
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L, 2L, 3L, 9L), 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(4L, 5L, 8L), 10000);
                        this.connect(issue, Administration.STATUS, Arrays.asList(6L, 7L, 9L, 10L, 11L), 10002);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(5L, 7L), 10006);
                        this.connect(issue, Administration.SEVERITY, 6, 10002);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(9L, 10L, 11L), 10003);
                        this.connect(issue, Administration.SEVERITY, 8, 10005);
                        this.connect(issue, Administration.SEVERITY, 12, 10000);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(0L, 1L), 10);
                        this.connect(issue, Administration.PRIORITY, 2, 9);
                        this.connect(issue, Administration.PRIORITY, 3, 8);
                        this.connect(issue, Administration.PRIORITY, 4, 7);
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, 0, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(1L, 4L), 6);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 9L), 7);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L, 7L, 8L, 10L, 11L), 13);
                        this.connect(issue, Administration.STATUS, 3, 5);

                        this.connect(issue, Administration.SEVERITY, 8, 4);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(5L, 6L, 7L, 9L, 10L, 11L, 12L), 7);

                        this.connect(issue, Administration.PRIORITY, 4, 7);
                        this.connect(issue, Administration.PRIORITY, 3, 8);
                        this.connect(issue, Administration.PRIORITY, 2, 9);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(1L, 0L), 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L, 4L), 0);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L), 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L, 7L, 8L), 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(9L, 10L, 11L), 4);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(5L, 6L, 7L, 9L, 10L, 11L), 0L);
                        this.connect(issue, Administration.SEVERITY, 8L, 1L);
                        this.connect(issue, Administration.SEVERITY, 12L, 2L);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L, 4L), 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L), 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L, 8L, 9L, 10L, 11L), 3);
                        this.connect(issue, Administration.STATUS, 7, 4);

                        this.connect(issue, Administration.SEVERITY, 9, 90201);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(5L, 6L, 7L, 10L, 11L, 12L), 90200);
                        this.connect(issue, Administration.SEVERITY, 8, 90202);
                        this.connect(issue, Administration.SEVERITY, 9L, 90203);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(0L, 1L, 2L), 2);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 4, 4);
                        break;
                }
                break;
            case RedMine:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case RedMine:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 1, 10);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 4L), 50);
                        this.connect(issue, Administration.STATUS, 3, 80);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L), 90);

                        this.connect(issue, Administration.SEVERITY, 1, 50);
                        this.connect(issue, Administration.SEVERITY, 2, 10);
                        this.connect(issue, Administration.SEVERITY, 3, 40);

                        this.connect(issue, Administration.PRIORITY, 1, 20);
                        this.connect(issue, Administration.PRIORITY, 2, 30);
                        this.connect(issue, Administration.PRIORITY, 3, 40);
                        this.connect(issue, Administration.PRIORITY, 4, 50);
                        this.connect(issue, Administration.PRIORITY, 5, 60);

                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L), 1);
                        this.connect(issue, Administration.STATUS, 2, 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 5L), 7);
                        this.connect(issue, Administration.STATUS, 4, 3);
                        this.connect(issue, Administration.STATUS, 5, 5);
                        this.connect(issue, Administration.STATUS, 6, 11);

                        this.connect(issue, Administration.SEVERITY, 1, 5);
                        this.connect(issue, Administration.SEVERITY, 2, 8);
                        this.connect(issue, Administration.SEVERITY, 3, 10);

                        this.connect(issue, Administration.PRIORITY, 1, 4);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, 4, 1);
                        this.connect(issue, Administration.PRIORITY, 5, 0);
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, 1, 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 4L), 4);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 5L, 6L), 5);

                        this.connect(issue, Administration.SEVERITY, 1, 1);
                        this.connect(issue, Administration.SEVERITY, 2, 7);
                        this.connect(issue, Administration.SEVERITY, 3, 4);

                        this.connect(issue, Administration.PRIORITY, 1, 4);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 1);
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, 1, 10000);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 4L), 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 5L, 6L), 10002);

                        this.connect(issue, Administration.SEVERITY, 1, 1);
                        this.connect(issue, Administration.SEVERITY, 2, 10005);
                        this.connect(issue, Administration.SEVERITY, 3, 10002);

                        this.connect(issue, Administration.PRIORITY, 1, 4);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 1);
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, 1, 1);
                        this.connect(issue, Administration.STATUS, 2, 7);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 13);
                        this.connect(issue, Administration.STATUS, 4, 2);
                        this.connect(issue, Administration.STATUS, 6, 15);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(1L, 3L), 7);
                        this.connect(issue, Administration.SEVERITY, 2, 4);

                        this.connect(issue, Administration.PRIORITY, 1, 7);
                        this.connect(issue, Administration.PRIORITY, 2, 8);
                        this.connect(issue, Administration.PRIORITY, 3, 9);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, 1, 0);
                        this.connect(issue, Administration.STATUS, 2, 1);
                        this.connect(issue, Administration.STATUS, 3, 2);
                        this.connect(issue, Administration.STATUS, 5, 4);
                        this.connect(issue, Administration.STATUS, 6, 5);

                        this.connect(issue, Administration.SEVERITY, 1, 0);
                        this.connect(issue, Administration.SEVERITY, 2, 1);
                        this.connect(issue, Administration.SEVERITY, 3, 2);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, 1, 1);
                        this.connect(issue, Administration.STATUS, 2, 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L), 4);

                        this.connect(issue, Administration.SEVERITY, 1, 90201);
                        this.connect(issue, Administration.SEVERITY, 2, 90202);
                        this.connect(issue, Administration.SEVERITY, 3, 90203);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(1L, 2L), 4);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(3L, 4L, 5L), 2);
                        break;
                }
                break;
            case Bugzilla:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Bugzilla:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 3, 30);
                        this.connect(issue, Administration.STATUS, 4, 50);
                        this.connect(issue, Administration.STATUS, 5, 80);

                        this.connect(issue, Administration.SEVERITY, 7, 10);
                        this.connect(issue, Administration.SEVERITY, 6, 20);
                        this.connect(issue, Administration.SEVERITY, 5, 50);
                        this.connect(issue, Administration.SEVERITY, 3, 60);
                        this.connect(issue, Administration.SEVERITY, 2, 2);
                        this.connect(issue, Administration.SEVERITY, 80, 1);

                        this.connect(issue, Administration.PRIORITY, 6, 10);
                        this.connect(issue, Administration.PRIORITY, 5, 20);
                        this.connect(issue, Administration.PRIORITY, 3, 30);
                        this.connect(issue, Administration.PRIORITY, 2, 70);
                        this.connect(issue, Administration.PRIORITY, 1, 80);

                        this.connect(issue, Administration.RESOLUTION, 1, 10);
                        this.connect(issue, Administration.RESOLUTION, 2, 20);
                        this.connect(issue, Administration.RESOLUTION, 3, 40);
                        this.connect(issue, Administration.RESOLUTION, 4, 90);
                        this.connect(issue, Administration.RESOLUTION, 5, 60);
                        this.connect(issue, Administration.RESOLUTION, 6, 70);

                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, 3, 0);
                        this.connect(issue, Administration.STATUS, 4, 2);
                        this.connect(issue, Administration.STATUS, 5, 7);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(1L, 2L, 3L, 4L), 12);
                        this.connect(issue, Administration.SEVERITY, 5, 7);
                        this.connect(issue, Administration.SEVERITY, 6, 6);
                        this.connect(issue, Administration.SEVERITY, 7, 10);

                        this.connect(issue, Administration.PRIORITY, 1, 1);
                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L, 6L), 4);
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, 3, 1);
                        this.connect(issue, Administration.STATUS, 4, 2);
                        this.connect(issue, Administration.STATUS, 5, 3);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(1L, 2L, 3L, 4L, 5L), 1);
                        this.connect(issue, Administration.SEVERITY, 7, 2);
                        this.connect(issue, Administration.SEVERITY, 6, 3);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(5L, 4L), 1);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, 1, 4);
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 3);
                        this.connect(issue, Administration.STATUS, 5, 10002);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(1L, 2L, 3L), 10000);
                        this.connect(issue, Administration.SEVERITY, 7, 10005);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(4L, 5L, 6L), 10002);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(5L, 6L), 5);
                        this.connect(issue, Administration.PRIORITY, 4, 4);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 1, 1);
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, 3, 4);
                        this.connect(issue, Administration.STATUS, 4, 7);
                        this.connect(issue, Administration.STATUS, 5, 13);

                        this.connect(issue, Administration.SEVERITY, 7, 4);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(2L, 3L, 4L, 5L, 6L), 7);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(5L, 6L, 4L), 7);
                        this.connect(issue, Administration.PRIORITY, 3, 8);
                        this.connect(issue, Administration.PRIORITY, 2, 9);
                        this.connect(issue, Administration.PRIORITY, 1, 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, 3, 0);
                        this.connect(issue, Administration.STATUS, 4, 1);
                        this.connect(issue, Administration.STATUS, 5, 2);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(1L, 2L, 3L), 2);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(4L, 5L, 6L), 0);
                        this.connect(issue, Administration.SEVERITY, 7, 1);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, 3, 1);
                        this.connect(issue, Administration.STATUS, 4, 2);
                        this.connect(issue, Administration.STATUS, 5, 3);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(1L, 2L, 3L, 4L, 5L), 90200);
                        this.connect(issue, Administration.SEVERITY, 6, 90203);
                        this.connect(issue, Administration.SEVERITY, 7, 90201);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(5L, 6L, 4L), 4);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(2L, 1L), 2);
                        break;
                }
                break;
            case Jira:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Jira:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 3, 30);
                        this.connect(issue, Administration.STATUS, 10000, 50);
                        this.connect(issue, Administration.STATUS, 10002, 80);

                        this.connect(issue, Administration.SEVERITY, 10006, 50);
                        this.connect(issue, Administration.SEVERITY, 10002, 40);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(10005L, 10003L), 10);
                        this.connect(issue, Administration.SEVERITY, 10000, 70);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 20);
                        this.connect(issue, Administration.PRIORITY, 3, 30);
                        this.connect(issue, Administration.PRIORITY, 2, 40);
                        this.connect(issue, Administration.PRIORITY, 1, 60);

                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, 3, 1);
                        this.connect(issue, Administration.STATUS, 10000, 5);
                        this.connect(issue, Administration.STATUS, 10002, 7);

                        this.connect(issue, Administration.SEVERITY, 10006, 5);
                        this.connect(issue, Administration.SEVERITY, 10002, 6);
                        this.connect(issue, Administration.SEVERITY, 10003, 10);
                        this.connect(issue, Administration.SEVERITY, 10005, 8);
                        this.connect(issue, Administration.SEVERITY, 10000, 12);

                        this.connect(issue, Administration.PRIORITY, 10, 1);
                        this.connect(issue, Administration.PRIORITY, 9, 2);
                        this.connect(issue, Administration.PRIORITY, 8, 3);
                        this.connect(issue, Administration.PRIORITY, 7, 4);
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, 10000, 1);
                        this.connect(issue, Administration.STATUS, 3, 2);
                        this.connect(issue, Administration.STATUS, 10002, 3);

                        this.connect(issue, Administration.SEVERITY, 1, 1);
                        this.connect(issue, Administration.SEVERITY, 10005, 2);
                        this.connect(issue, Administration.SEVERITY, 10002, 3);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 1);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        this.connect(issue, Administration.PRIORITY, 1, 4);
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, 10000, 3);
                        this.connect(issue, Administration.STATUS, 3, 4);
                        this.connect(issue, Administration.STATUS, 10002, 5);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(10006L, 10002L, 10003L, 10005L, 10000L), 1);

                        this.connect(issue, Administration.PRIORITY, 5, 5);
                        this.connect(issue, Administration.PRIORITY, 4, 4);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 1, 1);
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, 10000, 6);
                        this.connect(issue, Administration.STATUS, 3, 7);
                        this.connect(issue, Administration.STATUS, 10002, 12);

                        this.connect(issue, Administration.SEVERITY, 10005L, 4);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(10006L, 10002L, 10003L, 10000L), 7);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 7);
                        this.connect(issue, Administration.PRIORITY, 3, 8);
                        this.connect(issue, Administration.PRIORITY, 2, 9);
                        this.connect(issue, Administration.PRIORITY, 1, 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, 10000, 3);
                        this.connect(issue, Administration.STATUS, 3, 1);
                        this.connect(issue, Administration.STATUS, 10002, 2);

                        this.connect(issue, Administration.SEVERITY, 10005L, 1);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(10006L, 10002L, 10003L, 10000L), 0);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, 10000, 1);
                        this.connect(issue, Administration.STATUS, 3, 2);
                        this.connect(issue, Administration.STATUS, 10002, 3);

                        this.connect(issue, Administration.SEVERITY, 10005L, 90202);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(10006L, 10002L, 10003L, 10000L), 90200);

                        this.connect(issue, Administration.PRIORITY, Arrays.asList(4L, 5L), 4);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(1L, 2L), 2);
                        break;
                }
                break;
            case OpenProject:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case OpenProject:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 1, 10);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L, 4L), 20);
                        this.connect(issue, Administration.STATUS, Arrays.asList(5L, 6L, 7L), 30);
                        this.connect(issue, Administration.STATUS, Arrays.asList(8L, 9L), 40);
                        this.connect(issue, Administration.STATUS, Arrays.asList(10L, 11L), 50);
                        this.connect(issue, Administration.STATUS, 12, 80);
                        this.connect(issue, Administration.STATUS, Arrays.asList(13L, 14L, 15L), 90);

                        this.connect(issue, Administration.SEVERITY, 4, 10);
                        this.connect(issue, Administration.SEVERITY, 7, 50);

                        this.connect(issue, Administration.PRIORITY, 7, 20);
                        this.connect(issue, Administration.PRIORITY, 8, 30);
                        this.connect(issue, Administration.PRIORITY, 9, 40);
                        this.connect(issue, Administration.PRIORITY, 10, 60);

                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, Arrays.asList(1L, 2L, 3L, 4L, 5L), 0);
                        this.connect(issue, Administration.STATUS, 6, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(7L, 8L, 9L, 10L, 11L, 12L), 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(13L, 14L, 15L), 5);
                        this.connect(issue, Administration.STATUS, 5, 3);

                        this.connect(issue, Administration.SEVERITY, 4, 8);
                        this.connect(issue, Administration.SEVERITY, 7, 5);

                        this.connect(issue, Administration.PRIORITY, 7, 4);
                        this.connect(issue, Administration.PRIORITY, 8, 3);
                        this.connect(issue, Administration.PRIORITY, 9, 2);
                        this.connect(issue, Administration.PRIORITY, 10, 1);
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, 1, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L), 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(13L, 14L), 3);
                        this.connect(issue, Administration.STATUS, 2, 5);
                        this.connect(issue, Administration.STATUS, 15, 6);

                        this.connect(issue, Administration.SEVERITY, 4, 2);
                        this.connect(issue, Administration.SEVERITY, 7, 1);

                        this.connect(issue, Administration.PRIORITY, 7, 1);
                        this.connect(issue, Administration.PRIORITY, 8, 2);
                        this.connect(issue, Administration.PRIORITY, 9, 3);
                        this.connect(issue, Administration.PRIORITY, 10, 4);
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L), 3);
                        this.connect(issue, Administration.STATUS, 7L, 4);
                        this.connect(issue, Administration.STATUS, Arrays.asList(8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L), 5);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(4L, 7L), 1);

                        this.connect(issue, Administration.PRIORITY, 7, 4);
                        this.connect(issue, Administration.PRIORITY, 8, 3);
                        this.connect(issue, Administration.PRIORITY, 9, 2);
                        this.connect(issue, Administration.PRIORITY, 10, 1);
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L), 10000);
                        this.connect(issue, Administration.STATUS, 7L, 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L), 10002);

                        this.connect(issue, Administration.SEVERITY, 4, 10005);
                        this.connect(issue, Administration.SEVERITY, 7, 10006);

                        this.connect(issue, Administration.PRIORITY, 7, 4);
                        this.connect(issue, Administration.PRIORITY, 8, 3);
                        this.connect(issue, Administration.PRIORITY, 9, 9);
                        this.connect(issue, Administration.PRIORITY, 10, 10);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L), 0);
                        this.connect(issue, Administration.STATUS, 7L, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L), 2);

                        this.connect(issue, Administration.SEVERITY, 4, 1);
                        this.connect(issue, Administration.SEVERITY, 7, 0);
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L), 1);
                        this.connect(issue, Administration.STATUS, 7L, 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L), 3);

                        this.connect(issue, Administration.SEVERITY, 4, 90202);
                        this.connect(issue, Administration.SEVERITY, 7, 90200);

                        this.connect(issue, Administration.PRIORITY, 7, 4);
                        this.connect(issue, Administration.PRIORITY, 8, 3);
                        this.connect(issue, Administration.PRIORITY, Arrays.asList(9L, 10L), 2);
                        break;
                }
                break;
            case PivotalTracker:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case PivotalTracker:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 0, 10);
                        this.connect(issue, Administration.STATUS, 1, 30);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L, 4L), 80);

                        this.connect(issue, Administration.SEVERITY, 0, 20);
                        this.connect(issue, Administration.SEVERITY, 1, 10);
                        this.connect(issue, Administration.SEVERITY, 2, 2);

                        issue.setPriority(10, this.getValue(Administration.PRIORITY, 10));
                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, 0, 0);
                        this.connect(issue, Administration.STATUS, 1, 2);
                        this.connect(issue, Administration.STATUS, 2, 5);
                        this.connect(issue, Administration.STATUS, 4, 10);

                        this.connect(issue, Administration.SEVERITY, 0, 5);
                        this.connect(issue, Administration.SEVERITY, 1, 10);
                        this.connect(issue, Administration.SEVERITY, 2, 12);

                        issue.setPriority(4, this.getValue(Administration.PRIORITY, 4));
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, 0, 1);
                        this.connect(issue, Administration.STATUS, 1, 2);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 5L), 5);
                        this.connect(issue, Administration.STATUS, 4, 6);

                        this.connect(issue, Administration.SEVERITY, 0, 1);
                        this.connect(issue, Administration.SEVERITY, 1, 2);
                        this.connect(issue, Administration.SEVERITY, 2, 3);

                        issue.setPriority(2, this.getValue(Administration.PRIORITY, 2));
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, 0, 3);
                        this.connect(issue, Administration.STATUS, 1, 4);
                        this.connect(issue, Administration.STATUS, Arrays.asList(2L, 3L, 4L, 5L), 5);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(0L, 1L, 2L), 1);

                        issue.setPriority(3, this.getValue(Administration.PRIORITY, 3));
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L), 3);
                        this.connect(issue, Administration.STATUS, 2, 10000);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L, 5L), 10002);

                        this.connect(issue, Administration.SEVERITY, 0, 10006);
                        this.connect(issue, Administration.SEVERITY, 1, 10005);
                        this.connect(issue, Administration.SEVERITY, 2, 10000);

                        issue.setPriority(3, this.getValue(Administration.PRIORITY, 3));
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L), 1);
                        this.connect(issue, Administration.STATUS, 2, 7);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L, 5L), 13);

                        this.connect(issue, Administration.SEVERITY, 1, 4);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(0L, 2L), 7);

                        issue.setPriority(8, this.getValue(Administration.PRIORITY, 8));
                        break;
                    case Github:
                        break;
                    case Backlog:
                        this.connect(issue, Administration.STATUS, Arrays.asList(0L, 1L), 1);
                        this.connect(issue, Administration.STATUS, 2, 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L, 5L), 4);

                        this.connect(issue, Administration.SEVERITY, 0, 90200);
                        this.connect(issue, Administration.SEVERITY, 1, 90202);
                        this.connect(issue, Administration.SEVERITY, 2, 90203);

                        issue.setPriority(3, this.getValue(Administration.PRIORITY, 3));
                        break;
                }
                break;
            case Github:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Github:
                        return issue;
                    case MantisBT:
                        issue.setStatus(10, this.getValue(Administration.STATE, 10));
                        issue.setPriority(10, this.getValue(Administration.PRIORITY, 10));
                        issue.setSeverity(10, this.getValue(Administration.SEVERITY, 10));
                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        issue.setStatus(1, this.getValue(Administration.STATE, 1));
                        issue.setPriority(4, this.getValue(Administration.PRIORITY, 4));
                        issue.setSeverity(5, this.getValue(Administration.SEVERITY, 5));
                        break;
                    case RedMine:
                        issue.setStatus(1, this.getValue(Administration.STATE, 1));
                        issue.setPriority(2, this.getValue(Administration.PRIORITY, 2));
                        issue.setSeverity(1, this.getValue(Administration.SEVERITY, 1));
                        break;
                    case Bugzilla:
                        issue.setStatus(4, this.getValue(Administration.STATE, 4));
                        issue.setSeverity(4, this.getValue(Administration.SEVERITY, 4));
                        issue.setPriority(3, this.getValue(Administration.PRIORITY, 3));
                        break;
                    case Jira:
                        issue.setStatus(3, this.getValue(Administration.STATE, 3));
                        issue.setSeverity(10006, this.getValue(Administration.SEVERITY, 10006));
                        break;
                    case OpenProject:
                        issue.setStatus(4, this.getValue(Administration.STATE, 4));
                        issue.setSeverity(7, this.getValue(Administration.SEVERITY, 7));
                        break;
                    case PivotalTracker:
                        issue.setStatus(1, this.getValue(Administration.STATE, 1));
                        issue.setSeverity(0, this.getValue(Administration.SEVERITY, 0));
                        break;
                    case Backlog:
                        issue.setStatus(1, this.getValue(Administration.STATE, 1));
                        issue.setSeverity(90200, this.getValue(Administration.SEVERITY, 90200));
                        break;
                }
                break;
            case Backlog:
                switch (this.toBugService.getAuthentication().getTracker()) {
                    case Local:
                    case Backlog:
                        return issue;
                    case MantisBT:
                        this.connect(issue, Administration.STATUS, 1, 20);
                        this.connect(issue, Administration.STATUS, 2, 50);
                        this.connect(issue, Administration.STATUS, 3, 80);
                        this.connect(issue, Administration.STATUS, 4, 90);

                        this.connect(issue, Administration.SEVERITY, 90201, 40);
                        this.connect(issue, Administration.SEVERITY, 90200, 50);
                        this.connect(issue, Administration.SEVERITY, 90202, 20);
                        this.connect(issue, Administration.SEVERITY, 90203, 30);

                        this.connect(issue, Administration.PRIORITY, 2, 40);
                        this.connect(issue, Administration.PRIORITY, 3, 30);
                        this.connect(issue, Administration.PRIORITY, 4, 20);

                        issue.setState(10, this.getValue(Administration.VIEW, 10));
                        issue.setReproducibility(100, this.getValue(Administration.REPRODUCIBILITY, 100));
                        issue.setResolution(10, this.getValue(Administration.RESOLUTION, 10));
                        this.getDefaultCategory(issue);
                        break;
                    case YouTrack:
                        this.connect(issue, Administration.STATUS, 1, 1);
                        this.connect(issue, Administration.STATUS, 2, 2);
                        this.connect(issue, Administration.STATUS, 3, 5);
                        this.connect(issue, Administration.STATUS, 4, 7);

                        this.connect(issue, Administration.SEVERITY, 90201, 9);
                        this.connect(issue, Administration.SEVERITY, 90200, 5);
                        this.connect(issue, Administration.SEVERITY, 90202, 8);
                        this.connect(issue, Administration.SEVERITY, 90203, 9L);

                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 4, 4);
                        break;
                    case RedMine:
                        this.connect(issue, Administration.STATUS, 1, 1);
                        this.connect(issue, Administration.STATUS, 2, 2);
                        this.connect(issue, Administration.STATUS, 3, 3);
                        this.connect(issue, Administration.STATUS, 4, 5);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90200L, 90201L), 1);
                        this.connect(issue, Administration.SEVERITY, 90202, 2);
                        this.connect(issue, Administration.SEVERITY, 90203, 3L);

                        this.connect(issue, Administration.PRIORITY, 4, 1);
                        this.connect(issue, Administration.PRIORITY, 3, 2);
                        this.connect(issue, Administration.PRIORITY, 2, 3);
                        break;
                    case Bugzilla:
                        this.connect(issue, Administration.STATUS, 1, 3);
                        this.connect(issue, Administration.STATUS, 2, 4);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 5);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90200L, 90201L, 90202L, 90203L), 1);

                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 4, 2);
                        break;
                    case Jira:
                        this.connect(issue, Administration.STATUS, 1, 10000);
                        this.connect(issue, Administration.STATUS, 2, 3);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 10002);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90200L, 90201L), 10003);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90202L, 90203L), 10005);

                        this.connect(issue, Administration.PRIORITY, 2, 2);
                        this.connect(issue, Administration.PRIORITY, 3, 3);
                        this.connect(issue, Administration.PRIORITY, 4, 42);
                        break;
                    case OpenProject:
                        this.connect(issue, Administration.STATUS, 1, 1);
                        this.connect(issue, Administration.STATUS, 2, 7);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 13);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90200L, 90201L), 7);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90202L, 90203L), 4);

                        this.connect(issue, Administration.PRIORITY, 2, 9);
                        this.connect(issue, Administration.PRIORITY, 3, 8);
                        this.connect(issue, Administration.PRIORITY, 4, 7);
                        break;
                    case PivotalTracker:
                        this.connect(issue, Administration.STATUS, 1, 0);
                        this.connect(issue, Administration.STATUS, 2, 1);
                        this.connect(issue, Administration.STATUS, Arrays.asList(3L, 4L), 3);

                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90200L, 90201L), 0);
                        this.connect(issue, Administration.SEVERITY, Arrays.asList(90202L, 90203L), 1);
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
                    case Administration.VIEW:
                        if(issue.getState().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setState((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case Administration.REPRODUCIBILITY:
                        if(issue.getReproducibility().getKey().toString().equals(String.valueOf(oldId))) {
                            issue.setReproducibility((int) newId, this.getString(newEntries, newId));
                        }
                        break;
                    case Administration.SEVERITY:
                        if (!this.severity) {
                            if (issue.getSeverity().getKey().toString().equals(String.valueOf(oldId))) {
                                issue.setSeverity((int) newId, this.getString(newEntries, newId));
                                this.severity = true;
                            }
                        }
                        break;
                    case Administration.PRIORITY:
                        if (!this.priority) {
                            if (issue.getPriority().getKey().toString().equals(String.valueOf(oldId))) {
                                issue.setPriority((int) newId, this.getString(newEntries, newId));
                                this.priority = true;
                            }
                        }
                        break;
                    case Administration.STATUS:
                        if (!this.status) {
                            if (issue.getStatus().getKey().toString().equals(String.valueOf(oldId))) {
                                issue.setStatus((int) newId, this.getString(newEntries, newId));
                                this.status = true;
                            }
                        }
                        break;
                    case Administration.RESOLUTION:
                        if (!this.resolution) {
                            if (issue.getResolution().getKey().toString().equals(String.valueOf(oldId))) {
                                issue.setResolution((int) newId, this.getString(newEntries, newId));
                                this.resolution = true;
                            }
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
