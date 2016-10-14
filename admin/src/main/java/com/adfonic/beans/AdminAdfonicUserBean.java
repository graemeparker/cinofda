package com.adfonic.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.AdminRole;
import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.utils.TransactionalRunner;

@SessionScoped
@ManagedBean
public class AdminAdfonicUserBean extends BaseBean {
    public static final String SUMMARY_VIEW = "adfonicUserSummary";
    public static final String DETAIL_VIEW = "adfonicUser";

    // holds a reference for edit/delete etc
    private AdfonicUser adminAdfonicUser;
    private boolean editMode = false;

    String password;
    String passwordRetype;

    private Collection<AdfonicUser> users;
    
    private List<AdminRole> adminRoles;
       
    private String userSearch;
    private List<User> usersFound;
    private List<User> usersToAdd;
    private List<User> usersToDelete;
    
    private List<User> filteredUsers;
    
    @PostConstruct
    private void init() {
        TransactionalRunner runner = getTransactionalRunner();
        runner.runTransactional(
                new Runnable() {
                    public void run() {
                        load();
                    }
                }
        );
        if(isRestrictedUser()){
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext(); 
                ec.redirect(ec.getRequestContextPath() + "/admin/account.jsf");
                return;
            } catch (IOException ex){
                throw new AdminGeneralException("Internal error");
            }
        }
    }
    
    public Collection<AdfonicUser> getUsers() {
        if(users==null){
            init();
        }
        return this.users;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return this.editMode;
    }

    public String doCancel() {
        adminAdfonicUser = null;
        setEditMode(false);
        return SUMMARY_VIEW;
    }

    public String doEdit() {
        if (!editMode) {
            this.adminAdfonicUser = new AdfonicUser();
        }
        return DETAIL_VIEW;
    }

    public String doSave() {
        if (editMode) {
            // we only care about existing edits if password is non-blank
            if (!StringUtils.isBlank(password)) {
                if (!passwordRetype.equals(password)) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    fc.addMessage("mainForm:password",
                            messageForId("error.adfonicUser.password"));
                    password = passwordRetype = null;
                    return null;
                }
                adminAdfonicUser.setPassword(password);
            }
            //If no restricted admin, make sure assigned user accounts is empty
            if(!isRestrictedAdmin()){
                adminAdfonicUser.getUsers().clear();
            }

            getUserManager().update(adminAdfonicUser);
        }
        else { // new user mode
            // check loginName
            AdfonicUser au = getUserManager().getAdfonicUserByLoginName(this.adminAdfonicUser.getLoginName());

            // check login name in db
            if (au != null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:loginName",
                        messageForId("error.adfonicUser.loginName.duplicate"));
                return null;
            }

            // check email in db
            au = getUserManager().getAdfonicUserByEmail(this.adminAdfonicUser.getEmail());
            if (au != null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:email",
                        messageForId("error.adfonicUser.email.duplicate"));
                return null;
            }

            if (StringUtils.isBlank(password) || !passwordRetype.equals(password)) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage("mainForm:password",
                        messageForId("error.adfonicUser.password"));
                password = passwordRetype = null;
                return null;
            }
            adminAdfonicUser.setPassword(password);
            //If no restricted admin, make sure assigned user accounts is empty
            if(!isRestrictedAdmin()){
                adminAdfonicUser.getUsers().clear();
            }
            getUserManager().create(adminAdfonicUser);
        }
        password = passwordRetype = null;

        setRequestFlag("didUpdate");
        adminAdfonicUser = null;
        users = null;
        usersFound = new ArrayList<User>();
        userSearch = "";
        return SUMMARY_VIEW;
    }
    
    public void search(ActionEvent event){
        if(userSearch.length()>2){
            this.usersFound = getUserManager().getAllUsersForEmailLike(userSearch);
        }
        else{
            this.usersFound = new ArrayList<User>();
        }
        //Don't show in the search box the users already added to the account
        List<User> repeatedUsers = new ArrayList<User>();
        for(User u : usersFound){
            if(this.adminAdfonicUser.getUsers().contains(u)){
                repeatedUsers.add(u);
            }
        }
        if(!CollectionUtils.isEmpty(repeatedUsers)){
            usersFound.removeAll(repeatedUsers);
        }
    }
    
    public void addAll(ActionEvent event){
        if(!CollectionUtils.isEmpty(this.usersFound)){
            for(User u : usersFound){
                if(!this.adminAdfonicUser.getUsers().contains(u)){
                    this.adminAdfonicUser.getUsers().add(u);
                }
            }
            if(this.usersFound!=null){
                this.usersFound.clear();
            }
            if(this.usersToAdd!=null){
                this.usersToAdd.clear();
            }
            if(this.usersToDelete!=null){
                this.usersToDelete.clear();
            }
        }
    }
    
    public void add(ActionEvent event){
        if(!CollectionUtils.isEmpty(this.usersToAdd)){
            for(User u : usersToAdd){
                if(!this.adminAdfonicUser.getUsers().contains(u)){
                    this.adminAdfonicUser.getUsers().add(u);
                    this.usersFound.remove(u);
                }
            }
            if(this.usersToAdd!=null){
                this.usersToAdd.clear();
            }
            if(this.usersToDelete!=null){
                this.usersToDelete.clear();
            }
        }
    }
    
    public void remove(ActionEvent event){
        if(!CollectionUtils.isEmpty(this.usersToDelete)){
            for(User u : usersToDelete){
                this.adminAdfonicUser.getUsers().remove(u);
                if(this.userSearch.length()>2 && u.getEmail().contains(this.userSearch) && !usersFound.contains(u)){
                    usersFound.add(u);
                }
            }
            if(this.usersToAdd!=null){
                this.usersToAdd.clear();
            }
            if(this.usersToDelete!=null){
                this.usersToDelete.clear();
            }
        }
    }
    
    public void removeAll(ActionEvent event){
        for(User u : this.adminAdfonicUser.getUsers()){
            if(this.userSearch.length()>2 && u.getEmail().contains(this.userSearch) && !usersFound.contains(u)){
                usersFound.add(u);
            }
        }
        adminAdfonicUser.getUsers().clear();
        this.usersToAdd.clear();
        this.usersToDelete.clear();
    }

    public AdfonicUser getAdminAdfonicUser() {
        return adminAdfonicUser;
    }

    public void setAdminAdfonicUser(AdfonicUser adminAdfonicUser) {
        this.adminAdfonicUser = adminAdfonicUser;
    }

    public List<SelectItem> getStatusItems() {
        List<SelectItem> statuses = new ArrayList<SelectItem>();
        for (AdfonicUser.Status s : AdfonicUser.Status.values()) {
            statuses.add(new SelectItem(s));
        }
        return statuses;
    }

    public String getPasswordRetype() {
        return passwordRetype;
    }

    public void setPasswordRetype(String passwordRetype) {
        this.passwordRetype = passwordRetype;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<AdminRole> getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(List<AdminRole> adminRoles) {
        this.adminRoles = adminRoles;
    }

    public void setUsers(Collection<AdfonicUser> users) {
        this.users = users;
    }

    public String getUserSearch() {
        return userSearch;
    }

    public void setUserSearch(String userSearch) {
        this.userSearch = userSearch;
    }

    public List<User> getUsersFound() {
        return usersFound;
    }

    public void setUsersFound(List<User> usersFound) {
        this.usersFound = usersFound;
    }

    public List<User> getUsersToAdd() {
        return usersToAdd;
    }

    public void setUsersToAdd(List<User> usersToAdd) {
        this.usersToAdd = usersToAdd;
    }

    public List<User> getUsersToDelete() {
        return usersToDelete;
    }

    public void setUsersToDelete(List<User> usersToDelete) {
        this.usersToDelete = usersToDelete;
    }

    public boolean isRestrictedAdmin(){
        if(adminAdfonicUser!=null && !CollectionUtils.isEmpty(adminAdfonicUser.getRoles())){
            for (AdminRole r : adminAdfonicUser.getRoles()){
                if(r.getName().equals("RestrictedAdmin")){
                    return true;
                }
            }
        }
        return false; 
    }
    
    public List<User> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<User> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Transactional Units
    //------------------------------------------------------------------------------------------------------------------
    public void load() {
        this.users = getUserManager().getAllAdfonicUsers(new Sorting(SortOrder.asc("status"), SortOrder.asc("loginName")));
        for(AdfonicUser user : users){
            for(AdminRole role : user.getRoles()){
                role.getName();
            }
            for(User u : user.getUsers()){
                u.getEmail();
            }
        }
        this.adminRoles = getUserManager().getAllAdminRoles();
    }
}
