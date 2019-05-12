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

package de.domjos.unibugger.utils;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.util.Properties;

import de.domjos.unibugger.services.engine.Authentication;

public class Helper {

    public static Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    public static Authentication getAuthFromRes(int rawID, String bt) throws Exception {
        Properties properties = de.domjos.unibuggermobile.helper.Helper.readPropertiesFromRaw(rawID, Helper.getContext());
        return new Authentication(properties.getProperty(bt + "_server"), properties.getProperty(bt + "_api"), properties.getProperty(bt + "_user"), properties.getProperty(bt + "_pwd"));
    }
}
