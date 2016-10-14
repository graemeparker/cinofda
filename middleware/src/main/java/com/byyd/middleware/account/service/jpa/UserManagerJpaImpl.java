package com.byyd.middleware.account.service.jpa;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AccountType;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AdminRole;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Company;
import com.adfonic.domain.Role;
import com.adfonic.domain.Role.RoleType;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.byyd.middleware.account.dao.AdfonicUserDao;
import com.byyd.middleware.account.dao.AdminRoleDao;
import com.byyd.middleware.account.dao.RoleDao;
import com.byyd.middleware.account.dao.UserDao;
import com.byyd.middleware.account.filter.AdfonicUserFilter;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("userManager")
public class UserManagerJpaImpl extends BaseJpaManagerImpl implements UserManager {
    
    private static final String USER_FIELD_ROLES = "roles";
    private static final String USER_FIELD_EMAIL = "email";

    @Autowired(required=false)
    private UserDao userDao;
    
    @Autowired(required=false)
    private RoleDao roleDao;

    @Autowired(required=false)
    private AdminRoleDao adminRoleDao;

    @Autowired(required=false)
    private AdfonicUserDao adfonicUserDao;
    
    private Random random;
    
    public UserManagerJpaImpl() {
        this.random = new Random(System.currentTimeMillis());
    }
    
