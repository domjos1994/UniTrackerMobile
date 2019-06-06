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

package de.domjos.unitrackermobile.activities;

import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import de.domjos.unibuggerlibrary.model.Filter;
import de.domjos.unibuggerlibrary.model.ListObject;
import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.adapter.ListAdapter;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.helper.Validator;

public final class FilterActivity extends AbstractActivity {
    private ListView lvFilters;
    private ListAdapter filterAdapter;
    private BottomNavigationView navigationView;

    private EditText txtFilterTitle, txtFilterVersion, txtFilterDescription;
    private Spinner spFilterView, spFilterStatus;
    private ArrayAdapter<String> viewAdapter, statusAdapter;

    private Validator filterValidator;
    private Filter currentFilter;
    private Context ctx;

    public FilterActivity() {
        super(R.layout.filter_activity);
    }

    @Override
    protected void initActions() {
        this.lvFilters.setOnItemClickListener((parent, view, position, id) -> {
            ListObject listObject = this.filterAdapter.getItem(position);
            if (listObject != null) {
                this.currentFilter = (Filter) listObject.getDescriptionObject();
                this.manageControls(false, false, true);
                this.objectToControls();
            }
        });
    }

    @Override
    protected void initControls() {
        this.ctx = this.getApplicationContext();
        int spinner = android.R.layout.simple_spinner_item;

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
                        MainActivity.GLOBALS.getSqLiteGeneral().delete("filters", "id", this.currentFilter.getId());
                        this.reload();
                        this.manageControls(false, true, false);
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, FilterActivity.this);
                    }
                    break;
                case R.id.navCancel:
                    this.manageControls(false, true, false);
                    break;
                case R.id.navSave:
                    try {
                        if (this.filterValidator.getState()) {
                            this.controlsToObject();
                            MainActivity.GLOBALS.getSqLiteGeneral().insertOrUpdateFilter(this.currentFilter);
                            this.reload();
                            this.manageControls(false, true, false);
                        } else {
                            MessageHelper.printMessage(this.getString(R.string.validator_no_success), this.getApplicationContext());
                        }
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, FilterActivity.this);
                    }
                    break;
            }
            return true;
        });

        this.lvFilters = this.findViewById(R.id.lvFilters);
        this.filterAdapter = new ListAdapter(this.ctx, R.drawable.ic_filter_list_black_24dp);
        this.lvFilters.setAdapter(this.filterAdapter);
        this.filterAdapter.notifyDataSetChanged();

        this.txtFilterTitle = this.findViewById(R.id.txtFilterTitle);
        this.txtFilterVersion = this.findViewById(R.id.txtFilterVersion);
        this.txtFilterDescription = this.findViewById(R.id.txtFilterDescription);

        this.spFilterView = this.findViewById(R.id.spFilterView);
        this.viewAdapter = new ArrayAdapter<>(this.ctx, spinner, this.getResources().getStringArray(R.array.filter_view));
        this.spFilterView.setAdapter(this.viewAdapter);
        this.viewAdapter.notifyDataSetChanged();

        this.spFilterStatus = this.findViewById(R.id.spFilterStatus);
        this.statusAdapter = new ArrayAdapter<>(this.ctx, spinner, this.getResources().getStringArray(R.array.filter_status));
        this.spFilterStatus.setAdapter(this.statusAdapter);
        this.statusAdapter.notifyDataSetChanged();
    }

    @Override
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {
        this.navigationView.getMenu().getItem(0).setEnabled(!editMode);
        this.navigationView.getMenu().getItem(1).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(2).setEnabled(!editMode && selected);
        this.navigationView.getMenu().getItem(3).setEnabled(editMode);
        this.navigationView.getMenu().getItem(4).setEnabled(editMode);

        this.lvFilters.setEnabled(!editMode);
        this.txtFilterTitle.setEnabled(editMode);
        this.txtFilterVersion.setEnabled(editMode);
        this.spFilterView.setEnabled(editMode);
        this.spFilterStatus.setEnabled(editMode);
        this.txtFilterDescription.setEnabled(editMode);

        if (reset) {
            this.currentFilter = new Filter();
            this.objectToControls();
        }
    }

    @Override
    protected void reload() {
        this.filterAdapter.clear();
        for (Filter filter : MainActivity.GLOBALS.getSqLiteGeneral().getFilters()) {
            this.filterAdapter.add(new ListObject(this.getApplicationContext(), R.drawable.ic_filter_list_black_24dp, filter));
        }
    }

    @Override
    protected void initValidators() {
        this.filterValidator = new Validator(this.ctx);
        this.filterValidator.addEmptyValidator(this.txtFilterTitle);
    }

    private void controlsToObject() {
        this.currentFilter.setTitle(this.txtFilterTitle.getText().toString());
        this.currentFilter.setView(this.spFilterView.getSelectedItem().toString());
        this.currentFilter.setStatus(this.spFilterStatus.getSelectedItem().toString());
        this.currentFilter.setVersion(this.txtFilterVersion.getText().toString());
        this.currentFilter.setDescription(this.txtFilterDescription.getText().toString());
    }

    private void objectToControls() {
        this.txtFilterTitle.setText(this.currentFilter.getTitle());
        for (int i = 0; i <= this.viewAdapter.getCount() - 1; i++) {
            String view = this.viewAdapter.getItem(i);
            if (view != null) {
                if (view.equals(this.currentFilter.getView())) {
                    this.spFilterView.setSelection(i);
                    break;
                }
            }
        }
        for (int i = 0; i <= this.statusAdapter.getCount() - 1; i++) {
            String status = this.statusAdapter.getItem(i);
            if (status != null) {
                if (status.equals(this.currentFilter.getStatus())) {
                    this.spFilterStatus.setSelection(i);
                    break;
                }
            }
        }
        this.txtFilterVersion.setText(this.currentFilter.getVersion());
        this.txtFilterDescription.setText(this.currentFilter.getDescription());
    }
}
