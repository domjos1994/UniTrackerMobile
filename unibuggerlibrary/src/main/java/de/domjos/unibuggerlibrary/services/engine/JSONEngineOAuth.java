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

package de.domjos.unibuggerlibrary.services.engine;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.domjos.unibuggerlibrary.utils.Converter;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class JSONEngineOAuth {
    private Authentication authentication;
    private final List<String> headers;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client;
    private String currentMessage;
    private int state;

    public JSONEngineOAuth(Authentication authentication) throws Exception {
        this.authentication = authentication;
        this.headers = new LinkedList<>();
        JSONObject object = new JSONObject(authentication.getToken());
        this.headers.add("Authorization: token " + object.getString("access_token"));
        this.client = this.getClient();
    }

    public String getCurrentMessage() {
        return this.currentMessage;
    }

    public int getCurrentState() {
        return this.state;
    }

    protected int executeRequest(String path) throws Exception {
        Call call = this.initAuthentication(path);
        Response response = call.execute();
        this.state = response.code();

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            this.currentMessage = Converter.convertStreamToString(responseBody.byteStream());
        }
        return this.state;
    }

    protected int executeRequest(String path, String body, String type) throws Exception {
        Call call = this.initAuthentication(path, body, type);
        Response response = call.execute();
        this.state = response.code();

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            this.currentMessage = Converter.convertStreamToString(responseBody.byteStream());
        }
        return this.state;
    }

    protected int deleteRequest(String path) throws Exception {
        Call call = this.delete(path);
        Response response = call.execute();
        this.state = response.code();

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            this.currentMessage = Converter.convertStreamToString(responseBody.byteStream());
        }
        return this.state;
    }

    private Call initAuthentication(String path) {
        return this.initAuthentication(path, "", "");
    }

    private Call initAuthentication(String path, String body, String type) {
        RequestBody requestBody = RequestBody.create(JSON, "");
        if (body != null) {
            if (!body.trim().isEmpty()) {
                requestBody = RequestBody.create(JSON, body);
            }
        }

        Request request;
        switch (type.toUpperCase()) {
            case "POST":
                request = this.initRequestBuilder(path).post(requestBody).build();
                break;
            case "PUT":
                request = this.initRequestBuilder(path).put(requestBody).build();
                break;
            case "PATCH":
                request = this.initRequestBuilder(path).patch(requestBody).build();
                break;
            case "DELETE":
                request = this.initRequestBuilder(path).delete().build();
                break;
            default:
                request = this.initRequestBuilder(path).build();
        }
        return this.client.newCall(request);
    }

    private Call delete(String path) {
        return this.initAuthentication(path, "", "DELETE");
    }


    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request.Builder initRequestBuilder(String path) {
        Request.Builder builder = new Request.Builder();
        if (this.headers != null) {
            for (String entry : this.headers) {
                if (entry.contains(": ")) {
                    String[] fields = entry.split(": ");
                    if (fields.length == 2) {
                        builder = builder.addHeader(fields[0], fields[1]);
                    }
                }
            }
        }
        return builder.addHeader("Accept", "application/json").url(this.authentication.getServer() + path);
    }
}
