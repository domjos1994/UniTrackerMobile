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

package de.domjos.unitrackerlibrary.export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

final class ObjectXML {
    static void saveObjectListToXML(String root, List lst, String path, String xsltPath) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document document = dBuilder.newDocument();
        Element element = document.createElement(root);
        for (Object object : lst) {
            element.appendChild(ObjectXML.convertObjectToXMLElement(object, document));
        }
        document.appendChild(element);

        ObjectXML.transformXMLByXSLT(new File(xsltPath), document, path);
    }

    private static void transformXMLByXSLT(File file, Document document, String path) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Source xslt = null;
        if(file != null) {
            if(file.exists()) {
                xslt = new StreamSource(file);
            }
        }

        Transformer transformer;
        if(xslt!=null) {
            transformer = transformerFactory.newTransformer(xslt);
        } else {
            transformer = transformerFactory.newTransformer();
        }
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
                        element.setAttribute(field.getName(), Objects.requireNonNull(field.get(object)).toString());
                    } else if (field.getType() == Date.class) {
                        if (field.get(object) != null) {
                            element.setAttribute(field.getName(), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMAN).format((Date) Objects.requireNonNull(field.get(object))));
                        } else {
                            element.setAttribute(field.getName(), null);
                        }
                    } else if (field.getType() == List.class) {
                        Element sub = element.getOwnerDocument().createElement(field.getName());

                        for (Object subObject : (List) Objects.requireNonNull(field.get(object))) {
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
