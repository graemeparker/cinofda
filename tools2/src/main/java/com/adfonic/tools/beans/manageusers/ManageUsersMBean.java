package com.adfonic.tools.beans.manageusers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.faces.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Role;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.user.RoleDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.user.UserService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Component
@Scope("view")
@URLMappings(mappings = { @URLMapping(id = "manageUsers", pattern = "/agencyconsole/manageusers", viewId = "/WEB-INF/jsf/manageusers/manageusers.jsf") })
public class ManageUsersMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 4188436945608530254L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageUsersMBean.class);

    @Autowired
    private UserService uService;
    @Autowired
    private CompanyService cService;

    private UserDTO selectedUser;

    private List<AdvertiserDto> lAccounts = new ArrayList<AdvertiserDto>();

    private AdvertiserDto selectedAccount;
    
    private boolean isAdmin;
    private boolean isAdminOldValue;

    @Override
    @URLActions(actions = { @URLAction(mappingId = "manageUsers") })
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

    public List<AdvertiserDto> getlAccounts() {
        return lAccounts;
    }

    public void setlAccounts(List<AdvertiserDto> lAccounts) {
        this.lAccounts = lAccounts;
    }

    public AdvertiserDto getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(AdvertiserDto selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    public boolean isAccountEmpty() {
        return selectedAccount == null || selectedAccount.getId() == null;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public List<UserDTO> getUsers() {
        UserDTO user = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        return uService.getActiveUsersForUser(user.getCompany().getId(), user.getId());
    }
    

    public void loadUser(ActionEvent event) {
        LOGGER.debug("loadUser-->");
        lAccounts.clear();
        isAdmin=false;
        isAdminOldValue =false;
        
        lAccounts = cService.getAdvertisersForUser(selectedUser.getId());
        isAdmin = isAdminOldValue = uService.isAdminUser(selectedUser.getId());
        
        LOGGER.debug("loadUser<--");
    }

    public void addAccount(ActionEvent event) {
        LOGGER.debug("loadUser-->");
        if (!lAccounts.contains(selectedAccount)) {
            lAccounts.add(selectedAccount);
        }
        selectedAccount = null;
        LOGGER.debug("loadUser<--");
    }

    public void clearSelections(ActionEvent event) {
        LOGGER.debug("clearSelections-->");
        lAccounts.clear();
        LOGGER.debug("clearSelections<--");
    }

    public void removeAccount(ActionEvent event) {
        long id = Long.valueOf((Long) event.getComponent().getAttributes().get("accountId"));
        for (AdvertiserDto ad : lAccounts) {
            if (ad.getId() == id) {
                lAccounts.remove(ad);
                break;
            }
        }
    }

    public Collection<AdvertiserDto> complete(String query) {
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        return cService.doQuery(query, userDto.getCompany().getId());
    }

    public String doSave() {
        // Updating accounts
        uService.updateAdvertisersList(selectedUser.getId(), lAccounts);
        
        //Updating admin role
        if (this.isAdmin != this.isAdminOldValue){
            RoleDto adminRole = uService.getRoleByName(Role.USER_ROLE_ADMINISTRATOR);
            Set<RoleDto> roles = uService.getRoles(selectedUser.getId());
            if (isAdmin){
                roles.add(adminRole);
            }else{
                roles.remove(adminRole);
            }
            uService.updateRoles(selectedUser.getId(), roles);
        }
        
        //clear values
        this.lAccounts.clear();
        this.isAdmin=false;
        this.isAdminOldValue=false;

        return "pretty:dashboard-agency";
    }
}
