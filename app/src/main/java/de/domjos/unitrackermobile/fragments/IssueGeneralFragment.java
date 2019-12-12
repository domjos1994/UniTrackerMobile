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

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Profile;
import de.domjos.unitrackerlibrary.model.issues.Tag;
import de.domjos.unitrackerlibrary.model.issues.User;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.LoaderTask;
import de.domjos.unitrackerlibrary.tasks.UserTask;
import de.domjos.unitrackerlibrary.tasks.VersionTask;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.customwidgets.tokenizer.CommaTokenizer;
import de.domjos.unitrackermobile.helper.ArrayHelper;
import de.domjos.unitrackermobile.helper.DateConverter;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueGeneralFragment extends AbstractFragment {
    private EditText txtIssueGeneralSummary, txtIssueGeneralDueDate;
    private TextView txtIssueGeneralSubmitted, txtIssueGeneralUpdated;
    private AutoCompleteTextView txtIssueGeneralCategory, txtIssueGeneralVersion,
            txtIssueGeneralTargetVersion, txtIssueGeneralFixedInVersion,
            txtIssueGeneralPlatform, txtIssueGeneralOs, txtIssueGeneralBuild;
    private Spinner spIssueGeneralView, spIssueGeneralSeverity, spIssueGeneralReproducibility;
    private Spinner spIssueGeneralPriority, spIssueGeneralStatus, spIssueGeneralResolution, spIssueGeneralHandler;
    private MultiAutoCompleteTextView txtIssueGeneralTags;
    private ImageButton cmdIssueGeneralSmartPhone, cmdIssueGeneralSummaryToDescription;
    private ArrayAdapter<User> userAdapter;

    private String priorityValueArray, statusValueArray, severityValueArray, resolutionValueArray;
    private TableRow rowIssueGeneralDueDate, rowIssueGeneralDates, rowIssueGeneralCategory,
            rowIssueGeneralVersion, rowIssueGeneralTargetVersion, rowIssueGeneralFixedInVersion,
            rowIssueGeneralTags;
    private TableRow rowIssueGeneralView, rowIssueGeneralSeverity, rowIssueGeneralReproducibility,
            rowIssueGeneralPriority, rowIssueGeneralStatus, rowIssueGeneralResolution,
            rowIssueGeneralHandler, rowIssueGeneralProfile;

    private View root;
    private Issue issue;
    private boolean editMode;
    private Object pid;
    private IBugService bugService;
    private IssueDescriptionsFragment issueDescriptionsFragment;

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

        this.txtIssueGeneralPlatform = this.root.findViewById(R.id.txtIssueGeneralPlatform);
        this.txtIssueGeneralOs = this.root.findViewById(R.id.txtIssueGeneralOS);
        this.txtIssueGeneralBuild = this.root.findViewById(R.id.txtIssueGeneralOsBuild);

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
        this.rowIssueGeneralProfile = this.root.findViewById(R.id.rowIssueGeneralProfile);
        this.cmdIssueGeneralSmartPhone = this.root.findViewById(R.id.cmdIssueGeneralSmartPhone);


        try {
            if (this.getContext() != null && this.getActivity() != null) {
                this.userAdapter = new ArrayAdapter<>(this.getContext(), R.layout.spinner_item);
                this.spIssueGeneralHandler.setAdapter(this.userAdapter);
                this.userAdapter.notifyDataSetChanged();

                ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1);
                this.txtIssueGeneralTags.setAdapter(tagAdapter);
                this.txtIssueGeneralTags.setTokenizer(new CommaTokenizer());
                tagAdapter.notifyDataSetChanged();


                if (this.getActivity() != null) {
                    boolean show = MainActivity.GLOBALS.getSettings(this.getActivity()).showNotifications();
                    UserTask userTask = new UserTask(this.getActivity(), this.bugService, this.pid, false, show, R.drawable.ic_person_black_24dp);
                    List<User> users = userTask.execute(0).get();
                    users.add(0, new User());

                    LoaderTask loaderTask = new LoaderTask(this.getActivity(), this.bugService, show, LoaderTask.Type.Tags);
                    Object result = loaderTask.execute(this.pid).get();
                    if (result instanceof List) {
                        List lst = (List) result;
                        for (Object item : lst) {
                            if (item instanceof Tag) {
                                tagAdapter.add(((Tag) item).getTitle());
                            }
                        }
                    }

                    loaderTask = new LoaderTask(this.getActivity(), this.bugService, show, LoaderTask.Type.Profiles);
                    List<String> platform = new LinkedList<>();
                    List<String> os = new LinkedList<>();
                    List<String> build = new LinkedList<>();

                    result = loaderTask.execute(0).get();
                    List<Profile> profiles = new LinkedList<>();
                    if (result instanceof List) {
                        List lst = (List) result;
                        for (Object object : lst) {
                            if (object instanceof Profile) {
                                profiles.add((Profile) object);
                            }
                        }
                    }
                    for (Object object : profiles) {
                        if (object instanceof Profile) {
                            Profile profile = (Profile) object;
                            if (!profile.getPlatform().isEmpty()) {
                                if (!platform.contains(profile.getPlatform())) {
                                    platform.add(profile.getPlatform());
                                }
                            }
                            if (!profile.getOs().isEmpty()) {
                                if (!os.contains(profile.getOs())) {
                                    os.add(profile.getOs());
                                }
                            }
                            if (!profile.getOs_build().isEmpty()) {
                                if (!build.contains(profile.getOs_build())) {
                                    build.add(profile.getOs_build());
                                }
                            }
                        }
                    }

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
                    this.txtIssueGeneralPlatform.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, platform));
                    this.txtIssueGeneralOs.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, os));
                    this.txtIssueGeneralBuild.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, build));
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getActivity());
        }
        this.spIssueGeneralResolution = this.root.findViewById(R.id.spIssueGeneralResolution);
        this.txtIssueGeneralSubmitted = this.root.findViewById(R.id.txtIssueGeneralSubmitDate);
        this.txtIssueGeneralUpdated = this.root.findViewById(R.id.txtIssueGeneralLastUpdated);
        this.txtIssueGeneralDueDate = this.root.findViewById(R.id.txtIssueGeneralDueDate);

        this.txtIssueGeneralVersion = this.root.findViewById(R.id.txtIssueGeneralVersion);
        this.txtIssueGeneralTargetVersion = this.root.findViewById(R.id.txtIssueGeneralTargetVersion);
        this.txtIssueGeneralFixedInVersion = this.root.findViewById(R.id.txtIssueGeneralFixedInVersion);
        this.cmdIssueGeneralSummaryToDescription = this.root.findViewById(R.id.cmdIssueGeneralSummaryToDescription);

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
    @SuppressWarnings("unchecked")
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            issue.setTitle(this.txtIssueGeneralSummary.getText().toString());
            issue.setCategory(this.txtIssueGeneralCategory.getText().toString());
            issue.setState(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueGeneralView, "issues_general_view_values"), this.spIssueGeneralView.getSelectedItem().toString());
            if (this.rowIssueGeneralSeverity.getVisibility() == View.VISIBLE) {
                issue.setSeverity(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueGeneralSeverity, this.severityValueArray), this.spIssueGeneralSeverity.getSelectedItem().toString());
            }
            issue.setReproducibility(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueGeneralSeverity, "issues_general_reproducibility_values"), this.spIssueGeneralReproducibility.getSelectedItem().toString());
            issue.setPriority(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueGeneralPriority, this.priorityValueArray), this.getValuesFromSpinner(this.spIssueGeneralPriority));
            issue.setStatus(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueGeneralStatus, this.statusValueArray), this.getValuesFromSpinner(this.spIssueGeneralStatus));
            issue.setResolution(ArrayHelper.getIdOfEnum(this.getContext(), this.spIssueGeneralResolution, this.resolutionValueArray), this.getValuesFromSpinner(this.spIssueGeneralResolution));
            issue.setVersion(this.txtIssueGeneralVersion.getText().toString());
            issue.setTargetVersion(this.txtIssueGeneralTargetVersion.getText().toString());
            issue.setFixedInVersion(this.txtIssueGeneralFixedInVersion.getText().toString());
            issue.setHandler((User) this.spIssueGeneralHandler.getSelectedItem());
            Profile profile = new Profile();
            profile.setPlatform(this.txtIssueGeneralPlatform.getText().toString());
            profile.setOs(this.txtIssueGeneralOs.getText().toString());
            profile.setOs_build(this.txtIssueGeneralBuild.getText().toString());
            issue.setProfile(profile);

            issue.setTags(this.txtIssueGeneralTags.getText().toString());

            try {
                if (!this.txtIssueGeneralDueDate.getText().toString().equals("")) {
                    issue.setDueDate(DateConverter.convertStringToDate(this.txtIssueGeneralDueDate.getText().toString(), this.getContext()));
                }
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getActivity());
            }
        }
        return issue;
    }

    private String getValuesFromSpinner(Spinner spinner) {
        if(spinner.getSelectedItem()!=null) {
            return spinner.getSelectedItem().toString();
        }
        return "";
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
            this.txtIssueGeneralPlatform.setEnabled(editMode);
            this.txtIssueGeneralBuild.setEnabled(editMode);
            this.txtIssueGeneralOs.setEnabled(editMode);
            this.cmdIssueGeneralSmartPhone.setEnabled(editMode);
            this.cmdIssueGeneralSummaryToDescription.setEnabled(editMode);
        }
    }

    @Override
    protected void initData() {
        if (this.issue != null) {
            this.txtIssueGeneralSummary.setText(this.issue.getTitle());
            this.txtIssueGeneralCategory.setText(this.issue.getCategory());
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.issue.getState().getKey().toString()), "issues_general_view_values", spIssueGeneralView);
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.issue.getSeverity().getKey().toString()), this.severityValueArray, spIssueGeneralSeverity);
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.issue.getReproducibility().getKey().toString()), "issues_general_reproducibility_values", spIssueGeneralReproducibility);
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.issue.getStatus().getKey().toString()), this.statusValueArray, spIssueGeneralStatus);
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.issue.getResolution().getKey().toString()), this.resolutionValueArray, spIssueGeneralResolution);
            ArrayHelper.setValueOfEnum(this.getContext(), Integer.parseInt(this.issue.getPriority().getKey().toString()), this.priorityValueArray, spIssueGeneralPriority);


            if (this.issue.getHandler() != null) {
                for (int i = 0; i <= this.userAdapter.getCount() - 1; i++) {
                    User user = this.userAdapter.getItem(i);
                    if (user != null) {
                        if (user.getRealName().equals(this.issue.getHandler().getRealName())) {
                            this.spIssueGeneralHandler.setSelection(i);
                            break;
                        }
                    }
                }
            }

            this.txtIssueGeneralVersion.setText(this.issue.getVersion());
            this.txtIssueGeneralTargetVersion.setText(this.issue.getTargetVersion());
            this.txtIssueGeneralFixedInVersion.setText(this.issue.getFixedInVersion());
            if (this.issue.getProfile() != null) {
                this.txtIssueGeneralPlatform.setText(this.issue.getProfile().getPlatform());
                this.txtIssueGeneralOs.setText(this.issue.getProfile().getOs());
                this.txtIssueGeneralBuild.setText(this.issue.getProfile().getOs_build());
            } else {
                this.txtIssueGeneralPlatform.setText("");
                this.txtIssueGeneralOs.setText("");
                this.txtIssueGeneralBuild.setText("");
            }

            if (this.issue.getSubmitDate() != null) {
                this.txtIssueGeneralSubmitted.setText(DateConverter.convertDateTimeToString(this.issue.getSubmitDate(), this.getContext()));

            }
            if (this.issue.getLastUpdated() != null) {
                this.txtIssueGeneralUpdated.setText(DateConverter.convertDateTimeToString(this.issue.getLastUpdated(), this.getContext()));
            }
            if (this.issue.getDueDate() != null) {
                this.txtIssueGeneralDueDate.setText(DateConverter.convertDateTimeToString(this.issue.getDueDate(), this.getContext()));
            }

            this.cmdIssueGeneralSmartPhone.setOnClickListener(v -> {
                String smartPhone = "SmartPhone";
                String android = "Android";

                this.txtIssueGeneralPlatform.setText(smartPhone);
                this.txtIssueGeneralOs.setText(android);
                this.txtIssueGeneralBuild.setText(String.valueOf(Build.VERSION.RELEASE));
            });

            this.cmdIssueGeneralSummaryToDescription.setOnClickListener(v -> this.issueDescriptionsFragment.setDescription(this.txtIssueGeneralSummary.getText().toString()));
        }
    }

    public void setDescriptionFragment(IssueDescriptionsFragment issueDescriptionsFragment) {
        this.issueDescriptionsFragment = issueDescriptionsFragment;
    }

    @Override
    public Validator initValidator() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();
        Validator validator = new Validator(this.getContext());
        if (this.root != null) {
            validator.addEmptyValidator(this.txtIssueGeneralSummary);

            if (authentication.getTracker() == Authentication.Tracker.MantisBT) {
                validator.addEmptyValidator(this.txtIssueGeneralCategory);
            }
        }
        return validator;
    }

    @Override
    public void updateUITrackerSpecific() {
        Authentication authentication = MainActivity.GLOBALS.getSettings(this.getContext()).getCurrentAuthentication();
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
        if (this.bugService.getPermissions().listProfiles()) {
            this.rowIssueGeneralProfile.setVisibility(View.VISIBLE);
        } else {
            this.rowIssueGeneralProfile.setVisibility(View.GONE);
        }
        this.resolutionValueArray = "issues_general_resolution_values";

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
                this.txtIssueGeneralCategory.setOnFocusChangeListener((v, hasFocus) -> {
                    if(hasFocus) {
                        this.txtIssueGeneralCategory.showDropDown();
                    }
                });
                this.priorityValueArray = "issues_general_priority_mantisbt_values";
                this.statusValueArray = "issues_general_status_mantisbt_values";
                this.severityValueArray = "issues_general_severity_mantisbt_values";
                break;
            case YouTrack:
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTags.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_youtrack_values";
                this.statusValueArray = "issues_general_status_youtrack_values";
                this.severityValueArray = "issues_general_severity_youtrack_values";
                break;
            case RedMine:
                this.rowIssueGeneralDueDate.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralCategory.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTargetVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralView.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_redmine_values";
                this.statusValueArray = "issues_general_status_redmine_values";
                this.severityValueArray = "issues_general_severity_redmine_values";
                break;
            case Bugzilla:
                this.rowIssueGeneralDueDate.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.rowIssueGeneralResolution.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                this.rowIssueGeneralVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTags.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_bugzilla_values";
                this.statusValueArray = "issues_general_status_bugzilla_values";
                this.severityValueArray = "issues_general_severity_bugzilla_values";
                this.resolutionValueArray = "issues_general_resolution_bugzilla_values";
                break;
            case Github:
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                break;
            case Jira:
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDueDate.setVisibility(View.VISIBLE);
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTags.setVisibility(View.VISIBLE);
                this.rowIssueGeneralVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralFixedInVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_jira_values";
                this.statusValueArray = "issues_general_status_jira_values";
                this.severityValueArray = "issues_general_severity_jira_values";
                break;
            case PivotalTracker:
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.rowIssueGeneralTags.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_jira_values";
                this.statusValueArray = "issues_general_status_pivotal_values";
                this.severityValueArray = "issues_general_severity_pivotal_values";
                break;
            case OpenProject:
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDueDate.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.rowIssueGeneralCategory.setVisibility(View.VISIBLE);
                this.rowIssueGeneralVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_openproject_values";
                this.statusValueArray = "issues_general_status_openproject_values";
                this.severityValueArray = "issues_general_severity_openproject_values";
                break;
            case Backlog:
                this.rowIssueGeneralDates.setVisibility(View.VISIBLE);
                this.rowIssueGeneralDueDate.setVisibility(View.VISIBLE);
                this.rowIssueGeneralStatus.setVisibility(View.VISIBLE);
                this.rowIssueGeneralPriority.setVisibility(View.VISIBLE);
                this.rowIssueGeneralSeverity.setVisibility(View.VISIBLE);
                this.rowIssueGeneralResolution.setVisibility(View.VISIBLE);
                this.rowIssueGeneralCategory.setVisibility(View.VISIBLE);
                this.rowIssueGeneralVersion.setVisibility(View.VISIBLE);
                this.rowIssueGeneralHandler.setVisibility(View.VISIBLE);
                this.priorityValueArray = "issues_general_priority_backlog_values";
                this.statusValueArray = "issues_general_status_backlog_values";
                this.severityValueArray = "issues_general_severity_backlog_values";
                break;
            case Local:
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
                this.priorityValueArray = "issues_general_priority_mantisbt_values";
                this.statusValueArray = "issues_general_status_mantisbt_values";
                this.severityValueArray = "issues_general_severity_mantisbt_values";
                break;
        }

        this.spIssueGeneralPriority.setAdapter(Helper.setAdapter(this.getContext(), this.priorityValueArray));
        this.spIssueGeneralView.setAdapter(Helper.setAdapter(this.getContext(), "issues_general_view_values"));
        this.spIssueGeneralResolution.setAdapter(Helper.setAdapter(this.getContext(), this.resolutionValueArray));
        this.spIssueGeneralStatus.setAdapter(Helper.setAdapter(this.getContext(), this.statusValueArray));
        this.spIssueGeneralReproducibility.setAdapter(Helper.setAdapter(this.getContext(), "issues_general_reproducibility_values"));
        this.spIssueGeneralSeverity.setAdapter(Helper.setAdapter(this.getContext(), this.severityValueArray));
    }

    private void initCategories() {
        if (this.getContext() != null && this.getActivity() != null) {
            try {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1);

                boolean show = MainActivity.GLOBALS.getSettings(this.getActivity()).showNotifications();
                LoaderTask loaderTask = new LoaderTask(this.getActivity(), this.bugService, show, LoaderTask.Type.Categories);
                Object categoriesObjectList = loaderTask.execute(this.pid).get();
                if (categoriesObjectList instanceof List) {
                    List lst = (List) categoriesObjectList;
                    for (Object categoryObject : lst) {
                        if (categoryObject instanceof String) {
                            arrayAdapter.add((String) categoryObject);
                        }
                    }
                }
                this.txtIssueGeneralCategory.setAdapter(arrayAdapter);
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getActivity());
            }
        }
    }

    private void initVersions() {
        if (this.getContext() != null && this.getActivity() != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1);
            arrayAdapter.add("");
            try {
                if (this.bugService != null) {
                    List<Version> versions = new VersionTask(this.getActivity(), this.bugService, this.pid, false, MainActivity.GLOBALS.getSettings(this.getContext()).showNotifications(), "versions", R.drawable.ic_update_black_24dp).execute(0).get();
                    for (Version version : versions) {
                        arrayAdapter.add(version.getTitle());
                    }
                }
            } catch (Exception ex) {
                this.getActivity().runOnUiThread(() -> MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.getActivity()));
            }
            this.txtIssueGeneralVersion.setAdapter(arrayAdapter);
            this.txtIssueGeneralTargetVersion.setAdapter(arrayAdapter);
            this.txtIssueGeneralFixedInVersion.setAdapter(arrayAdapter);
        }
    }
}