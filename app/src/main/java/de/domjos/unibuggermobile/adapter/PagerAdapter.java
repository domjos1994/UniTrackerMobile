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

package de.domjos.unibuggermobile.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.fragments.AbstractFragment;
import de.domjos.unibuggermobile.fragments.IssueAttachmentsFragment;
import de.domjos.unibuggermobile.fragments.IssueDescriptionsFragment;
import de.domjos.unibuggermobile.fragments.IssueGeneralFragment;
import de.domjos.unibuggermobile.fragments.IssueNotesFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.issues_general, R.string.issues_descriptions, R.string.issues_notes, R.string.issues_attachments};
    private final Context context;

    private AbstractFragment general, notes, descriptions, attachments;

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        this.general = new IssueGeneralFragment();
        this.notes = new IssueNotesFragment();
        this.descriptions = new IssueDescriptionsFragment();
        this.attachments = new IssueAttachmentsFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return this.general;
            case 1:
                return this.descriptions;
            case 2:
                return this.notes;
            case 3:
                return this.attachments;
            default:
                return null;
        }
    }

    public void setPid(String pid) {
        this.general.setPid(pid);
    }

    public void manageControls(boolean editMode) {
        this.general.manageControls(editMode);
        this.descriptions.manageControls(editMode);
        this.notes.manageControls(editMode);
        this.attachments.manageControls(editMode);
    }

    public void setObject(DescriptionObject object) {
        this.general.setObject(object);
        this.descriptions.setObject(object);
        this.notes.setObject(object);
        this.attachments.setObject(object);
    }

    public DescriptionObject getObject() {
        DescriptionObject object = this.general.getObject(new Issue());
        object = this.descriptions.getObject(object);
        object = this.notes.getObject(object);
        return this.attachments.getObject(object);
    }

    public boolean validate() {
        return this.general.initValidator().getState() && this.descriptions.initValidator().getState() && this.attachments.initValidator().getState() && this.notes.initValidator().getState();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.context.getResources().getString(PagerAdapter.TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 4;
    }
}