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

package de.domjos.unibuggerlibrary.services.tracker.MantisBTSpecific;

import org.ksoap2.serialization.SoapObject;

import java.util.Vector;

import de.domjos.unibuggerlibrary.services.engine.Authentication;
import de.domjos.unibuggerlibrary.services.engine.SoapEngine;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public class ChangeLog extends SoapEngine {
    public ChangeLog(Authentication authentication) {
        super(authentication, "/api/soap/mantisconnect.php");
    }

    public String getChangeLog(String version) {
        try {
            StringBuilder content = new StringBuilder();
            SoapObject request = new SoapObject(super.soapPath, "mc_filter_search_issue_headers");
            SoapObject object = new SoapObject(NAMESPACE, "FilterSearchData");
            Vector<String> versions = new Vector<>();
            versions.add(version);
            object.addProperty("fixed_in_version", versions);
            Vector<Integer> vector = new Vector<>();
            vector.add(24);
            object.addProperty("project_id", vector);
            request.addProperty("filter", object);
            Object response = this.executeAction(request, "mc_filter_search_issue_headers", true);
            if (response instanceof Vector) {
                Vector responseVector = (Vector) response;
                for (int i = 0; i <= responseVector.size() - 1; i++) {
                    Object item = responseVector.get(i);
                    content.append(" - ");
                    content.append(((SoapObject) item).getPropertyAsString("summary"));
                    content.append("\n");
                }
            }
            return content.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
