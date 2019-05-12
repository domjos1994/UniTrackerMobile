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
import android.database.sqlite.SQLiteStatement;

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
            Authentication authentication = new Authentication(this.getString(cursor, "serverName"), this.getString(cursor, "userName"), this.getString(cursor, "password"));
            authentication.setAPIKey(this.getString(cursor, "apiKey"));
            authentication.setTitle(this.getString(cursor, "title"));
            authentication.setDescription(this.getString(cursor, "description"));
            authentication.setCover(cursor.getBlob(cursor.getColumnIndex("cover")));
            authentication.setTracker(Authentication.Tracker.valueOf(this.getString(cursor, "tracker")));
            authentication.setId(cursor.getLong(cursor.getColumnIndex("ID")));
            authentications.add(authentication);
        }
        cursor.close();
        return authentications;
    }

    public void insertOrUpdateAccount(Authentication authentication) {
        if (authentication != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            SQLiteStatement stmt;
            if (authentication.getId() != 0) {
                stmt = db.compileStatement("UPDATE accounts SET title=?, serverName=?, apiKey=?, userName=?, password=?, description=?, cover=?, tracker=? WHERE ID=?");
                stmt.bindLong(9, authentication.getId());
            } else {
                stmt = db.compileStatement("INSERT INTO accounts(title, serverName, apiKey, userName, password, description, cover, tracker) VALUES(?,?,?,?,?,?,?,?)");
            }
            stmt.bindString(1, authentication.getTitle());
            stmt.bindString(2, authentication.getServer());
            stmt.bindString(3, authentication.getAPIKey());
            stmt.bindString(4, authentication.getUserName());
            stmt.bindString(5, authentication.getPassword());
            stmt.bindString(6, authentication.getDescription());
            if (authentication.getCover() != null) {
                stmt.bindBlob(7, authentication.getCover());
            } else {
                stmt.bindNull(7);
            }
            if (authentication.getTracker() != null) {
                stmt.bindString(8, authentication.getTracker().name());
            } else {
                stmt.bindString(8, Authentication.Tracker.Local.name());
            }
            stmt.execute();
        }
    }

    public boolean duplicated(String table, String column, String value, String where) {
        boolean duplicated = false;
        SQLiteDatabase db = this.getReadableDatabase();
        where = where.trim().isEmpty() ? "" : " AND " + where;
        Cursor cursor = db.rawQuery((String.format("SELECT %s FROM %s WHERE %s=?", column, table, column) + where), new String[]{value});
        while (cursor.moveToNext()) {
            duplicated = true;
        }
        cursor.close();
        return duplicated;
    }

    public void delete(String table, String column, Object value) {
        if (value == null) {
            this.getWritableDatabase().execSQL("DELETE FROM " + table);
        } else {
            if (value instanceof Integer) {
                this.getWritableDatabase().execSQL("DELETE FROM " + table + " WHERE " + column + "=" + value.toString() + "");
            } else {
                this.getWritableDatabase().execSQL("DELETE FROM " + table + " WHERE " + column + "='" + value.toString() + "'");
            }
        }
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
