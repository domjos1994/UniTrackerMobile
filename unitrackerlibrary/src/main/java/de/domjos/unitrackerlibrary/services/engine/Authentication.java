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

package de.domjos.unitrackerlibrary.services.engine;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;

public class Authentication extends DescriptionObject<Long> {
    public static final String PUBLIC_KEY = "publicKey";
   public static final String SECRET_KEY = "secretKey";
    public static final String ENDPOINT_AUTH = "endPointAuth";
    public static final String ENDPOINT_TOKEN = "endPointToken";

    private String server;
    private String userName;
    private String password;
    private String APIKey;
    private byte[] cover;
    private Tracker tracker;
    private boolean guest;
    private Auth authentication;

    public Authentication() {
        this("", "", "", "");
    }

    public Authentication(String server, String user, String password) {
        this(server, "", user, password);
    }

    public Authentication(String server, String key, String userName, String password) {
        super();
        this.server = server;
        this.APIKey = key;
        this.userName = userName;
        this.password = password;
        this.cover = null;
        this.tracker = null;
        this.guest = false;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAPIKey() {
        return this.APIKey;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public byte[] getCover() {
        return this.cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    public Tracker getTracker() {
        return this.tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
        if(this.tracker == Tracker.Github) {
            this.getHints().put(Authentication.PUBLIC_KEY, "812f07f45f6be1f81c5e");
            this.getHints().put(Authentication.SECRET_KEY, "7eb14624b5fee480687d1a160775342b5955eea1");
            this.getHints().put(Authentication.ENDPOINT_AUTH, "https://github.com/login/oauth/authorize");
            this.getHints().put(Authentication.ENDPOINT_TOKEN, "https://github.com/login/oauth/access_token");
        }
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public Auth getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(Auth authentication) {
        this.authentication = authentication;
    }

    public enum Tracker {
        Local,
        YouTrack,
        MantisBT,
        RedMine,
        Bugzilla,
        Github,
        Jira,
        PivotalTracker,
        OpenProject,
        Backlog,
        AzureDevOps,
        TuLeap
    }

    public enum Auth {
        Basic,
        API_KEY,
        OAUTH
    }
}
