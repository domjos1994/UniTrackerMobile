/*
 * Copyright (C)  2019-2020 Domjos
 *  This file is part of UniTrackerMobile <https://unitrackermobile.de/>.
 *
 *  UniTrackerMobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UniTrackerMobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackerlibrary.services.tracker.GithubSpecific;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;
import de.domjos.unitrackerlibrary.services.tracker.Github;

public class SearchAll extends JSONEngine {
    public static final String SEARCH = "search";

    public SearchAll(Authentication authentication) {
        super(authentication);
    }

    public List<Project<Long>> getProjects(String user) throws Exception {
        List<Project<Long>> results = new LinkedList<>();

        int status = super.executeRequest("/users/" + user + "/repos");
        if(status == 200) {
            results.addAll(this.getDataFromArray(new JSONArray(super.getCurrentMessage())));
        }

        status = super.executeRequest("/orgs/" + user + "/repos");
        if(status == 200) {
            results.addAll(this.getDataFromArray(new JSONArray(super.getCurrentMessage())));
        }
        return results;
    }

    private List<Project<Long>> getDataFromArray(JSONArray array) throws Exception {
        List<Project<Long>> projects = new LinkedList<>();
        for(int i = 0; i<=array.length()-1; i++) {
            Project<Long> project = new Project<>();
            JSONObject jsonObject = array.getJSONObject(i);
            project.setId(jsonObject.getLong("id"));
            project.setTitle(jsonObject.getString("full_name"));
            project.setAlias(jsonObject.getString("name"));
            project.setPrivateProject(jsonObject.getBoolean("private"));
            project.setDescription(jsonObject.getString("description"));
            project.setWebsite(jsonObject.getString("homepage"));
            project.setEnabled(!jsonObject.getBoolean("disabled"));

            if (jsonObject.has("created_at")) {
                Date dt = ConvertHelper.convertStringToDate(jsonObject.getString("created_at"), Github.DATE_TIME_FORMAT);
                if (dt != null) {
                    project.setCreatedAt(dt.getTime());
                }
            }
            if (jsonObject.has("updated_at")) {
                Date dt = ConvertHelper.convertStringToDate(jsonObject.getString("updated_at"), Github.DATE_TIME_FORMAT);
                if (dt != null) {
                    project.setUpdatedAt(dt.getTime());
                }
            }
            projects.add(project);
        }
        return projects;
    }
}
