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

package de.domjos.unibuggermobile.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;

public class SQLiteGeneral extends SQLiteOpenHelper {
    private Context context;

    public SQLiteGeneral(Context context) throws Exception {
        super(context, "general.db", null, Helper.getVersionCode(context));
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.initDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.initDatabase(db);
        this.updateDatabase(db);
    }

    public List<Authentication> getAccounts(String where) {
        List<Authentication> authentications = new LinkedList<>();
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM accounts" + (!where.trim().equals("") ? " WHERE " + where : ""), null);
        while (cursor.moveToNext()) {
            Authentication authentication = new Authentication(this.getString(cursor, "server"), this.getString(cursor, "userName"), this.getString(cursor, "password"));
            authentication.setAPIKey(this.getString(cursor, "api_key"));
            authentication.setTitle(this.getString(cursor, "title"));
            authentication.setDescription(this.getString(cursor, "description"));
            authentications.add(authentication);
        }
        cursor.close();
        return authentications;
    }

    private String getString(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1) {
            return "";
        } else {
            return cursor.getString(cursor.getColumnIndex(key));
        }
    }

    private void initDatabase(SQLiteDatabase db) {
        try {
            String queries = Helper.readStringFromRaw(R.raw.init, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }

    private void updateDatabase(SQLiteDatabase db) {
        try {
            String queries = Helper.readStringFromRaw(R.raw.update, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }
}
