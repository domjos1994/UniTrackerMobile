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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SoapEngine {
    private Authentication authentication;
    private static final MediaType SOAP = MediaType.parse("text/xml");
    private final OkHttpClient client;
    protected final String soapPath;
    protected Document document;

    public SoapEngine(Authentication authentication, String path) {
        this.authentication = authentication;
        this.client = this.getClient();
        this.soapPath = this.authentication.getServer() + path;
    }

    protected Element startDocument() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        this.document = documentBuilder.newDocument();
        this.document.setXmlVersion("1.0");
        Element element = this.document.createElementNS("http://www.w3.org/2003/05/soap-envelope/", "soap:Envelope");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "soap:encodingStyle", "http://www.w3.org/2003/05/soap-encoding");
        element.appendChild(this.document.createElement("soap:header"));
        Element body = this.document.createElement("soap:body");
        element.appendChild(body);
        return body;
    }

    protected Call closeDocumentAndSend(Element element, String action) throws Exception {
        this.document.appendChild(element);
        return this.client.newCall(this.sendRequest(document, action));
    }

    private Request sendRequest(Document document, String action) throws Exception {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), result);

        String content = writer.toString();
        RequestBody requestBody = RequestBody.create(SOAP, "");
        if (!content.trim().isEmpty()) {
            requestBody = RequestBody.create(SOAP, content);
        }

        return this.initRequestBuilder(requestBody, action);
    }

    private Request initRequestBuilder(RequestBody body, String action) {
        Request.Builder builder = new Request.Builder();
        return builder.addHeader("content-type", "text/xml").url(this.soapPath + "/" + action).post(body).build();
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();
    }
}
