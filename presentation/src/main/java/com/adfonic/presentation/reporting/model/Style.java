package com.adfonic.presentation.reporting.model;

/**
 * Class which contains all information related with style for one cell in the report.
 * 
 * Responsibility: To provide detail information for a cell style 
 * 
 * @author David Martin
 */
public class Style {

    /** Horizontal alignment values */
    public enum HorizontalAlignment {
        ALIGN_GENERAL, 
        ALIGN_LEFT, 
        ALIGN_CENTER, 
        ALIGN_RIGHT
    }
    
    /** Vertical alignment values */
    public enum VerticalAlignment {
        VERTICAL_TOP, 
        VERTICAL_CENTER, 
        VERTICAL_BOTTOM, 
        VERTICAL_JUSTIFY
    }
    
    /** Border type values */
    public enum Border {
        BORDER_NONE, BORDER_THIN, BORDER_MEDIUM, BORDER_DASHED, BORDER_DOTTED, BORDER_THICK, 
        BORDER_DOUBLE, BORDER_HAIR, BORDER_MEDIUM_DASHED, BORDER_DASH_DOT, BORDER_MEDIUM_DASH_DOT, BORDER_DASH_DOT_DOT, 
        BORDER_MEDIUM_DASH_DOT_DOT, BORDER_SLANTED_DASH_DOT
    }
    
    /** Font style values */
    public enum FontStyle{
        BOLD, ITALIC
    }
    
    /** Font name values */
    public enum FontName {
        TAHOMA, HELVETICA
    }
    
    /** Horizontal alignment 
     * @see HorizontalAlignment */
    private HorizontalAlignment horizontalAlignment;
    
    /** Vertical alignment 
     * @see VerticalAlignment */
    private VerticalAlignment verticalAlignment;
    
    /** Border type 
     * @see Border */
    private Border border;
    
    /** 
     * Cell color 
     * @see Color */
    private Color cellColor;
    
    /** 
     * Font color 
     * @see Color */
    private Color fontColor;
    
    /** 
     * Font Style 
     * @see FontStyle */
    private FontStyle fontStyle;
    
    
    /** 
     * Font Name 
     * @see FontName */
    private FontName fontName;
    
    /** Font size */
    private short fontSize;
    
    /** 
     * Border color 
     * @see Color */
    private Color borderColor;
    
    /** Boolean value to specify if the cell is hidden or does not */
    private Boolean hidden;
    
    /** Boolean value to specify if the text have to be wrapped in the cell which is contained */
    private Boolean wrapped;
    
    // Getters & Setters
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }
    public Border getBorder() {
        return border;
    }
    public void setBorder(Border border) {
        this.border = border;
    }
    public Color getCellColor() {
        return cellColor;
    }
    public void setCellColor(Color cellColor) {
        this.cellColor = cellColor;
    }
    public Color getFontColor() {
        return fontColor;
    }
    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }
    public FontStyle getFontStyle() {
        return fontStyle;
    }
    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
    }
    public FontName getFontName() {
        return fontName;
    }
    public void setFontName(FontName fontName) {
        this.fontName = fontName;
    }
    public short getFontSize() {
        return fontSize;
    }
    public void setFontSize(short fontSize) {
        this.fontSize = fontSize;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public Boolean getWrapped() {
        return wrapped;
    }
    public Color getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
    public Boolean isHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
    public Boolean isWrapped() {
        return wrapped;
    }
    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }
}
