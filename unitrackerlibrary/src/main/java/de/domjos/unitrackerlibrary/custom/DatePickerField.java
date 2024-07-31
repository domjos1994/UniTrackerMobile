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

package de.domjos.unitrackerlibrary.custom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.Calendar;
import java.util.Objects;

import de.domjos.unitrackerlibrary.R;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;

public class DatePickerField extends LinearLayout {
    private Context context;
    private String dateFormat, timeFormat;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private Calendar calendar;
    private boolean timePicker;
    private TextInputLayout inputLayout;
    private EditText txt;

    public DatePickerField(Context context) {
        super(context);

        this.init(context, null);
        this.setParams(context);
        this.initDialog();
        this.initActions();
    }

    public DatePickerField(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.init(context, attrs);
        this.setParams(context);
        this.initDialog();
        this.initActions();
    }

    public DatePickerField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.init(context, attrs);
        this.setParams(context);
        this.initDialog();
        this.initActions();
    }

    public EditText getEditText() {
        return this.txt;
    }

    public void setText(String text) {
        this.txt.setText(text);
    }

    public Editable getText() {
        return this.txt.getText();
    }

    public void setTimePicker(boolean timePicker) {
        this.timePicker = timePicker;
    }

    private void setParams(Context context) {
        this.context = context;
        this.dateFormat = this.context.getString(R.string.sys_date_format);
        this.timeFormat = this.context.getString(R.string.sys_time_format);

        this.txt.setKeyListener(DigitsKeyListener.getInstance("0123456789 .:-/"));
    }

    private void initDialog() {
        this.calendar = Calendar.getInstance();

        this.onTimeSetListener = (timePicker, i, i1) -> {
            this.calendar.set(Calendar.HOUR_OF_DAY, i);
            this.calendar.set(Calendar.MINUTE, i1);
            this.txt.setText(ConvertHelper.convertDateToString(this.calendar.getTime(), this.dateFormat + " " + this.timeFormat));
        };

        this.onDateSetListener = (datePicker, i, i1, i2) -> {
            this.calendar.set(Calendar.YEAR, i);
            this.calendar.set(Calendar.MONTH, i1);
            this.calendar.set(Calendar.DAY_OF_MONTH, i2);
            this.txt.setText(ConvertHelper.convertDateToString(this.calendar.getTime(), this.dateFormat));
            if(this.timePicker) {
                Calendar calendar = this.getDefault(this.dateFormat + " " + this.timeFormat);
                new TimePickerDialog(this.context, this.onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        };
    }

    private void initActions() {
        this.inputLayout.setEndIconOnClickListener(v -> {
            Calendar calendar = this.getDefault(this.dateFormat);
            new DatePickerDialog(
                this.context,
                this.onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private Calendar getDefault(String format) {
        Calendar calendar;
        try {
            Date dt = ConvertHelper.convertStringToDate(Objects.requireNonNull(this.txt.getText()).toString().trim(), format);
            calendar = Calendar.getInstance();
            calendar.setTime(dt);
        } catch (Exception ex) {
            calendar = Calendar.getInstance();
        }
        return calendar;
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        View view = inflate(context, R.layout.date_picker_field, this);
        this.inputLayout = view.findViewById(R.id.tilCalendar);
        this.txt = view.findViewById(R.id.txtCalendar);

        if(attrs != null) {
            try (TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DatePickerField, 0, 0)) {
                try {
                    String tmp = a.getString(R.styleable.DatePickerField_android_hint);
                    if (tmp != null) {
                        this.inputLayout.setHint(tmp);
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
