/*
 * Copyright (C) 2017-2020  Dominic Joas
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 */

package de.domjos.unitrackerlibrary.tools;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/** @noinspection unused, SameParameterValue */
public class LogHelper {
    private final static String ERROR = "Error";
    private File logFile;


    public LogHelper(Activity activity) {
        try {
            this.logFile = new File(activity.getFilesDir().getAbsolutePath() + File.separatorChar + "error.log");
            if (!this.logFile.exists() && this.logFile.createNewFile()) {
                Log.v("Log-File", "NewFile");
            }
        } catch (Exception ex) {
            Log.e(LogHelper.ERROR, ex.getMessage(), ex);
        }
    }

    void logError(Exception ex) {
        try {
            StringBuilder builder = new StringBuilder(Objects.requireNonNull(ex.getMessage()));
            builder.append("\n");
            builder.append(ex);
            for (StackTraceElement element : ex.getStackTrace()) {
                builder.append(String.format("%s:%s#%s(%s)%n", element.getFileName(), element.getClassName(), element.getMethodName(), element.getLineNumber()));
            }

            try (BufferedWriter buf = new BufferedWriter(new FileWriter(this.logFile, true))) {
                buf.append(builder.toString());
                buf.newLine();
            }
        } catch (IOException e) {
            Log.e(LogHelper.ERROR, e.getMessage(), e);
        }
    }

    void logMessage(String message) {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(this.logFile, true))) {
            buf.append(message);
            buf.newLine();
        } catch (IOException e) {
            Log.e(LogHelper.ERROR, e.getMessage(), e);
        }
    }

    public void clearFile() {
        try {
            if (this.logFile.delete() && this.logFile.createNewFile()) {
                this.logMessage("Successfully create a new Log-File!");
            }
        } catch (IOException e) {
            Log.e(LogHelper.ERROR, e.getMessage(), e);
        }
    }

    public void export(String folder) throws IOException {
        File exportFile = new File(folder + File.separatorChar + this.logFile.getName());
        this.copy(this.logFile, exportFile);
    }

    private void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
