package de.domjos.unibuggermobile.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.VersionTask;


public class RoadMapDialog extends DialogFragment {
    private static final String TITLE = "title";
    private static final String VERSION_ID = "version_id";
    private static final String PROJECT_ID = "project_id";
    private static final String RoadMap = "RoadMap";
    private static final String ChangeLog = "ChangeLog";

    private ProgressBar pbState;
    private TextView lblTitle, lblPercentage;
    private SwipeRefreshDeleteList lvIssues;

    public static RoadMapDialog newInstance(boolean roadMap, Object pid, Object vid) {
        Bundle arguments = new Bundle();
        if(roadMap) {
            arguments.putString(RoadMapDialog.TITLE, RoadMapDialog.RoadMap);
        } else {
            arguments.putString(RoadMapDialog.TITLE, RoadMapDialog.ChangeLog);
        }
        arguments.putSerializable(RoadMapDialog.PROJECT_ID, (Serializable) pid);
        arguments.putSerializable(RoadMapDialog.VERSION_ID, (Serializable) vid);

        RoadMapDialog roadMapDialog = new RoadMapDialog();
        roadMapDialog.setArguments(arguments);
        return roadMapDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roadmap_dialog, container, false);

        Dialog dialog = super.getDialog();
        if(dialog != null) {
            Window window = dialog.getWindow();
            if(window != null) {
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.gravity = Gravity.FILL_HORIZONTAL;
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }

        this.pbState = view.findViewById(R.id.pbProcess);
        this.lblTitle = view.findViewById(R.id.lblTitle);
        this.lblPercentage = view.findViewById(R.id.lblPercentage);
        this.lvIssues = view.findViewById(R.id.lvIssues);

        this.initWithArguments();
        return view;
    }

    private void initWithArguments() {
        try {
            boolean isChangeLog = false;
            Bundle bundle = this.getArguments();
            if(bundle != null) {
                String title = bundle.getString(RoadMapDialog.TITLE);
                Object pid = bundle.getSerializable(RoadMapDialog.PROJECT_ID);
                Object vid = bundle.getSerializable(RoadMapDialog.VERSION_ID);

                if(title != null) {
                    if(!title.equals(RoadMapDialog.RoadMap)) {
                        isChangeLog = true;
                        this.pbState.setVisibility(View.GONE);
                    }
                    this.lblTitle.setText(title);
                }

                boolean changeLog = isChangeLog;
                boolean not = MainActivity.GLOBALS.getSettings(this.requireContext()).showNotifications();
                Activity act = this.requireActivity();
                IBugService<?>  bugs = Helper.getCurrentBugService(this.requireContext());
                int icon = R.drawable.icon_versions;

                VersionTask versionTask = new VersionTask(act, bugs, pid, false, not, "versions", icon);
                List<Version<?>> versions = versionTask.execute(0).get();
                String version_name = "";
                for(Version<?> version : versions) {
                    if(version.getId() instanceof Long) {
                        Long v = (Long) version.getId();
                        Long tmp = Long.parseLong(String.valueOf(vid));
                        if(v.equals(tmp)) {
                            version_name = version.getTitle();
                            break;
                        }
                    } else if(version.getId() instanceof String) {
                        String v = (String) version.getId();
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
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this.requireActivity());
        }
    }
}
