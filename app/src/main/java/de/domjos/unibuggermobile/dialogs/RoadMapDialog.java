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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.domjos.unitrackerlibrary.custom.SwipeRefreshDeleteList;
import de.domjos.unitrackerlibrary.model.BaseDescriptionObject;
import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.VersionTask;
import de.domjos.unitrackerlibrary.tools.Notifications;


public class RoadMapDialog extends AbstractDialog {
    private static final String RoadMap = "RoadMap";
    private static final String ChangeLog = "ChangeLog";

    private ProgressBar pbState;
    private TextView lblPercentage;
    private SwipeRefreshDeleteList lvIssues;

    private final Object pid;
    private final Object vid;
    private final boolean roadMap;

    public RoadMapDialog(Activity activity, boolean roadMap, Object pid, Object vid) {
        super(activity, R.layout.roadmap_dialog);

        this.pid = pid;
        this.vid = vid;
        this.roadMap = roadMap;

        if(this.roadMap) {
            this.setTitle(RoadMapDialog.RoadMap);
        } else {
            this.setTitle(RoadMapDialog.ChangeLog);
        }
        this.initWithArguments();
    }

    private void initWithArguments() {
        try {
            boolean changeLog = !this.roadMap;
            boolean not = MainActivity.GLOBALS.getSettings(this.activity).showNotifications();
            Activity act = this.activity;
            IBugService<?>  bugs = Helper.getCurrentBugService(this.activity);
            int icon = R.drawable.icon_versions;

            VersionTask versionTask = new VersionTask(act, bugs, pid, false, not, "versions", icon);
            List<Version<?>> versions = versionTask.execute(0).get();
            String version_name = "";
            for(Version<?> version : versions) {
                if(version.getId() instanceof Long v) {
                    Long tmp = Long.parseLong(String.valueOf(vid));
                    if(v.equals(tmp)) {
                        version_name = version.getTitle();
                        break;
                    }
                } else if(version.getId() instanceof String v) {
                    String tmp = String.valueOf(vid);
                    if(v.equals(tmp)) {
                        version_name = version.getTitle();
                        break;
                    }
                }
            }
            String version = version_name;

            IssueTask issueTask = new IssueTask(act, bugs, pid, false, false, not, icon);
            issueTask.after((AbstractTask.PostExecuteListener<List<Issue<?>>>) issues -> {
                int max = 0, resolved = 0;
                for(Issue<?> issue : issues) {
                    boolean item = Boolean.parseBoolean(issue.getHints().get("resolved"));
                    try {
                        IssueTask oneDetailed = new IssueTask(act, bugs, pid, false, true, not, icon);
                        issue = Objects.requireNonNull(oneDetailed.execute(issue.getId()).get()).get(0);
                        if(changeLog) {
                            if(issue.getFixedInVersion() != null) {
                                if(issue.getFixedInVersion().trim().equals(version.trim())) {
                                    BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                                    baseDescriptionObject.setTitle(issue.getTitle());
                                    baseDescriptionObject.setDescription(issue.getDescription());
                                    baseDescriptionObject.setObject(issue);
                                    baseDescriptionObject.setState(item);
                                    this.lvIssues.getAdapter().add(baseDescriptionObject);
                                }
                            }
                        } else {
                            if(issue.getTargetVersion() != null) {
                                if(issue.getTargetVersion().trim().equals(version.trim())) {
                                    max++;
                                    resolved = item ? resolved + 1 : resolved;

                                    BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                                    baseDescriptionObject.setTitle(issue.getTitle());
                                    baseDescriptionObject.setDescription(issue.getDescription());
                                    baseDescriptionObject.setObject(issue);
                                    baseDescriptionObject.setState(item);
                                    this.lvIssues.getAdapter().add(baseDescriptionObject);

                                    this.pbState.setMax(max);
                                    this.pbState.setProgress(resolved);

                                    double factor = 100.0 / max;
                                    String text = String.format(Locale.GERMANY, "%d", (int) factor * resolved) + "%";
                                    this.lblPercentage.setText(text);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
            issueTask.execute(0);
        } catch (Exception ex) {
            Notifications.printException(this.activity, ex, R.mipmap.ic_launcher_round);
        }
    }

    @Override
    protected void init(View view) {
        this.pbState = view.findViewById(R.id.pbProcess);
        this.lblPercentage = view.findViewById(R.id.lblPercentage);
        this.lvIssues = view.findViewById(R.id.lvIssues);
    }
}
