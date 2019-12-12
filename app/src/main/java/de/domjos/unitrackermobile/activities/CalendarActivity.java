package de.domjos.unitrackermobile.activities;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.customwidgets.widgets.calendar.Event;
import de.domjos.customwidgets.widgets.calendar.WidgetCalendar;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.AbstractTask;
import de.domjos.unitrackerlibrary.tasks.IssueTask;
import de.domjos.unitrackerlibrary.tasks.ProjectTask;
import de.domjos.unitrackerlibrary.tasks.VersionTask;
import de.domjos.unitrackermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unitrackermobile.helper.Helper;

public class CalendarActivity extends AbstractActivity {
    private WidgetCalendar widgetCalendar;
    private ProgressBar progressBar;
    private LinearLayout llToObject;
    private TextView lblCalendarTitle, lblCalendarSubTitle;
    private Intent intent;


    public CalendarActivity() {
        super(R.layout.calendar_activity);
    }

    @Override
    protected void initActions() {

        this.widgetCalendar.setOnClick(event -> {

            if(event.getObject() instanceof Project) {
                this.lblCalendarSubTitle.setText(((Project)event.getObject()).getTitle());
                this.lblCalendarTitle.setText(this.getString(R.string.projects));
                this.intent = new Intent(this.getApplicationContext(), ProjectActivity.class);
            } else if(event.getObject() instanceof Version) {
                this.lblCalendarSubTitle.setText(((Version)event.getObject()).getTitle());
                this.lblCalendarTitle.setText(this.getString(R.string.versions));
                this.intent = new Intent(this.getApplicationContext(), VersionActivity.class);
            } else if(event.getObject() instanceof Issue) {
                this.lblCalendarSubTitle.setText(((Issue)event.getObject()).getTitle());
                this.lblCalendarTitle.setText(this.getString(R.string.issues));
                this.intent = new Intent(this.getApplicationContext(), MainActivity.class);
            } else {
                this.lblCalendarSubTitle.setText(event.getName());
                this.lblCalendarTitle.setText(this.getString(R.string.calendar));
                this.intent = null;
            }
        });

        this.llToObject.setOnClickListener(view -> {
            if(intent!=null) {
                startActivity(intent);
            }
        });
    }

    @Override
    protected void initControls() {
        this.widgetCalendar = this.findViewById(R.id.cvEventCalendar);
        this.widgetCalendar.showDay(false);
        this.progressBar = this.findViewById(R.id.pbCalendar);

        this.llToObject = this.findViewById(R.id.llToObject);
        this.lblCalendarTitle = this.findViewById(R.id.lblCalendarTitle);
        this.lblCalendarSubTitle = this.findViewById(R.id.lblCalendarSubTitle);
    }

    @Override
    protected void reload() {
        this.insertEvents();
    }

    private void insertEvents() {
        try {
            this.progressBar.setVisibility(View.VISIBLE);
            List<Authentication> accounts = MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("");
            for(Authentication account : accounts) {
                IBugService bugService = Helper.getCurrentBugService(account, this.getApplicationContext());

                ProjectTask projectTask = new ProjectTask(CalendarActivity.this, bugService, false, false, R.drawable.ic_apps_black_24dp);
                projectTask.after(new AbstractTask.PostExecuteListener() {
                    @Override
                    public void onPostExecute(Object o) {
                        if(o instanceof List) {
                            List objects = (List) o;
                            for (Object obj : objects) {
                                if(obj instanceof Project) {
                                    Project project = (Project) obj;
                                    addEvent(project.getReleasedAt(), project, R.drawable.ic_apps_black_24dp);

                                    VersionTask versionTask  = new VersionTask(CalendarActivity.this, bugService, project.getId(), false, false, "versions", R.drawable.ic_update_black_24dp);
                                    versionTask.after(new AbstractTask.PostExecuteListener() {
                                        @Override
                                        public void onPostExecute(Object o) {
                                            List objList = (List) o;
                                            for(Object obj : objList) {
                                                Version version = (Version) obj;
                                                addEvent(version.getReleasedVersionAt(), version, R.drawable.ic_update_black_24dp);
                                            }
                                            runOnUiThread(()->widgetCalendar.reload());
                                        }
                                    });
                                    versionTask.execute(0);

                                    IssueTask issueTask = new IssueTask(CalendarActivity.this, bugService, project.getId(), false, false, false, R.drawable.ic_bug_report_black_24dp);
                                    issueTask.after(new AbstractTask.PostExecuteListener() {
                                        @Override
                                        public void onPostExecute(Object o) {

                                            List objList = (List) o;
                                            loadingFinish(0, objList.size());
                                            int i = 0;
                                            for(Object obj : objList) {
                                                Issue issue = (Issue) obj;

                                                IssueTask detailTasks = new IssueTask(CalendarActivity.this, bugService, project.getId(), false, true, false, R.drawable.ic_bug_report_black_24dp);
                                                detailTasks.after(new AbstractTask.PostExecuteListener() {
                                                    @Override
                                                    public void onPostExecute(Object o) {
                                                        List obj = (List) o;
                                                        Issue detailIssue = (Issue) obj.get(0);
                                                        if(detailIssue.getDueDate() != null) {
                                                            addEvent(detailIssue.getDueDate().getTime(), detailIssue, R.drawable.ic_bug_report_black_24dp);
                                                        }
                                                    }
                                                });
                                                detailTasks.execute(issue.getId());
                                                i++;
                                                loadingFinish(i, objList.size());
                                            }
                                            runOnUiThread(()->widgetCalendar.reload());
                                        }
                                    });
                                    issueTask.execute(0);
                                }
                            }
                        }
                    }
                });
                projectTask.execute(0);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, CalendarActivity.this);
        }
    }

    private void loadingFinish(int current, int max) {
        this.progressBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.progressBar.setProgress(current, true);
        } else {
            this.progressBar.setProgress(current);
        }
        if(current==max) {
            this.progressBar.setVisibility(View.GONE);
        } else {
            this.progressBar.setVisibility(View.VISIBLE);
        }
        this.progressBar.invalidate();
    }


    private void addEvent(long time, DescriptionObject descriptionObject, int icon) {
        Date dt = new Date();
        dt.setTime(time);

        Event event = new Event() {
            @Override
            public int getIcon() {
                return icon;
            }
        };
        event.setCalendar(dt);
        event.setObject(descriptionObject);
        event.setDescription(descriptionObject.getDescription());

        this.widgetCalendar.addEvent(event);
        this.widgetCalendar.invalidate();
    }
}
