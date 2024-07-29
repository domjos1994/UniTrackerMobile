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

package de.domjos.unitrackerlibrary.tasks;

import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.custom.WidgetCalendar;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.TrackerEvent;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;

/** @noinspection rawtypes*/
public final class CalendarTask extends AbstractTask<Void, Map.Entry<Integer, List<TrackerEvent>>, Void> {
    private final WeakReference<ProgressBar> progressBar;
    private final WeakReference<TextView> lbl;
    private final WeakReference<WidgetCalendar> calendar;
    private final List<IBugService<?>> bugServices;
    private int max, projectIcon, versionIcon, issueIcon;

    public CalendarTask(Activity activity, boolean showNotifications, int icon, ProgressBar progressBar, TextView lbl, WidgetCalendar calendar, List<IBugService<?>> bugServices) {
        super(activity, R.string.task_loader_title, R.string.task_loader_content, showNotifications, icon);

        this.progressBar = new WeakReference<>(progressBar);
        this.lbl = new WeakReference<>(lbl);
        this.bugServices = bugServices;
        this.calendar = new WeakReference<>(calendar);
    }

    public void setIcons(int project, int version, int issue) {
        this.projectIcon = project;
        this.versionIcon = version;
        this.issueIcon = issue;
    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(@NonNull Map.Entry<Integer, List<TrackerEvent>>... values) {
        int percentage = (int) (100.0 / max * values[0].getKey());
        this.progressBar.get().setProgress(percentage);

        TrackerEvent event = values[1].getValue().get(0);
        if(event != null) {
            String text = event.getBugTracker().getAuthentication().getTitle() + ": " + event.getProject().getTitle();
            this.lbl.get().setText(text);
        }

        this.calendar.get().getEvents().clear();
        for(TrackerEvent trackerEvent : values[0].getValue()) {
            this.calendar.get().addEvent(trackerEvent);
        }
        this.calendar.get().invalidate();
        this.calendar.get().reload();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Void doInBackground(Void... voids) {
        List<TrackerEvent> events = new LinkedList<>();

        try {
            for(IBugService bugService : this.bugServices) {
                List<Project> projects = bugService.getProjects();

                TrackerEvent currentEvent = null;
                for(Project project : projects) {
                    if(project.getReleasedAt() != 0) {
                        TrackerEvent projectEvent = new TrackerEvent();
                        projectEvent.setCalendar(this.getDate(project.getReleasedAt()));
                        projectEvent.setBugTracker(bugService);
                        projectEvent.setProject(project);
                        projectEvent.setIcon(this.projectIcon);
                        currentEvent = projectEvent;
                        events.add(projectEvent);
                    }

                    List<Version> versions = bugService.getVersions("versions", project.getId());
                    for(Version version : versions) {
                        if(version.getReleasedVersionAt() != 0) {
                            TrackerEvent versionEvent = new TrackerEvent();
                            versionEvent.setCalendar(this.getDate(version.getReleasedVersionAt()));
                            versionEvent.setBugTracker(bugService);
                            versionEvent.setProject(project);
                            versionEvent.setVersion(version);
                            versionEvent.setIcon(this.versionIcon);
                            currentEvent = versionEvent;
                            events.add(versionEvent);
                        }
                    }

                    List<Issue> issues = bugService.getIssues(project.getId());
                    this.max = issues.size();
                    int current = 0;
                    for(Issue issue : issues) {
                        issue = bugService.getIssue(issue.getId(), project.getId());

                        if(issue.getDueDate() != null) {
                            TrackerEvent issueEvent = new TrackerEvent();
                            issueEvent.setCalendar(issue.getDueDate());
                            issueEvent.setBugTracker(bugService);
                            issueEvent.setProject(project);
                            issueEvent.setIssue(issue);
                            issueEvent.setIcon(this.issueIcon);
                            currentEvent = issueEvent;
                            events.add(issueEvent);
                        }

                        publishProgress(new AbstractMap.SimpleEntry<>(current, events), new AbstractMap.SimpleEntry<>(current, Collections.singletonList(currentEvent)));
                        current++;
                    }
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        }
        return null;
    }

    private Date getDate(long time) {
        Date dt = new Date();
        dt.setTime(time);
        return dt;
    }
}
