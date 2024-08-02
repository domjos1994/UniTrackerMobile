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

package de.domjos.unitrackerlibrary.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.objects.DescriptionObject;
import de.domjos.unitrackerlibrary.services.engine.Authentication;

public class Template extends DescriptionObject<Long> {
    private final Gson gson;
    private Authentication authentication;
    private boolean useAsDefault;

    public Template() {
        super();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Map.Entry.class, new EntryInstanceCreator());
        this.gson = gsonBuilder.create();
    }

    public void setContent(Issue<?> issue) {
        this.setDescription(this.gson.toJson(issue));
    }

    public Issue<?> getContent() {
        String content = this.getDescription();
        return this.gson.fromJson(content, Issue.class);
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public boolean isUseAsDefault() {
        return this.useAsDefault;
    }

    public void setUseAsDefault(boolean useAsDefault) {
        this.useAsDefault = useAsDefault;
    }
}

class EntryTypeAdapter extends TypeAdapter<Map.Entry<Integer, String>> {

    @Override
    public void write(JsonWriter out, Map.Entry<Integer, String> value) throws IOException {
        out.value(value.getKey() + "|" + value.getValue());
    }

    @Override
    public Map.Entry<Integer, String> read(JsonReader in) throws IOException {
        String[] data = in.nextString().split("\\|");
        return new AbstractMap.SimpleEntry<>(Integer.parseInt(data[0]), data[1]);
    }
}

class EntryInstanceCreator implements InstanceCreator<Map.Entry<Integer, String>> {

    @Override
    public Map.Entry<Integer, String> createInstance(Type type) {
        return new AbstractMap.SimpleEntry<>(0, "");
    }
}