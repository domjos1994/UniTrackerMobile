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

package de.domjos.unibuggermobile.settings;

import android.content.Context;

import de.domjos.unibuggermobile.helper.SQLiteGeneral;

public class Globals {
    public final static String FILE_PROVIDER = "de.domjos.unibuggermobile.provider.FileProvider";

    private SQLiteGeneral sqLiteGeneral;
    private String password;

    public Globals() {
        this.sqLiteGeneral = null;
        this.password = "";
    }

    public SQLiteGeneral getSqLiteGeneral() {
        return this.sqLiteGeneral;
    }

    public void setSqLiteGeneral(SQLiteGeneral sqLiteGeneral) {
        this.sqLiteGeneral = sqLiteGeneral;
    }

    public Settings getSettings(Context context) {
        return new Settings(context);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
