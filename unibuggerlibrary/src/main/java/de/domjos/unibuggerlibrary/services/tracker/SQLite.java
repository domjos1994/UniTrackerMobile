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

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.utils.Converter;

public final class SQLite extends SQLiteOpenHelper implements IBugService {
    private String init, update;

    public SQLite(Context context, int id) throws Exception {
        super(context, "uniBugger.db", null, id);
        this.init = Converter.convertStreamToString(SQLite.class.getResourceAsStream("/sql/init.sql"));
        this.update = Converter.convertStreamToString(SQLite.class.getResourceAsStream("/sql/init.sql"));
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public List<Project> getProjects() throws Exception {
        return null;
    }

    @Override
    public Project getProject(String id) throws Exception {
        return null;
    }

    @Override
    public String insertOrUpdateProject(Project project) throws Exception {
        return null;
    }

    @Override
    public void deleteProject(String id) throws Exception {

    }

    @Override
    public int getCurrentState() {
        return 200;
    }

    @Override
    public String getCurrentMessage() {
        return "";
    }
}
