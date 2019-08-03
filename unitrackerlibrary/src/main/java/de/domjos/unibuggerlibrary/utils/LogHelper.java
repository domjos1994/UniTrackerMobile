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

package de.domjos.unibuggerlibrary.utils;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogHelper {
    private File logFile;


    LogHelper(Activity activity) {
        try {
            this.logFile = new File(activity.getFilesDir().getAbsolutePath() + File.separatorChar + "error.log");
            if (!this.logFile.exists()) {
                if (this.logFile.createNewFile()) {
                    Log.v("Log-File", "NewFile");
                }
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage(), ex);
        }
    }

    void logError(Exception ex) {
        try {
            StringBuilder builder = new StringBuilder(ex.getMessage());
            builder.append("\n");
            builder.append(ex.toString());
            for (StackTraceElement element : ex.getStackTrace()) {
                builder.append(String.format("%s:%s#%s(%s)%n", element.getFileName(), element.getClassName(), element.getMethodName(), element.getLineNumber()));
            }

            BufferedWriter buf = new BufferedWriter(new FileWriter(this.logFile, true));
            buf.append(builder.toString());
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.e("Error", e.getMessage(), e);
        }
    }

    public void clearFile() {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(this.logFile, true));
            buf.write("");
            buf.close();
        } catch (IOException e) {
            Log.e("Error", e.getMessage(), e);
        }
    }
}
