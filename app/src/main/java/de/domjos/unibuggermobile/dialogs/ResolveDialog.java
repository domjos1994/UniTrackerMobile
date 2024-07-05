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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.services.ArrayHelper;
import de.domjos.unitrackerlibrary.tasks.IssueTask;

public class ResolveDialog extends DialogFragment {
    private static final String ARRAY = "array";
    private static final String POSITION = "position";
    /** @noinspection rawtypes*/
    private static Issue issue = null;
    private static Runnable runnable = null;
    private static final String NOTIFICATION_ID = "notification_id";

    public static ResolveDialog newInstance(
            String array, int position, Issue<?> issue, Runnable runnable, int notificationId) {

        Bundle arguments = new Bundle();
        arguments.putString(ResolveDialog.ARRAY, array);
        arguments.putInt(ResolveDialog.POSITION, position);
        arguments.putInt(NOTIFICATION_ID, notificationId);
        ResolveDialog.issue = issue;
        ResolveDialog.runnable = runnable;

        ResolveDialog resolveDialog = new ResolveDialog();
        resolveDialog.setArguments(arguments);
        resolveDialog.setCancelable(true);
        return resolveDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** @noinspection rawtypes, unchecked */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.resolve_dialog, container, false);
        Objects.requireNonNull(this.getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);

        Settings settings = MainActivity.GLOBALS.getSettings(this.requireContext());
        Object pid = settings.getCurrentProjectId();
        boolean show = settings.showNotifications();
        IBugService bugService = Helper.getCurrentBugService(this.requireContext());
        assert this.getArguments() != null;
        String array = this.getArguments().getString(ResolveDialog.ARRAY);
        int position = this.getArguments().getInt(ResolveDialog.POSITION);
        int notificationId = this.getArguments().getInt(ResolveDialog.NOTIFICATION_ID);

        final Spinner cmbState = view.findViewById(R.id.cmbStatus);
        cmbState.setAdapter(Helper.setAdapter(this.requireContext(), array));
        try {
            if (position != -1) {
                cmbState.setSelection(position);
            }
        } catch (Exception ignored) {
        }
        final TextInputLayout txtDescription = view.findViewById(R.id.txtComment);
        final EditText etDescription = txtDescription.getEditText();
        final MaterialButton cmdSave = view.findViewById(R.id.cmdResolve);

        cmdSave.setOnClickListener(v -> {
            try {
                assert etDescription != null;
                String noteContent = etDescription.getText().toString();
                if (!noteContent.isEmpty()) {
                    Note<?> note = new Note<>();
                    note.setDescription(noteContent);
                    note.setTitle(noteContent);
                    note.setState(10, "öffentlich");
                    issue.getNotes().add(note);
                }
                issue.setStatus(ArrayHelper.getIdOfEnum(this.requireContext(), cmbState, array), cmbState.getSelectedItem().toString());

                IssueTask issueTask = new IssueTask(this.requireActivity(), bugService, pid, false, false, show, R.drawable.icon_issues);
                issueTask.setId(notificationId);
                issueTask.execute(issue).get();
                this.dismiss();
                runnable.run();
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.requireContext());
            }
        });

        return view;
    }
}
