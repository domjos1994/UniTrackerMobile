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

package de.domjos.unitrackerlibrary.tools;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import de.domjos.unitrackerlibrary.R;

/** @noinspection unused*/
public class ConvertHelper {

    /**
     * Convert dp to pixels
     * @param dp number of dp
     * @param context context
     * @return number of pixels
     */
    public static int convertDPToPixels(int dp, Context context) {
        final int scale = Math.round(context.getResources().getDisplayMetrics().density);
        return (int) (dp * scale + 0.5);
    }

    public static byte[] downloadFile(URL url) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Firefox");

        try (InputStream inputStream = conn.getInputStream()) {
            int n;
            byte[] buffer = new byte[1024];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
        byte[] img = output.toByteArray();
        ByteBuffer imageBytes = ByteBuffer.wrap(img);
        return imageBytes.array();
    }

    public static String convertStreamToString(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }

    public static Date convertStringToDate(String dt, String format) throws ParseException {
        if(dt!=null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
            return dt.isEmpty() ? null : simpleDateFormat.parse(dt);
        }
        return null;
    }

    public static String convertDoubleToString(double dbl) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(dbl);
    }

    public static double convertStringToDouble(String dbl) {
        try {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            if(dbl.contains(".")) {
                decimalFormatSymbols.setDecimalSeparator('.');
                decimalFormatSymbols.setGroupingSeparator(',');
                decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
                Number number = decimalFormat.parse(dbl);
                if(number != null) {
                    return number.doubleValue();
                }
            } else {
                decimalFormatSymbols.setDecimalSeparator(',');
                decimalFormatSymbols.setGroupingSeparator('.');
                decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
                Number number = decimalFormat.parse(dbl);
                if(number != null) {
                    return number.doubleValue();
                }
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    public static Date convertStringToDate(String dt, Context context) throws ParseException {
        String format = context.getString(R.string.sys_date_format);
        return convertStringToDate(dt, format);
    }

    public static String convertDateToString(Date date, String format) {
        if(date!=null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
            return simpleDateFormat.format(date);
        }
        return null;
    }

    public static String convertDateToString(Date date, Context context) {
        String format = context.getString(R.string.sys_date_format);
        return convertDateToString(date, format);
    }

    public static Calendar convertStringToCalendar(String dt, Context context) throws ParseException {
        Date date = convertStringToDate(dt, context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Time convertStringToTime(Context context, String time, int icon) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return new Time(Objects.requireNonNull(formatter.parse(time)).getTime());
    }

    public static Date convertStringTimeToDate(Context context, String time, int icon) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return formatter.parse(String.format("%s.%s.%s %s", day, month, year, time));
    }

    public static Drawable convertStringToImage(String url) {
        try(InputStream is = (InputStream) new URL(url).getContent()) {
            return Drawable.createFromStream(is, "src name");
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] convertStringToByteArray(String url) {
        try (InputStream is = (InputStream) new URL(url).getContent()) {
            return convertStreamToByteArray(is);
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] convertDrawableToByteArray(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] convertDrawableToByteArray(Context context, int id) throws Exception {
        Drawable d = convertResourcesToDrawable(context, id);
        if(d instanceof VectorDrawable) {
            return null;
        }
        if(d instanceof VectorDrawableCompat) {
            return null;
        }

        BitmapDrawable bitmapDrawable = ((BitmapDrawable)d);
        if(bitmapDrawable!=null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    public static Bitmap convertByteArrayToBitmap(byte[] bytes) {
        if(bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    public static Drawable convertByteArrayToDrawable(byte[] bytes) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Drawable drawable = BitmapDrawable.createFromStream(is, "icon");
        is.close();
        return drawable;
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        stream.close();
        return byteArray;
    }

    public static byte[] convertStreamToByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        stream.close();
        return buffer.toByteArray();
    }

    public static void convertByteArrayToFile(byte[] content, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        }
    }


    public static Drawable convertResourcesToDrawable(Context context, int resource_id) {
        return ContextCompat.getDrawable(context, resource_id);
    }

    public static Drawable convertResourcesToDrawable(Context context, int resource_id, @ColorRes int color) {
        Drawable drawable = ContextCompat.getDrawable(context, resource_id);
        if(drawable != null) {
            drawable.setTint(ContextCompat.getColor(context, color));
        }
        return drawable;
    }

    public static String convertURIToStringPath(Context context, Uri contentUri, int icon) {
        Cursor cursor = null;
        try {
            String[] projection = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  projection, null, null, null);
            int column_index = 0;
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            }
            if (cursor != null) {
                cursor.moveToFirst();
            }
            if (cursor != null) {
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static Bitmap convertUriToBitmap(Context context, Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
    }
}
