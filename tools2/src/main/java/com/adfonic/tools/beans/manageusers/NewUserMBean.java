package com.adfonic.tools.beans.manageusers;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Role;
import com.adfonic.dto.user.RoleDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.user.UserService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "newUser", pattern = "/agencyconsole/newuser", viewId = "/WEB-INF/jsf/manageusers/adduser.jsf") })
public class NewUserMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(NewUserMBean.class);

    @Autowired
    private UserService uService;

    private String name;

    private String lastName;

    private String emailAddress;

    private String password;

    private String retypePassword;

    private boolean isAdmin;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "newUser") })
    public void init() throws Exception {
        LOGGER.debug("init-->");

        LOGGER.debug("init<--");
    }

    public String doSave() throws Exception {
        LOGGER.debug("doSave-->");
        UserDTO creator = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);

        UserDTO user = new UserDTO();
        user.setEmail(emailAddress);
        user.setFirstName(name);
        user.setLastName(lastName);
        user.setPassword(password);

        if (!Utils.isValidEmailAddress(emailAddress)) {
            LOGGER.debug("Invalid email address");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "email-address", null, "error.register.email.invalid");
            return null;
        }

        UserDTO u = uService.getUserByEmail(user.getEmail());
        if (u != null) {
            LOGGER.debug("Duplicated email");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "email-address", null, "error.adfonicUser.email.duplicate");
            return null;
        }

        if (StringUtils.isBlank(password) || !retypePassword.equals(password)) {
            LOGGER.debug("Different passwords");
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "retype-password", null, "error.adfonicUser.password");
            return null;
        }

        final List<RoleDto> roles = new ArrayList<RoleDto>();
        if (isAdmin) {
            roles.add(uService.getRoleByName(Role.USER_ROLE_USER));
            roles.add(uService.getRoleByName(Role.USER_ROLE_ADMINISTRATOR));
            roles.add(uService.getRoleByName(Role.USER_ROLE_AGENCY));
        } else {
            roles.add(uService.getRoleByName(Role.USER_ROLE_USER));
            roles.add(uService.getRoleByName(Role.USER_ROLE_AGENCY));
        }

        uService.createUser(user, creator.getCompany().getId(), roles);
        LOGGER.debug("doSave<--");
        return "pretty:dashboard-agency";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

}
