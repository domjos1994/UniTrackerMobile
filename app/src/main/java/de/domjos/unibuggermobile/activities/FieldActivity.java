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

package de.domjos.unibuggermobile.activities;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.interfaces.IFunctionImplemented;
import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.tasks.FieldTask;
import de.domjos.unibuggermobile.R;
import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unibuggermobile.helper.Validator;
import de.domjos.unibuggermobile.settings.Settings;

public final class FieldActivity extends AbstractActivity {
    private BottomNavigationView navigationView;

    private SwipeRefreshDeleteList lvFields;

    private EditText txtFieldTitle, txtFieldDefault, txtFieldPossibleValues;
    private Spinner cmbFieldType;
    private CheckBox chkFieldNullable;
    private ArrayAdapter<CustomField.Type> fieldTypeAdapter;

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
        this.lvFields.setOnClickListener((SwipeRefreshDeleteList.SingleClickListener) listObject -> {
            if (listObject != null) {
                currentField = (CustomField) listObject.getObject();
                manageControls(false, false, true);
                objectToControls();
            }
        });

        this.lvFields.setOnDeleteListener(listObject -> {
            try {
                new FieldTask(FieldActivity.this, bugService, currentProject.getId(), true, settings.showNotifications(), R.drawable.ic_text_fields_black_24dp).execute(((CustomField)listObject.getObject()).getId()).get();
                manageControls(false, true, false);
            } catch (Exception ex) {
                MessageHelper.printException(ex, R.mipmap.ic_launcher_round, FieldActivity.this);
            }
        });

        this.lvFields.setOnReloadListener(this::reload);
    }

    @Override
    protected void reload() {
        try {
            this.lvFields.getAdapter().clear();
            if (this.permissions.listCustomFields()) {
                if(this.currentProject!=null) {
                    for (CustomField customField : new FieldTask(FieldActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications(), R.drawable.ic_text_fields_black_24dp).execute(0).get()) {
                        BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                        baseDescriptionObject.setObject(customField);
                        baseDescriptionObject.setTitle(customField.getTitle());
                        baseDescriptionObject.setDescription(customField.getDescription());
                        this.lvFields.getAdapter().add(baseDescriptionObject);
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, FieldActivity.this);
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
                        new FieldTask(FieldActivity.this, this.bugService, this.currentProject.getId(), true, this.settings.showNotifications(), R.drawable.ic_text_fields_black_24dp).execute(this.currentField.getId()).get();
                        this.reload();
                        this.manageControls(false, true, false);
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, R.mipmap.ic_launcher_round, FieldActivity.this);
                    }
                    break;
                case R.id.navCancel:
                    this.manageControls(false, true, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.fieldValidator.getState()) {
                            this.controlsToObject();
                            new FieldTask(FieldActivity.this, this.bugService, this.currentProject.getId(), false, this.settings.showNotifications(), R.drawable.ic_text_fields_black_24dp).execute(this.currentField).get();
                            this.reload();
                            this.manageControls(false, true, false);
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), R.mipmap.ic_launcher_round, this.getApplicationContext());
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, R.mipmap.ic_launcher_round, FieldActivity.this);
                    }
                    break;
            }
            return true;
        });

        // init controls
        this.lvFields = this.findViewById(R.id.lvFields);
        this.txtFieldTitle = this.findViewById(R.id.txtFieldTitle);
        this.txtFieldDefault = this.findViewById(R.id.txtFieldDefault);
        this.txtFieldPossibleValues = this.findViewById(R.id.txtFieldPossibleValues);
        this.cmbFieldType = this.findViewById(R.id.cmbFieldType);
        this.fieldTypeAdapter = new ArrayAdapter<>(FieldActivity.this, R.layout.spinner_item);
        this.cmbFieldType.setAdapter(this.fieldTypeAdapter);
        this.fieldTypeAdapter.notifyDataSetChanged();
        this.chkFieldNullable = this.findViewById(R.id.chkFieldNullable);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
        this.permissions = this.bugService.getPermissions();
        this.currentProject = this.settings.getCurrentProject(FieldActivity.this, this.bugService);
        this.updateUITrackerSpecific();
    }

    @Override
    protected void initValidator() {
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
        this.txtFieldPossibleValues.setEnabled(editMode);
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
        this.txtFieldPossibleValues.setText(this.currentField.getPossibleValues());
        if (this.currentField.getType() != null) {
            this.cmbFieldType.setSelection(this.fieldTypeAdapter.getPosition(this.currentField.getType()));
        }
        this.chkFieldNullable.setChecked(this.currentField.isNullable());
    }

    private void controlsToObject() {
        this.currentField.setTitle(this.txtFieldTitle.getText().toString());
        this.currentField.setDefaultValue(this.txtFieldDefault.getText().toString());
        this.currentField.setPossibleValues(this.txtFieldPossibleValues.getText().toString());
        this.currentField.setType(Objects.requireNonNull(this.fieldTypeAdapter.getItem(this.cmbFieldType.getSelectedItemPosition())));
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
                    this.fieldTypeAdapter.add(type);
                }
                break;
            case YouTrack:
                this.fieldTypeAdapter.add(CustomField.Type.TEXT);
                this.fieldTypeAdapter.add(CustomField.Type.TEXT_AREA);
                this.fieldTypeAdapter.add(CustomField.Type.DATE);
                this.fieldTypeAdapter.add(CustomField.Type.NUMBER);
                break;
            case Backlog:
                this.fieldTypeAdapter.add(CustomField.Type.TEXT);
                this.fieldTypeAdapter.add(CustomField.Type.TEXT_AREA);
                this.fieldTypeAdapter.add(CustomField.Type.DATE);
                this.fieldTypeAdapter.add(CustomField.Type.NUMBER);
                this.fieldTypeAdapter.add(CustomField.Type.LIST);
                this.fieldTypeAdapter.add(CustomField.Type.MULTI_SELECT_LIST);
                break;
        }
    }
}
