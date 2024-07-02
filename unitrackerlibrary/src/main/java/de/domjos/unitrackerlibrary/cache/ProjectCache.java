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

package de.domjos.unitrackerlibrary.cache;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class ProjectCache {
    private static List<Project<?>> projects = new LinkedList<>();
    private static Authentication currentAuth;
    private static Calendar calendar;

    public static List<Project<?>> getProjects() {
        return ProjectCache.projects;
    }

    public static boolean mustReload(Authentication authentication) {
        if(!CacheGlobals.useCache) {
            return true;
        }
        if(CacheGlobals.reload()) {
            return true;
        }
        if(ProjectCache.projects.isEmpty()) {
            return true;
        }
        if(CacheGlobals.differenceBetweenAuthentications(ProjectCache.currentAuth, authentication)) {
            return true;
        }

        return CacheGlobals.timesUp(ProjectCache.calendar);
    }

    public static void setData(List<Project<?>> projects, Authentication authentication) {
        ProjectCache.projects = projects;
        ProjectCache.currentAuth = authentication;
        ProjectCache.calendar = GregorianCalendar.getInstance();
    }
}
