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

package de.domjos.unibuggermobile.activities;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.customwidgets.widgets.calendar.WidgetCalendar;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.TrackerEvent;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.CalendarTask;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;

public final class CalendarActivity extends AbstractActivity {
    private WidgetCalendar widgetCalendar;
    private ProgressBar progressBar;
    private TextView lblCalendarTitle, lblCalendarSubTitle, lblCalendarState;


    public CalendarActivity() {
        super(R.layout.calendar_activity);
    }

    @Override
    protected void initActions() {

        this.widgetCalendar.setOnClick(event -> {
            TrackerEvent trackerEvent = (TrackerEvent) event;
            this.lblCalendarTitle.setText(trackerEvent.getBugTracker().getAuthentication().getTitle());

            String title = trackerEvent.getProject().getTitle();
            while (title.startsWith("-")) {
                title = title.replaceFirst("-", "");
            }

            StringBuilder subTitle = new StringBuilder();
            subTitle.append(this.getString(R.string.calendar_project)).append(": ").append(title).append("\n");
            if(trackerEvent.getVersion() != null) {
                subTitle.append(this.getString(R.string.calendar_version)).append(": ").append(trackerEvent.getVersion().getTitle()).append("\n");
            }
            if(trackerEvent.getIssue() != null) {
                subTitle.append(this.getString(R.string.calendar_issue)).append(": ").append(trackerEvent.getIssue().getTitle());
            }
            this.lblCalendarSubTitle.setText(subTitle.toString());
        });
    }

    @Override
    protected void initControls() {
        Helper.initToolbar(this);

        this.widgetCalendar = this.findViewById(R.id.cvEventCalendar);
        this.widgetCalendar.showDay(false);
        this.progressBar = this.findViewById(R.id.pbCalendar);

        this.lblCalendarTitle = this.findViewById(R.id.lblCalendarTitle);
        this.lblCalendarSubTitle = this.findViewById(R.id.lblCalendarSubTitle);
        this.lblCalendarState = this.findViewById(R.id.lblCalendarState);
    }

    @Override
    protected void reload() {
        this.insertEvents();
    }

    private void insertEvents() {
        try {
            this.progressBar.setVisibility(View.VISIBLE);
            boolean notifications = MainActivity.GLOBALS.getSettings(this.getApplicationContext()).showNotifications();

            List<Authentication> accounts = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("");
            List<IBugService<?>> bugServices = new LinkedList<>();
            for(Authentication account : accounts) {
                IBugService<?> bugService = Helper.getCurrentBugService(account, this.getApplicationContext());
                bugServices.add(bugService);
            }

            CalendarTask calendarTask = new CalendarTask(CalendarActivity.this, notifications, R.drawable.icon_calendar, this.progressBar, this.lblCalendarState, this.widgetCalendar, bugServices);
            calendarTask.setIcons(R.drawable.icon_projects, R.drawable.icon_versions, R.drawable.icon_issues);
            calendarTask.execute();
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, CalendarActivity.this);
        }
    }
}
