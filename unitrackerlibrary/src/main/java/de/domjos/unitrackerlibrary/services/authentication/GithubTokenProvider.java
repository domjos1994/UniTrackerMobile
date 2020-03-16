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

package de.domjos.unitrackerlibrary.services.authentication;

import org.json.JSONArray;
import org.json.JSONObject;

import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.services.engine.JSONEngine;

public final class GithubTokenProvider implements AccessTokenProvider {
    private Authentication authentication;
    private String token;

    public GithubTokenProvider(Authentication authentication) {
        this.authentication = authentication;
        this.token = this.authentication.getAPIKey();
    }

    @Override
    public String token() {
        if(this.token.equals("")) {
            this.token = this.refreshToken();
        }
        return this.token;
    }

    @Override
    public String refreshToken() {
        try {
            JSONEngine jsonEngine = new JSONEngine(this.authentication);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("client_secret", "b5d664ec43c104fe21b8dbf7699fbb6d82118ad8");
            jsonObject.put("client_id", "fb4ad3bae97a0a91e6a4");
            jsonObject.put("note", "UniTrackerMobile Authentication");
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("user");
            jsonArray.put("public_repo");
            jsonArray.put("delete_repo");
            jsonObject.put("scopes", jsonArray);

            jsonEngine.executeRequest(this.authentication.getServer() + "/authorizations", jsonObject.toString(), "POST");
            if(jsonEngine.getCurrentState() == 200 || jsonEngine.getCurrentState() == 201) {
                String token = new JSONObject(jsonEngine.getCurrentMessage()).getString("token");
                return token;
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public String authorization() {
        return this.authentication.getUserName() + ":" + this.token;
    }
}
