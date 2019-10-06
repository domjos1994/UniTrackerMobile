/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

package de.domjos.unitrackermobile.custom;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Abstract-Class for Activities
 */
public abstract class AbstractActivity extends AppCompatActivity {
    private int layout;

    /**
     * Constructor with the Layout-Resource-ID
     *
     * @param layout The Layout-Resource-ID
     */
    public AbstractActivity(int layout) {
        this.layout = layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.layout);
        this.initControls();
        this.initValidators();
        this.initActions();
        this.manageControls(false, true, false);
        this.reload();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Function to initialize the Controls
     *
     * @see #onCreate(Bundle)
     */
    protected abstract void initControls();

    /**
     * Function to initialize the Actions
     *
     * @see #onCreate(Bundle)
     */
    protected abstract void initActions();

    /**
     * Function to initialize the Validators (optional)
     *
     * @see #onCreate(Bundle)
     */
    protected void initValidators() {

    }

    /**
     * Function to manage the controls (optional)
     *
     * @param editMode controls are in editMode
     * @param reset    controls should be reset
     * @param selected list item is selected
     */
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {

    }

    /**
     * Function to reload List-View
     */
    protected void reload() {

    }
}
