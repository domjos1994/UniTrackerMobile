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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.caverock.androidsvg.SVG;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Converter {

    public static String convertStreamToString(InputStream stream) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }

    public static Date convertStringToDate(String dt, String format) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.GERMAN);
        return simpleDateFormat.parse(dt);
    }

    public static Drawable convertStringToImage(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "src name");
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] convertStringToByteArray(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Converter.convertStreamToByteArray(is);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] convertDrawableToByteArray(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] convertStreamToByteArray(InputStream stream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        stream.close();
        return buffer.toByteArray();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static byte[] convertSVGToByteArray(Context context, String url) throws Exception {
        byte[] imageAsBytes = Converter.convertStringToByteArray(url);
        String svgAsString = new String(imageAsBytes, StandardCharsets.UTF_8);

        try {
            SVG svg = SVG.getFromString(svgAsString);
            float svgWidth = (svg.getDocumentWidth() != -1) ? svg.getDocumentWidth() : 500f;
            float svgHeight = (svg.getDocumentHeight() != -1) ? svg.getDocumentHeight() : 500f;
            Bitmap newBM = Bitmap.createBitmap((int) Math.ceil(svgWidth), (int) Math.ceil(svgHeight), Bitmap.Config.ARGB_8888);
            Canvas bmcanvas = new Canvas(newBM);
            bmcanvas.drawRGB(255, 255, 255);
            svg.renderToCanvas(bmcanvas);

            return Converter.convertDrawableToByteArray(new BitmapDrawable(context.getResources(), newBM));
        } catch (Exception ex) {
            return null;
        }
    }
}
