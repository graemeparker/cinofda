package com.adfonic.weve;

public enum OperatorEnum {

    TELEFONICA(1, "Telefonica"),
    OPERATOR_NOT_FOUND(-1, "Error, operator not found");
    
    
    private int id;
    private String serviceUserName;
    
    OperatorEnum(int id, String name) {
        this.id = id;
        this.serviceUserName = name;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getServiceUserName() {
        return this.serviceUserName;
    }
    
    public static String getNameById(int id) {
        for (OperatorEnum item : OperatorEnum.values()) {
            if(item.getId() == id) {
                return item.name();
            }
        } 
        return null;
    }
}
