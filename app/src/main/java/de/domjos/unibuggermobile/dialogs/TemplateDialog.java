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

package de.domjos.unibuggermobile.dialogs;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.materialswitch.MaterialSwitch;

import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unibuggermobile.helper.SQLiteGeneral;
import de.domjos.unitrackerlibrary.model.Template;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tools.Notifications;

public class TemplateDialog extends AbstractDialog {
    private AutoCompleteTextView txtTemplateName;
    private MaterialSwitch chkTemplateDefault;
    private ArrayAdapter<Template> templateAdapter;
    private final SQLiteGeneral sql;
    private final Authentication authentication;
    private final Type type;

    public TemplateDialog(Activity activity, Issue<?> issue) {
        this(activity, Type.MANAGE, null, issue);
    }

    public TemplateDialog(Activity activity, boolean def, OnSelect onSelect) {
        this(activity, def ? Type.SELECT_DEFAULT : Type.SELECT, onSelect, null);
    }

    private TemplateDialog(Activity activity, Type type, OnSelect onSelect, Issue<?> issue) {
        super(activity, R.layout.template_dialog);
        this.type = type;
        this.sql = MainActivity.GLOBALS.getSqLiteGeneral();
        this.authentication = MainActivity.GLOBALS.getSettings(this.activity).getCurrentAuthentication();

        switch (type) {
            case SELECT -> {
                this.setTitle(R.string.template_title_choose);
                this.setOnSubmit(R.string.template_button_select, (a, b) -> {
                    if(onSelect != null) {
                        String name = this.txtTemplateName.getText().toString();
                        Template template = this.getTemplate(name);
                        if(template != null) {
                            onSelect.select(template);
                        }
                    }
                });
            }
            case SELECT_DEFAULT -> {
                for(Template template : this.sql.getTemplates(this.authentication)) {
                    if(template.isUseAsDefault()) {
                        if(onSelect != null) {
                            onSelect.select(template);
                        }
                    }
                }
            }
            case MANAGE -> {
                this.setTitle(R.string.template_title_manage);
                this.setOnCancel(R.string.template_button_delete, (a, b) -> {
                    try {
                        String name = this.txtTemplateName.getText().toString();
                        Template template = this.getTemplate(name);
                        this.sql.deleteTemplate(template);
                        Notifications.printMessage(activity, activity.getString(R.string.template_action_success), R.mipmap.ic_launcher_round);
                        this.reload();
                    } catch (Exception ex) {
                        Notifications.printException(activity, ex, R.mipmap.ic_launcher_round);
                    }
                });
                this.setOnSubmit(R.string.template_button_save, (a, b) -> {
                    try {
                        if(issue != null) {
                            Template template = new Template();
                            template.setTitle(this.txtTemplateName.getText().toString());
                            template.setUseAsDefault(this.chkTemplateDefault.isChecked());
                            template.setAuthentication(this.authentication);
                            template.setContent(issue);
                            this.sql.updateOrInsertTemplate(template);
                            Notifications.printMessage(activity, activity.getString(R.string.template_action_success), R.mipmap.ic_launcher_round);
                            this.reload();
                        }
                    } catch (Exception ex) {
                        Notifications.printException(activity, ex, R.mipmap.ic_launcher_round);
                    }
                });
            }
        }
    }

    @Override
    public void show() {
        if(this.type != Type.SELECT_DEFAULT) {
            super.show();
        }
    }

    @Override
    protected void init(View view) {
        this.txtTemplateName = view.findViewById(R.id.txtTemplateTitle);
        this.chkTemplateDefault = view.findViewById(R.id.chkTemplateSetAsDefault);

        // init Adapter
        this.templateAdapter = new ArrayAdapter<>(this.activity, android.R.layout.simple_spinner_dropdown_item);
        this.txtTemplateName.setAdapter(this.templateAdapter);
        this.templateAdapter.notifyDataSetChanged();

        switch (type) {
            case SELECT, SELECT_DEFAULT ->  this.chkTemplateDefault.setVisibility(View.GONE);
            case MANAGE ->  this.chkTemplateDefault.setVisibility(View.VISIBLE);
        }

        this.txtTemplateName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                Template template = getTemplate(editable.toString());
                chkTemplateDefault.setChecked(false);
                if(template != null) {
                    chkTemplateDefault.setChecked(template.isUseAsDefault());
                }
            }
        });

        this.reload();
    }

    private void reload() {
        this.txtTemplateName.setText("");
        this.chkTemplateDefault.setChecked(false);

        this.templateAdapter.clear();
        for(Template template : this.sql.getTemplates(this.authentication)) {
            this.templateAdapter.add(template);
        }
    }

    private Template getTemplate(String name) {
        for(int i = 0; i<=this.templateAdapter.getCount() - 1; i++) {
            Template template = this.templateAdapter.getItem(i);
            if(template != null) {
                if(name.equals(template.getTitle())) {
                    return template;
                }
            }
        }
        return null;
    }

    public enum Type {
        SELECT,
        SELECT_DEFAULT,
        MANAGE
    }

    @FunctionalInterface
    public interface OnSelect {
        void select(Template template);
    }
}
