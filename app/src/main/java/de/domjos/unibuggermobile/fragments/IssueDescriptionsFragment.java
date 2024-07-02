/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
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

package de.domjos.unibuggermobile.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TableRow;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.User;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.UserTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.adapter.SuggestionAdapter;
import de.domjos.customwidgets.tokenizer.SpecialTokenizer;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.customwidgets.utils.Validator;

/**
 * A placeholder fragment containing a simple view.
 * @noinspection rawtypes, rawtypes
 */
public final class IssueDescriptionsFragment extends AbstractFragment {
    private MultiAutoCompleteTextView txtIssueDescriptionsDescription, txtIssueDescriptionsSteps, txtIssueDescriptionsAdditional;
    private TableRow rowIssueDescriptionsSteps, rowIssueDescriptionsAdditional;

    private View root;
    private Issue<?> issue;
    private boolean editMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_descriptions, container, false);

        this.rowIssueDescriptionsSteps = this.root.findViewById(R.id.rowDescriptionsSteps);
        this.rowIssueDescriptionsAdditional = this.root.findViewById(R.id.rowDescriptionsAdditional);

        this.txtIssueDescriptionsDescription = this.root.findViewById(R.id.txtIssueDescriptionsDescription);
        this.txtIssueDescriptionsSteps = this.root.findViewById(R.id.txtIssueDescriptionsSteps);
        this.txtIssueDescriptionsAdditional = this.root.findViewById(R.id.txtIssueDescriptionsAdditional);

        if(this.getContext()!=null) {
            List<User<?>> users = new LinkedList<>();
            List<Issue<?>> issues = new LinkedList<>();
            try {
                IBugService<?> bugService = Helper.getCurrentBugService(this.getActivity());
                Object pid = MainActivity.GLOBALS.getSettings(this.getActivity()).getCurrentProjectId();
                boolean show = MainActivity.GLOBALS.getSettings(this.getActivity()).showNotifications();

                UserTask userTask = new UserTask(this.getActivity(), bugService, pid, false, show, R.drawable.icon_users);
                userTask.setId(notificationId);
                users = userTask.execute(0).get();

                IssueTask issueTask = new IssueTask(this.getActivity(), bugService, pid, false, false, show, R.drawable.icon_issues);
                issueTask.setId(notificationId);
                issues = issueTask.execute(0).get();
            } catch (Exception ignored) {}

            SuggestionAdapter descriptionAdapter = fillAdapter(users, issues, this.getActivity());
            this.txtIssueDescriptionsDescription.setAdapter(descriptionAdapter);
            this.txtIssueDescriptionsDescription.setTokenizer(new SpecialTokenizer());
            descriptionAdapter.notifyDataSetChanged();

            SuggestionAdapter stepsAdapter = fillAdapter(users, issues, this.getActivity());
            this.txtIssueDescriptionsDescription.setAdapter(stepsAdapter);
            this.txtIssueDescriptionsDescription.setTokenizer(new SpecialTokenizer());
            stepsAdapter.notifyDataSetChanged();

            SuggestionAdapter additionalAdapter = fillAdapter(users, issues, this.getActivity());
            this.txtIssueDescriptionsDescription.setAdapter(additionalAdapter);
            this.txtIssueDescriptionsDescription.setTokenizer(new SpecialTokenizer());
            additionalAdapter.notifyDataSetChanged();
        }

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        this.initValidator();
        return this.root;
    }

    static SuggestionAdapter fillAdapter(List<User<?>> users, List<Issue<?>> issues, Activity activity) {
        List<DescriptionObject<?>> descriptionObjects = new LinkedList<>();
        for(User<?> user : users) {
            DescriptionObject<?> descriptionObject = new DescriptionObject<>();
            descriptionObject.setTitle("@" + user.getTitle());
            descriptionObject.setDescription(user.getRealName());
            descriptionObjects.add(descriptionObject);
        }
        for(Issue<?> issue : issues) {
            DescriptionObject<?> descriptionObject = new DescriptionObject<>();
            descriptionObject.setTitle("#" + issue.getId());
            descriptionObject.setDescription(issue.getTitle());
            descriptionObjects.add(descriptionObject);
        }
        return new SuggestionAdapter(activity, descriptionObjects);
    }

    /** @noinspection rawtypes*/
    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue<?>) descriptionObject;
    }

    @Override
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue<?> issue = (Issue<?>) descriptionObject;

        if (this.root != null) {
            issue.setDescription(this.txtIssueDescriptionsDescription.getText().toString());
            issue.setStepsToReproduce(this.txtIssueDescriptionsSteps.getText().toString());
            issue.setAdditionalInformation(this.txtIssueDescriptionsAdditional.getText().toString());
        }
        return issue;
    }

    @Override
    public void manageControls(boolean editMode) {
        this.editMode = editMode;

        if (this.root != null) {
            this.txtIssueDescriptionsDescription.setEnabled(this.editMode);
            this.txtIssueDescriptionsAdditional.setEnabled(this.editMode);
            this.txtIssueDescriptionsSteps.setEnabled(this.editMode);
        }
    }

    public void setDescription(String text) {
        this.txtIssueDescriptionsDescription.setText(text);
    }

    @Override
    protected void initData() {
        if (this.issue != null) {
            this.txtIssueDescriptionsDescription.setText(this.issue.getDescription());
            this.txtIssueDescriptionsSteps.setText(this.issue.getStepsToReproduce());
            this.txtIssueDescriptionsAdditional.setText(this.issue.getAdditionalInformation());
        }
    }

    @Override
    protected Validator initValidator() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();
        this.validator = new Validator(this.getContext(), R.mipmap.ic_launcher_round);
        if (this.root != null) {
            if (authentication.getTracker() != Authentication.Tracker.Bugzilla) {
                this.validator.addEmptyValidator(this.txtIssueDescriptionsDescription);
            }
        }
        return this.validator;
    }

    @Override
    public void updateUITrackerSpecific() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();
        this.rowIssueDescriptionsAdditional.setVisibility(View.GONE);
        this.rowIssueDescriptionsSteps.setVisibility(View.GONE);
        this.txtIssueDescriptionsAdditional.setHint(R.string.issues_descriptions_additional);

        switch (authentication.getTracker()) {
            case MantisBT:
            case Local:
                this.rowIssueDescriptionsAdditional.setVisibility(View.VISIBLE);
                this.rowIssueDescriptionsSteps.setVisibility(View.VISIBLE);
                break;
            case Bugzilla:
                this.txtIssueDescriptionsDescription.setText(this.getString(R.string.issues_descriptions_website));
                break;
            case Jira:
                this.rowIssueDescriptionsAdditional.setVisibility(View.VISIBLE);
                this.txtIssueDescriptionsAdditional.setHint(R.string.issues_descriptions_environment);
                break;
        }
    }
}