package com.adfonic.webservices.dto.mapping;

import java.util.Set;

public class ReferenceSetCopier<R> {

    private final BaseReferenceEntityConverter<R> converter;

    public ReferenceSetCopier(BaseReferenceEntityConverter<R> converter) {
        this.converter = converter;
    }

    public void copy(Set<String> referenceStrs, Set<R> references) {
        if (referenceStrs != null) {
            references.clear();
            for (String reference : referenceStrs) {
                references.add(converter.resolveEntity(reference));
            }
        }
    }

}
