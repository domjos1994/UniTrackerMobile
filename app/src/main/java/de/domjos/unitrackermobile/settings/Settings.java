/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.List;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.helper.Helper;

import static android.content.Context.MODE_PRIVATE;

public class Settings {
    private static final String AUTH = "auth_id";
    private static final String PROJECT = "current_project";
    private static final String FILTER = "current_filter";

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences userPreferences;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);
        this.userPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public Authentication getCurrentAuthentication() {
        long authID = this.preferences.getLong(Settings.AUTH, 0);
        if (authID != 0) {
            List<Authentication> authentications = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("ID=" + authID);
            if (authentications.size() >= 1) {
                return MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("ID=" + authID).get(0);
            }
        }
        Authentication authentication = new Authentication();
        authentication.setTracker(Authentication.Tracker.Local);
        return authentication;
    }

    public void setCurrentAuthentication(Authentication authentication) {
        SharedPreferences.Editor editor = this.preferences.edit();
        if (authentication != null) {
            editor.putLong(Settings.AUTH, authentication.getId());
        } else {
            editor.putLong(Settings.AUTH, 0);
        }
        editor.apply();
    }

    public Project getCurrentProject(Activity activity, IBugService bugService) {
        try {
            String title = this.preferences.getString(Settings.PROJECT, "");
            if (!title.isEmpty()) {
                List<Project> projects = new ProjectTask(activity, bugService, false, this.showNotifications(), R.drawable.ic_apps_black_24dp).execute(title).get();
                if (projects.size() >= 1) {
                    return projects.get(0);
                }
            }

        } catch (Exception ignored) {
        }
        return null;
    }

    public Object getCurrentProjectId() {
        return this.preferences.getString(Settings.PROJECT, "");
    }

    public void setCurrentProject(String id) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(Settings.PROJECT, id);
        editor.apply();
    }

    public IBugService.IssueFilter getCurrentFilter() {
        String filter = this.preferences.getString(Settings.FILTER, "");
        if (!filter.isEmpty()) {
            return IBugService.IssueFilter.valueOf(filter);
        }
        return IBugService.IssueFilter.all;
    }

    public void setCurrentFilter(IBugService.IssueFilter filter) {
        this.preferences.edit().putString(Settings.FILTER, filter.name()).apply();
    }

    public boolean isFirstLogin(boolean write) {
        boolean login = this.preferences.getBoolean("firstLogin", false);
        if(write) {
            if (!login) {
                this.preferences.edit().putBoolean("firstLogin", true).apply();
            }
        }
        return login;
    }

    public String getWhatsNewVersion() {
        return this.preferences.getString("whats_new_version", "");
    }

    public void setWhatsNewVersion() {
        this.preferences.edit().putString("whats_new_version", Helper.getVersion(this.context)).apply();
    }

    public boolean showNotifications() {
        return this.userPreferences.getBoolean("swtNotifications", false);
    }

    public boolean isEncryptionEnabled() {
        return this.userPreferences.getBoolean("swtSecurityEnable", true);
    }

    public int getNumberOfItems() {
        String strNumber = this.userPreferences.getString("txtNumberOfItems", "-1");
        try {
            int number = Integer.parseInt(strNumber);
            if (number < -1) {
                return -1;
            }
            return number;
        } catch (Exception ex) {
            return -1;
        }
    }

    public int getReload() {
        String strNumber;
        if (Helper.isInWLan(this.context)) {
            strNumber = this.userPreferences.getString("txtWLanReload", "-1");
        } else {
            strNumber = this.userPreferences.getString("txtMobileReload", "-1");
        }
        try {
            int number = Integer.parseInt(strNumber);
            if (number < -1) {
                return -1;
            }
            return number;
        } catch (Exception ex) {
            return -1;
        }
    }

    public boolean isBlockMobile() {
        return this.userPreferences.getBoolean("swtBlockMobile", false);
    }

    public String getDateFormat() {
        return this.userPreferences.getString("txtFormatDate", this.context.getString(R.string.settings_general_date_format_default));
    }

    public String getTimeFormat() {
        return this.userPreferences.getString("txtFormatTime", this.context.getString(R.string.settings_general_time_format_default));
    }

    public boolean isScrollList() {
        return this.userPreferences.getBoolean("swtScrollListElement", false);
    }

    public boolean isLocalSyncAutomatically() {
        return this.userPreferences.getBoolean("swtSyncAutomatically", false);
    }

    public boolean isShowMantisBugsOfSubProjects() {
        return this.userPreferences.getBoolean("swtBugTrackerMantisSub", false);
    }

    public boolean isShowMantisFilter() {
        return this.userPreferences.getBoolean("swtBugTrackerMantisFilter", false);
    }

    public boolean isShowTutorial() {
        boolean show = this.userPreferences.getBoolean("swtShowTutorialAgain", false);
        if(show) {
            SharedPreferences.Editor editor = this.userPreferences.edit();
            editor.putBoolean("swtShowTutorialAgain", false);
            editor.apply();
        }
        return show;
    }

    public String getLocalSyncPath() {
        String path = this.userPreferences.getString("swtSyncPath", this.context.getFilesDir().getAbsolutePath());
        if (path.equals("")) {
            return this.context.getFilesDir().getAbsolutePath();
        }
        path = path.replace(":", "");
        return path;
    }
}
