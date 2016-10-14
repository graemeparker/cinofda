package com.adfonic.beans.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.SortOrder;

import com.adfonic.beans.approval.publication.dto.PublicationDto;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.service.PublicationManager;

public class LazyPublicationListPublicationDataModel extends ApprovalAbstractLazyDataModel<PublicationDto, PublicationManager> {
    private static final transient Logger LOG = Logger.getLogger(LazyPublicationListPublicationDataModel.class.getName());

    private static final long serialVersionUID = 1L;

    private PublicationList publicationList;
    
    public LazyPublicationListPublicationDataModel(PublicationManager publicationManager, PublicationList publicationList) {
        super(publicationManager);

        int numRows = getManager().countPublicationsForPublicationList(publicationList).intValue();
        setRowCount(numRows);
        
        this.publicationList = publicationList;
    }

    @Override
    public PublicationDto getRowData(String rowKey) {
        Publication publication = getManager().getPublicationByExternalId(rowKey, PublicationDto.getFetchStrategy());
        return publication == null ? null : new PublicationDto(publication);
    }

    @Override
    public String getRowKey(PublicationDto publication) {
        return publication.getExternalID();
    }
    
    @Override
    public List<PublicationDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading first=" + first + ", pageSize=" + pageSize + ", sortField=" + sortField + ", sortOrder=" + sortOrder + ", filters=" + filters);
        }

        // Query for a page of data and transform into DTOs
        List<PublicationDto> data = new ArrayList<>();
        for (Publication publication : getManager().getPublicationsForPublicationList(
                publicationList, 
                getPagination(first, pageSize, sortField, sortOrder), 
                PublicationDto.getFetchStrategy())) {
            data.add(new PublicationDto(publication));
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Resulting page size=" + data.size());
            for (PublicationDto p : data) {
                LOG.fine("id: " + p.getId());
            }
        }
        return data;
    }
    
    static Pagination getPagination(int first, int pageSize, String sortField, SortOrder sortOrder) {
        if (StringUtils.isEmpty(sortField)) {
            return new Pagination(first, pageSize, getSorting("id", SortOrder.DESCENDING));
        } else {
            return new Pagination(first, pageSize, getSorting(sortField, sortOrder));
        }
    }

    static Sorting getSorting(String sortField, SortOrder sortOrder) {
        Direction direction = SortOrder.ASCENDING.equals(sortOrder) ? Direction.ASC : Direction.DESC;
        switch (sortField) {
        case "id":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Publication.class, "id"));
        case "name":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Publication.class, "name"));
        case "friendlyName":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Publication.class, "friendlyName"));
        case "externalID":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Publication.class, "externalID"));
        case "publisher":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, Publisher.class, "name"));
        case "publicationType":
            return new Sorting(new com.byyd.middleware.iface.dao.SortOrder(direction, PublicationType.class, "name"));
        default:
            LOG.warning("Sort field not supported (yet): " + sortField);
            return null;
        }
    }
}