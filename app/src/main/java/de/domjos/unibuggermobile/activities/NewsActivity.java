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

package de.domjos.unibuggermobile.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import java.util.Date;
import java.util.List;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.customwidgets.widgets.swiperefreshdeletelist.SwipeRefreshDeleteList;
import de.domjos.unibuggermobile.R;
import de.domjos.unibuggermobile.helper.Helper;
import de.domjos.unitrackerlibrary.interfaces.IBugService;
import de.domjos.unitrackerlibrary.model.issues.History;

/** @noinspection rawtypes*/
public final class NewsActivity extends AbstractActivity {
    private SwipeRefreshDeleteList lvNews;
    private IBugService bugService;

    public NewsActivity() {
        super(R.layout.news_activity);
    }

    @Override
    protected void initActions() {

    }

    @Override
    protected void reload() {
        String format = String.format("%s %s", MainActivity.GLOBALS.getSettings(this).getDateFormat(), MainActivity.GLOBALS.getSettings(this).getTimeFormat());

        this.lvNews.getAdapter().clear();
        new Thread(()->{
            try {
                List histories = this.bugService.getNews();
                for(Object obj : histories) {
                    History<?> history = (History<?>) obj;

                    BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                    if(history.getTitle().trim().isEmpty()) {
                        String title;
                        if(history.getOldValue().trim().isEmpty()) {
                            title = String.format(this.getString(R.string.news_created), history.getNewValue().trim());
                        } else if(history.getNewValue().trim().isEmpty()) {
                            title = String.format(this.getString(R.string.news_deleted), history.getOldValue().trim());
                        } else {
                            title = history.getField() + ": " + history.getOldValue() + " -> " + history.getNewValue();
                        }

                        baseDescriptionObject.setTitle(title);
                    } else {
                        baseDescriptionObject.setTitle(history.getTitle());
                    }
                    if(history.getDescription().trim().isEmpty()) {
                        String date = ConvertHelper.convertDateToString(new Date(history.getTime()), format);
                        String name = "";
                        if(history.getIssue() != null) {
                            baseDescriptionObject.setCover(this.getBitmapFromDrawable(R.drawable.icon_issues));
                            name = history.getIssue().getTitle();
                        } else if(history.getVersion() != null) {
                            baseDescriptionObject.setCover(this.getBitmapFromDrawable(R.drawable.icon_versions));
                            name = history.getVersion().getTitle();
                        } else if(history.getProject() != null) {
                            baseDescriptionObject.setCover(this.getBitmapFromDrawable(R.drawable.icon_projects));
                            name = history.getProject().getTitle();
                        }
                        baseDescriptionObject.setDescription(String.format(this.getString(R.string.news_description), name, date));
                    } else {
                        baseDescriptionObject.setDescription(history.getDescription());
                        baseDescriptionObject.setCover(this.getBitmapFromDrawable(R.drawable.icon_issues));
                    }
                    this.runOnUiThread(()->this.lvNews.getAdapter().add(baseDescriptionObject));
                }
            } catch (Exception ex) {
                this.runOnUiThread(()->MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this));
            }
        }).start();
    }

    private Bitmap getBitmapFromDrawable(int resId) {
        try {
            Drawable drawable = ResourcesCompat.getDrawable(this.getResources(), resId, this.getTheme());
            if(drawable != null) {
                Canvas canvas = new Canvas();
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(canvas);
                return bitmap;
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    protected void initControls() {
        this.lvNews = this.findViewById(R.id.lvNews);
        this.lvNews.setOnReloadListener(this::reload);

        this.bugService = Helper.getCurrentBugService(this.getApplicationContext());
    }
}