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

package de.domjos.unitrackerlibrary.model.custom;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public abstract class Event {
    private long id;
    private Calendar calendar, end;
    private String name;
    private String description;
    private int color;
    private Object object;

    public Event() {
        this(new Date(), null, "", "", -1);
    }

    public Event(Date dt, Date end, String name, String description, int color) {
        this.setCalendar(dt);
        if(end!=null) {
            this.setEnd(end);
        }
        this.name = name;
        this.description = description;
        this.color = color;
        this.object = null;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public final void setCalendar(Date date) {
        this.calendar = Calendar.getInstance(Locale.getDefault());
        this.calendar.setTime(date);
    }

    public final void setEnd(Date date) {
        if(date==null) {
            this.end = null;
        } else {
            this.end = Calendar.getInstance(Locale.getDefault());
            this.end.setTime(date);
        }
    }

    public Calendar getEnd() {
        return this.end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public abstract int getIcon();

    @Override
    @NonNull
    public String toString() {
        return this.getName();
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
