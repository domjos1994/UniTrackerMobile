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
