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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.InputStream;
import java.util.Properties;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.tracker.Bugzilla;
import de.domjos.unibuggerlibrary.services.tracker.Github;
import de.domjos.unibuggerlibrary.services.tracker.MantisBT;
import de.domjos.unibuggerlibrary.services.tracker.Redmine;
import de.domjos.unibuggerlibrary.services.tracker.SQLite;
import de.domjos.unibuggerlibrary.services.tracker.YouTrack;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.activities.MainActivity;

import static android.support.constraint.motion.MotionScene.TAG;

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
        return Helper.getCurrentBugService(MainActivity.GLOBALS.getSettings(context).getCurrentAuthentication(), context);
    }

    public static IBugService getCurrentBugService(Authentication authentication, Context context) {
        IBugService bugService = null;
        try {
            if (authentication != null) {
                switch (authentication.getTracker()) {
                    case MantisBT:
                        bugService = new MantisBT(authentication);
                        break;
                    case Bugzilla:
                        bugService = new Bugzilla(authentication);
                        break;
                    case YouTrack:
                        bugService = new YouTrack(authentication);
                        break;
                    case RedMine:
                        bugService = new Redmine(authentication);
                        break;
                    case Github:
                        bugService = new Github(authentication);
                        break;
                    default:
                        bugService = new SQLite(context, Helper.getVersionCode(context), authentication);
                        break;
                }
            } else {
                bugService = new SQLite(context, Helper.getVersionCode(context), new Authentication());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, context);
        }
        return bugService;
    }

    public static ArrayAdapter<String> setAdapter(Context context, String key) {
        if (context != null) {
            int spItem = android.R.layout.simple_spinner_item;
            return new ArrayAdapter<>(context, spItem, ArrayHelper.getValues(context, key));
        }
        return null;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }
}
