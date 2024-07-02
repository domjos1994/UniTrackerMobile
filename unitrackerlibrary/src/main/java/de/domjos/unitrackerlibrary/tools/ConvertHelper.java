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
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.caverock.androidsvg.SVG;

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

import de.domjos.customwidgets.utils.GlobalHelper;
import de.domjos.customwidgets.utils.MessageHelper;

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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, GlobalHelper.getLocale());
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

    public static Date convertStringToDate(String dt, String format, Context context) throws ParseException {
        return de.domjos.customwidgets.utils.ConvertHelper.convertStringToDate(dt, format);
    }

    public static String convertDateToString(Date date, String format) {
        if(date!=null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, GlobalHelper.getLocale());
            return simpleDateFormat.format(date);
        }
        return null;
    }

    public static String convertDateToString(Date date, String format, Context context) {
        return de.domjos.customwidgets.utils.ConvertHelper.convertDateToString(date, format);
    }

    public static Calendar convertStringToCalendar(String dt, Context context) throws ParseException {
        Date date = de.domjos.customwidgets.utils.ConvertHelper.convertStringToDate(dt, context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Time convertStringToTime(Context context, String time, int icon) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return new Time(Objects.requireNonNull(formatter.parse(time)).getTime());
        } catch (Exception ex) {
            MessageHelper.printException(ex, icon, context);
        }
        return null;
    }

    public static Date convertStringTimeToDate(Context context, String time, int icon) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(new Date());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            return formatter.parse(String.format("%s.%s.%s %s", day, month, year, time));
        } catch (Exception ex) {
            MessageHelper.printException(ex, icon, context);
        }
        return null;
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
            return de.domjos.customwidgets.utils.ConvertHelper.convertStreamToByteArray(is);
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

    public static byte[] convertDrawableToByteArray(Context context, int id) {
        Drawable d = de.domjos.customwidgets.utils.ConvertHelper.convertResourcesToDrawable(context, id);
        if(d instanceof VectorDrawable) {
            return ConvertHelper.convertSVGToBytes(context, id);
        }
        if(d instanceof VectorDrawableCompat) {
            return ConvertHelper.convertSVGToBytes(context, id);
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

    private static byte[] convertSVGToBytes(Context context, int id) {
        try {
            SVG svg = SVG.getFromResource(context, id);
            Picture picture = svg.renderToPicture();
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.RGB_565);
            canvas.setBitmap(bitmap);
            svg.renderToPicture().draw(canvas);
            return de.domjos.customwidgets.utils.ConvertHelper.convertBitmapToByteArray(bitmap);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Bitmap convertSVGByteArrayToBitmap(byte[] bytes) {
        try {
            SVG svg = SVG.getFromInputStream(new ByteArrayInputStream(bytes));
            Picture picture = svg.renderToPicture();
            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.RGB_565);
            canvas.setBitmap(bitmap);
            svg.renderToPicture().draw(canvas);
            return bitmap;
        } catch (Exception ex) {
            return null;
        }
    }

    public static Bitmap convertByteArrayToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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
        } catch (Exception ex) {
            MessageHelper.printException(ex, icon, context);
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
