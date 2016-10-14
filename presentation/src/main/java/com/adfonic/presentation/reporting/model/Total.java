package com.adfonic.presentation.reporting.model;

/**
 * Entity used to represent the information of a Total cell
 * 
 * Responsibility: Provide details of a Total cell 
 * 
 * @author David Martin
 */
public class Total {
    /** Column name where the total cell applies */
    private String columnName;
    
    /** Formula to generate the total value for this cell */
    private Expression formula;
    
    /**
     * Constructor
     */
    public Total(){
    }
    
    /** 
     * Constructor
     * @param columnName Column name where the total cell applies
     * @param formula Formula to generate the total value for this cell
     */
    public Total(String columnName, Expression formula) {
        this.columnName = columnName;
        this.formula = formula;
    }
    
    // Getters & Setters
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    public Expression getFormula() {
        return formula;
    }
    public void setFormula(Expression formula) {
        this.formula = formula;
    }
}
