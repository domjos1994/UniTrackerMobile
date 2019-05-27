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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.Attachment;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.permissions.SQLitePermissions;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggerlibrary.utils.Utils;

public final class SQLite extends SQLiteOpenHelper implements IBugService<Long> {
    private Context context;
    private int id;
    private Authentication authentication;

    public SQLite(Context context, int id, Authentication authentication) {
        super(context, "uniBugger.db", null, id);
        this.id = id;
        this.context = context;
        this.authentication = authentication;
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
    public boolean testConnection() {
        return true;
    }

    @Override
    public String getTrackerVersion() {
        return String.valueOf(this.id);
    }

    @Override
    public List<Project<Long>> getProjects() {
        List<Project<Long>> projects = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM projects WHERE authentication=?", new String[]{String.valueOf(this.authentication.getTitle())});
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
        Cursor cursor = db.rawQuery("SELECT * FROM projects WHERE id=? AND authentication=?", new String[]{String.valueOf(id), this.authentication.getTitle()});
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
            sqLiteStatement = db.compileStatement("INSERT INTO projects(title,shortTitle,privateProject,enabledProject,website,statusText,statusID,iconUrl,defaultVersion,description, authentication) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
        } else {
            sqLiteStatement = db.compileStatement("UPDATE projects SET title=?,shortTitle=?,privateProject=?,enabledProject=?,website=?,statusText=?,statusID=?,iconUrl=?,defaultVersion=?,description=?, authentication=? WHERE id=?");
            sqLiteStatement.bindLong(12, project.getId());
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
        sqLiteStatement.bindString(11, this.authentication.getTitle());

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
    public List<Issue<Long>> getIssues(Long pid) {
        List<Issue<Long>> issues = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM issues WHERE project=?", new String[]{String.valueOf(pid)});
        while (cursor.moveToNext()) {
            issues.add(this.getIssue((long) this.getInt(cursor, "id")));
        }
        cursor.close();
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id) {
        Issue<Long> issue = new Issue<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM issues WHERE id=?", new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            issue.setId(this.getLong(cursor, "id"));
            issue.setTitle(this.getString(cursor, "title"));
            issue.setCategory(this.getString(cursor, "category"));
            issue.setState(this.getInt(cursor, "state_id"), "");
            issue.setPriority(this.getInt(cursor, "priority_id"), "");
            issue.setSeverity(this.getInt(cursor, "severity_id"), "");
            issue.setStatus(this.getInt(cursor, "status_id"), "");
            issue.setReproducibility(this.getInt(cursor, "reproducibility_id"), "");
            issue.setResolution(this.getInt(cursor, "resolution_id"), "");
            issue.setVersion(this.getString(cursor, "version"));
            issue.setFixedInVersion(this.getString(cursor, "fixedInVersion"));
            issue.setTargetVersion(this.getString(cursor, "targetVersion"));
            issue.setTags(this.getString(cursor, "tags"));

            long dueDate = this.getLong(cursor, "dueDate");
            if (dueDate != 0) {
                Date dt = new Date();
                dt.setTime(dueDate);
                issue.setDueDate(dt);
            }
            long lastUpdated = this.getLong(cursor, "lastUpdated");
            if (lastUpdated != 0) {
                Date dt = new Date();
                dt.setTime(lastUpdated);
                issue.setLastUpdated(dt);
            }
            long submitDate = this.getLong(cursor, "submitDate");
            if (submitDate != 0) {
                Date dt = new Date();
                dt.setTime(submitDate);
                issue.setSubmitDate(dt);
            }
            issue.setDescription(this.getString(cursor, "description"));
            issue.setStepsToReproduce(this.getString(cursor, "steps_to_reproduce"));
            issue.setAdditionalInformation(this.getString(cursor, "additional_information"));
        }
        cursor.close();

        cursor = db.rawQuery("SELECT * FROM notes WHERE issue=?", new String[]{String.valueOf(issue.getId())});
        while (cursor.moveToNext()) {
            Note<Long> note = new Note<>();
            note.setId(this.getLong(cursor, "id"));
            note.setState(this.getInt(cursor, "state_id"), "");
            note.setTitle(this.getString(cursor, "title"));
            note.setDescription(this.getString(cursor, "description"));
            long lastUpdated = this.getLong(cursor, "lustUpdated");
            if (lastUpdated != 0) {
                Date dt = new Date();
                dt.setTime(lastUpdated);
                note.setLastUpdated(dt);
            }
            long submitDate = this.getLong(cursor, "submitDate");
            if (submitDate != 0) {
                Date dt = new Date();
                dt.setTime(submitDate);
                note.setSubmitDate(dt);
            }
            issue.getNotes().add(note);
        }
        cursor.close();

        cursor = db.rawQuery("SELECT * FROM attachments WHERE issue=?", new String[]{String.valueOf(issue.getId())});
        while (cursor.moveToNext()) {
            Attachment<Long> attachment = new Attachment<>();
            attachment.setId(this.getLong(cursor, "id"));
            attachment.setTitle(this.getString(cursor, "title"));
            attachment.setContent(cursor.getBlob(cursor.getColumnIndex("content")));
            issue.getAttachments().add(attachment);
        }
        cursor.close();

        cursor = db.rawQuery("SELECT fieldResult.fieldValue, customFields.* FROM fieldResult INNER JOIN customFields ON fieldResult.field=customFields.id WHERE issue=?", new String[]{String.valueOf(issue.getId())});
        while (cursor.moveToNext()) {
            CustomField<Long> customField = new CustomField<>();
            customField.setId(this.getLong(cursor, "customFields.id"));
            customField.setTitle(this.getString(cursor, "customFields.title"));
            customField.setType(this.getInt(cursor, "customFields.type"));
            customField.setPossibleValues(this.getString(cursor, "customFields.possibleValues"));
            customField.setDefaultValue(this.getString(cursor, "customFields.defaultValue"));
            customField.setMinLength(this.getInt(cursor, "customFields.minLength"));
            customField.setMaxLength(this.getInt(cursor, "customFields.maxLength"));
            customField.setDescription(this.getString(cursor, "customFields.description"));
            issue.getCustomFields().put(customField, this.getString(cursor, "fieldResult.fieldValue"));
        }
        cursor.close();

        return issue;
    }

    @Override
    public Long insertOrUpdateIssue(Long pid, Issue<Long> issue) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
        if (issue.getId() == null) {
            sqLiteStatement = db.compileStatement(
                    "INSERT INTO issues(title,category,state_id,priority_id,severity_id," +
                            "status_id,reproducibility_id,resolution_id,version,fixedInVersion," +
                            "targetVersion,tags,dueDate,lastUpdated,submitDate,description," +
                            "steps_to_reproduce,additional_information,project) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );
        } else {
            sqLiteStatement = db.compileStatement(
                    "UPDATE issues SET title=?,category=?,state_id=?,priority_id=?,severity_id=?," +
                            "status_id=?,reproducibility_id=?,resolution_id=?,version=?,fixedInVersion=?," +
                            "targetVersion=?,tags=?,dueDate=?,lastUpdated=?,submitDate=?,description=?," +
                            "steps_to_reproduce=?,additional_information=?,project=? WHERE id=?"
            );
            sqLiteStatement.bindLong(20, Long.parseLong(String.valueOf(issue.getId())));
        }
        sqLiteStatement.bindString(1, issue.getTitle());
        sqLiteStatement.bindString(2, issue.getCategory());
        sqLiteStatement.bindLong(3, issue.getState().getKey());
        sqLiteStatement.bindLong(4, issue.getPriority().getKey());
        sqLiteStatement.bindLong(5, issue.getSeverity().getKey());
        sqLiteStatement.bindLong(6, issue.getStatus().getKey());
        sqLiteStatement.bindLong(7, issue.getReproducibility().getKey());
        sqLiteStatement.bindLong(8, issue.getResolution().getKey());
        sqLiteStatement.bindString(9, issue.getVersion());
        sqLiteStatement.bindString(10, issue.getFixedInVersion());
        sqLiteStatement.bindString(11, issue.getTargetVersion());
        sqLiteStatement.bindString(12, issue.getTags());

        if (issue.getDueDate() != null) {
            sqLiteStatement.bindLong(13, issue.getDueDate().getTime());
        } else {
            sqLiteStatement.bindNull(13);
        }
        issue.setLastUpdated(new Date());
        sqLiteStatement.bindLong(14, issue.getLastUpdated().getTime());
        if (issue.getSubmitDate() != null) {
            sqLiteStatement.bindLong(15, issue.getSubmitDate().getTime());
        } else {
            sqLiteStatement.bindNull(15);
        }
        sqLiteStatement.bindString(16, issue.getDescription());
        sqLiteStatement.bindString(17, issue.getStepsToReproduce());
        sqLiteStatement.bindString(18, issue.getAdditionalInformation());
        sqLiteStatement.bindLong(19, pid);

        if (issue.getId() != null) {
            sqLiteStatement.execute();
        } else {
            issue.setId(sqLiteStatement.executeInsert());
        }
        sqLiteStatement.close();

        if (!issue.getNotes().isEmpty()) {
            for (Note<Long> note : issue.getNotes()) {
                if (note.getId() != null) {
                    sqLiteStatement = db.compileStatement(
                            "UPDATE notes SET title=?, state_id=?, description=?, lustUpdated=?," +
                                    "submitDate=?,issue=? WHERE id=?"
                    );
                    sqLiteStatement.bindLong(7, note.getId());
                } else {
                    sqLiteStatement = db.compileStatement(
                            "INSERT INTO notes(title, state_id, description, lustUpdated, " +
                                    "submitDate, issue) VALUES(?,?,?,?,?,?)"
                    );
                }
                sqLiteStatement.bindString(1, note.getTitle());
                sqLiteStatement.bindLong(2, note.getState().getKey());
                sqLiteStatement.bindString(3, note.getDescription());
                note.setLastUpdated(new Date());
                sqLiteStatement.bindLong(4, note.getLastUpdated().getTime());
                if (note.getSubmitDate() != null) {
                    sqLiteStatement.bindLong(5, note.getSubmitDate().getTime());
                } else {
                    sqLiteStatement.bindNull(5);
                }
                sqLiteStatement.bindLong(6, Long.parseLong(String.valueOf(issue.getId())));
                sqLiteStatement.execute();
                sqLiteStatement.close();
            }
        }

        if (!issue.getAttachments().isEmpty()) {
            for (Attachment<Long> attachment : issue.getAttachments()) {
                if (attachment.getId() != null) {
                    sqLiteStatement = db.compileStatement(
                            "UPDATE attachments SET title=?, download_url=?, content=?, issue=? WHERE id=?"
                    );
                    sqLiteStatement.bindLong(5, attachment.getId());
                } else {
                    sqLiteStatement = db.compileStatement(
                            "INSERT INTO attachments(title, download_url, content, issue) VALUES(?,?,?,?)"
                    );
                }
                sqLiteStatement.bindString(1, attachment.getTitle());
                sqLiteStatement.bindString(2, attachment.getDownloadUrl());
                if (attachment.getContent() != null) {
                    sqLiteStatement.bindBlob(3, attachment.getContent());
                } else {
                    sqLiteStatement.bindNull(3);
                }
                sqLiteStatement.bindLong(4, Long.parseLong(String.valueOf(issue.getId())));
                sqLiteStatement.execute();
                sqLiteStatement.close();
            }
        }

        if (!issue.getCustomFields().entrySet().isEmpty()) {
            db.execSQL("DELETE FROM fieldResult WHERE issue=" + issue.getId());
            for (Map.Entry<CustomField<Long>, String> entry : issue.getCustomFields().entrySet()) {
                sqLiteStatement = db.compileStatement("INSERT INTO fieldResult(fieldValue, field, issue) VALUES(?,?,?)");
                sqLiteStatement.bindString(1, entry.getValue());
                sqLiteStatement.bindLong(2, entry.getKey().getId());
                sqLiteStatement.bindLong(3, Long.parseLong(String.valueOf(issue.getId())));
                sqLiteStatement.executeInsert();
                sqLiteStatement.close();
            }
        }

        return null;
    }

    @Override
    public void deleteIssue(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM fieldResult WHERE issue=" + id);
        db.execSQL("DELETE FROM notes WHERE issue=" + id);
        db.execSQL("DELETE FROM attachments WHERE issue=" + id);
        db.execSQL("DELETE FROM issues WHERE id=" + id);
    }

    @Override
    public List<String> getCategories(Long pid) {
        List<String> categories = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM issues WHERE project=?", new String[]{String.valueOf(pid)});
        while (cursor.moveToNext()) {
            categories.add(this.getString(cursor, "category"));
        }
        cursor.close();
        return categories;
    }

    @Override
    public List<User<Long>> getUsers(Long pid) {
        return new LinkedList<>();
    }

    @Override
    public User<Long> getUser(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateUser(User<Long> user) throws Exception {
        return null;
    }

    @Override
    public void deleteUser(Long id) throws Exception {

    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long pid) throws Exception {
        return null;
    }

    @Override
    public CustomField<Long> getCustomField(Long id) throws Exception {
        return null;
    }

    @Override
    public Long insertOrUpdateCustomField(CustomField<Long> user) throws Exception {
        return null;
    }

    @Override
    public void deleteCustomField(Long id) throws Exception {

    }

    @Override
    public List<Tag<Long>> getTags() {
        List<Tag<Long>> tags = new LinkedList<>();
        List<String> strTags = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT tags FROM issues", null);
        while (cursor.moveToNext()) {
            String tagList = this.getString(cursor, "category");
            if (tagList != null) {
                if (!tagList.trim().isEmpty()) {
                    if (tagList.contains(",")) {
                        for (String strTag : tagList.split(",")) {
                            if (!strTag.trim().isEmpty()) {
                                if (!strTags.contains(strTag)) {
                                    strTags.add(strTag);
                                }
                            }
                        }
                    }
                    if (tagList.contains(";")) {
                        for (String strTag : tagList.split(";")) {
                            if (!strTag.trim().isEmpty()) {
                                if (!strTags.contains(strTag)) {
                                    strTags.add(strTag);
                                }
                            }
                        }
                    }
                }
            }
        }

        long i = 1;
        for (String strTag : strTags) {
            Tag<Long> tag = new Tag<>();
            tag.setId(i);
            tag.setTitle(strTag);
            tags.add(tag);
            i++;
        }
        cursor.close();
        return tags;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new SQLitePermissions();
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
