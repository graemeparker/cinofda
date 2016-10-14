package com.adfonic.beans;

import static com.adfonic.beans.PublicationSettingsBean.PUBLICATION_FS;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Publication;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

/*
 * for auto-complete publication list
 *
 * limit 50
 */
@RequestScoped
@ManagedBean(name="publicationQueryBean")
public class PublicationQueryBean extends BaseBean {
    private static final int MAX_RESULTS = 50;
    private String search;

    public List<Publication> doQuery(String search) {
        List<Publication> results = new ArrayList<Publication>();
        if (StringUtils.isNotBlank(search)) {
            results = getPublicationManager().getAllPublicationsLike(
                    search,
                    new Pagination(0,MAX_RESULTS,
                    new Sorting(SortOrder.asc("name"))),
                    PUBLICATION_FS);
        }
        return results;
    }

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
