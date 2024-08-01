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

package de.domjos.unibuggermobile.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import java.util.Objects;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.SpinnerItem;
import de.domjos.unitrackerlibrary.custom.DropDown;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.services.ArrayHelper;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tools.Notifications;

/** @noinspection unchecked*/
public class ResolveDialog extends AbstractDialog {
    private EditText txtDescription;
    private DropDown<SpinnerItem> cmbState;
    private final String array;
    private final int position;

    public ResolveDialog(Activity activity, String array, int position, Issue<?> issue, IBugService<?> bugService, Object pid, boolean show, Runnable runnable, int notificationId) {
        super(activity, R.layout.resolve_dialog);

        this.array = array;
        this.position = position;

        this.setTitle(R.string.issues_context_solve);
        this.setOnSubmit(R.string.sys_save, (l, i) -> {
            try {
                String noteContent = txtDescription.getText().toString();
                if (!noteContent.isEmpty()) {
                    Note<?> note = new Note<>();
                    note.setDescription(noteContent);
                    note.setTitle(noteContent);
                    note.setState(10, "öffentlich");
                    //noinspection rawtypes
                    issue.getNotes().add((Note) note);
                }
                issue.setStatus(ArrayHelper.getIdOfEnum(activity, cmbState, array), Objects.requireNonNull(cmbState.getSelectedItem()).toString());

                IssueTask issueTask = new IssueTask(activity, bugService, pid, false, false, show, R.drawable.icon_issues);
                issueTask.setId(notificationId);
                issueTask.execute(issue).get();
                this.alertDialog.dismiss();
                runnable.run();
            } catch (Exception ex) {
                Notifications.printException(activity,  ex, R.mipmap.ic_launcher_round);
            }
        });
    }

    @Override
    protected void init(View view) {
        this.cmbState = view.findViewById(R.id.cmbStatus);
        cmbState.setAdapter(Helper.setDropDownAdapter(activity, array));
        try {
            if (position != -1) {
                cmbState.setSelection(position);
            }
        } catch (Exception ignored) {
        }
        this.txtDescription = view.findViewById(R.id.txtComment);
    }
}
