package com.adfonic.dto.category;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CategoryPartialDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Name of the channel that represents the default or uncategorized state.
     * This is a workaround to avoid using id=0 to convey the default. NOTE:
     * This MUST match the database exactly.
     */
    public static final String NOT_CATEGORIZED_NAME = "Uncategorized";
    @Source(value = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
