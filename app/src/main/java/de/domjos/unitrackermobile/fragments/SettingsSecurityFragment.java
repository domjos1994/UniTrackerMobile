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

package de.domjos.unitrackermobile.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.MainActivity;
import de.domjos.unitrackermobile.helper.Helper;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SettingsSecurityFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.setPreferencesFromResource(R.xml.pref_security, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SwitchPreference swtPasswordChange = this.findPreference("swtSecurityPassword");
        SwitchPreference swtPassword = this.findPreference("swtSecurityEnable");
        if (swtPasswordChange != null && swtPassword != null) {
            swtPasswordChange.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    if (Boolean.parseBoolean(newValue.toString())) {
                        if (swtPassword.isChecked()) {
                            swtPasswordChange.setPersistent(false);
                            Helper.showPasswordDialog(getActivity(), true, true, () -> triggerRebirth(getActivity()));
                        } else {
                            swtPasswordChange.setPersistent(false);
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, getActivity());
                }
                return true;
            });

            swtPassword.setOnPreferenceChangeListener(((preference, newValue) -> {
                try {
                    if (this.getActivity() != null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit();
                        if (!Boolean.parseBoolean(newValue.toString())) {
                            MainActivity.GLOBALS.getSqLiteGeneral().changePassword("");
                            MainActivity.GLOBALS.setPassword("");
                            editor.putBoolean("swtSecurityEnable", false);
                            editor.apply();
                            triggerRebirth(getActivity());
                        } else {
                            MainActivity.GLOBALS.getSqLiteGeneral().changePassword(MainActivity.GLOBALS.getPassword());
                            editor.putBoolean("swtSecurityEnable", true);
                            editor.apply();
                            Helper.showPasswordDialog(getActivity(), true, true, () -> triggerRebirth(getActivity()));
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, getActivity());
                }
                return true;
            }));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private static void triggerRebirth(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
        Runtime.getRuntime().exit(0);
    }
}
