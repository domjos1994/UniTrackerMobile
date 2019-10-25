package de.domjos.unitrackermobile.custom;

import android.graphics.drawable.Drawable;

import com.applandeo.materialcalendarview.EventDay;

import java.util.Calendar;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;

public class CustomEventDay extends EventDay {
    private DescriptionObject descriptionObject;

    public CustomEventDay(Calendar day) {
        super(day);
    }

    public CustomEventDay(Calendar day, int drawable) {
        super(day, drawable);
    }

    public CustomEventDay(Calendar day, Drawable drawable) {
        super(day, drawable);
    }

    public CustomEventDay(Calendar day, int drawable, int labelColor) {
        super(day, drawable, labelColor);
    }

    public CustomEventDay(Calendar day, Drawable drawable, int labelColor) {
        super(day, drawable, labelColor);
    }


    public DescriptionObject getDescriptionObject() {
        return this.descriptionObject;
    }

    public void setDescriptionObject(DescriptionObject descriptionObject) {
        this.descriptionObject = descriptionObject;
    }
}
