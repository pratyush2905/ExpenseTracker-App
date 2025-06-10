package org.example.service;

import org.example.entities.UserInfo;
import org.example.entities.UserRoles;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails extends UserInfo implements UserDetails {

    private String username,password;
    Collection<? extends GrantedAuthority>authorities;

    public CustomUserDetails(UserInfo obj){
        this.username = obj.getUsername();
        this.password = obj.getPassword();

        List<GrantedAuthority>auths = new ArrayList<>();
        for(UserRoles role: obj.getRoles()){
            auths.add(new SimpleGrantedAuthority(role.getName().toUpperCase()));
        }

        this.authorities=auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return true;
    }
}
