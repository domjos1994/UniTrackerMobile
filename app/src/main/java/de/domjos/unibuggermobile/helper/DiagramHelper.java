/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggermobile.helper;

import android.graphics.Typeface;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class DiagramHelper {
    private Map<Authentication, Map<Project, List<Issue>>> data;
    private List<Authentication> authentications;
    private TimeSpan timeSpan;
    private int year, month;

    public DiagramHelper(Map<Authentication, Map<Project, List<Issue>>> data) {
        this.data = data;
        this.timeSpan = TimeSpan.None;
        this.month = -1;
        this.year = -1;
        this.authentications = new LinkedList<>();
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

    public void setTime(String time) {
        this.month = -1;
        this.year = -1;
        switch (this.timeSpan) {
            case Year:
                try {
                    this.year = Integer.parseInt(time);
                } catch (Exception ex) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    this.year = calendar.get(Calendar.YEAR);
                }
                break;
            case Month:
                try {
                    if (time.contains(".")) {
                        String[] part = time.split("\\.");
                        this.month = Integer.parseInt(part[0]);
                        this.month = Integer.parseInt(part[1]);
                    } else if (time.contains("-")) {
                        String[] part = time.split("-");
                        this.month = Integer.parseInt(part[0]);
                        this.month = Integer.parseInt(part[1]);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    this.year = calendar.get(Calendar.YEAR);
                    this.month = calendar.get(Calendar.MONTH) + 1;
                }
                break;
        }
    }

    public void setAuthentications(List<Authentication> authentications) {
        this.authentications = authentications;
    }

    public void updateProjectBarChart(BarChart barChart) {
        this.createProjectBarData(barChart);
        barChart.invalidate();
    }

    public void updateUserBarChart(BarChart barChart) {
        this.createUserBarData(barChart);
        barChart.invalidate();
    }

    public void updateLineChart(LineChart lineChart) {
        this.createLineData(lineChart);
        lineChart.invalidate();
    }

    @SuppressWarnings("MagicConstant")
    private void createProjectBarData(BarChart barChart) {
        Random generator = new Random();
        BarData barData = new BarData();
        Map<Integer, Project> tmp = new LinkedHashMap<>();
        int i = 0;

        for (Map.Entry<Authentication, Map<Project, List<Issue>>> accounts : this.data.entrySet()) {
            boolean goOn = false;
            if (this.authentications.isEmpty()) {
                goOn = true;
            } else {
                for (Authentication authentication : this.authentications) {
                    if (authentication != null) {
                        if (authentication.getId() != null) {
                            if (authentication.getId().equals(accounts.getKey().getId())) {
                                goOn = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (goOn) {
                List<BarEntry> barEntries = new LinkedList<>();
                for (Map.Entry<Project, List<Issue>> projects : accounts.getValue().entrySet()) {
                    if (!projects.getValue().isEmpty()) {
                        int counter = 0;
                        switch (this.timeSpan) {
                            case Year:
                                for (Issue issue : projects.getValue()) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(issue.getLastUpdated());
                                    if (this.year == calendar.get(Calendar.YEAR)) {
                                        counter++;
                                    }
                                }
                                break;
                            case Month:
                                for (Issue issue : projects.getValue()) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(issue.getLastUpdated());
                                    if (this.year == calendar.get(Calendar.YEAR) && this.month - 1 == calendar.get(Calendar.MONTH)) {
                                        counter++;
                                    }
                                }
                                break;
                            case None:
                                counter = projects.getValue().size();
                                break;
                        }
                        if (counter != 0) {
                            tmp.put(i, projects.getKey());
                            barEntries.add(new BarEntry(i, counter));
                            i++;
                        }
                    }
                }
                BarDataSet barDataSet = new BarDataSet(barEntries, accounts.getKey().getTitle());
                barDataSet.setColor(generator.nextInt());
                barData.addDataSet(barDataSet);
            }
        }

        XAxis axis = barChart.getXAxis();
        axis.setTypeface(Typeface.DEFAULT);
        axis.setGranularity(1f);
        axis.setValueFormatter((value, axis1) -> {
            Project project = tmp.get((int) value);
            return project == null ? "" : project.getTitle();
        });

        barChart.setData(barData);
    }

    private void createUserBarData(BarChart barChart) {
        Random generator = new Random();
        BarData barData = new BarData();
        Map<String, Integer> tmp = new LinkedHashMap<>();
        Map<Integer, Integer> mp = new LinkedHashMap<>();
        int i = 0, current;

        for (Map.Entry<Authentication, Map<Project, List<Issue>>> accounts : this.data.entrySet()) {
            boolean goOn = false;
            if (this.authentications.isEmpty()) {
                goOn = true;
            } else {
                for (Authentication authentication : this.authentications) {
                    if (authentication != null) {
                        if (authentication.getId() != null) {
                            if (authentication.getId().equals(accounts.getKey().getId())) {
                                goOn = true;
                                break;
                            }
                        }
                    }
                }
            }

            if(goOn) {
                for (Map.Entry<Project, List<Issue>> projects : accounts.getValue().entrySet()) {
                    mp.clear();
                    for(Issue issue : projects.getValue()) {
                        if(issue.getHandler() != null) {
                            if(tmp.containsKey(issue.getHandler().getTitle())) {
                                current = Objects.requireNonNull(tmp.get(issue.getHandler().getTitle()));
                            } else {
                                i++;
                                tmp.put(issue.getHandler().getTitle(), i);
                                current = i;
                            }

                            if(mp.containsKey(current)) {
                                mp.put(current, mp.get(current) + 1);
                            } else {
                                mp.put(current, 1);
                            }
                        }
                    }


                    for(Map.Entry<Integer, Integer> entry : mp.entrySet()) {
                        List<BarEntry> barEntries = new LinkedList<>();
                        barEntries.add(new BarEntry(entry.getKey(), entry.getValue()));

                        String currentItem = "";
                        int counter = 1;
                        for(String item : tmp.keySet()) {
                            if(counter==entry.getKey()) {
                                currentItem = item;
                            }
                            counter++;
                        }

                        BarDataSet barDataSet = new BarDataSet(barEntries, currentItem);
                        barDataSet.setColor(generator.nextInt());
                        barData.addDataSet(barDataSet);
                    }
                }
            }
        }

        XAxis axis = barChart.getXAxis();
        axis.setTypeface(Typeface.DEFAULT);
        axis.setGranularity(1f);
        axis.setValueFormatter((value, axis1) -> {
            int counter = 1;
            for(String item : tmp.keySet()) {
                if(counter==value) {
                    return item;
                }
                counter++;
            }
            return "";
        });
        barChart.setData(barData);
    }

    @SuppressWarnings("MagicConstant")
    private void createLineData(LineChart lineChart) {
        Random generator = new Random();
        LineData lineData = new LineData();

        for (Map.Entry<Authentication, Map<Project, List<Issue>>> accounts : this.data.entrySet()) {
            boolean goOn = false;
            if (this.authentications.isEmpty()) {
                goOn = true;
            } else {
                for (Authentication authentication : this.authentications) {
                    if (authentication != null) {
                        if (authentication.getId() != null) {
                            if (authentication.getId().equals(accounts.getKey().getId())) {
                                goOn = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (goOn) {
                List<Entry> entries = new LinkedList<>();

                Calendar ts = Calendar.getInstance();
                ts.set(Calendar.YEAR, this.year);
                ts.set(Calendar.MONTH, this.month - 1);
                switch (this.timeSpan) {
                    case Year:
                        for (int i = 1; i <= 12; i++) {
                            for (Map.Entry<Project, List<Issue>> projects : accounts.getValue().entrySet()) {
                                for (Issue issue : projects.getValue()) {
                                    Calendar updated = Calendar.getInstance();
                                    updated.setTime(issue.getLastUpdated());

                                    if (updated.get(Calendar.YEAR) == ts.get(Calendar.YEAR) && updated.get(Calendar.MONTH) == (i - 1)) {
                                        boolean exists = false;
                                        for (int j = 0; j <= entries.size() - 1; j++) {
                                            if (entries.get(j).getX() == i) {
                                                entries.get(j).setY(entries.get(j).getY() + 1);
                                                exists = true;
                                                break;
                                            }
                                        }

                                        if (!exists) {
                                            entries.add(new Entry(i, 1));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case Month:
                        for (int i = 1; i <= ts.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                            entries.add(new Entry(i, 0));
                            for (Map.Entry<Project, List<Issue>> projects : accounts.getValue().entrySet()) {
                                for (Issue issue : projects.getValue()) {
                                    Calendar updated = Calendar.getInstance();
                                    updated.setTime(issue.getLastUpdated());

                                    if (updated.get(Calendar.YEAR) == ts.get(Calendar.YEAR) && updated.get(Calendar.MONTH) == ts.get(Calendar.MONTH) && updated.get(Calendar.DAY_OF_MONTH) == (i - 1)) {
                                        for (int j = 0; j <= entries.size() - 1; j++) {
                                            if (entries.get(j).getX() == i) {
                                                entries.get(j).setY(entries.get(j).getY() + 1);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }

                LineDataSet lineDataSet = new LineDataSet(entries, accounts.getKey().getTitle());
                lineDataSet.setColor(generator.nextInt());
                lineData.addDataSet(lineDataSet);
            }
        }

        lineChart.setData(lineData);
    }

    public enum TimeSpan {
        Year,
        Month,
        None
    }
}
