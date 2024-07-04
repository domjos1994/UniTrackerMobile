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

package de.domjos.unibuggermobile.sheets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.IssueActivity;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unitrackerlibrary.model.issues.Issue;

/** @noinspection rawtypes, unchecked */
public class BottomSheetIssue extends BottomSheetDialogFragment {
    public final static String TAG = "ModalBottomSheet";
    private Issue issue;

    private TableRow rowDate;
    private TableRow rowStatus;
    private TableRow rowSeverity;
    private TableRow rowProductVersion;
    private TableRow rowTargetVersion;
    private TableRow rowFixedInVersion;
    private TableRow rowPlatform;
    private TableRow rowHandler;
    private TableRow rowStepsToReproduce;
    private TableRow rowAdditional;

    private TextView lblSummary;
    private TextView lblDate;
    private TextView lblStatus;
    private TextView lblSeverity;
    private TextView lblProductVersion;
    private TextView lblTargetVersion;
    private TextView lblFixedInVersion;
    private TextView lblPlatform;
    private TextView lblOs;
    private TextView lblVersion;
    private TextView lblHandler;
    private TextView lblStepsToReproduce;
    private TextView lblAdditional;
    private TextView lblDescription;
    private SimpleDateFormat simpleDateFormat;

    public BottomSheetIssue() {
        super();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.bottom_sheet_main, container, false);

        this.rowDate = view.findViewById(R.id.rowDate);
        this.rowStatus = view.findViewById(R.id.rowStatus);
        this.rowSeverity = view.findViewById(R.id.rowSeverity);
        this.rowProductVersion = view.findViewById(R.id.rowProductVersion);
        this.rowTargetVersion = view.findViewById(R.id.rowTargetVersion);
        this.rowFixedInVersion = view.findViewById(R.id.rowFixedInVersion);
        this.rowPlatform = view.findViewById(R.id.rowPlatform);
        this.rowHandler = view.findViewById(R.id.rowHandler);
        this.rowStepsToReproduce = view.findViewById(R.id.rowStepsToReproduce);
        this.rowAdditional = view.findViewById(R.id.rowAdditional);

        this.lblSummary = view.findViewById(R.id.lblSummary);
        this.lblDate = view.findViewById(R.id.lblDate);
        this.lblStatus = view.findViewById(R.id.lblStatus);
        this.lblSeverity = view.findViewById(R.id.lblSeverity);
        this.lblProductVersion = view.findViewById(R.id.lblProductVersion);
        this.lblTargetVersion = view.findViewById(R.id.lblTargetVersion);
        this.lblFixedInVersion = view.findViewById(R.id.lblFixedInVersion);
        this.lblHandler = view.findViewById(R.id.lblHandler);
        this.lblPlatform = view.findViewById(R.id.lblPlatform);
        this.lblOs = view.findViewById(R.id.lblOs);
        this.lblVersion = view.findViewById(R.id.lblVersion);
        this.lblStepsToReproduce = view.findViewById(R.id.lblStepsToReproduce);
        this.lblAdditional = view.findViewById(R.id.lblAdditional);
        this.lblDescription = view.findViewById(R.id.lblDescription);
        Button cmdEdit = view.findViewById(R.id.cmdEdit);

        Activity activity = this.requireActivity();
        String format = activity.getString(de.domjos.customwidgets.R.string.date_format);
        this.simpleDateFormat = new SimpleDateFormat(
                format,
                Locale.getDefault()
        );

        cmdEdit.setOnClickListener((e) -> {
            if(this.issue != null) {
                Intent intent = new Intent(activity, IssueActivity.class);
                intent.putExtra("id", String.valueOf(this.issue.getId()));
                intent.putExtra("pid", String.valueOf(MainActivity.GLOBALS.getSettings(activity).getCurrentProjectId()));
                this.requireActivity().startActivity(intent);
            }
        });

        this.load(this.issue);
        return view;
    }

    /** @noinspection unchecked*/
    public void load(Issue issue) {
        this.issue = issue;

        if(this.lblSummary != null) {
            this.lblSummary.setText(issue.getTitle());
            this.lblDescription.setText(issue.getDescription());

            if(issue.getLastUpdated() != null) {
                this.rowDate.setVisibility(View.VISIBLE);
                this.lblDate.setText(this.simpleDateFormat.format(issue.getLastUpdated()));
            } else {
                this.rowDate.setVisibility(View.GONE);
            }

            this.setData(this.rowStatus, this.lblStatus, issue.getStatus());
            this.setData(this.rowSeverity, this.lblSeverity, issue.getSeverity());
            this.setData(this.rowProductVersion, this.lblProductVersion, issue.getVersion());
            this.setData(this.rowTargetVersion, this.lblTargetVersion, issue.getTargetVersion());
            this.setData(this.rowFixedInVersion, this.lblFixedInVersion, issue.getFixedInVersion());
            this.setData(this.rowStepsToReproduce, this.lblStepsToReproduce, issue.getStepsToReproduce());
            this.setData(this.rowAdditional, this.lblAdditional, issue.getAdditionalInformation());
            if(issue.getHandler() != null) {
                this.lblHandler.setText(issue.getHandler().getTitle());
                this.rowHandler.setVisibility(View.VISIBLE);
            } else {
                this.rowHandler.setVisibility(View.GONE);
            }
            if(issue.getProfile() != null) {
                this.lblPlatform.setText(issue.getProfile().getPlatform());
                this.lblOs.setText(issue.getProfile().getOs());
                this.lblVersion.setText(issue.getProfile().getOs_build());
                this.rowPlatform.setVisibility(View.VISIBLE);
            } else {
                this.rowPlatform.setVisibility(View.GONE);
            }
        }
    }

    private void setData(TableRow row, TextView lbl, String data) {
        if(data != null) {
            if(!data.isEmpty()) {
                lbl.setText(data);
                row.setVisibility(View.VISIBLE);
            } else {
                row.setVisibility(View.GONE);
            }
        } else {
            row.setVisibility(View.GONE);
        }
    }

    private void setData(TableRow row, TextView lbl, Map.Entry<Integer, String> data) {
        if(data != null) {
            lbl.setText(data.getValue());
            row.setVisibility(View.VISIBLE);
        } else {
            row.setVisibility(View.GONE);
        }
    }
}