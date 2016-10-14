package com.adfonic.presentation.reporting.model;

import java.util.Arrays;


/**
 * Class that groups the information detail for the report header
 * 
 * Responsibility: To provide information for the report header
 * 
 * @author David Martin
 */
public class HeaderDetails {

    /** Generic style to use in the header row */
    private Style headerStyle;
    
    /** Generic style to use in the columns name row */
    private Style columnNameStyle;
    
    /** Brang logo */
    private byte[] image;
    
    /** Title column number */
    private int titleColumnNumber = 1;
    
    /** Height */
    private float height = -1;
    
    public HeaderDetails(Style headerStyle, Style columnNameStyle, byte[] image, int titleColumnNumber, float height){
        this.headerStyle = headerStyle;
        this.columnNameStyle = columnNameStyle;
        this.image = Arrays.copyOf(image, image.length);
        this.titleColumnNumber = titleColumnNumber;
        this.height = height;
    }

    public Style getHeaderStyle() {
        return headerStyle;
    }

    public void setHeaderStyle(Style headerStyle) {
        this.headerStyle = headerStyle;
    }
    
    public Style getColumnNameStyle() {
        return columnNameStyle;
    }

    public void setColumnNameStyle(Style columnNameStyle) {
        this.columnNameStyle = columnNameStyle;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = Arrays.copyOf(image, image.length);
    }

    public int getTitleColumnNumber() {
        return titleColumnNumber;
    }

    public void setTitleColumnNumber(int titleColumnNumber) {
        this.titleColumnNumber = titleColumnNumber;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
}
