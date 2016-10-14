package com.adfonic.beans;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.byyd.middleware.utils.TransactionalRunner;

@ManagedBean
@RequestScoped
public class AccountVerificationCodesBean extends BaseBean {

    private User user;
    private List<VerificationCode> userVerificationCodes;

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

    public List<VerificationCode> getUserVerificationCodes() {
        if (userVerificationCodes == null) {
            userVerificationCodes = getAccountManager().getAllVerificationCodesForUser(user);
        }
        return userVerificationCodes;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

}
