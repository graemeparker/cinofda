package com.byyd.middleware.device.dao.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
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
import com.byyd.middleware.device.dao.ModelDao;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class ModelDaoJpaImpl extends BusinessKeyDaoJpaImpl<Model> implements ModelDao {
    
    public Predicate getPredicate(Root<Model> root, ModelFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        
        Predicate namePredicate = null;
        if(!StringUtils.isEmpty(filter.getName())) {
            namePredicate = getNamePredicate(root, filter, criteriaBuilder);
        }
        
        Predicate deletedPredicate = null;
        if(filter.getDeleted() != null) {
            Boolean deleted = filter.getDeleted();
            if(deleted) {
                deletedPredicate = criteriaBuilder.isTrue(root.get(Model_.deleted));
            } else {
                deletedPredicate = criteriaBuilder.isFalse(root.get(Model_.deleted));
            }
        }
        
        Predicate hiddenPredicate = null;
        if(filter.getHidden() != null) {
            Boolean hidden = filter.getHidden();
            if(hidden) {
                hiddenPredicate = criteriaBuilder.isTrue(root.get(Model_.hidden));
            } else {
                hiddenPredicate = criteriaBuilder.isFalse(root.get(Model_.hidden));
            }
        }
        
        Predicate platformsPredicate = null;
        if(!CollectionUtils.isEmpty(filter.getPlatforms())) {
            List<Platform> platforms = filter.getPlatforms();
            for(int i = 0;i < platforms.size();i++) {
                Platform platform = platforms.get(i);
                Predicate p = criteriaBuilder.isMember(platform, root.get(Model_.platforms));
                if(i == 0) {
                    platformsPredicate = p;
                } else {
                    platformsPredicate = or(platformsPredicate, p);
                }
            }
        }
        

        Predicate deviceGroupPredicate = null;
        if(filter.getDeviceGroup() != null) {
            deviceGroupPredicate = criteriaBuilder.equal(root.get(Model_.deviceGroup), filter.getDeviceGroup());
        }
        
        return and(namePredicate, deletedPredicate, hiddenPredicate, platformsPredicate, deviceGroupPredicate);
    }

    private Predicate getNamePredicate(Root<Model> root, ModelFilter filter, CriteriaBuilder criteriaBuilder) {
        Predicate namePredicate;
        String name = filter.getName();
        LikeSpec like = filter.getLikeSpec();
        boolean caseSensitive = filter.isCaseSensitive();
        boolean justVendorName = filter.isJustVendorName();
        Join<Model, Vendor> vendorJoin = root.join(Model_.vendor, JoinType.INNER);
        if (like == null) {
            // This is an equals
            boolean prependVendorName = filter.isPrependVendorName();
            if (prependVendorName) {
                Expression<String> nameExpression = criteriaBuilder.concat(criteriaBuilder.concat(vendorJoin.get(Vendor_.name), " "), root.get(Model_.name));
                if (caseSensitive) {
                    namePredicate = criteriaBuilder.like(nameExpression, name);
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(nameExpression), name.toLowerCase());
                }
            } else {
                if (caseSensitive) {
                    namePredicate = criteriaBuilder.like(root.get(Model_.name), name);
                } else {
                    if (justVendorName) {
                        namePredicate = criteriaBuilder.like(criteriaBuilder.lower(vendorJoin.get(Vendor_.name)), name.toLowerCase());
                    } else {
                        namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Model_.name)), name.toLowerCase());
                    }
                }
            }
        } else {
            // This is a LIKE, possibly with vendor name prepended
            boolean prependVendorName = filter.isPrependVendorName();
            if (prependVendorName) {
                Expression<String> nameExpression = criteriaBuilder.concat(criteriaBuilder.concat(vendorJoin.get(Vendor_.name), " "), root.get(Model_.name));
                if (caseSensitive) {
                    namePredicate = criteriaBuilder.like(nameExpression, like.getPattern(name));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(nameExpression), like.getPattern(name).toLowerCase());
                }
            } else {
                if (caseSensitive) {
                    namePredicate = criteriaBuilder.like(root.get(Model_.name), like.getPattern(name));
                } else {
                    namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Model_.name)), like.getPattern(name).toLowerCase());
                }
            }

        }
        return namePredicate;
    }
    
    @Override
    public Long countAll(ModelFilter filter) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Model> root = criteriaQuery.from(Model.class);

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<Model> findAll(ModelFilter filter, FetchStrategy... fetchStrategy) {
        return findAll(filter, null, null, fetchStrategy);
    }

    @Override
    public List<Model> findAll(ModelFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAll(filter, null, sort, fetchStrategy);
    }

    @Override
    public List<Model> findAll(ModelFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return findAll(filter, page, page.getSorting(), fetchStrategy);
    }

    protected List<Model> findAll(ModelFilter filter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Model> criteriaQuery = container.getQuery();
        Root<Model> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }
    
    protected Model findOne(ModelFilter filter, FetchStrategy... fetchStrategy) {
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Model> criteriaQuery = container.getQuery();
        Root<Model> root = container.getRoot();

        Predicate predicate = getPredicate(root, filter);
        criteriaQuery = criteriaQuery.where(predicate);

        CriteriaQuery<Model> select = criteriaQuery.select(root);
        return find(select);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Model getModelByName(String name, boolean caseSensitive, Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy) {
        return findOne(new ModelFilter(name, caseSensitive, deleted, hidden), fetchStrategy);
        /*
        if(deleted == null) {
            return getByName(name, caseSensitive, fetchStrategy);
        }
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Model> criteriaQuery = container.getQuery();
        Root<Model> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = null;
        if(caseSensitive) {
            namePredicate = criteriaBuilder.equal(root.get(Model_.name), name);
        } else {
            namePredicate = criteriaBuilder.equal(criteriaBuilder.lower(root.get(Model_.name)), name.toLowerCase());
        }
        Predicate deletedPredicate = null;
        if(deleted) {
            deletedPredicate = criteriaBuilder.isTrue(root.get(Model_.deleted));
        } else {
            deletedPredicate = criteriaBuilder.isFalse(root.get(Model_.deleted));
        }
        criteriaQuery = criteriaQuery.where(and(namePredicate, deletedPredicate));
        CriteriaQuery<Model> select = criteriaQuery.select(root);

        return find(select);
        */
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllModels(Boolean deleted, Boolean hidden) {
        return countAll(new ModelFilter(deleted, hidden));
    }
    
    @Override
    public List<Model> getAllModels(Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy) {
        return findAll(new ModelFilter(deleted, hidden), null, null, fetchStrategy);
    }
    @Override
    public List<Model> getAllModels(Boolean deleted, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy) {
           return findAll(new ModelFilter(deleted, hidden), null, sort, fetchStrategy);
    }
    
    @Override
    public List<Model> getAllModels(Boolean deleted, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy) {
           return findAll(new ModelFilter(deleted, hidden), page, page.getSorting(), fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    /*
    protected Predicate getModelsByNamePredicate(Root<Model> root, String name, LikeSpec like, boolean caseSensitive, Boolean deleted, boolean prependVendorName) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        Predicate namePredicate = null;
        if(prependVendorName) {
              Join<Model,Vendor> vendorJoin = root.join(Model_.vendor, JoinType.INNER);
            Expression<String> nameExpression = criteriaBuilder.concat(
                    criteriaBuilder.concat(
                            vendorJoin.get(Vendor_.name),
                            " "),
                    root.get(Model_.name));
            if(caseSensitive) {
                namePredicate = criteriaBuilder.like(nameExpression, like.getPattern(name));
            } else {
                namePredicate = criteriaBuilder.like(criteriaBuilder.lower(nameExpression), like.getPattern(name).toLowerCase());
            }
        } else {
            if(caseSensitive) {
                namePredicate = criteriaBuilder.like(root.get(Model_.name), like.getPattern(name));
            } else {
                namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(Model_.name)), like.getPattern(name).toLowerCase());
            }
        }
        Predicate deletedPredicate = null;
        if(deleted != null) {
            if(deleted) {
                deletedPredicate = criteriaBuilder.isTrue(root.get(Model_.deleted));
            } else {
                deletedPredicate = criteriaBuilder.isFalse(root.get(Model_.deleted));
            }
        }
        Predicate predicate = null;
        if(deletedPredicate == null) {
            predicate = namePredicate;
        } else {
            predicate = and(namePredicate, deletedPredicate);
        }
        return predicate;
    }
    */

    @Override
    public Long countModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName) {
        return countAll(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName));
        /*
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Model> root = criteriaQuery.from(Model.class);

        Predicate predicate = this.getModelsByNamePredicate(root, name, like, caseSensitive, deleted, prependVendorName);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
        */
    }

    @Override
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, FetchStrategy... fetchStrategy) {
        return this.getModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName, null, null, fetchStrategy);
    }

    @Override
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName, null, sort, fetchStrategy);
    }

    @Override
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName, page, page.getSorting(), fetchStrategy);
    }

    protected List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAll(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName), page, sort, fetchStrategy);
        /*
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<Model> criteriaQuery = container.getQuery();
        Root<Model> root = container.getRoot();

        Predicate predicate = this.getModelsByNamePredicate(root, name, like, caseSensitive, deleted, prependVendorName);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
        */
    }

    //------------------------------------------------------------------------------------------

    /*
    protected Predicate getModelsByNameAndPlatformPredicate(Root<Model> root, String name, LikeSpec like, boolean caseSensitive, Boolean deleted, boolean prependVendorName, List<Platform> platforms) {
        Predicate modelsByNamePredicate = this.getModelsByNamePredicate(root, name, like, caseSensitive, deleted, prependVendorName);
        if(platforms == null || platforms.size() == 0) {
            return modelsByNamePredicate;
        }
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate platformPredicate = null;
        for(int i = 0;i < platforms.size();i++) {
            Platform platform = platforms.get(i);
            Predicate p = criteriaBuilder.isMember(platform, root.get(Model_.platforms));
            if(i == 0) {
                platformPredicate = p;
            } else {
                platformPredicate = or(platformPredicate, p);
            }
        }

        Predicate predicate = null;
        if(platformPredicate == null) {
            predicate = modelsByNamePredicate;
        } else {
            predicate = and(modelsByNamePredicate, platformPredicate);
        }
        return predicate;
    }
    */

    @Override
    public Long countModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms) {
        return countAll(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms));
        /*
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Model> root = criteriaQuery.from(Model.class);

        Predicate predicate = this.getModelsByNameAndPlatformPredicate(root, name, like, caseSensitive, deleted, prependVendorName, platforms);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
        */
    }

    @Override
    public List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, FetchStrategy... fetchStrategy) {
        return this.getModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, null, null, fetchStrategy);
    }

    @Override
    public List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, null, sort, fetchStrategy);
    }

    @Override
    public List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, page, page.getSorting(), fetchStrategy);
    }

    protected List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAll(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms), page, sort, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup) {
        return countAll(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup));
    }
    
    @Override
    public List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy) {
        return this.getModelsByNameAndPlatformAndDeviceGroup(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup), null, null, fetchStrategy);
    }

    @Override
    public List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getModelsByNameAndPlatformAndDeviceGroup(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup), null, sort, fetchStrategy);
    }

    @Override
    public List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms, DeviceGroup deviceGroup, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getModelsByNameAndPlatformAndDeviceGroup(new ModelFilter(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup), page, page.getSorting(), fetchStrategy);
    }

    protected List<Model> getModelsByNameAndPlatformAndDeviceGroup(ModelFilter modelFilter, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return findAll(modelFilter, page, sort, fetchStrategy);
    }
    
    @Override
    public List<Model> getModelsByVendorNameAndPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy) {
        return findAll(new ModelFilter(vendorName, null, false, false, false, false, platforms, deviceGroup, true), null, null, fetchStrategy);
    }
}
