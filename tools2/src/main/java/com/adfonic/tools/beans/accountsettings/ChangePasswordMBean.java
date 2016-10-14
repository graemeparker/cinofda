package com.adfonic.tools.beans.accountsettings;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;

import javax.faces.application.FacesMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.user.UserService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "changePassword", pattern = "/changePassword", viewId = "/WEB-INF/jsf/accountsettings/changepassword.jsf") })
public class ChangePasswordMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 4188436945608530254L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePasswordMBean.class);

    private static final int PASSWORD_MIN_LENGHT = 6;
    private static final int PASSWORD_MAX_LENGHT = 32;

    @Autowired
    private UserService uService;

    private String pwd;
    private String pwdRetype;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "changePassword") })
    public void init() throws Exception {
        LOGGER.debug("init-->");
        LOGGER.debug("init<--");
    }

    public void doSave() {
        // Setting password
        if (isPasswordValid()) {
            UserDTO userDTO = getUser();
            userDTO.setPassword(this.pwd);
            uService.saveUser(userDTO);
            addFacesMessage(FacesMessage.SEVERITY_INFO, "save-button", null, "page.account.settings.changepwd.success");
        }
    }

    private boolean isPasswordValid() {
        boolean isValid = true;

        if ((this.pwd.length() < PASSWORD_MIN_LENGHT) || (this.pwd.length() > PASSWORD_MAX_LENGHT)) {
            LOGGER.debug("Invalid password lenght");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "password-input", null, "page.account.settings.changepwd.validation.error.length");
            isValid = false;
        }

        if (!this.pwd.equals(this.pwdRetype)) {
            LOGGER.debug("Passwords mismatch");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "password-retype-input", null,
                    "page.account.settings.changepwd.validation.error.match");
            isValid = false;
        }

        return isValid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwdRetype() {
        return pwdRetype;
    }

    public void setPwdRetype(String pwdRetype) {
        this.pwdRetype = pwdRetype;
    }
}
