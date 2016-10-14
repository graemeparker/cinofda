package com.adfonic.beans;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.User;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;

@RequestScoped
@ManagedBean(name="userQuery")
public class UserQueryBean extends BaseBean {
    private String search;
    private List<String> userEmails = new ArrayList<String>();

    /*
     * for auto-complete user list
     *
     * limit 50
     */
    public void doQuery() {
        if (StringUtils.isNotBlank(search)) {
            List<User> users = getUserManager().getAllUsersForEmailLike(
                    search,
                    new Pagination(0,50,
                    new Sorting(SortOrder.asc("email"))));
            for (User u : users) {
                userEmails.add(u.getEmail());
            }
        }
    }

    public List<String> getUserEmails() { return userEmails; }

    public void setSearch(String search) { this.search = search; }
}
