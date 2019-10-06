/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
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

package de.domjos.unitrackerlibrary.export;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ObjectCSV {

    static void saveObjectToCSV(List lst, String path) throws Exception {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(path));
        StringBuilder content = new StringBuilder();
        for(Object obj : lst) {
            content.append(ObjectCSV.convertObjectToString(obj, obj.getClass(), content));
            content.append("\n");
        }
        outputStreamWriter.write(content.toString());
        outputStreamWriter.close();
    }

    private static StringBuilder convertObjectToString(Object object, Class cls, StringBuilder content) throws Exception {
        if(!cls.getName().contains("BaseObject")) {
            if (cls.getSuperclass() != null) {
                content.append(ObjectCSV.convertObjectToString(object, cls.getSuperclass(), content));
            }
        }

        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(object) != null) {
                if (!field.getName().contains("shadow$")) {
                    Object obj = field.get(object);
                    if (field.getType().isPrimitive() || field.getType() == String.class || field.getType() == Long.class || field.getType() == Integer.class || field.getType() == Object.class) {
                        if(obj != null) {
                            content.append(obj.toString());
                            content.append(";");
                        }
                    } else if (field.getType() == Date.class) {
                        if (obj != null) {
                            String date =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN).format((Date) obj);
                            content.append(date);
                            content.append(";");
                        } else {
                            content.append("");
                            content.append(";");
                        }
                    } else if (field.getType() == List.class) {
                        if(obj!=null) {
                            for (Object subObject : (List) obj) {
                                content.append(ObjectCSV.convertObjectToString(subObject, subObject.getClass(), content));
                            }
                        }
                    } else if(field.getType() == java.util.Map.class) {
                        if(obj!=null) {
                            for(Object mp : ((Map)  obj).entrySet()) {
                                Map.Entry entry = (Map.Entry)mp;
                                content.append(entry.getKey().toString());
                                content.append("=");
                                content.append(entry.getValue().toString());
                                content.append(",");
                            }
                            content.append(";");
                        }
                    } else  {
                        if(obj != null) {
                            content.append(ObjectCSV.convertObjectToString(field.get(object), obj.getClass(), content));
                        }
                    }
                }
            }
        }
        return content;
    }
}
