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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import java.util.Objects;

public abstract class AccessTokenProvider {
    private final static int RESULT = 1234;
    private final AuthorizationServiceConfiguration configuration;
    private final Activity activity;
    private String token;

    public AccessTokenProvider(String authUrl, String tokenUrl, Activity activity) {
        this.configuration = new AuthorizationServiceConfiguration(Uri.parse(authUrl), Uri.parse(tokenUrl));
        this.activity = activity;
        this.token = "";
    }

    public void getAuthCode(String clientId, String responseUrl, String scopes) {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(this.configuration, clientId, ResponseTypeValues.CODE, Uri.parse(responseUrl));

        AuthorizationRequest request = builder.setScope(scopes).build();
        AuthorizationService service = new AuthorizationService(this.activity);
        Intent intent = service.getAuthorizationRequestIntent(request);
        this.activity.startActivityForResult(intent, AccessTokenProvider.RESULT);
    }

    public String returnResult(int requestCode, Intent data) {
        try {
            if (requestCode == AccessTokenProvider.RESULT) {
                AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                this.token = Objects.requireNonNull(resp).accessToken;
            }
        } catch (Exception ignored) {}
        return this.token;
    }

    public String token() {
        return this.token;
    }
}
