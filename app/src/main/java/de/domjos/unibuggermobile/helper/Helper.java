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

package de.domjos.unibuggermobile.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.custom.DropDownAdapter;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.services.ArrayHelper;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.tracker.AzureDevOps;
import de.domjos.unitrackerlibrary.services.tracker.Backlog;
import de.domjos.unitrackerlibrary.services.tracker.Bugzilla;
import de.domjos.unitrackerlibrary.services.tracker.Github;
import de.domjos.unitrackerlibrary.services.tracker.Jira;
import de.domjos.unitrackerlibrary.services.tracker.MantisBT;
import de.domjos.unitrackerlibrary.services.tracker.OpenProject;
import de.domjos.unitrackerlibrary.services.tracker.PivotalTracker;
import de.domjos.unitrackerlibrary.services.tracker.Redmine;
import de.domjos.unitrackerlibrary.services.tracker.SQLite;
import de.domjos.unitrackerlibrary.services.tracker.Tuleap;
import de.domjos.unitrackerlibrary.services.tracker.YouTrack;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.settings.Settings;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;
import de.domjos.unitrackerlibrary.tools.Notifications;

public class Helper {
    public static final List<Authentication.Tracker> disabledBugTrackers =
            Arrays.asList(
                Authentication.Tracker.TuLeap,
                Authentication.Tracker.AzureDevOps
            );

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String readStringFromRaw(int rawID, Context context) throws Exception {
        Resources res = context.getResources();
        try(InputStream in_s = res.openRawResource(rawID)) {
            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return new String(b);
        }
    }

    static int getVersionCode(Context context) throws Exception {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    }

    public static View getRowView(Context context, ViewGroup parent, int layout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            return inflater.inflate(layout, parent, false);
        }
        return new View(context);
    }

    public static Date checkDateAndReturn(EditText txt, Context context) throws Exception {
        String content = txt.getText().toString().trim();
        if(content.isEmpty()) {
            return new Date();
        } else {
            Settings settings = MainActivity.GLOBALS.getSettings(context);
            if(content.contains(":")) {
                return ConvertHelper.convertStringToDate(content, settings.getDateFormat() + " " + settings.getTimeFormat());
            } else {
                return ConvertHelper.convertStringToDate(content, settings.getDateFormat());
            }
        }
    }

    public static IBugService<?> getCurrentBugService(Context context) {
        return Helper.getCurrentBugService(MainActivity.GLOBALS.getSettings(context).getCurrentAuthentication(), context);
    }

    public static IBugService<?> getCurrentBugService(Authentication authentication, Context context) {
        IBugService<?> bugService = null;
        try {
            if (authentication != null) {
                bugService = switch (authentication.getTracker()) {
                    case MantisBT -> {
                        Settings settings = MainActivity.GLOBALS.getSettings(context);
                        yield new MantisBT(
                                authentication,
                                settings.isShowMantisBugsOfSubProjects(),
                                settings.isShowMantisFilter()
                        );
                    }
                    case Bugzilla -> new Bugzilla(authentication);
                    case YouTrack -> new YouTrack(authentication);
                    case RedMine -> new Redmine(authentication);
                    case Github -> new Github(authentication);
                    case Jira -> new Jira(authentication);
                    case PivotalTracker -> new PivotalTracker(authentication);
                    case OpenProject -> new OpenProject(authentication);
                    case Backlog -> new Backlog(authentication);
                    case AzureDevOps -> new AzureDevOps(authentication);
                    case TuLeap -> new Tuleap(authentication);
                    default -> new SQLite(context, Helper.getVersionCode(context), authentication);
                };
            } else {
                bugService = new SQLite(context, Helper.getVersionCode(context), new Authentication());
            }
        } catch (Exception ex) {
            Notifications.printException((Activity)context,  ex, R.mipmap.ic_launcher_round);
        }
        return bugService;
    }

    public static ArrayAdapter<SpinnerItem> setAdapter(Context context, String key) {
        if (context != null) {
            int spItem = R.layout.spinner_item;
            ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(context, spItem);
            Map<String, String> data = ArrayHelper.getMap(context, key);
            for(Map.Entry<String, String> entry : data.entrySet()) {
                adapter.add(new SpinnerItem(Integer.parseInt(entry.getKey()), entry.getValue()));
            }
            return adapter;
        }
        return null;
    }

    public static DropDownAdapter<SpinnerItem> setDropDownAdapter(Context context, String key) {
        if (context != null) {
            DropDownAdapter<SpinnerItem> adapter = new DropDownAdapter<>(context);
            Map<String, String> data = ArrayHelper.getMap(context, key);
            for(Map.Entry<String, String> entry : data.entrySet()) {
                adapter.add(new SpinnerItem(Integer.parseInt(entry.getKey()), entry.getValue()));
            }
            return adapter;
        }
        return null;
    }

    public static Boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    public static boolean isInWLan(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo!=null) {
                return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }

    public static void isStoragePermissionGranted(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("dd", "Permission is granted");
        } else {

            Log.v("dd", "Permission is revoked");
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public static FilePickerDialog initFilePickerDialog(Activity activity, boolean selectDir, String[] extensions, String title) {
        DialogProperties dialogProperties = new DialogProperties();
        dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
        dialogProperties.root = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.offset = new File(DialogConfigs.DEFAULT_DIR);
        dialogProperties.extensions = extensions;
        if(selectDir) {
            dialogProperties.selection_type = DialogConfigs.DIR_SELECT;
        } else {
            dialogProperties.selection_type = DialogConfigs.FILE_SELECT;
        }

        FilePickerDialog dialog = new FilePickerDialog(activity, dialogProperties);
        dialog.setCancelable(true);
        dialog.setTitle(title);
        return dialog;
    }

    public static String getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ex) {
            Notifications.printException((Activity)context,  ex, R.mipmap.ic_launcher_round);
        }
        return "";
    }

    public static byte[] getBytesFromIcon(Context context, @DrawableRes int res) {
        Bitmap bitmap;
        BitmapDrawable drawable = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), res, context.getTheme()));
        if(drawable != null) {
            bitmap = drawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    public static boolean checkDatabase() {
        try {
            List<Authentication> authenticationList = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("", true);
            return authenticationList != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
