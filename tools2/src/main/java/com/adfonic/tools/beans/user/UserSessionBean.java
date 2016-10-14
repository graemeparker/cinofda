package com.adfonic.tools.beans.user;

import static com.adfonic.presentation.login.LoginService.USER_TYPE_AGENCY;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.DynamicMenuModel;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Role;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.company.CompanyDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.tools.beans.accountsettings.AccountDetailsMBean;
import com.adfonic.tools.beans.navigation.NavigationMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.security.SecurityUtils;

@Component
@Scope("session")
public class UserSessionBean extends GenericAbstractBean implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDetailsMBean.class);
    private static final long serialVersionUID = 1L;
    
    private static final String USERTYPE_ADVERTISER = "advertiser";
    private static final String USERTYPE_PUBLISHER = "publisher";
    private static final String MAP_KEY_USER_DTO = "userDto";
    
    @Autowired 
    private CompanyService cService;
    
    @Autowired
    private NavigationMBean navigationBean;
    
    private Map<String, Object> map = new HashMap<String, Object>(0);
        
    private MenuModel advModel;

    public UserSessionBean() {
        super();
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    protected void init() {
        //do nothing
    }
    
     public MenuModel getAdvModel() {       
        advModel = new DynamicMenuModel();
        
        //adding the first advertisers link
        DefaultMenuItem item = new DefaultMenuItem(); 
        item.setId("advertiserMenuOptionId");
        item.setValue(FacesUtils.getBundleMessage("page.header.label.link.agencyconsole"));  
        item.setUrl("/");
        item.setStyleClass("current");
            
        //to reload the page completly and update all the components.
        advModel.addElement(item);
        
        if(getUser().getAdvertiserDto()!=null){
            DefaultSubMenu submenu = new DefaultSubMenu();  
            submenu.setId("advertiserSubmenuOptionsId");
            //the name of this ones is the current advertisers name.
            String advertisersName=getUser().getAdvertiserDto().getName();
            if(StringUtils.isEmpty(advertisersName)){
                advertisersName="advertiser_"+getUser().getAdvertiserDto().getId().longValue();
            }
            submenu.setLabel(advertisersName);
            advModel.addElement(submenu);
        }
        return advModel;
    }

    public void setAdvModel(MenuModel advModel) {
        this.advModel = advModel;
    }
    
    public String doRefreshAdvertiser() {
        return "pretty:dashboard-advertiser" ;
    }
    
    public void doLoadAdvertiser(ActionEvent event) {
        if (event instanceof MenuActionEvent){
            MenuItem menuItem = ((MenuActionEvent) event).getMenuItem();
            Long advId = Long.parseLong(menuItem.getParams().get("advId").get(0));
            Iterator<AdvertiserDto> it =  getUser().getAdvertiserListDto().iterator();
            while(it.hasNext()) {
                AdvertiserDto adv = it.next();
                if(adv.getId().longValue()==advId.longValue()){
                    getUser().setAdvertiserDto(adv);
                    break;
                }
            }
        }
    }
    
    public boolean isPublisher(){
        for(String userType : ((UserDTO)map.get(MAP_KEY_USER_DTO)).getUserTypes()){
            if(USERTYPE_PUBLISHER.equals(userType)){
                return true;
            }
        }
        return false;
    }
    
    public boolean isAdvertiser(){
        for(String userType : ((UserDTO)map.get(MAP_KEY_USER_DTO)).getUserTypes()){
            if(userType.equals(USERTYPE_ADVERTISER)){
                return true;
            }
        }
        return false;
    }
    
    public String switchToPublisher(){
        ((UserDTO)map.get(MAP_KEY_USER_DTO)).setUserType(USERTYPE_PUBLISHER);
        return "pretty:dashboard-redirect";
    }
    
    public String switchToAdvertiser(){
        ((UserDTO)map.get(MAP_KEY_USER_DTO)).setUserType(USERTYPE_ADVERTISER);
        return "pretty:dashboard-redirect";
    }
    
    public boolean isReadOnlyUser(){
        List<String> roles = new ArrayList<String>();
        roles.add(Role.COMPANY_ROLE_DSP_READ_ONLY);
        boolean isReadOnly = SecurityUtils.hasUserRoles(roles); 
        roles= new ArrayList<>();
        roles.add(Constants.LOGGED_IN_AS_ADMIN_ROLE);
        boolean isAdmin = SecurityUtils.hasUserRoles(roles); 
        return isReadOnly && !isAdmin;
    }
    
    public boolean isAdminAgency(){
        List<String> roles = new ArrayList<String>();
        roles.add(Role.USER_ROLE_ADMINISTRATOR);
        return SecurityUtils.hasUserRoles(roles);
    }
    
    public boolean hasTechFee(){
        UserDTO userDto = (UserDTO) this.map.get(Constants.USERDTO);
        return cService.hasTechFee(userDto);
    }
    
    /*
     * Note this doesn't distinguish between admin or not this is used
     * for tests for navigation
     */
    public boolean isAgencyUser() {
        return ((UserDTO)map.get(MAP_KEY_USER_DTO)).getUserTypes().contains(USER_TYPE_AGENCY);
    }
    
    public void updateSessionInfo(String firstName,
                                  String lastname, 
                                  CountryDto country, 
                                  String phone,
                                  String companyName, 
                                  String taxCode, 
                                  String timezone,
                                  boolean invoiceDateInGMT, 
                                  List<String> accountTypes) {
        UserDTO userDTOSession = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        userDTOSession.setFirstName(firstName);
        userDTOSession.setLastName(lastname);
        userDTOSession.setCountry(country);
        userDTOSession.setPhoneNumber(phone);
        List<String> userTypes = new ArrayList<String>();
        for (String type : accountTypes){
            userTypes.add(type.toLowerCase());
        }
        userDTOSession.setUserTypes(userTypes);
        CompanyDto companyDTO = userDTOSession.getCompany();
        companyDTO.setName(companyName);
        companyDTO.setTaxCode(taxCode);
        companyDTO.setDefaultTimeZoneId(timezone);
        companyDTO.setInvoiceDateInGMT(invoiceDateInGMT);
        companyDTO.setAccountTypes(accountTypes);
    }
    
    public void redirectToAdmin(){
        SecurityUtils.cleanAuthenticationSecurityInfo();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.redirect(navigationBean.getAdminLink());
        } catch (IOException e) {
            LOGGER.error("Error trying redirect to Admin " + navigationBean.getAdminLink());
        }
        getUserSessionBean().getMap().remove(Constants.USERDTO);
    }
}
