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

package de.domjos.unibuggerlibrary.services.tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggerlibrary.utils.Utils;

public final class SQLite extends SQLiteOpenHelper implements IBugService<Long> {
    private Context context;

    public SQLite(Context context, int id) {
        super(context, "uniBugger.db", null, id);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.initDatabase(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        this.initDatabase(sqLiteDatabase);
        this.updateDatabase(sqLiteDatabase);
    }

    @Override
    public String getTrackerVersion() throws Exception {
        return null;
    }

    @Override
    public List<Project<Long>> getProjects() throws Exception {
        return null;
    }

    @Override
    public Project<Long> getProject(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) throws Exception {
        return null;
    }

    @Override
    public void deleteProject(Long id) throws Exception {

    }

    @Override
    public List<Version<Long>> getVersions(Long pid, String filter) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) throws Exception {
        return null;
    }

    @Override
    public void deleteVersion(Long id) throws Exception {

    }

    @Override
    public int getCurrentState() {
        return 200;
    }

    @Override
    public String getCurrentMessage() {
        return "";
    }


    private void initDatabase(SQLiteDatabase db) {
        try {
            String queries = Utils.readStringFromRaw(R.raw.init, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }

    private void updateDatabase(SQLiteDatabase db) {
        try {
            String queries = Utils.readStringFromRaw(R.raw.update, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }
}
