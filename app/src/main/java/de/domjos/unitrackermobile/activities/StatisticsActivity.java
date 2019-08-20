/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniBuggerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.activities;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.AbstractTask;
import de.domjos.unibuggerlibrary.tasks.StatisticsTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.helper.Helper;

public final class StatisticsActivity extends AbstractActivity {
    private LineChart lcStatisticsBugs;
    private RadioButton rbStatisticsMonthly, rbStatisticsYearly;
    private EditText txtStatisticsValue;
    private ProgressBar pbStatistics;
    private ImageButton cmdStatisticsReload;

    private Spinner spStatisticsBugTracker;

    private Map<Authentication, Map<Project, List<Issue>>> data = new LinkedHashMap<>();

    public StatisticsActivity() {
        super(R.layout.statistics_activity);
    }

    @Override
    protected void initActions() {

        this.cmdStatisticsReload.setOnClickListener(view -> reloadDiagram());


        this.rbStatisticsMonthly.setOnCheckedChangeListener((buttonView, isChecked) -> reloadDiagram());
        this.rbStatisticsYearly.setOnCheckedChangeListener((buttonView, isChecked) -> reloadDiagram());
        this.txtStatisticsValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                reloadDiagram();
            }
        });
    }

    @Override
    protected void initControls() {
        this.rbStatisticsMonthly = this.findViewById(R.id.rbStatisticsMonthly);
        this.rbStatisticsYearly = this.findViewById(R.id.rbStatisticsYearly);
        this.txtStatisticsValue = this.findViewById(R.id.txtStatisticsValue);
        this.lcStatisticsBugs = this.findViewById(R.id.lcStatisticsBugs);
        this.pbStatistics = this.findViewById(R.id.pbStatistics);
        this.cmdStatisticsReload = this.findViewById(R.id.cmdStatisticsSync);

        this.spStatisticsBugTracker = this.findViewById(R.id.spStatisticsBugTracker);
        ArrayAdapter<Authentication> bugTrackerAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
        this.spStatisticsBugTracker.setAdapter(bugTrackerAdapter);
        bugTrackerAdapter.notifyDataSetChanged();
        bugTrackerAdapter.add(new Authentication());
        for (Authentication authentication : MainActivity.GLOBALS.getSqLiteGeneral().getAccounts("")) {
            bugTrackerAdapter.add(authentication);
        }
        this.spStatisticsBugTracker.setSelection(0);

        this.lcStatisticsBugs.setVisibleXRangeMinimum(0);
        this.lcStatisticsBugs.setVisibleYRangeMinimum(0, YAxis.AxisDependency.LEFT);

        this.initData();
    }

    protected void reloadDiagram() {
        try {
            load();
            int type = Calendar.MONTH;
            int x = Calendar.DAY_OF_MONTH;
            int max = 30;
            Calendar current = Calendar.getInstance();
            current.setTime(new Date());

            if (this.rbStatisticsYearly.isChecked()) {
                type = Calendar.YEAR;
                x = Calendar.MONTH;
                max = 12;
                try {
                    current.set(type, Integer.parseInt(this.txtStatisticsValue.getText().toString()));
                } catch (Exception ignored) {
                }
            }
            if (this.rbStatisticsMonthly.isChecked()) {
                type = Calendar.MONTH;
                x = Calendar.DAY_OF_MONTH;
                try {
                    current.set(type, Integer.parseInt(this.txtStatisticsValue.getText().toString()));
                } catch (Exception ignored) {
                }
                max = current.getActualMaximum(Calendar.DAY_OF_MONTH);
            }

            LineData lineData = new LineData();
            for (Map.Entry<Authentication, Map<Project, List<Issue>>> authEntry : this.data.entrySet()) {
                List<Entry> entries = new LinkedList<>();
                for (int i = 1; i <= max; i++) {
                    entries.add(new Entry(i, 0));
                }

                for (Map.Entry<Project, List<Issue>> projectEntry : authEntry.getValue().entrySet()) {
                    for (Issue issue : projectEntry.getValue()) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(issue.getSubmitDate());

                        if (calendar.get(type) == current.get(type)) {
                            for (int i = 1; i <= max; i++) {
                                if (i == calendar.get(x)) {
                                    entries.get(i - 1).setX(entries.get(i - 1).getY() + 1);
                                    break;
                                }
                            }
                        }
                    }
                }

                lineData.addDataSet(new LineDataSet(entries, authEntry.getKey().getTitle()));
            }
            this.lcStatisticsBugs.setData(lineData);
            load();
        } catch (Exception ex) {
            MessageHelper.printException(ex, StatisticsActivity.this);
        }
    }

    private void initData() {
        List<IBugService> bugServices = new LinkedList<>();
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

        load();
        StatisticsTask statisticsTask = new StatisticsTask(
                StatisticsActivity.this,
                bugServices,
                MainActivity.GLOBALS.getSettings(StatisticsActivity.this).showNotifications(),
                R.drawable.ic_multiline_chart_black_24dp
        );
        statisticsTask.after(new AbstractTask.PostExecuteListener<Map<Authentication, Map<Project, List<Issue>>>>() {
            @Override
            public void onPostExecute(Map<Authentication, Map<Project, List<Issue>>> result) {
                data = result;
                reloadDiagram();
                load();
            }
        });
        statisticsTask.execute();
    }

    private void load() {
        if (this.pbStatistics.getVisibility() == View.GONE) {
            this.pbStatistics.setVisibility(View.VISIBLE);
            this.cmdStatisticsReload.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 9));
        } else {
            this.pbStatistics.setVisibility(View.GONE);
            this.cmdStatisticsReload.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 10));
            MessageHelper.printMessage(this.getString(R.string.statistics_loaded), StatisticsActivity.this);
        }
    }
}
