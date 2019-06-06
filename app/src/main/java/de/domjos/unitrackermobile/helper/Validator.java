/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniBuggerMobile <https://github.com/domjos1994/UniBuggerMobile>.
 *
 * UniBuggerMobile is free software: you can redistribute it and/or modify
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
 * along with UniBuggerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.helper;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;

public class Validator {
    private Context context;
    private Map<Integer, Boolean> states;
    private Map<Integer, Map.Entry<EditText, String>> executeLater;
    private Map<Integer, TextWatcher> textWatchers;


    public Validator(Context context) {
        this.context = context;
        this.states = new LinkedHashMap<>();
        this.executeLater = new LinkedHashMap<>();
        this.textWatchers = new LinkedHashMap<>();
    }

    public void addEmptyValidator(EditText txt) {
        this.controlFieldIsEmpty(txt);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                controlFieldIsEmpty(txt);
            }
        };
        textWatchers.put(txt.getId(), textWatcher);
        txt.addTextChangedListener(textWatcher);
    }

    public void addDuplicatedEntry(EditText txt, String table, String column, long id) {
        this.executeLater.put(txt.getId(), new AbstractMap.SimpleEntry<>(txt, table + ":" + column + ":" + id));
    }

    public void addValueEqualsRegex(EditText txt, String regex) {
        this.controlFieldEqualsRegex(txt, regex);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                controlFieldEqualsRegex(txt, regex);
            }
        };
        textWatchers.put(txt.getId(), textWatcher);
        txt.addTextChangedListener(textWatcher);
    }

    public boolean getState() {
        for (Map.Entry<Integer, Map.Entry<EditText, String>> entry : this.executeLater.entrySet()) {
            String[] field = entry.getValue().getValue().split(":");
            if (field.length == 3) {
                boolean state = this.controlFieldIsDuplicated(entry.getValue().getKey(), field[0], field[1], Long.parseLong(field[2]));
                if (!state) {
                    return false;
                }
            }
        }

        for (Map.Entry<Integer, Boolean> entry : this.states.entrySet()) {
            if (!entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void removeValidator(EditText txt) {
        this.states.remove(txt.getId());
        txt.removeTextChangedListener(this.textWatchers.get(txt.getId()));
        txt.setError(null);
    }

    private void controlFieldIsEmpty(EditText txt) {
        if (txt != null) {
            if (txt.getText() != null) {
                if (txt.getText().toString().isEmpty()) {
                    txt.setError(String.format(this.context.getString(R.string.validator_empty), txt.getHint()));
                    this.states.put(txt.getId(), false);
                } else {
                    txt.setError(null);
                    this.states.put(txt.getId(), true);
                }
            }
        }
    }

    private boolean controlFieldIsDuplicated(EditText txt, String table, String column, long id) {
        if (txt != null) {
            if (txt.getText() != null) {
                if (MainActivity.GLOBALS.getSqLiteGeneral().duplicated(table, column, txt.getText().toString(), "ID<>" + id)) {
                    MessageHelper.printMessage(String.format(this.context.getString(R.string.validator_duplicated), txt.getText().toString(), txt.getHint()), this.context);
                    return false;
                }
            }
        }
        return true;
    }

    private void controlFieldEqualsRegex(EditText txt, String regex) {
        if (txt != null) {
            if (txt.getText() != null) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(txt.getText().toString());
                if (!matcher.matches()) {
                    txt.setError(String.format(this.context.getString(R.string.validator_matches), txt.getHint()));
                    this.states.put(txt.getId(), false);
                    return;
                }
            }
            txt.setError(null);
            this.states.put(txt.getId(), true);
        }
    }

}
