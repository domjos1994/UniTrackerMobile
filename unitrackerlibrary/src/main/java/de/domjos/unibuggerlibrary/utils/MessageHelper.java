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

package de.domjos.unibuggerlibrary.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat.Builder;

import java.util.Random;

import de.domjos.unibuggerlibrary.R;

public class MessageHelper {
    private final static String id = "UniBuggerChannel";

    public static void printException(Exception ex, Context context) {
        try {
            StringBuilder builder = new StringBuilder(ex.getMessage()).append("\n");
            for (StackTraceElement element : ex.getStackTrace()) {
                builder.append(String.format("%s.%s#%s(%s)%n", element.getFileName(), element.getClassName(), element.getMethodName(), element.getLineNumber()));
            }
            MessageHelper.printMessage(ex.toString(), context);
        } catch (Exception ignored) {
        }
        Log.e("Exception", "Error", ex);
    }

    public static void printMessage(String message, Context context) {
        if (context instanceof Activity) {
            MessageHelper.printMessage(message, (Activity) context);
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private static void printMessage(String message, Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, activity.findViewById(R.id.custom_toast_container));
        TextView text = layout.findViewById(R.id.text);
        text.setText(message);
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int startProgressNotification(Activity activity, String title, String content, int icon) {
        MessageHelper.createChannel(activity.getApplicationContext());
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        Builder builder = new Builder(activity, id);
        Notification notification = builder.setContentTitle(title).setContentText(content).setSmallIcon(icon).setProgress(0, 0, true).build();
        Random random = new Random();
        int id = random.nextInt();
        manager.notify(id, notification);
        return id;
    }

    public static void stopProgressNotification(Activity activity, int id) {
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
