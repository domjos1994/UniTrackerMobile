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

package de.domjos.unibuggerlibrary.model.issues;

import de.domjos.unibuggerlibrary.model.objects.DescriptionObject;

public class CustomField<T> extends DescriptionObject<T> {
    private int intType;
    private String possibleValues;
    private String defaultValue;
    private int minLength;
    private int maxLength;

    public CustomField() {
        super();

        this.intType = 0;
    }

    public void setType(Type type) {
        switch (type) {
            case TEXT:
                this.intType = 0;
                break;
            case NUMBER:
                this.intType = 1;
                break;
            case FLOATING_NUMBER:
                this.intType = 2;
                break;
            case ENUMERATION:
                this.intType = 3;
                break;
            case EMAIL:
                this.intType = 4;
                break;
            case CHECKBOX:
                this.intType = 5;
                break;
            case LIST:
                this.intType = 6;
                break;
            case MULTI_SELECT_LIST:
                this.intType = 7;
                break;
            case DATE:
                this.intType = 8;
                break;
            case CHOICE_BOX:
                this.intType = 9;
                break;
            case TEXT_AREA:
                this.intType = 10;
                break;
        }
    }

    public void setType(int type) {
        this.intType = type;
    }

    public Type getType() {
        return Type.values()[this.intType];
    }

    public int getIntType() {
        return this.intType;
    }

    public String getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public enum Type {
        TEXT,
        NUMBER,
        FLOATING_NUMBER,
        ENUMERATION,
        EMAIL,
        CHECKBOX,
        LIST,
        MULTI_SELECT_LIST,
        DATE,
        CHOICE_BOX,
        TEXT_AREA
    }
}
