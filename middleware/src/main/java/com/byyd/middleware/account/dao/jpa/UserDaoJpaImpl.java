package com.byyd.middleware.account.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Company;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.byyd.middleware.account.dao.UserDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;
import com.byyd.middleware.iface.dao.jpa.QueryParameter;

@Repository
public class UserDaoJpaImpl extends BusinessKeyDaoJpaImpl<User> implements UserDao {

    @Override
    public User getUserByEmail(String email, FetchStrategy... fetchStrategy){
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<User> criteriaQuery = container.getQuery();
        Root<User> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(User_.email), email);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

    @Override
    public User getUserByAlias(String alias, FetchStrategy... fetchStrategy){
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<User> criteriaQuery = container.getQuery();
        Root<User> root = container.getRoot();
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();

        Predicate creativeExpression = criteriaBuilder.equal(root.get(User_.alias), alias);
        criteriaQuery = criteriaQuery.where(creativeExpression);

        criteriaQuery = criteriaQuery.select(root);

        return find(criteriaQuery);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllForEmailLike(String email, AdfonicUser adfonicUser){
        StringBuilder sb = getUsersForAdfonicUserQuery(true);
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(adfonicUser.getId()));
        return this.executeAggregateFunctionByNativeQueryPositionalParameters(sb.toString(), params).longValue();
    }
    
    @Override
    public Long countAllForEmailLike(String email){
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate emailExpression = criteriaBuilder.like(root.get(User_.email), email);
        criteriaQuery = criteriaQuery.where(emailExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<User> getAllForEmailLike(String email, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, null, null, null, fetchStrategy);
    }

    @Override
    public List<User> getAllForEmailLike(String email, Sorting sort, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, null, null, sort, fetchStrategy);
    }

    @Override
    public List<User> getAllForEmailLike(String email, Pagination page, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, page, page.getSorting(), fetchStrategy);
    }
    
    @Override
    public List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, adfonicUser, null, null, fetchStrategy);
    }

    @Override
    public List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, Sorting sort, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, adfonicUser, null, sort, fetchStrategy);
    }

    @Override
    public List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, Pagination page, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, adfonicUser, page, page.getSorting(), fetchStrategy);
    }

    public List<User> getAllForEmailLike(String email, Pagination page, Sorting sort, FetchStrategy... fetchStrategy){
        return getAllForEmailLike(email, null, page, sort, fetchStrategy);
    }
    
    public List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, Pagination page, Sorting sort, FetchStrategy... fetchStrategy){
        if(adfonicUser==null){
            CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
            CriteriaQuery<User> criteriaQuery = container.getQuery();
            Root<User> root = container.getRoot();
            CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
    
            Predicate emailExpression = criteriaBuilder.like(root.get(User_.email), email);
            criteriaQuery = criteriaQuery.where(emailExpression);
    
            criteriaQuery = criteriaQuery.select(root);
    
            criteriaQuery = processOrderBy(criteriaBuilder, criteriaQuery, root, sort);
            return findAll(criteriaQuery, page);
        }else{
            return getAllForEmailLikeAndAdfonicUser(email, adfonicUser, page, sort, fetchStrategy);
        }
    }
    
    protected StringBuilder getUsersForAdfonicUserQuery(boolean countOnly) {
        // Remember, to be able to use sorting on both the native and the later
        // Criteria query that gets the actual entities out, you must alias each table queried
        // using the name of the class that maps it. So, to sort by fields on USER, 
        // we must join with it and alias it to User 
        return new StringBuilder("SELECT")
                                .append(countOnly ? " COUNT(DISTINCT User.ID)" : " DISTINCT(User.ID)")
                                .append(" FROM USER User, ADFONIC_USER_USER auu")
                                .append(" WHERE User.ID = auu.USER_ID AND auu.ADFONIC_USER_ID = ?")
                                .append(" AND User.EMAIL LIKE ?");
    }
    
    @SuppressWarnings("unchecked")
    protected List<User> getAllForEmailLikeAndAdfonicUser(String email, AdfonicUser adfonicUser, Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        StringBuilder sb = getUsersForAdfonicUserQuery(false);
        if(sort != null && !sort.isEmpty()) {
            sb.append(" ORDER BY " + sort.toString(true).toUpperCase());
        }
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        params.add(new QueryParameter(adfonicUser.getId()));
        params.add(new QueryParameter(email));
        List<Long> ids = null;
        if(page != null) {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), page.getOffet(), page.getLimit(), params);
        } else {
            ids = this.findByNativeQueryPositionalParameters(sb.toString(), params);
        }
        if(CollectionUtils.isEmpty(ids)) {
            return new ArrayList<User>();
        } else {
            return this.getObjectsByIds(ids, sort, fetchStrategy);
        }
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllEmailsForEmailLike(String email){
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate emailExpression = criteriaBuilder.like(root.get(User_.email), email);
        criteriaQuery = criteriaQuery.where(emailExpression);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<String> getAllEmailsForEmailLike(String email){
        return getAllEmailsForEmailLike(email, null, null);
    }

    @Override
    public List<String> getAllEmailsForEmailLike(String email, Sorting sort){
        return getAllEmailsForEmailLike(email, null, sort);
    }

    @Override
    public List<String> getAllEmailsForEmailLike(String email, Pagination page){
        return getAllEmailsForEmailLike(email, page, page.getSorting());
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllEmailsForEmailLike(String email, Pagination page, Sorting sort){
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate emailExpression = criteriaBuilder.like(root.get(User_.email), email);
        criteriaQuery = criteriaQuery.where(emailExpression);

        criteriaQuery = criteriaQuery.select(root.get(User_.email));

        criteriaQuery = processOrderByForObjects(criteriaBuilder, criteriaQuery, root, sort);
        return findAllObjects(criteriaQuery, page);
    }

    
    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllForCompany(Company company) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = criteriaQuery.from(User.class);

        Predicate predicate = criteriaBuilder.equal(root.get(User_.company), company);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(root));

        return executeLongAggregateFunction(criteriaQuery);
    }

    @Override
    public List<User> getAllForCompany(Company company, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, null, null, fetchStrategy);
    }

    @Override
    public List<User> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, page, page.getSorting(), fetchStrategy);
    }

    @Override
    public List<User> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy) {
        return getAllForCompany(company, null, sort, fetchStrategy);
    }

    protected List<User> getAllForCompany(Company company, Pagination page, Sorting sort, FetchStrategy ... fetchStrategy) {
        CriteriaBuilder criteriaBuilder = getTransactionalEntityManager().getCriteriaBuilder();
        CriteriaQueryContainer container = createCriteriaQuery(fetchStrategy);
        CriteriaQuery<User> criteriaQuery = container.getQuery();
        Root<User> root = container.getRoot();

        Predicate predicate = criteriaBuilder.equal(root.get(User_.company), company);
        criteriaQuery = criteriaQuery.where(predicate);

        criteriaQuery = criteriaQuery.select(root);

        criteriaQuery = processOrderBy(criteriaQuery, root, sort);
        return findAll(criteriaQuery, page);
    }


}
