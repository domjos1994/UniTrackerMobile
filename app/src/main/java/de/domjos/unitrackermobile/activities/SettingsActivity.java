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

package de.domjos.unitrackermobile.activities;

import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.custom.AbstractActivity;
import de.domjos.unitrackermobile.fragments.SettingsFragment;

public final class SettingsActivity extends AbstractActivity {

    public SettingsActivity() {
        super(R.layout.settings_activity);
    }

    @Override
    protected void initControls() {
        this.getSupportFragmentManager().beginTransaction().replace(R.id.llSettings, new SettingsFragment()).commit();
    }

    @Override
    protected void initActions() {

    }
}
