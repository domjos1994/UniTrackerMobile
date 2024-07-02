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

package de.domjos.unibuggermobile.fragments;

import androidx.fragment.app.Fragment;

import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.customwidgets.utils.Validator;

/** @noinspection rawtypes*/
public abstract class AbstractFragment extends Fragment {
    protected int notificationId;
    protected Validator validator = null;

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public abstract void setObject(DescriptionObject descriptionObject);

    public abstract DescriptionObject getObject(DescriptionObject descriptionObject);

    public abstract void manageControls(boolean editMode);

    protected abstract void initData();

    public Validator getValidator() {
        if(this.validator == null) {
            this.initValidator();
        }
        return this.validator;
    }

    protected abstract Validator initValidator();

    public abstract void updateUITrackerSpecific();

    public void setPid(String pid) {

    }
}
