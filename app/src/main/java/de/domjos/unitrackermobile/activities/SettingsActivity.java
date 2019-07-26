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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;

import java.util.List;

import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.helper.Helper;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public final class SettingsActivity extends PreferenceActivity {
    private AppCompatDelegate mDelegate;
    private boolean loadHeaders = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    @Override
    @NonNull
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (!this.loadHeaders) {
            loadHeadersFromResource(R.xml.pref_headers, target);
            this.loadHeaders = true;
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName) || SecurityPreferenceFragment.class.getName().equals(fragmentName) || InternetPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
        }
    }

    public static class InternetPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_internet);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
        }
    }

    public static class SecurityPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_security);

            SwitchPreference swtPasswordChange = (SwitchPreference) this.findPreference("swtSecurityPassword");
            SwitchPreference swtPassword = (SwitchPreference) this.findPreference("swtSecurityEnable");
            swtPasswordChange.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    if(Boolean.parseBoolean(newValue.toString())) {
                        if(swtPassword.isChecked()) {
                            swtPasswordChange.getEditor().putBoolean("swtSecurityPassword", false).apply();
                            Helper.showPasswordDialog(getActivity(), true, true, () -> triggerRebirth(getActivity()));
                        } else {
                            swtPasswordChange.getEditor().putBoolean("swtSecurityPassword", false).apply();
                        }
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, getActivity());
                }
                return true;
            });

            swtPassword.setOnPreferenceChangeListener(((preference, newValue) -> {
                try {
                    if(!Boolean.parseBoolean(newValue.toString())) {
                        MainActivity.GLOBALS.getSqLiteGeneral().changePassword("");
                        MainActivity.GLOBALS.setPassword("");
                        triggerRebirth(getActivity());
                    } else {
                        MainActivity.GLOBALS.getSqLiteGeneral().changePassword(MainActivity.GLOBALS.getPassword());
                        Helper.showPasswordDialog(getActivity(), true, true, () -> triggerRebirth(getActivity()));
                    }
                } catch (Exception ex) {
                    MessageHelper.printException(ex, getActivity());
                }
                return true;
            }));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            return super.onOptionsItemSelected(item);
        }
    }

    public static void triggerRebirth(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
        Runtime.getRuntime().exit(0);
    }
}
