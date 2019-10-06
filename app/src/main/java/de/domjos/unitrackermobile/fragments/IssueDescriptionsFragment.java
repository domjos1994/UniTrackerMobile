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

package de.domjos.unitrackermobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.helper.Validator;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueDescriptionsFragment extends AbstractFragment {
    private EditText txtIssueDescriptionsDescription, txtIssueDescriptionsSteps, txtIssueDescriptionsAdditional;
    private TableRow rowIssueDescriptionsSteps, rowIssueDescriptionsAdditional;

    private View root;
    private Issue issue;
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

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        this.initValidator();
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
    public Validator initValidator() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();
        Validator validator = new Validator(this.getContext());
        if (this.root != null) {
            if (authentication.getTracker() != Authentication.Tracker.Bugzilla) {
                validator.addEmptyValidator(this.txtIssueDescriptionsDescription);
            }
        }
        return validator;
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