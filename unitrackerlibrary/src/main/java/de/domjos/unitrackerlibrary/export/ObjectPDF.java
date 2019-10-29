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

    private static void saveElementToPDF(Object object, Document pdfDocument, Map<String, Font> fonts) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        if(object instanceof Project) {
            Project project = (Project) object;
            pdfDocument.add(ObjectPDF.addTitle(project.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
            pdfDocument.add(ObjectPDF.addEmptyLine(3));

            StringBuilder builder = new StringBuilder();
            builder.append("ID").append("\t\t: ").append(project.getId()).append("\n");
            builder.append("Alias").append("\t\t: ").append(project.getAlias()).append("\n");
            builder.append("Status").append("\t\t: ").append(project.getStatus()).append("\n");
            builder.append("Enabled").append("\t\t: ").append(project.isEnabled()).append("\n");
            builder.append("Website").append("\t\t: ").append(project.getWebsite()).append("\n");

            Date dt = new Date();
            dt.setTime(project.getCreatedAt());
            builder.append("Creation").append("\t\t: ").append(sdf.format(dt)).append("\n");
            dt.setTime(project.getUpdatedAt());
            builder.append("Update").append("\t\t: ").append(sdf.format(dt)).append("\n");
            pdfDocument.add(ObjectPDF.addParagraph("Data", builder.toString(), fonts.get(H2), fonts.get(BODY)));
            pdfDocument.add(ObjectPDF.addEmptyLine(2));

            pdfDocument.add(ObjectPDF.addParagraph("Description", project.getDescription(), fonts.get(H3), fonts.get(BODY)));
            pdfDocument.add(ObjectPDF.addEmptyLine(2));

            if(!project.getVersions().isEmpty()) {
                List<String> header = Arrays.asList("Title", "Description", "Released", "Obsolete");
                List<List<Map.Entry<String, BaseColor>>> cells = new LinkedList<>();
                for(Object obj : project.getVersions()) {
                    Version version = (Version) obj;
                    List<Map.Entry<String, BaseColor>> mp = new LinkedList<>();
                    mp.add(new AbstractMap.SimpleEntry<>(version.getTitle(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(version.getDescription(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(String.valueOf(version.isReleasedVersion()), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(String.valueOf(version.isDeprecatedVersion()), BaseColor.LIGHT_GRAY));
                    cells.add(mp);
                }
                ObjectPDF.addTable(header, null, cells);
            }
        } else if(object instanceof Issue) {
            Issue issue = (Issue) object;
            pdfDocument.add(ObjectPDF.addTitle(issue.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
            pdfDocument.add(ObjectPDF.addEmptyLine(3));

            StringBuilder builder = new StringBuilder();
            builder.append("ID").append("\t\t: ").append(issue.getId()).append("\n");
            builder.append("Category").append("\t\t: ").append(issue.getCategory()).append("\n");
            builder.append("View").append("\t\t: ").append(issue.getState().getValue().toString()).append("\n");
            builder.append("Status").append("\t\t: ").append(issue.getStatus().getValue().toString()).append("\n");
            builder.append("Priority").append("\t\t: ").append(issue.getPriority().getValue().toString()).append("\n");
            builder.append("Severity").append("\t\t: ").append(issue.getSeverity().getValue().toString()).append("\n");
            builder.append("Tags").append("\t\t: ").append(issue.getTags()).append("\n");
            builder.append("Version").append("\t\t: ").append(issue.getVersion()).append("\n");
            builder.append("Target Version").append("\t\t: ").append(issue.getTargetVersion()).append("\n");
            builder.append("Fixed In Version").append("\t\t: ").append(issue.getFixedInVersion()).append("\n");
            if(issue.getHandler()!=null) {
                builder.append("Handler").append("\t\t: ").append(issue.getHandler().getTitle()).append("\n");
            }
            if(issue.getDueDate()!=null) {
                builder.append("Due Date").append("\t\t: ").append(sdf.format(issue.getDueDate())).append("\n");
            }
            builder.append("Creation").append("\t\t: ").append(sdf.format(issue.getSubmitDate())).append("\n");
            builder.append("Update").append("\t\t: ").append(sdf.format(issue.getLastUpdated())).append("\n");
            pdfDocument.add(ObjectPDF.addParagraph("Data", builder.toString(), fonts.get(H2), fonts.get(BODY)));
            pdfDocument.add(ObjectPDF.addEmptyLine(2));

            pdfDocument.add(ObjectPDF.addParagraph("Description", issue.getDescription(), fonts.get(H3), fonts.get(BODY)));
            pdfDocument.add(ObjectPDF.addEmptyLine(1));
            pdfDocument.add(ObjectPDF.addParagraph("Steps to Reproduce", issue.getStepsToReproduce(), fonts.get(H3), fonts.get(BODY)));
            pdfDocument.add(ObjectPDF.addEmptyLine(1));
            pdfDocument.add(ObjectPDF.addParagraph("Additional Information", issue.getAdditionalInformation(), fonts.get(H3), fonts.get(BODY)));
            pdfDocument.add(ObjectPDF.addEmptyLine(2));

            if(!issue.getNotes().isEmpty()) {
                List<String> headers = Arrays.asList("Title", "Description");
                List<List<Map.Entry<String, BaseColor>>> cells = new LinkedList<>();
                for(Object obj : issue.getNotes()) {
                    List<Map.Entry<String, BaseColor>> mp = new LinkedList<>();
                    Note note = (Note) obj;
                    mp.add(new AbstractMap.SimpleEntry<>(note.getTitle(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(note.getDescription(), BaseColor.LIGHT_GRAY));
                    cells.add(mp);
                }

                pdfDocument.add(ObjectPDF.addTable(headers, new float[]{20f, 80f}, cells));
            }

            if(!issue.getCustomFields().isEmpty()) {
                List<String> headers = Arrays.asList("Title", "Type", "Value");
                List<List<Map.Entry<String, BaseColor>>> cells = new LinkedList<>();
                for(Object obj : issue.getCustomFields().entrySet()) {
                    Map.Entry entry = (Map.Entry) obj;
                    List<Map.Entry<String, BaseColor>> mp = new LinkedList<>();
                    CustomField customField = (CustomField) entry.getKey();

                    mp.add(new AbstractMap.SimpleEntry<>(customField.getTitle(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(customField.getType().name(), BaseColor.LIGHT_GRAY));
                    mp.add(new AbstractMap.SimpleEntry<>(entry.getValue().toString(), BaseColor.LIGHT_GRAY));
                    cells.add(mp);
                }
                pdfDocument.add(ObjectPDF.addTable(headers, new float[]{20f, 20f, 60f}, cells));
            }
        } else if(object instanceof CustomField) {
            CustomField customField = (CustomField) object;
            pdfDocument.add(ObjectPDF.addTitle(customField.getTitle(), fonts.get(H1), Paragraph.ALIGN_CENTER));
            pdfDocument.add(ObjectPDF.addEmptyLine(3));

            String builder = "ID" + "\t\t: " + customField.getId() + "\n" +
                    "Type" + "\t\t: " + customField.getType().name() + "\n" +
                    "Default" + "\t\t: " + customField.getDefaultValue() + "\n" +
                    "Min" + "\t\t: " + customField.getMinLength() + "\n" +
                    "Max" + "\t\t: " + customField.getMaxLength() + "\n" +
                    "Possible Values" + "\t\t: " + customField.getPossibleValues();
            pdfDocument.add(ObjectPDF.addParagraph("Data", builder, fonts.get(H2), fonts.get(BODY)));
        }
    }

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
        PdfPTable table = new PdfPTable(headers.size());
        if(headerWidth!=null) {
            table.setWidths(headerWidth);
        }

        for(String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLDITALIC)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
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
        private int maxPage;
        private byte[] background, icon;

        PDFPageEvent(int maxPage, byte[] bg, byte[] icon) {
            this.maxPage = maxPage;
            this.background = bg;
            this.icon = icon;
        }

        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle rect = writer.getBoxSize("art");
            ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_CENTER, new Phrase(document.getPageNumber() + " / " + this.maxPage), rect.getRight(), rect.getBottom(), 0);


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
