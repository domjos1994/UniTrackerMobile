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

package de.domjos.unibuggerlibrary.tasks.general;

import android.app.Activity;
import android.os.AsyncTask;

import de.domjos.unibuggerlibrary.R;
import de.domjos.unibuggerlibrary.interfaces.IBugService;
import de.domjos.unibuggerlibrary.utils.MessageHelper;

public abstract class AbstractTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Activity activity;
    protected final IBugService bugService;
    private final int icon;
    private int id;
    private final String title, content;
    private boolean showNotifications;

    public AbstractTask(Activity activity, IBugService bugService, int title, int content, boolean showNotifications) {
        super();
        this.activity = activity;
        this.icon = R.mipmap.ic_launcher_round;
        this.title = this.activity.getString(title);
        this.content = this.activity.getString(content);
        this.bugService = bugService;
        this.showNotifications = showNotifications;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (this.showNotifications) {
            this.id = MessageHelper.startProgressNotification(this.activity, this.title, this.content, this.icon);
        }
        this.before();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (this.showNotifications) {
            MessageHelper.stopProgressNotification(this.activity, this.id);
        }
        this.after();
    }

    protected void printMessage() {
        if (this.bugService.getCurrentState() != 200 && this.bugService.getCurrentState() != 201) {
            if (!this.bugService.getCurrentMessage().isEmpty()) {
                this.activity.runOnUiThread(() -> MessageHelper.printMessage(this.bugService.getCurrentMessage(), this.activity));
            }
        }
    }

    protected abstract void before();

    protected abstract void after();
}
