package com.adfonic.presentation.reporting.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.adfonic.presentation.reporting.model.BinaryExpression.Operator;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Color;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.ColumnsDetails;
import com.adfonic.presentation.reporting.model.ExpressionEvaluator;
import com.adfonic.presentation.reporting.model.FieldIterator;
import com.adfonic.presentation.reporting.model.FileReportingService;
import com.adfonic.presentation.reporting.model.FunctionExpression.Function;
import com.adfonic.presentation.reporting.model.HeaderDetails;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.presentation.reporting.model.Style;
import com.adfonic.presentation.reporting.model.Style.FontName;
import com.adfonic.presentation.reporting.model.Total;
 
public class ExcelFileReportingService<T> extends FileReportingService<T>{

    private enum FormatStyle {
        NUMERIC,
        DECIMAL, 
        PERCENTAGE, 
        CURRENCY, 
        DATE
    }
    
    private static final Integer CHARACTER_SIZE   = 255;
    private static final Integer HEADER_ROWS      = 2;
    private static final int ONE_HOUNDRED         = 100;
    private static final int TITLE_MERGED_COLUMNS = 10;
    
    @Override
    public OutputStream createReport(ReportDefinition<T> reportDefinition) throws IOException{
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(reportDefinition.getReportName()));
        
        List<XSSFCellStyle> formatStyles = createFormatStyles(wb, reportDefinition);
        
        composeBody(wb, sheet, reportDefinition, formatStyles);
        
        composeTotals(wb, sheet, reportDefinition, formatStyles);
        
        composeHeader(wb, sheet, reportDefinition);
        
