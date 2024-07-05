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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.settings.Settings;
import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Tag;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.LoaderTask;

public class TagDialog extends DialogFragment {
    private static List<BaseDescriptionObject> objects;
    private static int notificationId;

    public static TagDialog newInstance(List<BaseDescriptionObject> objects, int notificationId) {
        TagDialog.objects = objects;
        TagDialog.notificationId = notificationId;

        return new TagDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** @noinspection rawtypes*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_dialog, container, false);
        Objects.requireNonNull(this.getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Spinner cmbTags = view.findViewById(R.id.cmbTags);
        final TextInputLayout txtTags = view.findViewById(R.id.txtTags);
        final EditText etTags = txtTags.getEditText();
        final MaterialButton cmdTags = view.findViewById(R.id.cmdTags);

        Activity activity = this.requireActivity();
        Settings settings = MainActivity.GLOBALS.getSettings(this.requireContext());
        Object pid = settings.getCurrentProjectId();
        boolean show = settings.showNotifications();
        IBugService bugService = Helper.getCurrentBugService(this.requireContext());

        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        cmbTags.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
        LoaderTask loaderTask = new LoaderTask(activity, bugService, show, LoaderTask.Type.Tags);
        loaderTask.after((AbstractTask.PostExecuteListener<List<Tag<?>>>) o -> {
            if(o != null) {
                for(Tag<?> tag : o) {
                    tagAdapter.add(tag.getTitle());
                }
            }
        });
        loaderTask.execute(pid);

        cmbTags.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                assert etTags != null;
                String content = etTags.getText().toString().trim();
                String newVal = content.isEmpty() ? tagAdapter.getItem(i) : content + "; " + tagAdapter.getItem(i);
                etTags.setText(newVal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        cmdTags.setOnClickListener(event -> {
            try {
                assert etTags != null;
                String tags = etTags.getText().toString();

                for(BaseDescriptionObject listObject : objects) {
                    IssueTask issueTask = new IssueTask(activity, bugService, pid, false, true, show, R.drawable.icon_issues);
                    issueTask.setId(notificationId);
                    List<Issue<?>> issues = issueTask.execute(((Issue<?>)listObject.getObject()).getId()).get();

                    if(issues!=null) {
                        if(!issues.isEmpty()) {
                            issues.get(0).setTags(tags);
                            issueTask = new IssueTask(activity, bugService, pid, false, false, show, R.drawable.icon_issues);
                            issueTask.execute(issues.get(0)).get();
                        }
                    }
                }
                dismiss();
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, activity);
            }
        });
        return view;
    }
}
