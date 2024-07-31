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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import de.domjos.unibuggermobile.provider.FileProvider;

public class IntentHelper {

    public static void saveAndOpenFile(byte[] array, Activity activity) throws Exception {
        Uri uri = FileProvider.getUriForFile(activity, "de.domjos.unitrackermobile.provider.FileProvider", IntentHelper.saveFile(array, activity));
        String mimeType = activity.getContentResolver().getType(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(intent);
    }

    private static File saveFile(byte[] bytes, Activity activity) throws Exception {
        File tmpFile = File.createTempFile(
            String.valueOf(UUID.randomUUID().getLeastSignificantBits()),
            String.valueOf(UUID.randomUUID().getMostSignificantBits()),
            activity.getCacheDir()
        );

        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
        return tmpFile;
    }
}
