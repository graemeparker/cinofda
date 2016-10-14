package com.byyd.middleware.account.service;

import java.util.List;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AdminRole;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Company;
import com.adfonic.domain.Role;
import com.adfonic.domain.Role.RoleType;
import com.adfonic.domain.User;
import com.byyd.middleware.account.filter.AdfonicUserFilter;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.BaseManager;

public interface UserManager extends BaseManager {

    //------------------------------------------------------------------------------------------
    // User
    //------------------------------------------------------------------------------------------
    User newUser(Company company, String email, String password, FetchStrategy... fetchStrategy);
    User newUser(Company company, String firstName, String lastName, String email, String password, String alias, FetchStrategy... fetchStrategy);

    User getUserById(String id, FetchStrategy... fetchStrategy);
    User getUserById(Long id, FetchStrategy... fetchStrategy);
    //public User create(User user);
    User update(User user);
    void delete(User user);
    void deleteUsers(List<User> list);

    User getUserByEmail(String email, FetchStrategy... fetchStrategy);
    User getUserByAlias(String alias, FetchStrategy... fetchStrategy);

    Long countAllUsersForEmailLike(String email);
    List<User> getAllUsersForEmailLike(String email, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, Sorting sort, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, Pagination page, FetchStrategy... fetchStrategy);
    Long countAllUsersForEmailLike(String email, AdfonicUser adfonicUser);
    List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, Sorting sort, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllUsersForEmailLike(String email, LikeSpec likeSpec);
    List<User> getAllUsersForEmailLike(String email, LikeSpec likeSpec, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, LikeSpec likeSpec, Sorting sort, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, LikeSpec likeSpec, Pagination page, FetchStrategy... fetchStrategy);
    Long countAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec);
    List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec, Sorting sort, FetchStrategy... fetchStrategy);
    List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllUserEmailsForEmailLike(String email);
    List<String> getAllUsersEmailsForEmailLike(String email);
    List<String> getAllUsersEmailsForEmailLike(String email, Sorting sort);
    List<String> getAllUsersEmailsForEmailLike(String email, Pagination page);

    Long countAllUserEmailsForEmailLike(String email, LikeSpec likeSpec);
    List<String> getAllUsersEmailsForEmailLike(String email, LikeSpec likeSpec);
    List<String> getAllUsersEmailsForEmailLike(String email, LikeSpec likeSpec, Sorting sort);
    List<String> getAllUsersEmailsForEmailLike(String email, LikeSpec likeSpec, Pagination page);

    boolean userHasRole(User user, String roleName);
    boolean userHasCompanyRole(User user, String roleName);

    boolean isAgencyUser(User user);

    User updateLastLogin(User user);

    boolean isUserAuthorizedToManageAdvertiser(User user, Advertiser advertiser);

    String newDeveloperKey();
    
    Long countAllUsersForCompany(Company company);
    List<User> getAllUsersForCompany(Company company, FetchStrategy ... fetchStrategy);
    List<User> getAllUsersForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy);
    List<User> getAllUsersForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy);

    //------------------------------------------------------------------------------------------
    // Role
    //------------------------------------------------------------------------------------------
    Role newRole(String name, RoleType type);

    Role getRoleById(String id);
    Role getRoleById(Long id);
    Role update(Role role);
    void delete(Role role);
    void deleteRoles(List<Role> list);

    Role getRoleByName(String name);

    Long countAllRoles(RoleType roleType);
    List<Role> getAllRoles(RoleType roleType, FetchStrategy... fetchStrategy);
    List<Role> getAllRoles(RoleType roleType, Sorting sort, FetchStrategy... fetchStrategy);
    List<Role> getAllRoles(RoleType roleType, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllUserRoles();
    List<Role> getAllUserRoles(FetchStrategy... fetchStrategy);
    List<Role> getAllUserRoles(Sorting sort, FetchStrategy... fetchStrategy);
    List<Role> getAllUserRoles(Pagination page, FetchStrategy... fetchStrategy);

    Long countAllCompanyRoles();
    List<Role> getAllCompanyRoles(FetchStrategy... fetchStrategy);
    List<Role> getAllCompanyRoles(Sorting sort, FetchStrategy... fetchStrategy);
    List<Role> getAllCompanyRoles(Pagination page, FetchStrategy... fetchStrategy);
    
    //------------------------------------------------------------------------------------------
    // AdminRole
    //------------------------------------------------------------------------------------------

    AdminRole getAdminRoleById(String id);
    AdminRole getAdminRoleById(Long id);
    AdminRole create(AdminRole adminRole);
    AdminRole update(AdminRole adminRole);
    void delete(AdminRole adminRole);
    void deleteAdminRoles(List<AdminRole> list);

    AdminRole getAdminRoleByName(String name);

    Long countAllAdminRoles();
    List<AdminRole> getAllAdminRoles();
    List<AdminRole> getAllAdminRoles(Sorting sort);
    List<AdminRole> getAllAdminRoles(Pagination page);

    //------------------------------------------------------------------------------------------
    // AdfonicUser
    //------------------------------------------------------------------------------------------

    AdfonicUser getAdfonicUserById(String id, FetchStrategy... fetchStrategy);
    AdfonicUser getAdfonicUserById(Long id, FetchStrategy... fetchStrategy);
    AdfonicUser create(AdfonicUser adfonicUser);
    AdfonicUser update(AdfonicUser adfonicUser);
    void delete(AdfonicUser adfonicUser);
    void deleteAdfonicUsers(List<AdfonicUser> list);

    AdfonicUser getAdfonicUserByEmail(String emailAddress, FetchStrategy... fetchStrategy);
    AdfonicUser getAdfonicUserByLoginName(String loginName, FetchStrategy... fetchStrategy);

    Long countAllAdfonicUsers();
    List<AdfonicUser> getAllAdfonicUsers(FetchStrategy... fetchStrategy);
    List<AdfonicUser> getAllAdfonicUsers(Sorting sort, FetchStrategy... fetchStrategy);
    List<AdfonicUser> getAllAdfonicUsers(Pagination page, FetchStrategy... fetchStrategy);
    
    List<User> getUsersForAdfonicUser(AdfonicUser adfonicUser, FetchStrategy... fetchStrategy);

    boolean adfonicUserHasAdminRole(AdfonicUser user, String roleName);
    
    List<AdfonicUser> getAllAdfonicUsers(AdfonicUserFilter filter, FetchStrategy... fetchStrategy);
}
