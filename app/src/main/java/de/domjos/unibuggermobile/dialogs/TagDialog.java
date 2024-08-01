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

import java.util.List;

import de.domjos.unibuggermobile.R;
import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.unitrackerlibrary.custom.DropDown;
import de.domjos.unitrackerlibrary.custom.DropDownAdapter;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.BaseDescriptionObject;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Tag;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.LoaderTask;
import de.domjos.unitrackerlibrary.tools.Notifications;

public class TagDialog extends AbstractDialog {
    private final IBugService<?> bugService;
    private final boolean show;
    private final Object pid;
    private EditText txtTags;

    public TagDialog(Activity activity, IBugService<?> bugService, boolean show, Object pid, List<BaseDescriptionObject> objects, int notificationId) {
        super(activity, R.layout.tag_dialog);

        this.bugService = bugService;
        this.show = show;
        this.pid = pid;

        this.setOnSubmit(R.string.sys_save, (dialogInterface, i) -> {
            try {
                String tags = txtTags.getText().toString();

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
                this.alertDialog.dismiss();
            } catch (Exception ex) {
                Notifications.printException(activity,  ex, R.mipmap.ic_launcher_round);
            }
        });

        this.setTitle(R.string.issues_general_tags);
    }

    @Override
    protected void init(View view) {
        final DropDown<String> cmbTags = view.findViewById(R.id.cmbTags);
        this.txtTags = view.findViewById(R.id.txtTags);

        DropDownAdapter<String> tagAdapter = new DropDownAdapter<>(activity);
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

        cmbTags.setOnItemSelectedListener(i ->{
            String content = txtTags.getText().toString().trim();
            String newVal = content.isEmpty() ? tagAdapter.getItem(i) : content + "; " + tagAdapter.getItem(i);
            txtTags.setText(newVal);
        });
    }
}
