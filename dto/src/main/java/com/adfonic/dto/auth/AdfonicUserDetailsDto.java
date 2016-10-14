package com.adfonic.dto.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdto.annotation.DTOConstructor;
import org.jdto.annotation.Source;
import org.jdto.annotation.Sources;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.adfonic.dto.merger.CollectionValueMerger;

/**
 * This <tt>AdfonicUserDetailsDto</tt> is the implementation for Spring
 * Security. This is returned by the our implementation of
 * <tt>UserDetailsService</tt>
 *
 * We use jDTO to populate this DTO from our User domain object.
 *
 * @author antonysohal
 */
public class AdfonicUserDetailsDto implements UserDetails {

    private static final long serialVersionUID = 1L;

    protected String username;

    protected String password;

    protected String status;

    protected List<String> roles;

    protected List<GrantedAuthority> authorities;

    @DTOConstructor
    public AdfonicUserDetailsDto(@Source(value = "email") String username, @Source(value = "password") String password, @Sources(value = { @Source("roles"),
            @Source("company.roles") }, merger = CollectionValueMerger.class, mergerParam = "name") List<String> roles, @Source(value = "status") String status) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.status = status;
    }

    /**
     * @return the roles
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !("DISABLED".equalsIgnoreCase(status) || "UNVERIFIED".equalsIgnoreCase(status));
    }

    /**
     * Returns the Authorities for this user, using Adfonic Role Names from both
     * the User and Company roles.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList<GrantedAuthority>();
            for (String roleName : roles) {
                authorities.add(new SimpleGrantedAuthority(roleName));
            }
        }
        return authorities;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AdfonicUserDetailsDto [username=");
        builder.append(username);
        builder.append(", password=");
        builder.append(password);
        builder.append(", status=");
        builder.append(status);
        builder.append(", roles=");
        builder.append(roles);
        builder.append(", authorities=");
        builder.append(authorities);
        builder.append(", isAccountNonExpired()=");
        builder.append(isAccountNonExpired());
        builder.append(", isAccountNonLocked()=");
        builder.append(isAccountNonLocked());
        builder.append(", isCredentialsNonExpired()=");
        builder.append(isCredentialsNonExpired());
        builder.append(", isEnabled()=");
        builder.append(isEnabled());
        builder.append("]");
        return builder.toString();
    }

}
