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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import de.domjos.unibuggermobile.R;
import de.domjos.unitrackerlibrary.model.issues.Issue;

/** @noinspection rawtypes*/
public class BottomSheetIssue extends BottomSheetDialogFragment {
    public final static String TAG = "ModalBottomSheet";
    private Issue issue;

    private TextView lblSummary;

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

        this.lblSummary = view.findViewById(R.id.lblSummary);

        this.load(this.issue);
        return view;
    }

    public void load(Issue issue) {
        this.issue = issue;

        if(this.lblSummary != null) {
            this.lblSummary.setText(issue.getTitle());
        }
    }
}