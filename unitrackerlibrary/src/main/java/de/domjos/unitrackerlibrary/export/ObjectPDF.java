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

package de.domjos.unitrackerlibrary.export;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.domjos.unitrackerlibrary.model.issues.CustomField;
import de.domjos.unitrackerlibrary.model.issues.Issue;
import de.domjos.unitrackerlibrary.model.issues.Note;
import de.domjos.unitrackerlibrary.model.projects.Project;
import de.domjos.unitrackerlibrary.model.projects.Version;

/** @noinspection rawtypes*/
final class ObjectPDF {
    private static final String H1 = "H1", H2 = "H2", H3 = "H3", BODY = "Body";


    static void saveObjectToPDF(List lst, String path, byte[] array, byte[] icon) throws Exception {
        Map<String, Font> fonts  = new LinkedHashMap<>();
        fonts.put(H1, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLDITALIC, BaseColor.BLACK));
        fonts.put(H2, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK));
        fonts.put(H3, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK));
        fonts.put(BODY, new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK));

        Document pdfDocument = new Document();
        PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(path));
        writer.setBoxSize("art", new Rectangle(55, 25, 550, 788));
        writer.setPageEvent(new PDFPageEvent(lst.size(), array, icon));
        pdfDocument.open();

        for(Object object : lst) {
            pdfDocument.newPage();
            ObjectPDF.saveElementToPDF(object, pdfDocument, fonts);
        }
        pdfDocument.close();
    }

    static void createChangeLog(List<Issue> issues, String path, byte[] array, byte[] icon, Version<?> version) throws Exception {
        Map<String, Font> fonts  = new LinkedHashMap<>();
        fonts.put(H1, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLDITALIC, BaseColor.BLACK));
        fonts.put(H2, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK));
        fonts.put(H3, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK));
        fonts.put(BODY, new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK));

        Document pdfDocument = new Document();
        PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(path + File.separatorChar + "changelog_" + version.getId() + ".pdf"));
        writer.setBoxSize("art", new Rectangle(55, 25, 550, 788));
        writer.setPageEvent(new PDFPageEvent(0, array, icon));
        pdfDocument.open();
        ObjectPDF.saveChangeLogToPDF(issues, pdfDocument, fonts, version);
        pdfDocument.close();
    }


    private static void saveChangeLogToPDF(List<Issue> issues, Document pdfDocument, Map<String, Font> fonts, Version<?> version) throws Exception {
        pdfDocument.add(ObjectPDF.addTitle("Changelog of " + version.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
        pdfDocument.add(ObjectPDF.addEmptyLine(3));

        List<List<Map.Entry<String, BaseColor>>> bugTable = new LinkedList<>();
        for(Issue<?> issue : issues) {
            if(issue.getFixedInVersion().trim().equals(version.getTitle().trim())) {
                ObjectPDF.addRowToTable(String.valueOf(issue.getId()), issue.getTitle(), false, bugTable);
            }
        }
        pdfDocument.add(ObjectPDF.addTable(null, null, bugTable));
    }

    private static void saveElementToPDF(Object object, Document pdfDocument, Map<String, Font> fonts) throws Exception {
        if(object instanceof Project<?> project) {
            pdfDocument.add(ObjectPDF.addTitle(project.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
            pdfDocument.add(ObjectPDF.addEmptyLine(3));

            List<List<Map.Entry<String, BaseColor>>> bugTable = new LinkedList<>();
            ObjectPDF.addRowToTable("ID", project.getId(), false, bugTable);
            ObjectPDF.addRowToTable("Alias", project.getAlias(), false, bugTable);
            ObjectPDF.addRowToTable("Status", project.getStatus(), false, bugTable);
            ObjectPDF.addRowToTable("Enabled", project.isEnabled(), false, bugTable);
            ObjectPDF.addRowToTable("Website", project.getWebsite(), false, bugTable);
            ObjectPDF.addRowToTable("Creation", project.getCreatedAt(), true, bugTable);
            ObjectPDF.addRowToTable("Update", project.getUpdatedAt(), true, bugTable);
            ObjectPDF.addRowToTable("Description", project.getDescription(), true, bugTable);
            pdfDocument.add(ObjectPDF.addTable(null, null, bugTable));
            pdfDocument.add(ObjectPDF.addEmptyLine(2));

            if(!project.getVersions().isEmpty()) {
                pdfDocument.add(ObjectPDF.addTitle("Versions", fonts.get(H2), Paragraph.ALIGN_LEFT));
                pdfDocument.add(ObjectPDF.addEmptyLine(1));
                List<String> header = Arrays.asList("Title", "Description", "Released", "Obsolete");
                List<List<Map.Entry<String, BaseColor>>> cells = new LinkedList<>();
                for(Version<?> obj : project.getVersions()) {
                    List<Map.Entry<String, BaseColor>> mp = new LinkedList<>();
                    mp.add(new AbstractMap.SimpleEntry<>(obj.getTitle(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(obj.getDescription(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(String.valueOf(obj.isReleasedVersion()), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(String.valueOf(obj.isDeprecatedVersion()), BaseColor.LIGHT_GRAY));
                    cells.add(mp);
                }
                pdfDocument.add(ObjectPDF.addTable(header, null, cells));
            }
        } else if(object instanceof Issue<?> issue) {
            pdfDocument.add(ObjectPDF.addTitle(issue.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
            pdfDocument.add(ObjectPDF.addEmptyLine(3));

            List<List<Map.Entry<String, BaseColor>>> bugTable = new LinkedList<>();
            ObjectPDF.addRowToTable("ID", issue.getId(), false, bugTable);
            ObjectPDF.addRowToTable("Category", issue.getCategory(), false, bugTable);
            ObjectPDF.addRowToTable("View", issue.getState().getValue(), false, bugTable);
            ObjectPDF.addRowToTable("Status", issue.getStatus().getValue(), false, bugTable);
            ObjectPDF.addRowToTable("Priority", issue.getPriority().getValue(), false, bugTable);
            ObjectPDF.addRowToTable("Severity", issue.getSeverity().getValue(), false, bugTable);
            ObjectPDF.addRowToTable("Tags", issue.getTags(), false, bugTable);
            ObjectPDF.addRowToTable("Version", issue.getVersion(), false, bugTable);
            ObjectPDF.addRowToTable("Target Version", issue.getTargetVersion(), false, bugTable);
            ObjectPDF.addRowToTable("Fixed in Version", issue.getFixedInVersion(), false, bugTable);
            if(issue.getHandler()!=null) {
                ObjectPDF.addRowToTable("Handler", issue.getHandler().getTitle(), false, bugTable);
            }
            ObjectPDF.addRowToTable("Due Date", issue.getDueDate(), true, bugTable);
            ObjectPDF.addRowToTable("Creation", issue.getSubmitDate(), true, bugTable);
            ObjectPDF.addRowToTable("Update", issue.getLastUpdated(), true, bugTable);
            ObjectPDF.addRowToTable("Description", issue.getDescription(), false, bugTable);
            ObjectPDF.addRowToTable("Steps to Reproduce", issue.getStepsToReproduce(), false, bugTable);
            ObjectPDF.addRowToTable("Additional Information", issue.getAdditionalInformation(), false, bugTable);
            pdfDocument.add(ObjectPDF.addTable(null, null, bugTable));
            pdfDocument.add(ObjectPDF.addEmptyLine(2));

            if(!issue.getNotes().isEmpty()) {
                pdfDocument.add(ObjectPDF.addTitle("Notes", fonts.get(H2), Paragraph.ALIGN_LEFT));
                pdfDocument.add(ObjectPDF.addEmptyLine(1));

                List<String> headers = Arrays.asList("Title", "Description");
                List<List<Map.Entry<String, BaseColor>>> cells = new LinkedList<>();
                for(Note<?> obj : issue.getNotes()) {
                    List<Map.Entry<String, BaseColor>> mp = new LinkedList<>();
                    mp.add(new AbstractMap.SimpleEntry<>(obj.getTitle(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(obj.getDescription(), BaseColor.LIGHT_GRAY));
                    cells.add(mp);
                }

                pdfDocument.add(ObjectPDF.addTable(headers, new float[]{20f, 80f}, cells));
            }

            if(!issue.getCustomFields().isEmpty()) {
                pdfDocument.add(ObjectPDF.addTitle("Customfields", fonts.get(H2), Paragraph.ALIGN_LEFT));
                pdfDocument.add(ObjectPDF.addEmptyLine(1));

                List<String> headers = Arrays.asList("Title", "Type", "Value");
                List<List<Map.Entry<String, BaseColor>>> cells = new LinkedList<>();
                for(Map.Entry<?, ?> obj : issue.getCustomFields().entrySet()) {
                    List<Map.Entry<String, BaseColor>> mp = new LinkedList<>();
                    CustomField<?> customField = (CustomField<?>) obj.getKey();

                    mp.add(new AbstractMap.SimpleEntry<>(customField.getTitle(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(customField.getType().name(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(obj.getValue().toString(), BaseColor.LIGHT_GRAY));
                    cells.add(mp);
                }
                pdfDocument.add(ObjectPDF.addTable(headers, new float[]{20f, 20f, 60f}, cells));
            }
        } else if(object instanceof CustomField<?> customField) {
            pdfDocument.add(ObjectPDF.addTitle(customField.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
            pdfDocument.add(ObjectPDF.addEmptyLine(3));

            List<List<Map.Entry<String, BaseColor>>> bugTable = new LinkedList<>();
            ObjectPDF.addRowToTable("ID", customField.getId(), false, bugTable);
            ObjectPDF.addRowToTable("Type", customField.getType().name(), false, bugTable);
            ObjectPDF.addRowToTable("Default", customField.getDefaultValue(), false, bugTable);
            ObjectPDF.addRowToTable("Min", customField.getMinLength(), false, bugTable);
            ObjectPDF.addRowToTable("Max", customField.getMaxLength(), false, bugTable);
            ObjectPDF.addRowToTable("Possible Values", customField.getPossibleValues(), false, bugTable);
            pdfDocument.add(ObjectPDF.addTable(null, null, bugTable));
        }
    }

    private static void addRowToTable(String label, Object value, boolean isDate, List<List<Map.Entry<String, BaseColor>>> bugTable) {
        if(isDate) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
            if(value instanceof Long) {
                long item = (Long) value;
                if(item != 0) {
                    Date dt = new Date();
                    dt.setTime(item);
                    bugTable.add(Arrays.asList(new AbstractMap.SimpleEntry<>(label, BaseColor.GRAY), new AbstractMap.SimpleEntry<>(sdf.format(dt), BaseColor.LIGHT_GRAY)));
                }
            } else if(value instanceof Date dt) {
                bugTable.add(Arrays.asList(new AbstractMap.SimpleEntry<>(label, BaseColor.GRAY), new AbstractMap.SimpleEntry<>(sdf.format(dt), BaseColor.LIGHT_GRAY)));
            }
        } else {
            if(value instanceof Integer || value instanceof Long || value instanceof Boolean) {
                bugTable.add(Arrays.asList(new AbstractMap.SimpleEntry<>(label, BaseColor.GRAY), new AbstractMap.SimpleEntry<>(String.valueOf(value), BaseColor.LIGHT_GRAY)));
            } else if(value instanceof String item) {
                if(!item.trim().isEmpty()) {
                    bugTable.add(Arrays.asList(new AbstractMap.SimpleEntry<>(label, BaseColor.GRAY), new AbstractMap.SimpleEntry<>(item, BaseColor.LIGHT_GRAY)));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static Paragraph addParagraph(String title, String content, Font titleFont, Font bodyFont) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(ObjectPDF.addTitle(title, titleFont, Paragraph.ALIGN_LEFT));
        paragraph.add(new Paragraph(" "));
        paragraph.add(new Paragraph(content, bodyFont));
        return paragraph;
    }

    private static Paragraph addTitle(String title, Font font, int alignment) {
        Paragraph paragraph = new Paragraph();
        Paragraph pg = new Paragraph(title, font);
        pg.setAlignment(alignment);
        paragraph.add(pg);
        return paragraph;
    }

    private static PdfPTable addTable(List<String> headers, float[] headerWidth, List<List<Map.Entry<String, BaseColor>>> cells) throws Exception {
        PdfPTable table;
        if(headers != null) {
            table = new PdfPTable(headers.size());
            if(headerWidth!=null) {
                table.setWidths(headerWidth);
            }

            for(String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLDITALIC)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }
        } else {
            if(!cells.isEmpty()) {
                table = new PdfPTable(cells.get(0).size());
            } else {
                table = new PdfPTable(1);
            }
        }

        for(List<Map.Entry<String, BaseColor>> row : cells) {
            for(Map.Entry<String, BaseColor> cellItem : row) {
                PdfPCell cell = new PdfPCell(new Phrase(cellItem.getKey(), new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(cellItem.getValue());
                table.addCell(cell);
            }
        }

        return table;
    }

    private static Paragraph addEmptyLine(int number) {
        Paragraph paragraph = new Paragraph();
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
        return paragraph;
    }

    static class PDFPageEvent extends PdfPageEventHelper {
        private final int maxPage;
        private final byte[] background;
        private final byte[] icon;

        PDFPageEvent(int maxPage, byte[] bg, byte[] icon) {
            this.maxPage = maxPage;
            this.background = bg;
            this.icon = icon;
        }

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_CENTER, new Phrase(document.getPageNumber() + " / " + (this.maxPage != 0 ? this.maxPage : "")), rect.getRight(), rect.getBottom(), 0);


            try {
                if(this.background != null) {
                    this.addBackground(this.background, writer, document);
                }
                if(this.icon != null) {
                    this.addIcon(this.icon, writer);
                }
            } catch (Exception ignored) {}
        }

        private void addIcon(byte[] array, PdfWriter writer) throws Exception {
            Image image = Image.getInstance(array);
            image.setAbsolutePosition(10, 10);
            image.scaleAbsolute(32, 32);
            writer.getDirectContentUnder().addImage(image);
        }

        private void addBackground(byte[] array, PdfWriter writer, Document document) throws Exception {
            Rectangle rectangle = document.getPageSize();
            Image image = Image.getInstance(array);
            image.scaleAbsolute(rectangle.getWidth(), rectangle.getHeight());
            image.setAlignment(Image.UNDERLYING);
            image.setAbsolutePosition(0, 0);
            writer.getDirectContentUnder().addImage(image);
        }
    }
}
