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

import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.adapter.ListAdapter;
import de.domjos.unibuggermobile.custom.AbstractActivity;
import de.domjos.unibuggermobile.helper.Validator;

public final class FilterActivity extends AbstractActivity {
    private ListView lvFilters;
    private ListAdapter filterAdapter;
    private BottomNavigationView navigationView;

    private EditText txtFilterTitle;
    private Spinner spFilterView, spFilterStatus, spFilterResolution, spFilterReproducibility;
    private ArrayAdapter<String> viewAdapter, statusAdapter, resolutionAdapter, reproducibilityAdapter;

    private Validator filterValidator;
    private Context ctx;

    public FilterActivity() {
        super(R.layout.filter_activity);
    }

    @Override
    protected void initActions() {

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
                            this.reload();
                            this.manageControls(false, true, false);
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

        this.spFilterView = this.findViewById(R.id.spFilterView);
        this.viewAdapter = new ArrayAdapter<>(this.ctx, spinner);
        this.spFilterView.setAdapter(this.viewAdapter);
        this.viewAdapter.notifyDataSetChanged();

        this.spFilterStatus = this.findViewById(R.id.spFilterStatus);
        this.statusAdapter = new ArrayAdapter<>(this.ctx, spinner);
        this.spFilterStatus.setAdapter(this.statusAdapter);
        this.statusAdapter.notifyDataSetChanged();

        this.spFilterResolution = this.findViewById(R.id.spFilterResolution);
        this.resolutionAdapter = new ArrayAdapter<>(this.ctx, spinner);
        this.spFilterResolution.setAdapter(this.resolutionAdapter);
        this.resolutionAdapter.notifyDataSetChanged();

        this.spFilterReproducibility = this.findViewById(R.id.spFilterReproducibility);
        this.reproducibilityAdapter = new ArrayAdapter<>(this.ctx, spinner);
        this.spFilterReproducibility.setAdapter(this.reproducibilityAdapter);
        this.reproducibilityAdapter.notifyDataSetChanged();
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
        this.spFilterView.setEnabled(editMode);
        this.spFilterStatus.setEnabled(editMode);
        this.spFilterReproducibility.setEnabled(editMode);
        this.spFilterResolution.setEnabled(editMode);
    }

    @Override
    protected void initValidators() {
        this.filterValidator = new Validator(this.ctx);
        this.filterValidator.addEmptyValidator(this.txtFilterTitle);
    }

    private void controlsToObject() {

    }

    private void objectToControls() {

    }
}
