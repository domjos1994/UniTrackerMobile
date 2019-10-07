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

package de.domjos.unitrackermobile.helper;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.settings.Settings;

public class AdHelper {
    private InterstitialAd interstitialAd;
    private Settings settings;
    private Activity activity;

    public AdHelper(Settings settings, Activity activity) {
        this.settings = settings;
        this.activity = activity;

        MobileAds.initialize(activity, initializationStatus -> {});
        this.interstitialAd = new InterstitialAd(activity);
        // ToDo replace by ca-app-pub-4983888966373182/5163399444
        this.interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        this.interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public void show() {
        try {
            if(!this.settings.showAds()) {
                this.interstitialAd.show();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.activity);
        }
    }
}
