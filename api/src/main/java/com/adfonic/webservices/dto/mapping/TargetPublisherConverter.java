package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Publisher;
import com.adfonic.domain.TargetPublisher;
import com.adfonic.webservices.exception.ValidationException;
import com.byyd.middleware.account.filter.TargetPublisherFilter;

public class TargetPublisherConverter extends BaseReferenceEntityConverter<Publisher> {

    private final boolean satisfiesOldApplicableDspRole;


    public TargetPublisherConverter(boolean satisfiesOldApplicableDspRole) {
        super(Publisher.class, "name");
        this.satisfiesOldApplicableDspRole=satisfiesOldApplicableDspRole;
    }


    public TargetPublisherConverter() {
        this(false);
    }


    @Override
    // TODO check return value before doing same
    protected Publisher resolveEntity(String name) {
        if (satisfiesOldApplicableDspRole == false) {
            return null;
        }

        TargetPublisherFilter filter = new TargetPublisherFilter();
        filter.setName(name);

        TargetPublisher publisher = getPublisherManager().getTargetPublisherByPublisherId(filter);// TODO - this middleware method's name is misleading
        if (publisher == null) {
            throw new ValidationException("Invalid target publisher!");
        }
        return publisher.getPublisher();
    }


    // domain imposed assymetric mapping
    @Override
    public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
        if (source instanceof Publisher) {
            TargetPublisherFilter filter = new TargetPublisherFilter();
            filter.setPublisher((Publisher) source);

            TargetPublisher targetPublisher = getPublisherManager().getTargetPublisherByPublisherId(filter);
            if (targetPublisher!=null){
                return targetPublisher.getName();
            }else{
                return "";
            }
        }

        return super.convert(destination, source, destClass, sourceClass);
    }
}
