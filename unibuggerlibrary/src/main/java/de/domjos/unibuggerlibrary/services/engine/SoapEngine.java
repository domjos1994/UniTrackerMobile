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

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SoapEngine {
    private Authentication authentication;
    private static final MediaType SOAP = MediaType.parse("text/xml");
    private final OkHttpClient client;
    private final String soapPath;

    public SoapEngine(Authentication authentication, String path) {
        this.authentication = authentication;
        this.client = this.getClient();
        this.soapPath = this.authentication.getServer() + path;
    }


    private Request initRequestBuilder(RequestBody body) {
        Request.Builder builder = new Request.Builder();
        return builder.addHeader("content-type", "text/xml").url(this.soapPath).post(body).build();
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();
    }
}
