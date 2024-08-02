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

package de.domjos.unibuggermobile.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.domjos.unibuggermobile.activities.MainActivity;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.fragments.*;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unitrackerlibrary.tools.ConvertHelper;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * @noinspection rawtypes
 */
public class PagerAdapter extends FragmentStateAdapter {
    private final Context context;
    private final AbstractFragment general, notes, descriptions, attachments, custom, history, relation;
    private final IBugService bugService;
    private int count;

    public PagerAdapter(Context context, FragmentManager fm, Lifecycle lifecycle) {
        super(fm, lifecycle);

        this.context = context;
        this.general = new IssueGeneralFragment();
        this.notes = new IssueNotesFragment();
        this.descriptions = new IssueDescriptionsFragment();
        ((IssueDescriptionsFragment) this.descriptions).setAuthentication(MainActivity.GLOBALS.getSettings(this.context).getCurrentAuthentication());
        ((IssueGeneralFragment) this.general).setDescriptionFragment((IssueDescriptionsFragment) this.descriptions);
        this.attachments = new IssueAttachmentsFragment();
        this.custom = new IssueCustomFragment();
        this.history = new IssueHistoryFragment();
        this.relation = new IssueRelationsFragment();
        this.bugService = Helper.getCurrentBugService(context);


        this.count = 7;
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
        if(!this.bugService.getPermissions().listRelations()) {
            this.count--;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
        if(this.bugService.getPermissions().listRelations()) {
            if (position == i) {
                return this.relation;
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
        return new Fragment();
    }

    public void setPid(String pid) {
        this.general.setPid(pid);
    }

    public void setNotificationId(int notificationId) {
        this.general.setNotificationId(notificationId);
        this.descriptions.setNotificationId(notificationId);
        this.notes.setNotificationId(notificationId);
        this.attachments.setNotificationId(notificationId);
        this.relation.setNotificationId(notificationId);
        this.custom.setNotificationId(notificationId);
        this.history.setNotificationId(notificationId);
    }

    public void manageControls(boolean editMode) {
        this.general.manageControls(editMode);
        this.descriptions.manageControls(editMode);
        this.notes.manageControls(editMode);
        this.attachments.manageControls(editMode);
        this.relation.manageControls(editMode);
        this.custom.manageControls(editMode);
        this.history.manageControls(editMode);
    }

    /** @noinspection rawtypes*/
    public void setObject(DescriptionObject object) {
        this.general.setObject(object);
        this.descriptions.setObject(object);
        this.notes.setObject(object);
        this.attachments.setObject(object);
        this.relation.setObject(object);
        this.custom.setObject(object);
        this.history.setObject(object);
    }

    public DescriptionObject getObject() {
        DescriptionObject object = this.general.getObject(new Issue());
        object = this.descriptions.getObject(object);
        object = this.notes.getObject(object);
        object = this.attachments.getObject(object);
        object = this.relation.getObject(object);
        object = this.custom.getObject(object);
        return this.history.getObject(object);
    }

    public boolean validate() {
        return this.general.getValidator().getState() && this.descriptions.getValidator().getState()
                && this.attachments.getValidator().getState() && this.relation.getValidator().getState()
                && this.notes.getValidator().getState() && this.history.getValidator().getState();
    }

    public String getResult() {
        StringBuilder result = new StringBuilder();
        if(!this.general.getValidator().getState()) {
            result.append(this.general.getValidator().getResult()).append("\n");
        }
        if(!this.descriptions.getValidator().getState()) {
            result.append(this.descriptions.getValidator().getResult()).append("\n");
        }
        if(!this.attachments.getValidator().getState()) {
            result.append(this.attachments.getValidator().getResult()).append("\n");
        }
        if(!this.notes.getValidator().getState()) {
            result.append(this.notes.getValidator().getResult()).append("\n");
        }
        if(!this.relation.getValidator().getState()) {
            result.append(this.relation.getValidator().getResult()).append("\n");
        }
        if(!this.history.getValidator().getState()) {
            result.append(this.history.getValidator().getResult()).append("\n");
        }
        return result.toString();
    }

    public CharSequence getTitle(int position) {
        String title = this.context.getString(R.string.issues) + " - ";

        int i = 2;
        switch (position) {
            case 0:
                title += this.context.getString(R.string.issues_general);
                break;
            case 1:
                title += this.context.getString(R.string.issues_descriptions);
                break;
        }

        if (this.bugService.getPermissions().listNotes()) {
            if (position == i) {
                title += this.context.getString(R.string.issues_notes);
            }
            i++;
        }
        if (this.bugService.getPermissions().listAttachments()) {
            if (position == i) {
                title += this.context.getString(R.string.issues_attachments);
            }
            i++;
        }
        if (this.bugService.getPermissions().listRelations()) {
            if (position == i) {
                title += this.context.getString(R.string.issues_relations);
            }
            i++;
        }
        if (this.bugService.getPermissions().listCustomFields()) {
            if (position == i) {
                title += this.context.getString(R.string.issues_custom);
            }
            i++;
        }
        if (this.bugService.getPermissions().listHistory()) {
            if (position == i) {
                title += this.context.getString(R.string.issues_history);
            }
        }

        return title;
    }

    public CharSequence getTabTitle(int position) {
        Drawable drawable = switch (position) {
            case 0 ->
                    ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_issues, R.color.colorOnSurface);
            case 1 ->
                    ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_issues_descriptions, R.color.colorOnSurface);
            default -> null;
        };

        int i = 2;
        if (this.bugService.getPermissions().listNotes()) {
            if (position == i) {
                drawable = ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_issues_notes, R.color.colorOnSurface);
            }
            i++;
        }
        if (this.bugService.getPermissions().listAttachments()) {
            if (position == i) {
                drawable = ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_issues_attachments, R.color.colorOnSurface);
            }
            i++;
        }
        if (this.bugService.getPermissions().listRelations()) {
            if (position == i) {
                drawable = ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_issues, R.color.colorOnSurface);
            }
            i++;
        }
        if (this.bugService.getPermissions().listCustomFields()) {
            if (position == i) {
                drawable = ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_accounts, R.color.colorOnSurface);
            }
            i++;
        }
        if (this.bugService.getPermissions().listHistory()) {
            if (position == i) {
                drawable = ConvertHelper.convertResourcesToDrawable(this.context, R.drawable.icon_issues_history, R.color.colorOnSurface);
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
    public int getItemCount() {
        return this.count;
    }
}