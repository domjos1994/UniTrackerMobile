/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggerlibrary.services.engine;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class Authentication extends DescriptionObject<Long> {
    private String server;
    private String userName;
    private String password;
    private String APIKey;
    private byte[] cover;
    private Tracker tracker;
    private boolean guest;

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
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
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
        Backlog
    }
}
