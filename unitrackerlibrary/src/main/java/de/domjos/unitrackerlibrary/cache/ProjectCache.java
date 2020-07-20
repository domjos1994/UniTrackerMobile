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
