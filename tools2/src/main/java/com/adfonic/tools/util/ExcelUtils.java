package com.adfonic.tools.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.adfonic.dto.geotarget.LocationTargetDto;

public class ExcelUtils {

    private static String POST_CODE_HEADER = "Postal/Zip Code";
    private static String RADIUS_HEADER = "Radius (miles)";
    private static String NAME_HEADER = "Name";
    private static String LATITUDE_HEADER = "Latitude";
    private static String LONGITUDE_HEADER = "Longitude";

    public static List<LocationTargetDto> getLocationTargetFromExcel(InputStream inputFile, boolean isPostCode) throws IOException {
        List<LocationTargetDto> result = new ArrayList<LocationTargetDto>();

        // Create Workbook instance holding reference to .xls file
        HSSFWorkbook workbook = new HSSFWorkbook(inputFile);

        // Get first/desired sheet from the workbook
        HSSFSheet sheet = workbook.getSheetAt(0);

        // Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();
        // Headers coulumn
        if (rowIterator.hasNext()) {
            Row header = rowIterator.next();
            if (isCorrectHeader(isPostCode, header)) {
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    // For each row, add location targets
                    LocationTargetDto location = getLocationFromRow(isPostCode, row);
                    if (location != null) {
                        result.add(location);
                    }
                }
            } else {
                return null;
            }
        }

        return result;
    }

    private static LocationTargetDto getLocationFromRow(boolean isPostCode, Row row) {
        LocationTargetDto locationTarget = new LocationTargetDto();
        if (isPostCode) {
            // Header = Postcode, Radius
            Cell nameCell = row.getCell(0);
            Cell radiusCell = row.getCell(1);
            if (nameCell != null && nameCell.getCellType() == Cell.CELL_TYPE_STRING) {
                locationTarget.setName(nameCell.getStringCellValue());
            } else if (nameCell != null && nameCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                locationTarget.setName(Integer.toString((int) nameCell.getNumericCellValue()));
            } else {
                return null;
            }
            if (radiusCell != null && radiusCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                locationTarget.setRadiusMiles(new BigDecimal(Double.toString(radiusCell.getNumericCellValue())));
            } else {
                return null;
            }
        } else {
            Cell nameCell = row.getCell(0);
            Cell latCell = row.getCell(1);
            Cell lonCell = row.getCell(2);
            Cell radiusCell = row.getCell(3);
            // Header = Name, Latitude, Longitude, Radius
            if (nameCell != null && nameCell.getCellType() == Cell.CELL_TYPE_STRING) {
                locationTarget.setName(nameCell.getStringCellValue());
            } else if (nameCell != null && nameCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                locationTarget.setName(Integer.toString((int) nameCell.getNumericCellValue()));
            } else {
                return null;
            }
            if (latCell != null && latCell.getCellType() == Cell.CELL_TYPE_NUMERIC && latCell.getNumericCellValue() < 90
                    && latCell.getNumericCellValue() > -90) {
                locationTarget.setLatitude(new BigDecimal(Double.toString(latCell.getNumericCellValue())));
            } else {
                return null;
            }
            if (lonCell != null && lonCell.getCellType() == Cell.CELL_TYPE_NUMERIC && lonCell.getNumericCellValue() < 180
                    && lonCell.getNumericCellValue() > -180) {
                locationTarget.setLongitude(new BigDecimal(Double.toString(lonCell.getNumericCellValue())));
            } else {
                return null;
            }
            if (radiusCell != null && radiusCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                locationTarget.setRadiusMiles(new BigDecimal(Double.toString(radiusCell.getNumericCellValue())));
            } else {
                return null;
            }

        }
        return locationTarget;

    }

    private static boolean isCorrectHeader(boolean isPostCode, Row row) {
        if (isPostCode) {
            // Header = Postcode, Radius
            Cell nameCell = row.getCell(0);
            Cell radiusCell = row.getCell(1);
            if (nameCell == null || radiusCell == null || nameCell.getCellType() != Cell.CELL_TYPE_STRING
                    || !nameCell.getStringCellValue().equals(POST_CODE_HEADER) || radiusCell.getCellType() != Cell.CELL_TYPE_STRING
                    || !radiusCell.getStringCellValue().equals(RADIUS_HEADER)) {
                return false;
            }
        } else {
            Cell nameCell = row.getCell(0);
            Cell latCell = row.getCell(1);
            Cell lonCell = row.getCell(2);
            Cell radiusCell = row.getCell(3);
            // Header = Name, Latitude, Longitude, Radius
            if (nameCell == null || radiusCell == null || latCell == null || lonCell == null
                    || nameCell.getCellType() != Cell.CELL_TYPE_STRING || !nameCell.getStringCellValue().equals(NAME_HEADER)
                    || latCell.getCellType() != Cell.CELL_TYPE_STRING || !latCell.getStringCellValue().equals(LATITUDE_HEADER)
                    || lonCell.getCellType() != Cell.CELL_TYPE_STRING || !lonCell.getStringCellValue().equals(LONGITUDE_HEADER)
                    || radiusCell.getCellType() != Cell.CELL_TYPE_STRING || !radiusCell.getStringCellValue().equals(RADIUS_HEADER)) {
                return false;
            }

        }

        return true;
    }
}
