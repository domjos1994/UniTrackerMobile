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
