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

package de.domjos.unibuggermobile.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.tasks.FieldTask;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.customwidgets.tokenizer.CommaTokenizer;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;

public final class IssueCustomFragment extends AbstractFragment {
    private View root;
    private Issue issue;
    private boolean editMode;
    private TableLayout tblCustomFields;
    private List<View> views;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.views = new LinkedList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.issue_fragment_custom, container, false);

        this.tblCustomFields = this.root.findViewById(R.id.tblCustomFields);

        this.updateUITrackerSpecific();
        this.initData();
        this.manageControls(this.editMode);
        return root;
    }

    @Override
    public void setObject(DescriptionObject descriptionObject) {
        this.issue = (Issue) descriptionObject;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        Issue issue = (Issue) descriptionObject;

        if (this.root != null) {
            for (View view : this.views) {
                for (Object key : this.issue.getCustomFields().keySet()) {
                    CustomField customField = (CustomField) key;
                    boolean isTheId = false;

                    if (String.valueOf(view.getId()).equals(String.valueOf(customField.getId()))) {
                        isTheId = true;
                    } else if (String.valueOf(view.getTag()).equals(String.valueOf(customField.getId()))) {
                        isTheId = true;
                    }


                    if (isTheId) {
                        if (view instanceof EditText) {
                            issue.getCustomFields().put(key, ((EditText) view).getText().toString());
                        } else if (view instanceof CheckBox) {
                            issue.getCustomFields().put(key, String.valueOf(((CheckBox) view).isChecked()));
                        } else if (view instanceof Spinner) {
                            if(((Spinner) view).getSelectedItem()!=null) {
                                issue.getCustomFields().put(key, ((Spinner) view).getSelectedItem().toString());
                            }
                        } else if (view instanceof TableRow) {
                            TableRow tableRow = (TableRow) view;
                            for (int i = 0; i <= tableRow.getChildCount() - 1; i++) {
                                RadioButton radioButton = (RadioButton) tableRow.getChildAt(i);
                                if (radioButton.isChecked()) {
                                    issue.getCustomFields().put(key, radioButton.getText().toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        return issue;
    }

    @Override
    public void manageControls(boolean editMode) {
        this.editMode = editMode;
        if (this.root != null) {
            for (View view : this.views) {
                view.setEnabled(editMode);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initData() {
        Object pid = MainActivity.GLOBALS.getSettings(getContext()).getCurrentProjectId();

        if (this.issue != null) {
            if (this.getActivity() != null) {
                if (this.issue.getCustomFields().isEmpty()) {
                    try {
                        FieldTask fieldTask = new FieldTask(getActivity(), Helper.getCurrentBugService(getContext()), pid, false, false, R.drawable.icon_custom_fields);
                        List<CustomField> customFields = fieldTask.execute(0).get();
                        for (CustomField customField : customFields) {
                            this.issue.getCustomFields().put(customField, "");
                        }
                    } catch (Exception ignored) {
                    }
                }
                for (Object object : this.issue.getCustomFields().entrySet()) {
                    Map.Entry entry = (Map.Entry) object;
                    CustomField customField = (CustomField) entry.getKey();
                    String value = (String) entry.getValue();

                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 10);
                    TableRow tableRow = new TableRow(this.getActivity());
                    tableRow.setLayoutParams(layoutParams);

                    EditText editText;
                    if (customField.getPossibleValues() != null) {
                        if (!customField.getPossibleValues().isEmpty()) {
                            editText = new AutoCompleteTextView(this.getActivity());
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1);
                            if (customField.getDefaultValue() != null) {
                                for (String item : customField.getDefaultValue().split("\\|")) {
                                    arrayAdapter.add(item.trim());
                                }
                            }
                            ((AutoCompleteTextView) editText).setAdapter(arrayAdapter);
                        } else if (customField.getType() == CustomField.Type.MULTI_SELECT_LIST) {
                            editText = new MultiAutoCompleteTextView(this.getActivity());
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1);
                            if (customField.getDefaultValue() != null) {
                                for (String item : customField.getDefaultValue().split("\\|")) {
                                    arrayAdapter.add(item.trim());
                                }
                            }
                            ((MultiAutoCompleteTextView) editText).setAdapter(arrayAdapter);
                            ((MultiAutoCompleteTextView) editText).setTokenizer(new CommaTokenizer());
                            value = value.replace("|", ";");
                        } else {
                            editText = new EditText(this.getActivity());
                        }
                    } else {
                        editText = new EditText(this.getActivity());
                    }
                    editText.setLayoutParams(layoutParams);
                    editText.setHint(customField.getTitle());
                    if (value.isEmpty()) {
                        editText.setText(customField.getDefaultValue());
                    } else {
                        editText.setText(value);
                    }
                    try {
                        editText.setId(Integer.parseInt(String.valueOf(customField.getId())));
                    } catch (Exception ex) {
                        editText.setTag(String.valueOf(customField.getId()));
                    }

                    switch (customField.getType()) {
                        case TEXT:
                        case MULTI_SELECT_LIST:
                            editText.setInputType(InputType.TYPE_CLASS_TEXT);
                            this.views.add(editText);
                            tableRow.addView(editText);
                            break;
                        case NUMBER:
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            this.views.add(editText);
                            tableRow.addView(editText);
                            break;
                        case FLOATING_NUMBER:
                            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            this.views.add(editText);
                            tableRow.addView(editText);
                            break;
                        case DATE:
                            editText.setInputType(InputType.TYPE_CLASS_DATETIME);
                            this.views.add(editText);
                            tableRow.addView(editText);
                            break;
                        case EMAIL:
                            editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            this.views.add(editText);
                            tableRow.addView(editText);
                            break;
                        case TEXT_AREA:
                            editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            this.views.add(editText);
                            tableRow.addView(editText);
                            break;
                        case CHECKBOX:
                            CheckBox checkBox = new CheckBox(this.getActivity());
                            checkBox.setLayoutParams(layoutParams);
                            checkBox.setText(customField.getTitle());
                            try {
                                checkBox.setId(Integer.parseInt(String.valueOf(customField.getId())));
                            } catch (Exception ex) {
                                checkBox.setTag(String.valueOf(customField.getId()));
                            }
                            if (value.isEmpty()) {
                                if (!customField.getDefaultValue().isEmpty()) {
                                    checkBox.setChecked(Boolean.parseBoolean(customField.getDefaultValue()));
                                }
                            } else {
                                checkBox.setChecked(Boolean.parseBoolean(value));
                            }
                            this.views.add(checkBox);
                            tableRow.addView(checkBox);
                            break;
                        case CHOICE_BOX:
                            if (!customField.getPossibleValues().isEmpty()) {
                                for (String item : customField.getPossibleValues().split("\\|")) {
                                    RadioButton radioButton = new RadioButton(this.getActivity());
                                    radioButton.setLayoutParams(layoutParams);
                                    radioButton.setText(item.split(":")[0].trim());
                                    if (value.isEmpty()) {
                                        if (!customField.getDefaultValue().isEmpty()) {
                                            radioButton.setChecked(customField.getDefaultValue().equals(radioButton.getText().toString()));
                                        }
                                    } else {
                                        radioButton.setChecked(value.equals(radioButton.getText().toString()));
                                    }
                                    tableRow.addView(radioButton);
                                }
                                try {
                                    tableRow.setId(Integer.parseInt(String.valueOf(customField.getId())));
                                } catch (Exception ex) {
                                    tableRow.setTag(String.valueOf(customField.getId()));
                                }
                                this.views.add(tableRow);
                            }
                            break;
                        case LIST:
                        case ENUMERATION:
                            TextView textView = new TextView(this.getActivity());
                            textView.setText(customField.getTitle());
                            layoutParams.weight = 3;
                            textView.setLayoutParams(layoutParams);
                            Spinner spinner = new Spinner(this.getActivity());
                            layoutParams.weight = 7;
                            spinner.setLayoutParams(layoutParams);
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinner_item);
                            if (customField.isNullable()) {
                                if (customField.getDefaultValue() != null) {
                                    arrayAdapter.add(customField.getDefaultValue());
                                }
                            }
                            if (customField.getPossibleValues() != null) {
                                for (String item : customField.getPossibleValues().split("\\|")) {
                                    arrayAdapter.add(item.split(":")[0].trim());
                                }
                            }
                            spinner.setAdapter(arrayAdapter);
                            arrayAdapter.notifyDataSetChanged();
                            try {
                                spinner.setId(Integer.parseInt(String.valueOf(customField.getId())));
                            } catch (Exception ex) {
                                spinner.setTag(String.valueOf(customField.getId()));
                            }
                            String selected = "";
                            if (!value.isEmpty()) {
                                selected = value.trim();
                            } else {
                                if (customField.getDefaultValue() != null) {
                                    if (!customField.getDefaultValue().isEmpty()) {
                                        selected = customField.getDefaultValue().trim();
                                    }
                                }
                            }

                            if (!selected.isEmpty()) {
                                for (int i = 0; i <= arrayAdapter.getCount() - 1; i++) {
                                    if (selected.equals(arrayAdapter.getItem(i))) {
                                        spinner.setSelection(i);
                                        break;
                                    }
                                }
                            }
                            this.views.add(spinner);
                            tableRow.addView(textView);
                            tableRow.addView(spinner);
                            break;
                    }

                    this.tblCustomFields.addView(tableRow, layoutParams);
                }
            }
        }
    }

    @Override
    public Validator initValidator() {
        return null;
    }

    @Override
    public void updateUITrackerSpecific() {
    }
}