    //------------------------------------------------------------------------------------------
    // User
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public User newUser(Company company, String email, String password, FetchStrategy... fetchStrategy) {
        return this.newUser(company, null, null, email, password, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public User newUser(Company company, String firstName, String lastName, String email, String password, String alias, FetchStrategy... fetchStrategy) {
        User user = new User(company, email, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAlias(alias);
        user.setDeveloperKey(this.newDeveloperKey());
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(user);
        } else {
            user = create(user);
            return getUserById(user.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public User getUserById(String id, FetchStrategy... fetchStrategy) {
        return getUserById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public User getUserById(Long id, FetchStrategy... fetchStrategy) {
        return userDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly=false)
    public User create(User user) {
        return userDao.create(user);
    }

    @Override
    @Transactional(readOnly=false)
    public User update(User user) {
        return userDao.update(user);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(User user) {
        userDao.delete(user);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteUsers(List<User> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(User user : list) {
            delete(user);
        }
    }

    @Override
    public String newDeveloperKey() {
        return new String (Hex.encodeHex(DigestUtils.sha1(Integer.toString(this.random.nextInt()))));
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public User getUserByEmail(String email, FetchStrategy... fetchStrategy) {
        return userDao.getUserByEmail(email, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public User getUserByAlias(String alias, FetchStrategy... fetchStrategy) {
        return userDao.getUserByAlias(alias, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    
    @Override
    @Transactional(readOnly=true)
    public Long countAllUsersForEmailLike(String email) {
        return countAllUsersForEmailLike(email, LikeSpec.CONTAINS);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, FetchStrategy... fetchStrategy) {
        return getAllUsersForEmailLike(email, LikeSpec.CONTAINS, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllUsersForEmailLike(email, LikeSpec.CONTAINS, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllUsersForEmailLike(email, LikeSpec.CONTAINS, page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Long countAllUsersForEmailLike(String email, AdfonicUser adfonicUser) {
        return countAllUsersForEmailLike(email, adfonicUser, LikeSpec.CONTAINS);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, FetchStrategy... fetchStrategy) {
        return getAllUsersForEmailLike(email, adfonicUser, LikeSpec.CONTAINS, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, Sorting sort, FetchStrategy... fetchStrategy) {
        return getAllUsersForEmailLike(email, adfonicUser, LikeSpec.CONTAINS, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, Pagination page, FetchStrategy... fetchStrategy) {
        return getAllUsersForEmailLike(email, adfonicUser, LikeSpec.CONTAINS, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllUsersForEmailLike(String email, LikeSpec likeSpec) {
        return userDao.countAllForEmailLike(formatLikeSearchTarget(email, likeSpec));
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, LikeSpec likeSpec, FetchStrategy... fetchStrategy) {
        return userDao.getAllForEmailLike(formatLikeSearchTarget(email, likeSpec), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, LikeSpec likeSpec, Sorting sort, FetchStrategy... fetchStrategy) {
        return userDao.getAllForEmailLike(formatLikeSearchTarget(email, likeSpec), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, LikeSpec likeSpec, Pagination page, FetchStrategy... fetchStrategy) {
        return userDao.getAllForEmailLike(formatLikeSearchTarget(email, likeSpec), page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public Long countAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec) {
        return userDao.countAllForEmailLike(formatLikeSearchTarget(email, likeSpec), adfonicUser);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec, FetchStrategy... fetchStrategy) {
        return userDao.getAllForEmailLike(formatLikeSearchTarget(email, likeSpec), adfonicUser, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec, Sorting sort, FetchStrategy... fetchStrategy) {
        return userDao.getAllForEmailLike(formatLikeSearchTarget(email, likeSpec), adfonicUser, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForEmailLike(String email, AdfonicUser adfonicUser, LikeSpec likeSpec, Pagination page, FetchStrategy... fetchStrategy) {
        return userDao.getAllForEmailLike(formatLikeSearchTarget(email, likeSpec), adfonicUser, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public boolean userHasRole(User user, String roleName) {
        if (roleName == null || user == null) {
            return false;
        }
        boolean rolesLoaded = false;
        try {
            user.getRoles().size();
            rolesLoaded = true;
        } catch(Exception e) {
            //do nothing
        }
        User localUser = user;
        if(!rolesLoaded) {
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(User.class, USER_FIELD_ROLES, JoinType.LEFT);
            localUser = getUserById(user.getId(), fs);
        }
        for (Role role : localUser.getRoles()) {
            if (roleName.equals(role.getName())){
                return true;
            }
        }
        return false;
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public boolean userHasCompanyRole(User user, String roleName) {
        if (roleName == null || user == null) {
            return false;
        }
        boolean rolesLoaded = false;
        try {
            user.getCompany().getRoles().size();
            rolesLoaded = true;
        } catch(Exception e) {
          //do nothing
        }
        User localUser = user;
        if(!rolesLoaded) {
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(User.class, "company", JoinType.INNER);
            fs.addEagerlyLoadedFieldForClass(Company.class, USER_FIELD_ROLES, JoinType.LEFT);
            localUser = getUserById(user.getId(), fs);
        }
        for (Role role : localUser.getCompany().getRoles()) {
            if (roleName.equals(role.getName())){
                return true;
            }
        }
        return false;
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public boolean isAgencyUser(User user) {
         return user.getCompany().isAccountType(AccountType.AGENCY);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=false)
    public User updateLastLogin(User user) {
        user.updateLastLogin();
        return update(user);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllUserEmailsForEmailLike(String email) {
        return this.countAllUsersForEmailLike(email, LikeSpec.CONTAINS);
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> getAllUsersEmailsForEmailLike(String email) {
        return this.getAllUsersEmailsForEmailLike(email, LikeSpec.CONTAINS, new Sorting(asc(USER_FIELD_EMAIL)));
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> getAllUsersEmailsForEmailLike(String email, Sorting sort) {
        return this.getAllUsersEmailsForEmailLike(email, LikeSpec.CONTAINS, sort);
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> getAllUsersEmailsForEmailLike(String email, Pagination page) {
        Pagination localPage = page;
        if(page.getSorting() == null) {
            localPage = new Pagination(page, new Sorting(asc(USER_FIELD_EMAIL)));
        }
        return this.getAllUsersEmailsForEmailLike(email, LikeSpec.CONTAINS, localPage);
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllUserEmailsForEmailLike(String email, LikeSpec likeSpec) {
        return userDao.countAllEmailsForEmailLike(formatLikeSearchTarget(email, likeSpec));
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> getAllUsersEmailsForEmailLike(String email, LikeSpec likeSpec) {
        return getAllUsersEmailsForEmailLike(email, likeSpec, new Sorting(asc(USER_FIELD_EMAIL)));
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> getAllUsersEmailsForEmailLike(String email, LikeSpec likeSpec, Sorting sort) {
        return userDao.getAllEmailsForEmailLike(formatLikeSearchTarget(email, likeSpec), sort);
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> getAllUsersEmailsForEmailLike(String email, LikeSpec likeSpec, Pagination page) {
        Pagination localPage = page;
        if(page.getSorting() == null) {
            localPage = new Pagination(page, new Sorting(asc(USER_FIELD_EMAIL)));
        }
        return userDao.getAllEmailsForEmailLike(formatLikeSearchTarget(email, likeSpec), localPage);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public boolean isUserAuthorizedToManageAdvertiser(User user, Advertiser advertiser) {
        FetchStrategy userFs = new FetchStrategyBuilder()
                               .addInner(User_.company)
                               .addLeft(User_.roles)
                               .addLeft(User_.advertisers)
                               .build();
        FetchStrategy advertiserFs = new FetchStrategyBuilder()
                                .addInner(Advertiser_.company)
                                .build();
        User localUser = getUserById(user.getId(), userFs);
        
        AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
        Advertiser localAdvertiser = advertiserManager.getAdvertiserById(advertiser.getId(), advertiserFs);
        // First and foremost, and irrespective of agency vs. non, make sure the
        // advertiser belongs to the authenticated user's company.
        if (!localAdvertiser.getCompany().equals(localUser.getCompany())) {
            return false; // the advertiser belongs to another company
        }

        if (localUser.getCompany().isAccountType(AccountType.AGENCY)) {
            for (Role r : localUser.getRoles()) {
                if (Role.USER_ROLE_ADMINISTRATOR.equals(r.getName())) {
                    return true; // agency admin can manage any of the company's advertisers
                }
            }
            // Not an agency admin...make sure the user is associated with the advertiser
            return localUser.getAdvertisers().contains(localAdvertiser);
        } else {
            return true; // non-agency
        }
     }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllUsersForCompany(Company company) {
        return userDao.countAllForCompany(company);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForCompany(Company company, FetchStrategy ... fetchStrategy) {
        return userDao.getAllForCompany(company, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForCompany(Company company, Pagination page, FetchStrategy ... fetchStrategy) {
        return userDao.getAllForCompany(company, page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<User> getAllUsersForCompany(Company company, Sorting sort, FetchStrategy ... fetchStrategy) {
        return userDao.getAllForCompany(company, sort, fetchStrategy);
    }
    
    //------------------------------------------------------------------------------------------
    // Role
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public Role newRole(String name, RoleType type) {
        Role role = new Role(name, type);
        return create(role);
    }

    @Override
    @Transactional(readOnly=true)
    public Role getRoleById(String id) {
        return getRoleById(makeLong(id));
    }

    @Override
    @Transactional(readOnly=true)
    public Role getRoleById(Long id) {
        return roleDao.getById(id);
    }

    @Transactional(readOnly=false)
    public Role create(Role role) {
        return roleDao.create(role);
    }

    @Override
    @Transactional(readOnly=false)
    public Role update(Role role) {
        return roleDao.update(role);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(Role role) {
        roleDao.delete(role);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteRoles(List<Role> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(Role entry : list) {
            delete(entry);
        }
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Role getRoleByName(String name) {
        return roleDao.getByName(name);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllRoles(RoleType roleType) {
        return roleDao.countAllRoles(roleType);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllRoles(RoleType roleType, FetchStrategy... fetchStrategy) {
        return roleDao.getAllRoles(roleType, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllRoles(RoleType roleType, Sorting sort, FetchStrategy... fetchStrategy) {
        return roleDao.getAllRoles(roleType, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllRoles(RoleType roleType, Pagination page, FetchStrategy... fetchStrategy) {
        return roleDao.getAllRoles(roleType, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllUserRoles() {
        return this.countAllRoles(RoleType.USER);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllUserRoles(FetchStrategy... fetchStrategy) {
        return this.getAllRoles(RoleType.USER, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllUserRoles(Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(RoleType.USER, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllUserRoles(Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(RoleType.USER, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllCompanyRoles() {
        return this.countAllRoles(RoleType.COMPANY);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllCompanyRoles(FetchStrategy... fetchStrategy) {
        return this.getAllRoles(RoleType.COMPANY, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllCompanyRoles(Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(RoleType.COMPANY, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Role> getAllCompanyRoles(Pagination page, FetchStrategy... fetchStrategy) {
        return this.getAllRoles(RoleType.COMPANY, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------
    // AdminRole
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllAdminRoles() {
        return adminRoleDao.countAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<AdminRole> getAllAdminRoles() {
        return adminRoleDao.getAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<AdminRole> getAllAdminRoles(Sorting sort) {
        return adminRoleDao.getAll(sort);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AdminRole> getAllAdminRoles(Pagination page) {
        return adminRoleDao.getAll(page);
    }

    @Override
    @Transactional(readOnly=true)
    public AdminRole getAdminRoleById(String id) {
        return getAdminRoleById(makeLong(id));
    }

    @Override
    @Transactional(readOnly=true)
    public AdminRole getAdminRoleById(Long id) {
        return adminRoleDao.getById(id);
    }

    @Override
    @Transactional(readOnly=false)
    public AdminRole create(AdminRole adminRole) {
        return adminRoleDao.create(adminRole);
    }

    @Override
    @Transactional(readOnly=false)
    public AdminRole update(AdminRole adminRole) {
        return adminRoleDao.update(adminRole);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(AdminRole adminRole) {
        adminRoleDao.delete(adminRole);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAdminRoles(List<AdminRole> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(AdminRole entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public AdminRole getAdminRoleByName(String name) {
        return adminRoleDao.getByName(name);
    }

    //------------------------------------------------------------------------------------------
    // AdfonicUser
    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllAdfonicUsers() {
        return adfonicUserDao.countAll();
    }

    @Override
    @Transactional(readOnly=true)
    public List<AdfonicUser> getAllAdfonicUsers(FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AdfonicUser> getAllAdfonicUsers(Sorting sort, FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<AdfonicUser> getAllAdfonicUsers(Pagination page, FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getAll(page, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<User> getUsersForAdfonicUser(AdfonicUser adfonicUser, FetchStrategy... fetchStrategy){
        if (adfonicUser == null){
            return null;
        }
        boolean usersLoaded = false;
        try {
            adfonicUser.getUsers().size();
            usersLoaded = true;
        } catch(Exception e) {
            //do nothing
        }
        AdfonicUser localAdfonicUser = adfonicUser;
        if(!usersLoaded) {
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(AdfonicUser.class, "users", JoinType.LEFT);
            localAdfonicUser = getAdfonicUserById(adfonicUser.getId(), fs);
        }
        List<User> result = new ArrayList<User>();
        result.addAll(localAdfonicUser.getUsers());
        return result;
    }

    @Override
    @Transactional(readOnly=true)
    public AdfonicUser getAdfonicUserById(String id, FetchStrategy... fetchStrategy) {
        return getAdfonicUserById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public AdfonicUser getAdfonicUserById(Long id, FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public AdfonicUser create(AdfonicUser adfonicUser) {
        return adfonicUserDao.create(adfonicUser);
    }

    @Override
    @Transactional(readOnly=false)
    public AdfonicUser update(AdfonicUser adfonicUser) {
        return adfonicUserDao.update(adfonicUser);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(AdfonicUser adfonicUser) {
        adfonicUserDao.delete(adfonicUser);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAdfonicUsers(List<AdfonicUser> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(AdfonicUser user : list) {
            delete(user);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public AdfonicUser getAdfonicUserByEmail(String emailAddress, FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getByEmail(emailAddress, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public AdfonicUser getAdfonicUserByLoginName(String loginName, FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getByLoginName(loginName, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean adfonicUserHasAdminRole(AdfonicUser user, String roleName) {
        if (roleName == null || user == null){
            return false;
        }
        
        boolean rolesLoaded = false;
        try {
            user.getRoles().size();
            rolesLoaded = true;
        } catch(Exception e) {
            //do nothing
        }
        AdfonicUser localAdfonicUser = user;
        if(!rolesLoaded) {
            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(AdfonicUser.class, USER_FIELD_ROLES, JoinType.LEFT);
            localAdfonicUser = getAdfonicUserById(user.getId(), fs);
        }
        for (AdminRole role : localAdfonicUser.getRoles()) {
            if (roleName.equals(role.getName())){
                return true;
            }
        }
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AdfonicUser> getAllAdfonicUsers(AdfonicUserFilter filter, FetchStrategy... fetchStrategy) {
        return adfonicUserDao.getAll(filter, fetchStrategy);
    }
}
