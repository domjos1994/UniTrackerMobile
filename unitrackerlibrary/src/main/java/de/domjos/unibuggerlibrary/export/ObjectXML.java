/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
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
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unibuggerlibrary.export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

class ObjectXML {
    static void saveObjectListToXML(String root, List lst, String path) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document document = dBuilder.newDocument();
        Element element = document.createElement(root);
        for (Object object : lst) {
            element.appendChild(ObjectXML.convertObjectToXMLElement(object, document));
        }
        document.appendChild(element);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(path);
        transformer.transform(source, result);
    }

    private static Element convertObjectToXMLElement(Object object, Document document) throws Exception {
        Element element = document.createElement(object.getClass().getSimpleName());

        ObjectXML.convertObjectToXMLElementSubClass(object.getClass(), object, element, document);

        return element;
    }

    private static void convertObjectToXMLElementSubClass(Class cls, Object object, Element element, Document document) throws Exception {
        if (cls.getSuperclass() != null) {
            convertObjectToXMLElementSubClass(cls.getSuperclass(), object, element, document);
        }

        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(object) != null) {
                if (!field.getName().contains("shadow$")) {
                    if (field.getType().isPrimitive() || field.getType() == String.class) {
                        element.setAttribute(field.getName(), field.get(object).toString());
                    } else if (field.getType() == Date.class) {
                        if (field.get(object) != null) {
                            element.setAttribute(field.getName(), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN).format((Date) field.get(object)));
                        } else {
                            element.setAttribute(field.getName(), null);
                        }
                    } else if (field.getType() == List.class) {
                        Element sub = element.getOwnerDocument().createElement(field.getName());

                        for (Object subObject : (List) field.get(object)) {
                            Element subSub = element.getOwnerDocument().createElement(subObject.getClass().getSimpleName());
                            ObjectXML.convertObjectToXMLElementSubClass(subObject.getClass(), subObject, subSub, document);
                            sub.appendChild(subSub);
                        }
                        element.appendChild(sub);
                    } else if (field.getType() == byte[].class || field.getType() == Byte[].class) {
                        byte[] bytes = (byte[]) field.get(object);
                        element.setAttribute(field.getName(), Arrays.toString(bytes));
                    } else {
                        Element sub = document.createElement(field.getName());
                        ObjectXML.convertObjectToXMLElementSubClass(field.getType(), field.get(object), sub, document);
                        element.appendChild(sub);
                    }
                }
            }
        }
    }
}
