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

package de.domjos.unitrackermobile.helper;

import android.content.Context;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;

public class Validator {
    private Context context;
    private Map<EditText, Map.Entry<ValidatorType, String>> executeLater;


    public Validator(Context context) {
        this.context = context;
        this.executeLater = new LinkedHashMap<>();
    }

    public void addEmptyValidator(EditText txt) {
        this.executeLater.put(txt, new AbstractMap.SimpleEntry<>(ValidatorType.empty, ""));
    }

    public void addDuplicatedEntry(EditText txt, String table, String column, long id) {
        this.executeLater.put(txt, new AbstractMap.SimpleEntry<>(ValidatorType.duplicated, table + ":" + column + ":" + id));
    }

    public void addValueEqualsRegex(EditText txt, String regex) {
        this.executeLater.put(txt, new AbstractMap.SimpleEntry<>(ValidatorType.regex, regex));
    }

    public void addValueEqualsDate(EditText txt) {
        this.executeLater.put(txt, new AbstractMap.SimpleEntry<>(ValidatorType.date, ""));
    }

    public boolean getState() {
        boolean state = true;
        for (Map.Entry<EditText, Map.Entry<ValidatorType, String>> entry : this.executeLater.entrySet()) {
            ValidatorType type = entry.getValue().getKey();
            EditText txt = entry.getKey();
            String value = entry.getValue().getValue();

            boolean currentState;
            switch (type) {
                case empty:
                    currentState = this.controlFieldIsEmpty(txt);
                    if (!currentState) {
                        state = false;
                    }
                    break;
                case duplicated:
                    String[] field = value.split(":");
                    if (field.length == 3) {
                        currentState = this.controlFieldIsDuplicated(txt, field[0], field[1], Long.parseLong(field[2]));
                        if (!currentState) {
                            state = false;
                        }
                    }
                    break;
                case regex:
                    currentState = this.controlFieldEqualsRegex(txt, value);
                    if (!currentState) {
                        state = false;
                    }
                    break;
                case date:
                    currentState = this.controlFieldEqualsDate(txt);
                    if (!currentState) {
                        state = false;
                    }
                    break;
            }
        }

        return state;
    }

    public void removeValidator(EditText txt) {
        txt.setError(null);
    }

    private boolean controlFieldIsEmpty(EditText txt) {
        if (txt != null) {
            if (txt.getText() != null) {
                if (txt.getText().toString().isEmpty()) {
                    txt.setError(String.format(this.context.getString(R.string.validator_empty), txt.getHint()));
                    return false;
                } else {
                    txt.setError(null);
                    return true;
                }
            }
        }
        return true;
    }

    private boolean controlFieldIsDuplicated(EditText txt, String table, String column, long id) {
        if (txt != null) {
            if (txt.getText() != null) {
                if (MainActivity.GLOBALS.getSqLiteGeneral().duplicated(table, column, txt.getText().toString(), "ID<>" + id)) {
                    txt.setError(String.format(this.context.getString(R.string.validator_duplicated), txt.getText().toString(), txt.getHint()));
                    return false;
                }
            }
            txt.setError(null);
        }
        return true;
    }

    private boolean controlFieldEqualsDate(EditText txt) {
        try {
            String dateFormat = MainActivity.GLOBALS.getSettings(this.context).getDateFormat();
            String timeFormat = MainActivity.GLOBALS.getSettings(this.context).getTimeFormat();
            if (txt != null) {
                if (txt.getText() != null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat + " " + timeFormat, Locale.GERMAN);
                    simpleDateFormat.parse(txt.getText().toString());
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean controlFieldEqualsRegex(EditText txt, String regex) {
        if (txt != null) {
            if (txt.getText() != null) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(txt.getText().toString());
                if (!matcher.matches()) {
                    txt.setError(String.format(this.context.getString(R.string.validator_matches), txt.getHint()));
                    return false;
                }
            }
            txt.setError(null);
        }
        return true;
    }

    private enum ValidatorType {
        empty,
        duplicated,
        regex,
        date
    }
}
