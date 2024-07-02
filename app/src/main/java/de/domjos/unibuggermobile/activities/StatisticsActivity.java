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

package de.domjos.unibuggermobile.activities;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.domjos.unitrackerlibrary.custom.AbstractTask;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.StatisticsTask;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unibuggermobile.helper.DiagramHelper;
import de.domjos.unibuggermobile.helper.Helper;

public final class StatisticsActivity extends AbstractActivity {
    private BarChart bcStatisticsBugsPerProject, bcStatisticsBugsPerUser, bcStatisticsSolvedBugs;
    private LineChart lcStatisticsBugsInTime;
    private RadioButton rbStatisticsMonthly, rbStatisticsYearly;
    private EditText txtStatisticsValue;
    private ProgressBar pbStatistics;
    private ImageButton cmdStatisticsReload;

    private Spinner spStatisticsBugTracker;
    private Spinner spStatisticsDiagram;
    private ArrayAdapter<Authentication> bugTrackerAdapter;

    private Map<Authentication, Map<Project<?>, List<Issue<?>>>> data;
    private final List<Chart<?>> charts;

    public StatisticsActivity() {
        super(R.layout.statistics_activity);
        this.data = new LinkedHashMap<>();
        this.charts = new LinkedList<>();
    }

    @Override
    protected void initActions() {

        this.spStatisticsDiagram.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for(Chart<?> chart : charts) {
                    chart.setVisibility(View.GONE);
                }
                charts.get(i).setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.cmdStatisticsReload.setOnClickListener(view -> reloadCharts());
        this.rbStatisticsMonthly.setOnCheckedChangeListener((buttonView, isChecked) -> reloadCharts());
        this.rbStatisticsYearly.setOnCheckedChangeListener((buttonView, isChecked) -> reloadCharts());

        this.txtStatisticsValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                reloadCharts();
            }
        });

        this.spStatisticsBugTracker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                reloadCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        this.bcStatisticsBugsPerProject.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String label = bcStatisticsBugsPerProject.getXAxis().getValueFormatter().getFormattedValue(e.getX(), bcStatisticsBugsPerProject.getXAxis());
                MessageHelper.printMessage(label + ": " + (int) e.getY() + " Bugs", R.mipmap.ic_launcher_round, StatisticsActivity.this);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        this.lcStatisticsBugsInTime.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MainActivity.GLOBALS.getSettings(getApplicationContext()).getDateFormat(), Locale.GERMAN);
                    String value = txtStatisticsValue.getText().toString().trim();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    if (rbStatisticsMonthly.isChecked()) {
                        if (!value.isEmpty()) {
                            calendar.set(Calendar.MONTH, Integer.parseInt(value));
                        }
                        calendar.set(Calendar.DAY_OF_MONTH, (int) e.getX());
                        value = simpleDateFormat.format(calendar.getTime());
                    } else {
                        if (!value.isEmpty()) {
                            calendar.set(Calendar.YEAR, Integer.parseInt(value));
                        }
                        calendar.set(Calendar.MONTH, (int) e.getX());
                        value = simpleDateFormat.format(calendar.getTime());
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        value += "-" + simpleDateFormat.format(calendar.getTime());
                    }
                    MessageHelper.printMessage(value + ": Bugs: " + e.getY(), R.mipmap.ic_launcher_round, StatisticsActivity.this);
                } catch (Exception ex) {
                    MessageHelper.printException(ex, R.mipmap.ic_launcher_round, StatisticsActivity.this);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    @Override
    protected void initControls() {
        this.bcStatisticsBugsPerProject = this.findViewById(R.id.bcStatisticsBugsPerProject);
        Description description = new Description();
        description.setText(this.getString(R.string.statistics_bar_description));
        Legend legend = this.bcStatisticsBugsPerProject.getLegend();
        legend.setDrawInside(false);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        this.bcStatisticsBugsPerProject.setDescription(description);
        this.charts.add(this.bcStatisticsBugsPerProject);

        this.lcStatisticsBugsInTime = this.findViewById(R.id.lcStatisticsBugsInTime);
        description = new Description();
        description.setText(this.getString(R.string.statistics_line_description));
        legend = this.lcStatisticsBugsInTime.getLegend();
        legend.setDrawInside(false);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        this.lcStatisticsBugsInTime.setDescription(description);
        this.charts.add(this.lcStatisticsBugsInTime);

        this.bcStatisticsBugsPerUser = this.findViewById(R.id.bcStatisticsBugsPerUser);
        description = new Description();
        description.setText(this.getString(R.string.statistics_bar_user));
        this.bcStatisticsBugsPerUser.setDescription(description);
        legend = this.bcStatisticsBugsPerUser.getLegend();
        legend.setDrawInside(false);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        this.charts.add(this.bcStatisticsBugsPerUser);

        this.bcStatisticsSolvedBugs = this.findViewById(R.id.bcStatisticsSolvedBugs);
        description = new Description();
        description.setText(this.getString(R.string.statistics_solved_bugs));
        this.bcStatisticsSolvedBugs.setDescription(description);
        legend = this.bcStatisticsSolvedBugs.getLegend();
        legend.setDrawInside(false);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        this.charts.add(this.bcStatisticsSolvedBugs);

        this.rbStatisticsMonthly = this.findViewById(R.id.rbStatisticsMonthly);
        this.rbStatisticsYearly = this.findViewById(R.id.rbStatisticsYearly);
        this.txtStatisticsValue = this.findViewById(R.id.txtStatisticsValue);
        this.pbStatistics = this.findViewById(R.id.pbStatistics);
        this.cmdStatisticsReload = this.findViewById(R.id.cmdStatisticsSync);

        this.spStatisticsBugTracker = this.findViewById(R.id.spStatisticsBugTracker);
        this.bugTrackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), R.layout.spinner_item);
        this.spStatisticsBugTracker.setAdapter(bugTrackerAdapter);
        this.bugTrackerAdapter.notifyDataSetChanged();
        this.bugTrackerAdapter.add(new Authentication());
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            this.bugTrackerAdapter.add(authentication);
        }
        this.spStatisticsBugTracker.setSelection(0);
        this.spStatisticsDiagram = this.findViewById(R.id.spStatisticsDiagram);

        this.initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menExport) {
            this.lcStatisticsBugsInTime.saveToGallery("uniTrackerMobile_bugsInTime.jpg");
            this.bcStatisticsBugsPerProject.saveToGallery("uniTrackerMobile_bugsPerProject.jpg");
            this.bcStatisticsBugsPerUser.saveToGallery("uniTrackerMobile_bugsPerUser.jpg");
            this.bcStatisticsSolvedBugs.saveToGallery("uniTrackerMobile_solvedBugs.jpg");
            MessageHelper.printMessage(this.getString(R.string.statistics_export_succes), R.mipmap.ic_launcher_round, StatisticsActivity.this);
        }

        return super.onOptionsItemSelected(item);
    }


    private void initData() {
        List<IBugService<?>> bugServices = new LinkedList<>();
        if (this.spStatisticsBugTracker.getSelectedItem() != null) {
            Authentication selectedItem = (Authentication) this.spStatisticsBugTracker.getSelectedItem();
            if (selectedItem.getId() == null) {
                for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
                    bugServices.add(Helper.getCurrentBugService(authentication, StatisticsActivity.this));
                }
            } else {
                bugServices.add(Helper.getCurrentBugService(selectedItem, StatisticsActivity.this));
            }
        }

        this.data = new LinkedHashMap<>();
        StatisticsTask statisticsTask = new StatisticsTask(
                StatisticsActivity.this,
                bugServices,
                MainActivity.GLOBALS.getSettings(StatisticsActivity.this).showNotifications(),
                R.drawable.icon_statistics, this.pbStatistics
        );
        statisticsTask.setOnUpdate((auth, data) -> {
            this.data.put(auth, data);
            reloadCharts();
        });
        statisticsTask.after((AbstractTask.PostExecuteListener<Map<Authentication, Map<Project<?>, List<Issue<?>>>>>) result -> MessageHelper.printMessage(getString(R.string.statistics_loaded), R.mipmap.ic_launcher_round, StatisticsActivity.this));
        statisticsTask.execute();
    }

    private void reloadCharts() {
        DiagramHelper diagramHelper = new DiagramHelper(this.data, StatisticsActivity.this);
        if (this.rbStatisticsMonthly.isChecked()) {
            diagramHelper.setTimeSpan(DiagramHelper.TimeSpan.Month);
        } else if (this.rbStatisticsYearly.isChecked()) {
            diagramHelper.setTimeSpan(DiagramHelper.TimeSpan.Year);
        }
        diagramHelper.setTime(this.txtStatisticsValue.getText().toString());

        if (this.spStatisticsBugTracker.getSelectedItem() != null) {
            List<Authentication> authentications = new LinkedList<>();
            Authentication authentication = this.bugTrackerAdapter.getItem(this.spStatisticsBugTracker.getSelectedItemPosition());
            if (authentication != null) {
                if (authentication.getId() != null) {
                    authentications.add(this.bugTrackerAdapter.getItem(this.spStatisticsBugTracker.getSelectedItemPosition()));
                    diagramHelper.setAuthentications(authentications);
                } else {
                    diagramHelper.setAuthentications(MainActivity.GLOBALS.getSqLiteGeneral().getAccounts(""));
                }
            } else {
                diagramHelper.setAuthentications(MainActivity.GLOBALS.getSqLiteGeneral().getAccounts(""));
            }
        } else {
            diagramHelper.setAuthentications(MainActivity.GLOBALS.getSqLiteGeneral().getAccounts(""));
        }

        diagramHelper.updateProjectBarChart(this.bcStatisticsBugsPerProject);
        diagramHelper.updateLineChart(this.lcStatisticsBugsInTime);
        diagramHelper.updateUserBarChart(this.bcStatisticsBugsPerUser);
        diagramHelper.updateSolvedBugsBarChart(this.bcStatisticsSolvedBugs);
    }
}
