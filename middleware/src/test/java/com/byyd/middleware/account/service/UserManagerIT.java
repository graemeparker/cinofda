package com.byyd.middleware.account.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Company;
import com.adfonic.domain.Company_;
import com.adfonic.domain.Role;
import com.adfonic.domain.Role.RoleType;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class UserManagerIT {
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private CompanyManager companyManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetUserByIdWithInvalidId() {
        assertNull(userManager.getUserById(0L));
    }

    @Test
    public void testUser() {
        User user = null;
        try {
            Company company = companyManager.getCompanyById(2L);
            String firstName = "Carl";
            String lastName = "Sagan";
            String email = "carl.sagan@jpl.com";
            String password = "palebluedot";
            String alias = "CarlSagan";

            user = userManager.newUser(company, firstName, lastName, email, password, alias);
            assertNotNull(user);
            long id = user.getId();
            assertTrue(id > 0);
            assertEquals(company, user.getCompany());
            assertEquals(firstName, user.getFirstName());
            assertEquals(lastName, user.getLastName());
            assertEquals(email, user.getEmail());
            assertTrue(user.checkPassword(password));
            
            assertTrue(userManager.getAllUsersForCompany(company).contains(user));

            user = userManager.getUserById(id);
            assertNotNull(user);
            assertEquals(user.getId(), id);

            user = userManager.getUserById(Long.toString(id));
            assertNotNull(user);
            assertEquals(user.getId(), id);

            assertEquals(user, userManager.getUserByEmail(email));

            assertEquals(user, userManager.getUserByAlias(alias));

            String newEmail = "carl.sagan@nasa.gov";
            user.setEmail(newEmail);
            user = userManager.update(user);
            user = userManager.getUserById(user.getId());
            assertEquals(newEmail, user.getEmail());

            Long count = userManager.countAllUsersForEmailLike(newEmail, LikeSpec.CONTAINS);
            assertTrue(count > 0L);
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            if(user != null) {
                userManager.delete(user);
                assertNull(userManager.getUserById(user.getId()));
            }
        }
    }

    @Test
    public void testIsAgencyUser() {
        User user = userManager.getUserById(8635L);
        try {
            assertTrue(userManager.isAgencyUser(user));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }

    }

    @Test
    public void testUpdateLastLogin() {
        User user = userManager.getUserById(8635L);
        try {
            Date originalLastLogin = user.getLastLogin();
            if (originalLastLogin == null) {
                originalLastLogin = new Date();
                // The update may take sub-millisecond, especially if you're
                // on one of these bitchin fast Macs.  So make sure we actually
                // wait until "now" is no longer current before doing the update.
                Thread.sleep(100);
            }
            user = userManager.updateLastLogin(user);
            // make sure the last login is after the date we snagged
            assertTrue(user.getLastLogin().after(originalLastLogin));
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

    @Test
    public void testUserEmails() {
        String email = "adfonic";

        long userCount = userManager.countAllUsersForEmailLike(email);
        assertTrue(userCount > 0);

        List<User> users = userManager.getAllUsersForEmailLike(email);
        assertTrue(users.size() > 0);

        for(User user : users) {
            System.out.println(user.getFullName() + " " + user.getEmail());
        }

        long emailCount = userManager.countAllUserEmailsForEmailLike(email);
           assertTrue(emailCount > 0);
           assertTrue(userCount == emailCount);

           List<String> emails = userManager.getAllUsersEmailsForEmailLike(email);
           for(String e : emails) {
               System.out.println(e);
           }
    }

     //----------------------------------------------------------------------------------------------------------------

     @Test
     public void testGetRoleByIdWithInvalidId() {
         assertNull(userManager.getRoleById(0L));
     }

     @Test
     public void testRole() {
         String roleCompanyName = "CompanyTesting" + System.currentTimeMillis();
         Role roleCompany = null;
         String roleUserName = "UserTesting" + System.currentTimeMillis();
         Role roleUser = null;
         try {
             roleCompany = userManager.newRole(roleCompanyName,
                     RoleType.COMPANY);
             roleUser = userManager.newRole(roleUserName, RoleType.USER);

             assertNotNull(roleCompany);
             assertNotNull(roleUser);

             long companyRoleId = roleCompany.getId();
             long userRoleId = roleUser.getId();

             assertTrue(companyRoleId > 0L);
             assertTrue(userRoleId > 0L);

             assertEquals(roleCompany, userManager.getRoleById(companyRoleId));
             assertEquals(roleCompany,
                     userManager.getRoleById(Long.toString(companyRoleId)));
             assertEquals(roleUser, userManager.getRoleById(userRoleId));
             assertEquals(roleUser,
                     userManager.getRoleById(Long.toString(userRoleId)));

             assertEquals(roleCompany,
                     userManager.getRoleByName(roleCompanyName));
             assertEquals(roleUser, userManager.getRoleByName(roleUserName));

             String newName = roleUserName + " changed";
             roleUser.setName(newName);
             roleUser = userManager.update(roleUser);
             roleUser = userManager.getRoleById(roleUser.getId());
             assertEquals(newName, roleUser.getName());

             List<Role> companyRoles = userManager.getAllCompanyRoles();
             assertNotNull(companyRoles);
             assertTrue(companyRoles.contains(roleCompany));

             List<Role> userRoles = userManager.getAllUserRoles();
             assertNotNull(userRoles);
             assertTrue(userRoles.contains(roleUser));

         } catch (Exception e) {
             String stackTrace = ExceptionUtils.getStackTrace(e);
             System.out.println(stackTrace);
             fail(stackTrace);
         } finally {
             userManager.delete(roleCompany);
             assertNull(userManager.getRoleById(roleCompany.getId()));
             userManager.delete(roleUser);
             assertNull(userManager.getRoleById(roleUser.getId()));
         }
    }

     @Test
     public void testUserHasCompanyRole() {
         FetchStrategy fs = new FetchStrategyBuilder()
                                     .addInner(User_.company)
                                     .addLeft(Company_.roles)
                                     .build();
         User user = userManager.getUserById(1L, fs);
         Company company = user.getCompany();
         String roleCompanyName = "CompanyTesting" + System.currentTimeMillis();
         Role roleCompany = null;
         try {
             roleCompany = userManager.newRole(roleCompanyName, RoleType.COMPANY);
             company.getRoles().add(roleCompany);
             company = companyManager.update(company);

             user = userManager.getUserById(1L, fs);

             assertTrue(userManager.userHasCompanyRole(user, roleCompanyName));

             company.getRoles().remove(roleCompany);
             company = companyManager.update(company);

             user = userManager.getUserById(1L, fs);

             assertFalse(userManager.userHasCompanyRole(user, roleCompanyName));
         } catch (Exception e) {
             String stackTrace = ExceptionUtils.getStackTrace(e);
             System.out.println(stackTrace);
             fail(stackTrace);
         } finally {
             userManager.delete(roleCompany);
             assertNull(userManager.getRoleById(roleCompany.getId()));
         }

     }

     @Test
     public void testUserHasRole() {
         FetchStrategy fs = new FetchStrategyBuilder()
                                     .addLeft(User_.roles)
                                     .build();
         User user = userManager.getUserById(1L, fs);
         String roleUserName = "UserTesting" + System.currentTimeMillis();
         Role userCompany = null;
         try {
             userCompany = userManager.newRole(roleUserName, RoleType.USER);
             user.getRoles().add(userCompany);
             user = userManager.update(user);

             user = userManager.getUserById(1L, fs);

             assertTrue(userManager.userHasRole(user, roleUserName));

             user.getRoles().remove(userCompany);
             user = userManager.update(user);

             user = userManager.getUserById(1L, fs);

             assertFalse(userManager.userHasRole(user, roleUserName));
         } catch (Exception e) {
             String stackTrace = ExceptionUtils.getStackTrace(e);
             System.out.println(stackTrace);
             fail(stackTrace);
         } finally {
             userManager.delete(userCompany);
             assertNull(userManager.getRoleById(userCompany.getId()));
         }

     }
}
