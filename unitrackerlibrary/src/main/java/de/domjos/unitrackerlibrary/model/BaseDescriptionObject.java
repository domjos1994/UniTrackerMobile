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

package de.domjos.unitrackerlibrary.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import de.domjos.unitrackerlibrary.tools.ConvertHelper;

/**
 * Item-Class for the Swipe-Refresh-Delete-List
 * @author Dominic Joas
 */
public class BaseDescriptionObject {
    private long id;
    private String title;
    private String description;
    private Object object;
    private Bitmap cover;
    private boolean selected;
    private boolean state;

    /**
     * Default-Constructor
     */
    public BaseDescriptionObject() {
        super();
        this.id = 0;
        this.title = "";
        this.description = "";
        this.object = null;
        this.cover = null;
        this.selected = false;
        this.state = false;
    }

    /**
     * Gets the ID of the Item
     * @return the ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets the ID of the Item
     * @param id the ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the Title of the Item
     * @return the Title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the Title of the Item
     * @param title the Title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the Description of the Item
     * @return the Description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the Description of the Item
     * @param description the Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the Object of the Item
     * @return the Object
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * Sets the Object of the Item
     * @param object the Object
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Gets the Cover of the Item
     * @return the Cover
     */
    public Bitmap getCover() {
        return this.cover;
    }

    /**
     * Sets the Cover of the Item
     * @param cover the Cover
     */
    public void setCover(byte[] cover) {
        if(cover == null) {
            this.cover = null;
        } else {
            this.cover = ConvertHelper.convertByteArrayToBitmap(cover);
        }
    }

    /**
     * Sets the Cover of the Item
     * @param cover the Cover
     */
    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    /**
     * Shows whether the Item is selected
     * @return the Item is selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Sets the Selection of the Item
     * @param selected the Selection
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Shows whether the Item is positive
     * Notice: The Item changes the Background-Color
     * if selected and attribute "listItemBackgroundStatePositive" is Set
     * @return the Item is positive
     */
    public boolean isState() {
        return this.state;
    }

    /**
     * Sets the State of the Item
     * Notice: The Item changes the Background-Color
     * if selected and attribute "listItemBackgroundStatePositive" is Set
     * @param state the State
     */
    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    @NonNull
    public String toString() {
        return this.title;
    }
}