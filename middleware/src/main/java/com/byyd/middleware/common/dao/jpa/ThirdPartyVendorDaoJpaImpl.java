package com.byyd.middleware.common.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.ThirdPartyVendor;
import com.adfonic.domain.ThirdPartyVendorType_;
import com.adfonic.domain.ThirdPartyVendor_;
import com.byyd.middleware.common.dao.ThirdPartyVendorDao;
import com.byyd.middleware.common.filter.ThirdPartyVendorFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ThirdPartyVendorDaoJpaImpl extends BusinessKeyDaoJpaImpl<ThirdPartyVendor> implements ThirdPartyVendorDao {

	@Override
	public List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, FetchStrategy... fetchStrategy) {
		return getAll(filter, null, null, fetchStrategy);
	}

	@Override
	public List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
		return getAll(filter, null, sort, fetchStrategy);
	}

	@Override
	public List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
		return getAll(filter, page, page.getSorting(), fetchStrategy);
	}

	@Override
	public List<ThirdPartyVendor> getAll(ThirdPartyVendorFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
		CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
		CriteriaQuery<ThirdPartyVendor> criteriaQuery = container.getQuery();
		Root<ThirdPartyVendor> root = container.getRoot();

		if (filter != null) {
			Predicate predicate = getPredicate(root, filter);
			criteriaQuery = criteriaQuery.where(predicate);
		}

		criteriaQuery = criteriaQuery.select(root);

		criteriaQuery = processOrderBy(criteriaQuery, root, sort);
		return findAll(criteriaQuery, page);
	}

	private Predicate getPredicate(Root<ThirdPartyVendor> root, ThirdPartyVendorFilter filter) {
		CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

		Predicate namePredicate = null;
		Predicate thirdPartyVendorTypeIdsPredicate = null;

		if (!StringUtils.isEmpty(filter.getName())) {
			if (filter.getNameLikeSpec() == null) {
				if (filter.isNameCaseSensitive()) {
					namePredicate = criteriaBuilder.equal(root.get(ThirdPartyVendor_.name), filter.getName());
				} else {
					namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(ThirdPartyVendor_.name)), filter.getName().toLowerCase());
				}
			} else {
				if (filter.isNameCaseSensitive()) {
					namePredicate = criteriaBuilder.like(root.get(ThirdPartyVendor_.name), filter.getNameLikeSpec().getPattern(filter.getName()));
				} else {
					namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(ThirdPartyVendor_.name)), filter.getNameLikeSpec().getPattern(filter.getName()).toLowerCase());
				}
			}
		}
		
		if (CollectionUtils.isNotEmpty(filter.getThirdPartyVendorTypeIds())) {
			thirdPartyVendorTypeIdsPredicate = root.get(ThirdPartyVendor_.thirdPartyVendorType).get(ThirdPartyVendorType_.id).in(filter.getThirdPartyVendorTypeIds());
        }

		return and(namePredicate, thirdPartyVendorTypeIdsPredicate);
	}

}
