/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Relationship;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unibuggermobile.helper.ArrayHelper;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.customwidgets.utils.Validator;

import java.util.*;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueRelationsFragment extends AbstractFragment {
    private SwipeRefreshDeleteList lvIssuesRelations;
    private ImageButton cmdIssuesRelationsAdd, cmdIssuesRelationsEdit, cmdIssuesRelationsDelete, cmdIssuesRelationsCancel, cmdIssuesRelationsSave;
    private AutoCompleteTextView txtIssuesRelationsIssues;
    private Spinner spIssuesRelationsType;
    private ArrayAdapter<Issue<?>> issuesAdapter;
    private IBugService bugService;
    private ArrayAdapter<String> relationTypeAdapter;
    private Relationship currentEntry;

    private View root;
    private Issue issue;
    private boolean editMode;
    private String arrayKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_relations, container, false);

        this.lvIssuesRelations = this.root.findViewById(R.id.lvIssuesRelations);
        if (this.getContext() != null) {
            this.bugService = Helper.getCurrentBugService(this.getContext());
        }
        this.cmdIssuesRelationsAdd = this.root.findViewById(R.id.cmdIssuesRelationsAdd);
        this.cmdIssuesRelationsEdit = this.root.findViewById(R.id.cmdIssuesRelationsEdit);
        this.cmdIssuesRelationsDelete = this.root.findViewById(R.id.cmdIssuesRelationsDelete);
        this.cmdIssuesRelationsCancel = this.root.findViewById(R.id.cmdIssuesRelationsCancel);
        this.cmdIssuesRelationsSave = this.root.findViewById(R.id.cmdIssuesRelationsSave);
        this.txtIssuesRelationsIssues = this.root.findViewById(R.id.txtIssuesRelationsIssues);
        this.spIssuesRelationsType = this.root.findViewById(R.id.spIssuesRelationsType);


        if(this.getContext()!=null) {
            List<Issue<?>> issues = new LinkedList<>();
            try {
                IBugService<?> bugService = Helper.getCurrentBugService(this.getActivity());
                Object pid = MainActivity.GLOBALS.getSettings(this.getActivity()).getCurrentProjectId();
                boolean show = MainActivity.GLOBALS.getSettings(this.getActivity()).showNotifications();

                IssueTask issueTask = new IssueTask(this.getActivity(), bugService, pid, false, false, show, R.drawable.icon_issues);
                issueTask.setId(this.notificationId);
                issues = issueTask.execute(0).get();
            } catch (Exception ignored) {}


            this.issuesAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, issues);
            this.txtIssuesRelationsIssues.setAdapter(issuesAdapter);
            this.issuesAdapter.notifyDataSetChanged();
        }

        this.lvIssuesRelations.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            currentEntry = (Relationship<?>) listObject.getObject();
            manageRelationControls(false, false, true);
        });

        this.lvIssuesRelations.setOnDeleteListener(listObject -> {
            currentEntry = (Relationship<?>) listObject.getObject();
            delete();
        });

        this.cmdIssuesRelationsAdd.setOnClickListener(v -> this.manageRelationControls(true, true, false));

        this.cmdIssuesRelationsEdit.setOnClickListener(v -> this.manageRelationControls(true, false, false));

        this.cmdIssuesRelationsDelete.setOnClickListener(v -> {
            for(int i = 0; i<=this.lvIssuesRelations.getAdapter().getItemCount()-1; i++) {
                if(this.lvIssuesRelations.getAdapter().getItem(i).getTitle().equals(this.currentEntry.getTitle())) {
                    this.lvIssuesRelations.getAdapter().deleteItem(i);
                    break;
                }
            }
            this.manageRelationControls(false, true, false);
        });

        this.cmdIssuesRelationsCancel.setOnClickListener(v -> this.manageRelationControls(false, true, false));

        this.cmdIssuesRelationsSave.setOnClickListener(v -> {
            Issue<?> issue = null;
            for(int i = 0; i<=this.issuesAdapter.getCount()-1; i++) {
                Issue<?> tmp = this.issuesAdapter.getItem(i);
                if(tmp!=null) {
                    if(tmp.getTitle().equals(this.txtIssuesRelationsIssues.getText().toString().trim())) {
                        issue = tmp;
                        break;
                    }
                }
            }

            int index = -1;
            for(int i = 0; i<=this.lvIssuesRelations.getAdapter().getItemCount()-1; i++) {
                Object id = ((Relationship<?>)this.lvIssuesRelations.getAdapter().getItem(i).getObject()).getId();
                Object entryId = currentEntry.getId();
                if(id != null && entryId != null) {
                    if(entryId.toString().equals(id.toString())) {
                        index = i;
                        break;
                    }
                }
            }

            if(issue != null) {
                Relationship relationship = new Relationship<>();
                if(index != -1) {
                    relationship.setId(((Relationship)this.lvIssuesRelations.getAdapter().getItem(index).getObject()).getId());
                }
                relationship.setIssue(issue);
                int id = ArrayHelper.getIdOfEnum(this.getContext(), this.spIssuesRelationsType, this.arrayKey);
                relationship.setType(new AbstractMap.SimpleEntry<>(this.spIssuesRelationsType.getSelectedItem().toString(), id));
                issue.setDescription(this.spIssuesRelationsType.getSelectedItem().toString());
                BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                baseDescriptionObject.setObject(relationship);
                baseDescriptionObject.setTitle(relationship.getTitle());
                baseDescriptionObject.setDescription(relationship.getDescription());
                if(index != -1 && this.lvIssuesRelations.getAdapter().getItemCount() != 0)  {
                    this.lvIssuesRelations.getAdapter().deleteItem(index);
                }
                this.lvIssuesRelations.getAdapter().add(baseDescriptionObject);
                this.manageRelationControls(false, true, false);
            }
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
    @SuppressWarnings("unchecked")
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            issue.getRelations().clear();
            for(int i = 0; i <= this.lvIssuesRelations.getAdapter().getItemCount() - 1; i++) {
                Relationship relationship = (Relationship) this.lvIssuesRelations.getAdapter().getItem(i).getObject();
                issue.getRelations().add(relationship);
            }
        }
        return issue;
    }

    @Override
    public void manageControls(boolean editMode) {
        this.editMode = editMode;

        if (this.root != null) {
            this.cmdIssuesRelationsSave.setEnabled(this.editMode);
            this.manageRelationControls(false, true, false);
        }
    }

    @Override
    protected void initData() {
        this.lvIssuesRelations.getAdapter().clear();
        for(Object obj : this.issue.getRelations()) {
            if(obj instanceof Relationship) {
                Relationship relationship = (Relationship) obj;

                BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                baseDescriptionObject.setObject(relationship);
                baseDescriptionObject.setTitle(relationship.getTitle());
                baseDescriptionObject.setDescription(relationship.getDescription());
                if(this.getContext() != null) {
                    this.lvIssuesRelations.getAdapter().add(baseDescriptionObject);
                } else {
                    if(this.getActivity() != null) {
                        this.lvIssuesRelations.getAdapter().add(baseDescriptionObject);
                    }
                }
            }
        }
    }

    @Override
    public Validator initValidator() {
        return new Validator(this.getContext(), R.mipmap.ic_launcher_round);
    }

    @Override
    public void updateUITrackerSpecific() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();

        switch (authentication.getTracker()) {
            case MantisBT:
            case Local:
                this.arrayKey = "issues_general_relations_mantisbt_values";
                break;
            case YouTrack:
                this.arrayKey = "issues_general_relations_youtrack_values";
                break;
            case Jira:
                this.arrayKey = "issues_general_relations_jira_values";
                break;
            case RedMine:
                this.arrayKey = "issues_general_relations_redmine_values";
                break;
        }

        if(this.getContext()!=null)  {
            this.relationTypeAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, ArrayHelper.getValues(this.getContext(), this.arrayKey));
            this.spIssuesRelationsType.setAdapter(this.relationTypeAdapter);
            this.relationTypeAdapter.notifyDataSetChanged();
        }
    }

    private void manageRelationControls(boolean editMode, boolean reset, boolean selected) {
        this.txtIssuesRelationsIssues.setEnabled(editMode);
        this.spIssuesRelationsType.setEnabled(editMode);
        this.lvIssuesRelations.setEnabled(!editMode);
        this.cmdIssuesRelationsAdd.setEnabled(!editMode && this.editMode && this.bugService.getPermissions().addRelation());
        this.cmdIssuesRelationsEdit.setEnabled(!editMode && selected && this.editMode && this.bugService.getPermissions().updateRelation());
        this.cmdIssuesRelationsDelete.setEnabled(!editMode && selected && this.editMode && this.bugService.getPermissions().deleteRelation());
        this.cmdIssuesRelationsCancel.setEnabled(editMode);
        this.cmdIssuesRelationsSave.setEnabled(editMode);

        if (reset) {
            this.currentEntry = new Relationship();
        }
        if (selected) {
            this.txtIssuesRelationsIssues.setText(currentEntry.getTitle());

            for(int i = 0; i<=this.relationTypeAdapter.getCount()-1; i++) {
                if(this.currentEntry.getType().getKey().toString().equals(this.relationTypeAdapter.getItem(i))) {
                    this.spIssuesRelationsType.setSelection(i);
                    break;
                }
            }
        }
    }

    private void delete() {
        try {
            if(this.currentEntry != null) {
                new Thread(() -> {
                    try {
                        Object obj = this.returnTemp(MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentProjectId());
                        this.bugService.deleteBugRelation(this.currentEntry, this.issue.getId(), obj);
                    }  catch (Exception ex) {
                        Objects.requireNonNull(getActivity()).runOnUiThread(()-> MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getActivity()));
                    }
                }).start();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getActivity());
        }
    }

    private Object returnTemp(Object o) {
        if (o == null) {
            return null;
        } else {
            if (o.equals(0) || o.equals("")) {
                return null;
            } else {
                Object tmp;
                try {
                    tmp = Long.parseLong(String.valueOf(o));
                } catch (Exception ex) {
                    tmp = o;
                }
                return tmp;
            }
        }
    }
}