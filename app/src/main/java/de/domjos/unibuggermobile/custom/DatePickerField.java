package de.domjos.unibuggermobile.custom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

import java.util.Date;
import java.util.Calendar;
import java.util.Objects;

import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.unibuggermobile.activities.MainActivity;

public class DatePickerField extends androidx.appcompat.widget.AppCompatEditText {
    private Context context;
    private String dateFormat, timeFormat;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private Calendar calendar;
    private boolean timePicker;

    public DatePickerField(Context context) {
        super(context);

        this.setParams(context);
        this.initDialog();
        this.initActions();
    }

    public DatePickerField(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setParams(context);
        this.initDialog();
        this.initActions();
    }

    public DatePickerField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setParams(context);
        this.initDialog();
        this.initActions();
    }

    public void setTimePicker(boolean timePicker) {
        this.timePicker = timePicker;
    }

    private void setParams(Context context) {
        this.context = context;
        this.dateFormat = MainActivity.GLOBALS.getSettings(this.context).getDateFormat();
        this.timeFormat = MainActivity.GLOBALS.getSettings(this.context).getTimeFormat();

        this.setInputType(InputType.TYPE_CLASS_DATETIME);
        this.setClickable(true);
        this.setKeyListener(DigitsKeyListener.getInstance("0123456789 .:-/"));
    }

    private void initDialog() {
        this.calendar = Calendar.getInstance();

        this.onTimeSetListener = (timePicker, i, i1) -> {
            this.calendar.set(Calendar.HOUR_OF_DAY, i);
            this.calendar.set(Calendar.MINUTE, i1);
            this.setText(ConvertHelper.convertDateToString(this.calendar.getTime(), this.dateFormat + " " + this.timeFormat));
        };

        this.onDateSetListener = (datePicker, i, i1, i2) -> {
            this.calendar.set(Calendar.YEAR, i);
            this.calendar.set(Calendar.MONTH, i1);
            this.calendar.set(Calendar.DAY_OF_MONTH, i2);
            this.setText(ConvertHelper.convertDateToString(this.calendar.getTime(), this.dateFormat));
            if(this.timePicker) {
                Calendar calendar = this.getDefault(this.dateFormat + " " + this.timeFormat);
                new TimePickerDialog(this.context, this.onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        };
    }

    private void initActions() {
        this.setOnClickListener(v -> {
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
            Date dt = ConvertHelper.convertStringToDate(Objects.requireNonNull(this.getText()).toString().trim(), format);
            calendar = Calendar.getInstance();
            calendar.setTime(dt);
        } catch (Exception ex) {
            calendar = Calendar.getInstance();
        }
        return calendar;
    }
}
