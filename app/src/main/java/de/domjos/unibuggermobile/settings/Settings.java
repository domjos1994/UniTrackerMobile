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

package de.domjos.unibuggermobile.settings;

import android.content.Context;
import android.content.SharedPreferences;

import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggermobile.activities.MainActivity;

public class Settings {
    private SharedPreferences preferences;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public Authentication getCurrentAuthentication() {
        long authID = this.preferences.getLong("auth_id", 0);
        if (authID != 0) {
            return MainActivity.globals.getSqLiteGeneral().getAccounts("ID=" + authID).get(0);
        }
        return null;
    }

    public void setCurrentAuthentication(Authentication authentication) {
        SharedPreferences.Editor editor = this.preferences.edit();
        if (authentication != null) {
            editor.putLong("auth_id", authentication.getId());
        } else {
            editor.putLong("auth_id", 0);
        }
        editor.apply();
    }
}
