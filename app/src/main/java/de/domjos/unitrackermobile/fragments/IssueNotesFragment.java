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

package de.domjos.unitrackermobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Note;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.adapter.ListAdapter;
import de.domjos.unitrackermobile.helper.ArrayHelper;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueNotesFragment extends AbstractFragment {
    private ListView lvIssueNotes;
    private ListAdapter notesAdapter;
    private ImageButton cmdIssueNotesAdd, cmdIssueNotesEdit, cmdIssueNotesDelete, cmdIssueNotesCancel, cmdIssueNotesSave;
    private EditText txtIssueNotesText;
    private TextView txtIssueNotesSubmitDate, txtIssueNotesLastUpdated;
    private Spinner spIssueNotesView;
    private IBugService bugService;

    private View root;
    private Issue issue;
    private boolean editMode;
    private Note currentNote;
    private String statusValueArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_notes, container, false);

        this.lvIssueNotes = this.root.findViewById(R.id.lvIssuesNote);
        if (this.getContext() != null) {
            this.notesAdapter = new ListAdapter(this.getContext(), R.drawable.ic_note_black_24dp);
            this.lvIssueNotes.setAdapter(this.notesAdapter);
            this.notesAdapter.notifyDataSetChanged();
            this.bugService = Helper.getCurrentBugService(this.getContext());
        }
        this.cmdIssueNotesAdd = this.root.findViewById(R.id.cmdIssueNotesAdd);
        this.cmdIssueNotesEdit = this.root.findViewById(R.id.cmdIssueNotesEdit);
        this.cmdIssueNotesDelete = this.root.findViewById(R.id.cmdIssueNotesDelete);
        this.cmdIssueNotesCancel = this.root.findViewById(R.id.cmdIssueNotesCancel);
        this.cmdIssueNotesSave = this.root.findViewById(R.id.cmdIssueNotesSave);
        this.txtIssueNotesText = this.root.findViewById(R.id.txtIssueNotesContent);
        this.txtIssueNotesSubmitDate = this.root.findViewById(R.id.txtIssueNotesSubmitDate);
        this.txtIssueNotesLastUpdated = this.root.findViewById(R.id.txtIssueNotesLastUpdated);
        this.spIssueNotesView = this.root.findViewById(R.id.spIssueNotesView);
        this.spIssueNotesView.setAdapter(Helper.setAdapter(this.getContext(), this.statusValueArray));

        this.lvIssueNotes.setOnItemClickListener((parent, view, position, id) -> {
            ListObject object = this.notesAdapter.getItem(position);
            if (object != null) {
                this.currentNote = (Note) object.getDescriptionObject();
                this.noteToFields();
            }
            this.manageNoteControls(false, false, true);
        });

        this.cmdIssueNotesAdd.setOnClickListener(v -> {
            this.currentNote = new Note();
            this.noteToFields();
            this.manageNoteControls(true, true, false);
        });

        this.cmdIssueNotesEdit.setOnClickListener(v -> this.manageNoteControls(true, false, false));

        this.cmdIssueNotesDelete.setOnClickListener(v -> {
            Object id = this.currentNote.getId();
            for (int i = 0; i <= this.notesAdapter.getCount() - 1; i++) {
                ListObject listObject = this.notesAdapter.getItem(i);
                if (listObject != null) {
                    if (id != null) {
                        if (id.equals(listObject.getDescriptionObject().getId())) {
                            this.notesAdapter.remove(this.notesAdapter.getItem(i));
                            break;
                        }
                    } else {
                        if (this.currentNote.getDescription().equals(listObject.getDescriptionObject().getDescription())) {
                            this.notesAdapter.remove(this.notesAdapter.getItem(i));
                            break;
                        }
                    }
                }
            }
            this.currentNote = new Note();
            this.noteToFields();
            this.manageNoteControls(false, true, false);
        });

        this.cmdIssueNotesCancel.setOnClickListener(v -> {
            this.currentNote = new Note();
            this.noteToFields();
        });

        this.cmdIssueNotesSave.setOnClickListener(v -> {
            this.fieldsToNote();
            Object id = this.currentNote.getId();
            if (id != null) {
                for (int i = 0; i <= this.notesAdapter.getCount() - 1; i++) {
                    ListObject listObject = this.notesAdapter.getItem(i);
                    if (listObject != null) {
                        if (id.equals(listObject.getDescriptionObject().getId())) {
                            this.notesAdapter.remove(this.notesAdapter.getItem(i));
                            this.notesAdapter.add(new ListObject(this.getContext(), R.mipmap.ic_launcher_round, this.currentNote));
                            break;
                        }
                    }
                }
            } else {
                this.notesAdapter.add(new ListObject(this.getContext(), R.mipmap.ic_launcher_round, this.currentNote));
            }
            this.manageNoteControls(false, true, false);
        });

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        return this.root;
    }

    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue) descriptionObject;
    }

    @Override
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            issue.getNotes().clear();
            for (int i = 0; i <= this.notesAdapter.getCount() - 1; i++) {
                ListObject object = this.notesAdapter.getItem(i);
                if (object != null) {
                    issue.getNotes().add(object.getDescriptionObject());
                }
            }
        }
        return issue;
    }

    @Override
    public void manageControls(boolean editMode) {
        this.editMode = editMode;

        if (this.root != null) {
            this.cmdIssueNotesSave.setEnabled(this.editMode);
            this.manageNoteControls(false, true, false);
        }
    }

    @Override
    protected void initData() {
        this.notesAdapter.clear();
        for (Object note : this.issue.getNotes()) {
            this.spIssueNotesView.setAdapter(Helper.setAdapter(this.getContext(), "issues_general_view_values"));
            this.notesAdapter.add(new ListObject(this.getContext(), R.drawable.ic_note_black_24dp, (Note) note));
        }
    }

    @Override
    public Validator initValidator() {
        Validator validator = new Validator(this.getContext());
        if (this.root != null) {

        }
        return validator;
    }

    @Override
    public void updateUITrackerSpecific() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();

        switch (authentication.getTracker()) {
            case MantisBT:
                this.statusValueArray = "issues_general_view_values";
                break;
            case YouTrack:
                this.statusValueArray = "issues_general_view_values";
                break;
        }
    }

    private void noteToFields() {
        if (this.currentNote != null) {
            this.txtIssueNotesText.setText(this.currentNote.getDescription());
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.currentNote.getState().getKey().toString()), this.statusValueArray, this.spIssueNotesView);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
            if (this.currentNote.getLastUpdated() != null) {
                this.txtIssueNotesLastUpdated.setText(sdf.format(this.currentNote.getLastUpdated()));
            }
            if (this.currentNote.getSubmitDate() != null) {
                this.txtIssueNotesSubmitDate.setText(sdf.format(this.currentNote.getSubmitDate()));
            }

        }
    }

    private void fieldsToNote() {
        if (this.currentNote != null) {
            this.currentNote.setDescription(this.txtIssueNotesText.getText().toString());
            if (this.spIssueNotesView.getSelectedItem() != null) {
                this.currentNote.setState(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueNotesView, this.statusValueArray), this.spIssueNotesView.getSelectedItem().toString());
            }
            if (this.currentNote.getDescription().length() > 50) {
                this.currentNote.setTitle(this.currentNote.getDescription().substring(0, 50));
            } else {
                this.currentNote.setTitle(this.currentNote.getDescription());
            }
        }
    }

    private void manageNoteControls(boolean editMode, boolean reset, boolean selected) {
        this.txtIssueNotesText.setEnabled(editMode);
        this.spIssueNotesView.setEnabled(editMode);
        this.lvIssueNotes.setEnabled(!editMode);
        this.cmdIssueNotesAdd.setEnabled(!editMode && this.editMode && this.bugService.getPermissions().addNotes());
        this.cmdIssueNotesEdit.setEnabled(!editMode && selected && this.editMode && this.bugService.getPermissions().updateNotes());
        this.cmdIssueNotesDelete.setEnabled(!editMode && selected && this.editMode && this.bugService.getPermissions().deleteNotes());
        this.cmdIssueNotesCancel.setEnabled(editMode);
        this.cmdIssueNotesSave.setEnabled(editMode);

        if (reset) {
            this.currentNote = new Note();
            this.noteToFields();
        }
    }
}