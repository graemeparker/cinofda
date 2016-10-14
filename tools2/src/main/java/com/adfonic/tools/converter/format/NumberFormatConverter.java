package com.adfonic.tools.converter.format;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.adfonic.presentation.FacesUtils;

public class NumberFormatConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    private String unitsPosition;

    private String format;

    private String units;

    private Integer decimals;

    private static final Position DEFAULT_POSITION = Position.BACK;

    private static final Format DEFAULT_FORMAT = Format.NORMAL;

    private enum Position {
        FRONT, BACK
    };

    private enum Format {
        ABBREVIATED, NORMAL
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        NumberDisplay number = new NumberDisplay();

        if (value == null || value.trim().length() == 0) {
            return null;
        }

        String[] parts = value.split(".");
        if (parts.length > 2) {
            throw new ConverterException(new FacesMessage(FacesUtils.getLocalizedMessage("messages.converter.exception.notnumber")));
        } else if (parts.length == 1) {
            Long n;
            try {
                String noComas = value.replaceAll(",", "");
                n = Long.parseLong(noComas);
            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesUtils.getLocalizedMessage("messages.converter.exception.notnumber")));
            }
            number.setLongValue(n);
        }
        // double number
        else if (parts.length == 2) {
            Double n;
            try {
                String noComas = value.replaceAll(",", "");
                n = Double.parseDouble(noComas);
            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesUtils.getLocalizedMessage("messages.converter.exception.notnumber")));
            }
            number.setDoublevalue(n);
        } else {
            return null;
        }

        return number;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String stringValue = "";

        NumberDisplay num;
        try {
            if (value instanceof Double) {
                num = new NumberDisplay((Double) value);
            } else {
                num = new NumberDisplay((Long) value);
            }
        } catch (ClassCastException e) {
            return value.toString();
        }

        if (units != null) {
            num.setUnit(units);
        }

        if (decimals != null) {
            num.setDecimals(decimals);
        }

        Position positionEnum = unitsPosition == null ? DEFAULT_POSITION : Position.valueOf(unitsPosition.toUpperCase());

        if (value != null) {
            Format formatEnum = format == null ? DEFAULT_FORMAT : Format.valueOf(format.toUpperCase());
            if (formatEnum == Format.ABBREVIATED) {
                stringValue = num.getAbbreviatedNumber();
            } else {
                stringValue = num.getNormalNumber(context.getELContext().getLocale());
            }

            // Units position
            if (positionEnum == Position.FRONT) {
                return num.getUnit() + stringValue;
            } else {
                return stringValue + num.getUnit();
            }

        }

        return stringValue;
    }

    public String getUnitsPosition() {
        return unitsPosition;
    }

    public void setUnitsPosition(String unitsPosition) {
        this.unitsPosition = unitsPosition;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

}
