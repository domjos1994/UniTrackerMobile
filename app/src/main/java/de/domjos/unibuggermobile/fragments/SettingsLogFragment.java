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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import de.domjos.customwidgets.utils.LogHelper;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.unibuggermobile.R;

public class SettingsLogFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.setPreferencesFromResource(R.xml.pref_log, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (super.getActivity() != null) {
            SwitchPreference switchPreference = this.findPreference("swtLogClear");
            if (switchPreference != null) {
                switchPreference.setOnPreferenceChangeListener((preference1, newValue) -> {
                    try {
                        LogHelper logHelper = new LogHelper(super.getActivity());
                        logHelper.clearFile();
                    } catch (Exception ex) {
                        MessageHelper.printException(ex, R.mipmap.ic_launcher_round, super.getActivity());
                    } finally {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(super.getActivity()).edit();
                        editor.putBoolean("swtLogClear", false);
                        editor.apply();
                    }
                    return true;
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

