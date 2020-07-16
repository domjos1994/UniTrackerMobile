package de.domjos.unitrackerlibrary.cache;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class IssueCache {
    private static List<Issue> issues = new LinkedList<>();
    private static Authentication currentAuth;
    private static Object currentProjectId;
    private static Calendar calendar;
    private static int page, numberOfItems;
    private static String filter = "";

    public static List<Issue> getIssues() {
        return IssueCache.issues;
    }

    public static boolean mustReload(Authentication authentication, Object projectId, int page, int numberOfItems, String filter) {
        try {
            if(!CacheGlobals.useCache) {
                return true;
            }
            if(CacheGlobals.reload()) {
                return true;
            }
            if(IssueCache.issues.isEmpty()) {
                return true;
            }
            if(CacheGlobals.differenceBetweenAuthentications(IssueCache.currentAuth, authentication)) {
                return true;
            }
            if(IssueCache.page != page) {
                return true;
            }
            if(IssueCache.numberOfItems != numberOfItems) {
                return true;
            }
            if(!IssueCache.filter.equals(filter)) {
                return true;
            }
            if(CacheGlobals.timesUp(IssueCache.calendar)) {
                return true;
            }
            return !IssueCache.currentProjectId.equals(projectId);
        } catch (Exception ignored) {}
        return true;
    }

    public static void setData(List<Issue> issue, Authentication authentication, Object projectId, int page, int numberOfItems, String filter) {
        IssueCache.issues = issue;
        IssueCache.currentAuth = authentication;
        IssueCache.currentProjectId = projectId;
        IssueCache.page = page;
        IssueCache.numberOfItems = numberOfItems;
        IssueCache.filter = filter;
        IssueCache.calendar = GregorianCalendar.getInstance();
    }
}
