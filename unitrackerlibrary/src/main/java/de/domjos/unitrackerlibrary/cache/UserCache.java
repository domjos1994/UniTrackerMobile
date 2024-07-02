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

import de.domjos.unitrackerlibrary.model.issues.User;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class UserCache {
    private static List<User<?>> users = new LinkedList<>();
    private static Authentication currentAuth;
    private static Object currentProjectId;
    private static Calendar calendar;

    public static List<User<?>> getUsers() {
        return UserCache.users;
    }

    public static boolean mustReload(Authentication authentication, Object projectId) {
        if(!CacheGlobals.useCache) {
            return true;
        }
        if(CacheGlobals.reload()) {
            return true;
        }
        if(UserCache.users.isEmpty()) {
            return true;
        }
        if(CacheGlobals.differenceBetweenAuthentications(UserCache.currentAuth, authentication)) {
            return true;
        }
        if(!UserCache.currentProjectId.equals(projectId)) {
            return true;
        }

        return CacheGlobals.timesUp(UserCache.calendar);
    }

    public static void setData(List<User<?>> users, Authentication authentication, Object projectId) {
        UserCache.users = users;
        UserCache.currentAuth = authentication;
        UserCache.currentProjectId = projectId;
        UserCache.calendar = GregorianCalendar.getInstance();
    }
}
