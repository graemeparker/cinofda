package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Company;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface UserDao extends BusinessKeyDao<User> {

    User getUserByEmail(String email, FetchStrategy... fetchStrategy);
    User getUserByAlias(String alias, FetchStrategy... fetchStrategy);

    // In the following, it is assumed that email already contains the LIKE wildcards!
    Long countAllForEmailLike(String email);
    List<User> getAllForEmailLike(String email, FetchStrategy... fetchStrategy);
    List<User> getAllForEmailLike(String email, Sorting sort, FetchStrategy... fetchStrategy);
    List<User> getAllForEmailLike(String email, Pagination page, FetchStrategy... fetchStrategy);
    Long countAllForEmailLike(String email, AdfonicUser adfonicUser);
    List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, FetchStrategy... fetchStrategy);
    List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, Sorting sort, FetchStrategy... fetchStrategy);
    List<User> getAllForEmailLike(String email, AdfonicUser adfonicUser, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllEmailsForEmailLike(String email);
    List<String> getAllEmailsForEmailLike(String email);
    List<String> getAllEmailsForEmailLike(String email, Sorting sort);
    List<String> getAllEmailsForEmailLike(String email, Pagination page);

    Long countAllForCompany(Company company);
    List<User> getAllForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<User> getAllForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    List<User> getAllForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);

}
