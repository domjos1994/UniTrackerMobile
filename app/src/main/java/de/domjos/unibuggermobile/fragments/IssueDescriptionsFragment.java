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

package de.domjos.unibuggermobile.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;
import de.domjos.unibuggermobile.R;

/**
 * A placeholder fragment containing a simple view.
 */
public final class IssueDescriptionsFragment extends AbstractFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.issue_fragment_descriptions, container, false);

        return root;
    }

    @Override
    public void setObject(DescriptionObject descriptionObject) {

    }

    @Override
    public DescriptionObject getObject(DescriptionObject descriptionObject) {
        return null;
    }

    @Override
    public void manageControls(boolean editMode, boolean reset, boolean selected) {

    }
}