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

import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class CacheGlobals {
    public static boolean useCache = false, reloadOnPullDown = true;
    public static int minutesToReload = 30;
    public static boolean reload = false;

    public static boolean reload() {
        if(CacheGlobals.reloadOnPullDown) {
            if(CacheGlobals.reload) {
                CacheGlobals.reload = false;
                return true;
            }
        }
        return false;
    }

    public static boolean differenceBetweenAuthentications(Authentication authentication1, Authentication authentication2) {
        if(!authentication1.getTracker().equals(authentication2.getTracker())) {
            return true;
        }
        if(!authentication1.getServer().equals(authentication2.getServer())) {
            return true;
        }
        if(!authentication1.getUserName().equals(authentication2.getUserName())) {
            return true;
        }
        return !authentication1.getAPIKey().equals(authentication2.getAPIKey());
    }

    public static boolean timesUp(Calendar calendar) {
        Calendar current = GregorianCalendar.getInstance();
        calendar.add(Calendar.MINUTE, CacheGlobals.minutesToReload);
        return calendar.before(current);
    }
}
