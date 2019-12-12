/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

package de.domjos.unitrackermobile.helper;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.domjos.customwidgets.utils.Converter;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.settings.Settings;

public class DateConverter {
    public static Date convertStringToDate(String dt, Context context) throws Exception {
        Settings settings = MainActivity.GLOBALS.getSettings(context);
        try {
            return Converter.convertStringToDate(dt, settings.getDateFormat() + " " + settings.getTimeFormat());
        } catch (Exception ex) {
            return Converter.convertStringToDate(dt, settings.getDateFormat());
        }
    }

    public static String convertLongToString(Long dt, Context context) {
        Date date = new Date();
        date.setTime(dt);
        return convertDateTimeToString(date, context);
    }

    public static String convertDateToString(Date dt, Context context) {
        Settings settings = MainActivity.GLOBALS.getSettings(context);
        SimpleDateFormat sdf = new SimpleDateFormat(settings.getDateFormat(), Locale.getDefault());
        return sdf.format(dt);
    }

    public static String convertDateTimeToString(Date dt, Context context) {
        Settings settings = MainActivity.GLOBALS.getSettings(context);
        SimpleDateFormat sdf = new SimpleDateFormat(settings.getDateFormat() + " " + settings.getTimeFormat(), Locale.getDefault());
        return sdf.format(dt);
    }
}
