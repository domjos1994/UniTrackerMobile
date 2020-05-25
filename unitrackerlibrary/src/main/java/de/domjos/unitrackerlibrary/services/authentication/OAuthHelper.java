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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class OAuthHelper {
    private static final int RESULT = 12345;

    public static void startServiceConfig(String auth, String token, String clientId, Context context, Authentication authentication) {
        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(Uri.parse(auth), Uri.parse(token));
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(serviceConfig, clientId, ResponseTypeValues.CODE, Uri.parse("app://de.domjos.unitrackermobile"));
        AuthorizationRequest authorizationRequest = builder.build();
        AuthorizationService authorizationService = new AuthorizationService(context);
        CustomTabsIntent.Builder intentBuilder = authorizationService.createCustomTabsIntentBuilder(authorizationRequest.toUri());
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        Intent intent = authorizationService.getAuthorizationRequestIntent(authorizationRequest, customTabsIntent);
        intent.putExtra("name", authentication.getTitle());
        intent.putExtra("id", authentication.getId());
        ((Activity) context).startActivityForResult(intent, OAuthHelper.RESULT);
    }

    public static String getResult(Intent data) {
        if(data != null) {
            if(data.getData() != null) {
                return data.getData().getQueryParameter("code");
            }
        }
        return "";
    }
}
