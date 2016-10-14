package com.adfonic.presentation.reporting.model;

/**
 * Class which represents one color used in a report 
 * (cells background, font color, border color, etc.)
 * 
 * Responsibility: To maintains the RGB information for one color 
 * 
 * @author David Martin
 */
public class Color {
    
    private static final Integer MAX_VALUE = 255;
    private static final Integer MIN_VALUE = 0;
    
    // some basic colors
    public static final Color BLACK = new Color(MAX_VALUE, MAX_VALUE, MAX_VALUE);
    public static final Color WHITE = new Color(MIN_VALUE, MIN_VALUE, MIN_VALUE);
    
    private int red;
    private int green;
    private int blue;
    
    public Color(){
    }

    public Color(int red, int green, int blue) {
        super();
        this.red = red%(MAX_VALUE+1);
        this.green = green%(MAX_VALUE+1);
        this.blue = blue%(MAX_VALUE+1);
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red%(MAX_VALUE+1);
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green%(MAX_VALUE+1);
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue%(MAX_VALUE+1);
    }
}
