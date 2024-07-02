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

import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class VersionCache {
    private static List<Version<?>> versions = new LinkedList<>();
    private static Authentication currentAuth;
    private static Object currentProjectId;
    private static Calendar calendar;
    private static String filter = "";

    public static List<Version<?>> getVersions() {
        return VersionCache.versions;
    }

    public static boolean mustReload(Authentication authentication, Object projectId, String filter) {
        if(!CacheGlobals.useCache) {
            return true;
        }
        if(CacheGlobals.reload()) {
            return true;
        }
        if(VersionCache.versions.isEmpty()) {
            return true;
        }
        if(CacheGlobals.differenceBetweenAuthentications(VersionCache.currentAuth, authentication)) {
            return true;
        }
        if(!VersionCache.currentProjectId.equals(projectId)) {
            return true;
        }
        if(!VersionCache.filter.equals(filter)) {
            return true;
        }

        return CacheGlobals.timesUp(VersionCache.calendar);
    }

    public static void setData(List<Version<?>> versions, Authentication authentication, Object projectId, String filter) {
        VersionCache.versions = versions;
        VersionCache.currentAuth = authentication;
        VersionCache.currentProjectId = projectId;
        VersionCache.filter = filter;
        VersionCache.calendar = GregorianCalendar.getInstance();
    }
}
