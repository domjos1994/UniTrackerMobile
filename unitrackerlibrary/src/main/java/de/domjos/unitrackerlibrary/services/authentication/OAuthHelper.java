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
import net.openid.appauth.TokenRequest;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import de.domjos.unitrackerlibrary.custom.AsyncTaskExecutorService;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class OAuthHelper {
    private static final int RESULT = 12345;

    public static void startServiceConfig(Context context, Authentication authentication) {
        String auth = authentication.getHints().get(Authentication.ENDPOINT_AUTH);
        String token = authentication.getHints().get(Authentication.ENDPOINT_TOKEN);
        String clientId = Objects.requireNonNull(authentication.getHints().get(Authentication.PUBLIC_KEY));

        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(Uri.parse(auth), Uri.parse(token));
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(serviceConfig, clientId, ResponseTypeValues.CODE, Uri.parse("app://de.domjos.unitrackermobile"));
        AuthorizationRequest authorizationRequest = builder.setLoginHint(authentication.getUserName()).setScopes("repo", "user").build();
        AuthorizationService authorizationService = new AuthorizationService(context);
        CustomTabsIntent.Builder intentBuilder = authorizationService.createCustomTabsIntentBuilder(authorizationRequest.toUri());
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        Intent intent = authorizationService.getAuthorizationRequestIntent(authorizationRequest, customTabsIntent);
        ((Activity) context).startActivityForResult(intent, OAuthHelper.RESULT);
    }

    public static void getAccessToken(Intent data, Activity activity, Authentication authentication, Task.Saver saver) {
        Uri uri = data.getData();

        if(uri != null) {
            String auth = authentication.getHints().get(Authentication.ENDPOINT_AUTH);
            String token = authentication.getHints().get(Authentication.ENDPOINT_TOKEN);
            String redirect = "app://de.domjos.unitrackermobile";
            String clientSecret = Objects.requireNonNull(authentication.getHints().get(Authentication.SECRET_KEY));
            String clientId = Objects.requireNonNull(authentication.getHints().get(Authentication.PUBLIC_KEY));
            String state = uri.getQueryParameter("state");
            String code = uri.getQueryParameter("code");

            AuthorizationServiceConfiguration configuration = new AuthorizationServiceConfiguration(Uri.parse(auth), Uri.parse(token));
            TokenRequest.Builder builder = new TokenRequest.Builder(configuration, clientId);
            Map<String, String> additional = new LinkedHashMap<>();
            additional.put("client_secret", clientSecret);
            additional.put("state", state);
            builder.setRedirectUri(Uri.parse(redirect)).setAuthorizationCode(code).setAdditionalParameters(additional);

            try {
                Task task = new Task(activity, builder, authentication, saver);
                task.execute().get();
            } catch (Exception ignored) {}
        }
    }

    private static class Task extends AsyncTaskExecutorService<Void, Void, String> {
        private final WeakReference<Activity> context;
        private final TokenRequest.Builder builder;
        private final Authentication authentication;
        private final Saver saver;

        public Task(Activity activity, TokenRequest.Builder builder, Authentication authentication, Saver saver) {
            super(activity);
            this.context = new WeakReference<>(activity);
            this.builder = builder;
            this.authentication = authentication;
            this.saver = saver;
        }

        @Override
        protected String doInBackground(Void... voids) {
            AuthorizationService authorizationService = new AuthorizationService(this.context.get());
            authorizationService.performTokenRequest(this.builder.build(), (response, ex) -> {
                if (response != null) {
                    this.authentication.getHints().put("token", response.accessToken);
                    this.saver.save(this.authentication);
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String string) {

        }

        @FunctionalInterface
        public interface Saver {
            void save(Authentication authentication);
        }
    }
}
