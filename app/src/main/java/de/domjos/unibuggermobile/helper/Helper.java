/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
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
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.InputStream;
import java.util.Properties;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.permissions.BugzillaPermissions;
import de.domjos.unibuggerlibrary.permissions.GithubPermissions;
import de.domjos.unibuggerlibrary.permissions.MantisBTPermissions;
import de.domjos.unibuggerlibrary.permissions.RedminePermissions;
import de.domjos.unibuggerlibrary.permissions.SQLitePermissions;
import de.domjos.unibuggerlibrary.permissions.YoutrackPermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.tracker.Bugzilla;
import de.domjos.unibuggerlibrary.services.tracker.Github;
import de.domjos.unibuggerlibrary.services.tracker.MantisBT;
import de.domjos.unibuggerlibrary.services.tracker.Redmine;
import de.domjos.unibuggerlibrary.services.tracker.SQLite;
import de.domjos.unibuggerlibrary.services.tracker.YouTrack;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.activities.MainActivity;

public class Helper {

    public static Properties readPropertiesFromRaw(int rawID, Context context) throws Exception {
        Properties properties = new Properties();
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawID);
        properties.load(in_s);
        return properties;
    }

    public static String readStringFromRaw(int rawID, Context context) throws Exception {
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawID);

        byte[] b = new byte[in_s.available()];
        in_s.read(b);
        return new String(b);
    }

    public static int getVersionCode(Context context) throws Exception {
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return info.versionCode;
    }

    public static View getRowView(Context context, ViewGroup parent, int layout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return inflater.inflate(layout, parent, false);
        }
        return new View(context);
    }

    public static IBugService getCurrentBugService(Context context) {
        IBugService bugService = null;
        try {
            Authentication authentication = MainActivity.settings.getCurrentAuthentication();
            if (authentication != null) {
                switch (authentication.getTracker()) {
                    case MantisBT:
                        bugService = new MantisBT(authentication);
                        break;
                    case Bugzilla:
                        bugService = new Bugzilla(authentication);
                        break;
                    case YouTrack:
                        bugService = new YouTrack(MainActivity.settings.getCurrentAuthentication());
                        break;
                    case RedMine:
                        bugService = new Redmine(authentication);
                        break;
                    case Github:
                        bugService = new Github(authentication);
                        break;
                    default:
                        bugService = new SQLite(context, Helper.getVersionCode(context));
                        break;
                }
            } else {
                bugService = new SQLite(context, Helper.getVersionCode(context));
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, context);
        }
        return bugService;
    }

    public static IFunctionImplemented getCurrentPermissions(Context context) {
        IFunctionImplemented functionImplemented = null;
        try {
            Authentication authentication = MainActivity.settings.getCurrentAuthentication();
            if (authentication != null) {
                switch (authentication.getTracker()) {
                    case MantisBT:
                        functionImplemented = new MantisBTPermissions(authentication);
                        break;
                    case Bugzilla:
                        functionImplemented = new BugzillaPermissions(authentication);
                        break;
                    case YouTrack:
                        functionImplemented = new YoutrackPermissions(authentication);
                        break;
                    case RedMine:
                        functionImplemented = new RedminePermissions(authentication);
                        break;
                    case Github:
                        functionImplemented = new GithubPermissions(authentication);
                        break;
                    default:
                        functionImplemented = new SQLitePermissions();
                        break;
                }
            } else {
                functionImplemented = new SQLitePermissions();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, context);
        }
        return functionImplemented;
    }


    public static ArrayAdapter<String> setAdapter(Context context, String key) {
        if (context != null) {
            int spItem = android.R.layout.simple_spinner_item;
            return new ArrayAdapter<>(context, spItem, ArrayHelper.getValues(context, key));
        }
        return null;
    }
}
