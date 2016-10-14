package com.adfonic.beans;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.adfonic.domain.User;
import com.adfonic.domain.User.Status;
import com.byyd.middleware.utils.TransactionalRunner;

@ViewScoped
@ManagedBean
public class UserStatusBean extends BaseBean {
    
	@ManagedProperty(value = "#{adminAccountBean}")
    AdminAccountBean adminAccountBean;
 
    private User user;
    private Status userStatus;
    
    @PostConstruct
    public void init() {
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
        
        this.userStatus = user.getStatus();
    }
    
    public UserStatusBean(){
    	//
    }

    public void doSave() {
        if (userStatus != null && !userStatus.equals(user.getStatus())) {
            user.setStatus(userStatus);
            
            try {
                TransactionalRunner runner = getTransactionalRunner();
                user = runner.callTransactional(
                            new Callable<User>() {
                                public User call() throws Exception {
                                    return updateUser(
                                            user);
                                }
                            }
                        );
                setRequestFlag("didUpdate");
            } catch (Exception e) {
                logger.log(
                        Level.SEVERE,
                        "Error saving user status for user item id=" +
                        user.getId(),
                        e);
            }            
        }
    }

    public void setUserStatus(Status userStatus) {
        this.userStatus = userStatus;
    }
    
    public Status getUserStatus() {
        return this.userStatus;
    }
    
    public void setAdminAccountBean(AdminAccountBean adminAccountBean) {
        this.adminAccountBean = adminAccountBean;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        user = getUserManager().getUserById(user.getId());
    }
    
    public User updateUser(
            User user) {
        return getUserManager().update(user);
    }

}