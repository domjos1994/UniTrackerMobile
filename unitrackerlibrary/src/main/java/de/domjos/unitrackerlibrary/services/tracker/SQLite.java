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

package de.domjos.unitrackerlibrary.services.tracker;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.*;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.permissions.SQLitePermissions;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

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
        if (project.getWebsite() != null) {
            sqLiteStatement.bindString(5, project.getWebsite());
        } else {
            sqLiteStatement.bindNull(5);
        }
        sqLiteStatement.bindString(6, project.getStatus());
        sqLiteStatement.bindLong(7, project.getStatusID());
        sqLiteStatement.bindString(8, project.getIconUrl());
        if (project.getDefaultVersion() != null) {
            sqLiteStatement.bindString(9, project.getDefaultVersion());
        } else {
            sqLiteStatement.bindNull(9);
        }
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
    public List<Version<Long>> getVersions(String filter, Long project_id) {
        List<Version<Long>> versions = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM versions WHERE project=?", new String[]{String.valueOf(project_id)});
        while (cursor.moveToNext()) {
            Version<Long> version = new Version<>();
            version.setId(this.getLong(cursor, "id"));
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
    public void insertOrUpdateVersion(Version<Long> version, Long project_id) {
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
        sqLiteStatement.bindLong(6, project_id);

        if (version.getId() == null) {
            version.setId(sqLiteStatement.executeInsert());
        } else {
            sqLiteStatement.execute();
        }
        sqLiteStatement.close();
    }

    @Override
    public void deleteVersion(Long id, Long project_id) {
        this.getWritableDatabase().execSQL("DELETE FROM versions WHERE id=" + id);
    }

    @Override
    public long getMaximumNumberOfIssues(Long project_id, IssueFilter filter) {
        if(filter==null) {
            filter = IssueFilter.all;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = " AND status_id>=80";
            } else {
                filterQuery = " AND status_id<80";
            }
        }

        int number = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(id) FROM issues WHERE project=?" + filterQuery, new String[]{String.valueOf(project_id)});
        while (cursor.moveToNext()) {
            number = cursor.getInt(0);
        }
        cursor.close();

        return number;
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id) {
        return this.getIssues(project_id, 1, -1, IssueFilter.all);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, IssueFilter filter) {
        return this.getIssues(project_id, 1, -1, filter);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems) {
        return this.getIssues(project_id, page, numberOfItems, IssueFilter.all);
    }

    @Override
    public List<Issue<Long>> getIssues(Long project_id, int page, int numberOfItems, IssueFilter filter) {
        String limitation = "";
        if (numberOfItems != -1) {
            limitation = " limit " + (page - 1) * numberOfItems + ", " + numberOfItems;
        }

        String filterQuery = "";
        if (filter != IssueFilter.all) {
            if (filter == IssueFilter.resolved) {
                filterQuery = " AND status_id>=80";
            } else {
                filterQuery = " AND status_id<80";
            }
        }

        List<Issue<Long>> issues = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM issues WHERE project=?" + filterQuery + limitation, new String[]{String.valueOf(project_id)});
        while (cursor.moveToNext()) {
            Issue<Long> issue = this.getIssue((long) this.getInt(cursor, "id"), project_id);
            issue.getHints().put("version", issue.getVersion());
            issue.getHints().put("view", issue.getState().getValue());
            issue.getHints().put("status", issue.getStatus().getValue());
            issues.add(issue);
        }
        cursor.close();
        return issues;
    }

    @Override
    public Issue<Long> getIssue(Long id, Long project_id) {
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

            Profile<Long> profile = new Profile<>();
            profile.setPlatform(this.getString(cursor, "platform"));
            profile.setOs(this.getString(cursor, "os"));
            profile.setOs_build(this.getString(cursor, "os_build"));
            issue.setProfile(profile);
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

        List<CustomField<Long>> customFields = this.getCustomFields(project_id);
        for (CustomField<Long> customField : customFields) {
            cursor = db.rawQuery("SELECT fieldResult.fieldValue, customFields.id FROM fieldResult INNER JOIN customFields ON fieldResult.field=customFields.id WHERE issue=? AND customFields.id=?", new String[]{String.valueOf(issue.getId()), String.valueOf(customField.getId())});
            boolean hasValue = false;
            while (cursor.moveToNext()) {
                hasValue = true;
                issue.getCustomFields().put(customField, this.getString(cursor, "fieldResult.fieldValue"));
            }
            cursor.close();

            if (!hasValue) {
                issue.getCustomFields().put(customField, "");
            }
        }

        return issue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void insertOrUpdateIssue(Issue<Long> issue, Long project_id) {
        if (issue.getId() != null) {
            Issue<Long> oldIssue = this.getIssue(Long.parseLong(String.valueOf(issue.getId())), Long.parseLong(String.valueOf(project_id)));
            this.addHistoryItem(oldIssue, issue);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
        if (issue.getId() == null) {
            sqLiteStatement = db.compileStatement(
                    "INSERT INTO issues(title,category,state_id,priority_id,severity_id," +
                            "status_id,reproducibility_id,resolution_id,version,fixedInVersion," +
                            "targetVersion,tags,dueDate,lastUpdated,submitDate,description," +
                            "steps_to_reproduce,additional_information,platform,os,os_build,project) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );
        } else {
            sqLiteStatement = db.compileStatement(
                    "UPDATE issues SET title=?,category=?,state_id=?,priority_id=?,severity_id=?," +
                            "status_id=?,reproducibility_id=?,resolution_id=?,version=?,fixedInVersion=?," +
                            "targetVersion=?,tags=?,dueDate=?,lastUpdated=?,submitDate=?,description=?," +
                            "steps_to_reproduce=?,additional_information=?,platform=?,os=?,os_build=?,project=? WHERE id=?"
            );
            sqLiteStatement.bindLong(23, Long.parseLong(String.valueOf(issue.getId())));
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
        if (issue.getProfile() != null) {
            sqLiteStatement.bindString(19, issue.getProfile().getPlatform());
            sqLiteStatement.bindString(20, issue.getProfile().getOs());
            sqLiteStatement.bindString(21, issue.getProfile().getOs_build());
        } else {
            sqLiteStatement.bindString(19, "");
            sqLiteStatement.bindString(20, "");
            sqLiteStatement.bindString(21, "");
        }
        sqLiteStatement.bindLong(22, project_id);

        if (issue.getId() != null) {
            sqLiteStatement.execute();
        } else {
            issue.setId(sqLiteStatement.executeInsert());
        }
        sqLiteStatement.close();

        this.getWritableDatabase().execSQL("DELETE FROM notes WHERE issue=" + issue.getId());
        if (!issue.getNotes().isEmpty()) {
            for (DescriptionObject descriptionObject : issue.getNotes()) {
                if (descriptionObject instanceof Note) {
                    long id = Long.parseLong(String.valueOf(issue.getId()));
                    Note<Long> note = (Note<Long>) descriptionObject;
                    note.setId(null);
                    this.insertOrUpdateNote(note, id, project_id);
                }
            }
        }

        this.getWritableDatabase().execSQL("DELETE FROM attachments WHERE issue=" + issue.getId());
        if (!issue.getAttachments().isEmpty()) {
            for (DescriptionObject descriptionObject : issue.getAttachments()) {
                if (descriptionObject instanceof Attachment) {
                    long id = Long.parseLong(String.valueOf(issue.getId()));
                    Attachment<Long> attachment = (Attachment<Long>) descriptionObject;
                    attachment.setId(null);
                    this.insertOrUpdateAttachment(attachment, id, project_id);
                }
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
    }

    @Override
    public void deleteIssue(Long id, Long project_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM fieldResult WHERE issue=" + id);
        db.execSQL("DELETE FROM notes WHERE issue=" + id);
        db.execSQL("DELETE FROM attachments WHERE issue=" + id);
        db.execSQL("DELETE FROM issues WHERE id=" + id);
    }

    @Override
    public List<Note<Long>> getNotes(Long issue_id, Long project_id) {
        List<Note<Long>> notes = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notes", new String[]{});
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
            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    @Override
    public void insertOrUpdateNote(Note<Long> note, Long issue_id, Long project_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
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
        sqLiteStatement.bindLong(6, Long.parseLong(String.valueOf(issue_id)));
        sqLiteStatement.execute();
        sqLiteStatement.close();
    }

    @Override
    public void deleteNote(Long id, Long issue_id, Long project_id) {
        this.getWritableDatabase().execSQL("DELETE FROM notes WHERE id=" + id);
    }

    @Override
    public List<Attachment<Long>> getAttachments(Long issue_id, Long project_id) {
        List<Attachment<Long>> attachments = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM attachments", new String[]{});
        while (cursor.moveToNext()) {
            Attachment<Long> attachment = new Attachment<>();
            attachment.setId(this.getLong(cursor, "id"));
            attachment.setTitle(this.getString(cursor, "title"));
            attachment.setContent(cursor.getBlob(cursor.getColumnIndex("content")));
            attachments.add(attachment);
        }
        cursor.close();
        return attachments;
    }

    @Override
    public void insertOrUpdateAttachment(Attachment<Long> attachment, Long issue_id, Long project_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
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
        sqLiteStatement.bindLong(4, Long.parseLong(String.valueOf(issue_id)));
        sqLiteStatement.execute();
        sqLiteStatement.close();
    }

    @Override
    public void deleteAttachment(Long id, Long issue_id, Long project_id) {
        this.getWritableDatabase().execSQL("DELETE FROM attachments WHERE id=" + id);
    }

    @Override
    public List<Relationship<Long>> getBugRelations(Long issue_id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateBugRelations(Relationship<Long> relationship, Long issue_id, Long project_id) {

    }

    @Override
    public void deleteBugRelation(Relationship<Long> relationship, Long issue_id, Long project_id) {

    }

    @Override
    public List<User<Long>> getUsers(Long project_id) {
        return new LinkedList<>();
    }

    @Override
    public User<Long> getUser(Long id, Long project_id) {
        return null;
    }

    @Override
    public void insertOrUpdateUser(User<Long> user, Long project_id) {
    }

    @Override
    public void deleteUser(Long id, Long project_id) {
    }

    @Override
    public List<CustomField<Long>> getCustomFields(Long project_id) {
        List<CustomField<Long>> customFields = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM customFields WHERE project=?", new String[]{String.valueOf(project_id)});
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
            customFields.add(customField);
        }
        cursor.close();
        return customFields;
    }

    @Override
    public CustomField<Long> getCustomField(Long id, Long project_id) {
        CustomField<Long> customField = new CustomField<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM customFields WHERE id=?", new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            customField.setId(this.getLong(cursor, "id"));
            customField.setTitle(this.getString(cursor, "title"));
            customField.setType(this.getInt(cursor, "type"));
            customField.setPossibleValues(this.getString(cursor, "possibleValues"));
            customField.setDefaultValue(this.getString(cursor, "defaultValue"));
            customField.setMinLength(this.getInt(cursor, "minLength"));
            customField.setMaxLength(this.getInt(cursor, "maxLength"));
            customField.setDescription(this.getString(cursor, "description"));
        }
        cursor.close();
        return customField;
    }

    @Override
    public void insertOrUpdateCustomField(CustomField<Long> customField, Long project_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement sqLiteStatement;
        if (customField.getId() == null) {
            sqLiteStatement = db.compileStatement("INSERT INTO customFields(title, type, possibleValues, defaultValue, minLength, maxLength, description, project) VALUES(?,?,?,?,?,?,?,?)");
        } else {
            sqLiteStatement = db.compileStatement("UPDATE customFields SET title=?, type=?, possibleValues=?, defaultValue=?, minLength=?, maxLength=?, description=?,project=? WHERE ID=?");
            sqLiteStatement.bindLong(9, customField.getId());
        }
        sqLiteStatement.bindString(1, customField.getTitle());
        sqLiteStatement.bindLong(2, customField.getIntType());
        if (customField.getPossibleValues() != null) {
            sqLiteStatement.bindString(3, customField.getPossibleValues());
        } else {
            sqLiteStatement.bindNull(3);
        }
        sqLiteStatement.bindString(4, customField.getDefaultValue());
        sqLiteStatement.bindLong(5, customField.getMinLength());
        sqLiteStatement.bindLong(6, customField.getMaxLength());
        sqLiteStatement.bindString(7, customField.getDescription());
        sqLiteStatement.bindLong(8, project_id);

        if (customField.getId() != null) {
            sqLiteStatement.execute();
        } else {
            customField.setId(sqLiteStatement.executeInsert());
        }
        sqLiteStatement.close();

    }

    @Override
    public void deleteCustomField(Long id, Long project_id) {
        this.getWritableDatabase().execSQL("DELETE FROM customFields WHERE id=?", new String[]{String.valueOf(id)});
    }

    @Override
    public List<String> getCategories(Long project_id) {
        List<String> categories = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM issues WHERE project=?", new String[]{String.valueOf(project_id)});
        while (cursor.moveToNext()) {
            categories.add(this.getString(cursor, "category"));
        }
        cursor.close();
        return categories;
    }

    @Override
    public List<Tag<Long>> getTags(Long project_id) {
        List<Tag<Long>> tags = new LinkedList<>();
        List<String> strTags = new LinkedList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT DISTINCT tags FROM issues", new String[]{});
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
            cursor.close();
        } catch (Exception ignored) {
        }

        long i = 1;
        for (String strTag : strTags) {
            Tag<Long> tag = new Tag<>();
            tag.setId(i);
            tag.setTitle(strTag);
            tags.add(tag);
            i++;
        }
        return tags;
    }

    @Override
    public List<History<Long>> getHistory(Long issue_id, Long project_id) {
        List<History<Long>> historyItems = new LinkedList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE issue=?", new String[]{String.valueOf(issue_id)});
        while (cursor.moveToNext()) {
            History<Long> history = new History<>();
            history.setField(this.getString(cursor, "field"));
            history.setOldValue(this.getString(cursor, "oldVal"));
            history.setNewValue(this.getString(cursor, "newVal"));
            history.setUser("");
            history.setTime(this.getLong(cursor, "timestamp"));
            historyItems.add(history);
        }
        cursor.close();
        return historyItems;
    }

    @Override
    public List<Profile<Long>> getProfiles() {
        List<Profile<Long>> profiles = new LinkedList<>();
        profiles.add(new Profile<>("PC", "Windows", "XP"));
        profiles.add(new Profile<>("PC", "Windows", "7"));
        profiles.add(new Profile<>("PC", "Windows", "10"));
        profiles.add(new Profile<>("PC", "Linux", ""));
        profiles.add(new Profile<>("PC", "Other", ""));
        profiles.add(new Profile<>("Smartphone", "Android", "5.0"));
        profiles.add(new Profile<>("Smartphone", "Android", "6.0"));
        profiles.add(new Profile<>("Smartphone", "Android", "7.0"));
        profiles.add(new Profile<>("Smartphone", "Android", "8.0"));
        profiles.add(new Profile<>("Smartphone", "Android", "9.0"));
        profiles.add(new Profile<>("Smartphone", "iOs", ""));
        profiles.add(new Profile<>("Other", "", ""));
        return profiles;
    }

    @Override
    public IFunctionImplemented getPermissions() {
        return new SQLitePermissions();
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public List<String> getEnums(String title) {
        return null;
    }

    @Override
    public int getCurrentState() {
        return 200;
    }

    @Override
    public String getCurrentMessage() {
        return "";
    }

    @NonNull
    @Override
    public String toString() {
        return this.getAuthentication().getTitle();
    }

    private void addHistoryItem(Issue<Long> oldIssue, Issue<Long> newIssue) {
        if (!oldIssue.getTitle().equals(newIssue.getTitle())) {
            this.addHistory("title", oldIssue.getTitle(), newIssue.getTitle(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getCategory().equals(newIssue.getCategory())) {
            this.addHistory("category", oldIssue.getCategory(), newIssue.getCategory(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getTags().equals(newIssue.getTags())) {
            this.addHistory("tags", oldIssue.getTags(), newIssue.getTags(), String.valueOf(newIssue.getId()));
        }
        if (oldIssue.getDueDate() != null && newIssue.getDueDate() != null) {
            if (oldIssue.getDueDate().compareTo(newIssue.getDueDate()) != 0) {
                this.addHistory("dueDate", String.valueOf(oldIssue.getDueDate()), String.valueOf(newIssue.getDueDate()), String.valueOf(newIssue.getId()));
            }
        }
        if (!oldIssue.getVersion().equals(newIssue.getVersion())) {
            this.addHistory("version", oldIssue.getVersion(), newIssue.getVersion(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getFixedInVersion().equals(newIssue.getFixedInVersion())) {
            this.addHistory("fixedInVersion", oldIssue.getFixedInVersion(), newIssue.getFixedInVersion(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getTargetVersion().equals(newIssue.getTargetVersion())) {
            this.addHistory("targetVersion", oldIssue.getTargetVersion(), newIssue.getTargetVersion(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getVersion().equals(newIssue.getVersion())) {
            this.addHistory("version", oldIssue.getVersion(), newIssue.getVersion(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getResolution().getValue().equals(newIssue.getResolution().getValue())) {
            this.addHistory("resolution", oldIssue.getResolution().getValue(), newIssue.getResolution().getValue(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getReproducibility().getValue().equals(newIssue.getReproducibility().getValue())) {
            this.addHistory("reproducibility", oldIssue.getReproducibility().getValue(), newIssue.getReproducibility().getValue(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getStatus().getValue().equals(newIssue.getStatus().getValue())) {
            this.addHistory("status", oldIssue.getStatus().getValue(), newIssue.getStatus().getValue(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getSeverity().getValue().equals(newIssue.getSeverity().getValue())) {
            this.addHistory("severity", oldIssue.getSeverity().getValue(), newIssue.getSeverity().getValue(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getState().getValue().equals(newIssue.getState().getValue())) {
            this.addHistory("state", oldIssue.getState().getValue(), newIssue.getState().getValue(), String.valueOf(newIssue.getId()));
        }
        if (!oldIssue.getPriority().getValue().equals(newIssue.getPriority().getValue())) {
            this.addHistory("priority", oldIssue.getPriority().getValue(), newIssue.getPriority().getValue(), String.valueOf(newIssue.getId()));
        }
    }

    private void addHistory(String field, String oldVal, String newVal, String id) {
        this.getWritableDatabase()
                .execSQL(
                        "INSERT INTO history(field, oldVal, newVal, timestamp, issue) " +
                                "VALUES('" + field + "', '" + oldVal.replace("'", "\"") + "', '" + newVal.replace("'", "\"") + "', " + new Date().getTime() + ", " + id + ")"
                );
    }

    private void initDatabase(SQLiteDatabase db) {
        try {
            String queries = this.readStringFromRaw(R.raw.init_tracker, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.context);
        }
    }

    private void updateDatabase(SQLiteDatabase db) {
        try {
            String queries = this.readStringFromRaw(R.raw.update_tracker, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.context);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String readStringFromRaw(int rawID, Context context) throws Exception {
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(rawID);

        byte[] b = new byte[in_s.available()];
        in_s.read(b);
        return new String(b);
    }
}
