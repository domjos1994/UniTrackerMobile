/*
 * Copyright (C)  2019-2024 Domjos
 * This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
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

package de.domjos.unitrackerlibrary.custom;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

/**
 * A Parent-Class for Activity which contains several useful Functionalities
 * and basic methods to fill
 * @author Dominic Joas
 * @noinspection unused
 */
public abstract class AbstractActivity extends AppCompatActivity {
    private final int id;
    private Map.Entry<String, byte[]> entry;
    private final boolean noBackground;
    private final int resId;
    protected Bundle savedInstanceState;

    /**
     * Constructor with Background-Image for the Activity
     * @param id The Layout-ID from the Resources
     * @param entry The Entry with the Image-Name and the Content (Can be null)
     * @param resId The Image from the Resources (Will be taken if entry is null)
     */
    public AbstractActivity(int id, Map.Entry<String, byte[]> entry, int resId) {
        super();

        this.id = id;
        this.entry = entry;
        this.noBackground = false;
        this.resId = resId;
    }

    /**
     * Constructor with no Image for the Activity
     * @param id The Layout-ID from the Resources
     */
    public AbstractActivity(int id) {
        super();

        this.id = id;
        this.entry = null;
        this.noBackground = true;
        this.resId = 0;
    }

    /**
     * Sets the Background-Entry of the Image
     * @param entry The Entry of the Image
     */
    public void setBackground(Map.Entry<String, byte[]> entry) {
        this.entry = entry;
    }

    /**
     * The OnCreate Method with the Function-Calls from this Class
     * @see androidx.appcompat.app.AppCompatActivity#onCreate(Bundle, PersistableBundle)
     *
     * Notice: Don't call manually when extending this Class
     * @param savedInstanceState The PersistableBundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.id);
        this.initControls();
        this.hideExperimentalFeatures();
        this.initValidator();
        this.initActions();
        if(!this.noBackground) {
            setBackgroundToActivity(this, this.entry, this.resId);
        }
        this.reload();
        this.manageControls(false, true, false);

        this.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onPressBack();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    /**
     * Creates a SnackBar at the bottom of the Activity with the following message
     * @param message the message to show in the SnackBar
     */
    protected void createSnackBar(String message) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        Snackbar snackbar = Snackbar.make(viewGroup, message, BaseTransientBottomBar.LENGTH_LONG);
        snackbar.show();
    }

    /**
     * Method to reload things like lists...
     */
    protected void reload() {

    }

    /**
     * Method to manage the Controls of an Activity
     * @param editMode Controls are enabled
     * @param reset Control-Values will be reseted
     * @param selected Item of a List is selected
     */
    protected void manageControls(boolean editMode, boolean reset, boolean selected) {

    }

    /**
     * Method to initialize the Controls of an Activity
     */
    protected abstract void initControls();

    /**
     * Method to initialize the Validators
     */
    protected void initValidator() {}

    /**
     * Method to initialize the Actions like onClick of an Activity
     */
    protected abstract void initActions();

    /**
     * Method to hide experimental Features of an Activity
     */
    protected void hideExperimentalFeatures() {}

    public static void setBackgroundToActivity(Activity activity, Map.Entry<String, byte[]> entry, int resId) {
        if(entry!=null && !entry.getKey().isEmpty()) {
            activity.getWindow().getDecorView().getRootView().setBackground(new BitmapDrawable(activity.getResources(), BitmapFactory.decodeByteArray(entry.getValue(), 0, entry.getValue().length)));
            return;
        }
        activity.getWindow().getDecorView().getRootView().setBackgroundResource(resId);
    }

    protected void onPressBack() {

    }
}
