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

package de.domjos.unibuggermobile.fragments;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.issues.Tag;
import de.domjos.unibuggerlibrary.model.issues.User;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.model.projects.Version;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.versions.ListVersionTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.custom.CommaTokenizer;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueGeneralFragment extends AbstractFragment {
    private EditText txtIssueGeneralSummary, txtIssueGeneralDueDate;
    private TextView txtIssueGeneralSubmitted, txtIssueGeneralUpdated;
    private AutoCompleteTextView txtIssueGeneralCategory, txtIssueGeneralVersion, txtIssueGeneralTargetVersion, txtIssueGeneralFixedInVersion;
    private Spinner spIssueGeneralView, spIssueGeneralSeverity, spIssueGeneralReproducibility;
    private Spinner spIssueGeneralPriority, spIssueGeneralStatus, spIssueGeneralResolution, spIssueGeneralHandler;
    private MultiAutoCompleteTextView txtIssueGeneralTags;
    private ArrayAdapter<User> userAdapter;
    private ArrayAdapter<String> tagAdapter;

    private int priorityValueArray;
    private TableRow rowIssueGeneralDueDate, rowIssueGeneralDates, rowIssueGeneralCategory, rowIssueGeneralVersion, rowIssueGeneralTargetVersion, rowIssueGeneralFixedInVersion, rowIssueGeneralTags;
    private TableRow rowIssueGeneralView, rowIssueGeneralSeverity, rowIssueGeneralReproducibility, rowIssueGeneralPriority, rowIssueGeneralStatus, rowIssueGeneralResolution, rowIssueGeneralHandler;

    private View root;
    private Issue issue;
    private boolean editMode;
    private Object pid;
    private IBugService bugService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bugService = Helper.getCurrentBugService(this.getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_general, container, false);

        this.txtIssueGeneralSummary = this.root.findViewById(R.id.txtIssueGeneralSummary);

        this.txtIssueGeneralCategory = this.root.findViewById(R.id.txtIssueGeneralCategory);
        this.initCategories();

        this.spIssueGeneralView = this.root.findViewById(R.id.spIssueGeneralView);
        this.spIssueGeneralSeverity = this.root.findViewById(R.id.spIssueGeneralSeverity);
        this.spIssueGeneralReproducibility = this.root.findViewById(R.id.spIssueGeneralReproducibilitty);
        this.spIssueGeneralPriority = this.root.findViewById(R.id.spIssueGeneralPriority);
        this.spIssueGeneralStatus = this.root.findViewById(R.id.spIssueGeneralStatus);
        this.spIssueGeneralHandler = this.root.findViewById(R.id.spIssueGeneralHandler);
        this.txtIssueGeneralTags = this.root.findViewById(R.id.txtIssueGeneralTags);

        this.rowIssueGeneralDueDate = this.root.findViewById(R.id.rowIssueGeneralDueDates);
        this.rowIssueGeneralDates = this.root.findViewById(R.id.rowIssueGeneralDates);
        this.rowIssueGeneralCategory = this.root.findViewById(R.id.rowIssueGeneralCategory);
        this.rowIssueGeneralVersion = this.root.findViewById(R.id.rowIssueGeneralVersion);
        this.rowIssueGeneralFixedInVersion = this.root.findViewById(R.id.rowIssueGeneralFixedInVersion);
        this.rowIssueGeneralTargetVersion = this.root.findViewById(R.id.rowIssueGeneralTargetVersion);
        this.rowIssueGeneralView = this.root.findViewById(R.id.rowIssueGeneralView);
        this.rowIssueGeneralPriority = this.root.findViewById(R.id.rowIssueGeneralPriority);
        this.rowIssueGeneralSeverity = this.root.findViewById(R.id.rowIssueGeneralSeverity);
        this.rowIssueGeneralStatus = this.root.findViewById(R.id.rowIssueGeneralStatus);
        this.rowIssueGeneralReproducibility = this.root.findViewById(R.id.rowIssueGeneralReproducibility);
        this.rowIssueGeneralResolution = this.root.findViewById(R.id.rowIssueGeneralResolution);
        this.rowIssueGeneralTags = this.root.findViewById(R.id.rowIssueGeneralTags);
        this.rowIssueGeneralHandler = this.root.findViewById(R.id.rowIssueGeneralHandler);


        try {
            if (this.getContext() != null) {
                this.userAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item);
                this.spIssueGeneralHandler.setAdapter(this.userAdapter);
                this.userAdapter.notifyDataSetChanged();

                this.tagAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1);
                this.txtIssueGeneralTags.setAdapter(this.tagAdapter);
                this.txtIssueGeneralTags.setTokenizer(new CommaTokenizer());
                this.tagAdapter.notifyDataSetChanged();


                if (this.getActivity() != null) {
                    new Thread(() -> {
                        try {
                            List<User> users = new LinkedList<>();
                            users.addAll(this.bugService.getUsers(pid));
                            users.add(0, new User());

                            for (Object tag : this.bugService.getTags()) {
                                this.tagAdapter.add(((Tag) tag).getTitle());
                            }

                            this.getActivity().runOnUiThread(() -> {
                                for (User user : users) {
                                    this.userAdapter.add(user);
                                }
                                if (this.issue != null) {
                                    if (this.issue.getHandler() != null) {
                                        for (int i = 0; i <= this.userAdapter.getCount() - 1; i++) {
                                            User user = this.userAdapter.getItem(i);
                                            if (user != null) {
                                                if (user.toString().equals(this.issue.getHandler().toString())) {
                                                    this.spIssueGeneralHandler.setSelection(i);
                                                    break;
                                                }
                                            }
                                        }

                                    }
                                    this.txtIssueGeneralTags.setText(this.issue.getTags());
                                }
                            });
                        } catch (Exception ex) {
                            this.getActivity().runOnUiThread(() -> MessageHelper.printException(ex, this.getActivity()));
                        }
                    }).start();
                }


            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.getActivity());
        }
        this.spIssueGeneralResolution = this.root.findViewById(R.id.spIssueGeneralResolution);
        this.txtIssueGeneralSubmitted = this.root.findViewById(R.id.txtIssueGeneralSubmitDate);
        this.txtIssueGeneralUpdated = this.root.findViewById(R.id.txtIssueGeneralLastUpdated);
        this.txtIssueGeneralDueDate = this.root.findViewById(R.id.txtIssueGeneralDueDate);

        this.txtIssueGeneralVersion = this.root.findViewById(R.id.txtIssueGeneralVersion);
        this.txtIssueGeneralTargetVersion = this.root.findViewById(R.id.txtIssueGeneralTargetVersion);
        this.txtIssueGeneralFixedInVersion = this.root.findViewById(R.id.txtIssueGeneralFixedInVersion);
        this.initVersions();

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        this.initValidator();
        return root;
    }

    @Override
    public void setPid(String pid) {
        try {
            this.pid = Long.parseLong(pid);
        } catch (Exception ex) {
            this.pid = pid;
        }
    }

    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue) descriptionObject;
    }

    @Override
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            issue.setTitle(this.txtIssueGeneralSummary.getText().toString());
            issue.setCategory(this.txtIssueGeneralCategory.getText().toString());
            issue.setState(this.getIdOfEnum(this.spIssueGeneralView, R.array.issues_general_view_values), this.spIssueGeneralView.getSelectedItem().toString());
            issue.setSeverity(this.getIdOfEnum(this.spIssueGeneralSeverity, R.array.issues_general_severity_values), this.spIssueGeneralSeverity.getSelectedItem().toString());
            issue.setReproducibility(this.getIdOfEnum(this.spIssueGeneralSeverity, R.array.issues_general_reproducibility_values), this.spIssueGeneralReproducibility.getSelectedItem().toString());
            issue.setPriority(this.getIdOfEnum(this.spIssueGeneralPriority, this.priorityValueArray), this.spIssueGeneralPriority.getSelectedItem().toString());
            issue.setStatus(this.getIdOfEnum(this.spIssueGeneralStatus, R.array.issues_general_status_values), this.spIssueGeneralStatus.getSelectedItem().toString());
            issue.setResolution(this.getIdOfEnum(this.spIssueGeneralResolution, R.array.issues_general_resolution_values), this.spIssueGeneralResolution.getSelectedItem().toString());
            issue.setVersion(this.txtIssueGeneralVersion.getText().toString());
            issue.setTargetVersion(this.txtIssueGeneralTargetVersion.getText().toString());
            issue.setFixedInVersion(this.txtIssueGeneralFixedInVersion.getText().toString());
            issue.setHandler((User) this.spIssueGeneralHandler.getSelectedItem());
            issue.setTags(this.txtIssueGeneralTags.getText().toString());

            try {
                if (!this.txtIssueGeneralDueDate.getText().toString().equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
                    issue.setDueDate(sdf.parse(this.txtIssueGeneralDueDate.getText().toString()));
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, this.getActivity());
            }
        }
        return issue;
    }

    @Override
    public void manageControls(boolean editMode) {
        this.editMode = editMode;
        if (this.root != null) {
            this.txtIssueGeneralSummary.setEnabled(editMode);
            this.txtIssueGeneralCategory.setEnabled(editMode);
            this.spIssueGeneralView.setEnabled(editMode);
            this.spIssueGeneralSeverity.setEnabled(editMode);
            this.spIssueGeneralReproducibility.setEnabled(editMode);
            this.spIssueGeneralStatus.setEnabled(editMode);
            this.spIssueGeneralPriority.setEnabled(editMode);
            this.spIssueGeneralResolution.setEnabled(editMode);
            this.txtIssueGeneralVersion.setEnabled(editMode);
            this.txtIssueGeneralTargetVersion.setEnabled(editMode);
            this.txtIssueGeneralFixedInVersion.setEnabled(editMode);
            this.txtIssueGeneralDueDate.setEnabled(editMode);
            this.spIssueGeneralHandler.setEnabled(editMode);
            this.txtIssueGeneralTags.setEnabled(editMode);
        }
    }

    @Override
    protected void initData() {
        if (this.issue != null) {
            this.txtIssueGeneralSummary.setText(this.issue.getTitle());
            this.txtIssueGeneralCategory.setText(this.issue.getCategory());
            this.setValueOfEnum(Integer.parseInt(this.issue.getState().getKey().toString()), R.array.issues_general_view_values, spIssueGeneralView);
            this.setValueOfEnum(Integer.parseInt(this.issue.getSeverity().getKey().toString()), R.array.issues_general_severity_values, spIssueGeneralSeverity);
            this.setValueOfEnum(Integer.parseInt(this.issue.getReproducibility().getKey().toString()), R.array.issues_general_reproducibility_values, spIssueGeneralReproducibility);
            this.setValueOfEnum(Integer.parseInt(this.issue.getPriority().getKey().toString()), this.priorityValueArray, spIssueGeneralPriority);
            this.setValueOfEnum(Integer.parseInt(this.issue.getStatus().getKey().toString()), R.array.issues_general_status_values, spIssueGeneralStatus);
            this.setValueOfEnum(Integer.parseInt(this.issue.getResolution().getKey().toString()), R.array.issues_general_resolution_values, spIssueGeneralResolution);
            this.spIssueGeneralHandler.setSelection(this.userAdapter.getPosition(this.issue.getHandler()));
            this.txtIssueGeneralVersion.setText(this.issue.getVersion());
            this.txtIssueGeneralTargetVersion.setText(this.issue.getTargetVersion());
            this.txtIssueGeneralFixedInVersion.setText(this.issue.getFixedInVersion());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
            if (this.issue.getSubmitDate() != null) {
                this.txtIssueGeneralSubmitted.setText(sdf.format(this.issue.getSubmitDate()));
            }
            if (this.issue.getLastUpdated() != null) {
                this.txtIssueGeneralUpdated.setText(sdf.format(this.issue.getLastUpdated()));
            }
            if (this.issue.getDueDate() != null) {
                this.txtIssueGeneralDueDate.setText(sdf.format(this.issue.getDueDate()));
            }
        }
    }

    @Override
    public Validator initValidator() {
        Validator validator = new Validator(this.getContext());
        if (this.root != null) {
            validator.addEmptyValidator(this.txtIssueGeneralSummary);
            validator.addEmptyValidator(this.txtIssueGeneralCategory);
            validator.addEmptyValidator(this.txtIssueGeneralTargetVersion);
        }
        return validator;
    }

    @Override
    public void updateUITrackerSpecific() {
        Authentication authentication = MainActivity.settings.getCurrentAuthentication();
        this.rowIssueGeneralDueDate.setVisibility(View.GONE);
        this.rowIssueGeneralDates.setVisibility(View.GONE);
        this.rowIssueGeneralCategory.setVisibility(View.GONE);
        this.rowIssueGeneralVersion.setVisibility(View.GONE);
        this.rowIssueGeneralFixedInVersion.setVisibility(View.GONE);
        this.rowIssueGeneralTargetVersion.setVisibility(View.GONE);
        this.rowIssueGeneralView.setVisibility(View.GONE);
        this.rowIssueGeneralPriority.setVisibility(View.GONE);
        this.rowIssueGeneralSeverity.setVisibility(View.GONE);
        this.rowIssueGeneralStatus.setVisibility(View.GONE);
        this.rowIssueGeneralReproducibility.setVisibility(View.GONE);
        this.rowIssueGeneralResolution.setVisibility(View.GONE);
        this.rowIssueGeneralTags.setVisibility(View.GONE);
        this.rowIssueGeneralHandler.setVisibility(View.GONE);

        switch (authentication.getTracker()) {
            case MantisBT:
                this.rowIssueGeneralDueDate.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralCategory.setVisibility(View.VISIBLE);
                this.rowIssueGeneralVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralFixedInVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTargetVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralView.setVisibility(View.VISIBLE);
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralReproducibility.setVisibility(View.VISIBLE);
                this.rowIssueGeneralResolution.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTags.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                this.priorityValueArray = R.array.issues_general_priority_mantisbt_values;
                break;
            case YouTrack:
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.priorityValueArray = R.array.issues_general_priority_youtrack_values;
                break;
        }

        if (this.getActivity() != null) {
            int spItem = android.R.layout.simple_spinner_item;
            String[] array = this.getResources().getStringArray(this.priorityValueArray);

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getActivity(), spItem, Arrays.asList(array));
            this.spIssueGeneralPriority.setAdapter(arrayAdapter);
        }
    }

    private void initCategories() {
        if (this.getContext() != null && this.getActivity() != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1);
            new Thread(() -> {
                try {
                    if (this.bugService != null) {
                        List<String> categories = this.bugService.getCategories(this.pid);
                        for (String category : categories) {
                            arrayAdapter.add(category);
                        }
                        this.getActivity().runOnUiThread(() -> txtIssueGeneralCategory.setAdapter(arrayAdapter));
                    }
                } catch (Exception ex) {
                    this.getActivity().runOnUiThread(() -> MessageHelper.printException(ex, this.getActivity()));
                }

            }).start();
        }
    }

    private void initVersions() {
        if (this.getContext() != null && this.getActivity() != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1);
            arrayAdapter.add("");
            try {
                if (this.bugService != null) {
                    List<Version> versions = new ListVersionTask(this.getActivity(), this.bugService, this.pid, "versions").execute().get();
                    for (Version version : versions) {
                        arrayAdapter.add(version.getTitle());
                    }
                }
            } catch (Exception ex) {
                this.getActivity().runOnUiThread(() -> MessageHelper.printException(ex, this.getActivity()));
            }
            this.txtIssueGeneralVersion.setAdapter(arrayAdapter);
            this.txtIssueGeneralTargetVersion.setAdapter(arrayAdapter);
            this.txtIssueGeneralFixedInVersion.setAdapter(arrayAdapter);
        }
    }

    private int getIdOfEnum(Spinner spinner, int resource) {
        int id = 0;
        int position = spinner.getSelectedItemPosition();
        TypedArray typedArray = this.getResources().obtainTypedArray(resource);
        for (int i = 0; i <= typedArray.length() - 1; i++) {
            if (position == i) {
                id = typedArray.getResourceId(i, 0);
            }
        }
        typedArray.recycle();
        return id;
    }

    private void setValueOfEnum(int id, int resource, Spinner spinner) {
        TypedArray typedArray = this.getResources().obtainTypedArray(resource);
        for (int i = 0; i <= typedArray.length() - 1; i++) {
            if (id == typedArray.getResourceId(i, 0)) {
                spinner.setSelection(i);
                break;
            }
        }
        typedArray.recycle();
    }
}