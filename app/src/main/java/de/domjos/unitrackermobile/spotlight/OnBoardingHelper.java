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

package de.domjos.unitrackermobile.spotlight;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import de.domjos.unibuggerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.activities.AccountActivity;
import de.domjos.unitrackermobile.activities.MainActivity;

public class OnBoardingHelper {
    private static final String ON_BOARDING = "onBoarding";
    private static final int ON_BOARDING_ID = 145;
    private static boolean secondSteps;

    public static void startTutorial(boolean firstLogIn, Activity activity, Toolbar toolbar, DrawerLayout drawerLayout, NavigationView navigationView, ImageView ivMainCover) {
        if (!firstLogIn && !OnBoardingHelper.secondSteps || MainActivity.GLOBALS.getSettings(activity).isShowTutorial()) {

            SpotlightHelper helper = new SpotlightHelper(activity);
            helper.addTargetToHamburger(toolbar, R.string.messages_tutorial_new_account_hamburger, () -> drawerLayout.openDrawer(navigationView));
            helper.show();

            drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                }

                @Override
                public void onDrawerOpened(@NonNull View drawerView) {
                    SpotlightHelper helper = new SpotlightHelper(activity);
                    helper.addTarget(ivMainCover, R.string.messages_tutorial_new_account_title, R.string.messages_tutorial_new_account_drawer_header, null, () -> {
                        Intent intent = new Intent(activity.getApplicationContext(), AccountActivity.class);
                        intent.putExtra(OnBoardingHelper.ON_BOARDING, true);
                        activity.startActivityForResult(intent, OnBoardingHelper.ON_BOARDING_ID);
                    });
                    helper.show();
                    drawerLayout.removeDrawerListener(this);
                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                }
            });
        }
    }

    public static void tutorialStep2(Activity activity, Runnable editModeRunnable, ScrollView tbl) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(1000);
                    Intent intent = activity.getIntent();
                    if (intent.hasExtra(OnBoardingHelper.ON_BOARDING)) {
                        if (intent.getBooleanExtra(OnBoardingHelper.ON_BOARDING, false)) {
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int y = displayMetrics.heightPixels;
                            int width = displayMetrics.widthPixels / 5;
                            int height = displayMetrics.widthPixels / 5;

                            activity.runOnUiThread(() -> {
                                SpotlightHelper helper = new SpotlightHelper(activity);
                                helper.addTarget(10 + width / 2, y - (width / 2), height, width, R.string.messages_tutorial_new_account_title, R.string.messages_tutorial_new_account_add, null, editModeRunnable);
                                helper.addTarget(tbl, R.string.messages_tutorial_new_account_title, R.string.messages_tutorial_new_account_save, null, null);
                                helper.show();
                            });
                        }
                    }
                } catch (Exception ex) {
                    activity.runOnUiThread(() -> MessageHelper.printException(ex, activity));
                }
                return null;
            }
        };
        task.execute();
    }

    public static void tutorialStep3(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent.hasExtra(OnBoardingHelper.ON_BOARDING)) {
            if (intent.getBooleanExtra(OnBoardingHelper.ON_BOARDING, false)) {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            }
        }
    }

    public static void tutorialStep4(int resultCode, int requestCode, Activity activity, Spinner spMainAccounts, Runnable reload) {
        if (resultCode == Activity.RESULT_OK && requestCode == OnBoardingHelper.ON_BOARDING_ID) {
            OnBoardingHelper.secondSteps = true;
            SpotlightHelper spotlightHelper = new SpotlightHelper(activity);
            spotlightHelper.addTarget(spMainAccounts, R.string.messages_tutorial_new_account_title, R.string.messages_tutorial_new_account_choose, null, reload);
            spotlightHelper.show();
        }
    }

    public static void tutorialStep5(Activity activity, Spinner spMainProjects, DrawerLayout drawerLayout, NavigationView navigationView) {
        if (OnBoardingHelper.secondSteps) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            }
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Thread.sleep(1000);
                        activity.runOnUiThread(() -> {
                            SpotlightHelper spotlightHelper = new SpotlightHelper(activity);
                            spotlightHelper.addTarget(spMainProjects, R.string.messages_tutorial_new_account_title, R.string.messages_tutorial_new_account_project, null, null);
                            spotlightHelper.show();
                        });
                        OnBoardingHelper.secondSteps = false;
                    } catch (Exception ex) {
                        activity.runOnUiThread(() -> MessageHelper.printException(ex, activity));
                    }
                    return null;
                }
            };
            task.execute();
        }
    }
}
