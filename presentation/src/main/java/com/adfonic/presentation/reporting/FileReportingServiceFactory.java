package com.adfonic.presentation.reporting;

import com.adfonic.presentation.reporting.impl.ExcelFileReportingService;
import com.adfonic.presentation.reporting.model.FileReportingService;

/**
 * Class to implement factory pattern.
 * 
 * Responsibility: To instantiate file reporting services by type
 * 
 * @author David Martin
 *
 * @param <T> Row class
 */
public class FileReportingServiceFactory<T> {

    /** Enum to define type of reporting services avaiable */
    public enum FileServiceType {
        EXCEL_FILE_REPORTING_SERVICE
    }
    
    /**
     * Factory method
     * 
     * @param serviceType Service type
     * 
     * @return Instance of file reporting service
     */
    public FileReportingService<T> getFileService(FileServiceType serviceType){
        FileReportingService<T> fileReportingService = null;
        switch (serviceType) {
        case EXCEL_FILE_REPORTING_SERVICE:
            fileReportingService = new ExcelFileReportingService<T>();
            break;
        default:
            break;
        }
        return fileReportingService;
    }
}
