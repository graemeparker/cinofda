package com.byyd.middleware.device.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Model;
import com.adfonic.domain.Model_;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Vendor;
import com.adfonic.domain.Vendor_;
import com.byyd.middleware.device.dao.VendorDao;
import com.byyd.middleware.device.filter.VendorFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class VendorDaoJpaImpl extends BusinessKeyDaoJpaImpl<Vendor> implements VendorDao {

    @Override
    public List<Vendor> getVendorsByPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy) {
        return findAll(new VendorFilter(vendorName, LikeSpec.CONTAINS, false, platforms, deviceGroup), null, null, fetchStrategy);
    }

    protected List<Vendor> findAll(VendorFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Vendor> criteriaQuery = container.getQuery();
        Root<Vendor> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);

        criteriaQuery.distinct(true);
        criteriaQuery = criteriaQuery.select(root);
        
        // Can be empty in case of all vendor model selection (neither device group nor platform)
        if(predicate != null){
            criteriaQuery = criteriaQuery.where(predicate);
        }

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }

    public Predicate getPredicate(Root<Vendor> root, VendorFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = null;
        Predicate platformsPredicate = null;
        Predicate deviceGroupPredicate = null;

        if (!StringUtils.isEmpty(filter.getName())) {
            String name = filter.getName();
            LikeSpec like = filter.getLikeSpec();
            boolean caseSensitive = filter.isCaseSensitive();

            // This is an equals
            if (like == null) {
                if (caseSensitive) {
                    namePredicate = criteriaBuilder.like(root.get(Vendor_.name), name);
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Vendor_.name)), name.toLowerCase());
                }

            } else {
                // This is a LIKE
                if (caseSensitive) {
                    namePredicate = criteriaBuilder.like(root.get(Vendor_.name), like.getPattern(name));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Vendor_.name)), like.getPattern(name).toLowerCase());
                }
            }
        }

        Join<Vendor, Model> modelJoin = root.join(Vendor_.models, JoinType.INNER);

        if (!CollectionUtils.isEmpty(filter.getPlatforms())) {
            List<Platform> platforms = filter.getPlatforms();
            for (int i = 0; i < platforms.size(); i++) {
                Platform platform = platforms.get(i);
                Predicate p = criteriaBuilder.isMember(platform, modelJoin.get(Model_.platforms));
                if (i == 0) {
                    platformsPredicate = p;
                } else {
                    platformsPredicate = or(platformsPredicate, p);
                }
            }
        }

        if (filter.getDeviceGroup() != null) {
            deviceGroupPredicate = criteriaBuilder.equal(modelJoin.get(Model_.deviceGroup), filter.getDeviceGroup());
        }

        return and(namePredicate, platformsPredicate, deviceGroupPredicate);
    }

}
