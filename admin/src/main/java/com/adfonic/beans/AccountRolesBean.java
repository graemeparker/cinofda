package com.adfonic.beans;

import static com.adfonic.domain.Role.USER_ROLE_USER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import com.adfonic.domain.Role;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.utils.TransactionalRunner;

@ManagedBean
@ViewScoped
public class AccountRolesBean extends BaseBean {

    private User user;
    private List<Role> userRoles;
    private List<SelectItem> roles;

    @ManagedProperty(value = "#{adminAccountBean}")
    private AdminAccountBean adminAccountBean;

    @PostConstruct
    private void init() {
        if(isRestrictedUser()){
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext(); 
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex){
                throw new AdminGeneralException("Internal error");
            }
        }
        if (adminAccountBean == null || adminAccountBean.getUser() == null) {
            // this shouldn't happen as the view triggers a check
            throw new AdminGeneralException("no user loaded");
        }
        this.user = adminAccountBean.getUser();

        TransactionalRunner runner = getTransactionalRunner();
        runner.runTransactional(
                new Runnable() {
                    public void run() {
                        load();
                    }
                }
        );
    }

    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }

    public void setUserRoles(List<Role> userRoles) {
        this.userRoles = userRoles;
    }

    public List<Role> getUserRoles() {
        return userRoles;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void doCancelRole() {
        userRoles = null;
    }

    public void doSaveRoles(){
        
        boolean valid = false;
        if (userRoles != null) {
            for (Role r : userRoles) {
                if (r.getName().equals(USER_ROLE_USER)) {
                    valid = true;
                    break;
                }
            }
        }

        if (!valid) {
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.addMessage("detailForm:rolesList",
                    messageForId("error.adminAccountRoles.userRoleRequired"));
            return;
        }
        else {
            try {
                TransactionalRunner runner = getTransactionalRunner();
                user = runner.callTransactional(
                        new Callable<User>() {
                            public User call() throws Exception {
                                return updateAccountRoles(
                                        user,
                                        userRoles
                                );
                            }
                        }
                );
                setRequestFlag("didUpdateRoles");
            } catch (Exception e) {
                logger.log(
                        Level.SEVERE,
                        "Error saving account roles for user id=" +
                                user.getId(),
                        e);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
        logger.log(Level.FINE,"Loading roles for userId: " + user.getId());
        roles = getAllUserRoles();
        userRoles = new ArrayList<Role>(user.getRoles());
    }

    public User updateAccountRoles(User user, List<Role> userRoles) {
        user.getRoles().clear();
        user.getRoles().addAll(userRoles);
        user = getUserManager().update(user);
        setRequestFlag("didUpdate");

        return user;
    }

    public List<SelectItem> getAllUserRoles(){
        roles = new ArrayList<SelectItem>();
        for (Role r : getUserManager().getAllRoles(Role.RoleType.USER, new Sorting(SortOrder.asc("name")))) {
            roles.add(new SelectItem(r, r.getName()));
        }
        return roles;
    }

}
