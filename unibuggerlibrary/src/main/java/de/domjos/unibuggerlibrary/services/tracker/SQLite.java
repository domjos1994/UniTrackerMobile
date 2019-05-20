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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggerlibrary.utils.Utils;

public final class SQLite extends SQLiteOpenHelper implements IBugService<Long> {
    private Context context;
    private int id;

    public SQLite(Context context, int id) {
        super(context, "uniBugger.db", null, id);
        this.id = id;
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
    public String getTrackerVersion() {
        return String.valueOf(this.id);
    }

    @Override
    public List<Project<Long>> getProjects() {
        List<Project<Long>> projects = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM projects", null);
        while (cursor.moveToNext()) {
            Project<Long> project = this.getProject(this.getLong(cursor, "id"));
            if (project != null) {
                Cursor subProjects = db.rawQuery("SELECT childProject FROM subProjects WHERE parentProject=?", new String[]{String.valueOf(project.getId())});
                while (subProjects.moveToNext()) {
                    project.getSubProjects().add(this.getProject(this.getLong(cursor, "childProject")));
                }
                subProjects.close();
            }
            projects.add(project);
        }
        cursor.close();
        return projects;
    }

    @Override
    public Project<Long> getProject(Long id) {
        Project<Long> project = new Project<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM projects WHERE id=?", new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            project.setId(this.getLong(cursor, "id"));
            project.setTitle(this.getString(cursor, "title"));
            project.setAlias(this.getString(cursor, "shortTitle"));
            project.setPrivateProject(this.getBoolean(cursor, "privateProject"));
            project.setEnabled(this.getBoolean(cursor, "enabledProject"));
            project.setWebsite(this.getString(cursor, "website"));
            project.setStatus(this.getString(cursor, "statusText"), this.getInt(cursor, "statusID"));
            project.setIconUrl(this.getString(cursor, "iconUrl"));
            project.setDefaultVersion(this.getString(cursor, "defaultVersion"));
            project.setDescription(this.getString(cursor, "description"));
        }
        cursor.close();
        return project;
    }

    @Override
    public Long insertOrUpdateProject(Project<Long> project) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
        if (project.getId() == null) {
            sqLiteStatement = db.compileStatement("INSERT INTO projects(title,shortTitle,privateProject,enabledProject,website,statusText,statusID,iconUrl,defaultVersion,description) VALUES(?,?,?,?,?,?,?,?,?,?)");
        } else {
            sqLiteStatement = db.compileStatement("UPDATE projects SET title=?,shortTitle=?,privateProject=?,enabledProject=?,website=?,statusText=?,statusID=?,iconUrl=?,defaultVersion=?,description=? WHERE id=?");
            sqLiteStatement.bindLong(11, project.getId());
        }
        sqLiteStatement.bindString(1, project.getTitle());
        sqLiteStatement.bindString(2, project.getAlias());
        sqLiteStatement.bindLong(3, project.isPrivateProject() ? 1 : 0);
        sqLiteStatement.bindLong(4, project.isEnabled() ? 1 : 0);
        sqLiteStatement.bindString(5, project.getWebsite());
        sqLiteStatement.bindString(6, project.getStatus());
        sqLiteStatement.bindLong(7, project.getStatusID());
        sqLiteStatement.bindString(8, project.getIconUrl());
        sqLiteStatement.bindString(9, project.getDefaultVersion());
        sqLiteStatement.bindString(10, project.getDescription());

        if (project.getId() == null) {
            project.setId(sqLiteStatement.executeInsert());
        } else {
            sqLiteStatement.execute();
        }
        sqLiteStatement.close();

        db.execSQL("DELETE FROM subProjects WHERE parentProject=" + project.getId());
        for (Project<Long> subProject : project.getSubProjects()) {
            if (subProject.getId() != null) {
                sqLiteStatement = db.compileStatement("INSERT INTO subProjects(parentProject, childProject) VALUES(?,?)");
                sqLiteStatement.bindLong(1, project.getId());
                sqLiteStatement.bindLong(2, subProject.getId());
                sqLiteStatement.execute();
                sqLiteStatement.close();
            }
        }

