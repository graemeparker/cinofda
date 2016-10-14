package com.adfonic.presentation.reporting.model;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract class to represent a file reporting service
 * 
 * Responsibility: To define common methods to all file reporting services
 * 
 * @author David Martin
 *
 * @param <T> Row class
 */
public abstract class FileReportingService<T> { 
    
    /**
     * Create a new field iterator instance
     * 
     * @param columnsDetail Column details information
     * @param object Object to iterate
     * 
     * @return FieldIterator instance
     */
    public FieldIterator<T> createFieldIterator(ColumnsDetails columnsDetail, T object){
        return new FieldIterator<T>(columnsDetail, object);
    }
    
    /**
     * Create a new report from a datamodel collection
     * 
     * @param reportDefinition Report detail information
     * 
     * @return Stream with the generated report
     * 
     * @throws IOException
     */
    public abstract OutputStream createReport(ReportDefinition<T> reportDefinition)  throws IOException;
}
