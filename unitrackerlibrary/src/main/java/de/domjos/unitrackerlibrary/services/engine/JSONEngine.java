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

package de.domjos.unitrackerlibrary.services.engine;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.domjos.unitrackerlibrary.tools.ConvertHelper;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class JSONEngine {
    private final Authentication authentication;
    private final List<String> headers;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType OctetStream = MediaType.get("application/octet-stream");
    private final OkHttpClient client;
    private String currentMessage;
    private int state;
    private final Authenticator authenticator;

    public JSONEngine(Authentication authentication) {
        this.authentication = authentication;
        this.headers = new LinkedList<>();
        this.client = this.getClient();
        this.authenticator = null;
    }

    public JSONEngine(Authentication authentication, Authenticator authenticator) {
        this.authentication = authentication;
        this.headers = new LinkedList<>();
        this.client = this.getClient();
        this.authenticator = authenticator;
    }

    public String getCurrentMessage() {
        return this.currentMessage;
    }

    public int getCurrentState() {
        return this.state;
    }

    @SuppressWarnings("SameParameterValue")
    protected void addHeader(String header) {
        this.headers.add(header);
    }

    @SuppressWarnings("SameParameterValue")
    protected void removeHeader(String header) {
        this.headers.remove(header);
    }

    public int executeRequest(String path) throws Exception {
        try {
            Call call = this.initAuthentication(path);
            try (Response response = call.execute()) {
                this.state = response.code();

                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    this.currentMessage = ConvertHelper.convertStreamToString(responseBody.byteStream());
                }
                return this.state;
            }
        } catch (OutOfMemoryError ex) {
            this.currentMessage = ex.getLocalizedMessage();
            return 500;
        }
    }

    public int executeRequest(String path, String body, String type) throws Exception {
        Call call = this.initAuthentication(path, body, type);
        try(Response response = call.execute()) {
            this.state = response.code();

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                this.currentMessage = ConvertHelper.convertStreamToString(responseBody.byteStream());
            }
            return this.state;
        } catch (Exception ex) {
            this.currentMessage = ex.getLocalizedMessage();
            return 500;
        }
    }

    protected int executeRequest(String path, byte[] body, String type) throws Exception {
        Call call = this.initAuthentication(path, body, type);
        try(Response response = call.execute()) {
            this.state = response.code();

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                this.currentMessage = ConvertHelper.convertStreamToString(responseBody.byteStream());
            }
            return this.state;
        } catch (Exception ex) {
            this.currentMessage = ex.getLocalizedMessage();
            return 500;
        }
    }

    protected void deleteRequest(String path) throws Exception {
        Call call = this.delete(path);
        try(Response response = call.execute()) {
            this.state = response.code();

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                this.currentMessage = ConvertHelper.convertStreamToString(responseBody.byteStream());
            }
        } catch(Exception ex) {
            this.currentMessage = ex.getLocalizedMessage();
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected int addMultiPart(String path, String jsonBody, String contentType, byte[] data, String type) {
        return this.addMultiPart(path, jsonBody, contentType, data, "attachment", type);
    }

    protected int addMultiPart(String path, String jsonBody, String contentType, byte[] data, String filename, String type) {
        if(!contentType.isEmpty()) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MediaType.get("multipart/form-data"));
            builder.addFormDataPart("metadata", jsonBody);
            RequestBody requestBody = RequestBody.create(data, MediaType.get(contentType));
            builder.addFormDataPart("file", filename, requestBody);

            Call call = this.initAuthentication(path, builder.build(), type);
            try(Response response = call.execute()) {
                this.state = response.code();

                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    this.currentMessage = ConvertHelper.convertStreamToString(responseBody.byteStream());
                }
            } catch (Exception ex) {
                this.currentMessage = ex.getLocalizedMessage();
                return 500;
            }
        }
        return this.state;
    }

    protected long getLong(JSONObject object, String key) throws Exception {
        long value = 0L;
        if (object.has(key)) {
            if (!object.isNull(key)) {
                value = object.getLong(key);
            }
        }
        return value;
    }

    protected boolean getBoolean(JSONObject object, String key) throws Exception {
        boolean value = false;
        if (object.has(key)) {
            if (!object.isNull(key)) {
                value = object.getBoolean(key);
            }
        }
        return value;
    }

    private Call initAuthentication(String path) {
        return this.initAuthentication(path, "", "");
    }

    private Call initAuthentication(String path, String body, String type) {
        RequestBody requestBody = RequestBody.create("", JSON);
        if (body != null) {
            if (!body.trim().isEmpty()) {
                requestBody = RequestBody.create(body, JSON);
            }
        }

        Request request = switch (type.toUpperCase()) {
            case "POST" -> this.initRequestBuilder(path).post(requestBody).build();
            case "PUT" -> this.initRequestBuilder(path).put(requestBody).build();
            case "PATCH" -> this.initRequestBuilder(path).patch(requestBody).build();
            case "DELETE" -> this.initRequestBuilder(path).delete().build();
            default -> this.initRequestBuilder(path).build();
        };
        return this.client.newCall(request);
    }

    private Call initAuthentication(String path, MultipartBody requestBody, String type) {
        Request request = switch (type.toUpperCase()) {
            case "POST" -> this.initRequestBuilder(path).post(requestBody).build();
            case "PUT" -> this.initRequestBuilder(path).put(requestBody).build();
            case "PATCH" -> this.initRequestBuilder(path).patch(requestBody).build();
            case "DELETE" -> this.initRequestBuilder(path).delete().build();
            default -> this.initRequestBuilder(path).build();
        };
        return this.client.newCall(request);
    }

    private Call initAuthentication(String path, byte[] body, String type) {
        RequestBody requestBody = RequestBody.create("", OctetStream);
        if (body != null) {
            requestBody = RequestBody.create(body, OctetStream);
        }

        Request request = switch (type.toUpperCase()) {
            case "POST" -> this.initRequestBuilder(path).post(requestBody).build();
            case "PUT" -> this.initRequestBuilder(path).put(requestBody).build();
            case "PATCH" -> this.initRequestBuilder(path).patch(requestBody).build();
            case "DELETE" -> this.initRequestBuilder(path).delete().build();
            default -> this.initRequestBuilder(path).build();
        };
        return this.client.newCall(request);
    }

    private Call delete(String path) {
        return this.initAuthentication(path, "", "DELETE");
    }

    private OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(100, 5, TimeUnit.MINUTES));

        if(this.authenticator != null) {
            builder.authenticator(this.authenticator);
        } else {
            if (this.authentication.getAuthentication() != Authentication.Auth.OAUTH) {
                builder.authenticator((route, response) -> {
                    String credential;
                    if (this.authentication.getAPIKey().trim().isEmpty()) {
                        credential = Credentials.basic(authentication.getUserName(), authentication.getPassword().trim());
                    } else {
                        credential = Credentials.basic(authentication.getAPIKey(), UUID.randomUUID().toString());
                    }
                    return response.request().newBuilder().header("Authorization", credential).build();
                });
            } else {
                builder.addInterceptor(new TokenInterceptor(this.authentication)).build();
            }
        }

        return builder.build();
    }

    private Request.Builder initRequestBuilder(String path) {
        Request.Builder builder = new Request.Builder();
        if (this.headers != null) {
            for (String entry : this.headers) {
                if (entry.contains(": ")) {
                    String[] fields = entry.split(": ");
                    if (fields.length == 2) {
                        builder = builder.addHeader(fields[0], fields[1]);
                    } else if (fields.length > 2) {
                        builder = builder.addHeader(fields[0], entry.replace(fields[0] + ":", "").trim());
                    }
                }
            }
        }
        if(path.trim().startsWith("http")) {
            return builder.addHeader("Accept", "application/json").url(path);
        } else {
            return builder.addHeader("Accept", "application/json").url(this.authentication.getServer() + path);
        }
    }
}
