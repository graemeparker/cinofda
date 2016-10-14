package com.byyd.middleware.common.dao.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Format;
import com.adfonic.domain.Format_;
import com.adfonic.domain.TransparentNetwork;
import com.byyd.middleware.common.dao.FormatDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class FormatDaoJpaImpl extends BusinessKeyDaoJpaImpl<Format> implements FormatDao {

    @Override
    public Format getBySystemName(String systemName, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Format> criteriaQuery = container.getQuery();
        Root<Format> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate predicate = criteriaBuilder.equal(root.get(Format_.systemName), systemName);
        criteriaQuery = criteriaQuery.where(predicate);
        CriteriaQuery<Format> select = criteriaQuery.select(root);

        return find(select);
     }

    /**
     * JDO Version:
     *
     *  Query q = getPersistenceManager().newQuery(AdSpace.class, "p1.contains(this) && formats.contains(vf)");
     *  q.declareVariables("com.adfonic.domain.Format vf");
     *  q.setResult("distinct vf");
     *  q.declareParameters("java.util.Collection p1");
     *  return (Collection<Format>) q.execute(adSpaces);
     *
     * Generated SQL from the JDO version (with AdSpace 1, 2 and 5):
     *
     * SELECT DISTINCT `UNBOUND_VF`.`ID` FROM `AD_SPACE` `THIS`
     *    CROSS JOIN `FORMAT` `UNBOUND_VF`
     *    WHERE EXISTS (
     *        SELECT 1 FROM `AD_SPACE_FORMAT` `THIS_FORMATS_VF`
     *        WHERE `THIS_FORMATS_VF`.`AD_SPACE_ID` = `THIS`.`ID`
     *        AND `UNBOUND_VF`.`ID` = `THIS_FORMATS_VF`.`FORMAT_ID`
     *        AND `UNBOUND_VF`.`ID` = `UNBOUND_VF`.`ID`
     *        AND (
     *            (<1> = `THIS`.`ID`
     *             OR <2> = `THIS`.`ID`
     *             OR <5> = `THIS`.`ID`)
     *        )
     *    )
     *
     * No idea how to implement this using Criteria, so, the generated SQL is reused
     *
     * @param adSpaces
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Format> getSupportedFormats(Collection<AdSpace> adSpaces, FetchStrategy... fetchStrategy) {
        List<Format> formats = new ArrayList<Format>();
        StringBuilder buffer = new StringBuilder();
        List<QueryParameter> params = new ArrayList<QueryParameter>();

        buffer.append("SELECT DISTINCT `UNBOUND_VF`.`ID` FROM `AD_SPACE` `THIS`");
        buffer.append(" CROSS JOIN `FORMAT` `UNBOUND_VF`");
        buffer.append(" WHERE EXISTS (");
        buffer.append("        SELECT 1 FROM `AD_SPACE_FORMAT` `THIS_FORMATS_VF`");
        buffer.append("        WHERE `THIS_FORMATS_VF`.`AD_SPACE_ID` = `THIS`.`ID`");
        buffer.append("        AND `UNBOUND_VF`.`ID` = `THIS_FORMATS_VF`.`FORMAT_ID`");
        buffer.append("        AND `UNBOUND_VF`.`ID` = `UNBOUND_VF`.`ID`");
        if(adSpaces != null && !adSpaces.isEmpty()) {
            buffer.append("        AND (");
            int i = 0;
            for(AdSpace adSpace : adSpaces) {
                if(i > 0) {
                    buffer.append(" OR ");
                }
                buffer.append("`THIS`.`ID` = ?");
                params.add(new QueryParameter(adSpace.getId()));
                i++;
            }
            buffer.append("        )");
        }
        buffer.append(")");
        List<Number> ids = findByNativeQueryPositionalParameters(buffer.toString(), params);

        if(ids != null && !ids.isEmpty()) {
            for(Number id : ids) {
                formats.add(this.getById(id.longValue(), fetchStrategy));
            }
        }

        return formats;
    }

    /**
     * JDO version:
     *
     *  if (campaign.getTransparentNetworks().isEmpty()) {
     *      Query q = pm.newQuery(AdSpace.class, "publication.transparentNetwork == null && status == ps && formats.contains(vf)");
     *      q.declareVariables("com.adfonic.domain.Format vf");
     *      q.setResult("distinct vf");
     *      q.declareParameters("com.adfonic.domain.AdSpace$Status ps");
     *      return (Collection<Format>) q.execute(AdSpace.Status.VERIFIED);
     *  } else {
     *      Query q = pm.newQuery(AdSpace.class, "status == ps && pn.contains(publication.transparentNetwork) && formats.contains(vf)");
     *      q.declareVariables("com.adfonic.domain.Format vf");
     *      q.declareParameters("java.util.Collection pn, com.adfonic.domain.AdSpace$Status ps");
     *      q.setResult("distinct vf");
     *      return (Collection<Format>) q.execute(campaign.getTransparentNetworks(),AdSpace.Status.VERIFIED);
     *  }
     *
     * @param campaign
     * @param fetchStrategy
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Format> getSupportedFormats(Campaign campaign, FetchStrategy... fetchStrategy) {
        List<Format> formats = new ArrayList<Format>();
        StringBuilder buffer = new StringBuilder();
        List<QueryParameter> params = new ArrayList<QueryParameter>();

        if (campaign.getTransparentNetworks().isEmpty()) {
            buffer.append("SELECT DISTINCT ID FROM vw_list_active_formats");
         } else {
            buffer.append("SELECT DISTINCT `UNBOUND_VF`.`ID`");
            buffer.append(" FROM `AD_SPACE` `THIS`");
            buffer.append(" LEFT OUTER JOIN `PUBLICATION` `THIS_PUBLICATION_TRANSPARENT_NETWORK` ON `THIS`.`PUBLICATION_ID` = `THIS_PUBLICATION_TRANSPARENT_NETWORK`.`ID`");
            buffer.append(" CROSS JOIN `FORMAT` `UNBOUND_VF`");
            buffer.append(" WHERE EXISTS (");
            buffer.append("     SELECT 1 FROM `AD_SPACE_FORMAT` `THIS_FORMATS_VF`");
            buffer.append("     WHERE `THIS_FORMATS_VF`.`AD_SPACE_ID` = `THIS`.`ID`");
            buffer.append("     AND `UNBOUND_VF`.`ID` = `THIS_FORMATS_VF`.`FORMAT_ID`");
            buffer.append("     AND `UNBOUND_VF`.`ID` = `UNBOUND_VF`.`ID`");
            buffer.append("     AND `THIS`.`STATUS` = ?");
               params.add(new QueryParameter(AdSpace.Status.VERIFIED.toString()));
               buffer.append("    AND (");
               int i = 0;
               for(TransparentNetwork tn : campaign.getTransparentNetworks()) {
                   if(i > 0) {
                       buffer.append(" OR ");
                   }
                   Long tnId = tn.getId();
                   buffer.append("? = `THIS_PUBLICATION_TRANSPARENT_NETWORK`.`TRANSPARENT_NETWORK_ID`");
                   params.add(new QueryParameter(tnId));
                   i++;
               }
            buffer.append("    )"); // AND
            buffer.append(")"); // EXISTS
        }
        List<Number> ids = findByNativeQueryPositionalParameters(buffer.toString(), params);

        if(ids != null && !ids.isEmpty()) {
            for(Number id : ids) {
                formats.add(this.getById(id.longValue(), fetchStrategy));
            }
        }

        return formats;
    }

}
