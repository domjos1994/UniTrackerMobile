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
