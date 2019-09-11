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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.Toolbar;

import com.takusemba.spotlight.OnTargetStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.RoundedRectangle;
import com.takusemba.spotlight.target.SimpleTarget;
import com.takusemba.spotlight.target.Target;

import java.util.ArrayList;

import de.domjos.unitrackermobile.R;

class SpotlightHelper {
    private ArrayList<Target> targets;
    private Activity activity;
    private final int OVERLAY_X, OVERLAY_Y;

    SpotlightHelper(Activity activity) {
        this.targets = new ArrayList<>();
        this.activity = activity;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.OVERLAY_X = displayMetrics.widthPixels / 20;
        this.OVERLAY_Y = displayMetrics.heightPixels / 2;
    }

    void addTarget(View view, int title, int description, Runnable start, Runnable end) {
        float width = (view.getWidth() / 2f) + ((int) (view.getWidth() * 0.01));
        float height = (view.getHeight() / 2f) + ((int) (view.getHeight() * 0.01));

        int[] location = new int[2];
        view.getLocationInWindow(location);
        int x = location[0] + view.getWidth() / 2;
        int y = location[1] + view.getHeight() / 2;

        RoundedRectangle rectangle = new RoundedRectangle(height, width, view.getWidth() / 2f);

        SimpleTarget target = new SimpleTarget.Builder(this.activity)
                .setPoint(x, y)
                .setShape(rectangle)
                .setTitle(this.activity.getString(title))
                .setDescription(this.activity.getString(description))
                .setOverlayPoint(this.OVERLAY_X, this.OVERLAY_Y)
                .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                    @Override
                    public void onStarted(SimpleTarget target) {
                        if (start != null) {
                            start.run();
                        }
                    }

                    @Override
                    public void onEnded(SimpleTarget target) {
                        if (end != null) {
                            end.run();
                        }
                    }
                })
                .build();

        this.targets.add(target);
    }

    void addTarget(int x, int y, int height, int width, int title, int description, Runnable start, Runnable end) {
        RoundedRectangle rectangle = new RoundedRectangle(height, width, width / 2f);

        SimpleTarget target = new SimpleTarget.Builder(this.activity)
                .setPoint(x, y)
                .setShape(rectangle)
                .setTitle(this.activity.getString(title))
                .setDescription(this.activity.getString(description))
                .setOverlayPoint(this.OVERLAY_X, this.OVERLAY_Y)
                .setOnSpotlightStartedListener(new OnTargetStateChangedListener<SimpleTarget>() {
                    @Override
                    public void onStarted(SimpleTarget target) {
                        if (start != null) {
                            start.run();
                        }
                    }

                    @Override
                    public void onEnded(SimpleTarget target) {
                        if (end != null) {
                            end.run();
                        }
                    }
                })
                .build();

        this.targets.add(target);
    }

    void addTargetToHamburger(Toolbar toolbar, int description, Runnable end) {
        this.addTarget(this.getNavigationIconView(toolbar), R.string.messages_tutorial_new_account_title, description, null, end);
    }

    void show() {
        Spotlight spotlight = Spotlight.with(this.activity)
                .setOverlayColor(R.color.background)
                .setDuration(100L)
                .setAnimation(new DecelerateInterpolator(2f))
                .setTargets(this.targets)
                .setClosedOnTouchedOutside(true);
        spotlight.start();
    }

    private View getNavigationIconView(Toolbar toolbar) {
        String previousContentDescription = (String) toolbar.getNavigationContentDescription();
        boolean hadContentDescription = !TextUtils.isEmpty(previousContentDescription);
        String contentDescription = hadContentDescription ? previousContentDescription : "navigationIcon";
        toolbar.setNavigationContentDescription(contentDescription);

        ArrayList<View> potentialViews = new ArrayList<>();
        toolbar.findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        View navIcon = null;
        if (potentialViews.size() > 0) {
            navIcon = potentialViews.get(0);
        }

        if (!hadContentDescription) {
            toolbar.setNavigationContentDescription(previousContentDescription);
        }

        return navIcon;
    }
}
