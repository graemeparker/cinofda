package com.adfonic.tools.beans.manageusers;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.User;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.user.UserService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "reactivateUsers", pattern = "/agencyconsole/reactivateuser", viewId = "/WEB-INF/jsf/manageusers/reactivateuser.jsf") })
public class ReactivateUserMBean extends GenericAbstractBean implements Serializable {
    /**
     *
     */

    private static final long serialVersionUID = 4188436945608530254L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactivateUserMBean.class);

    @Autowired
    private UserService uService;

    private UserDTO selectedUser;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "reactivateUsers") })
    public void init() throws Exception {
        LOGGER.debug("init-->");

        LOGGER.debug("init<--");
    }

    public String reactivateSelectedUser() {
        LOGGER.debug("reactivateSelectedUser-->");

        uService.changeStatus(selectedUser.getId(), User.Status.UNVERIFIED);

        LOGGER.debug("reactivateSelectedUser<--");
        return "pretty:dashboard-agency";

    }

    public UserDTO getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(UserDTO selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List<UserDTO> getUsers() {
        UserDTO user = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        return uService.getRemovedUsersForUser(user.getCompany().getId(), user.getId());
    }
}
