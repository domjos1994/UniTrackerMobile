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

package de.domjos.unibuggermobile.custom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Abstract-Class for Activities
 */
public abstract class AbstractActivity extends AppCompatActivity {
    private int layout;

    /**
     * Constructor with the Layout-Resource-ID
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
    }

    /**
     * Function to initialize the Controls
     * @see #onCreate(Bundle)
     */
    protected abstract void initControls();

    /**
     * Function to initialize the Actions
     * @see #onCreate(Bundle)
     */
    protected abstract void initActions();

    /**
     * Function to initialize the Validators (optional)
     * @see #onCreate(Bundle)
     */
    protected void initValidators() {

    }
}
