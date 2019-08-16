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

package de.domjos.unitrackermobile.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.Issue;
import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackermobile.R;
import de.domjos.unitrackermobile.fragments.AbstractFragment;
import de.domjos.unitrackermobile.fragments.IssueAttachmentsFragment;
import de.domjos.unitrackermobile.fragments.IssueCustomFragment;
import de.domjos.unitrackermobile.fragments.IssueDescriptionsFragment;
import de.domjos.unitrackermobile.fragments.IssueGeneralFragment;
import de.domjos.unitrackermobile.fragments.IssueHistoryFragment;
import de.domjos.unitrackermobile.fragments.IssueNotesFragment;
import de.domjos.unitrackermobile.helper.Helper;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private final Context context;
    private AbstractFragment general, notes, descriptions, attachments, custom, history;
    private IBugService bugService;
    private int count;

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        this.general = new IssueGeneralFragment();
        this.notes = new IssueNotesFragment();
        this.descriptions = new IssueDescriptionsFragment();
        ((IssueGeneralFragment) this.general).setDescriptionFragment((IssueDescriptionsFragment) this.descriptions);
        this.attachments = new IssueAttachmentsFragment();
        this.custom = new IssueCustomFragment();
        this.history = new IssueHistoryFragment();
        this.bugService = Helper.getCurrentBugService(context);
        this.count = 6;
        if (!this.bugService.getPermissions().listNotes()) {
            this.count--;
        }
        if (!this.bugService.getPermissions().listAttachments()) {
            this.count--;
        }
        if (!this.bugService.getPermissions().listCustomFields()) {
            this.count--;
        }
        if (!this.bugService.getPermissions().listHistory()) {
            this.count--;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return this.general;
            case 1:
                return this.descriptions;
        }
        int i = 2;
        if (this.bugService.getPermissions().listNotes()) {
            if (position == i) {
                return this.notes;
            }
            i++;
        }
        if (this.bugService.getPermissions().listAttachments()) {
            if (position == i) {
                return this.attachments;
            }
            i++;
        }
        if (this.bugService.getPermissions().listCustomFields()) {
            if (position == i) {
                return this.custom;
            }
            i++;
        }
        if (this.bugService.getPermissions().listHistory()) {
            if (position == i) {
                return this.history;
            }
        }
        return null;
    }

    public void setPid(String pid) {
        this.general.setPid(pid);
    }

    public void manageControls(boolean editMode) {
        this.general.manageControls(editMode);
        this.descriptions.manageControls(editMode);
        this.notes.manageControls(editMode);
        this.attachments.manageControls(editMode);
        this.custom.manageControls(editMode);
        this.history.manageControls(editMode);
    }

    public void setObject(DescriptionObject object) {
        this.general.setObject(object);
        this.descriptions.setObject(object);
        this.notes.setObject(object);
        this.attachments.setObject(object);
        this.custom.setObject(object);
        this.history.setObject(object);
    }

    public DescriptionObject getObject() {
        DescriptionObject object = this.general.getObject(new Issue());
        object = this.descriptions.getObject(object);
        object = this.notes.getObject(object);
        object = this.attachments.getObject(object);
        object = this.custom.getObject(object);
        return this.history.getObject(object);
    }

    public boolean validate() {
        return this.general.initValidator().getState() && this.descriptions.initValidator().getState()
                && this.attachments.initValidator().getState() && this.notes.initValidator().getState()
                && this.history.initValidator().getState();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        Drawable drawable = null;

        switch (position) {
            case 0:
                drawable = context.getResources().getDrawable(R.drawable.ic_bug_report_black_24dp);
                break;
            case 1:
                drawable = context.getResources().getDrawable(R.drawable.ic_description_black_24dp);
                break;
        }

        int i = 2;
        if (this.bugService.getPermissions().listNotes()) {
            if (position == i) {
                drawable = context.getResources().getDrawable(R.drawable.ic_note_black_24dp);
            }
            i++;
        }
        if (this.bugService.getPermissions().listAttachments()) {
            if (position == i) {
                drawable = context.getResources().getDrawable(R.drawable.ic_file_upload_black_24dp);
            }
            i++;
        }
        if (this.bugService.getPermissions().listCustomFields()) {
            if (position == i) {
                drawable = context.getResources().getDrawable(R.drawable.ic_account_circle_black_24dp);
            }
            i++;
        }
        if (this.bugService.getPermissions().listHistory()) {
            if (position == i) {
                drawable = context.getResources().getDrawable(R.drawable.ic_history_black_24dp);
            }
        }

        if (drawable != null) {
            SpannableStringBuilder sb = new SpannableStringBuilder(" ");
            try {
                drawable.setBounds(5, 5, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE);
                sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception ignored) {
            }
            return sb;
        }
        return null;
    }

    @Override
    public int getCount() {
        return this.count;
    }
}