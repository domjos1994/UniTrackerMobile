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

package de.domjos.unibuggerlibrary.tasks;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.model.issues.CustomField;

public final class FieldTask extends AbstractTask<Object, Void, List<CustomField>> {
    private Object project_id;
    private boolean delete;

    public FieldTask(Activity activity, IBugService bugService, Object project_id, boolean delete, boolean showNotifications) {
        super(activity, bugService, R.string.task_field_list_title, R.string.task_field_content, showNotifications);
        this.project_id = project_id;
        this.delete = delete;
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<CustomField> doInBackground(Object... customFields) {
        List<CustomField> result = new LinkedList<>();
        try {
            for (Object customField : customFields) {
                if (customField instanceof CustomField) {
                    super.bugService.insertOrUpdateCustomField((CustomField) customField, this.project_id);
                } else {
                    if (this.delete) {
                        super.bugService.deleteCustomField(super.returnTemp(customField), this.project_id);
                    } else {
                        result.addAll(super.bugService.getCustomFields(this.project_id));
                    }
                }
            }
            //super.printMessage();
        } catch (Exception ex) {
            super.printException(ex);
        }
        return result;
    }
}
