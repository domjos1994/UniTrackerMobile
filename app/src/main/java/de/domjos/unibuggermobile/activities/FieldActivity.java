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

package de.domjos.unibuggermobile.activities;

import android.support.design.widget.BottomNavigationView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unibuggerlibrary.model.issues.CustomField;
import de.domjos.unibuggerlibrary.model.projects.Project;
import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.tasks.FieldTask;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.adapter.ListObject;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;
import de.domjos.unibuggermobile.settings.Settings;

public final class FieldActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private ListView lvFields;
    private ListAdapter fieldAdapter;

    private EditText txtFieldTitle, txtFieldDefault;
    private Spinner cmbFieldType;
    private CheckBox chkFieldNullable;
    private ArrayAdapter<String> fieldTypeAdapter;

    private IBugService bugService;
    private IFunctionImplemented permissions;
    private Project currentProject;
    private CustomField currentField;

    private Validator fieldValidator;
    private Settings settings;

    public FieldActivity() {
        super(R.layout.field_activity);
    }

    @Override
    protected void initActions() {
        this.lvFields.setOnItemClickListener((parent, view, position, id) -> {
            ListObject ls = this.fieldAdapter.getItem(position);
            if (ls != null) {
                this.currentField = (CustomField) ls.getDescriptionObject();
                this.manageControls(false, false, true);
                this.objectToControls();
            }
        });
    }

    @Override
    protected void reload() {
        try {
            this.fieldAdapter.clear();
            if (this.permissions.listCustomFields()) {
                for (CustomField customField : new FieldTask(FieldActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications()).execute(0).get()) {
                    this.fieldAdapter.add(new ListObject(this.getApplicationContext(), R.drawable.ic_text_fields_black_24dp, customField));
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, FieldActivity.this);
        }
    }

    @Override
    protected void initControls() {
        this.settings = MainActivity.GLOBALS.getSettings(this.getApplicationContext());

        // init Navigation-View
        this.navigationView = this.findViewById(R.id.nav_view);
        this.navigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navAdd:
                    this.manageControls(true, true, false);
                    break;
                case R.id.navEdit:
                    this.manageControls(true, false, false);
                    break;
                case R.id.navDelete:
                    try {
                        new FieldTask(FieldActivity.this, this.bugService, this.currentProject.getId(), true, this.settings.showNotifications()).execute(this.currentField.getId()).get();
                        this.reload();
                        this.manageControls(false, true, false);
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, FieldActivity.this);
                    }
                    break;
                case R.id.navCancel:
                    this.manageControls(false, true, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.fieldValidator.getState()) {
                            this.controlsToObject();
                            new FieldTask(FieldActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications()).execute(this.currentField).get();
                            this.reload();
                            this.manageControls(false, true, false);
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, FieldActivity.this);
                    }
                    break;
            }
            return true;
        });

        // init controls
        this.lvFields = this.findViewById(R.id.lvFields);
        this.fieldAdapter = new ListAdapter(this.getApplicationContext(), R.drawable.ic_text_fields_black_24dp);
        this.lvFields.setAdapter(this.fieldAdapter);
        this.fieldAdapter.notifyDataSetChanged();

        this.txtFieldTitle = this.findViewById(R.id.txtFieldTitle);
        this.txtFieldDefault = this.findViewById(R.id.txtFieldDefault);
        this.cmbFieldType = this.findViewById(R.id.cmbFieldType);
        this.fieldTypeAdapter = new ArrayAdapter<>(FieldActivity.this, android.R.layout.simple_spinner_item);
        this.cmbFieldType.setAdapter(this.fieldTypeAdapter);
        this.fieldTypeAdapter.notifyDataSetChanged();
        this.chkFieldNullable = this.findViewById(R.id.chkFieldNullable);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.permissions = this.bugService.getPermissions();
        this.currentProject = this.settings.getCurrentProject(FieldActivity.this, this.bugService);
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidators() {
        this.fieldValidator = new Validator(this.getApplicationContext());
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode && this.permissions.addCustomFields());
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected && this.permissions.updateCustomFields());
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected && this.permissions.deleteCustomFields());
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.txtFieldTitle.setEnabled(editMode);
        this.txtFieldDefault.setEnabled(editMode);
        this.cmbFieldType.setEnabled(editMode);
        this.chkFieldNullable.setEnabled(editMode);

        if (reset) {
            this.currentField = new CustomField();
            this.objectToControls();
        }
    }

    private void objectToControls() {
        this.txtFieldTitle.setText(this.currentField.getTitle());
        this.txtFieldDefault.setText(this.currentField.getDefaultValue());
        if (this.currentField.getType() != null) {
            this.cmbFieldType.setSelection(this.fieldTypeAdapter.getPosition(this.currentField.getType().name()));
        }
        this.chkFieldNullable.setChecked(this.currentField.isNullable());
    }

    private void controlsToObject() {
        this.currentField.setTitle(this.txtFieldTitle.getText().toString());
        this.currentField.setDefaultValue(this.txtFieldDefault.getText().toString());
        this.currentField.setType(this.cmbFieldType.getSelectedItemPosition());
        this.currentField.setNullable(this.chkFieldNullable.isChecked());
    }

    private void updateUITrackerSpecific() {
        Authentication.Tracker tracker;
        if (this.settings.getCurrentAuthentication() != null) {
            tracker = this.settings.getCurrentAuthentication().getTracker();
        } else {
            return;
        }

        switch (tracker) {
            case MantisBT:
            case Local:
                for (CustomField.Type type : CustomField.Type.values()) {
                    this.fieldTypeAdapter.add(type.name());
                }
                break;
            case YouTrack:
                this.fieldTypeAdapter.add(CustomField.Type.TEXT.name());
                this.fieldTypeAdapter.add(CustomField.Type.TEXT_AREA.name());
                this.fieldTypeAdapter.add(CustomField.Type.DATE.name());
                this.fieldTypeAdapter.add(CustomField.Type.NUMBER.name());
                break;
        }
    }
}
