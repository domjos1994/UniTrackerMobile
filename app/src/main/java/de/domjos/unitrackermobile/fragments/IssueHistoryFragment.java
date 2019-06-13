/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.History;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.helper.Helper;
import de.domjos.unitrackermobile.helper.Validator;

public final class IssueHistoryFragment extends AbstractFragment {
    private Issue issue;
    private TableLayout tblCustomFields;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.issue_fragment_history, container, false);

        this.tblCustomFields = root.findViewById(R.id.tblHistory);

        this.updateUITrackerSpecific();
        this.initData();
        return root;
    }

    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue) descriptionObject;
    }

    @Override
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        return descriptionObject;
    }

    @Override
    public void manageControls(boolean editMode) {
    }

    @Override
    protected void initData() {
        if (this.issue != null) {
            if (this.getActivity() != null) {
                new Thread(() -> {
                    try {
                        IBugService bugService = Helper.getCurrentBugService(IssueHistoryFragment.this.getActivity());
                        if (this.issue.getId() != null) {
                            List objects;
                            try {
                                Object projectId = MainActivity.GLOBALS.getSettings(getContext()).getCurrentProjectId();
                                long project_id = 0l;
                                if (projectId != null) {
                                    project_id = Long.parseLong(String.valueOf(projectId));
                                }
                                Long id = (Long) this.issue.getId();
                                objects = bugService.getHistory(id, project_id);
                            } catch (Exception ex) {
                                Object projectId = MainActivity.GLOBALS.getSettings(getContext()).getCurrentProjectId();
                                String project_id = "";
                                if (projectId != null) {
                                    project_id = (String) projectId;
                                }
                                String id = String.valueOf(this.issue.getId());
                                objects = bugService.getHistory(id, project_id);
                            }

                            if (objects != null) {
                                for (Object object : objects) {
                                    History history = (History) object;

                                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 10);
                                    TableRow tableRow = new TableRow(IssueHistoryFragment.this.getActivity());
                                    tableRow.setLayoutParams(layoutParams);

                                    tableRow.addView(this.createTextView(history.getField()));
                                    tableRow.addView(this.createTextView(history.getUser()));
                                    Date dt = new Date();
                                    dt.setTime(history.getTime());
                                    tableRow.addView(this.createTextView(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.GERMAN).format(dt)));
                                    tableRow.addView(this.createTextView(history.getOldValue()));
                                    tableRow.addView(this.createTextView(history.getNewValue()));

                                    this.getActivity().runOnUiThread(() -> this.tblCustomFields.addView(tableRow, layoutParams));
                                }
                            }
                        }
                    } catch (Exception ex) {
                        this.getActivity().runOnUiThread(() -> MessageHelper.printException(ex, this.getActivity()));
                    }
                }).start();
            }
        }
    }

    private TextView createTextView(String text) {
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
        TextView textView = new TextView(this.getActivity());
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        return textView;
    }

    @Override
    public Validator initValidator() {
        return new Validator(getContext());
    }

    @Override
    public void updateUITrackerSpecific() {
    }
}
