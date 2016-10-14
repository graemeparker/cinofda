package com.adfonic.tools.beans.manageusers;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
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
@URLMappings(mappings = { @URLMapping(id = "resetPassword", pattern = "/agencyconsole/resetpassword", viewId = "/WEB-INF/jsf/manageusers/resetpassword.jsf") })
public class ResetPasswordMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveUserMBean.class);

    @Autowired
    private UserService uService;

    private UserDTO selectedUser;

    private String password;

    private String retypePassword;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "resetPassword") })
    public void init() throws Exception {
        LOGGER.debug("init-->");

        LOGGER.debug("init<--");
    }

    public UserDTO getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(UserDTO selectedUser) {
        this.selectedUser = selectedUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRetypePassword() {
        return retypePassword;
    }

    public void setRetypePassword(String retypePassword) {
        this.retypePassword = retypePassword;
    }

    public String resetPassword() {
        LOGGER.debug("resetPassword-->");
        if (StringUtils.isBlank(password) || !retypePassword.equals(password)) {
            LOGGER.debug("Different passwords");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "retype-password", null, "error.adfonicUser.password");
            return null;
        }

        selectedUser.setPassword(password);
        uService.saveUser(selectedUser);

        LOGGER.debug("resetPassword<--");
        return "pretty:dashboard-agency";
    }
}
