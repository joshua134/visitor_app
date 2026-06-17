package com.visitor.service;

//Java Core Imports
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

//Apache POI Imports (Excel)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//iText PDF Imports (PDF)
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.visitor.dto.response.VisitorResponse;

//Lombok / Logging Imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final VisitorService visitorService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public void exportToExcel(HttpServletResponse response, List<VisitorResponse> visitors) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Visitors Report");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Full Name", "Email", "Phone", "National ID", "Gender", "Age", "Registration Date"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        
        // Create data rows
        int rowNum = 1;
        for (VisitorResponse visitor : visitors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(visitor.getId());
            row.createCell(1).setCellValue(visitor.getFullName());
            row.createCell(2).setCellValue(visitor.getEmail());
            row.createCell(3).setCellValue(visitor.getPhoneNumber());
            row.createCell(4).setCellValue(visitor.getNationalId());
            row.createCell(5).setCellValue(visitor.getGender());
            row.createCell(6).setCellValue(visitor.getAge() != null ? visitor.getAge() : 0);
            row.createCell(7).setCellValue(visitor.getRegistrationDate());
        }
        
        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to response
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    public void exportToPDF(HttpServletResponse response, List<VisitorResponse> visitors) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        
        // Add title
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Visitors Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        // Add generation date
        com.itextpdf.text.Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(formatter), dateFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);
        
        document.add(new Paragraph(" "));
        
        // Create table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Set column widths
        float[] columnWidths = {5f, 15f, 20f, 15f, 15f, 10f, 8f, 20f};
        table.setWidths(columnWidths);
        
        // Add header cells
        String[] headers = {"ID", "Full Name", "Email", "Phone", "National ID", "Gender", "Age", "Registration Date"};
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(headerCell);
        }
        
        // Add data cells
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (VisitorResponse visitor : visitors) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(visitor.getId()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(visitor.getFullName(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(visitor.getEmail(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(visitor.getPhoneNumber(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(visitor.getNationalId(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(visitor.getGender(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(visitor.getAge()), cellFont)));
            table.addCell(new PdfPCell(new Phrase(visitor.getRegistrationDate(), cellFont)));
        }
        
        document.add(table);
        document.close();
    }
}