        return project.getId();
    }

    @Override
    public void deleteProject(Long id) {
        this.getWritableDatabase().execSQL("DELETE FROM projects WHERE id=" + id);
    }

    @Override
    public List<Version<Long>> getVersions(Long pid, String filter) {
        List<Version<Long>> versions = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM versions WHERE project=?", new String[]{String.valueOf(pid)});
        while (cursor.moveToNext()) {
            Version<Long> version = new Version<>();
            version.setTitle(this.getString(cursor, "title"));
            version.setReleasedVersion(this.getBoolean(cursor, "releasedVersion"));
            version.setDeprecatedVersion(this.getBoolean(cursor, "deprecatedVersion"));
            version.setReleasedVersionAt(this.getLong(cursor, "releasedVersionAt"));
            version.setDescription(this.getString(cursor, "description"));
            versions.add(version);
        }
        db.close();
        return versions;
    }

    @Override
    public Long insertOrUpdateVersion(Long pid, Version<Long> version) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
        if (version.getId() == null) {
            sqLiteStatement = db.compileStatement("INSERT INTO versions(title,releasedVersion,deprecatedVersion,releasedVersionAt,description, project) VALUES(?,?,?,?,?,?)");
        } else {
            sqLiteStatement = db.compileStatement("UPDATE versions SET title=?,releasedVersion=?,deprecatedVersion=?,releasedVersionAt=?,description=?,project=? WHERE id=?");
            sqLiteStatement.bindLong(7, version.getId());
        }
        sqLiteStatement.bindString(1, version.getTitle());
        sqLiteStatement.bindLong(2, version.isReleasedVersion() ? 1 : 0);
        sqLiteStatement.bindLong(3, version.isDeprecatedVersion() ? 1 : 0);
        sqLiteStatement.bindLong(4, version.getReleasedVersionAt());
        sqLiteStatement.bindString(5, version.getDescription());
        sqLiteStatement.bindLong(6, pid);

        if (version.getId() == null) {
            version.setId(sqLiteStatement.executeInsert());
        } else {
            sqLiteStatement.execute();
        }
        sqLiteStatement.close();

        return version.getId();
    }

    @Override
    public void deleteVersion(Long id) {
        this.getWritableDatabase().execSQL("DELETE FROM versions WHERE id=" + id);
    }

    @Override
    public int getCurrentState() {
        return 200;
    }

    @Override
    public String getCurrentMessage() {
        return "";
    }

    @Override
    public List<Issue<Long>> getIssues(Long pid) throws Exception {
        return null;
    }

    @Override
    public Issue<Long> getIssue(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateIssue(Long pid, Issue<Long> issue) throws Exception {
        return null;
    }

    @Override
    public void deleteIssue(Long id) throws Exception {

    }

    @Override
    public List<String> getCategories(Long pid) throws Exception {
        return null;
    }


    private void initDatabase(SQLiteDatabase db) {
        try {
            String queries = Utils.readStringFromRaw(R.raw.init_tracker, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }

    private void updateDatabase(SQLiteDatabase db) {
        try {
            String queries = Utils.readStringFromRaw(R.raw.update_tracker, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }

    private String getString(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1) {
            return "";
        } else {
            return cursor.getString(cursor.getColumnIndex(key));
        }
    }

    private boolean getBoolean(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1) {
            return false;
        } else {
            return cursor.getInt(cursor.getColumnIndex(key)) == 1;
        }
    }

    private int getInt(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1) {
            return 0;
        } else {
            return cursor.getInt(cursor.getColumnIndex(key));
        }
    }

    private long getLong(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1) {
            return 0L;
        } else {
            return cursor.getLong(cursor.getColumnIndex(key));
        }
    }
}