        return writeWorkbookToOutputStream(wb);
    }

    private void composeBody(XSSFWorkbook wb, 
                                XSSFSheet sheet, 
                                ReportDefinition<T> reportDefinition,
                                List<XSSFCellStyle> formatStyles) {
        List<T> reportRows = reportDefinition.getData();
        if (reportRows!=null && !reportRows.isEmpty()){
            ColumnsDetails columnsDetail = reportDefinition.getColumnsDetail();
            List<Column> columns = columnsDetail.getColumns();
            
            // Creating generic style for all header cells and a collection for store each column style
            XSSFCellStyle xssfGenericColumnStyle = addStyle(wb, wb.createCellStyle(), columnsDetail.getColumnsStyle()); 
            List<XSSFCellStyle> xssfColumnStyles = new ArrayList<XSSFCellStyle>(columns.size());
            
            for (int rowIndex=0; rowIndex<reportRows.size(); rowIndex++){
                // Creating row
                XSSFRow row = sheet.createRow((short)rowIndex+HEADER_ROWS);
                            
                // Getting the element
                T element = reportRows.get(rowIndex);
                FieldIterator<T> fieldIterator = createFieldIterator(columnsDetail, element);
                
                // Generating cells
                generateCells(wb, formatStyles, columns, xssfGenericColumnStyle, xssfColumnStyles, row, fieldIterator);
            }
        }
    }

    private void generateCells(XSSFWorkbook wb, 
                               List<XSSFCellStyle> formatStyles, 
                               List<Column> columns, 
                               XSSFCellStyle xssfGenericColumnStyle, 
                               List<XSSFCellStyle> xssfColumnStyles, 
                               XSSFRow row, 
                               FieldIterator<T> fieldIterator) {
        int colIndex=0;
        while (fieldIterator.hasNext()){
            XSSFCell cell = row.createCell(colIndex);
            setValue(cell, fieldIterator, columns.get(colIndex).getType());
            
            //Assigning styles
            if (xssfColumnStyles.size()<colIndex+1){
                xssfColumnStyles.add(addStyle(wb, xssfGenericColumnStyle, columns.get(colIndex).getDataStyle()));
                
            }
            XSSFCellStyle cellStyle = addDataFormat(wb, formatStyles, xssfColumnStyles.get(colIndex), columns.get(colIndex).getType());
            cell.setCellStyle(cellStyle);
            
            if (columns.get(colIndex).getColumnWidth()!=null){
                cell.getCellStyle().setWrapText(true);
            }
            colIndex++;
        }
    }
    
    private void composeHeader(XSSFWorkbook wb, XSSFSheet sheet, ReportDefinition<T> reportDefinition) {
        HeaderDetails headerDetails = reportDefinition.getHeaderDetail();
        
        // Generate column name rows
        List<Column> columns = reportDefinition.getColumnsDetail().getColumns();
        if (columns!=null && !columns.isEmpty()){
            // Generate header row in position 0
            XSSFRow headerRow = sheet.createRow((short)0);
            // Creating generic style for all header cells
            XSSFCellStyle xssfGenericHeaderStyles = addStyle(wb, wb.createCellStyle(), headerDetails.getHeaderStyle());
            headerRow.setRowStyle(xssfGenericHeaderStyles);
            headerRow.setHeightInPoints(headerDetails.getHeight());
            
            //Inserts header image
            insertHeaderImage(wb, sheet, headerDetails);
            
            //Report Title
            XSSFCell titleCell = headerRow.createCell(headerDetails.getTitleColumnNumber());
            titleCell.setCellValue(reportDefinition.getReportName());
            titleCell.setCellStyle(xssfGenericHeaderStyles);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, headerDetails.getTitleColumnNumber(), headerDetails.getTitleColumnNumber() + TITLE_MERGED_COLUMNS));
            
            // Creating columns name row in position 1
            XSSFRow columnsNameRow = sheet.createRow((short)1);
            // Creating generic style for all columns name cells
            XSSFCellStyle xssfColumnNameStyle = addStyle(wb, wb.createCellStyle(), headerDetails.getColumnNameStyle());
            columnsNameRow.setRowStyle(xssfColumnNameStyle);
            
            // Creating header cells
            generateHeaderCells(wb, sheet, columns, columnsNameRow, xssfColumnNameStyle);
        }
    }

    private void generateHeaderCells(XSSFWorkbook wb, 
                                     XSSFSheet sheet, 
                                     List<Column> columns, 
                                     XSSFRow columnsNameRow,
                                     XSSFCellStyle xssfColumnNameStyle) {
        for (int i=0; i<columns.size(); i++){
            XSSFCell cell = columnsNameRow.createCell(i);
            cell.setCellValue(columns.get(i).getColumnNameValue());
            cell.setCellStyle(addStyle(wb, xssfColumnNameStyle, columns.get(i).getHeaderStyle()));
            if (columns.get(i).getColumnWidth()==null){
                sheet.autoSizeColumn(i);
            }else{
                sheet.setColumnWidth(i, CHARACTER_SIZE*columns.get(i).getColumnWidth());
            }
        }
    }

    private void insertHeaderImage(XSSFWorkbook wb, XSSFSheet sheet, HeaderDetails headerDetails) {
        // Insert image
        if (headerDetails.getImage()!=null){
            int pictureIdx = wb.addPicture(headerDetails.getImage(), Workbook.PICTURE_TYPE_PNG);
            
            //Returns an object that handles instantiating concrete classes
            CreationHelper helper = wb.getCreationHelper();
             
            //Creates the top-level drawing patriarch.
            Drawing drawing = sheet.createDrawingPatriarch();
             
            //Get height and width
            InputStream in = null;
            int imgWidht = 0;
            int imgHeight = 0;
            try{
                in = new ByteArrayInputStream(headerDetails.getImage());
                BufferedImage bimg = ImageIO.read(in);
                imgWidht = bimg.getWidth();
                imgHeight = bimg.getHeight();
            }catch (IOException ioe){
                //do nothing
            }finally{
                if (in!=null){
                    try { 
                        in.close(); 
                    } catch (IOException e) {
                        //do nothing
                    }
                }
            }
            
            //Create an anchor that is attached to the worksheet
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setDx1(0);
            anchor.setDx2(imgWidht * XSSFShape.EMU_PER_PIXEL);
            anchor.setDy1(0);
            anchor.setDy2(imgHeight * XSSFShape.EMU_PER_PIXEL);
            anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);
            
            //Creates a picture
            drawing.createPicture(anchor, pictureIdx);
        }
    }
    
    private void composeTotals(XSSFWorkbook wb, 
                               XSSFSheet sheet, 
                               ReportDefinition<T> reportDefinition,
                               List<XSSFCellStyle> formatStyles) {
        if (reportDefinition.getData()!=null && !reportDefinition.getData().isEmpty()){
            List<Total> totals = reportDefinition.getTotalsDetail().getTotals();
            if (totals!=null && !totals.isEmpty()){
                // Obtaining columns detail
                final ColumnsDetails columnsDetail = reportDefinition.getColumnsDetail();
                
                // Getting total of data report rows
                final Integer totalRows = reportDefinition.getData()!=null? reportDefinition.getData().size() : 0;
                
                // Creating generic style for all header cells
                XSSFCellStyle xssfGenericTotalsStyle = addStyle(wb, wb.createCellStyle(), reportDefinition.getTotalsDetail().getTotalStyle());
                
                // Creating the totals rows at the end of the report
                XSSFRow row = sheet.createRow((short) (totalRows + HEADER_ROWS));
                
                for(int cnt=0;cnt<totals.size();cnt++){
                    generateTotalCell(wb, formatStyles, totals, columnsDetail, totalRows, xssfGenericTotalsStyle, row, cnt);
                }
            }
        }
    }

    private void generateTotalCell(XSSFWorkbook wb, List<XSSFCellStyle> formatStyles, List<Total> totals,
            final ColumnsDetails columnsDetail, final Integer totalRows, XSSFCellStyle xssfGenericTotalsStyle, XSSFRow row, int cnt) {
        Total total = totals.get(cnt);
        
        // Getting column position
        Integer colIndex = columnsDetail.getColumnIndex(total.getColumnName());
        
        // Generate total formula
        String formula = total.getFormula().evaluate(new ExcelExpressionEvaluator(columnsDetail, totalRows));
        
        if (StringUtils.isNotBlank(formula)){
            //Creating new cell for current total
            XSSFCell cell = row.createCell(colIndex);
            cell.setCellType(XSSFCell.CELL_TYPE_FORMULA);
            cell.setCellFormula(formula);
            XSSFCellStyle cellStyle = addDataFormat(wb, formatStyles, xssfGenericTotalsStyle, columnsDetail.getColumns().get(colIndex).getType());
            cell.setCellStyle(cellStyle);
        }
    }

    private List<XSSFCellStyle> createFormatStyles(XSSFWorkbook wb, 
                                                   ReportDefinition<T> reportDefinition) {
        List<XSSFCellStyle> formatStyles = new ArrayList<XSSFCellStyle>(FormatStyle.values().length);
        
        XSSFCellStyle numericStyle = wb.createCellStyle();
        numericStyle.setDataFormat(wb.createDataFormat().getFormat(reportDefinition.getNumericFormat()));
        formatStyles.add(FormatStyle.NUMERIC.ordinal(), numericStyle);
        
        XSSFCellStyle decimalStyle = wb.createCellStyle();
        decimalStyle.setDataFormat(wb.createDataFormat().getFormat(reportDefinition.getDecimalFormat()));
        formatStyles.add(FormatStyle.DECIMAL.ordinal(), decimalStyle);
        
        XSSFCellStyle percentageStyle = wb.createCellStyle();
        percentageStyle.setDataFormat(wb.createDataFormat().getFormat(reportDefinition.getPercentageFormat()));
        formatStyles.add(FormatStyle.PERCENTAGE.ordinal(), percentageStyle);
        
        XSSFCellStyle currencyStyle = wb.createCellStyle();
        currencyStyle.setDataFormat(wb.createDataFormat().getFormat(reportDefinition.getCurrencyFormat()));
        formatStyles.add(FormatStyle.CURRENCY.ordinal(), currencyStyle);
        
        XSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.createDataFormat().getFormat(reportDefinition.getDateFormat()));
        formatStyles.add(FormatStyle.DATE.ordinal(), dateStyle);
        
        return formatStyles;
    }

    private OutputStream writeWorkbookToOutputStream(XSSFWorkbook wb) throws IOException{
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        try {
            wb.write(report);
        } finally {
            report.close();
        }
        return report;
    }
    
    private XSSFCellStyle addStyle(XSSFWorkbook wb, XSSFCellStyle sourceStyle, Style style) {
        XSSFCellStyle mergeStyle = wb.createCellStyle();
        mergeStyle.cloneStyleFrom(sourceStyle);
        if (style!=null){
            if (style.getHorizontalAlignment()!=null) {
                mergeStyle.setAlignment((short) style.getHorizontalAlignment().ordinal());
            }
            
            if (style.getVerticalAlignment()!=null) {
                mergeStyle.setVerticalAlignment((short) style.getVerticalAlignment().ordinal());
            }
            
            if (style.getBorder()!=null){
                mergeStyle.setBorderTop((short) style.getBorder().ordinal());
                mergeStyle.setBorderRight((short) style.getBorder().ordinal());
                mergeStyle.setBorderBottom((short) style.getBorder().ordinal());
                mergeStyle.setBorderLeft((short) style.getBorder().ordinal());
                
                if (style.getBorderColor()!=null){
                    XSSFColor borderColor = transformColor(style.getBorderColor());
                    mergeStyle.setTopBorderColor(borderColor);
                    mergeStyle.setRightBorderColor(borderColor);
                    mergeStyle.setBottomBorderColor(borderColor);
                    mergeStyle.setLeftBorderColor(borderColor);
                }
            }
            if (style.getCellColor()!=null){
                mergeStyle.setFillForegroundColor(transformColor(style.getCellColor()));
                mergeStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
            }
            if ((style.getFontColor()!=null) ||
                (style.getFontSize()!=0) ||
                (style.getFontStyle()!=null) ||
                (style.getFontName()!=null)){
                setFontStyle(wb, style, mergeStyle);
            }
            
            if (style.isHidden()!=null) {
                mergeStyle.setHidden(style.isHidden());
            }
            
            if (style.isWrapped()!=null) {
                mergeStyle.setWrapText(style.isWrapped());
            }
        }
        return mergeStyle;
    }

    private void setFontStyle(XSSFWorkbook wb, Style style, XSSFCellStyle mergeStyle) {
        XSSFFont font = wb.createFont();
        
        if (style.getFontColor()!=null) {
            font.setColor(transformColor(style.getFontColor()));
        }
        
        if (style.getFontSize()!=0) {
            font.setFontHeightInPoints(style.getFontSize());
        }
        
        if (style.getFontStyle()!=null){
            switch (style.getFontStyle()) {
                case BOLD:
                    font.setBold(true);
                    break;
                case ITALIC:
                    font.setItalic(true);
                    break;
                default:
                    break;
            }
        }
        if (style.getFontName()!=null) {
            font.setFontName(getFontName(style.getFontName()));
        }
        mergeStyle.setFont(font);
    }

    private XSSFCellStyle addDataFormat(XSSFWorkbook wb, List<XSSFCellStyle> formatStyles, XSSFCellStyle sourceStyle, CellType cellType) {
        XSSFCellStyle cellStyle = getFormatStyle(formatStyles, cellType);
        
        XSSFCellStyle duplicate = wb.createCellStyle();
        duplicate.cloneStyleFrom(sourceStyle);
        if (cellStyle!=null){
            duplicate.setDataFormat(wb.createDataFormat().getFormat(cellStyle.getDataFormatString()));
        }
        return duplicate;
    }

    private XSSFColor transformColor(Color color) {
        return new XSSFColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
    }
    
    private void setValue(XSSFCell cell, FieldIterator<T> fieldIterator, CellType cellType) {
        Object cellValue = fieldIterator.next();
        
        switch(cellType){
            case STRING:
                cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                if (cellValue!=null){
                    cell.setCellValue(String.valueOf(cellValue));
                }
                break;
            case NUMERIC:
            case DECIMAL:
            case CURRENCY:
                cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
                if (cellValue!=null){
                    cell.setCellValue(((Number) cellValue).doubleValue());
                }
                break;
            case PERCENTAGE:
                cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
                if (cellValue!=null){
                    cell.setCellValue(((Double) cellValue)/ONE_HOUNDRED);
                }
                break;
            case BOOLEAN:
                cell.setCellType(XSSFCell.CELL_TYPE_BOOLEAN);
                if (cellValue!=null){
                    cell.setCellValue((Boolean) cellValue);
                }
                break;
            case DATE:
                cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                if (cellValue!=null){
                    cell.setCellValue((Date) cellValue);
                }
                break;
            default:
                cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                if (cellValue!=null){
                    cell.setCellValue(cellValue.toString());
                }
                break;
        }
    }
    
    private XSSFCellStyle getFormatStyle(List<XSSFCellStyle> formatStyles, CellType cellType){
        XSSFCellStyle cellStyle = null;
        switch(cellType){
            case NUMERIC:
                cellStyle = formatStyles.get(FormatStyle.NUMERIC.ordinal());
                break;
            case DECIMAL:
                cellStyle = formatStyles.get(FormatStyle.DECIMAL.ordinal());
                break;
            case PERCENTAGE:
                cellStyle = formatStyles.get(FormatStyle.PERCENTAGE.ordinal());
                break;
            case CURRENCY:
                cellStyle = formatStyles.get(FormatStyle.CURRENCY.ordinal());
                break;
            case DATE:
                cellStyle = formatStyles.get(FormatStyle.DATE.ordinal());
                break;
            default:
                break;
        }
        return cellStyle;
    }
    
    private String getFontName(FontName fontName) {
        return fontName.name();
    }
    
    private static class ExcelExpressionEvaluator implements ExpressionEvaluator{
        
        final ColumnsDetails columnsDetail;
        final Integer totalRows;
        
        protected ExcelExpressionEvaluator(final ColumnsDetails columnsDetail, final Integer totalRows){
            this.columnsDetail=columnsDetail;
            this.totalRows=totalRows;
        }
        
        @Override
        public String evaluateOperator(String expr1, Operator operator, String expr2) {
            StringBuilder result = null;
            switch (operator) {
                case DIV:
                    StringBuilder div = new StringBuilder(expr1).append(operator.getStringOperator()).append(expr2);
                    result = new StringBuilder("IF(ISERROR(").append(div).append("), 0, ").append(div).append(")");
                    break;
                default:
                    break;
            }
            return result==null ? null: result.toString();
        }
        
        @Override
        public String evaluateFunction(Function function, String expr2) {
            StringBuilder sb = new StringBuilder(function.name()).append("(").append(expr2).append(")");
            return sb.toString();
        }
        
        @Override
        public String evaluateDataset(String columnName) {
            Integer colIndex = columnsDetail.getColumnIndex(columnName);
            String columnLetter = CellReference.convertNumToColString(colIndex);
            StringBuilder sb = new StringBuilder(columnLetter).append("2:").append(columnLetter).append(totalRows+HEADER_ROWS);
            return sb.toString();
        }
    }
}
